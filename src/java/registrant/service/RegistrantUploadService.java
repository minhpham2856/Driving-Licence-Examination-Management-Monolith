package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.util.Map;

/**
 * Hợp đồng service upload và gửi duyệt hồ sơ tài liệu (UploadDocumentsServlet).
 * Quản lý 4 loại bắt buộc + Other trên bảng Document (Cloudinary), chuyển ExamRegistration Draft/Rejected → Pending với marker Notes, xóa tài liệu khi trạng thái cho phép. Không tạo Payment/Candidate.
 */
public interface RegistrantUploadService {

    /** Xây model trang upload tài liệu (slot bắt buộc, Other, trạng thái duyệt). */
    Map<String, Object> loadUploadPage(UserDTO user, HttpServletRequest request);

    /** Trả về null nếu upload thành công. */
    String handleUpload(UserDTO user, String documentType, Part filePart, String reasonNote, HttpServletRequest request);

    /** Upload nhiều tệp hồ sơ khác cùng lúc (không gắn hạng — hạng chọn lúc gửi duyệt). Trả về null nếu thành công. */
    String handleOtherUpload(UserDTO user, String reasonNote,
            java.util.List<Part> fileParts, HttpServletRequest request);

    /**
     * Gửi yêu cầu duyệt hồ sơ (chính hoặc bổ sung).
     * approvalLicenceCode: hạng GPLX thí sinh gửi để managing staff duyệt.
     * isRetake: thi lại (true) hoặc thi lần đầu (false) — lưu ExamRegistration.IsRetake.
     * Trả về null nếu thành công.
     */
    String requestApproval(UserDTO user, String requestNote, String approvalLicenceCode, boolean isRetake,
            HttpSession session);

    /** Trả về null nếu xóa thành công. */
    String deleteDocument(UserDTO user, int documentId, HttpServletRequest request);
}
