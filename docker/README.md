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

## 기본 계정

- MariaDB
  - username: `atlas`
  - password: `atlas`
- PostgreSQL
  - username: `atlas`
  - password: `atlas`
