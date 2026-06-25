package DAO;

import Models.ManagingStaffApprovalView;
import Models.RegistrantDocumentView;
import java.util.List;
import java.util.Map;

/**
 * Truy cập bảng Document cho luồng thí sinh upload hồ sơ.
 */
public interface DocumentDAO {

    List<RegistrantDocumentView> listByProfileId(int profileId);

    /** Giống listByProfileId nhưng có DocumentId — phục vụ nhật ký theo dõi hồ sơ. */
    List<RegistrantDocumentView> listByProfileIdWithDocumentId(int profileId);

    /** Ghi đè hoặc thêm mới tài liệu theo loại (DocumentType). */
    boolean upsertDocument(int profileId, String documentType, String documentUrl, String notes);

    /** Thêm mới một tài liệu (dùng cho hồ sơ Other — upload nhiều tệp). */
    boolean insertDocument(int profileId, String documentType, String documentUrl, String notes);

    /** Tìm tài liệu thuộc hồ sơ (null nếu không tồn tại hoặc không thuộc profile). */
    RegistrantDocumentView findById(int profileId, int documentId);

    /** Xóa một tài liệu theo DocumentId (chỉ khi thuộc profile). */
    boolean deleteDocument(int profileId, int documentId);

    /** Cập nhật ghi chú/trạng thái duyệt theo loại tài liệu. */
    boolean updateDocumentNotes(int profileId, String documentType, String notes);

    /** Đánh dấu mọi tài liệu đã upload là chờ duyệt (gửi yêu cầu cho ban quản lý). */
    boolean requestApproval(int profileId, String requestNote);

    /** Duyệt hoặc từ chối toàn bộ tài liệu đang chờ duyệt của hồ sơ. */
    boolean reviewProfileDocuments(int profileId, boolean approved, String staffNote);

    /** Danh sách hồ sơ có ít nhất một tài liệu đang chờ duyệt. */
    List<ManagingStaffApprovalView> listPendingApprovals();

    /** Map nhãn hiển thị theo DocumentType. */
    Map<String, String> typeLabels();

    /** Slot mặc định cho UI upload. */
    Map<String, RegistrantDocumentView> defaultDocumentSlots();
}
