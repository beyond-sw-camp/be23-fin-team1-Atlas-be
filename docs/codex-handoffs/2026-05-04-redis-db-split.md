# Redis DB Split Handoff

## Context

- Branch: `chore/remove-chat-typing`
- Goal: separate Redis usage by purpose and remove unused chat typing WebSocket flow.
- Redis DB mapping implemented in code:
  - DB 0: auth verification keys
  - DB 1: auth refresh token keys
  - DB 2: control notification Redis template
  - DB 3: control chat Redis template
- DB indexes are configurable through `atlas.redis.*.database` in service yml files and K8s env vars.

## Changes

- `auth-service`
  - Added `AuthRedisConfig` with DB 0 verification and DB 1 session `StringRedisTemplate` beans.
  - Added `atlas.redis.verification.database` and `atlas.redis.session.database` settings.
  - Moved login IP verification and password-change verification from JPA repository storage to Redis DB 0 with 3 minute TTL.
  - Changed refresh token Redis access to DB 1 and key prefix `auth:refresh-token:{userId}`.
  - Removed password-change verification entity/repository.
  - Converted `LoginVerification` from JPA entity to an in-memory result object used by controller flow.

- `control-service`
  - Added `ControlRedisConfig` with notification DB 2 and chat DB 3 templates.
  - Added `atlas.redis.notification.database` and `atlas.redis.chat.database` settings.
  - Chat message publishing and presence use `chatRedisTemplate`.
  - Notification publishing uses `notificationRedisTemplate`.
  - Removed unused chat typing DTO, STOMP mapping, Redis topic constant, and subscriber branch.

## Notes

- Redis Pub/Sub channels are not meaningfully isolated by logical DB index. Channel prefixes still provide the actual chat/notification separation.
- Existing refresh tokens stored in the old DB 0 raw `userId` key format will not validate after this change.
- Existing JPA verification table data is no longer read by the application.
- Cleanup executed against `auth_db` on 2026-05-04:
  - `login_verification` existed with 1 row before cleanup.
  - `password_change_verification` existed with 0 rows before cleanup.
  - Both tables were dropped and verified missing after cleanup.
- If the deployment Redis is Redis Cluster or a managed Redis that does not support multiple logical DBs, replace DB index separation with separate Redis instances or prefix-only separation.

## Verification

- `./gradlew :auth-service:compileJava` succeeded.
- `./gradlew :control-service:compileJava` succeeded.
