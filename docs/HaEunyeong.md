# 포트 설정 오류 수정
기본 제공된 서버를 분석하는 과정에서 서비스 간 포트 불일치 이슈를 발견하고 수정했다.

| 서비스 | 수정 내용 |
|---|---|
| `bank-service` | `application.yml` 포트 `8080` → `8084` |
| `card-authorization-service` | `BankClient` URL `8080` → `8084` |
| `card-authorization-service` | 서버 포트 `9090` → `8083` |

---

# Swagger API 명세 작성
팀원들이 API를 빠르게 파악하고 테스트할 수 있도록 POS와 VAN 서비스에 Swagger 명세를 작성했다.

**공통 작업**
- `OpenApiConfig` 클래스 추가 (서비스명, 설명, 서버 URL 설정)
- `HealthController` 추가 (`GET /api/health` — 서비스 상태 확인)
- `build.gradle`에 `springdoc-openapi-starter-webmvc-ui:2.3.0` 의존성 추가

**명세 작성**
- `@Operation`, `@ApiResponses`, `@ExampleObject` 어노테이션으로 각 엔드포인트 명세 작성
- 결제 승인 요청 / 승인 결과 조회 엔드포인트 대상
- 요청 및 응답 예시 데이터 포함
- DTO 변경 시 Swagger 예시 데이터 즉시 반영
- 전체 서비스 실행 후 Swagger UI로 엔드포인트 동작 확인 및 이슈 공유

| 서비스 | Swagger UI |
|---|---|
| POS Service | `http://localhost:8081/swagger-ui/index.html` |
| VAN Gateway | `http://localhost:8082/swagger-ui/index.html` |