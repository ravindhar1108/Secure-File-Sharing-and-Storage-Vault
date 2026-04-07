package com.example.Vault.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService{

    private final String STORAGE_DIR = System.getProperty("user.dir") + "/storage/";

    @Override
    public String upload(MultipartFile file) throws Exception
    {
//        File dir = new File(STORAGE_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }

        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        File dest = new File(STORAGE_DIR + uniqueName);

        file.transferTo(dest);
        return dest.getPath();
    }

    @Override
    public byte[] download(String path) throws Exception
    {
        return Files.readAllBytes(new File(path).toPath());
    }


}
