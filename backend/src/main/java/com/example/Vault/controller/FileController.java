package com.example.Vault.controller;

import com.example.Vault.model.FileEntity;
import com.example.Vault.model.FileVersionEntity;
import com.example.Vault.model.User;
import com.example.Vault.service.FileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public FileEntity upload(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Long folderId) throws Exception {
        System.out.println("Received upload request for file: " + file.getOriginalFilename());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        System.out.println("Uploading for userId: " + userId);

        return fileService.upload(file, userId, folderId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @RequestParam(required = false, defaultValue = "download") String action) throws Exception {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FileEntity file = fileService.getFile(id);

        if(!file.getUserId().equals(user.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }

        byte[] data;
        try {
            data = fileService.download(id);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }
        ByteArrayResource resource = new ByteArrayResource(data);

        String contentType = URLConnection.guessContentTypeFromName(file.getOriginalName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String dispositionType = "view".equals(action) ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header("Content-Disposition", dispositionType + "; filename=\"" + file.getOriginalName() + "\"")
                .header("Content-Type", contentType)
                .contentLength(file.getSize())
                .body(resource);
    }

    @GetMapping("/version/{versionId}")
    public ResponseEntity<byte[]> downloadVersion(@PathVariable Long versionId) throws Exception {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        byte[] data;
        try {
            data = fileService.downloadVersion(versionId);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }

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
