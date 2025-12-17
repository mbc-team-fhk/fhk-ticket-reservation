FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Gradle bootJar 결과물을 항상 이 이름으로 나오게 맞춰놓는 걸 추천
# (build.gradle: bootJar { archiveFileName = 'xxx-service.jar.' })
# jenkins docker build 시 주입할 환경 변수 (docker build --build-arg AAA=${AAA Path}
# *** 기본값 의미 없음
ARG JAR_PATH=build/libs/fhk-ticketing-ticket-service.jar

# set 환경변수
# 기본값작성, 실제 프로필은 k8s Deployment env 에서 덮어씀
ENV SERVER_PORT=8080

# 이미지 app 복사
COPY ${JAR_PATH} /app/app.jar

# jar 파일 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]