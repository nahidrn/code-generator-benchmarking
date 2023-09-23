package com.nahidio.UniqueCodeGeneratorBackendService.service;

import java.util.Collections;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.GenerationRequest;
import com.nahidio.UniqueCodeGeneratorBackendService.repository.GenerationRequestRepository;

@Service
public class GenerationRequestService {

    @Autowired
    private GenerationRequestRepository requestRepository;

    public List<GenerationRequest> findAllRequests() {
        try {
            List<GenerationRequest> requests = requestRepository.findAll();
            
            // // Initializing for each request to prevent lazy initialization issues
            // for(GenerationRequest request : requests) {
            //     Hibernate.initialize(request.getGeneratedCodes());
            // }
            
            return requests;
        } catch(Exception e) {
            e.printStackTrace(); // Log the full stack trace
        }
        return Collections.emptyList();  // Return an empty list instead of null
    }
}