package com.example.Vault.controller;

import com.example.Vault.DTO.ShareRequest;
import com.example.Vault.model.FileEntity;
import com.example.Vault.model.ShareLink;
import com.example.Vault.model.User;
import com.example.Vault.service.FileService;
import com.example.Vault.service.ShareService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/share")
public class ShareController {

    private final ShareService shareService;
    private final FileService fileService;

    public ShareController(ShareService shareService, FileService fileService) {
        this.shareService = shareService;
        this.fileService = fileService;
    }

    @PostMapping("/create/{id}")
    public String create(@PathVariable Long id, @RequestBody ShareRequest shareRequest)
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FileEntity file = fileService.getFile(id);
        if(!file.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        ShareLink link =  shareService.createShare(id, shareRequest);
        return "http://localhost:8080/share/" + link.getToken();
    }

    @PostMapping("/{token}/validate")
    public ResponseEntity<java.util.Map<String, Object>> validateToken(@PathVariable String token, @RequestBody(required = false) java.util.Map<String, String> body) {
        String password = body != null ? body.get("password") : null;
        ShareLink link = shareService.validate(token, password);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("valid", true);
        response.put("viewOnce", link.isViewOnce());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token, @RequestParam(required = false) String password, @RequestParam(required = false, defaultValue = "download") String action) throws java.io.IOException
    {
        ShareLink link = shareService.validate(token, password);

        if (link.isViewOnce() && "download".equals(action)) {
            // Force it to be view-only
            action = "view";
        }

        FileEntity file = fileService.getFile(link.getFileId());

        File diskFile = new File(file.getStoragePath());

        InputStreamResource resource = new InputStreamResource(new java.io.FileInputStream(diskFile));

        shareService.incrementDownload(link);

        String contentType = java.nio.file.Files.probeContentType(diskFile.toPath());
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

}
