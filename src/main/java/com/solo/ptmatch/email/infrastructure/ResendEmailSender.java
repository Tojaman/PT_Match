package com.solo.ptmatch.email.infrastructure;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.core.net.RequestOptions;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResendEmailSender implements EmailSender {

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailSender(ResendProperties properties) {
        this.resend = new Resend(properties.getApiKey());
        this.fromEmail = properties.getFromEmail();
    }

    @Override
    public void send(String to, String subject, String htmlBody, String referenceId) {
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(htmlBody)
                .build();

        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(referenceId)
                    .build();

            resend.emails().send(emailOptions, requestOptions);
            log.debug("Resend 이메일 발송 완료. to={}, referenceId={}", to, referenceId);
        } catch (ResendException e) {
            throw new RuntimeException("Resend 이메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
