package com.example.Vault.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
@Data
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Long fileId;
    private LocalDateTime expiryTime;
    private Integer maxDownloads;
    private Integer downloadCount;
    private String password;
    private boolean viewOnce;
    private boolean active;
    private LocalDateTime createdAt;

}
