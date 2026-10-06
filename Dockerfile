FROM eclipse-temurin:21-jdk AS build
WORKDIR /app


COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon


COPY src src
RUN ./gradlew bootJar --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app


RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]