# Unique Code Generation System

This system is designed to efficiently generate a specified number of unique 7-character alphanumeric codes upon user request.

## 🛠 Technologies Used

- **Spring Boot 3.1**: 
  - The backbone of my application, offering a rich framework to build stand-alone, production-ready Spring applications swiftly.
- **Thymeleaf**: 
  - Thymeleaf is favored for its natural templating capability and smooth integration with Spring Boot.
- **Hibernate with StatelessSession**: 
  - This is the secret sauce for efficient bulk operations. Stateless sessions provide a speed boost during bulk inserts by bypassing some of the overheads found in typical Hibernate sessions.
- **MySQL**: 
  - Robust RDBMS solution, where all the generated codes and request logs reside.
- **Docker and Docker Compose**: 
  - These containerization tools ensure that the app and its dependencies (like MySQL) remain consistent across different environments.

## 🚀 How to Run the Project

1. **Prerequisites**: 
   - Make sure Docker and Docker Compose are installed on any machine.

2. **Setting Up**:
    ```bash
    git clone https://github.com/nahidrn/code-generator-benchmarking
    cd code-generator-benchmarking
    ```

3. **Using Docker Compose**:
    ```bash
    docker-compose up --build
    ```

    Note: In case the first time if web service is random-code-generator-web-1 is still down after random-code-generator-mysql-db-1 is up. Try just starting that container again. That means the volume was ready but the table was not created in time web was trying to connect it.

4. It takes around 5 minutes to build and run the first time. Your application should be alive and kicking at: `http://localhost:8032`

5. **Shutting It Down**:
    ```bash
    docker-compose down
    ```


## CodeService Logic Explanation

### Overview
`CodeService` is a core component of the application responsible for generating unique codes. This service contains methods for handling the code generation logic, saving these codes, and handling associated metadata for code generation requests.

## Key Methods

### `generateCodes`

Orchestrates the complete code generation and persistence workflow. Specifically, it:
1. Notes the initiation time and desired code quantity.
2. Produces a list of distinct codes.
3. Stores the codes in the database utilizing a stateless session to optimize bulk insertions.
4. Logs the duration taken for both the code production and database operations.
5. Updates the generation request with the ending time.

### `convertToBase62`

A utility function that alters a given number to its base 62 (alphanumeric) representation. The outcome is fashioned to keep a constant code length.

### `createCodeList`

Employs the `convertToBase62` function with Java Streams to craft a list of unique codes.

### `partitionList`

A function that divides a list into smaller segments, facilitating the segmented storage of generated codes for efficient database insertions.

## Design Considerations

- **Concurrency**: The service employs `ExecutorService` with a fixed thread pool for increased speed.
- **Stateless Sessions**: For database operations, stateless sessions are used. They are ideal for bulk transactions as they don’t carry persistence-related overhead.
- **Error Handling**: Provisions for transaction rollbacks are present in case of database operation errors. Additionally, mechanisms to shut down the executor service gracefully are in place.
- **Performance Monitoring**: Vital metrics like duration taken for code generation and database tasks are logged.

## Possible Enhancements and Future Work

1. **Exception Handling**: There's room for improved error-handling and recovery, making the service even more robust.
2. **Scalability**: Considerations for more scalable storage solutions or code production algorithms may be needed as the application scales.
3. **Customizability**: Options to modify parameters like the size of the thread pool can be added for enhanced flexibility.
---

By following the above methods sequentially, the service ensures efficient generation and storage of unique codes while capturing relevant metadata about each generation request.

## UI

![Sample UI](https://user-images.githubusercontent.com/34538577/269796762-8a9f5763-b646-4073-a1a8-57ec91791b45.png)

1. **Main Page Layout**: The application has a clean and straightforward design that's easy to understand.

2. **Input Field**: Users can specify how many unique codes they want to generate by entering a number.

3. **Generate Button**: Clicking this button starts the code generation process.

4. **Real-time Feedback**:

- Timer: Shows how long the code generation is taking.
- Spinner Icon: Indicates the process is still running.

5. **Performance Benchmark**: A table is displayed, giving users an idea of expected performance metrics for generating specific quantities of codes.

6. **Results Table**: After generating codes, a table appears at the bottom. It displays recent code generation requests, with colors indicating their performance against the benchmark.

## Weaknesses and Potential Improvements

### Weaknesses:  
1. **Transaction rollback**: Currently if an error occurs I did not implement a way to rollback the current changes.

2. **Unit Test Coverage**: The current coverage of unit tests is suboptimal, leaving certain parts of the codebase untested and potentially prone to undetected issues.

3. **Error Propagation in CodeService**
In the present architecture, if an exception occurs during task execution inside the `ExecutorService`, it is rethrown by `Future.get()`. This behavior might not be optimal for all scenarios, especially if we need more granular error information or handling.

4. **Stateless Session Overheads**
While stateless sessions are efficient for bulk operations, they might not be the best choice for more transactional tasks or when entity states are crucial. Over-relying on them can introduce challenges in more complex workflows.

5. **Potential Code Collisions**
Given a large enough number of requests, there's a possibility, albeit small, of generating colliding codes. This isn't currently checked or handled in the service.

6. **Scalability Concerns**
The service, as it stands, is designed for a single-node application. In a distributed setup or microservices architecture, additional concerns like distributed locking might arise to ensure unique code generation across nodes.

7. **Dependency on External Services**
With autowired components like the `SessionFactory` and `GenerationRequestRepository`, the service is reliant on these external components' availability and performance. Any bottleneck or failure in these dependencies can directly impact `CodeService`.

## Scope for Improvement

### 1. **Dynamic Thread Pool Configuration**
While the service currently uses a fixed-size thread pool, it could benefit from a dynamic or configurable thread pool size. This would allow the application to adapt to varying workloads and system capacities.

### 2. **Enhanced Logging**
Incorporating more detailed logging, especially around error cases, can aid in quicker issue resolution. Moreover, integrating with a centralized logging system can provide real-time insights.

### 3. **Database Retry Mechanisms**
To increase resilience, especially in distributed environments, implementing retry mechanisms for database operations can ensure data integrity in case of transient errors.

### 4. **Code Expiry and Cleanup**
Currently, the generated codes persist indefinitely. Introducing an expiry mechanism for codes, and periodically cleaning up expired codes, can improve database performance.

### 5. **Rate Limiting**
For systems that may be exposed to external requests, implementing rate limiting can protect the service from being overwhelmed by too many code generation requests in a short time.

