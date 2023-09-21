# ---- Build Stage ----
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
# Copy the pom.xml and src directory (containing the source code) into the image
COPY pom.xml .
COPY src ./src
# Build the application
RUN mvn clean package

# ---- Run Stage ----
FROM openjdk:17
# Set the application's jar to app.jar
COPY --from=build /app/target/*.jar app.jar
# Specify the command to run on container start
ENTRYPOINT ["java","-jar","/app.jar"]