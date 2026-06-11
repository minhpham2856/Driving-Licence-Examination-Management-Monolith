package Services.Impl;

import Services.EmailService;
import Utils.ConfigManager;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private Properties props;
    private String senderUsername;
    private String senderPassword;

    public EmailServiceImpl() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        props = new Properties();
        props.put("mail.smtp.host", ConfigManager.get("MAIL_SMTP_HOST", "smtp.gmail.com"));
        props.put("mail.smtp.port", ConfigManager.get("MAIL_SMTP_PORT", "587"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", ConfigManager.get("MAIL_SMTP_HOST", "smtp.gmail.com"));

        senderUsername = ConfigManager.get("MAIL_SENDER_USERNAME");
        senderPassword = ConfigManager.get("MAIL_SENDER_PASSWORD");
    }

    @Override
    public boolean sendTextEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, false);
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        return sendEmail(to, subject, htmlContent, true);
    }

    private boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        if (senderUsername == null || senderPassword == null) {
            return false;
        }

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderUsername, senderPassword);
            }
        };

        Session session = Session.getInstance(props, auth);
        try {
            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(senderUsername));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject, "UTF-8");

            if (isHtml) {
                msg.setContent(body, "text/html; charset=UTF-8");
            } else {
                msg.setText(body, "UTF-8");
            }

            Transport.send(msg);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
