# 1단계: Gradle 빌드
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2단계: 경량 런타임
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV TZ=Asia/Seoul
RUN mkdir -p /app/uploads

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]