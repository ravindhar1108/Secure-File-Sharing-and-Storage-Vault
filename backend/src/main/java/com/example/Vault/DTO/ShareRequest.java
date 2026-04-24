package com.example.Vault.DTO;

import lombok.Data;

@Data
public class ShareRequest {

    private Integer maxDownloads;
    private Integer expiryHours;
    private boolean viewOnce;
    private String password;
    private String shareType;

}
