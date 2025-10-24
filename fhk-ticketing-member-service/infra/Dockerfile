FROM ubuntu:latest
LABEL authors="JUNO"

ENTRYPOINT ["top", "-b"]


# 모듈별 브랜치 분기 필요  Profile ???

#JVM 기반 base 이미지
FROM eclipse-temurin:17-jre

# jenkins docker build 시 주입할 환경 변수 (docker build --build-arg AAA=${AAA}
ARG JAR_PATH

ARG SERVER_PORT
ARG DB_URL
ARG DB_USER
ARG DB_PASSWD

# set 환경변수
ENV SERVER_PORT=${SERVER_PORT}
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}


# 이미지 app 복사
COPY ${JAR_PATH} /app/app.jar

# jar 파일 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]