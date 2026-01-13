# ---------- build stage ----------
FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY . .

# 어떤 모듈을 빌드할지 선택 (billing / message / billing-batch)
ARG MODULE=billing
RUN gradle :${MODULE}:clean :${MODULE}:bootJar -x test

# ---------- run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

ARG MODULE=billing
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]