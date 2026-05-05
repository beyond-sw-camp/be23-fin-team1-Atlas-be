# Supply Process Number Chain

## 변경 의도

- `SHIP-PO-...`, `RTN-SHIP-PO-...`, `RS-RTN-SHIP-PO-...`처럼 프로세스 번호가 누적되는 구조 제거.
- 발주, 출하, 반품, 반품출하, 교환출하는 각 도메인별 독립 번호를 사용.
- 프로세스 추적은 번호 문자열이 아니라 DB 내부 id/publicId 연결로 처리.

## 적용 방식

- `SequenceCodeType`에 `SHIP`, `RTN`, `RS`, `EX` 독립 시퀀스 타입 추가.
- 일반 출하 번호는 `SHIP-yyyy-0000001` 형식으로 생성.
- 반품 번호는 `RTN-yyyy-0000001` 형식으로 생성.
- 반품 출하 번호는 `RS-yyyy-0000001` 형식으로 생성.
- 교환 출하 번호는 `EX-yyyy-0000001` 형식으로 생성.
- `return_request`에 원출하/반품출하/교환출하 연결용 내부 id와 교환 출하 publicId 추가.

## DB 반영 필요

운영 프로필은 `ddl-auto: none`이므로 배포 DB에는 아래 DDL 반영 필요.

```sql
ALTER TABLE return_request
  ADD COLUMN IF NOT EXISTS source_shipment_id bigint,
  ADD COLUMN IF NOT EXISTS return_shipment_id bigint,
  ADD COLUMN IF NOT EXISTS exchange_shipment_public_id varchar(26),
  ADD COLUMN IF NOT EXISTS exchange_shipment_id bigint;
```

## 검증

- `bash gradlew :supply-service:compileJava` 성공.
