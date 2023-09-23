package com.nahidio.UniqueCodeGeneratorBackendService.entity;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "generatedCodes",
       uniqueConstraints=@UniqueConstraint(columnNames={"code"}))
public class GeneratedCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 7, max = 7, message = "Code must be exactly 7 characters")
    @Column(nullable = false, name = "code", columnDefinition = "CHAR(7) COLLATE utf8mb4_bin")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generationRequestId", nullable = false)
    private GenerationRequest generationRequest;

}
