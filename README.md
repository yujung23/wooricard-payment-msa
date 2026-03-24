# wooricard-payment-msa

우리카드 결제 MSA 프로젝트 — POS → VAN → 카드사 → 은행 결제 승인 흐름을 마이크로서비스로 구현합니다.

---

## 전체 흐름

```
사용자 ↔ API Gateway(8080) ↔ POS(8081) ↔ VAN(8082) ↔ 카드사(8083) ↔ 은행(8084)
```

---

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| `eureka-server` | 8761 | 서비스 디스커버리 |
| `config-server` | 8888 | 중앙 설정 관리 |
| `api-gateway` | 8080 | 라우팅 및 진입점 |
| `pay-pos-service` | 8081 | POS 단말기 역할, 결제 요청 |
| `pay-van-gateway` | 8082 | VAN 서버, 카드사 라우팅 |
| `card-authorization-service` | 8083 | 카드사, 승인/거절 처리 |
| `bank-service` | 8084 | 은행, 잔액 조회 및 출금 |

---

## API

### 결제 요청

```
POST /api/v1/approval/request
```

**Request Body**
```json
{
  "primaryAccountNumber": "9123456789012345",
  "expirationDate": "2028-12",
  "transactionAmount": 50000,
  "cardAcceptorId": "MERCHANT_001",
  "terminalId": "TERM_001",
  "installmentMonths": 0,
  "posOrderId": "POS_ORDER_001"
}
```

**Response**
```json
{
  "systemTraceAuditNumber": "uuid-...",
  "responseCode": "00",
  "responseMessage": "승인",
  "approvedAt": "2026-03-23T10:30:00",
  "cardCompany": "WOORICARD",
  "posOrderId": "POS_ORDER_001"
}
```

### 승인 결과 조회

```
GET /api/v1/approval/{stan}
```

---

## ISO 8583 필드 규칙

POS ↔ VAN 간 요청/응답 필드는 ISO 8583 Data Element 기준으로 명명합니다.

| 필드명 | DE | 설명 |
|--------|----|------|
| `primaryAccountNumber` | DE02 | 카드번호 (PAN) |
| `transactionAmount` | DE04 | 거래금액 |
| `systemTraceAuditNumber` | DE11 | 거래 고유번호 (STAN) |
| `expirationDate` | DE14 | 카드 유효기간 |
| `responseCode` | DE39 | 응답 코드 |
| `terminalId` | DE41 | 단말기 ID |
| `cardAcceptorId` | DE42 | 가맹점 ID |

**주요 응답 코드**

| 코드 | 의미 |
|------|------|
| `00` | 승인 |
| `51` | 잔액 부족 |
| `54` | 유효기간 만료 |
| `61` | 한도 초과 |
| `96` | 시스템 오류 |

---

## 흐름도
<img width="1989" height="1820" alt="image" src="https://github.com/user-attachments/assets/f8bf2158-4088-48aa-a097-50606acf18c9" />



## 서비스 간 통신

모든 서비스 간 통신은 **OpenFeign + Eureka** 기반으로 서비스명을 사용합니다.
OpenFeign 구현 클래스 없이 인터페이스만 있으면 Feign이 자동으로 HTTP 호출 코드를 만들어줍니다.

<img width="1238" height="330" alt="image" src="https://github.com/user-attachments/assets/3aba8f2e-7870-4ac1-b72c-db07bd2d1228" />

### 구현 예시
```java
// VAN → 카드사
@FeignClient(name = "card-authorization-service")

// 카드사 → 은행
@FeignClient(name = "bank-service")
```



---
# 팀원별 작업 내역

## 개발자 1 - 유승준 | POS & VAN 로직 개발 & Eureka 서버 보조

### 1. POS ↔ VAN DTO 필드 통일
<img width="500" height="220" alt="image" src="https://github.com/user-attachments/assets/35ad3015-af52-4d6c-9111-e0efd6fc9ef4" />

