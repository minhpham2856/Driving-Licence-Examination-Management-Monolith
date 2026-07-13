package registrant.service.impl;

import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.impl.DocumentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.enums.ProfileRegistrationStatus;
import shared.model.Profile;
import registrant.dto.RegistrantDocumentSummary;
import registrant.dto.RegistrantRegisteredExamRow;
import auth.dto.UserDTO;
import registrant.service.RegistrantProfileService;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantProfileSupport;
import registrant.util.RegistrantSessionSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.ZoneId;
import java.util.List;

/** Nạp và cập nhật hồ sơ cá nhân thí sinh từ bảng Profile + User. */
public class RegistrantProfileServiceImpl implements RegistrantProfileService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();

    @Override
    public void copyProfileToRequest(UserDTO user, HttpServletRequest request) {
        request.setAttribute("email", user.getEmail());
        request.setAttribute("accountUsername", user.getUsername());

        var ctx = RegistrantProfileSupport.loadContext(profiledao, documentdao, registrantdao, user);
        if (!ctx.hasProfile()) {
            request.setAttribute("hasProfile", false);
            request.setAttribute("profileIncomplete", true);
            return;
        }

        Profile profile = ctx.getProfile();
        request.setAttribute("hasProfile", true);
        request.setAttribute("registrantName", profile.getFullName());
        if (profile.getDateOfBirth() != null) {
            request.setAttribute("birthday", profile.getDateOfBirth().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate().toString());
        }
        request.setAttribute("gender", profile.isSex() ? "Nữ" : "Nam");
        request.setAttribute("phone", profile.getPhoneNumber());
        request.setAttribute("address", profile.getAddress());
        request.setAttribute("idCardNumber", profile.getGovernmentIdNumber());

        String registrationStatus = ctx.getRegistrationStatus();
        String licenceClass = registrantdao.resolveLatestLicenceClassByProfileId(profile.getProfileId());
        request.setAttribute("licenceClass", licenceClass);
        request.setAttribute("licenceClassDescription", RegistrantExamSupport.licenceClassDescription(licenceClass));

        List<RegistrantRegisteredExamRow> activeExamRegistrations =
                registrantdao.listActiveExamRegistrationsByProfileId(profile.getProfileId(), 20);
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
        request.setAttribute("showHealthAlert", hasRejectedHealthDocument(profile.getProfileId()));

        RegistrantDocumentSummary documentSummary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentdao.typeLabels(), registrationStatus);
        request.setAttribute("documentSummary", documentSummary);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
    }

    @Override
    public String validateProfileUpdate(UserDTO user, Profile updated) {
        if (updated == null || RegistrantProfileSupport.isBlank(updated.getFullName())) {
            return "Họ và tên không được để trống.";
        }
        Profile existing = profiledao.getByUserId(user.getUserId());
        if (existing == null) {
            return validateGovIdForNewProfile(updated);
        }
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(
                registrantdao.findProfileDocumentRegistrationStatus(existing.getProfileId()))) {
            return validateGovIdForNewProfile(updated);
        }
        return null;
    }

    @Override
    public boolean updateProfile(UserDTO user, Profile updated, HttpSession session) {
        if (updated == null || RegistrantProfileSupport.isBlank(updated.getFullName())) {
            return false;
        }

        Profile existing = profiledao.getByUserId(user.getUserId());
        if (existing == null) {
            return insertNewProfile(user, updated, session);
        }
        return updateExistingProfile(user, existing, updated, session);
    }

    @Override
    public boolean hasRejectedHealthDocument(int profileId) {
        return documentdao.listByProfileId(profileId).stream()
                .anyMatch(d -> "HealthCertificate".equals(d.getDocumentType())
                        && "danger".equals(d.getStatusClass()));
    }

    private boolean insertNewProfile(UserDTO user, Profile updated, HttpSession session) {
        Profile created = new Profile();
        created.setUserId(user.getUserId());
        created.setFullName(updated.getFullName().trim());
        created.setDateOfBirth(updated.getDateOfBirth());
        created.setSex(updated.isSex());
        created.setPhoneNumber(trimToNull(updated.getPhoneNumber()));
        created.setAddress(trimToNull(updated.getAddress()));
        created.setGovernmentIdNumber(RegistrantExamSupport.normalizeGovIdNumber(updated.getGovernmentIdNumber()));
        boolean inserted = profiledao.insert(created);
        if (inserted) {
            user.setProfile(created);
            RegistrantSessionSupport.setProfileId(user, created.getProfileId());
            if (session != null) {
                RegistrantAuditHelper.logProfileCreate(session, created.getProfileId());
            }
        }
        return inserted;
    }

    private boolean updateExistingProfile(UserDTO user, Profile existing, Profile updated, HttpSession session) {
        existing.setFullName(updated.getFullName().trim());
        if (updated.getDateOfBirth() != null) {
            existing.setDateOfBirth(updated.getDateOfBirth());
        }
        existing.setSex(updated.isSex());
        existing.setPhoneNumber(mergeOptionalText(existing.getPhoneNumber(), updated.getPhoneNumber()));
        existing.setAddress(mergeOptionalText(existing.getAddress(), updated.getAddress()));

        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(existing.getProfileId());
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            String normalizedGovId = RegistrantExamSupport.normalizeGovIdNumber(updated.getGovernmentIdNumber());
            if (normalizedGovId != null) {
                existing.setGovernmentIdNumber(normalizedGovId);
            }
        }
        boolean ok = profiledao.update(existing);
        if (ok) {
            user.setProfile(existing);
            RegistrantSessionSupport.setProfileId(user, existing.getProfileId());
            if (session != null) {
                RegistrantAuditHelper.logProfileUpdate(session, existing.getProfileId(),
                        "Cập nhật thông tin cá nhân: " + existing.getFullName());
            }
        }
        return ok;
    }

    private static String validateGovIdForNewProfile(Profile updated) {
        if (RegistrantProfileSupport.isBlank(updated.getGovernmentIdNumber())) {
            return "Vui lòng nhập số CCCD / CMND.";
        }
        if (!RegistrantExamSupport.isValidGovIdNumber(updated.getGovernmentIdNumber())) {
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
