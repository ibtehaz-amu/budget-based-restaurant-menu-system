 FROM openjdk:17-jdk-slim
WORKDIR /app
COPY backend/backend/pom.xml .
COPY backend/backend/mvnw .
COPY backend/backend/.mvn .mvn
RUN chmod +x mvnw
COPY backend/backend/src ./src
RUN ./mvnw clean package -DskipTests
RUN cp target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]