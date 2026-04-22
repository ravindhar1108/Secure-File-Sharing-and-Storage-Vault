package com.example.Vault.service;

import com.example.Vault.model.FileEntity;
import com.example.Vault.model.FileVersionEntity;
import com.example.Vault.repository.FileRepository;
import com.example.Vault.repository.FileVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final StorageService storageService;

    public FileService(FileRepository fileRepository, FileVersionRepository fileVersionRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.storageService = storageService;
    }

    private String calculateHash(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(file.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public FileEntity upload(MultipartFile file, Long userId, Long folderId) throws Exception {

        String fileHash = calculateHash(file);
        String storagePath;

        // Deduplication check
        FileEntity existingByHash = fileRepository.findFirstByFileHash(fileHash);
        if (existingByHash != null) {
            storagePath = existingByHash.getStoragePath();
        } else {
            FileVersionEntity existingVersionByHash = fileVersionRepository.findFirstByFileHash(fileHash);
            if (existingVersionByHash != null) {
                storagePath = existingVersionByHash.getStoragePath();
            } else {
                // If not found in DB, upload to disk
                storagePath = storageService.upload(file);
            }
        }

        // Versioning check
        FileEntity existingFile = fileRepository.findByUserIdAndFolderIdAndOriginalNameAndIsDeletedFalse(userId, folderId, file.getOriginalFilename());

        if (existingFile != null && existingFile.getFileHash() != null && !existingFile.getFileHash().equals(fileHash)) {
            // Backup existing to FileVersionEntity
            FileVersionEntity version = new FileVersionEntity();
            version.setFileId(existingFile.getId());
            version.setStoragePath(existingFile.getStoragePath());
            version.setFileHash(existingFile.getFileHash());
            version.setSize(existingFile.getSize());
            version.setVersion(existingFile.getVersion());
            version.setUploadedAt(existingFile.getUploadedAt());
            fileVersionRepository.save(version);

            // Update existing pointer
            existingFile.setStoragePath(storagePath);
            existingFile.setFileHash(fileHash);
            existingFile.setSize(file.getSize());
            existingFile.setVersion(existingFile.getVersion() + 1);
            existingFile.setUploadedAt(LocalDateTime.now());
            return fileRepository.save(existingFile);
        } else if (existingFile != null) {
             // File is identical, just return it
             return existingFile;
        } else {
            // Create brand new
            FileEntity entity = new FileEntity();
            entity.setOriginalName(file.getOriginalFilename());
            entity.setStoragePath(storagePath);
            entity.setFileHash(fileHash);
            entity.setSize(file.getSize());
            entity.setUserId(userId);
            entity.setFolderId(folderId);
            entity.setVersion(1);
            entity.setUploadedAt(LocalDateTime.now());
            entity.setDeleted(false);
            return fileRepository.save(entity);
        }
    }

    public byte[] download(Long fileId) throws Exception {
        FileEntity file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        if(file.isDeleted()) throw new RuntimeException("File is in trash");
        return storageService.download(file.getStoragePath());
    }

    public byte[] downloadVersion(Long versionId) throws Exception {
        FileVersionEntity version = fileVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("Version not found"));
        return storageService.download(version.getStoragePath());
    }

    public void softDelete(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not Found"));
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        file.setDeleted(true);
        file.setDeletedAt(LocalDateTime.now());
        fileRepository.save(file);
    }

    public void restore(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not Found"));
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        file.setDeleted(false);
        file.setDeletedAt(null);
        fileRepository.save(file);
    }

    public void permanentDelete(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not Found"));
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        String path = file.getStoragePath();
        String fileHash = file.getFileHash();
        fileRepository.delete(file);

        // Only delete from disk if no other file references it (Deduplication cleanup)
        if (fileHash != null && !fileRepository.existsByFileHash(fileHash) && !fileVersionRepository.existsByFileHash(fileHash)) {
            File diskFile = new File(path);
            if (diskFile.exists()) diskFile.delete();
        }
    }

    public List<FileEntity> getUserFiles(Long userId) {
        // Return files in root directory by default or all depending on usage. 
        // Let's modify getting root files vs all. Here return root.
        return fileRepository.findByUserIdAndFolderIdAndIsDeletedFalse(userId, null);
    }

    public List<FileEntity> getFilesByFolder(Long userId, Long folderId) {
        return fileRepository.findByUserIdAndFolderIdAndIsDeletedFalse(userId, folderId);
    }

    public List<FileEntity> getTrashedFiles(Long userId) {
        return fileRepository.findByUserIdAndIsDeletedTrue(userId);
    }

    public FileEntity getFile(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not Found"));
    }

    public List<FileVersionEntity> getFileVersions(Long fileId) {
        return fileVersionRepository.findByFileIdOrderByVersionDesc(fileId);
    }
}
