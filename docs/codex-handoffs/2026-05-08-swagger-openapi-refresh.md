# 2026-05-08 Swagger/OpenAPI refresh

## Context

- Branch: `docs/swagger-openapi-refresh`
- Request: BE controller/DTO code 기준으로 Swagger/OpenAPI 명세를 전부 갱신하고 `docs` 아래 JSON 파일 업데이트.
- Existing state: `docs/openapi/atlas-backend-openapi.yaml`만 있고 `docs/openapi/atlas-backend-openapi.json`은 없었다.

## Changes

- `tools/generate_openapi_from_controllers.py`
  - 기존 JSON 파일이 없어도 기본 OpenAPI skeleton으로 새 JSON을 생성하도록 수정.
  - 최근 추가 컨트롤러 라벨 반영: Department, ItemInventory, KafkaMonitoring, SupplySidebarBadge.
- `docs/openapi/atlas-backend-openapi.json`
  - 컨트롤러/DTO 기준으로 재생성.
  - REST mapping 218개와 OpenAPI operation 218개 일치 확인.
- 서비스 DTO/검색 DTO Swagger 보강
  - `api-gateway`, `auth-service`, `supply-service`, `control-service`, `file-service`, `common`의 DTO 계열 파일 중 `@Schema`가 전혀 없던 파일을 보강.
  - DTO/검색 DTO 기준 `@Schema` 미적용 파일 0개 확인.

## Verification

- `python3 tools/generate_openapi_from_controllers.py`
- `jq empty docs/openapi/atlas-backend-openapi.json`
- `bash gradlew compileJava`
- DTO/검색 DTO `@Schema` 누락 파일 수 확인: 0개

## Notes

- `.gitignore`가 `docs/`와 `/tools`를 ignore한다. 이번 Swagger 산출물과 생성 스크립트는 `git add -f`가 필요하다.
- 기존 YAML은 갱신하지 않았다. 현재 요청은 JSON 갱신 기준으로 처리했다.
