package com.example.Vault.service;

import com.example.Vault.model.FileEntity;
import com.example.Vault.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;

    public FileService(FileRepository fileRepository, StorageService storageService)
    {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
    }

    public FileEntity upload(MultipartFile file, Long userId) throws Exception {

        // 1️⃣ Upload to storage (local now, cloud later)
        String path = storageService.upload(file);

        // 2️⃣ Determine version
        int version = 1; // later we improve this

        // 3️⃣ Save metadata
        FileEntity entity = new FileEntity();
        entity.setOriginalName(file.getOriginalFilename());
        entity.setStoragePath(path);
        entity.setSize(file.getSize());
        entity.setUserId(userId);
        entity.setVersion(version);
        entity.setUploadedAt(LocalDateTime.now());

        return fileRepository.save(entity);

    }

    public byte[] download(Long fileId) throws Exception
    {
        FileEntity file = fileRepository.findById(fileId).orElseThrow();

        return storageService.download(file.getStoragePath());
    }

    public void delete(Long fileId, Long userId)
    {
        FileEntity file = fileRepository.findById(fileId).orElseThrow( () -> new RuntimeException("File not Found"));

        if(!file.getUserId().equals(userId))
        {
            throw new RuntimeException("Unauthorized");
        }

        File diskFile = new File(file.getStoragePath());
        if(diskFile.exists())
        {
            diskFile.delete();
        }

        fileRepository.delete(file);
    }



    public List<FileEntity> getUserFiles(Long userId) {
        return fileRepository.findByUserId(userId);
    }

    public FileEntity getFile(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not Found"));
    }
}
