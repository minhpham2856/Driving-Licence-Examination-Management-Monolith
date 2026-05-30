package Services.Impl;

import Services.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());

    private Properties smtpProperties;
    private String senderUsername;
    private String senderPassword;

    public EmailServiceImpl() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        smtpProperties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Could not find email.properties");
                return;
            }

            Properties props = new Properties();
            props.load(input);

            smtpProperties.put("mail.smtp.host", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
            smtpProperties.put("mail.smtp.port", props.getProperty("mail.smtp.port", "587"));
            smtpProperties.put("mail.smtp.auth", props.getProperty("mail.smtp.auth", "true"));
            smtpProperties.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable", "true"));

            // get credentials
            senderUsername = props.getProperty("mail.sender.username");
            senderPassword = props.getProperty("mail.sender.password");

            LOGGER.log(Level.INFO, "Email properties loaded successfully.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load email properties.", e);
        }
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
            LOGGER.log(Level.SEVERE, "Sending failed: SMTP credentials not configured.");
            return false;
        }

        // create authenticator
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderUsername, senderPassword);
            }
        };

        // create authenticated session
        Session session = Session.getInstance(smtpProperties, auth);

        try {
            // create a new msg
            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(senderUsername));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject, "UTF-8");

            // set header for each type of email
            if (isHtml) {
                msg.setContent(body, "text/html; charset=UTF-8");
            } else {
                msg.setText(body, "UTF-8");
            }

            // send email
            LOGGER.log(Level.INFO, "Attempting to send email to {0}...", to);
            Transport.send(msg);

            LOGGER.log(Level.INFO, "Email successfully sent to {0} (Subject: {1})", new Object[]{to, subject});
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception encountered while sending email to " + to, e);
            return false;
        }
    }
}
