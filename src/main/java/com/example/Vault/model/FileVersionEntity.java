package com.example.Vault.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_versions")
@Data
public class FileVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    
    private String storagePath;
    
    private String fileHash;
    
    private Long size;
    
    private Integer version;
    
    private LocalDateTime uploadedAt;

}
