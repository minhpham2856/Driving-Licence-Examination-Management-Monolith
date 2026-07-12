package auth.service;

public interface EmailService {

    boolean sendTextEmail(String to, String subject, String content);
}
