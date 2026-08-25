package com.linkops.auth.service;

import com.linkops.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
public class PasswordResetEmailService {

    private final String frontendUrl;
    private final String emailFrom;
    private final String mailHost;
    private final int mailPort;
    private final String mailUsername;
    private final String mailPassword;
    private final boolean startTls;
    private final boolean production;

    public PasswordResetEmailService(
            @Value("${linkops.security.password-reset.frontend-url}") String frontendUrl,
            @Value("${linkops.security.password-reset.email-from}") String emailFrom,
            @Value("${linkops.security.password-reset.mail-host:}") String mailHost,
            @Value("${linkops.security.password-reset.mail-port:587}") int mailPort,
            @Value("${linkops.security.password-reset.mail-username:}") String mailUsername,
            @Value("${linkops.security.password-reset.mail-password:}") String mailPassword,
            @Value("${linkops.security.password-reset.mail-starttls:true}") boolean startTls,
            Environment environment
    ) {
        this.frontendUrl = frontendUrl;
        this.emailFrom = emailFrom;
        this.mailHost = mailHost;
        this.mailPort = mailPort;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.startTls = startTls;
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

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailHost);
        sender.setPort(mailPort);
        sender.setUsername(mailUsername);
        sender.setPassword(mailPassword);

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", Boolean.toString(mailUsername != null && !mailUsername.isBlank()));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(recipient);
        message.setSubject("Redefinir a palavra-passe do LinkOps");
        message.setText("Recebemos um pedido para redefinir a sua palavra-passe. "
                + "Use a ligação abaixo nos próximos minutos:\n\n"
                + resetUrl
                + "\n\nSe não fez este pedido, ignore esta mensagem.");

        try {
            sender.send(message);
        } catch (RuntimeException exception) {
            log.error("Falha ao enviar e-mail de recuperação.", exception);
            throw new ServiceUnavailableException(
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente."
            );
        }
    }
}
