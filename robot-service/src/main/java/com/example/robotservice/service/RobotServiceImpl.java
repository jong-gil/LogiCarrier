package com.example.robotservice.service;

import com.example.robotservice.Repoistory.*;
import com.example.robotservice.dto.*;
import com.example.robotservice.handler.RobotHandler;
import com.example.robotservice.entity.*;
import com.example.robotservice.massagequeue.KafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {
    private final Environment env;
    private final ShelfRepository shelfRepository;
    private final PickerRepository pickerRepository;
    private final ShelfStockRepository stockRepository;
    private final RedisRepository redisRepository;
    private final RedisHashRepository redisHashRepository;
    private final RobotHandler robotHandler;
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private Set<String> isUsed;

    public static int[][] deltas = {
            {1, 0, -1, 0},
            {0, 1, 0, -1}
    };

    @Override
    public boolean turn() throws InterruptedException {
        boolean result = false;
        ObjectMapper objectMapper = new ObjectMapper();
        turnLock();
        try {
            Long turn = objectMapper.readValue(redisRepository.get("turn"), Long.class);
            turn++;

            redisRepository.set("turn", objectMapper.writeValueAsString(turn));
            log.info("turn increase!" + turn);
            result = true;
        } catch (Exception e) {
        }finally {
            turnUnlock();
        }
        return result;
    }
    @Override
    public void pick() {
        bitLock();
        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");
        int ablePicker = 0;                                                //redis에 캐쉬된 가동가능한 picker찾기
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '0' && progressBit.charAt(i) == '0') {
                ablePicker++;
            }
        }
        for (int i = 0; i < ablePicker * 3; i++) {
            kafkaProducer.requestOrderInfo();
        }
        bitUnlock();
    }
    @Override
    public void push() {
        bitLock();
        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");
        int ableWorker = 0;                                                //redis에 캐쉬된 가동가능한 picker찾기
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '1' && progressBit.charAt(i) == '0') {
                ableWorker++;
            }
        }

        for (int i = 0; i < ableWorker * 3; i++) {
            kafkaProducer.requestPushInfo();
        }
        bitUnlock();
    }
    @Override
    public void pickStart() {
        turnLock();
        bitLock();
        fieldLock();
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();

        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");
        int ablePicker = 0;
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '0' && progressBit.charAt(i) == '0') {
                ablePicker++;
            }
        }
        int disable = 0;
        for (int i = 0; i < ablePicker; i++) {
            if (disable > ablePicker * 2) {
                break;
            }
            String leftPop = listOperations.leftPop("orderDeque");
            try {
                Payload payload = objectMapper.readValue(leftPop, Payload.class);
                if (!find(payload)) {
                    listOperations.rightPush("orderDeque", leftPop);
                    i--;
                    disable++;
                }
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        fieldUnlock();
        bitUnlock();
        turnUnlock();
    }
    @Override
    public void pushStart() {
        turnLock();
        bitLock();
        fieldLock();
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        ObjectMapper objectMapper = new ObjectMapper();

        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");
        int ableWorker = 0;
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '1' && progressBit.charAt(i) == '0'){
                ableWorker++;
            }
        }
        int disable = 0;
        for (int i = 0; i < ableWorker; i++) {
            if( disable > ableWorker * 2){
                break;
            }
            try{

                String leftPop = listOperations.leftPop("pusherDeque");
                Payload payload = objectMapper.readValue(leftPop, Payload.class);
                if(! findSpace(payload)){
                    listOperations.rightPush("orderDeque", leftPop);
                    i --;
                    disable++;
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        fieldUnlock();
        bitUnlock();
        turnUnlock();
    }
    @Override
    public Boolean findSpace(Payload payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Long turn = 0L;

        turn = objectMapper.readValue(redisRepository.get("turn"), Long.class);

        for (ResponseItem responseItem : payload.getResponseItemList()) {
            responseItem.setId(0L);                                     //빈공간 찾기
        }
        List<CandidateDto> candidateDtoList = new ArrayList<>();
        HashMap<Long, ArrayDeque<CandidateDto>> selected = new HashMap<>();  //stockId별로 고른 stock
        HashMap<Long, ArrayList<Integer>> request = new HashMap<>();  // 몇개 필요, 몇개 골랐는지 처음 픽하기 위해
        int minCost = 100000;
        HashMap<Long, Integer> selectHashMap = new HashMap<>();//해당선반 몇번 픽됬는지
        HashMap<Long, Pick> pickHashMap = new HashMap<>(); // 해당선반에서 몇번 스톡을 몇개 꺼내야하는지

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        //stockList를 돌면서 후보군 조회
        for (ResponseItem item : payload.getResponseItemList()) {
            //몇개가 필요한지 몇개 골란는지 hashmap에 추가
            request.put(item.getId(), new ArrayList<>());
            request.get(item.getId()).add(item.getQty());
            request.get(item.getId()).add(0);

            selected.put(item.getId(), new ArrayDeque<>());

            List<Object[]> res = stockRepository.findCandidate(item.getId());
            res.forEach(obj -> {
                CandidateDto candidateDto = new CandidateDto().fromObj(obj);
                candidateDtoList.add(candidateDto);
            });
            //db에서 조회한 stock의 수가 필요한수보다 적다면 중단
            if (item.getQty() > res.size()) {
                return false;
            }
        }

        //푸셔위치 받아오기
        request.put(0L, new ArrayList<>());
        request.get(0L).add(1);
        request.get(0L).add(0);

        selected.put(0L, new ArrayDeque<>());
        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");         //redis에 캐쉬된 가동가능한 picker찾기
        int pusherCnt = 0;
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '1' && progressBit.charAt(i) == '0') {
                Picker picker = pickerRepository.findById(Long.valueOf(i))
                        .orElseThrow(() -> new NoSuchElementException("pusher not exist!"));
                CandidateDto candidateDto = CandidateDto.build(0L,
                        100000 + picker.getPickerId(),
                        picker.getX(),
                        picker.getY()
                );
                candidateDtoList.add(candidateDto);
                pusherCnt ++;
            }
        }


        //물건을 받을수 있는 피커가 없다면 false 반환
        if (pusherCnt == 0) {
            return false;
        }


        int index = 0;
        //일단 넣어요
        for (CandidateDto candidateDto : candidateDtoList) {
            Long stockId = candidateDto.getStockId();
            if (selected.get(stockId).size() < request.get(stockId).get(0)) {
                selected.get(stockId).add(candidateDto);
                //deque에 넣을떄 몇번째인지 저장
                request.get(stockId).set(1, request.get(stockId).get(1) + 1);
                if (selectHashMap.containsKey(candidateDto.getId())) {
                    selectHashMap.put(candidateDto.getId(), index);
                } else {
                    selectHashMap.put(candidateDto.getId(), 1);
                }
                index++;
            }
        }
        //가득 차면 거리 측정
        int pickerY = selected.get(0L).peek().getY();
        CalculateResultDto res = check(minCost, selected);
        int distance = res.getDistance();
        minCost = res.getMeanCost();
        if (res.isChange()) {
            pickHashMap = res.getPickHash();
        }

        //deque를 채운 index보다 작으면 패스
        for (int i = 0; i < candidateDtoList.size(); i++) {
            CandidateDto candidateDto = candidateDtoList.get(i);
            long stockId = candidateDto.getStockId();
            if (request.get(stockId).get(1) < i) {
                //하나 뺀다, 해당 선반 몇번 골랐는지 변경
                ArrayDeque<CandidateDto> selectedItem = selected.get(stockId);
                CandidateDto polled = selectedItem.poll();
                //제거하는 선반이 여러번 선택되었다면 --, 아니면 제거
                if (selectHashMap.get(polled.getId()) == 1) {
                    selectHashMap.remove(polled.getId());
                } else {
                    selectHashMap.put(polled.getId(), selectHashMap.get(polled.getId()) - 1);
                }
                //deque에 추가
                selectedItem.add(candidateDto);
                //피커가 아니면 selectHashMap 갱신, 거리 갱신
                if (stockId != 0L) {
                    if (selectHashMap.containsKey(candidateDto.getId())) {
                        selectHashMap.put(candidateDto.getId(), selectHashMap.get(candidateDto.getId()) + 1);
                    } else {
                        selectHashMap.put(candidateDto.getId(), 1);
                        distance += -Math.abs(pickerY - polled.getY()) + Math.abs(pickerY - candidateDto.getY());
                        //민코스트가 작다면 해당 정보 저장하기
                        minCost = Math.min(distance, minCost);
                    }
                } else {
                    pickerY = candidateDto.getX();
                    selectHashMap.put(candidateDto.getId(), 1);
                    res = check(minCost, selected);
                    distance = res.getDistance();
                    minCost = res.getMeanCost();
                    if (res.isChange()) {
                        pickHashMap = res.getPickHash();
                    }
                }
            }
        }

        log.info("mincost: " + minCost);
        // 이동해야하는 선반 찾고 이동해야하는 위치와 dfsㄲㄲ
        int endX = 0;
        int endY = 0;
        long pickerId = 0L;
        for (Long key : pickHashMap.keySet()) {
            if (key >= 1000) {
                pickerId = key;
                break;
            }
        }

        for (CandidateDto candidateDto : candidateDtoList) {
            if (candidateDto.getId() == pickerId) {
                endX = candidateDto.getX();
                endY = candidateDto.getY();
                break;
            }
        }

        for (Long key : pickHashMap.keySet()) {
            for (CandidateDto candidateDto : candidateDtoList) {
                if (candidateDto.getId() == key && candidateDto.getStockId() != 0L) {
                    int x = candidateDto.getX();
                    int y = candidateDto.getY();
                    Shelf shelf = shelfRepository.findById(candidateDto.getId()).orElseThrow(); // shelf 할당
                    shelf.setStatus(true);
                    shelfRepository.save(shelf);


                    send(new int[]{x, y}, new int[]{endX, endY}, turn, candidateDto.getId(), payload, "pusherRes"); // 최단거리, 턴 계산
                    break;
                }
            }
        }

        for (String key : isUsed) {                                                                //schedule 정리
            if (!key.startsWith("8")) {
                continue;
            }

            Road road = objectMapper.readValue(redisHashRepository.get("roadHash", key), Road.class);
            long biggest = 0L;
            for (long[] schedule : road.getSchedule()) {
                if (schedule[0] > biggest) biggest = schedule[0];
            }
            ArrayList<long[]> newSchedule = new ArrayList<>();
            newSchedule.add(new long[]{0L, biggest});
            road.setSchedule(newSchedule);
            redisHashRepository.put("roadHash", key, objectMapper.writeValueAsString(road));
        }
        isUsed = new HashSet<>();

        return true;
    }
    @Override
    public Boolean find(Payload payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Long turn;
        turn = objectMapper.readValue(redisRepository.get("turn"), Long.class);


        List<CandidateDto> candidateDtoList = new ArrayList<>();
        HashMap<Long, ArrayDeque<CandidateDto>> selected = new HashMap<>();  //stockId별로 고른 stock
        HashMap<Long, ArrayList<Integer>> request = new HashMap<>();  // 몇개 필요, 몇개 골랐는지 처음 픽하기 위해
        int minCost = 100000;
        HashMap<Long, Integer> selectHashMap = new HashMap<>();//해당선반 몇번 픽됬는지
        HashMap<Long, Pick> pickHashMap = new HashMap<>(); // 해당선반에서 몇번 스톡을 몇개 꺼내야하는지

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        //stockList를 돌면서 후보군 조회
        for (ResponseItem item : payload.getResponseItemList()) {
            //몇개가 필요한지 몇개 골란는지 hashmap에 추가
            request.put(item.getId(), new ArrayList<>());
            request.get(item.getId()).add(item.getQty());
            request.get(item.getId()).add(0);

            selected.put(item.getId(), new ArrayDeque<>());

            List<Object[]> res = stockRepository.findCandidate(item.getId());
            res.forEach(obj -> {
                CandidateDto candidateDto = new CandidateDto().fromObj(obj);
                candidateDtoList.add(candidateDto);
            });
            //db에서 조회한 stock의 수가 필요한수보다 적다면 중단
            if (item.getQty() > res.size()) {
                return false;
            }
        }

        //피커위치 받아오기
        request.put(0L, new ArrayList<>());
        request.get(0L).add(1);
        request.get(0L).add(0);

        selected.put(0L, new ArrayDeque<>());
        String workerBit = redisRepository.get("workerBit");
        String progressBit = redisRepository.get("progressBit");         //redis에 캐쉬된 가동가능한 picker찾기
        int pickerCnt = 0;
        for (int i = 0; i < workerBit.length(); i++) {
            if (workerBit.charAt(i) == '0' && progressBit.charAt(i) == '0') {
                Picker picker = pickerRepository.findById(Long.valueOf(i))
                        .orElseThrow(() -> new NoSuchElementException("picker not exist!"));
                CandidateDto candidateDto = CandidateDto.build(0L,
                        100000 + picker.getPickerId(),
                        picker.getX(),
                        picker.getY()
                );
                candidateDtoList.add(candidateDto);
                pickerCnt ++;
            }
        }


        //물건을 받을수 있는 피커가 없다면 false 반환
        if (pickerCnt == 0) {
            return false;
        }


        int index = 0;
        //일단 넣어요 1 1 1 2 2 에서 1 1 2 뽑는다 하면  1 1 1 2 읽어야 꽉차서 안되요 없어 못채우면? 블가능함
        for (CandidateDto candidateDto : candidateDtoList) {
            Long stockId = candidateDto.getStockId();
            if (selected.get(stockId).size() < request.get(stockId).get(0)) {
                selected.get(stockId).add(candidateDto);
                //deque에 넣을떄 몇번째인지 저장
                request.get(stockId).set(1, request.get(stockId).get(1) + 1);
                if (selectHashMap.containsKey(candidateDto.getId())) {
                    selectHashMap.put(candidateDto.getId(), index);
                } else {
                    selectHashMap.put(candidateDto.getId(), 1);
                }
                index++;
            }
        }
        //가득 차면 거리 측정
        int pickerY = selected.get(0L).peek().getY();
        CalculateResultDto res = check(minCost, selected);
        int distance = res.getDistance();
        minCost = res.getMeanCost();
        if (res.isChange()) {
            pickHashMap = res.getPickHash();
        }

        //deque를 채운 index보다 작으면 패스
        for (int i = 0; i < candidateDtoList.size(); i++) {
            CandidateDto candidateDto = candidateDtoList.get(i);
            long stockId = candidateDto.getStockId();
            if (request.get(stockId).get(1) < i) {
                //하나 뺀다, 해당 선반 몇번 골랐는지 변경
                ArrayDeque<CandidateDto> selectedItem = selected.get(stockId);
                CandidateDto polled = selectedItem.poll();
                //제거하는 선반이 여러번 선택되었다면 --, 아니면 제거
                if (selectHashMap.get(polled.getId()) == 1) {
                    selectHashMap.remove(polled.getId());
                } else {
                    selectHashMap.put(polled.getId(), selectHashMap.get(polled.getId()) - 1);
                }
                //deque에 추가
                selectedItem.add(candidateDto);
                //피커가 아니면 selectHashMap 갱신, 거리 갱신
                if (stockId != 0L) {
                    if (selectHashMap.containsKey(candidateDto.getId())) {
                        selectHashMap.put(candidateDto.getId(), selectHashMap.get(candidateDto.getId()) + 1);
                    } else {
                        selectHashMap.put(candidateDto.getId(), 1);
                        distance += -Math.abs(pickerY - polled.getY()) + Math.abs(pickerY - candidateDto.getY());
                        //민코스트가 작다면 해당 정보 저장하기
                        minCost = Math.min(distance, minCost);
                    }
                } else {
                    pickerY = candidateDto.getX();
                    selectHashMap.put(candidateDto.getId(), 1);
                    res = check(minCost, selected);
                    distance = res.getDistance();
                    minCost = res.getMeanCost();
                    if (res.isChange()) {
                        pickHashMap = res.getPickHash();
                    }
                }
            }
        }

        log.info("mincost: " + minCost);
        // 이동해야하는 선반 찾고 이동해야하는 위치와 dfsㄲㄲ
        int endX = 0;
        int endY = 0;
        long pickerId = 0L;
        for (Long key : pickHashMap.keySet()) {
            if (key >= 1000) {
                pickerId = key;
                break;
            }
        }

        for (CandidateDto candidateDto : candidateDtoList) {
            if (candidateDto.getId() == pickerId) {
                endX = candidateDto.getX();
                endY = candidateDto.getY();
                break;
            }
        }

        for (Long key : pickHashMap.keySet()) {
            for (CandidateDto candidateDto : candidateDtoList) {
                if (candidateDto.getId().equals(key) && candidateDto.getStockId() != 0L) {
                    int x = candidateDto.getX();
                    int y = candidateDto.getY();
                    Shelf shelf = shelfRepository.findById(candidateDto.getId()).orElseThrow(); // shelf 할당
                    shelf.setStatus(true);
                    shelfRepository.save(shelf);


                    send(new int[]{x, y}, new int[]{endX, endY}, turn, candidateDto.getId(), payload, "pickerRes"); // 최단거리, 턴 계산
                    break;
                }
            }
        }
        for (String key : isUsed) {                                                                //schedule 정리
            if (!key.startsWith("8")) {
                continue;
            }
            Road road = objectMapper.readValue(redisHashRepository.get("roadHash", key), Road.class);
            long biggest = 0L;
            for (long[] schedule : road.getSchedule()) {
                if (schedule[0] > biggest) biggest = schedule[0];
            }
            ArrayList<long[]> newSchedule = new ArrayList<>();
            newSchedule.add(new long[]{0L, biggest});
            road.setSchedule(newSchedule);
            redisHashRepository.put("roadHash", key, objectMapper.writeValueAsString(road));

        }
        isUsed = new HashSet<>();

        return true;
    }
    @Override
    public void receive(int[] start, Long shelfId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Long turn = 0L;
        while (redisRepository.lock("turnLock")) {
            Thread.sleep(100);
        }
        turn = objectMapper.readValue(redisRepository.get("turn"), Long.class);

        Shelf shelf = shelfRepository.findById(shelfId).orElseThrow();
        // 선반과 상호작용할 위치 찾기
        int[] pick = new int[]{shelf.getX(), shelf.getY()};
        int[] pickField = new int[2];
        int lengthY = Integer.valueOf(env.getProperty("field.y"));
        if (pick[1] >= 1 && redisHashRepository.hasKey("roadHash", redisHashRepository.get("field", String.valueOf(pick[0]) + "." + String.valueOf(pick[1] - 1)))) {
            pickField = new int[]{pick[0], pick[1] - 1};
        } else if (pick[1] + 1 < lengthY && redisHashRepository.hasKey("roadHash", redisHashRepository.get("field", String.valueOf(pick[0]) + "." + String.valueOf(pick[1] + 1)))) {        //yml에서 길이 받기
            pickField = new int[]{pick[0], pick[1] + 1};
        }
        Payload payload = new Payload();
        int[] end = new int[]{0, pickField[1]};
        dfs(start, pickField, end, turn, pick, shelfId, payload, "");           //반환 로직에선 카프카 안써요
    }
    public static CalculateResultDto check(int minCost, HashMap<Long, ArrayDeque<CandidateDto>> selected) throws Exception {
        boolean ischange = false;
        int pickerX = selected.get(0L).peek().getX();
        int distance = 0;
        HashMap<Long, Pick> pickHashMap = new HashMap<>();

        for (Deque<CandidateDto> deque : selected.values()) {
            Iterator<CandidateDto> iterator = deque.iterator();
            while (iterator.hasNext()) {
                CandidateDto now = iterator.next();

                if (!pickHashMap.containsKey(now.getId())) {
                    HashMap<Long, Integer> stockInfo = new HashMap<>();
                    stockInfo.put(now.getStockId(), 1);
                    Pick pick = new Pick(stockInfo);
                    pickHashMap.put(now.getId(), pick);
                    distance += Math.abs(pickerX - now.getX());
                } else {
                    Pick pick = pickHashMap.get(now.getId());
                    HashMap<Long, Integer> stockInfo = pick.getStockInfo();
                    if (stockInfo.containsKey(now.getStockId())) {
                        int qty = stockInfo.get(now.getStockId());
                        stockInfo.put(now.getStockId(), qty + 1);
                    } else {
                        stockInfo.put(now.getStockId(), 1);
                    }
                }
            }
        }
        if (minCost > distance) {
            minCost = distance;
            ischange = true;

        }

        return new CalculateResultDto(distance, minCost, ischange, pickHashMap);
    }
    public void send(int[] pick, int[] end, long turn, long shelfId, Payload payload, String topic) throws Exception {
        // 선반과 상호작용할 위치 찾기
        int[] pickField = new int[2];
        int[] start = new int[2];
        int lengthY = Integer.valueOf(env.getProperty("field.y"));
        if (pick[1] >= 1 && redisHashRepository.hasKey("roadHash", redisHashRepository.get("field", String.valueOf(pick[0]) + "." + String.valueOf(pick[1] - 1)))) {
            pickField = new int[]{pick[0], pick[1] - 1};
        } else if (pick[1] + 1 < lengthY && redisHashRepository.hasKey("roadHash", redisHashRepository.get("field", String.valueOf(pick[0]) + "." + String.valueOf(pick[1] + 1)))) {
            pickField = new int[]{pick[0], pick[1] + 1};
        }
        start = new int[]{0, pickField[1]};
        dfs(start, pickField, end, turn, pick, shelfId, payload, topic);
    }
    public void dfs(int[] start, int[] pick, int[] end, long turn, int[] shelf, long shelfId, Payload payload, String topic) throws Exception {
        long[][] visit = new long[9][13];
        Stack<int[]> ans = new Stack<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                visit[i][j] = Long.MAX_VALUE;
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();

        //결과
        ArrayList<String> ansKeyList = new ArrayList<>();
        ArrayList<Long> ansSizeList = new ArrayList<>();
        ArrayList<Boolean> ansDirectList = new ArrayList<>();
        ArrayList<Long> ansStartList = new ArrayList<>();
        Stack<int[]> ansStack = new Stack<>();
        long fastest = 0;

        Stack<int[]> stack = new Stack<>();
        stack.add(new int[]{start[0], start[1], 0});
        visit[start[0]][start[1]] = 1;
        while (!stack.isEmpty()) {
            int[] now = stack.peek();
            int x = now[0];
            int y = now[1];
            int direction = now[2];
            long time = visit[x][y];
            //찾으면 가장 빠른 출발 시간 찾기
            if (now[0] == end[0] && now[1] == end[1] && visit[pick[0]][pick[1]] != Long.MAX_VALUE) {
                //사용경로키 사이즈 방향 진입시간 저장
                ArrayList<String> keyList = new ArrayList<>();
                ArrayList<Long> sizeList = new ArrayList<>();
                ArrayList<Boolean> directList = new ArrayList<>();
                ArrayList<Long> startList = new ArrayList<>();

                //사용 경로의 키, 사이즈, 방향성 구하기
                int i = 0;
                while (i < stack.size()) {
                    boolean isCorver = false;
                    int nx = stack.get(i)[0];
                    int ny = stack.get(i)[1];
                    int nDirection = stack.get(i)[2];
                    StringBuilder sb = new StringBuilder();
                    String key = redisHashRepository.get("field", sb.append(nx).append(".").append(ny).toString());
                    keyList.add(key);
                    Road road = objectMapper.readValue(redisHashRepository.get("roadHash", keyList.get(keyList.size() - 1)), Road.class);
                    if (nDirection % 2 == 0 && !road.isCorner()) {
                        directList.add(true);
                    } else {
                        directList.add(false);
                    }
                    sizeList.add(1L);
                    long startTime = visit[nx][ny];
                    if (i == 0) {
                        startTime = 1L;
                    } else {
                        startTime = startList.get(startList.size() - 1) + sizeList.get(sizeList.size() - 2);
                    }
                    while (i < stack.size() && key.equals(redisHashRepository.get("field", new StringBuilder().append(stack.get(i)[0]).append(".").append(stack.get(i)[1]).toString()))) {
                        i++;
                        isCorver = true;
                    }
                    if (isCorver) {
                        i--;
                        long endTime = visit[stack.get(i)[0]][stack.get(i)[1]];
                        if (i != 0) {
                            sizeList.set(sizeList.size() - 1, endTime - startList.get(startList.size() - 1) - sizeList.get(sizeList.size() - 2) + 1);
                        } else {
                            sizeList.set(sizeList.size() - 1, endTime);
                        }
                    }
                    if (road.isCorner()) {
                        startList.add(startTime - sizeList.get(sizeList.size() - 1) + 1);
                    } else {
                        startList.add(startTime);
                    }

                    i++;
                }

                //안되는 시간 구하기
                ArrayList<long[]> disableTime = new ArrayList<>();
                for (int j = keyList.size() - 1; j >= 0; j--) {
                    long size = sizeList.get(j);
                    String key = keyList.get(j);
                    Road road = objectMapper.readValue(redisHashRepository.get("roadHash", key), Road.class);
                    ArrayList<long[]> schedule = road.getSchedule();

                    //이전 값 갱신
                    for (int k = 0; k < disableTime.size(); k++) {
                        disableTime.get(k)[0] -= size;
                        disableTime.get(k)[1] -= size;
                    }
                    //불가능한 시간 추가
                    if (directList.get(j)) {//정방향이면
                        disableTime.addAll(schedule);
                    } else { //역방향일시
                        if (road.isCorner()) {
                            for (long[] disable : schedule) {
                                disableTime.add(new long[]{disable[0] - size + 1, disable[1]});
                            }
                        } else {
                            for (long[] disable : schedule) {
                                disableTime.add(new long[]{disable[0] - size, disable[1] + size});
                            }
                        }
                    }

                }
                //가장 빠른 되는시간 계산 및 결과 저장
                disableTime.add(new long[]{0, turn});
                disableTime.sort(new Comparator<long[]>() {
                    @Override
                    public int compare(long[] o1, long[] o2) {
                        return (int) (o1[0] - o2[0]); // 두 시간의 차가 int범위 내야한다!
                    }
                });
                boolean isChange = false;
                for (long[] disable : disableTime) {
                    if (fastest < disable[0]) {
                        break;
                    } else if (fastest < disable[1] + 1) {
                        fastest = disable[1] + 1;
                        isChange = true;
                    }
                }
                if (isChange) {
                    ansKeyList = (ArrayList<String>) keyList.clone();
                    ansSizeList = (ArrayList<Long>) sizeList.clone();
                    ansDirectList = (ArrayList<Boolean>) directList.clone();
                    ansStartList = (ArrayList<Long>) startList.clone();
                    for (int[] value : stack) {
                        ansStack.add(new int[]{value[0], value[1], value[2]});
                    }
                }
                stack.pop();
                continue;
            }

            boolean logic = true;
            int lengthY = Integer.valueOf(env.getProperty("field.y"));
            int lengthX = Integer.valueOf(env.getProperty("field.x"));

            for (int i = 0; i < 4; i++) {
                int dx = x + deltas[0][(i + direction) % 4];
                int dy = y + deltas[1][(i + direction) % 4];
                String fieldKey = redisHashRepository.get("field", new StringBuilder().append(dx).append(".").append(dy).toString());
                long cost = i % 2 + 1;
                if (dx == pick[0] && dy == pick[1]) cost += 7; //선반 꺼내오기
                if (dx == end[0] && dy == end[1] && !(direction % 2 == 0)) cost++; //마지막에 나가는 방향으로 바꾸기
                if (0 <= dx && dx < lengthX && 0 <= dy && dy < lengthY && !fieldKey.equals("") && visit[dx][dy] > time + cost && time + cost <= visit[end[0]][end[1]]) {
                    stack.add(new int[]{dx, dy, (i + direction) % 4});
                    visit[dx][dy] = visit[x][y] + cost;
                    logic = false;
                    break;
                }
            }
            if (logic) {
                stack.pop();
            }
        }
        // 안되는 시간 갱신
        for (int j = ansKeyList.size() - 1; j >= 0; j--) {
            long size = ansSizeList.get(j);
            long time = ansStartList.get(j);
            String key = ansKeyList.get(j);
            String fieldKey = redisHashRepository.get("field", new StringBuilder().append(pick[0]).append(".").append(pick[1]).toString());
            Road road = objectMapper.readValue(redisHashRepository.get("roadHash", key), Road.class);
            //불가능한 시간 추가
            if (ansDirectList.get(j) && !fieldKey.equals(key)) {//정방향고 선반을 픽업하지 않으면
                road.getSchedule().add(new long[]{time + fastest - 1, time + fastest - 1});
            } else { //역방향일시
                if (road.isCorner()) {
                    road.getSchedule().add(new long[]{time + fastest - 1, time + size - 1 + fastest - 1});
                    System.out.println("asd");
                } else {
                    road.getSchedule().add(new long[]{time - size + fastest - 1, time + size + fastest - 1});
                }
            }
            redisHashRepository.put("roadHash", key, objectMapper.writeValueAsString(road));
            isUsed.add(key); //업데이트한 키 추가 -> 스케줄 정리하기 위해 -> 주문이 섞이는것 방지
        }

        StringBuilder sb = new StringBuilder();         //경로 출력
        Robot robot = new Robot();

        if (payload.getId() != null) {                   //출발지에 대기중인 로봇 스택 읽기
            String fieldKey = redisHashRepository.get("field", new StringBuilder().append(start[0]).append(".").append(start[1]).toString());
            RobotStack robotStack = objectMapper.
                    readValue( redisHashRepository.get("robotStack", fieldKey), RobotStack.class);

            Stack<String> robotIdStack = robotStack.getRobotIdStack();
            String id = robotIdStack.pop();
            robot = objectMapper.readValue(redisHashRepository.get("robot", id), Robot.class);

            //stack의 최대 길이는 5
            for (int k = 0; k < 5 - robotIdStack.size() - 1; k++) {
                sb.append('U');
            }
        }
        int direction = ansStack.get(0)[2];
        int i = 1;
        while (i < ansStack.size()) {
            int[] now = ansStack.get(i);
            if (now[2] == direction) {
                sb.append('U');
            } else if (now[2] == (direction + 1) % 4) {
                sb.append('R');
                direction = 1;
            } else {
                sb.append('L');
                direction = 3;
            }
            if (now[0] == pick[0] && now[1] == pick[1]) {
                for (int j = 0; j < 4; j++) {
                    int dx = pick[0] + deltas[0][(j + direction) % 4];
                    int dy = pick[1] + deltas[1][(j + direction) % 4];
                    if (Arrays.equals(shelf, new int[]{dx, dy})) {
                        if (j == 1) {
                            sb.append("RUTRRUR");
                        } else {
                            sb.append("LUTLLUL");
                        }
                    }
                }
            }
            i++;
        }
        if (payload.getId() == null) {                        //반환 로직이면 스택 길이 만큼 더 들어가고 180도 회전
            String fieldKey = redisHashRepository.get("field", new StringBuilder().append(start[0]).append(".").append(start[1]).toString());
            RobotStack robotStack = objectMapper.
                    readValue(redisHashRepository.get("robotStack", fieldKey), RobotStack.class);

            Stack<String> robotIdStack = robotStack.getRobotIdStack();
            //stack의 최대 길이는 5
            for (int k = 0; k < 5 - robotIdStack.size() - 1; k++) {
                sb.append('U');
            }
            sb.append("RR");
        }
        //압축
        StringBuilder pressured = new StringBuilder();
        char[] unPressured = sb.toString().toCharArray();
        int j = 1;
        pressured.append(unPressured[0]);
        pressured.append("1 ");                            //같은 명령이 10번 되면 안된다 귀찮아요
        while (j < unPressured.length) {
            if (pressured.charAt(pressured.length() - 3) != unPressured[j]) {
                pressured.append(unPressured[j]);
                pressured.append("1 ");
                j++;
                continue;
            }
            int cnt = Character.getNumericValue(pressured.charAt(pressured.length() - 2));
            while (j < unPressured.length && pressured.charAt(pressured.length() - 3) == unPressured[j]) {
                cnt++;
                j++;
            }
            pressured.deleteCharAt(pressured.length() - 1);
            pressured.deleteCharAt(pressured.length() - 1);
            pressured.append(cnt);
            pressured.append(" ");
        }
        log.info(pressured.toString());

        if (payload.getId() != null) {                            //반환 로직이 아니면 worker-service로 결과 전송
            WorkerRes workerRes = WorkerRes.builder()
                    .robotId(robot.getRobotId())
                    .shelfId(shelfId)
                    .orderId(payload.getId())
                    .pickerId("1")                              //redis로 받는 로직 추가 필요!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    .turn(fastest + visit[end[0]][end[1]])      //도착 시간
                    //.responseItemList()                       // 해당선반에서 꺼내는거
                    .build();
            kafkaProducer.toWorker(workerRes, topic);
        }

        robotHandler.sendCommand(robot.getRobotId(), pressured.toString(), robot.getPositionX(), robot.getPositionY(), shelfId, turn, fastest);//해당 로봇에게 메세지 전달

    }
    @Override
    public void turnLock(){
        while (redisRepository.lock("turnLock")) {
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }
        }
    }
    @Override
    public void turnUnlock(){
        redisRepository.unlock("turnLock");
    }
    @Override
    public void bitLock(){
        while(!redisRepository.lock("workerBitLock")){
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }
        }
        while(!redisRepository.lock("progressBitLock")){
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }
        }
    }
    @Override
    public void bitUnlock(){
        redisRepository.unlock("progressBit");
        redisRepository.unlock("workerBit");
    }
    @Override
    public void fieldLock(){
        while(!redisRepository.lock("roadHashLock")){
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }
        }
        while(!redisRepository.lock("fieldBitLock")){
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }
        }
    }
    @Override
    public void fieldUnlock(){
        redisRepository.unlock("fieldBitLock");
        redisRepository.unlock("roadHashLock");
    }
}

