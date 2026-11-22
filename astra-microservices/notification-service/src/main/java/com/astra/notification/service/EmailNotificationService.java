
package com.astra.notification.service;

import com.astra.notification.entity.NotificationEvent;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(NotificationEvent notification) {

        if (notification.getRecipientEmail() == null
                || notification.getRecipientEmail().isBlank()) {
            throw new IllegalArgumentException(
                "Notification recipient email is missing"
            );
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                false,
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(buildEmailBody(notification), true);

            mailSender.send(message);

        } catch (Exception exception) {
            throw new IllegalStateException(
                "Email delivery failed: " + exception.getMessage(),
                exception
            );
        }
    }

    private String buildEmailBody(NotificationEvent notification) {

        return """
            <html>
            <body style="font-family:Arial,sans-serif;color:#333;">
                <h2 style="color:#2563eb;">Astra Notification</h2>
                <p>Hello,</p>
                <p>%s</p>
                <p>Order Reference: <strong>%s</strong></p>
                <p>Thank you for shopping with Astra!</p>
                <hr/>
                <small>This is an automated notification from Astra.</small>
            </body>
            </html>
            """.formatted(
                escapeHtml(notification.getMessage()),
                escapeHtml(notification.getOrderNumber() == null
                    ? "N/A" : notification.getOrderNumber())
            );
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
