package auth.service;

public interface EmailService {

    boolean sendTextEmail(String to, String subject, String content);

    /**
     * Gửi email và trả về lý do cụ thể khi thất bại (vd "Chưa cấu hình MAIL_SENDER_USERNAME/PASSWORD
     * trong .env", "Sai tài khoản/App Password Gmail", "Không kết nối được máy chủ SMTP"...).
     * @return null nếu gửi thành công; mô tả ngắn gọn nếu thất bại.
     * Mặc định (cho các lớp cài đặt cũ chưa override): chỉ báo chung chung dựa theo sendTextEmail().
     */
    default String sendTextEmailAndGetError(String to, String subject, String content) {
        return sendTextEmail(to, subject, content) ? null : "Gửi email thất bại (xem log server để biết chi tiết).";
    }
}
