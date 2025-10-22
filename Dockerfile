FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /home/gradle/project
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew --no-daemon dependencies
COPY src src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]