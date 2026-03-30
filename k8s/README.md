# Kubernetes Manifests

적용 순서:

1. `namespace.yaml`
2. `secrets/jwt-secret.yaml`
3. `secrets/app-secret.yaml`
4. 각 서비스의 `deployment.yaml`, `service.yaml`
5. `ingress/ingress.yaml`

주의:

- 현재 `deployment.yaml`의 `image` 값은 로컬 Docker Desktop Kubernetes 테스트 기준으로 `*:local` 이미지명을 사용합니다.
- 로컬에서 적용 전 아래 순서로 jar와 이미지를 먼저 빌드해야 합니다.
  - `bash gradlew :api-gateway:bootJar :auth-service:bootJar :supply-service:bootJar :control-service:bootJar :file-service:bootJar`
  - `docker build -t api-gateway:local ./api-gateway`
  - `docker build -t auth-service:local ./auth-service`
  - `docker build -t supply-service:local ./supply-service`
  - `docker build -t control-service:local ./control-service`
  - `docker build -t file-service:local ./file-service`
- 운영 배포 시에는 `image` 값을 실제 레지스트리 경로로 다시 바꿔야 합니다.
- `EXAMPLE_CHANGE_ME` 값들은 실제 환경값으로 교체해야 합니다.
- JWT 시크릿은 `stringData` 기준으로 넣어두었으니 운영 시에는 외부 Secret 관리 방식으로 대체하는 것이 좋습니다.
- 로컬 실행은 각 서비스의 `application-local.yml` 기준입니다.
- 운영/K8s 실행은 각 서비스의 `application-prod.yml`과 Deployment 환경변수 기준입니다.
