package registrant.util;

import registrant.enums.ProfileRegistrationStatus;
import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dto.RegistrantProfileContext;
import shared.model.Profile;
import registrant.dto.ProfileRegistrationSyncResult;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantRegisteredExamRow;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper nạp hồ sơ thí sinh dùng chung giữa các service/controller cổng Registrant.
 * Resolve Profile từ session, liệt kê Document, đồng bộ ExamRegistration.RegistrationStatus, build RegistrantProfileContext và gắn attribute lên request cho JSP.
 */
public final class RegistrantProfileSupport {

    private static final Logger LOG = Logger.getLogger(RegistrantProfileSupport.class.getName());

    private RegistrantProfileSupport() {
    }

    /** Lấy Profile theo user và gắn lại vào session DTO. */
    public static Profile resolveProfile(ProfileDAO profiledao, UserDTO user) {
        if (user == null) {
            return null;
        }
        if (user.getProfile() != null && user.getProfile().getProfileId() > 0) {
            return user.getProfile();
        }
        Profile profile = profiledao.getByUserId(user.getUserId());
        if (profile != null) {
            RegistrantSessionSupport.attachProfile(user, profile);
        }
        return profile;
    }

    /** Nạp Profile + documents + sync RegistrationStatus. */
    public static RegistrantProfileContext loadContext(ProfileDAO profiledao, DocumentDAO documentdao,
            RegistrantDAO registrantdao, UserDTO user) {
        Profile profile = resolveProfile(profiledao, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        List<RegistrantDocumentView> docs = documentdao.listByProfileId(profile.getProfileId());
        Map<Integer, String> supplementErStatuses = registrantdao.mapSupplementRegistrationStatuses(profile.getProfileId());
        if (!supplementErStatuses.isEmpty()) {
            documentdao.reconcileOtherDocumentsWithSupplementEr(profile.getProfileId(), supplementErStatuses);
            docs = documentdao.listByProfileId(profile.getProfileId());
        }
        ProfileRegistrationSyncResult sync = loadRegistrationSync(profile.getProfileId(), registrantdao);
        return new RegistrantProfileContext(profile, docs, sync);
    }

    /** Như loadContext nhưng bỏ qua danh sách tài liệu. */
    public static RegistrantProfileContext loadContextWithoutDocuments(ProfileDAO profiledao,
            RegistrantDAO registrantdao, UserDTO user) {
        Profile profile = resolveProfile(profiledao, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        ProfileRegistrationSyncResult sync = loadRegistrationSync(profile.getProfileId(), registrantdao);
        return new RegistrantProfileContext(profile, List.of(), sync);
    }

    /** Gắn nhãn/class trạng thái hồ sơ lên request. */
    public static void applyRegistrationStatus(HttpServletRequest request, String registrationStatus) {
        request.setAttribute("profileRegistrationStatus", registrationStatus);
        request.setAttribute("profileRegistrationStatusLabel",
                ProfileRegistrationStatus.toDisplayLabel(registrationStatus));
        request.setAttribute("profileRegistrationStatusClass",
                ProfileRegistrationStatus.toBadgeClass(registrationStatus));
    }

    /** Gắn nhãn/class trạng thái hồ sơ vào map model. */
    public static void applyRegistrationStatus(Map<String, Object> model, String registrationStatus) {
        model.put("profileRegistrationStatus", registrationStatus);
        model.put("profileRegistrationStatusLabel",
                ProfileRegistrationStatus.toDisplayLabel(registrationStatus));
        model.put("profileRegistrationStatusClass",
                ProfileRegistrationStatus.toBadgeClass(registrationStatus));
    }

    /** Đẩy kết quả sync RegistrationStatus lên request. */
    public static void applySyncToRequest(HttpServletRequest request, ProfileRegistrationSyncResult sync) {
        if (sync == null) {
            applyRegistrationStatus(request, ProfileRegistrationStatus.DRAFT);
            return;
        }
        applyRegistrationStatus(request, sync.getExpectedStatus());
        request.setAttribute("registrationStatusAligned", sync.isAligned());
        request.setAttribute("registrationUserNotice", sync.getUserNotice());
    }

    /** Đẩy kết quả sync RegistrationStatus vào model. */
    public static void applySyncToMap(Map<String, Object> model, ProfileRegistrationSyncResult sync) {
        if (sync == null) {
            applyRegistrationStatus(model, ProfileRegistrationStatus.DRAFT);
            return;
        }
        applyRegistrationStatus(model, sync.getExpectedStatus());
        model.put("registrationStatusAligned", sync.isAligned());
        model.put("registrationUserNotice", sync.getUserNotice());
    }

    /** True nếu đã có URL upload cho DocumentType. */
    public static boolean hasUploadedDocument(List<RegistrantDocumentView> docs, String documentType) {
        if (docs == null || documentType == null) {
            return false;
        }
        for (RegistrantDocumentView doc : docs) {
            if (documentType.equals(doc.getDocumentType())) {
                String url = doc.getDocumentUrl();
                return url != null && !url.isBlank();
            }
        }
        return false;
    }

    /** True nếu đã tải đủ mặt trước và mặt sau CCCD. */
    public static boolean isCccdComplete(List<RegistrantDocumentView> docs) {
        return hasUploadedDocument(docs, "IdFront") && hasUploadedDocument(docs, "IdBack");
    }

    /** True nếu thiếu họ tên/SĐT/địa chỉ/CCCD trên Profile. */
    public static boolean isProfileIncomplete(Profile profile) {
        if (profile == null) {
            return true;
        }
        return isBlank(profile.getFullName())
                || profile.getDateOfBirth() == null
                || isBlank(profile.getPhoneNumber())
                || isBlank(profile.getAddress())
                || isBlank(profile.getGovernmentIdNumber());
    }

    /** Ghép danh sách hạng đang đăng ký thành chuỗi hiển thị. */
    public static String buildActiveLicenceClassesLabel(List<RegistrantRegisteredExamRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Set<String> codes = new LinkedHashSet<>();
        for (RegistrantRegisteredExamRow row : rows) {
            if (row.getLicenceClass() != null && !row.getLicenceClass().isBlank()) {
                codes.add(row.getLicenceClass());
            }
        }
        if (codes.isEmpty()) {
            return null;
        }
        return String.join(", ", codes);
    }

    /** Tên hiển thị thân thiện (họ tên / username / "bạn"). */
    public static String displayName(UserDTO user, Profile profile) {
        if (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()) {
            return profile.getFullName().trim();
        }
        if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return "bạn";
    }

    /** Tìm DocumentUrl đã lưu theo DocumentType. */
    public static String findStoredUrlForType(List<RegistrantDocumentView> docs, String documentType) {
        if (docs == null || documentType == null || registrant.dao.impl.DocumentDAOImpl.isOtherType(documentType)) {
            return null;
        }
        for (RegistrantDocumentView doc : docs) {
            if (documentType.equals(doc.getDocumentType())) {
                return doc.getDocumentUrl();
            }
        }
        return null;
    }

    /** True nếu chuỗi null hoặc blank. */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Đọc RegistrationStatus hồ sơ gốc - không ghi đè DB. */
    public static ProfileRegistrationSyncResult loadRegistrationSync(int profileId,
            RegistrantDAO registrantdao) {
        String status = registrantdao.findProfileDocumentRegistrationStatus(profileId);
        ProfileRegistrationSyncResult result = new ProfileRegistrationSyncResult();
        result.setExpectedStatus(status);
        result.setActualStatus(status);
        result.setAligned(true);
        result.setUpdated(false);
        return result;
    }

    /** Ghi RegistrationStatus + Notes mô tả lên dòng hồ sơ gốc (#PROFILE_DOC#). */
    public static boolean updateRegistrationStatus(int profileId, String status,
            List<RegistrantDocumentView> docs, RegistrantDAO registrantdao) {
        return updateRegistrationStatus(profileId, status, docs, registrantdao, 0);
    }

    /** Như trên, kèm hạng GPLX thí sinh chọn khi gửi duyệt. */
    public static boolean updateRegistrationStatus(int profileId, String status,
            List<RegistrantDocumentView> docs, RegistrantDAO registrantdao, int licenceId) {
        if (profileId <= 0 || status == null || status.isBlank()) {
            return false;
        }
        String notes = deriveRegistrationNotes(docs, status.trim());
        boolean written = licenceId > 0
                ? registrantdao.syncProfileDocumentRegistration(profileId, status.trim(), notes, licenceId)
                : registrantdao.syncProfileDocumentRegistration(profileId, status.trim(), notes);
        if (!written) {
            LOG.log(Level.WARNING, "Không cập nhật RegistrationStatus profile {0} → {1}",
                    new Object[] { profileId, status });
        }
        return written;
    }

    /** Sinh Notes mô tả theo status và số giấy tờ đã tải. */
    public static String deriveRegistrationNotes(List<RegistrantDocumentView> allDocs, String status) {
        Map<String, RegistrantDocumentView> slots = RegistrantDocumentHelper.buildRequiredSlots(allDocs);
        int uploaded = RegistrantDocumentHelper.countUploadedRequired(slots);
        int requiredTotal = RegistrantDocumentStatusHelper.REQUIRED_TYPES.length;

        return switch (status) {
            case ProfileRegistrationStatus.APPROVED -> String.format(
                    "Ban quản lý đã duyệt hồ sơ (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);
            case ProfileRegistrationStatus.PENDING -> String.format(
                    "Thí sinh đã gửi yêu cầu duyệt (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);
            case ProfileRegistrationStatus.REJECTED -> "Có giấy tờ bị từ chối - vui lòng bổ sung và gửi duyệt lại.";
            case ProfileRegistrationStatus.DRAFT -> uploaded > 0
                    ? String.format("Đang bổ sung hồ sơ (%d/%d giấy tờ bắt buộc).", uploaded, requiredTotal)
                    : "Chưa tải giấy tờ bắt buộc.";
            default -> "";
        };
    }
}
