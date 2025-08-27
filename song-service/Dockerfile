FROM gradle:jdk21-alpine as build
LABEL authors="DzmitryBarkouski1"

WORKDIR /app

COPY ./gradle ./gradle
COPY ./build.gradle .
COPY --chmod=755 ./gradlew .
COPY ./src ./src

RUN ./gradlew dependencies --no-daemon
RUN ./gradlew assemble --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine-3.22 as deploy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

EXPOSE 8081

CMD ["java", "-jar", "./app.jar"]