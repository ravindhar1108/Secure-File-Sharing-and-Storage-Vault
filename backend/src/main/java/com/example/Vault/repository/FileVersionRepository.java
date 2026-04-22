package com.example.Vault.repository;

import com.example.Vault.model.FileVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersionEntity, Long> {
    List<FileVersionEntity> findByFileIdOrderByVersionDesc(Long fileId);
    boolean existsByFileHash(String fileHash);
    FileVersionEntity findFirstByFileHash(String fileHash);
}
