package com.nahidio.RandomCodeGenerator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.error.CodeGenerationErrorException;
import com.nahidio.RandomCodeGenerator.error.InvalidNumberOfCodeRequestedException;
import com.nahidio.RandomCodeGenerator.service.CodeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

public class CodeGeneratorControllerTest {

    @InjectMocks
    CodeGeneratorController codeGeneratorController;

    @Mock
    CodeService codeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testNegativeNumberOfCodes() {
        assertThrows(InvalidNumberOfCodeRequestedException.class, () -> codeGeneratorController.generateCodes(-5L));
    }

    @Test
    public void testZeroNumberOfCodes() {
        assertThrows(InvalidNumberOfCodeRequestedException.class, () -> codeGeneratorController.generateCodes(0L));
    }

    @Test
    public void testExceedMaxLimitOfCodes() {
        assertThrows(InvalidNumberOfCodeRequestedException.class, () -> codeGeneratorController.generateCodes(CodeGeneratorController.MAX_CODES + 10));
    }

    @Test
    public void testNumberNotDivisibleByTen() {
        assertThrows(InvalidNumberOfCodeRequestedException.class, () -> codeGeneratorController.generateCodes(13L));
    }

    @Test
    public void testSuccessfulCodeGeneration() throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();
        GenerationRequest sample = new GenerationRequest();
        sample.setId(1L);
        sample.setStartedAt(startTime);
        sample.setEndedAt(endTime);
        sample.setNumberOfCodes(2000);
        when(codeService.generateCodes(100L)).thenReturn(sample);
        ResponseEntity<GenerationRequest> response = codeGeneratorController.generateCodes(100L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(sample, response.getBody());
    }

    @Test
    public void testFailedCodeGeneration() throws Exception {
        when(codeService.generateCodes(100L)).thenThrow(new RuntimeException("Error generating codes"));
        assertThrows(CodeGenerationErrorException.class, () -> codeGeneratorController.generateCodes(100L));
    }

    // ... add more tests as per requirements
}