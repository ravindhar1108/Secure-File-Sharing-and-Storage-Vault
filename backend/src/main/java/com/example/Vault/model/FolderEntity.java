package com.example.Vault.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "folders")
@Data
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private Long userId;
    
    private Long parentId;
    
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted = false;
    
    private LocalDateTime deletedAt;
    
    @PrePersist
    public void prePersist() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
