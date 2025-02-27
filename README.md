# TEAM 2

## 진행 상황
- 코드 리팩토링을 통한 품질 개선 진행

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
- 지정가/시장가 주문 처리 로직
- 매수/매도 주문 매칭 알고리즘
- 호가창 생성 및 관리

### TradeHistoryService
- 거래 내역 저장 및 조회
- 캔들 차트 데이터 생성
- 실시간 차트 업데이트

### KisWebSocketService
- 한국투자증권 API 연동
- 실시간 주식 데이터 수신 및 처리

## 다음 작업 계획
- 코드 리팩토링 완료
- 단위 테스트 작성
- 기능 통합 및 테스트
