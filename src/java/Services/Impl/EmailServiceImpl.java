package Services.Impl;

import Services.EmailService;
import Utils.ConfigManager;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataHandler;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailServiceImpl implements EmailService {

    private static final Logger LOG = Logger.getLogger(EmailServiceImpl.class.getName());
    private static final String MAIL_HOST = ConfigManager.get("MAIL_SMTP_HOST", "smtp.gmail.com");
    private static final String MAIL_PORT = ConfigManager.get("MAIL_SMTP_PORT", "587");
    private static final String MAIL_USERNAME = ConfigManager.get("MAIL_SENDER_USERNAME");
    private static final String MAIL_PASSWORD = ConfigManager.get("MAIL_SENDER_PASSWORD");

    private Properties props;
    private String senderUsername;
    private String senderPassword;

    public EmailServiceImpl() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        props = new Properties();
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", MAIL_HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        senderUsername = normalize(MAIL_USERNAME);
        senderPassword = normalizeAppPassword(MAIL_PASSWORD);
    }

    @Override
    public boolean isConfigured() {
        return senderUsername != null && !senderUsername.isEmpty()
                && senderPassword != null && !senderPassword.isEmpty();
    }

    @Override
    public boolean sendTextEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, false);
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        return sendEmail(to, subject, htmlContent, true);
    }

    @Override
    public boolean sendHtmlEmailWithAttachment(String to, String subject, String htmlContent,
            byte[] attachment, String attachmentName, String attachmentContentType) {
        loadConfiguration();
        if (!isConfigured() || to == null || to.isBlank()) {
            LOG.warning("Email with attachment skipped: SMTP or recipient is not configured.");
            return false;
        }
        if (attachment == null || attachment.length == 0) {
            LOG.warning("Email with attachment skipped: attachment is empty.");
            return false;
        }

        Session session = createSession();
        try {
            MimeMessage msg = baseMessage(session, to, subject);
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(htmlContent, "text/html; charset=UTF-8");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            String contentType = attachmentContentType == null || attachmentContentType.isBlank()
                    ? "application/octet-stream" : attachmentContentType;
            attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment, contentType)));
            attachmentPart.setFileName(MimeUtility.encodeText(
                    attachmentName == null ? "attachment" : attachmentName, "UTF-8", null));
            attachmentPart.setDisposition(Part.ATTACHMENT);

            MimeMultipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(bodyPart);
            multipart.addBodyPart(attachmentPart);
            msg.setContent(multipart);
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send attachment email to " + to + ": " + e.getMessage(), e);
            return false;
        }
    }

    private boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        loadConfiguration();

        if (!isConfigured()) {
            LOG.warning("Email skipped: MAIL_SENDER_USERNAME or MAIL_SENDER_PASSWORD is not configured.");
            return false;
        }

        if (to == null || to.isBlank()) {
            LOG.warning("Email skipped: recipient address is empty.");
            return false;
        }

        Session session = createSession();
        try {
            MimeMessage msg = baseMessage(session, to, subject);

            if (isHtml) {
                msg.setContent(body, "text/html; charset=UTF-8");
            } else {
                msg.setText(body, "UTF-8");
            }

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send email to " + to + ": " + e.getMessage(), e);
            return false;
        }
    }

    private Session createSession() {
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderUsername, senderPassword);
            }
        };
        return Session.getInstance(props, auth);
    }

    private MimeMessage baseMessage(Session session, String to, String subject) throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderUsername));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.trim()));
        msg.setSubject(subject, "UTF-8");
        return msg;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeAppPassword(String value) {
        if (value == null) {
            return null;
        }
        return value.replace(" ", "").trim();
    }
}
