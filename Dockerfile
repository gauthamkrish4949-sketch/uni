# Step 1: Build the app using Maven and OpenJDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Change 'unistore' below if your folder name is different (e.g. unistore_final/unistore)
WORKDIR /app/unistore
RUN ./mvnw clean package -DskipTests

# Step 2: Run the app using OpenJDK 17
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the compiled jar from the nested target directory
COPY --from=build /app/unistore/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
