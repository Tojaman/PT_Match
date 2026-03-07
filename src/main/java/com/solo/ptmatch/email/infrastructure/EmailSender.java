package com.solo.ptmatch.email.infrastructure;

public interface EmailSender {
    void send(String to, String subject, String htmlBody, String referenceId);
}
