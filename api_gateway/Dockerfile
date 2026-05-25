FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system spring --ingroup spring
COPY --from=build /workspace/target/*.jar app.jar
USER spring:spring
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
