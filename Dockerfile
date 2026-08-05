FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /work
RUN useradd --system --uid 10001 appuser
COPY --from=build --chown=appuser:appuser /workspace/target/quarkus-app/ ./
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
