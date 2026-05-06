# 물품 미디어 지원 작업 메모

## 반영 범위

- `supply-service` 물품에 `primaryMediaFilePublicId` nullable 컬럼 추가.
- `PATCH /api/supply/items/{itemPublicId}/media/{filePublicId}/primary` 추가.
- 대표 미디어 지정 전 file-service의 `ITEM` 첨부를 조회해 해당 물품 파일인지, 이미지 파일인지 검증.
- 물품 목록/상세/발주/하위발주/출하 관련 응답 DTO에 대표 미디어 public id 전달.
- FE는 별도 브랜치 `feat/item-media-ui`에서 물품 등록/상세/발주 화면의 이미지/동영상 표시와 업로드 UI를 함께 반영.

## 확인

- BE 검증: `./gradlew :supply-service:test`
- FE 검증: `npm run build`

## 후속 후보

- file-service에 품목 미디어 삭제, 정렬, 대표 이미지 API를 통합할지 결정.
- 현재 대표 지정은 supply-service가 `ITEM` 첨부의 이미지 파일인지 검증한 뒤 물품 컬럼만 갱신.
- 물품 미디어 목록은 FE에서 `/api/files/attachments/by-ref?refType=ITEM` 조회를 사용.
