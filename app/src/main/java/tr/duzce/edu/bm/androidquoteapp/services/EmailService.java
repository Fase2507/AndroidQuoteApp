package tr.duzce.edu.bm.androidquoteapp.services;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tr.duzce.edu.bm.androidquoteapp.BuildConfig;

public class EmailService {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = BuildConfig.MAIL_USERNAME;
    private static final String PASSWORD = BuildConfig.MAIL_PASSWORD;

    public static void sendVerificationEmail(String toEmail, String token) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Email Verification - Quote App");
        
        String verificationLink = "quoteapp://verify?email=" + toEmail + "&token=" + token;
        String emailContent = "<h1>Welcome to Quote App!</h1>" +
                "<p>Please click the link below to verify your account:</p>" +
                "<a href=\"" + verificationLink + "\">Verify My Account</a>" +
                "<p>If the link doesn't work, use this token: <b>" + token + "</b></p>";

        message.setContent(emailContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
