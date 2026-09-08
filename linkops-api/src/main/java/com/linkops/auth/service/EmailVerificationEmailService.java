package com.linkops.auth.service;

import com.linkops.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class EmailVerificationEmailService {
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String emailFrom;
    private final String mailHost;
    private final Duration expiration;
    private final boolean production;

    public EmailVerificationEmailService(
            JavaMailSender mailSender,
            @Value("${linkops.security.email-verification.frontend-url}") String frontendUrl,
            @Value("${linkops.security.email-verification.email-from}") String emailFrom,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${linkops.security.email-verification.expiration}") Duration expiration,
            Environment environment
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.emailFrom = emailFrom;
        this.mailHost = mailHost;
        this.expiration = expiration;
        this.production = environment.acceptsProfiles(Profiles.of("prod"));
    }

    public void send(String recipient, String rawToken) {
        String url = frontendUrl + (frontendUrl.contains("?") ? "&" : "?") + "token=" + rawToken;
        if (mailHost == null || mailHost.isBlank()) {
            if (production) throw new ServiceUnavailableException("O envio do e-mail de confirmação não está configurado.");
            log.warn("CONFIRMAÇÃO DE E-MAIL DEV para {}: {}", recipient, url);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(recipient);
        message.setSubject("Confirme seu e-mail na LinkOps");
        message.setText("Bem-vindo à LinkOps! Confirme seu endereço de e-mail para continuar:\n\n"
                + url + "\n\nEste link é válido por " + expiration.toMinutes()
                + " minutos. Se não criou esta conta, ignore esta mensagem.");
        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            log.error("Falha ao enviar e-mail de confirmação.", exception);
            throw new ServiceUnavailableException("Não foi possível enviar o e-mail de confirmação. Tente novamente.");
        }
    }
}
