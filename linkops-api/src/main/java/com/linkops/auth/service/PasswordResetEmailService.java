package com.linkops.auth.service;

import com.linkops.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String emailFrom;
    private final String mailHost;
    private final boolean production;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${linkops.security.password-reset.frontend-url}") String frontendUrl,
            @Value("${linkops.security.password-reset.email-from}") String emailFrom,
            @Value("${spring.mail.host:}") String mailHost,
            Environment environment
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.emailFrom = emailFrom;
        this.mailHost = mailHost;
        this.production = environment.acceptsProfiles(Profiles.of("prod"));
    }

    public void send(String recipient, String rawToken) {
        String resetUrl = frontendUrl + (frontendUrl.contains("?") ? "&" : "?") + "token=" + rawToken;

        if (mailHost == null || mailHost.isBlank()) {
            if (production) {
                throw new ServiceUnavailableException(
                        "O envio do e-mail de recuperação não está configurado."
                );
            }
            log.warn("RECUPERAÇÃO DEV para {}: {}", recipient, resetUrl);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(recipient);
        message.setSubject("Redefinir a palavra-passe do LinkOps");
        message.setText("Recebemos um pedido para redefinir a sua palavra-passe. "
                + "Use a ligação abaixo nos próximos 5 minutos:\n\n"
                + resetUrl
                + "\n\nSe não fez este pedido, ignore esta mensagem.");

        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            log.error("Falha ao enviar e-mail de recuperação.", exception);
            throw new ServiceUnavailableException(
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente."
            );
        }
    }
}
