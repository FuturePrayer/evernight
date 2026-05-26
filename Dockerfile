FROM maven:3.9.15-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:26-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN addgroup --system evernight && adduser --system --ingroup evernight evernight
COPY --from=build /workspace/target/evernight.jar /app/evernight.jar
EXPOSE 25924
ENTRYPOINT ["java", "-jar", "/app/evernight.jar"]
