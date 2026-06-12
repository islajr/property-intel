package io.propertyintel.api.global.resend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import io.propertyintel.api.global.exception.exceptions.EmailVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@Slf4j
public class ResendEmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;    // thymeleaf as default choice

    @Value("${email.sender.address}")
    private String senderAddress;

    @Value("${email.confirmation.expiryHours}")
    private int expiryHours;

    public ResendEmailService(@Value("${resend.api-key}") String apiKey, TemplateEngine templateEngine) {
        this.resend = new Resend(apiKey);
        this.templateEngine = templateEngine;
    }

    /*
    * Fire and forget: Called by the @Async method from the verification service
    * Throws Runtime Exception on failure so the threadExecutor can log/retry the action.
    * */
    public void sendVerificationEmail(String emailTo, String verificationURL) {
        String html = buildVerificationHTML(emailTo, verificationURL);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(senderAddress)
                .to(List.of(emailTo))
                .subject("PropertyIntel — Confirm your e-mail address")
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Verification e-mail sent to {} | Resend ID: {}", emailTo, response.getId());
        } catch (ResendException e) {
            log.warn("Failed to send verification e-mail to {}", emailTo);
            throw new EmailVerificationException("Email dispatch failed for user: %s".formatted(emailTo));
        }
    }

    private String buildVerificationHTML(String email, String verificationURL) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verificationURL", verificationURL);
        context.setVariable("expiryHours", expiryHours);

        return templateEngine.process("email/verification", context);
    }
}
