package registrant.service;

import shared.model.Profile;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Hợp đồng service trang hồ sơ cá nhân ({@code ProfileServlet}).
 * <p>
 * Nạp {@code Profile} + trạng thái duyệt tài liệu lên request, validate form POST,
 * INSERT/UPDATE bảng {@code Profile} và ghi audit khi cập nhật thành công.
 * CCCD ({@code GovernmentIdNumber}) là khóa nối sang {@code Candidate} về sau.
 */
public interface RegistrantProfileService {
    /** Nạp hồ sơ, trạng thái duyệt và cờ UI lên request cho trang profile. */
    void copyProfileToRequest(UserDTO user, HttpServletRequest request);

    /** Tạo mới hoặc cập nhật Profile; ghi audit nếu thành công. */
    boolean updateProfile(UserDTO user, Profile updated, HttpSession session);

    /** Kiểm tra dữ liệu form hồ sơ; trả message lỗi tiếng Việt hoặc null. */
    String validateProfileUpdate(UserDTO user, Profile updated);

    /** True nếu giấy khám sức khỏe của hồ sơ đang bị từ chối. */
    boolean hasRejectedHealthDocument(int profileId);
}
