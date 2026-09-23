# notification-service MongoDB 전환 결정 배경

## 현재 상태 (전환 전)

notification-service는 MySQL 단일 DB를 사용하며, 다음 스키마로 알림을 저장한다.

### `notification` 테이블
| 컬럼         | 타입        | 비고                                  |
| ------------ | ----------- | ------------------------------------- |
| id           | BIGINT PK   |                                       |
| member_id    | BIGINT      | 알림 수신자                           |
| type         | VARCHAR(20) | PAYMENT_SUCCESS, ORDER_CANCELED 등    |
| order_id     | BIGINT NULL | 주문 관련 알림에만 채워짐             |
| payment_id   | BIGINT NULL | 결제 성공 알림에만 채워짐             |
| review_id    | BIGINT NULL | 리뷰 알림에만 채워짐                  |
| content      | VARCHAR     | 표시용 메시지                         |
| is_read      | BOOLEAN     |                                       |

### `processed_event_notification` 테이블
| 컬럼         | 타입        | 비고                                  |
| ------------ | ----------- | ------------------------------------- |
| id           | BIGINT PK   |                                       |
| kafka_topic  | VARCHAR(30) | ENUM                                  |
| target_id    | BIGINT      | topic에 따라 orderId 또는 reviewId    |
| UNIQUE       | (kafka_topic, target_id) |                          |

## 문제점 인식

### 1) 알림 타입 확장 시 스키마 반복 변경
새 알림 타입이 추가될 때마다 `notification` 테이블에 nullable 참조 컬럼이 하나씩 늘어난다.

- 현재: `order_id`, `payment_id`, `review_id` 세 개의 nullable 컬럼
- 향후 예상: 리뷰 답글, 상품 재입고, 쿠폰 발급, 팔로우 등 새로운 알림마다 컬럼 추가

이는 시니어 리뷰 관점에서 대표적인 안티패턴이다. **"타입마다 컬럼이 늘어나는 테이블"** 은 관계형 스키마의 잘못된 활용이다.

### 2) 스키마와 실제 필드 사용의 불일치
스키마만 봐서는 각 알림 타입이 어떤 컬럼을 사용하는지 알 수 없다.

- `PAYMENT_SUCCESS` → `order_id`, `payment_id` 사용
- `PAYMENT_FAILED` / `ORDER_CANCELED` → `order_id`만 사용
- `REVIEW_CREATED` → `review_id`만 사용

이 매핑은 코드로만 표현되며, DB만 보는 사람에겐 불투명하다.

### 3) `processed_event_notification`의 target_id 의미 모호
`target_id` 하나의 컬럼이 topic에 따라 다른 의미(orderId 또는 reviewId)를 가진다. 현재는 `kafka_topic`을 discriminator 삼아 UNIQUE 제약만으로 멱등성을 보장하지만, 컬럼명 자체는 여전히 의미가 흐릿하다.

### 4) 알림 도메인 자체가 관계형에 잘 맞지 않음
알림은 다음과 같은 특성을 가진다.

- **스키마 이질성**: 타입마다 담아야 할 정보가 다르다
- **쓰기 위주**: 대부분 append-only, 수정은 읽음 처리 정도
- **높은 볼륨**: 사용자당 수천~수만 개 쌓일 수 있음
- **단순 조회 패턴**: 대부분 "내 알림 최신순 페이징"
- **관계형 조인 불필요**: 다른 테이블과 FK로 얽히지 않음

이는 문서 지향 NoSQL(MongoDB) 유스케이스에 정확히 부합한다.

## 왜 대안(JSON metadata 컬럼)이 아닌 MongoDB인가

MySQL 안에서 `metadata VARCHAR/JSON` 컬럼 하나로 확장성 문제만 해결할 수 있다. 그러나 다음 이유로 MongoDB 전환을 선택했다.

### 이력서/경력 관점
- **폴리글롯 저장소(polyglot persistence)** 는 MSA의 실전 원칙 중 하나이며, 리뷰어에게 "언제 어떤 DB를 쓰는지 판단할 수 있다"는 시그널을 줄 수 있다.
- "왜 이 서비스만 MongoDB인가?"라는 질문에 명확한 답(알림 도메인 특성)을 준비할 수 있다.
- MongoDB의 UNIQUE 인덱스, 트랜잭션, replica set 등 인프라 경험은 이력서 서술 시 별도 가치가 있다.

