package com.nahidio.RandomCodeGenerator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;
import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;
import com.nahidio.RandomCodeGenerator.repository.GenerationRequestRepository;

public class CodeServiceTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private GenerationRequestRepository requestRepository;

    @InjectMocks
    @Autowired
    private CodeService codeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateCodes() throws Exception {
        // Setting up the mocked interactions
        long numberOfCodes = 10L;
        GenerationRequest request = new GenerationRequest();
        
        when(requestRepository.save(any(GenerationRequest.class))).thenReturn(request);
        
        StatelessSession statelessSession = mock(StatelessSession.class);
        Transaction tx = mock(Transaction.class);
        
        when(sessionFactory.openStatelessSession()).thenReturn(statelessSession);
        when(statelessSession.beginTransaction()).thenReturn(tx);

        // Call the service method
        codeService.generateCodes(numberOfCodes);

        // Verify interactions and assertions
        verify(requestRepository, times(2)).save(any(GenerationRequest.class));
        verify(statelessSession, times((int) numberOfCodes)).insert(any(GeneratedCode.class));
        verify(tx).commit();
        verify(statelessSession).close();
    }
  
    // TODO: Add more tests on statelessSession error, rollback on unique query error etc
}