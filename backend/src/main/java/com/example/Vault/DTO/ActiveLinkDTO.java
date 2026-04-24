package com.example.Vault.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActiveLinkDTO {
    private String token;
    private Long fileId;
    private String fileName;
    private LocalDateTime expiryTime;
    private Integer maxDownloads;
    private Integer downloadCount;
    private Integer viewCount;
    private boolean viewOnce;
    private boolean active;
    private String shareType;
    private LocalDateTime createdAt;
}
