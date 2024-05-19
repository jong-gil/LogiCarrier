### 프로젝트 소개

 입출고 자동화시스템을 구현하여 물류 작업 효율성을 높이는 데 초점을 맞추고 있습니다. 입고 과정에서는 빈 선반을 탐색하고 최단 거리의 선반을 찾아 새로운 물품을 배치합니다. 출고 과정에서는 요청된 물품이 있는 선반을 탐색하고 작업 가능한 출고 담당자와 선반의 위치를 조정하여 최적 경로를 계획합니다.

## 개발 기간

- 2023.07 ~2023.12

## 개발환경

- **Version** : Java 11
- **IDE** : IntelliJ
- **Framework** : SpringBoot 2.7.16
- **ORM** : Spring-Data-JPA

## 기술 스택

- **Server** : AWS EC2
- **DataBase** : MySQL, Redis
- **Message Queue**: Kafka
- **Library**: Spring-cloud, Spring Kafka Lombok, Spring-security, JWT,  Modelmapper, validation
- **Infra**: AWS S3, AWS codedeploy, Github Action, Docker

#### 아키텍처

![logiCarrier-architecture.jpg](C:\Users\bosung\Desktop\LogiCarrier\ReadMe\logiCarrier-architecture.jpg)

#### ERD

![321093489-5e68b296-7981-47db-a1ca-923f69b12f61.png](C:\Users\bosung\Desktop\LogiCarrier\ReadMe\321093489-5e68b296-7981-47db-a1ca-923f69b12f61.png)

#### 주요기능

1. 입고/출고 자동화
   
   - 입고 시
     
     - 빈 선반 탐색 (새로운 물건이 들어갈 수 있는 장소가 있는지)
     - 로봇 위치 및 선반 위치 탐지 후 최단 거리의 선반 조회
   
   - 출고 시
     
     - 요청된 물품이 들어있는 선반 중 입고 시간이 빠른 순서로 3배수로 후보군 탐색
     
     - 작업 가능한 출고 담당자와 후보 선반의 위치를 좌표로 정렬
     
     - 최단 거리의 선반 찾기

2. AGV(Automated Guided Vehicle) 운용
   
   - 로봇
     
     - ESP8266 모듈과 Arduino UNO, 적외선 센서와 DC모터로 구성
     
     - webSocket통신을 통해 서버와 정보 교환
     
     - 현재위치, 배터리 상태, 이동중인 선반 Id를 전송 
   
   - 서버
     
     - webSocket통신을 통해 서버와 정보 교환
     
     - 계획된 경로 전송 
     
     - 변경된 로봇 정보를 redis에 저장 

3. 로봇 경로 계획
   
   - Kafka를 사용해 주문 서비스에서 주문 정보 받기
   
   - DFS로 최단거리인 경로들 탐색
   
   - Redis에 저장된 각 경로에 쓰이는 그래프들의 진입 불가 시간조회
   
   - 마지막 경로에서 부터 진입 불가 시간을 보고 이전 경로에 진입 할수 없는 시간 갱신 반복
   
   - 결론적으로 로봇이 출발할수없는 시간들을 찾아 가장 빨리 출발할수 있는 시간 찾기
   
   - 각 최단경로중 가장 빨리 출발할 수 있는 경로 선택
   
   - worker-service로 로봇이 도착하는 턴과 물건 정보들을 Kafka로 전송

4. 주문/재고 처리
   
   - Read Through 캐시 전략 사용
   
   - Redis에 해당 상품의 재고 변화가 생기면 Redis에 해당 키가 있는 지 확인
   
   - 없다면 DB에서 데이터 조회후 캐싱
   
   - 스케줄러를 통해 DB에 변화량 일괄처리 캐시 제거

5. 입고/출고 담당자 처리
   
   - 작업라인의 상태를 문자열로 레디스에 캐싱
   
   - spin-lock을 구현해  동시성 문제해결
   
   - Redis의 scored sorted set을 사용해 할당된 작업 처리 시간 순서대로처리
   
   - 주문을 처리하면 Kafka를 통해 order-service로 끝낸 주문 정보를 전달해 로직처리
