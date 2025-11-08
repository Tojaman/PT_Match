package com.solo.ptmatch.common.storage;

public record PresignedUploadCommand(
        Long ownerId,
        String originalFileName,
        String contentType,
        String directory
) {
}
