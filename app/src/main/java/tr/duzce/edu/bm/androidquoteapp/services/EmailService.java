package tr.duzce.edu.bm.androidquoteapp.services;

import android.util.Log;
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
    private static final String TAG = "EmailService";
    private static final String HOST = "smtp.gmail.com";
    
    // Port 465 (SSL) genellikle 587'den (STARTTLS) çok daha kararlıdır.
    private static final String PORT = "465"; 
    
    private static final String USERNAME = BuildConfig.MAIL_USERNAME;
    private static final String PASSWORD = BuildConfig.MAIL_PASSWORD;

    public static void sendVerificationEmail(String toEmail, String verificationCode) throws MessagingException {
        if (USERNAME == null || USERNAME.isEmpty() || PASSWORD == null || PASSWORD.isEmpty()) {
            Log.e(TAG, "Email credentials are missing in local.properties!");
            throw new MessagingException("Email credentials are not configured.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        
        // Port 465 için SSL Yapılandırması (Kritik)
        props.put("mail.smtp.socketFactory.port", PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.enable", "true");
        
        // Zaman aşımı sürelerini artır (15 saniye)
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        
        // Güvenlik ayarları
        props.put("mail.smtp.ssl.trust", HOST);

        // Debug Modu: Logcat'te tüm SMTP trafiğini görmek için
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Email Verification - Quote App");
            
            String emailContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;\">" +
                    "<h1>Welcome to Quote App!</h1>" +
                    "<p>Thank you for registering. To verify your account, please use this code:</p>" +
                    "<h2 style=\"color: #2196F3; font-size: 32px; letter-spacing: 5px; background: #f4f4f4; padding: 15px; display: inline-block; border-radius: 5px;\">" + 
                    verificationCode + "</h2>" +
                    "</div>";

            message.setContent(emailContent, "text/html; charset=utf-8");

            Log.d(TAG, "Sending email via Port 465 (SSL)...");
            Transport.send(message);
            Log.d(TAG, "Email successfully sent.");
            
        } catch (MessagingException e) {
            Log.e(TAG, "SMTP Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
