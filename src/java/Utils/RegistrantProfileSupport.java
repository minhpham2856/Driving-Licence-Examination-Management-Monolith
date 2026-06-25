package Utils;

import Constants.ProfileRegistrationStatus;
import DAO.DocumentDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import Models.RegistrantProfileContext;
import Models.Profile;
import Models.ProfileRegistrationSyncResult;
import Models.RegistrantDocumentView;
import Models.RegistrantRegisteredExamRow;
import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Helper dùng chung: nạp profile, tài liệu, trạng thái duyệt cho cổng thí sinh. */
public final class RegistrantProfileSupport {

    private RegistrantProfileSupport() {
    }

    public static Profile resolveProfile(ProfileDAO profileDAO, User user) {
        if (user == null) {
            return null;
        }
        if (user.getProfile() != null && user.getProfile().getId() > 0) {
            return user.getProfile();
        }
        Profile profile = profileDAO.getByUserId(user.getId());
        if (profile != null) {
            user.setProfile(profile);
            user.setProfileId(profile.getId());
        }
        return profile;
    }

    public static RegistrantProfileContext loadContext(ProfileDAO profileDAO, DocumentDAO documentDAO,
            RegistrantDAO registrantDAO, User user) {
        Profile profile = resolveProfile(profileDAO, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        List<RegistrantDocumentView> docs = documentDAO.listByProfileId(profile.getId());
        ProfileRegistrationSyncResult sync = RegistrantProfileRegistrationSync.load(
                profile.getId(), registrantDAO);
        return new RegistrantProfileContext(profile, docs, sync);
    }

    public static RegistrantProfileContext loadContextWithoutDocuments(ProfileDAO profileDAO,
            RegistrantDAO registrantDAO, User user) {
        Profile profile = resolveProfile(profileDAO, user);
        if (profile == null) {
            return new RegistrantProfileContext(null, List.of(), null);
        }
        ProfileRegistrationSyncResult sync = RegistrantProfileRegistrationSync.load(
                profile.getId(), registrantDAO);
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
        if (docs == null || documentType == null || DAO.Impl.DocumentDAOImpl.isOtherType(documentType)) {
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
}
