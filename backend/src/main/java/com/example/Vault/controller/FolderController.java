package com.example.Vault.controller;

import com.example.Vault.model.FolderEntity;
import com.example.Vault.model.User;
import com.example.Vault.service.FolderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public FolderEntity createFolder(@RequestParam String name, @RequestParam(required = false) Long parentId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return folderService.createFolder(name, parentId, user.getId());
    }

    @GetMapping
    public List<FolderEntity> getRootFolders() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return folderService.getFoldersInRoot(user.getId());
    }

    @GetMapping("/{id}/subfolders")
    public List<FolderEntity> getSubFolders(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return folderService.getSubFolders(id, user.getId());
    }

    @DeleteMapping("/{id}")
    public String deleteFolder(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        folderService.deleteFolder(id, user.getId());
        return "Folder Deleted";
    }
}
