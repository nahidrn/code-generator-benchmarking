package com.nahidio.RandomCodeGenerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nahidio.RandomCodeGenerator.entity.GeneratedCode;

public interface GeneratedCodeRepository extends JpaRepository<GeneratedCode, Long> {
}
