package service;

import model.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.util.Map;

public interface RegistrantUploadService {

    Map<String, Object> loadUploadPage(User user, HttpServletRequest request);

    /** @return null nếu upload thành công. */
    String handleUpload(User user, String documentType, Part filePart, String reasonNote, HttpServletRequest request);

    /** Upload nhiều tệp hồ sơ khác cùng lúc. @return null nếu thành công. */
    String handleOtherUpload(User user, String reasonNote, String supplementLicenceCode,
            java.util.List<Part> fileParts, HttpServletRequest request);

    /** @return null nếu gửi yêu cầu duyệt thành công. */
    String requestApproval(User user, String requestNote, HttpSession session);

    /** @return null nếu xóa thành công. */
    String deleteDocument(User user, int documentId, HttpServletRequest request);
}
