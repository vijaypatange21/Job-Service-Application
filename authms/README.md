# authms

Production-grade authentication microservice for the JobApp microservices system.

## Runtime

- Spring Boot 3.5.6
- Spring Security 6
- PostgreSQL
- Eureka Client
- Spring Cloud Config Client
- BCrypt password hashing
- Stateless JWT authentication

## APIs

- `POST /api/v1/auth/register` creates a user and returns a JWT.
- `POST /api/v1/auth/login` authenticates credentials and returns a JWT.
- `POST /api/v1/auth/validate` validates the Bearer token through the security filter and returns the authenticated subject.

## File Map

- `authms/pom.xml` declares Spring Boot, Security, JPA, PostgreSQL, Eureka, Config Client, validation, Lombok, JWT, and test dependencies.
- `authms/src/main/resources/application.yml` configures service name, port, PostgreSQL, Config Server, Eureka registration, actuator, logging, and JWT properties.
- `authms/src/main/java/com/authms/AuthmsApplication.java` boots the service, enables discovery, and binds JWT configuration properties.
- `authms/src/main/java/com/authms/config/JwtProperties.java` validates strongly typed JWT settings from configuration.
- `authms/src/main/java/com/authms/config/SecurityConfig.java` defines stateless Spring Security, public auth routes, JWT filter registration, `AuthenticationManager`, `AuthenticationProvider`, and BCrypt `PasswordEncoder`.
- `authms/src/main/java/com/authms/controller/AuthController.java` exposes DTO-based register, login, and token validation APIs.
- `authms/src/main/java/com/authms/dto/request/RegisterRequest.java` models and validates registration input.
- `authms/src/main/java/com/authms/dto/request/LoginRequest.java` models and validates login input.
- `authms/src/main/java/com/authms/dto/response/AuthResponse.java` returns token metadata and sanitized user data.
- `authms/src/main/java/com/authms/dto/response/TokenValidationResponse.java` returns the authenticated subject for valid tokens.
- `authms/src/main/java/com/authms/dto/response/ErrorResponse.java` gives all API errors a consistent JSON shape.
- `authms/src/main/java/com/authms/entity/AppUser.java` persists user identity, password hash, account state, roles, and audit timestamps.
- `authms/src/main/java/com/authms/entity/Role.java` defines supported roles: `USER`, `ADMIN`, and `RECRUITER`.
- `authms/src/main/java/com/authms/repository/AppUserRepository.java` provides user persistence lookup methods for authentication.
- `authms/src/main/java/com/authms/security/AppUserPrincipal.java` adapts `AppUser` to Spring Security `UserDetails`.
- `authms/src/main/java/com/authms/security/JwtAuthenticationFilter.java` extracts and validates Bearer tokens once per request.
- `authms/src/main/java/com/authms/security/RestAuthenticationEntryPoint.java` returns JSON `401` responses instead of default HTML responses.
- `authms/src/main/java/com/authms/service/AuthService.java` defines authentication use cases.
- `authms/src/main/java/com/authms/service/CustomUserDetailsService.java` loads users for Spring Security authentication.
- `authms/src/main/java/com/authms/service/JwtService.java` defines token generation and validation operations.
- `authms/src/main/java/com/authms/service/impl/AuthServiceImpl.java` implements registration, login, password hashing, role assignment, and JWT issuing.
- `authms/src/main/java/com/authms/service/impl/JwtServiceImpl.java` signs, parses, and validates JWTs.
- `authms/src/main/java/com/authms/exception/DuplicateResourceException.java` represents registration conflicts.
- `authms/src/main/java/com/authms/exception/InvalidCredentialsException.java` hides credential failure details behind a safe auth error.
- `authms/src/main/java/com/authms/exception/GlobalExceptionHandler.java` centralizes validation, conflict, auth, and unexpected error responses.
- `authms/src/test/java/com/authms/AuthmsApplicationTests.java` verifies the application context using an isolated H2 database.

## Production Notes

- Override `JWT_SECRET` in every deployed environment. The default value is for local development only.
- Public registration defaults users to `USER`; it rejects client-side `ADMIN` assignment.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for local development. Use Flyway or Liquibase migrations before production.
