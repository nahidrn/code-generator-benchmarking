package com.nahidio.RandomCodeGenerator.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.service.CodeService;
import com.nahidio.RandomCodeGenerator.service.GenerationRequestService;

// Controller to handle endpoints related to code generation
@RestController
@RequestMapping("/api")
public class CodeGeneratorController {

    // Autowiring the service responsible for code generation logic
    @Autowired
    private CodeService codeService;

    @Autowired
    private GenerationRequestService requestService;

    // Constant representing the maximum possible unique codes for a 7 character base-62 code
    private static final long MAX_CODES = (long) Math.pow(62, 7);

    // Endpoint to trigger code generation
    @GetMapping("/generateCodes")
    public ResponseEntity<String> generateCodes(@RequestParam long number) {
        // Input validation: Ensure requested number of codes is greater than 0
        if (number <= 0) {
            throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Number should be greater than 0");
        }
        
        // Input validation: Ensure requested number of codes does not exceed the maximum possible unique codes
        if (number > MAX_CODES) {
            throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Number exceeds maximum allowed limit of unique codes.");
        }

        // Input validation: Ensure requested number of codes is a rounded number (e.g., divisible by 10).
        // This could be useful in certain use-cases, but might not be always necessary.
        if (number % 10 != 0) {
          throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Number should be a round number (e.g., divisible by 10).");
        }

        try {
            // Call the service method to generate the codes
            codeService.generateCodes(number);
            return ResponseEntity.ok("Codes generated successfully!");
        } catch (Exception e) {
            // Catching all exceptions for simplicity. In a real-world scenario, 
            // exceptions should be handled more granularly based on their type and expected behavior.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/generationRequests")
    public ResponseEntity<List<GenerationRequest>> getGenerationRequests() {
        List<GenerationRequest> requests = requestService.findAllRequests();
        return ResponseEntity.ok(requests);
    }
}