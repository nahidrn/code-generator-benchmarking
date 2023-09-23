package com.nahidio.UniqueCodeGeneratorBackendService.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.GeneratedCode;

public interface GeneratedCodeRepository extends JpaRepository<GeneratedCode, Long> {

    // Using native query for performance to get the maximum code value
    @Query(value = "SELECT MAX(id) FROM generated_codes", nativeQuery = true)
    Optional<Long> getMaxId();
}
