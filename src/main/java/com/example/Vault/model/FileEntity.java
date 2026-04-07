package com.example.Vault.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String storagePath;
    private Long size;
    private Integer version;
    private Long userId;
    private LocalDateTime uploadedAt;

}
