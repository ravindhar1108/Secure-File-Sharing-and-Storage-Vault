package com.example.Vault.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file) throws Exception;

    byte[] download(String path) throws Exception;
}
