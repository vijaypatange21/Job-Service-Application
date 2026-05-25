# API Gateway JWT Security

The gateway validates JWTs before forwarding protected traffic to downstream services.

## Public Routes

- `/auth/**` forwards to `authms` and rewrites to `/api/v1/auth/**`.
- `/eureka/**` forwards to Eureka.
- `/actuator/**` is public for health and operational checks.

## Protected Routes

- `/jobs/**` requires `ROLE_USER`, `ROLE_RECRUITER`, or `ROLE_ADMIN`.
- `/companies/**` requires `ROLE_USER`, `ROLE_RECRUITER`, or `ROLE_ADMIN`.
- `/reviews/**` requires `ROLE_USER` or `ROLE_ADMIN`.
- `/admin/**` requires `ROLE_ADMIN`.

## Files

- `api_gateway/pom.xml` adds Spring Security and JJWT dependencies for reactive JWT validation.
- `api_gateway/src/main/resources/application.properties` defines public/protected routes, route filters, JWT issuer, and JWT secret.
- `api_gateway/src/main/java/com/jobapp/api_gateway/ApiGatewayApplication.java` enables typed JWT configuration binding.
- `api_gateway/src/main/java/com/jobapp/api_gateway/config/JwtProperties.java` validates gateway JWT settings.
- `api_gateway/src/main/java/com/jobapp/api_gateway/config/SecurityConfig.java` configures reactive Spring Security, disables stateful login mechanisms, registers JWT authentication, and applies role-based route authorization.
- `api_gateway/src/main/java/com/jobapp/api_gateway/dto/GatewayErrorResponse.java` defines a consistent JSON error body.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/BearerTokenServerAuthenticationConverter.java` extracts `Authorization: Bearer <token>` from incoming requests.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JwtAuthenticationToken.java` carries the raw bearer token into the reactive authentication manager.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JwtReactiveAuthenticationManager.java` validates the token and creates an authenticated Spring Security principal.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JwtTokenService.java` verifies JWT signature, issuer, expiration, subject, and role claims using the same HMAC secret as `authms`.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JsonServerAuthenticationEntryPoint.java` returns JSON `401 Unauthorized` responses.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/JsonServerAccessDeniedHandler.java` returns JSON `403 Forbidden` responses for authenticated users without enough roles.
- `api_gateway/src/main/java/com/jobapp/api_gateway/security/GlobalGatewayExceptionHandler.java` returns JSON responses for unexpected gateway exceptions.
- `api_gateway/src/main/java/com/jobapp/api_gateway/filter/JwtClaimsRelayGatewayFilterFactory.java` strips spoofed identity headers and forwards trusted `X-Authenticated-User` and `X-Authenticated-Roles` headers to protected downstream services.
- `api_gateway/src/main/java/com/jobapp/api_gateway/config/RateLimitConfig.java` configures Redis-backed route rate limiting keys.
- `api_gateway/src/main/java/com/jobapp/api_gateway/filter/CorrelationIdGlobalFilter.java` propagates `X-Correlation-Id`.
- `api_gateway/src/main/java/com/jobapp/api_gateway/util/TokenHashing.java` hashes JWTs before Redis blacklist lookups.

## Required Runtime Secret

Set the same `JWT_SECRET` and `JWT_ISSUER` in both `authms` and `api_gateway`.

```bash
JWT_ISSUER=jobapp-authms
JWT_SECRET=<strong-shared-hmac-secret-at-least-32-bytes>
```

The checked-in default is only for local development.
