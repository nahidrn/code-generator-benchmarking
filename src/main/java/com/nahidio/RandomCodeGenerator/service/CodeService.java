package com.nahidio.RandomCodeGenerator.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;
import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.repository.GeneratedCodeRepository;
import com.nahidio.RandomCodeGenerator.repository.GenerationRequestRepository;

import jakarta.transaction.Transactional;

@Service
public class CodeService {

    @Autowired
    private GeneratedCodeRepository codeRepository;

    @Autowired
    private GenerationRequestRepository requestRepository;

    @Transactional
    public void generateCodes(BigInteger number) {
        // Step 1: Create and save the GenerationRequest entity
        GenerationRequest request = new GenerationRequest();
        request.setStartedAt(LocalDateTime.now());
        request.setNumberOfCodes(number);
        requestRepository.save(request);

        List<GeneratedCode> codes = new ArrayList<>();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(number) < 0; i = i.add(BigInteger.ONE)) {
            // Generating a 7 character alphanumeric code
            String codeValue = RandomStringUtils.randomAlphanumeric(7);
            
            GeneratedCode code = new GeneratedCode();
            code.setCode(codeValue);
            code.setGenerationRequest(request);
            codes.add(code);
            
            // This is a naive way of generating unique codes.
            // You might want to implement further checks for uniqueness
        }

        // Step 2: Save generated codes
        codeRepository.saveAll(codes);

        // Step 3: Update the GenerationRequest with endedAt
        request.setEndedAt(LocalDateTime.now());
        requestRepository.save(request);
    }
}




