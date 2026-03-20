# Troubleshooting

---

## 문서 변경 이력

| @author | 날짜/시각 | 변경 내용 |
|---------|-----------|-----------|
| 개발자1 | 2026.03.20.16.00 | 최초 작성 - cardCompany 설계 오류 및 해결 과정 기록 |
| 개발자1 | 2026.03.20.17.00 | POS PaymentRequest terminalId, posOrderId 누락 이슈 추가 |
| 개발자1 | 2026.03.20.18.00 | CARD-001 카드사→은행 RestClient Eureka 미적용 이슈 추가 |

---

## [VAN-001] cardCompany 책임 위치 설계 오류

### 발생 배경

`PosPaymentResponse`에 `cardCompany` 필드가 필요하여, 카드사(`card-authorization-service`)의 `AuthorizationResponse`에 `cardCompany` 필드를 추가하고 `AuthorizationService` 내부에서 BIN 번호로 카드사를 유추하는 `resolveCardCompany()` 메서드를 구현함.

### 문제점

카드사(`card-authorization-service`)는 실제로 여러 카드사 중 하나를 시뮬레이션하는 서비스임. 현실에서 각 카드사는 자신이 어느 회사인지 이미 알고 있으므로 BIN 번호로 유추할 필요가 없음.

또한 BIN 번호 기반 카드사 식별은 **VAN의 핵심 역할** (어느 카드사로 라우팅할지 판단) 임에도 불구하고 카드사 내부에 로직이 들어간 것은 책임 분리 원칙에 어긋남.

```
잘못된 흐름:
POS → VAN → 카드사(BIN으로 카드사 유추 후 cardCompany 응답에 포함) → VAN → POS

올바른 흐름:
POS → VAN(BIN으로 카드사 판단 후 cardCompany 직접 채움) → 카드사(승인만 처리) → VAN → POS
```

### 해결

1. `AuthorizationResponse`에서 `cardCompany` 필드 삭제
2. `AuthorizationService`에서 `resolveCardCompany()` 및 관련 코드 제거
3. VAN의 `PaymentServiceImpl`에 `resolveCardCompany()` 이동
4. `PosPaymentResponse.from()` 시그니처에 `cardCompany` 파라미터 추가, VAN이 직접 채워서 POS에 전달

### 적용 코드

```java
// PaymentServiceImpl.java (pay-van-gateway)
private String resolveCardCompany(String primaryAccountNumber) {
    if (primaryAccountNumber == null || primaryAccountNumber.isEmpty()) {
        return "UNKNOWN";
    }
    return switch (primaryAccountNumber.charAt(0)) {
        case '4' -> "VISA";
        case '5' -> "MASTERCARD";
        case '9' -> "WOORICARD";
        default -> "UNKNOWN";
    };
}
```

### 교훈

- 각 서비스의 책임 범위를 명확히 할 것
- **카드사**: 카드 유효성 검증 및 승인/거절 처리만 담당
- **VAN**: BIN 라우팅, 카드사 식별, 필드 변환 담당

---

## [POS-001] PaymentRequest에 terminalId, posOrderId 필드 누락

### 발생 배경

POS ↔ VAN 스펙 합의 후 `PaymentRequest`에 `terminalId`, `posOrderId` 필드를 추가했으나, 이후 코드 수정 과정에서 해당 필드들이 주석으로만 남고 실제 필드가 누락됨.

### 문제점

```java
// 누락된 상태
public record PaymentRequest(
        String primaryAccountNumber,
        String expirationDate,
        Long transactionAmount,
        String cardAcceptorId,
        // DE41 - 단말기 ID (선택, 없으면 null)   ← 필드 없이 주석만 존재
        int installmentMonths
        // POS 주문 ID                             ← 필드 없이 주석만 존재
) {}
```

VAN의 `PosPaymentRequest`에는 `posOrderId`가 있고 `PaymentHistory`에 `nullable = false`로 설정되어 있어서 요청 시 500 에러 발생.

```
PropertyValueException: not-null property references a null or transient value: PaymentHistory.posOrderId
```

### 해결

```java
public record PaymentRequest(
        String primaryAccountNumber,    // DE02 - 카드번호
        String expirationDate,          // DE14 - 유효기간
        Long transactionAmount,         // DE04 - 거래금액
        String cardAcceptorId,          // DE42 - 가맹점ID
        String terminalId,              // DE41 - 단말기 ID (선택, 없으면 null)
        int installmentMonths,          // 할부 개월수
        String posOrderId               // POS 주문 ID
) {}
```

### 교훈

- POS ↔ VAN 스펙 합의 후 양쪽 DTO 필드가 동일한지 반드시 대조 확인할 것
- 주석으로만 남긴 필드는 실제 코드에 반영되지 않으므로 주의

---

## [CARD-001] 카드사→은행 RestClient에 Eureka 로드밸런서 미적용

### 발생 배경

`card-authorization-service`가 `bank-service`를 호출할 때 config-server에 설정된 `bank.service.url: http://bank-service` (서비스명 방식)를 사용하도록 설정되어 있었으나, `BankClientImpl`이 `RestClient`를 직접 생성하는 방식이어서 Eureka 로드밸런서를 거치지 않았음.

### 문제점

```
에러 메시지: 시스템 오류: 잔액 조회 최종 실패
원인: http://bank-service → DNS 조회 실패 → 1회 재시도 후 BankClientException 발생
```

```java
// 문제 코드: RestClient 직접 생성 → Eureka 미적용
public BankClientImpl(@Value("${bank.service.url:http://localhost:8080}") String bankServiceUrl) {
    this.balanceRestClient = RestClient.builder()
            .baseUrl(bankServiceUrl)  // http://bank-service → DNS 조회 → 실패
            .build();
}
```

VAN → 카드사 연동은 `OpenFeign`을 사용하여 Eureka 자동 연동이 되었으나, 카드사 → 은행 연동은 `RestClient` 직접 생성 방식이어서 서비스명 기반 URL이 동작하지 않는 불일치가 있었음.

```
VAN → 카드사: @FeignClient → Eureka 자동 연동 ✅
카드사 → 은행: RestClient 직접 생성 → Eureka 미적용 ❌
```

### 해결

`BankClientImpl` 삭제 후 `BankClient` 인터페이스를 `@FeignClient`로 교체하여 통일.

1. `build.gradle`에 `spring-cloud-starter-openfeign` 추가
2. `CardAuthorizationServiceApplication`에 `@EnableFeignClients` 추가
3. `BankClient.java`를 `@FeignClient(name = "bank-service")`로 변환
4. `BankClientImpl.java` 삭제
5. `AuthorizationService`에서 Feign 인터페이스 메서드 시그니처에 맞게 DTO 객체로 호출 수정

```java
// 변경 후: BankClient.java
@FeignClient(name = "bank-service")
public interface BankClient {

    @PostMapping("/api/account/balance")
    BalanceResponse checkBalance(@RequestBody BalanceRequest request);

    @PostMapping("/api/account/debit")
    DebitResponse requestDebit(@RequestBody DebitRequest request);
}
```

### 교훈

- MSA에서 서비스명 기반 URL(`http://서비스명`)은 Eureka + 로드밸런서가 적용된 클라이언트에서만 동작함
- `RestClient`는 기본적으로 로드밸런서를 거치지 않으므로 서비스명 방식 사용 시 `@LoadBalanced` 설정이 별도로 필요
- 프로젝트 내 서비스 간 통신 방식은 **OpenFeign으로 통일**하면 Eureka 연동이 자동으로 처리됨
