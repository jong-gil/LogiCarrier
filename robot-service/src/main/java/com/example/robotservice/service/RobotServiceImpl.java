package com.example.robotservice.service;

import com.example.robotservice.dto.CalculateResultDto;
import com.example.robotservice.dto.CandidateDto;
import com.example.robotservice.dto.ItemRequestDto;
import com.example.robotservice.dto.Pick;
import com.example.robotservice.jpa.ShelfRepository;
import com.example.robotservice.jpa.ShelfStockRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService{
    private final ShelfRepository shelfRepository;
    private final ShelfStockRepository stockRepository;
   //어떤 선반이 몇반 골라졌는지
    @Override
    public HashMap<Long, Pick> find() {
        List<CandidateDto> candidateDtoList = new ArrayList<>();
        HashMap<Long, ArrayDeque<CandidateDto>> selected = new HashMap<>();  //stockId별로 고른 stock
        HashMap<Long, ArrayList<Integer>> request = new HashMap<>();  // 몇개 필요, 몇개 골랐는지 처음 픽하기 위해
        int minCost = 100000;
        HashMap<Long, Integer> selectHashMap = new HashMap<>();//해당선반 몇번 픽됬는지
        HashMap<Long, Pick> pickHashMap = new HashMap<>(); // 해당선반에서 몇번 스톡을 몇개 꺼내야하는지

        List<ItemRequestDto> stockList = new ArrayList<>();
        ItemRequestDto itemRequestDto1 = new ItemRequestDto();
        itemRequestDto1.setId(1L);
        itemRequestDto1.setQty(2);
        stockList.add(itemRequestDto1);
        ItemRequestDto itemRequestDto2 = new ItemRequestDto();
        itemRequestDto2.setId(2L);
        itemRequestDto2.setQty(1);
        stockList.add(itemRequestDto2);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        //stockList를 돌면서 후보군 조회
        for(ItemRequestDto item : stockList){
            //처음 나온 스톡아이디이면 해쉬맵에 추가
            if(! selected.containsKey(item.getId())){
                request.put(item.getId(), new ArrayList<>());
                request.get(item.getId()).add(item.getQty());
                request.get(item.getId()).add(0);


                selected.put(item.getId(), new ArrayDeque<>());
            }

            List<Object[]> res = stockRepository.findCandidate(item.getId());
            res.forEach(obj ->{
                CandidateDto candidateDto = new CandidateDto().fromObj(obj);
                candidateDtoList.add(candidateDto);
            });
        }

        //피커위치 카프카로 받아오기 candidatedto와 동일
        request.put(0L, new ArrayList<>());
        request.get(0L).add(1);
        request.get(0L).add(0);

        selected.put(0L, new ArrayDeque<>());

        CandidateDto candidateDto1 = new CandidateDto();
        candidateDto1.setX(candidateDtoList.get(0).getX());
        candidateDto1.setY(0);
        candidateDto1.setStockId(0L);
        candidateDto1.setId(0L);
        candidateDtoList.add(candidateDto1);

        CandidateDto candidateDto2 = new CandidateDto();
        candidateDto2.setX(candidateDtoList.get(candidateDtoList.size()-1).getX());
        candidateDto2.setY(0);
        candidateDto2.setStockId(0L);
        candidateDto2.setId(0L);
        candidateDtoList.add(candidateDto2);



        int index = 0;
        //일단 넣어요 1 1 1 2 2 에서 1 1 2 뽑는다 하면  1 1 1 2 읽어야 꽉차서 안되요
        for(CandidateDto candidateDto : candidateDtoList){
            Long id = candidateDto.getId();
            Long stockId = candidateDto.getStockId();
            if (selected.get(stockId).size() < request.get(stockId).get(0)){
                selected.get(stockId).add(candidateDto);
                //deque에 넣을떄 몇번째인지 저장
                request.get(stockId).set(1, request.get(stockId).get(1) + 1);
                if (selectHashMap.containsKey(candidateDto.getId())){
                    selectHashMap.put(candidateDto.getId(), index);
                }else{
                    selectHashMap.put(candidateDto.getId(), 1);
                }
                index ++;
            }
        }

        //가득 차면 거리 측정
        int pickerX = selected.get(0L).peek().getX();
        CalculateResultDto res = check(minCost, selected);
        int distance = res.getDistance();
        minCost = res.getMeanCost();
        if(res.isChange()){
            pickHashMap = res.getPickHash();
        }

        //deque를 채운 index보다 작으면 패스
        for(int i = 0; i < candidateDtoList.size(); i++){
            CandidateDto candidateDto = candidateDtoList.get(i);
            long stockId = candidateDto.getStockId();
            if (request.get(stockId).get(1) < i){
                //하나 뺀다, 해당 선반 몇번 골랐는지 변경
                ArrayDeque<CandidateDto> selectedItem = selected.get(stockId);
                CandidateDto polled = selectedItem.poll();
                //제거하는 선반이 여러번 선택되었다면 --, 아니면 제거

                if(selectHashMap.get(polled.getId()) == 1){
                    selectHashMap.remove(polled.getId());
                }else {
                    selectHashMap.put(polled.getId(), selectHashMap.get(polled.getId()) - 1);
                }
                //deque에 추가
                selectedItem.add(candidateDto);
                //피커가 아니면 selectHashMap 갱신, 거리 갱신
                if (stockId != 0L){
                    if (selectHashMap.containsKey(candidateDto.getId())){
                        selectHashMap.put(candidateDto.getId(), selectHashMap.get(candidateDto.getId()) + 1);
                    }else{
                        selectHashMap.put(candidateDto.getId(), 1);
                        distance += - Math.abs(pickerX - polled.getX()) + Math.abs(pickerX - candidateDto.getX());
                        //민코스트가 작다면 해당 정보 저장하기
                        minCost = Math.min(distance, minCost);
                    }
                }else{
                    selectHashMap.put(0L, 1);
                    res = check(minCost, selected);
                    distance = res.getDistance();
                    minCost = res.getMeanCost();
                    if(res.isChange()){
                        pickHashMap = res.getPickHash();
                    }
                }
            }
        }

        System.out.println("mincost: " + minCost);
        return pickHashMap;
    }

    public static CalculateResultDto check(int minCost, HashMap<Long, ArrayDeque<CandidateDto>> selected){
        ArrayList peakList = new ArrayList<>();
        boolean ischange = false;
        int pickerX = selected.get(0L).peek().getX();
        int distance = 0;
        HashSet<Long> isUsed = new HashSet<>();
        HashMap<Long, Pick> pickHashMap = new HashMap<>();

        for (Deque<CandidateDto> deque : selected.values()){
            Iterator<CandidateDto> iterator = deque.iterator();
            while (iterator.hasNext()){
                CandidateDto now = iterator.next();

                if (! pickHashMap.containsKey(now.getId())){
                    HashMap<Long, Integer> stockInfo = new HashMap<>();
                    stockInfo.put(now.getStockId(), 1);
                    Pick pick = new Pick(stockInfo);
                    pickHashMap.put(now.getId(), pick);
                    distance += Math.abs(pickerX - now.getX());
                }else {
                    Pick pick = pickHashMap.get(now.getId());
                    HashMap<Long, Integer> stockInfo = pick.getStockInfo();
                    if (stockInfo.containsKey(now.getStockId())){
                        int qty = stockInfo.get(now.getStockId());
                        stockInfo.put(now.getStockId(), qty + 1);
                    }else{
                        stockInfo.put(now.getStockId(), 1);
                    }
                }
            }
        }
        if (minCost > distance){
            minCost = distance;
            ischange = true;

        }
        CalculateResultDto calculateResultDto = new CalculateResultDto(distance, minCost, ischange, pickHashMap);

        return calculateResultDto;
    }


}

