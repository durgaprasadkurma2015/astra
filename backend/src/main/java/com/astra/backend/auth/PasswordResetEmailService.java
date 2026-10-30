
package com.astra.backend.auth;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtp(String email, String otp) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException(
                    "SMTP sender address is not configured"
            );
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // Multipart mode supports both plain-text and HTML content.
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("ASTRA | Password Reset Verification");

            String safeOtp = escapeHtml(otp);

            String plainText = """
                    ASTRA Password Reset

                    Your ASTRA password reset code is: %s

                    This code expires in 5 minutes.

                    If you did not request a password reset, ignore this email.
                    """.formatted(otp);

            helper.setText(plainText, buildHtmlEmail(safeOtp));

            mailSender.send(message);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to send password reset email", e
            );
        }
    }

    private String buildHtmlEmail(String safeOtp) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport"
                        content="width=device-width, initial-scale=1.0">
                  <meta name="color-scheme" content="dark">
                  <meta name="supported-color-schemes" content="dark">
                  <title>ASTRA Password Reset</title>

                  <style>
                    body {
                      margin: 0;
                      padding: 0;
                      background-color: #080b18;
                      font-family: Arial, Helvetica, sans-serif;
                      color: #f4f6ff;
                    }

                    table {
                      border-collapse: collapse;
                      border-spacing: 0;
                    }

                    .email-container {
                      width: 100%%;
                      max-width: 580px;
                      background-color: #10162b;
                      border: 1px solid #28345a;
                      border-radius: 18px;
                      overflow: hidden;
                    }

                    .content-padding {
                      padding-left: 28px;
                      padding-right: 28px;
                    }

                    .heading {
                      font-size: 29px;
                      line-height: 1.35;
                      font-weight: 800;
                      color: #ffffff;
                    }

                    .otp-code {
                      display: inline-block;
                      padding: 18px 22px;
                      background-color: #171e3a;
                      border: 1px solid #6250d9;
                      border-radius: 12px;
                      color: #ffffff;
                      font-size: 32px;
                      font-weight: 800;
                      letter-spacing: 8px;
                      overflow-wrap: anywhere;
                    }

                    @media only screen and (max-width: 600px) {
                      .outer-padding {
                        padding: 12px 8px !important;
                      }

                      .content-padding {
                        padding-left: 18px !important;
                        padding-right: 18px !important;
                      }

                      .heading {
                        font-size: 25px !important;
                      }

                      .otp-code {
                        font-size: 26px !important;
                        letter-spacing: 5px !important;
                        padding: 16px 12px !important;
                      }

                      .brand {
                        font-size: 30px !important;
                      }
                    }
                  </style>
                </head>

                <body style="margin:0;padding:0;background-color:#080b18;">

                  <table role="presentation" width="100%%"
                         cellpadding="0" cellspacing="0" border="0"
                         bgcolor="#080b18">
                    <tr>
                      <td align="center" class="outer-padding"
                          style="padding:32px 14px;">

                        <table role="presentation" width="100%%"
                               cellpadding="0" cellspacing="0" border="0"
                               class="email-container" bgcolor="#10162b"
                               style="max-width:580px;background-color:#10162b;
                                      border:1px solid #28345a;
                                      border-radius:18px;">

                          <!-- ASTRA brand -->
                          <tr>
                            <td align="center" bgcolor="#0c1122"
                                style="padding:32px 20px 26px;
                                       background-color:#0c1122;">

                              <div class="brand"
                                   style="font-size:36px;font-weight:800;
                                          letter-spacing:4px;color:#ffffff;">
                                ASTRA
                              </div>

                              <div style="font-size:10px;letter-spacing:4px;
                                          color:#9ba8d4;padding-top:9px;">
                                SHOP &bull; EXPLORE &bull; ENJOY
                              </div>

                              <table role="presentation" align="center"
                                     cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td width="58" height="4" bgcolor="#8065ff"
                                      style="background-color:#8065ff;
                                             font-size:0;line-height:0;">
                                    &nbsp;
                                  </td>
                                </tr>
                              </table>

                            </td>
                          </tr>

                          <!-- Introduction -->
                          <tr>
                            <td align="center" class="content-padding"
                                style="padding-top:34px;">

                              <div style="font-size:38px;line-height:1.2;">
                                &#128274;
                              </div>

                              <div class="heading"
                                   style="font-size:29px;line-height:1.35;
                                          font-weight:800;color:#ffffff;
                                          margin-top:18px;">
                                Reset Your<br>
                                <span style="color:#9582ff;">
                                  Password
                                </span>
                              </div>

                              <p style="font-size:15px;color:#e0e5ff;
                                        margin:20px 0 10px;">
                                Let's get you back into ASTRA.
                              </p>

                              <p style="font-size:14px;line-height:1.8;
                                        color:#aeb9da;margin:0;">
                                We received a request to reset your password.
                                Use the verification code below to continue.
                              </p>

                            </td>
                          </tr>

                          <!-- OTP card -->
                          <tr>
                            <td class="content-padding"
                                style="padding-top:28px;">

                              <table role="presentation" width="100%%"
                                     cellpadding="0" cellspacing="0" border="0"
                                     bgcolor="#0b1125"
                                     style="background-color:#0b1125;
                                            border:1px solid #5144b9;
                                            border-radius:15px;">
                                <tr>
                                  <td align="center"
                                      style="padding:26px 12px;">

                                    <p style="font-size:12px;font-weight:bold;
                                              letter-spacing:2px;
                                              color:#b8c2e8;margin:0 0 22px;">
                                      PASSWORD RESET CODE
                                    </p>

                                    <div class="otp-code"
                                         style="display:inline-block;
                                                padding:18px 22px;
                                                background-color:#171e3a;
                                                border:1px solid #6250d9;
                                                border-radius:12px;
                                                color:#ffffff;font-size:32px;
                                                font-weight:800;
                                                letter-spacing:8px;">
                                      %s
                                    </div>

                                    <p style="font-size:13px;line-height:1.8;
                                              color:#aeb9da;margin:22px 0 0;">
                                      This code expires in
                                      <strong style="color:#b7a5ff;">
                                        5 minutes
                                      </strong>.
                                    </p>

                                  </td>
                                </tr>
                              </table>

                            </td>
                          </tr>

                          <!-- Security notice -->
                          <tr>
                            <td class="content-padding"
                                style="padding-top:18px;padding-bottom:28px;">

                              <table role="presentation" width="100%%"
                                     cellpadding="0" cellspacing="0" border="0"
                                     bgcolor="#171a35"
                                     style="background-color:#171a35;
                                            border:1px solid #34305e;
                                            border-radius:12px;">
                                <tr>
                                  <td style="padding:18px 16px;">

                                    <p style="font-size:14px;font-weight:bold;
                                              color:#c3b5ff;margin:0 0 8px;">
                                      Keep your account secure
                                    </p>

                                    <p style="font-size:12px;line-height:1.8;
                                              color:#b5bfdc;margin:0;">
                                      If you did not request a password reset,
                                      you can safely ignore this email.
                                      Never share your verification code.
                                    </p>

                                  </td>
                                </tr>
                              </table>

                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td align="center" bgcolor="#0c1122"
                                style="padding:26px 22px 30px;
                                       background-color:#0c1122;
                                       border-top:1px solid #29314c;">

                              <p style="font-size:15px;font-weight:bold;
                                        color:#ffffff;margin:0 0 12px;">
                                Your security matters to us.
                              </p>

                              <p style="font-size:12px;line-height:1.8;
                                        color:#929fbe;margin:0;">
                                ASTRA &bull; Shop. Explore. Enjoy.
                              </p>

                              <p style="font-size:11px;color:#687695;
                                        margin:22px 0 0;">
                                &copy; ASTRA. All rights reserved.
                              </p>

                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>

                </body>
                </html>
                """.formatted(safeOtp);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
