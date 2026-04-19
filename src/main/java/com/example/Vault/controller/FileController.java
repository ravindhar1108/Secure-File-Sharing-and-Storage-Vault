package com.example.Vault.controller;

import com.example.Vault.model.FileEntity;
import com.example.Vault.model.FileVersionEntity;
import com.example.Vault.model.User;
import com.example.Vault.service.FileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public FileEntity upload(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Long folderId) throws Exception {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();

        return fileService.upload(file, userId, folderId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FileEntity file = fileService.getFile(id);

        if(!file.getUserId().equals(user.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }

        File diskFile = new File(file.getStoragePath());

        InputStreamResource resource = new InputStreamResource(new FileInputStream(diskFile));


        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=" + file.getOriginalName())
                .contentLength(file.getSize())
                .body(resource);
    }

    @GetMapping("/version/{versionId}")
    public ResponseEntity<byte[]> downloadVersion(@PathVariable Long versionId) throws Exception {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        byte[] data = fileService.downloadVersion(versionId);

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=version_" + versionId)
                .body(data);
    }

    @GetMapping
    public List<FileEntity> getFiles(@RequestParam(required = false) Long folderId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user==null)
        {
            throw new RuntimeException("User not Found");
        }
        Long userId = user.getId();

        if (folderId != null) {
            return fileService.getFilesByFolder(userId, folderId);
        }
        return fileService.getUserFiles(userId);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        fileService.softDelete(id, userId);

        return "File moved to trash";
    }

    @GetMapping("/trash")
    public List<FileEntity> getTrashedFiles() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fileService.getTrashedFiles(user.getId());
    }

    @PostMapping("/{id}/restore")
    public String restoreFile(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        fileService.restore(id, user.getId());
        return "File restored";
    }

    @DeleteMapping("/{id}/permanent")
    public String permanentDelete(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        fileService.permanentDelete(id, user.getId());
        return "File permanently deleted";
    }

    @GetMapping("/{id}/versions")
    public List<FileVersionEntity> getFileVersions(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FileEntity file = fileService.getFile(id);
        if(!file.getUserId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized");
        }
        return fileService.getFileVersions(id);
    }
}
