# Base image: OpenJDK 17 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 후 생성된 JAR 파일 복사
COPY build/libs/member-service-*.jar member-service.jar

# API Gateway 서버 포트 노출
EXPOSE 9001

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "member-service.jar"]
