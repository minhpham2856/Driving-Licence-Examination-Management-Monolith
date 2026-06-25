package Services.Impl;

import DAO.DocumentDAO;
import DAO.ProfileDAO;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.RegistrantDAO;
import DAO.Impl.RegistrantDAOImpl;
import Constants.ProfileRegistrationStatus;
import Models.Profile;
import Models.RegistrantDocumentSummary;
import Models.RegistrantRegisteredExamRow;
import Models.User;
import Services.RegistrantProfileService;
import Utils.RegistrantAuditHelper;
import Utils.RegistrantDocumentStatusHelper;
import Utils.RegistrantExamSupport;
import Utils.RegistrantProfileSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/** Nạp và cập nhật hồ sơ cá nhân thí sinh từ bảng Profile + User. */
public class RegistrantProfileServiceImpl implements RegistrantProfileService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();

    @Override
    public void copyProfileToRequest(User user, HttpServletRequest request) {
        request.setAttribute("email", user.getEmail());
        request.setAttribute("accountUsername", user.getUsername());

        var ctx = RegistrantProfileSupport.loadContext(profileDAO, documentDAO, registrantDAO, user);
        if (!ctx.hasProfile()) {
            request.setAttribute("hasProfile", false);
            request.setAttribute("profileIncomplete", true);
            return;
        }

        Profile profile = ctx.getProfile();
        request.setAttribute("hasProfile", true);
        request.setAttribute("registrantName", profile.getFullName());
        if (profile.getDateOfBirth() != null) {
            request.setAttribute("birthday", profile.getDateOfBirth().toString());
        }
        request.setAttribute("gender", profile.isGender() ? "Nữ" : "Nam");
        request.setAttribute("phone", profile.getPhoneNo());
        request.setAttribute("address", profile.getAddress());
        request.setAttribute("idCardNumber", profile.getGovIdNo());

        String registrationStatus = ctx.getRegistrationStatus();
        String licenceClass = registrantDAO.resolveLatestLicenceClassByProfileId(profile.getId());
        request.setAttribute("licenceClass", licenceClass);
        request.setAttribute("licenceClassDescription", RegistrantExamSupport.licenceClassDescription(licenceClass));

        List<RegistrantRegisteredExamRow> activeExamRegistrations =
                registrantDAO.listActiveExamRegistrationsByProfileId(profile.getId(), 20);
        request.setAttribute("activeExamRegistrations", activeExamRegistrations);
        request.setAttribute("hasActiveExamRegistrations", !activeExamRegistrations.isEmpty());
        request.setAttribute("hasExamLicence", !activeExamRegistrations.isEmpty());
        request.setAttribute("activeLicenceClassesLabel",
                RegistrantProfileSupport.buildActiveLicenceClassesLabel(activeExamRegistrations));

        boolean cccdComplete = RegistrantProfileSupport.isCccdComplete(ctx.getDocuments());
        request.setAttribute("cccdFrontUploaded", RegistrantProfileSupport.hasUploadedDocument(ctx.getDocuments(), "IdFront"));
        request.setAttribute("cccdBackUploaded", RegistrantProfileSupport.hasUploadedDocument(ctx.getDocuments(), "IdBack"));
        request.setAttribute("cccdImagesComplete", cccdComplete);

        boolean profileApproved = ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus);
        request.setAttribute("idCardEditable", !profileApproved);
        request.setAttribute("profileIncomplete", RegistrantProfileSupport.isProfileIncomplete(profile));
        request.setAttribute("showHealthAlert", hasRejectedHealthDocument(profile.getId()));

        RegistrantDocumentSummary documentSummary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentDAO.typeLabels(), registrationStatus);
        request.setAttribute("documentSummary", documentSummary);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
    }

    @Override
    public String validateProfileUpdate(User user, Profile updated) {
        if (updated == null || RegistrantProfileSupport.isBlank(updated.getFullName())) {
            return "Họ và tên không được để trống.";
        }
        Profile existing = profileDAO.getByUserId(user.getId());
        if (existing == null) {
            return validateGovIdForNewProfile(updated);
        }
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(
                registrantDAO.findProfileDocumentRegistrationStatus(existing.getId()))) {
            return validateGovIdForNewProfile(updated);
        }
        return null;
    }

    @Override
    public boolean updateProfile(User user, Profile updated, HttpSession session) {
        if (updated == null || RegistrantProfileSupport.isBlank(updated.getFullName())) {
            return false;
        }

        Profile existing = profileDAO.getByUserId(user.getId());
        if (existing == null) {
            return insertNewProfile(user, updated, session);
        }
        return updateExistingProfile(user, existing, updated, session);
    }

    @Override
    public boolean hasRejectedHealthDocument(int profileId) {
        return documentDAO.listByProfileId(profileId).stream()
                .anyMatch(d -> "HealthCertificate".equals(d.getDocumentType())
                        && "danger".equals(d.getStatusClass()));
    }

    private boolean insertNewProfile(User user, Profile updated, HttpSession session) {
        Profile created = new Profile();
        created.setUserId(user.getId());
        created.setFullName(updated.getFullName().trim());
        created.setDateOfBirth(updated.getDateOfBirth());
        created.setGender(updated.isGender());
        created.setPhoneNo(trimToNull(updated.getPhoneNo()));
        created.setAddress(trimToNull(updated.getAddress()));
        created.setGovIdNo(RegistrantExamSupport.normalizeGovIdNumber(updated.getGovIdNo()));
        boolean inserted = profileDAO.insert(created);
        if (inserted) {
            user.setProfile(created);
            user.setProfileId(created.getId());
            if (session != null) {
                RegistrantAuditHelper.logProfileCreate(session, created.getId());
            }
        }
        return inserted;
    }

    private boolean updateExistingProfile(User user, Profile existing, Profile updated, HttpSession session) {
        existing.setFullName(updated.getFullName().trim());
        if (updated.getDateOfBirth() != null) {
            existing.setDateOfBirth(updated.getDateOfBirth());
        }
        existing.setGender(updated.isGender());
        existing.setPhoneNo(mergeOptionalText(existing.getPhoneNo(), updated.getPhoneNo()));
        existing.setAddress(mergeOptionalText(existing.getAddress(), updated.getAddress()));

        String registrationStatus = registrantDAO.findProfileDocumentRegistrationStatus(existing.getId());
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            String normalizedGovId = RegistrantExamSupport.normalizeGovIdNumber(updated.getGovIdNo());
            if (normalizedGovId != null) {
                existing.setGovIdNo(normalizedGovId);
            }
        }
        boolean ok = profileDAO.update(existing);
        if (ok) {
            user.setProfile(existing);
            user.setProfileId(existing.getId());
            if (session != null) {
                RegistrantAuditHelper.logProfileUpdate(session, existing.getId(),
                        "Cập nhật thông tin cá nhân: " + existing.getFullName());
            }
        }
        return ok;
    }

    private static String validateGovIdForNewProfile(Profile updated) {
        if (RegistrantProfileSupport.isBlank(updated.getGovIdNo())) {
            return "Vui lòng nhập số CCCD / CMND.";
        }
        if (!RegistrantExamSupport.isValidGovIdNumber(updated.getGovIdNo())) {
            return "Số CCCD / CMND không hợp lệ (CCCD 12 số hoặc CMND 9 số).";
        }
        return null;
    }

    private static String mergeOptionalText(String existingValue, String submittedValue) {
        if (submittedValue == null || submittedValue.isBlank()) {
            return existingValue;
        }
        return submittedValue.trim();
    }

    private static String trimToNull(String value) {
        return RegistrantProfileSupport.isBlank(value) ? null : value.trim();
    }
}
