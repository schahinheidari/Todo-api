FROM openjdk:11-jre-slim

RUN groupadd -r springboot && useradd -r -g springboot springboot

RUN mkdir -p /app && chown springboot:springboot /app

WORKDIR /app

COPY target/todo-api-0.0.1-SNAPSHOT.jar app.jar

RUN chown springboot:springboot app.jar

USER springboot

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]