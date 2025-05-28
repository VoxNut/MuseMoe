package com.javaweb.utils;

import com.javaweb.constant.AppConstant;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class SendEmailUtil {
    public static void sendEmail(String to, String tempPassword) {
        final String username = System.getenv("EMAIL_USERNAME");
        final String emailPassword = System.getenv("EMAIL_PASSWORD");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, emailPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("MuseMoe - Password Reset Request");
            message.setSentDate(new Date());

            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();

            // Main background color: #1A1A1A (BACKGROUND_COLOR)
            // Text color: #F8E1E9 (TEXT_COLOR)
            // Button background: #2E2D3B (BUTTON_BACKGROUND_COLOR)
            // Active button: #F8E1E9 (ACTIVE_BUTTON_BACKGROUND_COLOR)

            String htmlContent = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "  <style>\n" +
                    "    @font-face {\n" +
                    "      font-family: 'IBM Plex Sans JP';\n" +
                    "      src: url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans+JP&display=swap');\n" +
                    "    }\n" +
                    "    body {\n" +
                    "      font-family: 'IBM Plex Sans JP', 'Arial', sans-serif;\n" +
                    "      background-color: #F5F5F5;\n" +
                    "      margin: 0;\n" +
                    "      padding: 0;\n" +
                    "      font-size: 16px;\n" +
                    "      color: #333333;\n" +
                    "    }\n" +
                    "    .email-container {\n" +
                    "      max-width: 600px;\n" +
                    "      margin: 40px auto;\n" +
                    "      background-color: #FFFFFF;\n" +
                    "      border-radius: 8px;\n" +
                    "      overflow: hidden;\n" +
                    "      box-shadow: 0 4px 10px rgba(0,0,0,0.1);\n" +
                    "    }\n" +
                    "    .email-header {\n" +
                    "      background-color: #1A1A1A;\n" +
                    "      padding: 20px 30px;\n" +
                    "      text-align: center;\n" +
                    "    }\n" +
                    "    .email-body {\n" +
                    "      padding: 30px;\n" +
                    "      color: #333333;\n" +
                    "    }\n" +
                    "    .logo {\n" +
                    "      width: 80px;\n" +
                    "      height: auto;\n" +
                    "    }\n" +
                    "    .title {\n" +
                    "      color: #F8E1E9;\n" +
                    "      font-size: 24px;\n" +
                    "      margin: 15px 0 0;\n" +
                    "      font-weight: bold;\n" +
                    "    }\n" +
                    "    .password-container {\n" +
                    "      background-color: #F8F8F8;\n" +
                    "      border-radius: 6px;\n" +
                    "      padding: 20px;\n" +
                    "      margin: 25px 0;\n" +
                    "      text-align: center;\n" +
                    "    }\n" +
                    "    .password {\n" +
                    "      background-color: #2E2D3B;\n" +
                    "      color: #F8E1E9;\n" +
                    "      padding: 12px 25px;\n" +
                    "      border-radius: 4px;\n" +
                    "      font-size: 18px;\n" +
                    "      font-weight: bold;\n" +
                    "      display: inline-block;\n" +
                    "      letter-spacing: 1px;\n" +
                    "    }\n" +
                    "    .footer {\n" +
                    "      background-color: #F8F8F8;\n" +
                    "      padding: 20px 30px;\n" +
                    "      text-align: center;\n" +
                    "      font-size: 12px;\n" +
                    "      color: #777777;\n" +
                    "      border-top: 1px solid #EEEEEE;\n" +
                    "    }\n" +
                    "    .button {\n" +
                    "      background-color: #3A2E2A;\n" +
                    "      color: #FFFFFF;\n" +
                    "      padding: 12px 25px;\n" +
                    "      text-decoration: none;\n" +
                    "      border-radius: 4px;\n" +
                    "      display: inline-block;\n" +
                    "      margin-top: 20px;\n" +
                    "      font-weight: bold;\n" +
                    "    }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <div class=\"email-container\">\n" +
                    "    <div class=\"email-header\">\n" +
                    "      <img src=\"cid:logo\" alt=\"MuseMoe Logo\" class=\"logo\">\n" +
                    "      <h1 class=\"title\">MuseMoe</h1>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div class=\"email-body\">\n" +
                    "      <h2>Password Reset</h2>\n" +
                    "      <p>We received a request to reset your password for your MuseMoe account.</p>\n" +
                    "      <p>Your temporary password is:</p>\n" +
                    "      \n" +
                    "      <div class=\"password-container\">\n" +
                    "        <span class=\"password\">" + tempPassword + "</span>\n" +
                    "      </div>\n" +
                    "      \n" +
                    "      <p>Please use this temporary password to log in and change your password immediately for security reasons.</p>\n" +
                    "      <p>If you didn't request this password reset, please contact our support team immediately.</p>\n" +
                    "      \n" +
                    "      <div style=\"text-align: center;\">\n" +
                    "        <a href=\"http://localhost:8081\" class=\"button\">Go to MuseMoe</a>\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div class=\"footer\">\n" +
                    "      <p>&copy; " + java.time.Year.now().getValue() + " MuseMoe. All rights reserved.</p>\n" +
                    "      <p>Your musical companion - Where every note finds its harmony.</p>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</body>\n" +
                    "</html>";

            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // Image part - using the MuseMoe logo
            MimeBodyPart imagePart = new MimeBodyPart();
            DataSource fds = new FileDataSource(AppConstant.MUSE_MOE_LOGO_PATH);
            imagePart.setDataHandler(new javax.activation.DataHandler(fds));
            imagePart.setHeader("Content-ID", "<logo>");
            imagePart.setFileName("muse_moe_logo.png");
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Password reset email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

