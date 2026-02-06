FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/virtual-thread-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Xmx512m", "-jar", "app.jar"]
