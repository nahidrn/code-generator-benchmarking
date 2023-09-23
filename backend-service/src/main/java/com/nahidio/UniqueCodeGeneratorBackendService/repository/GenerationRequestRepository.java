package com.nahidio.UniqueCodeGeneratorBackendService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.GenerationRequest;

public interface GenerationRequestRepository extends JpaRepository<GenerationRequest, Long> {
}
