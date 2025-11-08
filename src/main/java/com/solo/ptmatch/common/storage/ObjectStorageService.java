package com.solo.ptmatch.common.storage;

public interface ObjectStorageService {

    PresignedUpload issuePresignedUpload(PresignedUploadCommand command);
}
