# Ecommerce MSA

Spring Boot 기반 이커머스 마켓플레이스 MSA 프로젝트.  
판매자가 상품을 등록하고 회원이 주문·결제하는 구조로, Kafka 기반 비동기 이벤트 처리 학습을 목적으로 구현.

---

## 기술 스택

| 분류              | 기술                                     |
| ----------------- | ---------------------------------------- |
| Language          | Java 25                                  |
| Framework         | Spring Boot 4.0.6, Spring Cloud 2025.1.1 |
| DB                | MySQL, JPA/Hibernate, QueryDSL 7.1       |
| 인증              | JWT (jjwt 0.12.6)                        |
| 서비스 디스커버리 | Eureka                                   |
| API Gateway       | Spring Cloud Gateway (WebFlux)           |
| 서비스 간 통신    | OpenFeign, Apache Kafka 4.1.2 (KRaft)    |
| 인프라            | Docker, Docker Compose                   |
| 테스트            | JUnit 5, Mockito, Testcontainers         |

---

## 서비스 구성

| 서비스               | 포트 | 설명                          |
| -------------------- | ---- | ----------------------------- |
| eureka-server        | 8761 | 서비스 디스커버리             |
| gateway-service      | 8080 | 진입점, JWT 검증 및 헤더 변환 |
| member-service       | 8081 | 회원/판매자 가입, 로그인      |
| product-service      | 8082 | 상품 등록·수정·삭제·조회      |
| inventory-service    | 8083 | 재고 관리                     |
| order-service        | 8084 | 주문 생성·조회·취소           |
| payment-service      | 8085 | 결제 생성, 웹훅 수신          |
| notification-service | 8086 | 알림 생성·조회                |

---

## 주문 흐름

```
POST /order
  └─ order-service: 주문 저장 (PENDING) → order.created 발행

order.created
  └─ inventory-service: 재고 차감
       ├─ 성공 → inventory.decreased 발행
       └─ 실패 → inventory.failed 발행 → 주문 FAILED

inventory.decreased
  └─ payment-service: 결제 생성

payment.success
  └─ order-service: 주문 PAID

payment.failed
  └─ order-service: 주문 FAILED → order.failed 발행
       └─ inventory-service: 재고 복구
```

---

## 핵심 설계 결정

### 1. Feign vs Kafka 선택 기준

동기적 처리가 필요한 경우 Feign, 비동기로 처리해도 되는 경우 Kafka 사용.

- **Feign**: 상품 가격·이름 조회(읽기), 주문 취소(결제 취소가 사용자의 주요 관심사라고 판단하여 Feign을 활용하여 동기처리)
- **Kafka**: 재고 차감·복구, 결제 생성, 알림 등 주문 흐름의 비동기 이벤트 처리

Feign 클라이언트에 Resilience4j 서킷브레이커 적용, 연속 실패 시 해당 서비스 호출 차단. `FallbackFactory`로 실패 원인 로깅, 읽기 실패 시 null 반환(graceful degradation), 쓰기 실패 시 `ExternalServiceException` 발생으로 503 응답.

### 2. Saga 패턴 — 분산 트랜잭션 처리

하나의 비즈니스 로직이 여러 서비스로 분산되어 하나의 트랜잭션으로 묶기 힘듦. 이에 **Saga 패턴** 을 활용한 보상 트랜잭션 사용.
각 서비스는 자신의 로컬 트랜잭션만 책임지고, 성공·실패 이벤트를 Kafka로 발행해 다음 단계를 트리거. 결제 실패 시 `order.failed` 이벤트로 재고를 복구하는 보상 트랜잭션 실행.

### 3. 재고 차감 멱등성 — Kafka at-least-once 대응

Kafka at-least-once 보장으로 동일 이벤트가 재전달시 재고가 중복으로 차감/복구 될 수 있음. `processed_events` 테이블 도입.

```sql
CREATE TABLE processed_events (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(20) NOT NULL,  -- DECREASE / INCREASE
    order_id   BIGINT      NOT NULL,
    UNIQUE (event_type, order_id)
);
```

중복 체크 → 레코드 저장 → 재고 변경을 **하나의 트랜잭션**으로 처리해 중복 실행 차단. 재고 부족(`InsufficientStockException`)은 재시도해도 의미가 없으므로 Kafka `not-retryable` 예외로 등록해 즉시 DLQ로 이동.

### 4. Pessimistic Lock — 동시 주문 oversell 방지

동시 주문 시 Race Condition으로 oversell이 발생할 수 있음. 재고 특성상 충돌 빈도가 높다고 판단해 `SELECT FOR UPDATE` 비관적 락 선택.
다중 상품 주문 시 데드락 방지를 위해 `productId ASC` 순서로 정렬하여 락 획득.

---

## 실행 방법

```bash
docker-compose up -d
```
