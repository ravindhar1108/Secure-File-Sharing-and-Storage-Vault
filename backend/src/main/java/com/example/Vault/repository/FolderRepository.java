package com.example.Vault.repository;

import com.example.Vault.model.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    List<FolderEntity> findByUserIdAndIsDeletedFalse(Long userId);
    List<FolderEntity> findByUserIdAndParentIdAndIsDeletedFalse(Long userId, Long parentId);
    List<FolderEntity> findByUserIdAndIsDeletedTrue(Long userId);
}
