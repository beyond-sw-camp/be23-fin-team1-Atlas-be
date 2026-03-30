# Local Infra

로컬 개발용 인프라는 `docker/docker-compose.infra.yml`로 실행한다.

## 시작

```bash
docker compose -f docker/docker-compose.infra.yml up -d
```

## 중지

```bash
docker compose -f docker/docker-compose.infra.yml down
```

## 초기화

```bash
docker compose -f docker/docker-compose.infra.yml down -v
```

## 포함된 인프라

- MariaDB (`3306`)
- PostgreSQL (`5432`)
- Redis (`6379`)
- Kafka (`9092`)
- Elasticsearch (`9200`)

## 데이터베이스

- MariaDB
  - `auth_db`
  - `supply_db`
  - `file_db`
- PostgreSQL
  - `control_db`
  - 기본 스키마: `atlas`

## 기본 계정

- MariaDB
  - username: `atlas`
  - password: `atlas`
- PostgreSQL
  - username: `atlas`
  - password: `atlas`

## 주의

- `docker-entrypoint-initdb.d` 스크립트는 볼륨이 비어 있을 때만 실행된다.
- 이미 한 번 띄운 뒤 계정/DB/스키마 초기화 스크립트를 바꿨다면 아래처럼 볼륨까지 내리고 다시 올려야 반영된다.

```bash
docker compose -f docker/docker-compose.infra.yml down -v
docker compose -f docker/docker-compose.infra.yml up -d
```
