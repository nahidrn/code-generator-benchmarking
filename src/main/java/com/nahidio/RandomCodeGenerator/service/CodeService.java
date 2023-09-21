package com.nahidio.RandomCodeGenerator.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;
import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.repository.GenerationRequestRepository;

@Service
public class CodeService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private GenerationRequestRepository requestRepository;

    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int MAX_LENGTH = 7;
    
    public void generateCodes(long numberOfCodes) throws Exception {
        // Step 1: Create and save the GenerationRequest entity
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(BigInteger.valueOf(numberOfCodes));
        requestRepository.save(request);

        List<GeneratedCode> codes = this.createCodeList(numberOfCodes, request);
        // Step 2: Bulk Save generated codes using StatelessSession
        StatelessSession statelessSession = null;
        Transaction tx = null;
        
        try {
            statelessSession = sessionFactory.openStatelessSession();
            tx = statelessSession.beginTransaction();

            for (GeneratedCode code : codes) {
                statelessSession.insert(code);
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
            // Optionally, log the exception here
            throw new Exception("Error generating codes", ex);
        } finally {
            if (statelessSession != null) {
                statelessSession.close();
            }
        }

        // Step 3: Update the GenerationRequest with endedAt
        request.setEndedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    private GeneratedCode convertToBase62(long value, GenerationRequest request) {
        StringBuilder codeBuilder = new StringBuilder();
        do {
            int index = (int) (value % ALPHANUMERIC.length());
            codeBuilder.insert(0, ALPHANUMERIC.charAt(index)); // Prepend the character.
            value /= ALPHANUMERIC.length();
        } while (value > 0);
    
        String code = String.format("%" + MAX_LENGTH + "s", codeBuilder.toString()).replace(' ', '0');
    
        GeneratedCode generatedCode = new GeneratedCode();
        generatedCode.setCode(code);
        generatedCode.setGenerationRequest(request);
    
        return generatedCode;
    }
    
    private List<GeneratedCode> createCodeList(long numberOfCodes, GenerationRequest request) {
      return LongStream.range(0, numberOfCodes)
              .parallel()
              .mapToObj(i -> convertToBase62(i, request))
              .collect(Collectors.toList());
    }
}