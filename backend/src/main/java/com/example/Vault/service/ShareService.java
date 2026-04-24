package com.example.Vault.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.Vault.DTO.ActiveLinkDTO;
import com.example.Vault.DTO.ShareRequest;
import com.example.Vault.model.FileEntity;
import com.example.Vault.model.ShareLink;
import com.example.Vault.repository.FileRepository;
import com.example.Vault.repository.ShareLinkRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final FileRepository fileRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ShareService(ShareLinkRepository shareLinkRepository, FileRepository fileRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.fileRepository = fileRepository;
    }

    public ShareLink createShare(Long fileId, Long userId, ShareRequest shareRequest)
    {
        Integer maxDownloads = shareRequest.getMaxDownloads() != null
                ? shareRequest.getMaxDownloads()
                : Integer.MAX_VALUE;
        Integer hours = shareRequest.getExpiryHours() != null
                ? shareRequest.getExpiryHours()
                : 24;

        ShareLink shareLink = new ShareLink();
        shareLink.setToken(UUID.randomUUID().toString());
        shareLink.setFileId(fileId);
        shareLink.setUserId(userId);
        shareLink.setCreatedAt(LocalDateTime.now());
        shareLink.setDownloadCount(0);
        shareLink.setViewCount(0);
        shareLink.setActive(true);
        shareLink.setShareType(shareRequest.getShareType() != null ? shareRequest.getShareType() : "PRIVATE");
        
        if ("PUBLIC".equals(shareLink.getShareType())) {
            shareLink.setViewOnce(false);
            shareLink.setMaxDownloads(Integer.MAX_VALUE);
            shareLink.setExpiryTime(LocalDateTime.now().plusYears(10)); // Indefinite
            shareLink.setPassword(null);
        } else {
            shareLink.setViewOnce(shareRequest.isViewOnce());
            shareLink.setMaxDownloads(maxDownloads);
            shareLink.setExpiryTime(LocalDateTime.now().plusHours(hours));
            if (shareRequest.getPassword() != null && !shareRequest.getPassword().isEmpty()) {
                shareLink.setPassword(encoder.encode(shareRequest.getPassword()));
            } else {
                shareLink.setPassword(null);
            }
        }

        return shareLinkRepository.save(shareLink);
    }

    public ShareLink validate(String token, String password) {

        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));

        if(link.getPassword() != null)
        {
            if(password==null)
            {
                throw new RuntimeException("Password Required");
            }
            if(!encoder.matches(password, link.getPassword()))
            {
                throw new RuntimeException("Invalid Password");
            }
        }

        if (!link.isActive()) {
            throw new RuntimeException("Link disabled");
        }

        if (link.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link expired");
        }

        if (link.getDownloadCount() >= link.getMaxDownloads()) {
            throw new RuntimeException("Download limit reached");
        }

        return link;
    }

    @Transactional
    public void incrementDownload(ShareLink link) {

        link.setDownloadCount(link.getDownloadCount() + 1);

        if (link.isViewOnce()) {
            link.setActive(false);
        }

        if (link.getDownloadCount() >= link.getMaxDownloads()) {
            link.setActive(false);
        }
        System.out.println(link.getDownloadCount());
        System.out.println(link.isViewOnce());
        shareLinkRepository.save(link);
    }
    @Transactional
    public void incrementView(ShareLink link) {
        if(link.getViewCount() == null) link.setViewCount(0);
        link.setViewCount(link.getViewCount() + 1);
        shareLinkRepository.save(link);
    }

    public java.util.List<ActiveLinkDTO> getActiveLinksForUser(Long userId) {
        java.util.List<ShareLink> links = shareLinkRepository.findByUserId(userId);
        java.util.List<ActiveLinkDTO> dtos = new java.util.ArrayList<>();
        
        for(ShareLink link : links) {
            ActiveLinkDTO dto = new ActiveLinkDTO();
            dto.setToken(link.getToken());
            dto.setFileId(link.getFileId());
            dto.setExpiryTime(link.getExpiryTime());
            dto.setMaxDownloads(link.getMaxDownloads());
            dto.setDownloadCount(link.getDownloadCount());
            dto.setViewCount(link.getViewCount() != null ? link.getViewCount() : 0);
            dto.setViewOnce(link.isViewOnce());
            dto.setShareType(link.getShareType());
            dto.setActive(link.isActive() && link.getExpiryTime().isAfter(LocalDateTime.now()));
            dto.setCreatedAt(link.getCreatedAt());

            fileRepository.findById(link.getFileId()).ifPresent(f -> {
                dto.setFileName(f.getOriginalName());
            });
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public void toggleActive(String token, Long userId) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));
        if (!link.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        link.setActive(!link.isActive());
        shareLinkRepository.save(link);
    }

    @Transactional
    public void deleteLink(String token, Long userId) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));
        if (!link.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        shareLinkRepository.delete(link);
    }
}
