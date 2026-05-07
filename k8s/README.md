# Kubernetes Manifests

적용 순서:

1. `namespace.yaml`
2. `redis.yaml`
3. `kafka.yaml`
4. `elasticsearch.yaml`
5. `api-gateway/service.yaml`, `api-gateway/deployment.yaml`
6. `auth-service/service.yaml`, `auth-service/deployment.yaml`
7. `control-service/service.yaml`, `control-service/deployment.yaml`
8. `supply-service/service.yaml`, `supply-service/deployment.yaml`
9. `file-service/service.yaml`, `file-service/deployment.yaml`
10. `ingress/ingress.yaml`

운영 배포 기준:

- EKS 노드 그룹은 `t3.large` 온디맨드, 최소 3대, 최대 4대, 원하는 크기 3대로 둡니다.
- 프론트엔드는 S3와 CloudFront로 배포하고, K8s에는 백엔드와 미들웨어만 배포합니다.
- DB는 RDS MariaDB/PostgreSQL을 사용합니다.
- Redis, Kafka, Elasticsearch는 K8s 내부 Pod와 ClusterIP Service로 배포합니다.
- AI orchestrator service는 1차 배포에서 제외합니다.
- `deployment.yaml`의 `image` 값은 ECR 이미지 주소를 사용합니다.
- `<AWS_ACCOUNT_ID>`와 `<YOUR_ACM_CERT_ARN>`은 배포 전 실제 값으로 치환해야 합니다.
- `secrets/app-secret.yaml`, `secrets/jwt-secret.yaml`은 예시 파일입니다. 실제 배포에서는 `atlas-secrets` Secret을 직접 생성해서 사용합니다.

필수 Secret 이름:

`atlas-secrets`

필수 Secret key:

- `MARIADB_HOST`
- `MARIADB_PASSWORD`
- `POSTGRES_HOST`
- `POSTGRES_PASSWORD`
- `REDIS_HOST`
- `REDIS_PASSWORD`
- `ELASTICSEARCH_USERNAME`
- `ELASTICSEARCH_PASSWORD`
- `JWT_ACCESS_TOKEN_SECRET`
- `JWT_REFRESH_TOKEN_SECRET`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `KAKAO_REST_API_KEY`

내부 Service DNS:

- Redis: `redis-service:6379`
- Kafka: `kafka-service:9092`
- Elasticsearch: `http://elasticsearch-service:9200`

주의:

- RDS 보안 그룹은 EKS 노드 그룹 보안 그룹에서만 접근 가능하게 제한합니다.
- Ingress는 `api.atlas-scm.cloud`를 `api-gateway` Service로 라우팅합니다.
- `ingress/ingress.yaml`의 ACM 인증서는 서울 리전(`ap-northeast-2`) 인증서 ARN을 사용합니다.
- CloudFront용 인증서는 별도로 `us-east-1` 리전에 발급해야 합니다.
