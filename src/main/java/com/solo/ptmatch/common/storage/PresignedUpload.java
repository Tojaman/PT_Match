package com.solo.ptmatch.common.storage;

public record PresignedUpload(
        String uploadUrl,
        String objectKey,
        int expirySeconds
) {
}
