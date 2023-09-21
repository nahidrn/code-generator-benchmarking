package com.nahidio.RandomCodeGenerator.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;
import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.repository.GenerationRequestRepository;

@Service
public class CodeService {
    private static final Logger logger = LoggerFactory.getLogger(CodeService.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private GenerationRequestRepository requestRepository;

    // A string representation of alphanumeric characters (base 62: 0-9, A-Z, a-z).
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // The maximum length of the code string.
    private static final int MAX_LENGTH = 7;
    
    /**
     * Generate and store unique codes.
     *
     * @param numberOfCodes Number of unique codes to generate.
     * @throws Exception If there is an issue during code generation or database operations.
     */
    public GenerationRequest generateCodes(long numberOfCodes) throws Exception {
        // Step 1: Create a new GenerationRequest entity and persist it to the database.
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(numberOfCodes);
        requestRepository.save(request);

        // Generate the list of unique codes.
        long startGenerateTime = System.nanoTime();
        List<GeneratedCode> codes = this.createCodeList(numberOfCodes, request);
        long endGenerateTime = System.nanoTime();
        double elapsedGenerateTime = (double) (endGenerateTime - startGenerateTime) / 1_000_000_000; // Convert nanoseconds to seconds
        logger.info("Time taken to generate codes: {} seconds", elapsedGenerateTime);
        // Step 2: Use a stateless session for bulk insertion of generated codes.
        // A stateless session is a lightweight alternative to the standard session,
        // ideal for bulk database operations as it does not keep track of persistent objects.
        StatelessSession statelessSession = null;
        Transaction tx = null;
        long startDbTime = System.nanoTime();
        try {
            statelessSession = sessionFactory.openStatelessSession();
            tx = statelessSession.beginTransaction();

            for (GeneratedCode code : codes) {
                statelessSession.insert(code);
            }

            tx.commit();
        } catch (Exception ex) {
            // Rollback the transaction in case of any exception.
            if (tx != null) {
                tx.rollback();
            }
            // Optionally, further logging can be added here to trace the exception.
            throw new Exception("Error generating codes", ex);
        } finally {
            // Ensure the stateless session is closed after operations.
            if (statelessSession != null) {
                statelessSession.close();
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
      // Use parallel streams for efficient generation of codes.
      // Convert each number in the range to its base 62 representation.
      return LongStream.range(0, numberOfCodes)
              .parallel()
              .mapToObj(i -> convertToBase62(i, request))
              .collect(Collectors.toList());
    }
}