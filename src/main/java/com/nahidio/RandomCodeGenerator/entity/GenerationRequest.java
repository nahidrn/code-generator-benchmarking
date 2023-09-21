package com.nahidio.RandomCodeGenerator.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "generationRequest")
public class GenerationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private BigInteger numberOfCodes;

    @OneToMany(mappedBy = "generationRequest", fetch = FetchType.LAZY)
    @JsonIgnore  // To prevent serialization of this side of the relationship
    private List<GeneratedCode> generatedCodes;

}