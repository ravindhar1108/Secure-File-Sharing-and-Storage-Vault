package com.example.Vault.repository;

import com.example.Vault.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByUserIdAndIsDeletedFalse(Long userId);
    List<FileEntity> findByUserIdAndFolderIdAndIsDeletedFalse(Long userId, Long folderId);
    List<FileEntity> findByUserIdAndIsDeletedTrue(Long userId);
    
    // For deduplication
    boolean existsByFileHash(String fileHash);
    FileEntity findFirstByFileHash(String fileHash);
    
    // For versioning
    FileEntity findByUserIdAndFolderIdAndOriginalNameAndIsDeletedFalse(Long userId, Long folderId, String originalName);
}
