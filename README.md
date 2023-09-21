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

4. It takes around a minute to build and run the first time. Your application should be alive and kicking at: `http://localhost:8032`

5. **Shutting It Down**:
    ```bash
    docker-compose down
    ```


## CodeService Logic Explanation

### Overview
`CodeService` is a core component of the application responsible for generating unique codes. This service contains methods for handling the code generation logic, saving these codes, and handling associated metadata for code generation requests.

### Key Methods

#### 1. `generateCodes(long numberOfCodes)`

- **Purpose**: Generates unique alphanumeric codes and saves them to the database.
  
- **Steps**:
    1. **Initialize Generation Request**: Records the starting time and the number of codes to be generated.
    2. **Generate Codes**: Uses a helper method `createCodeList` to generate a list of unique codes.
    3. **Bulk Save**: Saves the generated codes to the database using a stateless session for optimized bulk insertion.
    4. **Finalize Generation Request**: Records the ending time for the generation request.

#### 2. `convertToBase62(long value, GenerationRequest request)`

- **Purpose**: Converts a given long value to a base 62 string representation.
  
- **Steps**:
    1. Uses a loop to convert the long value to base 62 using the predefined alphanumeric characters.
    2. Pads the result to ensure a consistent length for all generated codes.
    3. Associates the generated code with the provided `GenerationRequest`.
    4. Returns the constructed `GeneratedCode` object.

#### 3. `createCodeList(long numberOfCodes, GenerationRequest request)`

- **Purpose**: Produces a list of unique alphanumeric codes.
  
- **Steps**:
    1. Utilizes Java's `LongStream` to generate a sequential stream of numbers.
    2. Converts each number to its base 62 representation using the `convertToBase62` method.
    3. Returns the list of generated codes.

---

By following the above methods sequentially, the service ensures efficient generation and storage of unique codes while capturing relevant metadata about each generation request.

## UI

![Sample UI](https://user-images.githubusercontent.com/34538577/269719286-488c2a58-5731-446f-ab05-4654e9a73f96.png)

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
1. **Performance Shortfall**: The current code generation and insertion process may not always meet the required performance benchmarks, especially for very large batches of codes.
  
2. **Database Insertion Time**: A significant portion of the total processing time is consumed by the database insertion process, especially when dealing with large volumes of codes.

3. **Unit Test Coverage**: The current coverage of unit tests is suboptimal, leaving certain parts of the codebase untested and potentially prone to undetected issues.

### Potential Improvements:

1. **Optimize Database Writes**:
    - **Batching**: Group codes into larger batches before writing to the database to minimize the overhead of individual transactions.
    - **Database Tuning**: Optimize the database schema, indexing, and configuration settings to better handle bulk insert operations.
    - **Distributed Databases**: Consider using distributed databases like Cassandra or Amazon DynamoDB that are optimized for high-volume write operations.

2. **Parallel Processing**:
    - Introduce parallel processing or multi-threading at various stages, such as during code generation or database insertion, to expedite the overall process.
    - Consider employing distributed compute resources, like Apache Spark, for massive-scale code generation tasks.

3. **Code Generation Algorithm Optimization**:
    - Investigate alternative algorithms or libraries that might provide faster code generation without compromising uniqueness.

4. **Increase Unit Test Coverage**:
    - Dedicate time to write tests, especially for the core functionalities to ensure that the codebase remains robust against regressions.
    - Use mocking libraries, like Mockito, to simulate database operations or other external dependencies during testing.
    - Set up Continuous Integration (CI) pipelines to run tests automatically on every code push, providing instant feedback on the health of the codebase.

5. **Monitoring & Logging**:
    - Introduce comprehensive logging and monitoring tools like Grafana, Prometheus, or ELK Stack (Elasticsearch, Logstash, Kibana) to gain insights into system bottlenecks and performance metrics.
    - Monitoring can help identify specific operations or queries that might be causing slowdowns.

---

It's crucial to continually assess and re-evaluate the system's performance, especially as the demands and scale of the operation grow. The above points are starting places for potential enhancements, and further profiling can reveal more targeted optimizations.