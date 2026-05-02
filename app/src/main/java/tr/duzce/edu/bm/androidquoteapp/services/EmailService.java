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

    /**
     * Sends a verification email containing a 6-digit code.
     * Unused deep link logic has been removed.
     */
    public static void sendVerificationEmail(String toEmail, String verificationCode) throws MessagingException {
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
        
        // Clean HTML content showing only the verification code
        String emailContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;\">" +
                "<h1>Welcome to Quote App!</h1>" +
                "<p>Thank you for registering. To verify your account, please enter the following code in the app:</p>" +
                "<h2 style=\"color: #2196F3; font-size: 32px; letter-spacing: 5px; background: #f4f4f4; padding: 10px; display: inline-block; border-radius: 5px;\">" + 
                verificationCode + "</h2>" +
                "<p>Enter this code in the verification screen to activate your account.</p>" +
                "</div>";

        message.setContent(emailContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
