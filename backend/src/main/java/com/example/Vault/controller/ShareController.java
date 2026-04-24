package com.example.Vault.controller;

import com.example.Vault.DTO.ActiveLinkDTO;
import com.example.Vault.DTO.ShareRequest;
import com.example.Vault.model.FileEntity;
import com.example.Vault.model.ShareLink;
import com.example.Vault.model.User;
import com.example.Vault.service.FileService;
import com.example.Vault.service.ShareService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLConnection;

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
        ShareLink link =  shareService.createShare(id, user.getId(), shareRequest);
        return "http://localhost:8080/share/" + link.getToken();
    }

    @GetMapping("/active")
    public ResponseEntity<java.util.List<ActiveLinkDTO>> getActiveLinks() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(shareService.getActiveLinksForUser(user.getId()));
    }

    @PutMapping("/{token}/toggle")
    public ResponseEntity<?> toggleLink(@PathVariable String token) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        shareService.toggleActive(token, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<?> deleteLink(@PathVariable String token) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        shareService.deleteLink(token, user.getId());
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Resource> download(@PathVariable String token, @RequestParam(required = false) String password, @RequestParam(required = false, defaultValue = "download") String action) throws Exception
    {
        ShareLink link = shareService.validate(token, password);

        if (link.isViewOnce() && "download".equals(action)) {
            // Force it to be view-only
            action = "view";
        }

        FileEntity file = fileService.getFile(link.getFileId());

        byte[] data;
        try {
            data = fileService.download(file.getId());
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }
        
        ByteArrayResource resource = new ByteArrayResource(data);

        if ("view".equals(action)) {
            shareService.incrementView(link);
        } else {
            shareService.incrementDownload(link);
        }

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

}
