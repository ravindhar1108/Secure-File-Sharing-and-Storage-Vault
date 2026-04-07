package com.example.Vault.controller;

import com.example.Vault.model.FileEntity;
import com.example.Vault.model.User;
import com.example.Vault.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public FileEntity upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {

        User user = (User)request.getAttribute("user");
        Long userId = user.getId();

        return fileService.upload(file, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpServletRequest request) throws Exception {

        User user = (User)request.getAttribute("user");
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

    @GetMapping
    public List<FileEntity> getFiles(HttpServletRequest request) {

        User user = (User)request.getAttribute("user");
        if(user==null)
        {
            throw new RuntimeException("User not Found");
        }
        Long userId = user.getId();

        return fileService.getUserFiles(userId);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request) {

        User user = (User)request.getAttribute("user");
        Long userId = user.getId();
        fileService.delete(id, userId);

        return "File Deleted";
    }
}
