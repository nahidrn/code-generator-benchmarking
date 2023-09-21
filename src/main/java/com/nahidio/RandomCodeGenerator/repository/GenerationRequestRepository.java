package com.nahidio.RandomCodeGenerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nahidio.RandomCodeGenerator.entity.GenerationRequest;

public interface GenerationRequestRepository extends JpaRepository<GenerationRequest, Long> {
}
