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

    @GetMapping("/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token, @RequestParam(required = false) String password)
    {
        ShareLink link = shareService.validate(token, password);

        FileEntity file = fileService.getFile(link.getFileId());

        File diskFile = new File(file.getStoragePath());

        InputStreamResource resource = new InputStreamResource(new FileSystemResource(diskFile));

        shareService.incrementDownload(link);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename="+ file.getOriginalName())
                .body(resource);
    }

}
