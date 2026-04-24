package com.example.Vault.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageService implements StorageService{

    private final String STORAGE_DIR = System.getProperty("user.dir").endsWith("backend") 
        ? System.getProperty("user.dir") + "/../storage/" 
        : System.getProperty("user.dir") + "/storage/";

    @Override
    public String upload(MultipartFile file) throws Exception
    {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new SecurityException("Cannot store file with relative path outside current directory " + fileName);
        }
        String uniqueName = UUID.randomUUID() + "_" + fileName;

        File dest = new File(STORAGE_DIR + uniqueName);

        file.transferTo(dest);
        return dest.getPath();
    }

    @Override
    public byte[] download(String path) throws Exception
    {
        return Files.readAllBytes(new File(path).toPath());
    }

    @Override
    public void delete(String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public boolean exists(String path) throws Exception {
        return new File(path).exists();
    }
}
