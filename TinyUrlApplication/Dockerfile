FROM gradle:7.6.0-jdk17 as build
WORKDIR /app

COPY build.gradle settings.gradle* ./
RUN gradle clean bootJar --no-daemon

COPY . .
RUN gradle build -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]