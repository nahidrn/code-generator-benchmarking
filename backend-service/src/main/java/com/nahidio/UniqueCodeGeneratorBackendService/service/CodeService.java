package com.nahidio.UniqueCodeGeneratorBackendService.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final int CHUNK_SIZE = 10000;
    private final int MAX_NUMBER_OF_THREADS = 20;


    /**
     * Generate and store unique codes.
     *
     * @param numberOfCodes Number of unique codes to generate.
     * @throws Exception If there is an issue during code generation or database operations.
     */
    public GenerationRequest generateCodes(long numberOfCodes) throws Exception {

        // Step 1: Create a new GenerationRequest entity and persist it to the database.
        int numberOfThreads = (int) Math.ceil((double) numberOfCodes / CHUNK_SIZE);
        numberOfThreads = numberOfThreads <= MAX_NUMBER_OF_THREADS ? numberOfThreads : MAX_NUMBER_OF_THREADS;
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(numberOfCodes);
        requestRepository.save(request);

        initializeLastCodeValue();

        // Generate the list of unique codes.
        long startGenerateTime = System.nanoTime();
        List<GeneratedCode> codes = this.createCodeList(numberOfCodes, request);
        long endGenerateTime = System.nanoTime();
        double elapsedGenerateTime = (double) (endGenerateTime - startGenerateTime) / 1_000_000_000; // Convert nanoseconds to seconds
        logger.info("Time taken to generate codes: {} seconds", elapsedGenerateTime);
        // Step 2: Use a stateless session for bulk insertion of generated codes.
        // A stateless session is a lightweight alternative to the standard session,
        // ideal for bulk database operations as it does not keep track of persistent objects.

        long startDbTime = System.nanoTime();
        if(numberOfThreads > 1) {
          ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads); // Limit concurrency
          List<List<GeneratedCode>> chunks = this.partitionList(codes, CHUNK_SIZE);

          List<Future<?>> futures = new ArrayList<>();
          for (List<GeneratedCode> chunk : chunks) {
              Future<?> future = executorService.submit(() -> {
                  StatelessSession session = sessionFactory.openStatelessSession();
                  Transaction tx = session.beginTransaction();
                  try {
                      for (GeneratedCode code : chunk) {
                          session.insert(code);
                      }
                      tx.commit();
                  } catch (Exception e) {
                      tx.rollback(); // Important to rollback on exception
                      // Log the exception or notify the relevant system
                  } finally {
                      session.close();
                  }
              });
              futures.add(future);
          }

          for (Future<?> future : futures) {
              try {
                  //Using Future.get() to wait for each task to complete. 
                  //If an exception occurred during task execution, it will be rethrown by get()
                  future.get();
              } catch (ExecutionException e) {
                  Throwable cause = e.getCause();
                  // TODO: rollback the transaction
              }
          }

          executorService.shutdown();
          try {
              // Allow tasks to complete or timeout after a certain period
              if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                  executorService.shutdownNow();
              }
          } catch (InterruptedException e) {
              executorService.shutdownNow();
          }
        } else {
          // No need to create threads
          StatelessSession session = sessionFactory.openStatelessSession();
          Transaction tx = session.beginTransaction();
          try {
              for (GeneratedCode code : codes) {
                  session.insert(code);
              }
              tx.commit();
          } catch (Exception e) {
              tx.rollback();
              throw e;  // Re-throwing the exception to be handled outside or notify the user.
          } finally {
              session.close();
          }
        }



        long endDbTime = System.nanoTime();
        double elapsedDbTime = (double) (endDbTime - startDbTime) / 1_000_000_000; // Convert nanoseconds to seconds
        logger.info("Time taken for DB operations: {} seconds", elapsedDbTime);
        // Step 3: Update the GenerationRequest record with the end time.
        LocalDateTime endTime = LocalDateTime.now();
        request.setEndedAt(endTime);
        requestRepository.save(request);
        return request;
    }

    /**
     * Convert a given number into a base 62 alphanumeric representation.
     *
     * @param value The number to be converted.
     * @param request The associated GenerationRequest.
     * @return A GeneratedCode object with the computed code string.
     */
    private GeneratedCode convertToBase62(long value, GenerationRequest request) {
        StringBuilder codeBuilder = new StringBuilder();

        // Convert the number into base 62 representation using the ALPHANUMERIC characters.
        do {
            int index = (int) (value % ALPHANUMERIC.length());
            codeBuilder.insert(0, ALPHANUMERIC.charAt(index)); // Prepend the character.
            value /= ALPHANUMERIC.length();
        } while (value > 0);
    
        // Format the code to the required length, padding with zeros if needed.
        String code = String.format("%" + MAX_LENGTH + "s", codeBuilder.toString()).replace(' ', '0');
    
        GeneratedCode generatedCode = new GeneratedCode();
        generatedCode.setCode(code);
        generatedCode.setGenerationRequest(request);
    
        return generatedCode;
    }
    
    /**
     * Create a list of unique GeneratedCode objects.
     *
     * @param numberOfCodes Number of unique codes to generate.
     * @param request The associated GenerationRequest.
     * @return A list of GeneratedCode objects.
     */
    private List<GeneratedCode> createCodeList(long numberOfCodes, GenerationRequest request) {
        long startValue = lastCodeId.getAndAdd(numberOfCodes);  // Atomically increments by numberOfCodes and returns the previous value.
        return LongStream.range(startValue, startValue + numberOfCodes)
                .parallel()
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