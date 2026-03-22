# MSA 인프라 구축


| 구성 요소 | 역할 |
|---|---|
| Eureka Server | 서비스 디스커버리 (서비스 주소록) |
| Config Server | 중앙 설정 관리 (GitHub 연동) |
| API Gateway | 단일 진입점 및 라우팅 |

---

## 전체 인프라 아키텍처
```
외부 클라이언트
      │
      ▼
┌─────────────────┐     ┌─────────────────────┐
│  API Gateway    │     │   Config Server      │
│  (port: 8080)   │     │   (port: 8888)       │
└────────┬────────┘     │   GitHub 연동         │
         │              └──────────────────────┘
    ┌────▼────────────┐
    │  Eureka Server  │
    │  (port: 8761)   │
    └────┬────────────┘
         │ 서비스 등록/조회
    ┌────┴──────────────────────────┐
    │                               │
┌───▼────────┐  ┌──────────────┐  ┌▼─────────────┐
│ POS Service│  │ VAN Gateway  │  │ Card / Bank  │
│  (8081)    │─▶│  (8082)      │─▶│ (8083/8084)  │
└────────────┘  └──────────────┘  └──────────────┘
```

---

## 1. Eureka Server

각 서비스가 IP를 하드코딩하지 않고 이름으로 서로를 찾을 수 있게 해주는 서비스 주소록
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


---

## 2. Config Server

모든 서비스의 설정(DB 주소, 포트, 비밀번호 등)을 GitHub 한 곳에서 관리
```yaml
# config-server/application.yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/yujung23/wooricard-config-repo.git
          default-label: main
```

### GitHub Config Repository 구조
```
wooricard-config-repo/
├── application.yml                    # 모든 서비스 공통 설정 (Eureka 주소)
├── api-gateway.yml                    # API Gateway 라우팅 규칙
├── pay-pos-service.yml                # POS 전용 (DB, 포트 8081)
├── pay-van-gateway.yml                # VAN 전용 (포트 8082)
├── card-authorization-service.yml
└── bank-service.yml
```

### 각 서비스 연동 방식
```yaml
# 각 서비스의 application.yaml (로컬) — 최소화
spring:
  application:
    name: pay-pos-service    # ← 이 이름으로 config repo 파일 매칭
  config:
    import: "optional:configserver:http://localhost:8888"
```

---

## 3. API Gateway

외부에서 보이는 창구를 8080 하나로 통일, URL 패턴에 따라 내부 서비스로 라우팅
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

### `lb://` 동작 방식
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

## 4. 서비스 포트 설계

| 서비스 | 포트 | Eureka 서비스명 |
|---|---|---|
| Eureka Server | 8761 | — |
| Config Server | 8888 | config-server |
| API Gateway | 8080 | api-gateway |
| POS Service | 8081 | pay-pos-service |
| VAN Gateway | 8082 | pay-van-gateway |
| Card Authorization | 8083 | card-authorization-service |
| Bank Service | 8084 | bank-service |

---
