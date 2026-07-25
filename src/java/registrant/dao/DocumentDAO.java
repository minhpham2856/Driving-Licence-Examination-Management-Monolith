package registrant.dao;

import registrant.dto.RegistrantDocumentView;
import java.util.List;
import java.util.Map;

/**
 * Truy cập bảng Document cho luồng upload hồ sơ thí sinh.
 * CRUD theo ProfileId + DocumentType; trạng thái duyệt lưu trong Notes (marker #PENDING#, #APPROVED#, #LICENCE#). Dùng bởi upload, profile và track-profile.
 */
public interface DocumentDAO {

    /** Liệt kê tài liệu của hồ sơ. */
    List<RegistrantDocumentView> listByProfileId(int profileId);

    /** Giống listByProfileId nhưng có DocumentId - phục vụ nhật ký theo dõi hồ sơ. */
    List<RegistrantDocumentView> listByProfileIdWithDocumentId(int profileId);

    /** Ghi đè hoặc thêm mới tài liệu theo loại (DocumentType). */
    boolean upsertDocument(int profileId, String documentType, String documentUrl, String notes);

    /** Thêm mới một tài liệu (dùng cho hồ sơ Other - upload nhiều tệp). */
    boolean insertDocument(int profileId, String documentType, String documentUrl, String notes);

    /** Tìm tài liệu thuộc hồ sơ (null nếu không tồn tại hoặc không thuộc profile). */
    RegistrantDocumentView findById(int profileId, int documentId);

    /** Xóa một tài liệu theo DocumentId (chỉ khi thuộc profile). */
    boolean deleteDocument(int profileId, int documentId);

    /** Cập nhật ghi chú/trạng thái duyệt theo loại tài liệu. */
    boolean updateDocumentNotes(int profileId, String documentType, String notes);

    /** Đánh dấu mọi tài liệu đã upload là chờ duyệt (gửi yêu cầu cho ban quản lý). */
    boolean requestApproval(int profileId, String requestNote);

    /**
     * Đồng bộ Document.Notes với trạng thái ExamRegistration bổ sung
     * (sửa lệch khi chỉ cập nhật ER bằng SQL hoặc tệp legacy thiếu #SUPPLEMENT_ER#).
     */
    int reconcileOtherDocumentsWithSupplementEr(int profileId, Map<Integer, String> supplementErStatuses);

    /** Map nhãn hiển thị theo DocumentType. */
    Map<String, String> typeLabels();

    /** Slot mặc định cho UI upload. */
    Map<String, RegistrantDocumentView> defaultDocumentSlots();
}
