package com.nahidio.UniqueCodeGeneratorBackendService.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.GenerationRequest;
import com.nahidio.UniqueCodeGeneratorBackendService.error.CodeGenerationErrorException;
import com.nahidio.UniqueCodeGeneratorBackendService.error.InvalidNumberOfCodeRequestedException;
import com.nahidio.UniqueCodeGeneratorBackendService.service.CodeService;
import com.nahidio.UniqueCodeGeneratorBackendService.service.GenerationRequestService;

// Controller to handle endpoints related to code generation
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8033")
public class CodeGeneratorController {

    // Autowiring the service responsible for code generation logic
    @Autowired
    private CodeService codeService;

    @Autowired
    private GenerationRequestService requestService;

    // Constant representing the maximum possible unique codes for a 7 character base-62 code
    static final long MAX_CODES = (long) Math.pow(62, 7);

    // Endpoint to trigger code generation
    @GetMapping("/generateCodes")
    public ResponseEntity<GenerationRequest> generateCodes(@RequestParam long number)
              throws CodeGenerationErrorException, InvalidNumberOfCodeRequestedException  {
        // Input validation: Ensure requested number of codes is greater than 0
        if (number <= 0) {
            throw new InvalidNumberOfCodeRequestedException( "Number should be greater than 0");
        }
        
        // Input validation: Ensure requested number of codes does not exceed the maximum possible unique codes
        if (number > MAX_CODES) {
            throw new InvalidNumberOfCodeRequestedException( "Number exceeds maximum allowed limit of unique codes.");
        }

        // Input validation: Ensure requested number of codes is a whole number.
        if (number != Math.floor(number)) {
            throw new InvalidNumberOfCodeRequestedException("Number should be a whole number.");
        }

        try {
            // Call the service method to generate the codes
            GenerationRequest request = codeService.generateCodes(number);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            // Catching all exceptions for simplicity. In a real-world scenario, 
            // exceptions should be handled more granularly based on their type and expected behavior.
            throw new CodeGenerationErrorException(e.getMessage());
        }
    }

    @GetMapping("/generationRequests")
    public ResponseEntity<List<GenerationRequest>> getGenerationRequests() {
        List<GenerationRequest> requests = requestService.findAllRequests();
        return ResponseEntity.ok(requests);
    }
}