<<<<<<<< Updated upstream:src/java/util/registrant/RegistrantProfileSupport.java
package util.registrant;

import enums.registrant.ProfileRegistrationStatus;
import dao.DocumentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import dto.registrant.RegistrantProfileContext;
import model.user.Profile;
import dto.registrant.ProfileRegistrationSyncResult;
import dto.registrant.RegistrantDocumentView;
import dto.registrant.RegistrantRegisteredExamRow;
========
package util;

import constant.ProfileRegistrationStatus;
import dao.DocumentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import model.registrant.RegistrantProfileContext;
import model.user.Profile;
import model.registrant.ProfileRegistrationSyncResult;
import model.registrant.RegistrantDocumentView;
import model.registrant.RegistrantRegisteredExamRow;
>>>>>>>> Stashed changes:src/java/util/RegistrantProfileSupport.java
import model.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Helper dùng chung: nạp profile, tài liệu, trạng thái duyệt cho cổng thí sinh. */
public final class RegistrantProfileSupport {

    private static final Logger LOG = Logger.getLogger(RegistrantProfileSupport.class.getName());

    private RegistrantProfileSupport() {
    }

    public static Profile resolveProfile(ProfileDAO profiledao, User user) {
        if (user == null) {
            return null;
        }
        if (user.getProfile() != null && user.getProfile().getId() > 0) {
            return user.getProfile();
        }
        Profile profile = profiledao.getByUserId(user.getId());
        if (profile != null) {
            user.setProfile(profile);
            user.setProfileId(profile.getId());
        }
        return profile;
    }

    public static RegistrantProfileContext loadContext(ProfileDAO profiledao, DocumentDAO documentdao,
            RegistrantDAO registrantdao, User user) {
        Profile profile = resolveProfile(profiledao, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        List<RegistrantDocumentView> docs = documentdao.listByProfileId(profile.getId());
        Map<Integer, String> supplementErStatuses = registrantdao.mapSupplementRegistrationStatuses(profile.getId());
        if (!supplementErStatuses.isEmpty()) {
            documentdao.reconcileOtherDocumentsWithSupplementEr(profile.getId(), supplementErStatuses);
            docs = documentdao.listByProfileId(profile.getId());
        }
        ProfileRegistrationSyncResult sync = loadRegistrationSync(profile.getId(), registrantdao);
        return new RegistrantProfileContext(profile, docs, sync);
    }

    public static RegistrantProfileContext loadContextWithoutDocuments(ProfileDAO profiledao,
            RegistrantDAO registrantdao, User user) {
        Profile profile = resolveProfile(profiledao, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        ProfileRegistrationSyncResult sync = loadRegistrationSync(profile.getId(), registrantdao);
        return new RegistrantProfileContext(profile, List.of(), sync);
    }

    public static void applyRegistrationStatus(HttpServletRequest request, String registrationStatus) {
        request.setAttribute("profileRegistrationStatus", registrationStatus);
        request.setAttribute("profileRegistrationStatusLabel",
                ProfileRegistrationStatus.toDisplayLabel(registrationStatus));
        request.setAttribute("profileRegistrationStatusClass",
                ProfileRegistrationStatus.toBadgeClass(registrationStatus));
    }

    public static void applyRegistrationStatus(Map<String, Object> model, String registrationStatus) {
        model.put("profileRegistrationStatus", registrationStatus);
        model.put("profileRegistrationStatusLabel",
                ProfileRegistrationStatus.toDisplayLabel(registrationStatus));
        model.put("profileRegistrationStatusClass",
                ProfileRegistrationStatus.toBadgeClass(registrationStatus));
    }

    public static void applySyncToRequest(HttpServletRequest request, ProfileRegistrationSyncResult sync) {
        if (sync == null) {
            applyRegistrationStatus(request, ProfileRegistrationStatus.DRAFT);
            return;
        }
        applyRegistrationStatus(request, sync.getExpectedStatus());
        request.setAttribute("registrationStatusAligned", sync.isAligned());
        request.setAttribute("registrationUserNotice", sync.getUserNotice());
    }

    public static void applySyncToMap(Map<String, Object> model, ProfileRegistrationSyncResult sync) {
        if (sync == null) {
            applyRegistrationStatus(model, ProfileRegistrationStatus.DRAFT);
            return;
        }
        applyRegistrationStatus(model, sync.getExpectedStatus());
        model.put("registrationStatusAligned", sync.isAligned());
        model.put("registrationUserNotice", sync.getUserNotice());
    }

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

    public static boolean isCccdComplete(List<RegistrantDocumentView> docs) {
        return hasUploadedDocument(docs, "IdFront") && hasUploadedDocument(docs, "IdBack");
    }

    public static boolean isProfileIncomplete(Profile profile) {
        if (profile == null) {
            return true;
        }
        return isBlank(profile.getFullName())
                || profile.getDateOfBirth() == null
                || isBlank(profile.getPhoneNo())
                || isBlank(profile.getAddress())
                || isBlank(profile.getGovIdNo());
    }

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

    public static String displayName(User user, Profile profile) {
        if (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()) {
            return profile.getFullName().trim();
        }
        if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return "bạn";
    }

    public static String findStoredUrlForType(List<RegistrantDocumentView> docs, String documentType) {
        if (docs == null || documentType == null || dao.impl.DocumentDAOImpl.isOtherType(documentType)) {
            return null;
        }
        for (RegistrantDocumentView doc : docs) {
            if (documentType.equals(doc.getDocumentType())) {
                return doc.getDocumentUrl();
            }
        }
        return null;
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Đọc RegistrationStatus hồ sơ gốc — không ghi đè DB. */
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

    /** Ghi RegistrationStatus + Notes mô tả lên dòng hồ sơ gốc ({@code #PROFILE_DOC#}). */
    public static boolean updateRegistrationStatus(int profileId, String status,
            List<RegistrantDocumentView> docs, RegistrantDAO registrantdao) {
        if (profileId <= 0 || status == null || status.isBlank()) {
            return false;
        }
        String notes = deriveRegistrationNotes(docs, status.trim());
        boolean written = registrantdao.syncProfileDocumentRegistration(profileId, status.trim(), notes);
        if (!written) {
            LOG.log(Level.WARNING, "Không cập nhật RegistrationStatus profile {0} → {1}",
                    new Object[] { profileId, status });
        }
        return written;
    }

    public static String deriveRegistrationNotes(List<RegistrantDocumentView> allDocs, String status) {
        Map<String, RegistrantDocumentView> slots = RegistrantDocumentHelper.buildRequiredSlots(allDocs);
        int uploaded = RegistrantDocumentHelper.countUploadedRequired(slots);
        int requiredTotal = RegistrantDocumentStatusHelper.REQUIRED_TYPES.length;

        return switch (status) {
            case ProfileRegistrationStatus.APPROVED -> String.format(
                    "Ban quản lý đã duyệt hồ sơ (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);
            case ProfileRegistrationStatus.PENDING -> String.format(
                    "Thí sinh đã gửi yêu cầu duyệt (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);
            case ProfileRegistrationStatus.REJECTED -> "Có giấy tờ bị từ chối — vui lòng bổ sung và gửi duyệt lại.";
            case ProfileRegistrationStatus.DRAFT -> uploaded > 0
                    ? String.format("Đang bổ sung hồ sơ (%d/%d giấy tờ bắt buộc).", uploaded, requiredTotal)
                    : "Chưa tải giấy tờ bắt buộc.";
            default -> "";
        };
    }
}