### 기술적 관점
- JSON 컬럼은 MySQL의 강점(트랜잭션/조인)을 못 살리고 오히려 쿼리를 복잡하게 만든다. 알림은 조인도 트랜잭션도 거의 안 쓰니 MySQL을 쓰는 이유가 희박하다.
- MongoDB의 문서 스키마는 알림 타입별로 필요한 필드만 정확히 담을 수 있다.
- 알림 볼륨이 커지면 MongoDB의 수평 확장이 MySQL보다 유리하다.

## CLAUDE.md 원칙과의 관계

프로젝트 원칙: `DB는 하나만 사용(테이블만 분리해놓고 별개의 DB처럼 설계해서 사용)`

이 원칙은 **학습 초기의 단순화 목적**이었다. 서비스 분리 개념은 익히되 인프라 부담은 줄이려는 의도. 프로젝트가 성숙하면서 `notification-service`가 폴리글롯 저장소 사례로 예외 처리하는 것은 원칙을 발전적으로 극복하는 결정이다.

CLAUDE.md에도 예외 사유를 명시할 예정.

## 마이그레이션 시 함께 정리할 것

### 1) `processed_event_notification` 테이블 제거
- 별도 컬렉션 대신 `notification` 문서에 UNIQUE 인덱스 `(type, referenceId)`로 대체
- 소비자에서 `insert` 시도 → `DuplicateKeyException` 발생 시 이미 처리된 것으로 판단 → ACK skip
- 트랜잭션 불필요, 쓰기 1번, 코드 단순화

```java
try {
    notificationRepository.save(notification);
} catch (DuplicateKeyException e) {
    log.info("이미 처리된 이벤트 - type={}, refId={}", ...);
    // ACK만
}
```

### 2) 참조 필드 통합
현재 `orderId`, `paymentId`, `reviewId` 세 컬럼을 문서 필드로 정리:

```json
{
  "_id": ObjectId,
  "memberId": 5,
  "type": "REVIEW_CREATED",
  "referenceId": 100,
  "metadata": {
    "productId": 200
  },
  "content": "판매 상품에 대한 리뷰가 생성되었습니다.",
  "isRead": false,
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

- `referenceId`: 알림 클릭 시 이동 대상 (주 참조)
- `metadata`: 부가 정보 (type별로 다름)

### 3) `NotificationService.createNotification` 분기 로직 단순화
현재는 type별 컬럼 매핑 로직이 필요 없지만, MongoDB 전환 시 type별 문서 구조를 만드는 팩토리 메서드가 명확히 필요해진다.

### 4) TestContainers MongoDB 도입
- `MongoDBContainer("mongo:7.0")` 등으로 통합 테스트
- 기존 MySQL TestContainers와 동일한 패턴

## 향후 결정 필요 사항

- [ ] MongoDB 버전 (7.0 or 8.0)
- [ ] Replica set 세팅 여부 (트랜잭션 필요 시 필수, UNIQUE 인덱스로 대체하면 불필요)
- [ ] 기존 알림 데이터 마이그레이션 스크립트 필요 여부 (학습 프로젝트라 skip 가능)
- [ ] Spring Data MongoDB vs MongoTemplate 사용 선택

## 마이그레이션 트리거

이 문서 작성 시점 기준, 마이그레이션은 **연기**된 상태다. 다음 조건이 충족되면 착수:

1. review-service Kafka 이벤트 흐름이 안정화된 후
2. 별도 알림 타입 (예: 리뷰 답글) 추가 요구가 생겼을 때 — 컬럼 추가하려는 시점이 자연스러운 리팩터링 신호
3. 이력서 정리 단계에서 폴리글롯 스토리를 넣고 싶어질 때

## 참고

- 결정 시점: 2026-09-23
- 관련 컨텍스트: review-service Kafka producer 추가 및 notification-service consumer 구현 중, `Notification` 엔티티에 `reviewId` 컬럼 추가하는 순간 "이렇게 계속 컬럼 추가할 건가?" 문제가 명확히 드러남.
