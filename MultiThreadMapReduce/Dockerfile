FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app

RUN mkdir -p /app/src/main/resources/files /app/src/main/resources/processed

COPY src/main/resources/files /app/src/main/resources/files

COPY --from=build /app/target/*.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]