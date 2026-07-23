package registrant.service.impl;

import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.DocumentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.enums.ProfileRegistrationStatus;
import registrant.dto.RegistrantLicenceOption;
import shared.model.Profile;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantProfileContext;
import auth.dto.UserDTO;
import registrant.service.RegistrantUploadService;
import shared.storage.CloudinaryDocumentStorage;
import registrant.util.DocumentUrlResolver;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantDocumentHelper;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantProfileSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Upload tài liệu hồ sơ thí sinh — đẩy lên Cloudinary (folder pending) để staff xem khi duyệt.
 * RegistrationStatus chỉ đổi khi thí sinh bấm «Gửi yêu cầu duyệt».
 */
public class RegistrantUploadServiceImpl implements RegistrantUploadService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();

    @Override
    public Map<String, Object> loadUploadPage(UserDTO user, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profiledao, documentdao, registrantdao, user);
        if (!ctx.hasProfile()) {
            return model;
        }

        List<RegistrantDocumentView> docs = ctx.getDocuments();
        String registrationStatus = ctx.getRegistrationStatus();

        RegistrantDocumentStatusHelper.applyDocumentLabelsFromRegistrationStatus(docs, registrationStatus);
        DocumentUrlResolver.resolveViewUrls(docs, request);

        Map<String, RegistrantDocumentView> slots = RegistrantDocumentHelper.mergeRequiredDocumentSlots(
                documentdao, docs);
        List<RegistrantDocumentView> otherDocs = RegistrantDocumentHelper.listOtherDocuments(docs);
        DocumentUrlResolver.resolveViewUrls(otherDocs, request);

        boolean profileApproved = ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus);
        boolean primaryPending = ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus);
        boolean profileRejected = ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(registrationStatus);
        boolean supplementPending = RegistrantDocumentStatusHelper.hasAnySupplementPendingReview(
                registrantdao, ctx.getProfileId(), docs);

        model.put("documentList", docs);
        model.put("documentsByType", slots);
        model.put("otherDocuments", otherDocs);
        model.put("otherDocumentCount", otherDocs.size());
        model.put("profileApproved", profileApproved);
        model.put("profileRejected", profileRejected);
        model.put("primaryPendingReview", primaryPending);
        model.put("supplementLicenceOptions", listSupplementLicenceOptions());
        model.put("hasSupplementAwaitingSubmit", RegistrantDocumentStatusHelper.hasSupplementAwaitingSubmit(docs));
        model.put("hasSupplementPendingReview", supplementPending);
        model.put("canRequestApproval", canRequestApproval(registrationStatus, docs, supplementPending));
        model.put("hasPendingReview", primaryPending || supplementPending);
        RegistrantProfileSupport.applySyncToMap(model, ctx.getSyncResult());
        return model;
    }

    @Override
    public String handleOtherUpload(UserDTO user, String reasonNote, String supplementLicenceCode,
            List<Part> fileParts, HttpServletRequest request) {
        if (reasonNote == null || reasonNote.isBlank()) {
            return "Vui lòng nhập lý do / ghi chú cho hồ sơ khác.";
        }
        String licenceError = validateSupplementLicenceCode(supplementLicenceCode);
        if (licenceError != null) {
            return licenceError;
        }
        if (fileParts == null || fileParts.isEmpty()) {
            return "Vui lòng chọn ít nhất một tệp để tải lên.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        String normalizedLicence = supplementLicenceCode.trim();
        for (Part filePart : fileParts) {
            String error = uploadSingleOther(profile.getProfileId(), reasonNote, normalizedLicence, filePart, request);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String handleUpload(UserDTO user, String documentType, Part filePart, String reasonNote,
            HttpServletRequest request) {
        if (!RegistrantDocumentHelper.isAllowedDocumentType(documentType)) {
            return "Loại tài liệu không hợp lệ.";
        }
        if ("Other".equals(documentType)) {
            Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
            if (profile == null) {
                return "Không tìm thấy hồ sơ.";
            }
            String licenceError = validateSupplementLicenceCode(request.getParameter("supplementLicenceCode"));
            if (licenceError != null) {
                return licenceError;
            }
            return uploadSingleOther(profile.getProfileId(), reasonNote,
                    request.getParameter("supplementLicenceCode").trim(), filePart, request);
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profile.getProfileId());
        String statusError = validateMandatoryUploadAllowed(registrationStatus);
        if (statusError != null) {
            return statusError;
        }

        String validationError = RegistrantDocumentHelper.validateMandatoryUpload(filePart, documentType);
        if (validationError != null) {
            return validationError;
        }

        try {
            String submitted = filePart.getSubmittedFileName();
            String ext = RegistrantDocumentHelper.extractExtension(submitted);
            String previousUrl = RegistrantProfileSupport.findStoredUrlForType(
                    documentdao.listByProfileId(profile.getProfileId()), documentType);
            String storedRef = uploadToCloudinary(filePart, profile.getProfileId(), documentType, ext);
            String notes = DocumentDAOImpl.buildUploadNote(documentType, reasonNote, filePart.getSize(), submitted);

            if (!documentdao.upsertDocument(profile.getProfileId(), documentType, storedRef, notes)) {
                return "Không lưu được thông tin tài liệu.";
            }

            deleteReplacedStoredRef(request, previousUrl, storedRef);
            RegistrantAuditHelper.logDocumentUpload(
                    request.getSession(), profile.getProfileId(), documentType, submitted);
            return null;
        } catch (IOException ex) {
            return "Lỗi khi lưu tệp: " + ex.getMessage();
        }
    }

    @Override
    public String requestApproval(UserDTO user, String requestNote, HttpSession session) {
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        List<RegistrantDocumentView> docs = documentdao.listByProfileId(profile.getProfileId());
        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profile.getProfileId());

        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đang chờ duyệt.";
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            if (registrantdao.hasOpenSupplementPending(profile.getProfileId())) {
                return "Hồ sơ bổ sung đang chờ duyệt.";
            }
            if (!RegistrantDocumentStatusHelper.hasSupplementAwaitingSubmit(docs)) {
                return "Chưa có hồ sơ bổ sung mới. Vui lòng thêm tệp tại mục Hồ sơ khác trước khi gửi duyệt.";
            }
            if (!requestSupplementApproval(profile.getProfileId(), requestNote, docs)) {
                return "Không thể gửi yêu cầu duyệt hồ sơ bổ sung. Vui lòng thử lại.";
            }
            RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getProfileId(), requestNote);
            return null;
        } else if (!hasUploadableDocumentsForReview(docs)) {
            return "Chưa có tài liệu nào để gửi duyệt. Vui lòng tải lên ít nhất một tệp.";
        }

        if (!documentdao.requestApproval(profile.getProfileId(), requestNote)) {
            return "Không thể gửi yêu cầu duyệt. Vui lòng thử lại.";
        }

        RegistrantProfileSupport.updateRegistrationStatus(
                profile.getProfileId(), ProfileRegistrationStatus.PENDING, docs, registrantdao);
        RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getProfileId(), requestNote);
        return null;
    }

    /**
     * Tạo dòng ExamRegistration bổ sung + gắn tệp Other — không đổi hồ sơ gốc, không upload lại Cloudinary.
     */
    private boolean requestSupplementApproval(int profileId, String requestNote,
            List<RegistrantDocumentView> docs) {
        List<RegistrantDocumentView> awaiting = new ArrayList<>();
        for (RegistrantDocumentView doc : docs) {
            if (!DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                continue;
            }
            if (doc.getDocumentUrl() == null || doc.getDocumentUrl().isBlank()) {
                continue;
            }
            if (DocumentDAOImpl.isApproved(doc.getNotes())) {
                continue;
            }
            if (DocumentDAOImpl.isPendingReview(doc.getNotes())) {
                Integer linkedEr = RegistrantDocumentHelper.parseSupplementErId(doc.getNotes());
                if (linkedEr != null) {
                    continue;
                }
            }
            awaiting.add(doc);
        }
        if (awaiting.isEmpty()) {
            return false;
        }

        String licenceCode = awaiting.stream()
                .map(RegistrantDocumentView::getSupplementLicenceCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null);
        if (licenceCode == null) {
            for (RegistrantDocumentView doc : awaiting) {
                licenceCode = DocumentDAOImpl.parseLicenceCode(doc.getNotes());
                if (licenceCode != null && !licenceCode.isBlank()) {
                    break;
                }
            }
        }
        int licenceId = licenceCode != null ? registrantdao.resolveLicenceIdByUiCode(licenceCode) : 0;
        if (licenceId <= 0) {
            licenceId = resolveLicenceIdForSupplement(profileId);
        }

        String erMessage = requestNote != null && !requestNote.isBlank()
                ? "Thí sinh gửi duyệt hồ sơ bổ sung. Yêu cầu: " + requestNote.trim()
                : "Thí sinh gửi duyệt hồ sơ bổ sung.";
        int supplementErId = registrantdao.insertSupplementDocumentRegistration(
                profileId, licenceId, ProfileRegistrationStatus.PENDING, erMessage);
        if (supplementErId <= 0) {
            return false;
        }

        boolean updated = false;
        for (RegistrantDocumentView doc : awaiting) {
            String merged = RegistrantDocumentHelper.appendSupplementErMarker(doc.getNotes(), supplementErId);
            merged = mergeSupplementPendingNote(merged, requestNote);
            if (documentdao.updateDocumentNotes(profileId, doc.getDocumentType(), merged)) {
                updated = true;
            }
        }
        return updated;
    }

    private int resolveLicenceIdForSupplement(int profileId) {
        try {
            return registrantdao.resolveLicenceIdByUiCode(
                    registrantdao.resolveLatestLicenceClassByProfileId(profileId));
        } catch (Exception ex) {
            return registrantdao.resolveLicenceIdByUiCode("B2");
        }
    }

    private static String mergeSupplementPendingNote(String existingNotes, String requestNote) {
        String base = existingNotes != null ? existingNotes.trim() : "";
        if (!base.contains(DocumentDAOImpl.MARK_PENDING)) {
            base = (base.isBlank() ? "" : base + " | ")
                    + DocumentDAOImpl.MARK_PENDING + " Gửi yêu cầu duyệt hồ sơ.";
        }
        if (requestNote != null && !requestNote.isBlank()) {
            String suffix = " Yêu cầu: " + requestNote.trim();
            if (!base.contains(suffix.trim())) {
                base = base + suffix;
            }
        }
        return truncateDocumentNotes(base);
    }

    private static String truncateDocumentNotes(String notes) {
        if (notes == null) {
            return null;
        }
        return notes.length() <= 255 ? notes : notes.substring(0, 252) + "...";
    }

    @Override
    public String deleteDocument(UserDTO user, int documentId, HttpServletRequest request) {
        if (documentId <= 0) {
            return "Tài liệu không hợp lệ.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        RegistrantDocumentView doc = documentdao.findById(profile.getProfileId(), documentId);
        if (doc == null) {
            return "Không tìm thấy tài liệu.";
        }

        String deleteError = validateDeleteAllowed(profile.getProfileId(), doc);
        if (deleteError != null) {
            return deleteError;
        }

        DocumentUrlResolver.deleteStoredRef(request.getServletContext(), doc.getDocumentUrl());
        if (!documentdao.deleteDocument(profile.getProfileId(), documentId)) {
            return "Không xóa được tài liệu.";
        }

        RegistrantAuditHelper.logDocumentDelete(
                request.getSession(), profile.getProfileId(), doc.getDocumentType(), doc.getFileName());
        return null;
    }

    private String uploadSingleOther(int profileId, String reasonNote, String supplementLicenceCode,
            Part filePart, HttpServletRequest request) {
        String validationError = RegistrantDocumentHelper.validateOtherUpload(filePart);
        if (validationError != null) {
            return validationError;
        }

        try {
            String submitted = filePart.getSubmittedFileName();
            String ext = RegistrantDocumentHelper.extractExtension(submitted);
            String storageType = DocumentDAOImpl.newOtherDocumentType();
            String storedRef = uploadToCloudinary(filePart, profileId, storageType, ext);
            String notes = DocumentDAOImpl.buildUploadNote(
                    storageType, reasonNote, filePart.getSize(), submitted, supplementLicenceCode);

            if (!documentdao.insertDocument(profileId, storageType, storedRef, notes)) {
                return "Không lưu được thông tin tài liệu.";
            }

            RegistrantAuditHelper.logDocumentUpload(request.getSession(), profileId, storageType, submitted);
            return null;
        } catch (IOException ex) {
            return "Lỗi khi lưu tệp: " + ex.getMessage();
        }
    }

    private String validateSupplementLicenceCode(String supplementLicenceCode) {
        if (supplementLicenceCode == null || supplementLicenceCode.isBlank()) {
            return "Vui lòng chọn hạng bằng mà hồ sơ này bổ sung.";
        }
        String code = supplementLicenceCode.trim();
        if (RegistrantDocumentStatusHelper.isBasicDocsOnlyLicence(code)) {
            return "Hạng A1 và A2 không cần hồ sơ bổ sung — vui lòng chọn hạng từ B1 trở lên.";
        }
        if (registrantdao.resolveLicenceIdByUiCode(code) <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }
        return null;
    }

    private List<RegistrantLicenceOption> listSupplementLicenceOptions() {
        return registrantdao.listOpenLicenceOptions().stream()
                .filter(opt -> opt.getCode() != null
                        && !RegistrantDocumentStatusHelper.isBasicDocsOnlyLicence(opt.getCode()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String validateMandatoryUploadAllowed(String registrationStatus) {
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đã được duyệt. Vui lòng bổ sung qua mục «Hồ sơ khác» bên dưới, "
                    + "sau đó bấm «Gửi yêu cầu duyệt» khi cần ban quản lý xem xét.";
        }
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đang chờ duyệt — không thể thay đổi giấy tờ bắt buộc lúc này.";
        }
        return null;
    }

    private String validateDeleteAllowed(int profileId, RegistrantDocumentView doc) {
        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profileId);
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đang chờ duyệt — không thể xóa tài liệu.";
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            if (!DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                return "Không thể xóa giấy tờ bắt buộc đã được duyệt. Chỉ xóa được hồ sơ bổ sung tại mục «Hồ sơ khác».";
            }
            if (DocumentDAOImpl.isPendingReview(doc.getNotes())) {
                return "Hồ sơ bổ sung đang chờ duyệt — không thể xóa.";
            }
        }
        return null;
    }

    private static String uploadToCloudinary(Part part, int profileId, String docType, String ext)
            throws IOException {
        if (!CloudinaryDocumentStorage.isConfigured()) {
            throw new IOException(
                    "Cloudinary chưa được cấu hình. Thêm CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET vào .env.");
        }
        return CloudinaryDocumentStorage.upload(part, profileId, docType, ext);
    }

    private static void deleteReplacedStoredRef(HttpServletRequest request, String previousUrl, String newUrl) {
        if (previousUrl == null || previousUrl.isBlank() || previousUrl.equals(newUrl)) {
            return;
        }
        DocumentUrlResolver.deleteStoredRef(request.getServletContext(), previousUrl);
    }

    private static boolean hasUploadableDocumentsForReview(List<RegistrantDocumentView> docs) {
        return docs.stream().anyMatch(doc ->
                doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank());
    }

    private static boolean canRequestApproval(String registrationStatus, List<RegistrantDocumentView> docs,
            boolean supplementErPending) {
        String status = registrationStatus != null ? registrationStatus.trim() : ProfileRegistrationStatus.DRAFT;
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return false;
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)) {
            if (supplementErPending) {
                return false;
            }
            return RegistrantDocumentStatusHelper.hasSupplementAwaitingSubmit(docs);
        }
        return hasUploadableDocumentsForReview(docs);
    }
}
