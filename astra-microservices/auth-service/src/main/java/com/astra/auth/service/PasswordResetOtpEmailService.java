
package com.astra.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetOtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public PasswordResetOtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("ASTRA - Password Reset OTP");

        message.setText("""
                Hello!

                Your ASTRA password reset OTP is: %s

                This OTP expires in 5 minutes.

                Do not share this OTP with anyone.

                If you did not request a password reset, please ignore this email.

                Thank you,
                ASTRA Team
                """.formatted(otp));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "Unable to send password reset OTP email", ex
            );
        }
    }
}
