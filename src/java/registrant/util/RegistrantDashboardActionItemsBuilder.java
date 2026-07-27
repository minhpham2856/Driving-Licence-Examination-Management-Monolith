package registrant.util;

import registrant.enums.ProfileRegistrationStatus;
import shared.model.Profile;
import registrant.dto.RegistrantDashboardActionItem;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.util.RegistrantProfileSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder panel "Việc cần làm" trên dashboard thí sinh — tầng util, gọi từ RegistrantDashboardServiceImpl.
 * Sinh tối đa 4 RegistrantDashboardActionItem theo trạng thái hồ sơ (ProfileRegistrationStatus), tài liệu, số ca đăng ký và ca sắp tới.
 */
public final class RegistrantDashboardActionItemsBuilder {

    private static final int MAX_ITEMS = 4;

    private RegistrantDashboardActionItemsBuilder() {
    }

    /** Sinh danh sách Việc cần làm tối đa 4 mục theo trạng thái hồ sơ/thi. */
    public static List<RegistrantDashboardActionItem> build(
            Profile profile,
            String registrationStatus,
            List<RegistrantDocumentView> documents,
            int registeredExams,
            int examResults,
            RegistrantRegisteredExamRow upcoming,
            boolean hasCancelledPreferredWithoutActive) {

        List<RegistrantDashboardActionItem> items = new ArrayList<>();
        String status = registrationStatus != null ? registrationStatus.trim() : ProfileRegistrationStatus.DRAFT;

        if (profile == null) {
            add(items, item(
                    "Hoàn thiện hồ sơ cá nhân",
                    "Bạn cần tạo hồ sơ trước khi upload tài liệu và đăng ký thi.",
                    "Tạo hồ sơ",
                    "/registrant/profile",
                    "warning"));
            return items;
        }

        if (RegistrantProfileSupport.isProfileIncomplete(profile)) {
            add(items, item(
                    "Bổ sung thông tin cá nhân",
                    "Họ tên, ngày sinh, SĐT, địa chỉ và số CCCD cần đầy đủ trước khi nộp hồ sơ.",
                    "Cập nhật hồ sơ",
                    "/registrant/profile",
                    "warning"));
        }

        if (ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status)) {
            add(items, item(
                    "Hồ sơ bị từ chối",
                    "Ban quản lý yêu cầu bổ sung tài liệu hoặc sửa thông tin theo ghi chú duyệt.",
                    "Bổ sung hồ sơ",
                    "/registrant/upload-documents",
                    "danger"));
        } else if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            add(items, item(
                    "Hồ sơ đang chờ duyệt",
                    "Theo dõi tiến trình xử lý và phản hồi từ Ban quản lý.",
                    "Theo dõi tiến trình",
                    "/registrant/track-profile",
                    "neutral"));
        } else if (ProfileRegistrationStatus.DRAFT.equalsIgnoreCase(status)
                || ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)) {
            appendDocumentActions(items, documents, status);
        }

        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status) && registeredExams <= 0) {
            if (hasCancelledPreferredWithoutActive) {
                add(items, item(
                        "Nguyện vọng ngày thi đã bị hủy",
                        "Bạn có thể chọn ngày thi dự kiến khác đang mở đăng ký.",
                        "Đăng ký lại",
                        "/registrant/register-exam",
                        "warning"));
            } else {
                add(items, item(
                        "Chưa gửi nguyện vọng ngày thi",
                        "Hồ sơ đã duyệt — chọn ngày thi dự kiến và chờ thông báo lịch chính thức từ trung tâm.",
                        "Đăng ký nguyện vọng",
                        "/registrant/register-exam",
                        "info"));
            }
        }

        if (upcoming != null && upcoming.isPreferredDate()) {
            add(items, item(
                    "Đã gửi nguyện vọng ngày thi",
                    "Trạng thái: chờ thông báo từ phía trung tâm về lịch thi chính thức và số báo danh.",
                    "Xem lịch thi",
                    "/registrant/my-exams",
                    "neutral"));
        } else if (upcoming != null && !upcoming.isSessionTimePublished()) {
            add(items, item(
                    "Chờ cập nhật giờ ca thi",
                    "Ngày thi đã có; giờ ca sẽ hiển thị khi Ban sát hạch mở ca.",
                    "Xem lịch thi",
                    "/registrant/my-exams",
                    "neutral"));
        }

        if (examResults > 0) {
            add(items, item(
                    "Tra cứu bảng điểm",
                    "Kết quả lý thuyết, sa hình hoặc đường trường đã được cập nhật.",
                    "Xem kết quả",
                    "/registrant/my-exams",
                    "success"));
        }

        return items;
    }

    private static void appendDocumentActions(List<RegistrantDashboardActionItem> items,
            List<RegistrantDocumentView> documents, String status) {
        if (!ProfileRegistrationStatus.DRAFT.equalsIgnoreCase(status)) {
            return;
        }
        boolean cccdComplete = RegistrantProfileSupport.isCccdComplete(documents);
        boolean healthUploaded = RegistrantProfileSupport.hasUploadedDocument(documents, "HealthCertificate");
        boolean portraitUploaded = RegistrantProfileSupport.hasUploadedDocument(documents, "Portrait");

        if (!cccdComplete) {
            add(items, item(
                    "Tải ảnh CCCD",
                    "Cần ảnh mặt trước và mặt sau CCCD/CMND để Ban quản lý đối chiếu.",
                    "Upload CCCD",
                    "/registrant/upload-documents",
                    "warning"));
        }
        if (cccdComplete && !portraitUploaded) {
            add(items, item(
                    "Tải ảnh chân dung",
                    "Ảnh 3×4 chuẩn hồ sơ thi - bắt buộc trước khi gửi duyệt.",
                    "Upload ảnh",
                    "/registrant/upload-documents",
                    "warning"));
        }
        if (cccdComplete && portraitUploaded && !healthUploaded) {
            add(items, item(
                    "Tải giấy khám sức khỏe",
                    "Giấy khám sức khỏe lái xe còn hiệu lực theo quy định.",
                    "Upload giấy khám",
                    "/registrant/upload-documents",
                    "warning"));
        }
        if (cccdComplete && portraitUploaded && healthUploaded) {
            add(items, item(
                    "Gửi hồ sơ chờ duyệt",
                    "Tài liệu đã đủ - gửi yêu cầu duyệt từ trang upload hồ sơ.",
                    "Quản lý tài liệu",
                    "/registrant/upload-documents",
                    "info"));
        }
    }

    private static void add(List<RegistrantDashboardActionItem> items, RegistrantDashboardActionItem item) {
        if (items.size() >= MAX_ITEMS || item == null) {
            return;
        }
        items.add(item);
    }

    private static RegistrantDashboardActionItem item(
            String title, String description, String actionLabel, String href, String tone) {
        RegistrantDashboardActionItem item = new RegistrantDashboardActionItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setActionLabel(actionLabel);
        item.setHref(href);
        item.setTone(tone);
        return item;
    }
}
