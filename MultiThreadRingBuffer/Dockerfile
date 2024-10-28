FROM maven:3.8.1-openjdk-17 as build

WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]