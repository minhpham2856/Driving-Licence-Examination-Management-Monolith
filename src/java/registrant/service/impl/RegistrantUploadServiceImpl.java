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
import registrant.util.CloudinaryDocumentStorage;
import registrant.util.DocumentUrlResolver;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantDocumentHelper;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantProfileSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upload hồ sơ (Cloudinary / storage):
 * <ul>
 *   <li>4 loại bắt buộc → bảng Document; Notes marker #PENDING# / #APPROVED#</li>
 *   <li>Gửi duyệt → ExamRegistration Draft/Rejected → Pending (+ #PROFILE_DOC#)</li>
 *   <li>Other / xin hạng sau Approved — ER #SUPPLEMENT_DOC# / #LICENCE_DOC#</li>
 * </ul>
 * Không liên quan thu phí SePay.
 */
public class RegistrantUploadServiceImpl implements RegistrantUploadService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();

    /** Nạp slot tài liệu, summary và cờ đủ điều kiện gửi duyệt. */
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
        model.put("approvalLicenceOptions", listApprovalLicenceOptions());
        model.put("hasSupplementAwaitingSubmit", RegistrantDocumentStatusHelper.hasSupplementAwaitingSubmit(docs));
        model.put("hasSupplementPendingReview", supplementPending);
        model.put("canRequestApproval", canRequestApproval(registrationStatus, docs, supplementPending));
        model.put("hasPendingReview", primaryPending || supplementPending);
        RegistrantProfileSupport.applySyncToMap(model, ctx.getSyncResult());
        return model;
    }

    /** Upload nhiều tệp Hồ sơ khác; trả lỗi tiếng Việt hoặc null. */
    @Override
    public String handleOtherUpload(UserDTO user, String reasonNote,
            List<Part> fileParts, HttpServletRequest request) {
        if (reasonNote == null || reasonNote.isBlank()) {
            return "Vui lòng nhập lý do / ghi chú cho hồ sơ khác.";
        }
        if (fileParts == null || fileParts.isEmpty()) {
            return "Vui lòng chọn ít nhất một tệp để tải lên.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        for (Part filePart : fileParts) {
            String error = uploadSingleOther(profile.getProfileId(), reasonNote, filePart, request);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /** Upload một giấy tờ bắt buộc (Portrait/CCCD/SK); null nếu thành công. */
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
            return uploadSingleOther(profile.getProfileId(), reasonNote, filePart, request);
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

    /** Gửi duyệt: Pending→từ chối; Approved→requestSupplementApproval; Draft/Rejected→duyệt hồ sơ chính (+ hạng đã chọn). */
    @Override
    public String requestApproval(UserDTO user, String requestNote, String approvalLicenceCode,
            HttpSession session) {
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        String licenceError = validateApprovalLicenceCode(approvalLicenceCode);
        if (licenceError != null) {
            return licenceError;
        }
        String licenceCode = approvalLicenceCode.trim();
        int licenceId = registrantdao.resolveLicenceIdByUiCode(licenceCode);
        if (licenceId <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }

        List<RegistrantDocumentView> docs = documentdao.listByProfileId(profile.getProfileId());
        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profile.getProfileId());

        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đang chờ duyệt.";
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            if (registrantdao.hasOpenSupplementPending(profile.getProfileId())) {
                return "Hồ sơ bổ sung / xin duyệt hạng đang chờ duyệt.";
            }
            List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(
                    profile.getProfileId());
            boolean alreadyApprovedForLicence = RegistrantDocumentStatusHelper
                    .isLicenceAllowedWithDocuments(licenceCode, docs, approvedLicences);

            if (!RegistrantDocumentStatusHelper.hasSupplementAwaitingSubmit(docs)) {
                if (alreadyApprovedForLicence) {
                    return "Hạng " + licenceCode.trim().toUpperCase()
                            + " đã được duyệt kèm hồ sơ. Bạn có thể đăng ký thi hạng này ngay.";
                }
                // Tái sử dụng hồ sơ đã duyệt: xin duyệt thêm hạng mới (không upload lại)
                String msg = requestNote != null && !requestNote.isBlank()
                        ? requestNote.trim()
                        : "Xin duyệt hạng " + licenceCode.trim().toUpperCase()
                            + " với hồ sơ đã có (tái sử dụng 4 giấy tờ bắt buộc).";
                int erId = registrantdao.insertLicenceDocumentRegistration(
                        profile.getProfileId(), licenceId, ProfileRegistrationStatus.PENDING, msg);
                if (erId <= 0) {
                    return "Không thể gửi yêu cầu duyệt hạng " + licenceCode + ". Vui lòng thử lại.";
                }
                RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getProfileId(), msg);
                return null;
            }
            if (!requestSupplementApproval(profile.getProfileId(), requestNote, docs, licenceId, licenceCode)) {
                return "Không thể gửi yêu cầu duyệt hồ sơ bổ sung. Vui lòng thử lại.";
            }
            RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getProfileId(), requestNote);
            return null;
        } else if (!hasUploadableDocumentsForReview(docs)) {
            return "Chưa có tài liệu nào để gửi duyệt. Vui lòng tải lên ít nhất một tệp.";
        }

        // A1/A/B1 (và hạng chỉ-4-giấy): không bắt buộc Hồ sơ khác khi gửi duyệt lần đầu
        stampLicenceOnOtherDocuments(profile.getProfileId(), docs, licenceCode);

        if (!documentdao.requestApproval(profile.getProfileId(), requestNote)) {
            return "Không thể gửi yêu cầu duyệt. Vui lòng thử lại.";
        }

        docs = documentdao.listByProfileId(profile.getProfileId());
        RegistrantProfileSupport.updateRegistrationStatus(
                profile.getProfileId(), ProfileRegistrationStatus.PENDING, docs, registrantdao, licenceId);
        RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getProfileId(), requestNote);
        return null;
    }

    /** Tạo ER bổ sung + gắn Other awaiting với hạng chọn lúc gửi duyệt. */
    private boolean requestSupplementApproval(int profileId, String requestNote,
            List<RegistrantDocumentView> docs, int licenceId, String licenceCode) {
        List<RegistrantDocumentView> awaiting = new ArrayList<>();
        for (RegistrantDocumentView doc : docs) {
            if (!DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                continue;
            }
            if (!hasUploadedUrl(doc)) {
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

        stampLicenceOnDocuments(profileId, awaiting, licenceCode);

        String erMessage = requestNote != null && !requestNote.isBlank()
                ? "Thí sinh gửi duyệt hồ sơ bổ sung hạng " + licenceCode.trim().toUpperCase()
                    + ". Yêu cầu: " + requestNote.trim()
                : "Thí sinh gửi duyệt hồ sơ bổ sung hạng " + licenceCode.trim().toUpperCase() + ".";
        int supplementErId = registrantdao.insertSupplementDocumentRegistration(
                profileId, licenceId, ProfileRegistrationStatus.PENDING, erMessage);
        if (supplementErId <= 0) {
            return false;
        }

        boolean updated = false;
        for (RegistrantDocumentView doc : awaiting) {
            String notes = doc.getNotes();
            String merged = RegistrantDocumentHelper.appendSupplementErMarker(notes, supplementErId);
            merged = mergeSupplementPendingNote(merged, requestNote);
            if (documentdao.updateDocumentNotes(profileId, doc.getDocumentType(), merged)) {
                updated = true;
            }
        }
        return updated;
    }

    private void stampLicenceOnOtherDocuments(int profileId, List<RegistrantDocumentView> docs,
            String licenceCode) {
        List<RegistrantDocumentView> others = new ArrayList<>();
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType()) && hasUploadedUrl(doc)
                    && !DocumentDAOImpl.isApproved(doc.getNotes())) {
                others.add(doc);
            }
        }
        stampLicenceOnDocuments(profileId, others, licenceCode);
    }

    private void stampLicenceOnDocuments(int profileId, List<RegistrantDocumentView> docs, String licenceCode) {
        if (docs == null || licenceCode == null || licenceCode.isBlank()) {
            return;
        }
        for (RegistrantDocumentView doc : docs) {
            String stamped = applyLicenceMarker(doc.getNotes(), licenceCode);
            if (stamped != null && !stamped.equals(doc.getNotes())) {
                documentdao.updateDocumentNotes(profileId, doc.getDocumentType(), stamped);
                doc.setNotes(stamped);
                doc.setSupplementLicenceCode(licenceCode.trim().toUpperCase());
            }
        }
    }

    private static String applyLicenceMarker(String notes, String licenceCode) {
        String body = DocumentDAOImpl.stripLicenceMarker(notes != null ? notes : "");
        return DocumentDAOImpl.encodeLicenceMarker(licenceCode) + (body != null ? body.trim() : "");
    }

    private static boolean hasUploadedUrl(RegistrantDocumentView doc) {
        return doc != null && doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank();
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

    /** Xóa tài liệu (Cloudinary/local + DB) nếu còn được phép xóa. */
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

    private String uploadSingleOther(int profileId, String reasonNote,
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
                    storageType, reasonNote, filePart.getSize(), submitted, null);

            if (!documentdao.insertDocument(profileId, storageType, storedRef, notes)) {
                return "Không lưu được thông tin tài liệu.";
            }

            RegistrantAuditHelper.logDocumentUpload(request.getSession(), profileId, storageType, submitted);
            return null;
        } catch (IOException ex) {
            return "Lỗi khi lưu tệp: " + ex.getMessage();
        }
    }

    private String validateApprovalLicenceCode(String approvalLicenceCode) {
        if (approvalLicenceCode == null || approvalLicenceCode.isBlank()) {
            return "Vui lòng chọn hạng bằng khi gửi yêu cầu duyệt.";
        }
        if (registrantdao.resolveLicenceIdByUiCode(approvalLicenceCode.trim()) <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }
        return null;
    }

    private List<RegistrantLicenceOption> listApprovalLicenceOptions() {
        List<RegistrantLicenceOption> fromDb = registrantdao.listOpenLicenceOptions();
        if (fromDb != null && !fromDb.isEmpty()) {
            return new ArrayList<>(fromDb);
        }
        // Fallback nếu Licence trống / lỗi DB — vẫn cho chọn đủ hạng seed
        List<RegistrantLicenceOption> fallback = new ArrayList<>();
        for (String code : List.of("A1", "A", "B1")) {
            RegistrantLicenceOption opt = new RegistrantLicenceOption();
            opt.setCode(code);
            opt.setName(switch (code) {
                case "A1" -> "Xe mô tô đến 125 cm³";
                case "A" -> "Xe mô tô trên 125 cm³";
                default -> "Xe mô tô ba bánh";
            });
            opt.setExamFee(RegistrantExamSupport.defaultExamFee(code));
            opt.setVehicleType(RegistrantExamSupport.inferVehicleType(code));
            fallback.add(opt);
        }
        return fallback;
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
                    "Hệ thống lưu tệp tạm thời chưa sẵn sàng. Vui lòng thử lại sau hoặc liên hệ trung tâm hỗ trợ.");
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

    /** Nút Gửi duyệt: Draft cần ≥1 tệp; Approved: cho gửi xin duyệt hạng khác / hồ sơ khác nếu không còn Pending. */
    private static boolean canRequestApproval(String registrationStatus, List<RegistrantDocumentView> docs,
            boolean supplementErPending) {
        String status = registrationStatus != null ? registrationStatus.trim() : ProfileRegistrationStatus.DRAFT;
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return false;
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)) {
            return !supplementErPending;
        }
        return hasUploadableDocumentsForReview(docs);
    }
}
