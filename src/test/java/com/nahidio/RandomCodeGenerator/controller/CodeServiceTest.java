package com.nahidio.RandomCodeGenerator.controller;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;
import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.repository.GeneratedCodeRepository;
import com.nahidio.RandomCodeGenerator.repository.GenerationRequestRepository;
import com.nahidio.RandomCodeGenerator.service.CodeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CodeServiceTest {

    @Mock
    private GeneratedCodeRepository generatedCodeRepository;

    @Mock
    private GenerationRequestRepository generationRequestRepository;

    @InjectMocks
    private CodeService codeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    //@Test
    public void generateCodesTest() {
        // Mocking the behavior of repositories
        when(generationRequestRepository.save(any(GenerationRequest.class)))
                .thenReturn(new GenerationRequest());
        when(generatedCodeRepository.saveAll(any(List.class)))
                .thenReturn(null);

        BigInteger numberOfCodes = BigInteger.valueOf(5);
        //List<GeneratedCode> codes = codeService.generateCodes(numberOfCodes);

        assertEquals(numberOfCodes.intValue(), 15);//codes.size());
    }
}