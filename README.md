# Unique Code Generation System

This system is designed to efficiently generate a specified number of unique 7-character alphanumeric codes upon user request.

## 🛠 Technologies Used

- **Spring Boot 3.1**: 
  - The backbone of our application, offering a rich framework to build stand-alone, production-ready Spring applications swiftly.
- **Thymeleaf**: 
  - Our chosen templating engine. It's favored for its natural templating capability and smooth integration with Spring Boot.
- **Hibernate with StatelessSession**: 
  - This is the secret sauce for efficient bulk operations. Stateless sessions provide a speed boost during bulk inserts by bypassing some of the overheads found in typical Hibernate sessions.
- **MySQL**: 
  - Our robust RDBMS solution, where all the generated codes and request logs reside.
- **Docker and Docker Compose**: 
  - These containerization tools ensure that our app and its dependencies (like MySQL) remain consistent across different environments.

## 🤔 Why These Technologies?

1. **Spring Boot**: 
   - It offers an expansive ecosystem which addresses many challenges inherent to building enterprise-level applications.
2. **Thymeleaf**: 
   - We love its natural syntax that is both readable and writable. Plus, it's a perfect match with Spring apps.
3. **Hibernate's StatelessSession**: 
   - Our choice for performance. It's especially beneficial during bulk insert operations.
4. **MySQL**: 
   - A tried-and-tested robust relational database system.
5. **Docker**: 
   - Simplifies deployments and guarantees that our app functions uniformly across different environments.

## 🚀 How to Run the Project

1. **Prerequisites**: 
   - Make sure Docker and Docker Compose are installed on your machine.

2. **Setting Up**:
    ```bash
    git clone https://github.com/nahidrn/code-generator-benchmarking
    cd code-generator-benchmarking
    ```

3. **Using Docker Compose**:
    ```bash
    docker-compose up --build
    ```

4. It takes around a minute to build and run the first time. Your application should be alive and kicking at: `http://localhost:8032`

5. **Shutting It Down**:
    ```bash
    docker-compose down
    ```