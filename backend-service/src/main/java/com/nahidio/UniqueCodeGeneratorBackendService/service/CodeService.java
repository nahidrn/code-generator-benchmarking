package com.nahidio.UniqueCodeGeneratorBackendService.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.GeneratedCode;
import com.nahidio.UniqueCodeGeneratorBackendService.entity.GenerationRequest;
import com.nahidio.UniqueCodeGeneratorBackendService.repository.GeneratedCodeRepository;
import com.nahidio.UniqueCodeGeneratorBackendService.repository.GenerationRequestRepository;

@Service
public class CodeService {
    private static final Logger logger = LoggerFactory.getLogger(CodeService.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private GenerationRequestRepository requestRepository;

    @Autowired
    private GeneratedCodeRepository generatedCodeRepository;

    // A string representation of alphanumeric characters (base 62: 0-9, A-Z, a-z).
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // The maximum length of the code string.
    private static final int MAX_LENGTH = 7;
    private static AtomicLong lastCodeId = new AtomicLong();
    private final int DB_INSERTION_CHUNK_SIZE = 10000;
    private final int MAX_NUMBER_OF_THREADS = 40;
    private final long CODE_GENERATION_CHUNK_SIZE = 1_000_000L;

    public CodeService() {
        initializeLastCodeValue();
    }
    /**
     * Generate and store unique codes.
     *
     * @param numberOfCodes Number of unique codes to generate.
     * @throws Exception If there is an issue during code generation or database operations.
     */
    public GenerationRequest generateCodes(long numberOfCodes) throws Exception {

        // Step 1: Create a new GenerationRequest entity and persist it to the database.
        // Calculate how many full chunks we'll have, and the size of the final chunk
        long fullChunks = numberOfCodes / CODE_GENERATION_CHUNK_SIZE;
        long lastChunkSize = numberOfCodes % CODE_GENERATION_CHUNK_SIZE;
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(numberOfCodes);
        requestRepository.save(request);


        for (long i = 0; i < fullChunks; i++) {
            processCodeGenerationChunk(CODE_GENERATION_CHUNK_SIZE, request);
        }
        
        if (lastChunkSize > 0) {
            processCodeGenerationChunk(lastChunkSize, request);
        }
        
        // Step 4: Update the GenerationRequest record with the end time.
        LocalDateTime endTime = LocalDateTime.now();
        request.setEndedAt(endTime);
        requestRepository.save(request);
        return request;
    }

    private void processCodeGenerationChunk(long chunkSize, GenerationRequest request) throws Exception {
       
        // Step 2: Generate the list of unique codes.
        long startGenerateTime = System.nanoTime();
        List<GeneratedCode> codes = this.createCodeList(chunkSize, request);

        long endGenerateTime = System.nanoTime();
        double elapsedGenerateTime = (double) (endGenerateTime - startGenerateTime) / 1_000_000_000; // Convert nanoseconds to seconds
        logger.info("Time taken to generate codes: {} seconds", elapsedGenerateTime);

        // Step 3: Code Insertion Section
        long startDbTime = System.nanoTime();
        int numberOfThreads = (int) Math.ceil((double) chunkSize / DB_INSERTION_CHUNK_SIZE);
        numberOfThreads = numberOfThreads <= MAX_NUMBER_OF_THREADS ? numberOfThreads : MAX_NUMBER_OF_THREADS;
        logger.info("NR:: Number of threads allocated" + numberOfThreads);
        // An atomic boolean flag to detect if any chunk operation fails
        // AtomicBoolean failureFlag = new AtomicBoolean(false); // TODO: Add it back when the problem handling with connection pool get fixed

        // Create a thread pool with a fixed number of threads to limit concurrency
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads); 

        // Partition the list of codes into chunks for processing in parallel
        List<List<GeneratedCode>> chunks = this.partitionList(codes, DB_INSERTION_CHUNK_SIZE);

        // A list to store the transactions of each thread for later commit or rollback
        List<Future<Transaction>> futureTransactions = new ArrayList<>();
        
        // Using a stateless session for bulk insertion of generated codes.
        // A stateless session is a lightweight alternative to the standard session,
        // ideal for bulk database operations as it does not keep track of persistent objects.
        for (List<GeneratedCode> chunk : chunks) {
            // For each chunk, process it in a separate thread
            Future<Transaction> future = executorService.submit(() -> {
                StatelessSession session = sessionFactory.openStatelessSession();
                Transaction tx = session.beginTransaction();
                try {
                    for (GeneratedCode code : chunk) {
                        session.insert(code);
                    }
                    tx.commit(); // TODO: Remove it when the problem handling with connection pool get fixed
                    // Return the transaction without committing it, 
                    // to allow centralized commit/rollback later
                    return tx; 
                } catch (Exception e) {
                    // If there's an exception, mark the failureFlag as true
                    // failureFlag.set(true); // TODO: Add it back when the problem handling with connection pool get fixed
                    // Rollback the current transaction due to the exception
                    tx.rollback();
                    // Close the session after rolling back
                    // session.close();
                    // Rethrow the exception so the outer code can detect the error
                    throw e; 
                } finally {
                    session.close(); // TODO: Remove it when the problem handling with connection pool get fixed
                }
            });
            futureTransactions.add(future);
        }

        // Process the results of each thread, checking for any exceptions
        for (Future<Transaction> future : futureTransactions) {
            try {
                // This will throw an exception if the thread faced any errors
                future.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                // Handle or log the exception as needed
            }
        }
        /* TODO: Add it back when the problem handling with connection pool get fixed
        // After all threads have processed, decide to commit or rollback
        // Check if there was a failure in any thread
        if (!failureFlag.get()) {
            // If all operations were successful, commit all transactions
            for (Future<Transaction> future : futureTransactions) {
                Transaction tx = future.get();
                tx.commit();
            }
        } else {
            // If any operation failed, roll back all transactions
            for (Future<Transaction> future : futureTransactions) {
                Transaction tx = future.get();
                tx.rollback();
            }
        }
        */

        // Gracefully shut down the executor service
        executorService.shutdown();
        try {
            // Wait for all tasks to finish or for a timeout to occur
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                // Force shutdown if tasks don't finish in the given timeout
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Force shutdown if the await gets interrupted
            executorService.shutdownNow();
        }

        long endDbTime = System.nanoTime();
        double elapsedDbTime = (double) (endDbTime - startDbTime) / 1_000_000_000; // Convert nanoseconds to seconds
        logger.info("Time taken for DB operations: {} seconds", elapsedDbTime);

    }

