package service;

import model.user.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantRegisterExamService {
    void loadRegisterExamPage(User user, HttpServletRequest request);

    /** @return null nếu thành công; ngược lại thông báo lỗi tiếng Việt. */
    String submitRegistration(User user, HttpServletRequest request);

    /** URL GET trang đăng ký, giữ lựa chọn/lọc hiện tại (PRG sau POST). */
    String buildRegisterExamPageUrl(HttpServletRequest request, String fragment);
}
