package com.royal_medical.prescription_service.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;
    private String fileName;
    private String fileType;
    private String fileUrl;

    private LocalDateTime uploadedAt;

    @Lob
    private String extractedText;
}
