# TASK

## 현재 우선순위

### 1. 문서/Swagger 유지

- [x] `springdoc-openapi` 기반 Swagger/OpenAPI 도입
- [x] 통합 OpenAPI 산출물 `docs/openapi/atlas-backend-openapi.json` 생성
- [x] 컨트롤러 REST 매핑과 통합 OpenAPI operation 수 대조 검증
- [x] 통합 OpenAPI에 `summary/description/tag` 및 스키마 설명 자동 보강
- [ ] SwaggerHub에 최신 통합 JSON 재import
- [ ] 서비스 런타임 Swagger UI 후처리(`OpenApiConfig` + `OpenApiCustomizer`) 적용 여부 결정
- [ ] 주요 DTO에 수동 `@Schema`를 더 추가해 자동 설명보다 자연스러운 문구로 보강

### 2. 빌드 안정화

- [ ] `auth-service` 기존 Lombok/DTO 컴파일 오류 정리
- [ ] 멀티모듈 전체 `compileJava` 재검증
- [ ] Swagger 관련 변경 이후 서비스별 실행 검증 기준 정리

### 3. 기능 구현 연동

- [ ] 새 REST API 추가 시 OpenAPI 생성 스크립트와 문서 반영 흐름 유지
- [ ] `README.md`에 문서 확인 방법과 SwaggerHub 반영 절차 보강
- [ ] 로컬 개발 시작 방법 문서화
- [x] `supply-service` 신규 Kafka 계약 기본 발행 로직 추가
  - [x] `logistics-node.capacity-status-changed`
  - [x] `inventory.shortage-detected`
  - [x] `supplier.score-dropped`
  - [x] `supplier.esg-violated`
  - [ ] `lot.expiration-imminent` 배치/스케줄 감지 발행
  - [ ] `supplier-certificate.expiring`
  - [ ] `supplier-certificate.expired`
- [ ] `supply-service` 도메인 이벤트 발행 지점 확장/검증
  - [ ] 물류거점 생성/수정/활성화/비활성화 이벤트 계약 필요 여부 결정
  - [x] 물류거점 용량 상태 변경 흐름 Kafka 이벤트 발행
  - [x] LOT 생성/상태 변경/품질 상태 변경 시 Kafka 이벤트 발행
  - [x] 발주 생성/수정/거절/취소/확정 흐름 Kafka 이벤트 발행
  - [x] 하위 발주 생성/승인/거절 흐름 Kafka 이벤트 발행
  - [x] 출하/배송 예외 흐름 Kafka 이벤트 발행
  - [x] 반품 요청/상태 변경 흐름 Kafka 이벤트 발행
  - [x] 협력사 인증서 생성/승인/거절/철회 흐름 Kafka 이벤트 발행
  - [ ] 협력사 인증서 만료 임박/만료 배치 발행

### 4. Kafka E2E 검증

- [x] 로컬 인프라 compose 파일 추가
- [ ] `application-local.yml` 로컬 인프라 기준 정리 또는 실행 환경변수 예시 추가
- [ ] `docker compose -f docker/docker-compose.infra.yml up -d` 로컬 인프라 기동 확인
- [ ] `supply-service` API 호출 후 `outbox_event` 적재 확인
- [ ] `SupplyOutboxPublisher`가 Kafka 토픽으로 발행하는지 확인
- [ ] `control-service` consumer가 이벤트를 수신하고 알림을 저장하는지 확인
- [ ] Redis publish 및 FE 알림 수신 확인
- [ ] 이벤트 규칙 OFF 시 알림 미생성 및 triggered count 미증가 확인

## 인프라/배포 작업

- [ ] Dockerfile을 각 서비스에 추가 또는 정리
- [x] 로컬 개발용 인프라 compose 파일 추가
- [ ] K8s 이미지 경로 실제 값으로 교체
- [ ] `k8s/secrets/*.yaml` 실제 운영 값 전략 정리
- [ ] Redis/Kafka/Elasticsearch/S3 접속 정보 표준화
- [ ] Ingress 경로와 Gateway 경로 규칙 검증

## 문서/설계 동기화

- [ ] ERD 기준과 실제 엔티티 구현 범위 맞추기
- [ ] 패키지 구조 원칙을 필요시 `AGENTS.md`에 보강

## 확인 체크리스트

- [ ] `bash gradlew projects`
- [ ] `bash gradlew test`
- [ ] `rtk ./gradlew compileJava`
- [ ] 각 서비스 `application-local.yml` 로컬 실행 확인
- [ ] `api-gateway` → 각 서비스 로컬 라우팅 확인
- [ ] IntelliJ에서 중복 Gradle import 없는지 확인
- [x] `docs/openapi/atlas-backend-openapi.json` 유효성 확인
- [x] 컨트롤러 REST 매핑 수와 OpenAPI operation 수 일치 확인

## 이후 확장 후보

- [ ] Spring Cloud Config Server 도입 여부 결정
- [ ] 공통 모듈(`common`)이 정말 필요한지 재평가
- [ ] control-service 내 sLLM 연동 방식 구체화
- [ ] 협업/화상회의를 별도 서비스로 분리할지 검토
