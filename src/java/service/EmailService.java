package service;

public interface EmailService {

    boolean isConfigured();

    /**
     * Sends a plain text email to the recipient.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param content The message content.
     * @return true if email was sent successfully, else false.
     */
    boolean sendTextEmail(String to, String subject, String content);

    /**
     * Sends an HTML email to the recipient.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param htmlContent The HTML formatted message content.
     * @return true if the email was sent successfully, false otherwise.
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
}
