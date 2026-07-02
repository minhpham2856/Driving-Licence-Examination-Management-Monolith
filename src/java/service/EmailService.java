package service;
public interface EmailService {
    boolean isConfigured();
    boolean sendTextEmail(String to, String subject, String content);
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
}
