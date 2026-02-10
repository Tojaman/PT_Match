# 피티 매칭
트레이너 정보를 한눈에 비교하고, 일정에 맞춰 PT를 손쉽게 신청할 수 있도록 만든 매칭 서비스입니다.

### 프로젝트 개요
- 목적: 트레이너와 상품 정보를 투명하게 공개해 사용자가 원하는 시간에 간편하게 신청할 수 있게 합니다
- 타겟 사용자: PT를 찾는 사용자와 자신의 서비스를 효과적으로 노출하고 싶은 트레이너


### 핵심 기능
- 트레이너/상품 검색 및 필터링(지역, 전문 분야, 가격 등)
- 지도 기반 트레이너/상품 탐색
- 트레이너 프로필·포트폴리오 조회
- PT 매칭 신청 및 상태 관리
- 후기 작성/관리 및 좋아요/팔로우

### 시스템 아키텍처

### ERD

### 프로젝트 구조
```text
src/main/java/com/solo/ptmatch
├─ common            (여러 도메인 공통 코드)
└─ {domain}
   ├─ application    (서비스)
   ├─ domain         (엔티티)
   ├─ infrastructure (DB/캐시)
   │  └─ provider    (DB/캐시 접근)
   └─ presentation   (컨트롤러)
```

### 주요 기술 스택
- Java 17, Spring Boot
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL, PostGIS
- Redis, Caffeine Cache
- MinIO
- JWT, SpringDoc OpenAPI
- Prometheus, Grafana, Loki, OpenTelemetry

### API 명세서(Swagger)

### 데모/스크린샷
