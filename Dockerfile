FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean install -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/connect4-backend-1.0.0.jar"]

