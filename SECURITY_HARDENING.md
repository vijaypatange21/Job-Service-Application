# Production Security Hardening

This implementation keeps `authms` as the token authority and `api_gateway` as the enforcement point for external traffic.

## RBAC

- `authms/src/main/java/com/authms/entity/Role.java` defines application roles.
- `api_gateway/src/main/java/com/jobapp/api_gateway/config/SecurityConfig.java` enforces route-level authorization:
  - `/jobs/**`, `/companies/**`, `/reviews/**` require `USER` or `ADMIN`.
  - `/admin/**` requires `ADMIN`.
  - `/auth/**`, `/eureka/**`, and `/actuator/**` are public.
- `authms/src/main/java/com/authms/controller/AdminUserController.java` and `authms/src/main/java/com/authms/service/impl/UserAdminServiceImpl.java` use `@PreAuthorize("hasRole('ADMIN')")` for method-level authorization.

## Refresh Tokens

- `authms/src/main/java/com/authms/entity/RefreshToken.java` stores hashed opaque refresh tokens.
- `authms/src/main/java/com/authms/service/impl/RefreshTokenServiceImpl.java` creates, rotates, revokes, and detects reuse of refresh tokens.
- `POST /api/v1/auth/refresh` accepts a refresh token, revokes it, creates a replacement, and issues a new access token.
- `POST /api/v1/auth/logout` revokes the refresh token and blacklists the current access token until its natural expiry.

## Token Blacklist

- `authms/src/main/java/com/authms/service/impl/RedisTokenBlacklistService.java` writes revoked access token hashes to Redis.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JwtTokenService.java` rejects access tokens whose SHA-256 hash is present in Redis.
- Redis keys use the prefix `jwt:blacklist:` and expire automatically when the JWT would have expired.

## Request Tracing And Logging

- `api_gateway/src/main/java/com/jobapp/api_gateway/filter/CorrelationIdGlobalFilter.java` creates or propagates `X-Correlation-Id`.
- `authms/src/main/java/com/authms/config/CorrelationIdFilter.java` propagates the same correlation ID.
- `authms/src/main/java/com/authms/service/impl/Slf4jSecurityAuditService.java` logs security events such as login, failed login, refresh, and logout.

## Rate Limiting

- `api_gateway/src/main/java/com/jobapp/api_gateway/config/RateLimitConfig.java` keys rate limits by authenticated principal or client IP.
- `api_gateway/src/main/resources/application.properties` and `application-docker.properties` apply Redis-backed `RequestRateLimiter` filters to auth and protected service routes.

## Secure Service-To-Service Calls

- `api_gateway/src/main/java/com/jobapp/api_gateway/filter/JwtClaimsRelayGatewayFilterFactory.java` strips spoofed identity headers, adds trusted identity headers, and adds `X-Internal-Service-Secret`.
- `jobms`, `companyms`, and `reviewms` each have `InternalServiceAuthenticationFilter` to reject direct calls without the internal secret.
- Their `InternalFeignAuthConfig` classes add the same internal header to Feign service-to-service calls.

## Secrets

- JWT and internal service secrets are environment variables:
  - `JWT_ISSUER`
  - `JWT_SECRET`
  - `INTERNAL_SERVICE_SECRET`
  - `REDIS_PASSWORD`
- `.env.example` documents the required environment variables.
- Local defaults exist only to keep development bootstrappable. Production deployments must inject strong secrets through a secret manager or orchestrator secrets.

## Docker

- `authms/Dockerfile` and `api_gateway/Dockerfile` build non-root container images.
- `docker-compose.yaml` adds Redis, builds `authms` and `api_gateway`, wires shared secrets through environment variables, and passes the internal secret to downstream services.
