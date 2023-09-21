package com.nahidio.RandomCodeGenerator.controller;

import java.math.BigInteger;

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

    @GetMapping("/generateCodes")
    public ResponseEntity<String> generateCodes(@RequestParam BigInteger number) {
        // Input validation
        if (number.compareTo(BigInteger.ZERO) <= 0) {
            throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Number should be greater than 0");
        }

        // Check if the number exceeds BigInteger's range.
        // But this is highly unlikely since BigInteger has a vast range. 
        // You can set a custom limit if needed.

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