    /**
     * Convert a given number into an encrypted base 62 alphanumeric representation.
     *
     * @param value The number to be converted.
     * @param request The associated GenerationRequest.
     * @return A GeneratedCode object with the encrypted code string.
     */
    private GeneratedCode convertToBase62(long value, GenerationRequest request) {
        // Mix the counter with the generationRequest ID
        //long mixedValue = value ^ SECRET; // Simple bitwise XOR
        UUID uuid = UUID.randomUUID();
        long uuidMostSigBits = uuid.getMostSignificantBits();
        long mixedValue = value ^ uuidMostSigBits; // Simple bitwise XOR with the most significant bits of UUID
        StringBuilder codeBuilder = new StringBuilder();
        try {
          for (int i = 0; i < MAX_LENGTH; i++) {
              int index = (int) (Math.abs(mixedValue) % ALPHANUMERIC.length());
              codeBuilder.insert(0, ALPHANUMERIC.charAt(index));
              mixedValue /= ALPHANUMERIC.length();
          }

          String code = codeBuilder.toString();

          GeneratedCode generatedCode = new GeneratedCode();
          generatedCode.setCode(code);
          generatedCode.setGenerationRequest(request);

          return generatedCode;
    
        } catch (Exception e) {
            logger.error("Error during code generation", e);
            throw new RuntimeException("Error during code generation", e);
        }
    }

    /**
     * Create a list of unique GeneratedCode objects.
     *
     * @param numberOfCodes Number of unique codes to generate.
     * @param request The associated GenerationRequest.
     * @return A list of GeneratedCode objects.
     */
    private List<GeneratedCode> createCodeList(long numberOfCodes, GenerationRequest request) {
        if (lastCodeId.get() == 0L)
          lastCodeId.set(1L);
        long startValue = lastCodeId.getAndAdd(numberOfCodes);  // Atomically increments by numberOfCodes and returns the previous value.
        
        return LongStream.range(startValue, startValue + numberOfCodes)
                .parallel()
             //   .peek(i -> System.out.println("Mapping value: " + i))
                .mapToObj(i -> convertToBase62(i, request))
                .collect(Collectors.toList());
    }

    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(
                list.subList(i, Math.min(i + size, list.size())))
            );
        }
        return partitions;
    }

    private void initializeLastCodeValue() {
        if (lastCodeId.get() != 0L) {
            Optional<Long> maxValueOpt = generatedCodeRepository.getMaxId();
            long maxValue = maxValueOpt.orElse(0L); // Default to 0 if no codes exist
            lastCodeId.set(maxValue);
        }
    }
}