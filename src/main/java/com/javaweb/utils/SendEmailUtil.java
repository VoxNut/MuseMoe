package com.javaweb.utils;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendEmailUtil {

    private static final ExecutorService emailExecutor = Executors.newSingleThreadExecutor();

    public static void sendEmail(String to, String tempPassword) {
        final String username = "jonathanvex2@gmail.com";
        final String emailPassword = "zykykavxtvjyejht";

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
            message.setSubject("Password Reset Request");
            message.setSentDate(new Date());
            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent =
                    "<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "  <meta charset=\"UTF-8\">\n" +
                            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "  <link href='https://fonts.googleapis.com/css?family=JetBrains Mono' rel='stylesheet'>\n" +
                            "  <script src=\"https://cdn.tailwindcss.com\"></script>\n" +
                            "</head>\n" +
                            "<body style=\"font-family: 'Jetbrains Mono'; background-color: #f5f5f5; margin: 0; padding: 0; font-size: 18px;\">\n" +
                            "  <div style=\"background-color: #ffffff; max-width: 600px; margin: 40px auto; padding: 30px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);\">\n" +
                            "    <div style=\"display: flex; align-items: center; margin-bottom: 25px;\">\n" +
                            "      <img style=\"width: 50px; height: 50px; margin-right: 10px;\" src=\"cid:logo\" alt=\"Latte Literature Logo\" />\n" +
                            "      <h1 style=\"font-size: 24px; color: #333; margin: 0;\">Latte Literature</h1>\n" +
                            "    </div>\n" +
                            "    <hr style=\"margin-bottom: 25px;\">\n" +
                            "    <p style=\"font-size: 16px; color: #333; margin-bottom: 10px;\">Chúng tôi nhận thấy một yêu cầu đổi mật khẩu từ bạn.</p>\n" +
                            "    <p style=\"font-size: 16px; color: #333; margin-bottom: 20px;\">Mật khẩu tạm thời của bạn là:</p>\n" +
                            "    <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
                            "      <span style=\"background: #2E2D2B; color: #D7B899; padding: 12px 20px; border-radius: 3px; font-size: 16px; font-weight: bold;\">"
                            + tempPassword + "</span>\n" +
                            "    </div>\n" +
                            "    <p style=\"font-size: 14px; color: #555;\">Vui lòng sử dụng mật khẩu này để đăng nhập và đổi mật khẩu ngay lập tức.</p>\n" +
                            "    <div style=\"margin-top: 40px; border-top: 1px solid #ddd; padding-top: 10px; font-size: 12px; color: #bbb;\">\n" +
                            "      © 2024 Latte Literature. Niềm vui của bạn là niềm vui của chúng tôi.\n" +
                            "    </div>\n" +
                            "  </div>\n" +
                            "</body>\n" +
                            "</html>";
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
            // Image part
            MimeBodyPart imagePart = new MimeBodyPart();
            DataSource fds = new FileDataSource("src/main/java/com/javaweb/view/imgs/logo/Logo.png");
            imagePart.setDataHandler(new javax.activation.DataHandler(fds));
            imagePart.setHeader("Content-ID", "<logo>");
            imagePart.setFileName("logo.png");
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendEmailAsync(String to, String tempPassword) {
        emailExecutor.submit(() -> sendEmail(to, tempPassword));
    }

    private static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}