- ISO 8583 Data Element 기준으로 필드명 표준화
- `terminalId`(DE41), `posOrderId` 필드 추가
- `cardCompany` 응답 필드 추가 (VAN이 BIN 기반으로 채움)

### 2. VAN 서비스 하드코딩 제거 → OpenFeign 연동
- `PaymentServiceImpl`에서 항상 SUCCESS를 반환하던 하드코딩 제거
- `CardAuthorizationClient` Feign 인터페이스 작성
- 실제 카드사 서비스 호출로 교체

### 3. BIN 번호 기반 카드사 식별 + cardCompany 책임 위치 VAN으로 이동
<img width="507" height="208" alt="image" src="https://github.com/user-attachments/assets/4d7a2c2a-b477-40f7-9149-2c2619479dc4" />

- 초기 설계: 카드사 서비스 내에서 BIN 번호로 카드사 유추
- 수정: VAN이 BIN 번호로 카드사 판단 후 POS에 전달
- 카드사 서비스는 승인/거절만 처리하도록 책임 분리

### 4. POS API 경로 수정
- `/api/v1/van/approval` → `/api/v1/approval` (API Gateway 라우팅과 일치)
- 경로 불일치로 인한 404 에러 해결

### 5. 카드사→은행 RestClient → OpenFeign 교체
- `BankClientImpl` (RestClient 직접 생성) 삭제
- `BankClient` 인터페이스를 `@FeignClient(name = "bank-service")`로 교체
- `http://bank-service` 서비스명으로 Eureka 자동 로드밸런싱 적용

### 6. [테스트 코드 작성](https://github.com/yujung23/wooricard-payment-msa/blob/main/pay-pos-service/src/test/java/com/card/payment/pos/PayPosServiceApplicationTests.java) 
- 정상 결제 요청 시 VAN 호출 후 승인 응답 리턴
- 정상 결제 요청 시 결제 이력 DB 저장
- 존재하는 STAN으로 조회 시 정상 응답 리턴
- 존재하지 않는 STAN으로 조회 시 RuntimeException 발생
---


## 개발자 2 — 박주호 | VAN 서버

POS로부터 결제 요청을 수신하고 카드사로 중계하는 VAN 게이트웨이 역할을 담당.

### 주요 구현
- `POST /api/v1/approval/request` — 카드 승인 요청 API
- DTO를 Java Record로 설계 (불변성 보장, 결제 정보 변조 방지)
- OpenFeign으로 카드사 서버 연동
- `PaymentHistory` DB 저장 (`@Transactional`)

### 테스트 코드 작성 (`@SpringBootTest` + `@MockitoBean`)

실제 카드사 서버 대신 `CardAuthorizationClient`를 Mock으로 대체하여 VAN 서비스 로직만 독립 검증

- VISA 카드 정상 승인 — 응답 데이터 매핑 및 DB 저장 검증
- 잔액 부족(51) — 응답 코드/메시지 POS 정상 전달 검증
- 미정의 카드번호 입력 — UNKNOWN 카드사 식별 검증

### DTO 변경 대응 — 테스트 코드 수정
개발자1의 DTO 변경(`terminalId`, `posOrderId` 필드 추가)으로 인해 `PosPaymentRequest` 생성자 호출부 컴파일 실패 <br>
테스트 케이스 3개 전체의 생성자 호출부에 누락된 인자(`terminalId`, `posOrderId`)를 추가하여 수정

---

## 개발자 3 — 김유정 | MSA 인프라 구축

Eureka, Config Server, API Gateway를 구성하여 전체 MSA 기반 인프라를 담당.

### 서비스 포트 설계

| 서비스 | 포트 | Eureka 서비스명 |
|---|---|---|
| Eureka Server | 8761 | — |
| Config Server | 8888 | config-server |
| API Gateway | 8080 | api-gateway |
| POS Service | 8081 | pay-pos-service |
| VAN Gateway | 8082 | pay-van-gateway |
| Card Authorization | 8083 | card-authorization-service |
| Bank Service | 8084 | bank-service |

