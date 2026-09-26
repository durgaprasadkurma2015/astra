package com.astra.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Backoff;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("astraTaskExecutor")
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void sendOtp(String to, String otp) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Astra Account");

            String html = buildOtpEmail(otp);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException ex) {
            throw new RuntimeException(
                    "Failed to send verification email.",
                    ex
            );
        }
    }

    @Async("astraTaskExecutor")
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void sendPasswordReset(String to, String token) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Astra Password");

            String resetUrl =
                    "http://localhost:3000/reset-password?token=" + token;

            String html = buildPasswordResetEmail(resetUrl);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException ex) {
            throw new RuntimeException(
                    "Failed to send password reset email.",
                    ex
            );
        }
    }

    @Recover
    public void recoverEmail(RuntimeException ex, String to, String otp) {
        // Final failure intentionally handled by the async executor.
    }

    private String buildOtpEmail(String otp) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Verify Your Astra Account</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f7fb;
                    font-family:Arial,Helvetica,sans-serif;
                ">

                    <table width="100%%"
                           cellpadding="0"
                           cellspacing="0"
                           border="0"
                           style="
                               background-color:#f4f7fb;
                               padding:40px 15px;
                           ">
                        <tr>
                            <td align="center">

                                <table width="600"
                                       cellpadding="0"
                                       cellspacing="0"
                                       border="0"
                                       style="
                                           max-width:600px;
                                           width:100%%;
                                           background:#ffffff;
                                           border-radius:16px;
                                           overflow:hidden;
                                       ">

                                    <tr>
                                        <td align="center"
                                            style="
                                                background:#2563eb;
                                                padding:32px 20px;
                                            ">

                                            <div style="
                                                color:#ffffff;
                                                font-size:32px;
                                                font-weight:bold;
                                            ">
                                                ASTRA
                                            </div>

                                            <div style="
                                                color:#dbeafe;
                                                font-size:14px;
                                                margin-top:8px;
                                            ">
                                                Welcome to Astra
                                            </div>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:40px 35px;">

                                            <h1 style="
                                                margin:0 0 15px 0;
                                                color:#111827;
                                                font-size:26px;
                                                text-align:center;
                                            ">
                                                Verify Your Account
                                            </h1>

                                            <p style="
                                                color:#4b5563;
                                                font-size:16px;
                                                line-height:1.6;
                                                text-align:center;
                                            ">
                                                Thank you for creating
                                                your Astra account.
                                                Please use the verification
                                                code below to complete
                                                your registration.
                                            </p>

                                            <table width="100%%"
                                                   cellpadding="0"
                                                   cellspacing="0">
                                                <tr>
                                                    <td align="center"
                                                        style="
                                                            padding:25px;
                                                            background:#eff6ff;
                                                            border-radius:12px;
                                                        ">

                                                        <div style="
                                                            color:#6b7280;
                                                            font-size:13px;
                                                            margin-bottom:10px;
                                                            text-transform:uppercase;
                                                            letter-spacing:1px;
                                                        ">
                                                            Verification Code
                                                        </div>

                                                        <div style="
                                                            color:#2563eb;
                                                            font-size:36px;
                                                            font-weight:bold;
                                                            letter-spacing:8px;
                                                        ">
                                                            {{OTP}}
                                                        </div>

                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="
                                                text-align:center;
                                                color:#6b7280;
                                                font-size:14px;
                                            ">
                                                This code will expire
                                                in <strong>10 minutes</strong>.
                                            </p>

                                            <div style="
                                                margin-top:30px;
                                                padding:18px;
                                                background:#fff7ed;
                                                border-left:4px solid #f97316;
                                                border-radius:8px;
                                            ">

                                                <strong style="color:#9a3412;">
                                                    Security Notice
                                                </strong>

                                                <p style="
                                                    color:#7c2d12;
                                                    font-size:13px;
                                                    line-height:1.6;
                                                ">
                                                    Never share this verification
                                                    code with anyone. Astra will
                                                    never ask you for your OTP by
                                                    phone, email, or chat.
                                                </p>

                                            </div>

                                            <p style="
                                                color:#6b7280;
                                                font-size:13px;
                                                text-align:center;
                                            ">
                                                If you did not create an Astra
                                                account, you can safely ignore
                                                this email.
                                            </p>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center"
                                            style="
                                                background:#f9fafb;
                                                padding:25px;
                                            ">

                                            <strong style="color:#374151;">
                                                Astra Team
                                            </strong>

                                            <p style="
                                                color:#9ca3af;
                                                font-size:12px;
                                            ">
                                                This is an automated message.
                                                Please do not reply.
                                            </p>

                                            <p style="
                                                color:#9ca3af;
                                                font-size:12px;
                                            ">
                                                © Astra
                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """.replace("{{OTP}}", otp);
    }

    private String buildPasswordResetEmail(String resetUrl) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Reset Your Astra Password</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f7fb;
                    font-family:Arial,Helvetica,sans-serif;
                ">

                    <table width="100%%"
                           cellpadding="0"
                           cellspacing="0"
                           border="0"
                           style="
                               background-color:#f4f7fb;
                               padding:40px 15px;
                           ">
                        <tr>
                            <td align="center">

                                <table width="600"
                                       cellpadding="0"
                                       cellspacing="0"
                                       border="0"
                                       style="
                                           max-width:600px;
                                           width:100%%;
                                           background:#ffffff;
                                           border-radius:16px;
                                           overflow:hidden;
                                       ">

                                    <tr>
                                        <td align="center"
                                            style="
                                                background:#2563eb;
                                                padding:32px 20px;
                                            ">

                                            <div style="
                                                color:#ffffff;
                                                font-size:32px;
                                                font-weight:bold;
                                            ">
                                                ASTRA
                                            </div>

                                            <div style="
                                                color:#dbeafe;
                                                font-size:14px;
                                                margin-top:8px;
                                            ">
                                                Password Recovery
                                            </div>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:40px 35px;">

                                            <h1 style="
                                                margin:0 0 15px 0;
                                                color:#111827;
                                                font-size:26px;
                                                text-align:center;
                                            ">
                                                Reset Your Password
                                            </h1>

                                            <p style="
                                                color:#4b5563;
                                                font-size:16px;
                                                line-height:1.6;
                                                text-align:center;
                                            ">
                                                We received a request to reset
                                                your Astra account password.
                                            </p>

                                            <div style="
                                                text-align:center;
                                                margin:30px 0;
                                            ">

                                                <a href="{{RESET_URL}}"
                                                   style="
                                                       display:inline-block;
                                                       background:#2563eb;
                                                       color:#ffffff;
                                                       padding:14px 28px;
                                                       border-radius:8px;
                                                       text-decoration:none;
                                                       font-weight:bold;
                                                       font-size:16px;
                                                   ">
                                                    Reset Password
                                                </a>

                                            </div>

                                            <p style="
                                                color:#6b7280;
                                                font-size:13px;
                                                line-height:1.6;
                                                text-align:center;
                                            ">
                                                If you did not request a password
                                                reset, you can safely ignore this
                                                email.
                                            </p>

                                            <p style="
                                                color:#9ca3af;
                                                font-size:12px;
                                                text-align:center;
                                                word-break:break-all;
                                            ">
                                                If the button does not work,
                                                copy and paste this URL:
                                                <br>
                                                {{RESET_URL}}
                                            </p>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center"
                                            style="
                                                background:#f9fafb;
                                                padding:25px;
                                            ">

                                            <strong style="color:#374151;">
                                                Astra Team
                                            </strong>

                                            <p style="
                                                color:#9ca3af;
                                                font-size:12px;
                                            ">
                                                This is an automated message.
                                                Please do not reply.
                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """.replace("{{RESET_URL}}", resetUrl);
    }
}
