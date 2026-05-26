FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
RUN mkdir -p src/main/java && echo "package bootstrap; public class Placeholder {}" > src/main/java/Placeholder.java && \
    ./mvnw -q -DskipTests dependency:go-offline || true
COPY backend .
RUN if [ -f mvnw ]; then chmod +x mvnw && ./mvnw -q -DskipTests package; else \
      apk add --no-cache maven && mvn -q -DskipTests package; fi

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
