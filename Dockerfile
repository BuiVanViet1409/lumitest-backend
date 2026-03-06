# Stage 1: Build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime
FROM mcr.microsoft.com/playwright/java:v1.41.0-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Tạo thư mục cho screenshots và video
RUN mkdir -p /app/screenshots

# Biến môi trường mặc định
ENV SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/lumitest

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