### MSA 아키텍쳐 설계
```
 외부 클라이언트
      │
      ▼
┌─────────────────┐     ┌─────────────────────┐
│  API Gateway    │     │   Config Server     │
│  (port: 8080)   │     │   (port: 8888)      │
└────────┬────────┘     │   GitHub 연동       │
         │              └─────────────────────┘
    ┌────▼────────────┐
    │  Eureka Server  │
    │  (port: 8761)   │
    └────┬────────────┘
         │ 서비스 등록/조회
    ┌────┴────────────────────────────┐
    │                                 │
┌───▼────────┐   ┌──────────────┐   ┌─▼────────────┐
│ POS Service│   │ VAN Gateway  │   │ Card / Bank  │
│  (8081)    │─▶│  (8082)      │─▶│ (8083/8084)  │
└────────────┘   └──────────────┘   └──────────────┘
```

### Eureka Server (8761) 
서비스 디스커버리: 서비스 간 IP 하드코딩 없이 이름으로 통신
```yaml
# eureka-server/application.yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false  
    fetch-registry: false
  instance:
    hostname: localhost
```

### Config Server (8888): 
GitHub 연동 중앙 설정 관리 (`wooricard-config-repo`)
```
wooricard-config-repo/
├── application.yml                    # 모든 서비스 공통 설정 (Eureka 주소)
├── api-gateway.yml                    # API Gateway 라우팅 규칙
├── pay-pos-service.yml                # POS 전용 (DB, 포트 8081)
├── pay-van-gateway.yml                # VAN 전용 (포트 8082)
├── card-authorization-service.yml
└── bank-service.yml
```

### API Gateway (8080): 
단일 진입점, `lb://서비스명` 방식으로 Eureka 자동 로드밸런싱
```groovy
// api-gateway/build.gradle 핵심 의존성
implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
implementation 'org.springframework.cloud:spring-cloud-starter-loadbalancer'  // lb:// 사용에 필수
```
```yaml
# api-gateway.yml (GitHub Config Repo)
server:
  port: 8080
spring:
  cloud:
    gateway:
      routes:
        - id: pos-service
          uri: lb://pay-pos-service       # Eureka에서 서비스 이름으로 조회
          predicates:
            - Path=/api/v1/approval/**    # 이 경로 → POS 서비스로 전달
```

### `lb://` 동작 방식 이해
```
POST /api/v1/approval/request
          │
    API Gateway
          │  Path 매칭
          ▼
  Eureka에서 'pay-pos-service' 조회
          │
          ▼
  pay-pos-service:8081/api/v1/approval/request
```

> IP를 직접 쓰지 않기 때문에, 인스턴스가 늘어나도 자동 로드밸런싱 적용

---
```yaml
# API Gateway 라우팅 예시
routes:
  - id: pos-service
    uri: lb://pay-pos-service
    predicates:
      - Path=/api/v1/approval/**
```

---

## 개발자 4 — 하은영 | 기본 제공 서버 분석 & Swagger 명세

기본 제공된 카드사/은행 서버를 분석하고 포트 불일치 이슈를 발견·수정, Swagger 명세 작성을 담당.

### 포트 설정 오류 수정

| 서비스 | 수정 내용 |
|--------|---------|
| `bank-service` | 포트 `8080` → `8084` |
| `card-authorization-service` | `BankClient` URL `8080` → `8084` |
| `card-authorization-service` | 서버 포트 `9090` → `8083` |

### Swagger 명세 작성
- `OpenApiConfig` 클래스 추가, `HealthController` (`GET /api/health`) 추가
- `@Operation`, `@ApiResponses`, `@ExampleObject`로 각 엔드포인트 명세 작성
- DTO 변경 시 Swagger 예시 즉시 반영

