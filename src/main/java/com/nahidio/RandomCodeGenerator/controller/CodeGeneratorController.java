package com.nahidio.RandomCodeGenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nahidio.RandomCodeGenerator.service.CodeService;

@RestController
@RequestMapping("/api")
public class CodeGeneratorController {

    @Autowired
    private CodeService codeService;

    private static final long MAX_CODES = (long) Math.pow(62, 7);

    @GetMapping("/generateCodes")
    public ResponseEntity<String> generateCodes(@RequestParam long number) {
        // Input validation
        if (number <= 0) {
            throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Number should be greater than 0");
        }
        
        // Check if number exceeds 62^7
        if (number > MAX_CODES) {
            throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Number exceeds maximum allowed limit of unique codes.");
        }

        // Ensure number is rounded (e.g., divisible by 10)
        if (number % 10 != 0) {
          throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Number should be a round number (e.g., divisible by 10).");
        }

        try {
            codeService.generateCodes(number);
            return ResponseEntity.ok("Codes generated successfully!");
        } catch (Exception e) {
            // For simplicity, capturing all exceptions. 
            // Refine this as per actual needs.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
