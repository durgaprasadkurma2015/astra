package com.astra.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RegistrationOtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public RegistrationOtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("ASTRA - Email Verification");

        message.setText("""
                Welcome to ASTRA!

                Your email verification OTP is: %s

                This OTP expires in 5 minutes.

                Do not share this OTP with anyone.

                Thank you,
                ASTRA Team
                """.formatted(otp));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "Unable to send registration OTP email", ex
            );
        }
    }
}