| 서비스 | Swagger UI |
|--------|-----------|
| POS Service | `http://localhost:8081/swagger-ui/index.html` |
| VAN Gateway | `http://localhost:8082/swagger-ui/index.html` |

---

# 트러블슈팅 

### 유승준

### [VAN-001] cardCompany 책임 위치 설계 오류

**증상**: 카드사 서비스 내부에서 BIN 번호로 카드사를 유추하는 로직 작성

**원인**: MSA 책임 분리 원칙 미준수. 카드사는 승인/거절만 담당해야 하며, BIN 라우팅은 VAN의 역할

**해결**: `resolveCardCompany()`를 VAN의 `PaymentServiceImpl`로 이동
```java
// pay-van-gateway: PaymentServiceImpl.java
private String resolveCardCompany(String primaryAccountNumber) {
    return switch (primaryAccountNumber.charAt(0)) {
        case '4' -> "VISA";
        case '5' -> "MASTERCARD";
        case '9' -> "WOORICARD";
        default -> "UNKNOWN";
    };
}
```

---

### [POS-001] PaymentRequest terminalId, posOrderId 필드 누락

**증상**: 결제 요청 시 500 에러
```
PropertyValueException: not-null property references a null or transient value: PaymentHistory.posOrderId
```

**원인**: POS ↔ VAN 스펙 합의 후 `PaymentRequest` DTO에 `terminalId`, `posOrderId` 필드가 주석으로만 남고 실제 필드 누락

**해결**: `PaymentRequest` record에 두 필드 복구
```java
public record PaymentRequest(
        String primaryAccountNumber,
        String expirationDate,
        Long transactionAmount,
        String cardAcceptorId,
        String terminalId,       // 복구
        int installmentMonths,
        String posOrderId        // 복구
) {}
```

---

### [CARD-001] 카드사→은행 RestClient Eureka 미적용

**증상**: 체크카드 결제 시 `시스템 오류: 잔액 조회 최종 실패`

**원인**: config-server에 `bank.service.url: http://bank-service`로 설정되어 있으나, `BankClientImpl`이 `RestClient`를 직접 생성하여 Eureka 로드밸런서를 거치지 않음 → DNS 조회 실패

```
VAN → 카드사: @FeignClient → Eureka 자동 연동 ✅
카드사 → 은행: RestClient 직접 생성 → Eureka 미적용 ❌
```

**해결**: `BankClientImpl` 삭제 후 `@FeignClient`로 교체하여 Feign으로 통일
```java
@FeignClient(name = "bank-service")
public interface BankClient {
    @PostMapping("/api/account/balance")
    BalanceResponse checkBalance(@RequestBody BalanceRequest request);

    @PostMapping("/api/account/debit")
    DebitResponse requestDebit(@RequestBody DebitRequest request);
}
```

### 교훈 
- 서비스 간 통신을 OpenFeign으로 진행하자고 함.
- Config 서버를 설계할 때, Eureka 연동이 자동 처리되도록 함.
- POS, VAN 개발 시, OpenFeign 을 잘 사용 했지만, 제공받은 카드사, 은행 쪽 코드는 RestTemplate으로 레거시 코드 유지
-  카드사, 은행 쪽도 OpenFeign 종속성 추가 후, 기존 코드와 일치하도록 코딩. 
> MSA 방식으로 개발 할 경우 공통 설정에 예외가 없도록 설계하는 것이 중요하다 느낌

###  박주호

**DTO 변경 대응 — 테스트 코드 수정**
- 개발자1(유승준)의 DTO 변경(`terminalId`, `posOrderId` 필드 추가)으로 인해 `PosPaymentRequest` 생성자 호출부 컴파일 실패
- 테스트 케이스 3개 전체의 생성자 호출부에 누락된 인자(`terminalId`, `posOrderId`) 추가하여 수정
