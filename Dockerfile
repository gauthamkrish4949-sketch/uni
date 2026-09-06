# Step 1: Build the app using Maven and OpenJDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Change to the exact directory containing pom.xml
WORKDIR /app/UniStore_Live_Tracking_Fixed/unistore_final/unistore
RUN mvn clean package -DskipTests

# Step 2: Run the app using OpenJDK 17
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/UniStore_Live_Tracking_Fixed/unistore_final/unistore/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
