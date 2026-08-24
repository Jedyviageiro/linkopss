package com.linkops.media.service;

public interface CloudMediaStorage {

    StoredMedia upload(
            byte[] content,
            String contentType,
            String filename,
            String folder
    );

    record StoredMedia(String url, String contentType, long size) {
    }
}
