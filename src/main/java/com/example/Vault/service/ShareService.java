package com.example.Vault.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.Vault.DTO.ShareRequest;
import com.example.Vault.model.ShareLink;
import com.example.Vault.repository.ShareLinkRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ShareService(ShareLinkRepository shareLinkRepository) {
        this.shareLinkRepository = shareLinkRepository;
    }

    public ShareLink createShare(Long fileId, ShareRequest shareRequest)
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
        shareLink.setCreatedAt(LocalDateTime.now());
        shareLink.setDownloadCount(0);
        shareLink.setActive(true);
        shareLink.setViewOnce(shareRequest.isViewOnce());
        shareLink.setMaxDownloads(maxDownloads);
        shareLink.setExpiryTime(LocalDateTime.now().plusHours(hours));
        shareLink.setPassword(encoder.encode(shareRequest.getPassword()));

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
            if(!encoder.matches(link.getPassword(), password))
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
}
