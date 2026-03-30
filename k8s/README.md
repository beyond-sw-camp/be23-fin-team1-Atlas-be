# Kubernetes Manifests

적용 순서:

1. `namespace.yaml`
2. `secrets/jwt-secret.yaml`
3. `secrets/app-secret.yaml`
4. 각 서비스의 `deployment.yaml`, `service.yaml`
5. `ingress/ingress.yaml`

주의:

- `image` 값은 실제 레지스트리 경로로 바꿔야 합니다.
- `EXAMPLE_CHANGE_ME` 값들은 실제 환경값으로 교체해야 합니다.
- JWT 시크릿은 `stringData` 기준으로 넣어두었으니 운영 시에는 외부 Secret 관리 방식으로 대체하는 것이 좋습니다.
- 로컬 실행은 각 서비스의 `application-local.yml` 기준입니다.
- K8s 실행은 각 서비스의 `application-k8s.yml`과 Deployment 환경변수 기준입니다.
