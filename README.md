# TEAM 2

## 진행 상황
- 주문 매칭 엔진 코드 리팩토링을 통한 가독성 및 유지보수성 개선
- 실시간 데이터 처리 로직 최적화 및 확장성 강화


## 프로젝트 구조
```
src
├─main
│  ├─java
│  │  └─org
│  │      └─scoula
│  │          └─backend
│  │              ├─global
│  │              │  ├─config
│  │              │  └─entity
│  │              ├─member
│  │              │  ├─controller
│  │              │  │  ├─request
│  │              │  │  └─response
│  │              │  ├─domain
│  │              │  ├─dto
│  │              │  ├─repository
│  │              │  └─service
│  │              └─order
│  │                  ├─controller
│  │                  │  ├─request
│  │                  │  └─response
│  │                  ├─domain
│  │                  ├─dto
│  │                  ├─repository
│  │                  └─service
│  │                      ├─exception
│  │                      ├─kiswebsocket
│  │                      ├─simulator
│  │                      └─validator
│  └─resources
└─test
    └─java
        └─org
            └─scoula
                └─backend
```

## 주요 구현 내용

### OrderBookService
- 가격-시간 우선 원칙에 따른 주문 매칭 알고리즘 구현
- TreeMap 자료구조를 활용한 효율적인 주문장(Order Book) 관리
- 시장가/지정가 주문 처리 로직 분리를 통한 코드 가독성 향상
- 동시성 고려한 주문 처리 메커니즘 설계
- 매수/매도 주문 큐의 효율적인 관리를 위한 PriorityQueue 활용

### TradeHistoryService
- 실시간 거래 내역의 효율적인 메모리 관리 (ConcurrentLinkedQueue 활용)
- 15초 간격 캔들 차트 데이터 생성 및 관리 로직
- WebSocket을 통한 실시간 차트 데이터 전송 구현
- 다양한 시간대 차트 지원을 위한 확장 가능한 구조 설계
- 거래량 집계 및 OHLC(Open, High, Low, Close) 데이터 계산 로직

### KisWebSocketService
- 한국투자증권 WebSocket API 연동을 통한 실시간 시세 데이터 수신
- 비동기 메시지 처리 및 에러 핸들링 로직 구현
- 수신된 데이터 파싱 및 정규화 처리
- 연결 관리 및 재연결 메커니즘 구현
- 메시지 큐를 활용한 안정적인 데이터 처리

## 다음 작업 계획
- 다중 종목 처리를 위한 확장성 개선
- 사용자 계정 연동 및 권한 관리 구현
- Redis 캐시를 활용한 차트 데이터 관리 최적화
- 단위 테스트 및 통합 테스트 작성
- 성능 모니터링 및 병목 현상 개선
