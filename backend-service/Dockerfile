# ---- Build Stage ----
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
# Copy just the pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline
# Copy the rest and build
COPY src ./src
RUN mvn clean package

# ---- Run Stage ----
FROM openjdk:17
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]