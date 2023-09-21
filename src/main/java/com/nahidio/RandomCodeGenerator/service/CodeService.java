package com.nahidio.RandomCodeGenerator.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void generateCodes(BigInteger number) throws Exception {
        int totalLength = 7;
        int numberOfIds = number.intValue();
        String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        // Step 1: Create and save the GenerationRequest entity
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(number);
        requestRepository.save(request);

        List<GeneratedCode> codes = new ArrayList<>();

        for (int i = 0; i < numberOfIds; i++) {

            StringBuilder end = new StringBuilder();
            int current = i;//depending on exact case, you would need to keep track of current
            int remainder = current % ALPHANUMERIC.length();//the index of next character
            
            do {
                end.append(ALPHANUMERIC.charAt(remainder));
                current /= ALPHANUMERIC.length();//update to check if we need to add more characters
                remainder = current % ALPHANUMERIC.length();//update index, only used if more chars are needed
            } while (current > 0);
            
            int padCount = totalLength - end.length();
            
            StringBuilder result = new StringBuilder();
            
            for (int j = 0; j < padCount; j++) {
                result.append("0");
            }
            result.append(end);
            
            if(result.toString().equalsIgnoreCase("000000a"))
              System.out.println("NR::"+result);

            GeneratedCode code = new GeneratedCode();
            code.setCode(result.toString());
            code.setGenerationRequest(request);
            codes.add(code);
        }

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
}