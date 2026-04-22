package com.example.Vault.service;

import com.example.Vault.model.FolderEntity;
import com.example.Vault.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public FolderEntity createFolder(String name, Long parentId, Long userId) {
        FolderEntity folder = new FolderEntity();
        folder.setName(name);
        folder.setParentId(parentId);
        folder.setUserId(userId);
        return folderRepository.save(folder);
    }

    public List<FolderEntity> getFoldersInRoot(Long userId) {
        return folderRepository.findByUserIdAndParentIdAndIsDeletedFalse(userId, null);
    }

    public List<FolderEntity> getSubFolders(Long folderId, Long userId) {
        return folderRepository.findByUserIdAndParentIdAndIsDeletedFalse(userId, folderId);
    }

    public FolderEntity getFolder(Long id) {
        return folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Folder not found"));
    }

    public void deleteFolder(Long id, Long userId) {
        FolderEntity folder = getFolder(id);
        if (!folder.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        folder.setDeleted(true);
        folder.setDeletedAt(LocalDateTime.now());
        folderRepository.save(folder);
        // Note: cascade soft-deleting subfolders and files inside should be implemented as well.
    }
}
