package Services.Impl;

import DAO.DocumentDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Constants.ProfileRegistrationStatus;
import Models.Profile;
import Models.RegistrantDocumentView;
import Models.RegistrantProfileContext;
import Models.User;
import Services.RegistrantUploadService;
import Utils.CloudinaryDocumentStorage;
import Utils.DocumentUrlResolver;
import Utils.RegistrantAuditHelper;
import Utils.RegistrantDocumentStatusHelper;
import Utils.RegistrantDocumentUploadSupport;
import Utils.RegistrantProfileRegistrationSync;
import Utils.RegistrantProfileSupport;
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

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();

    @Override
    public Map<String, Object> loadUploadPage(User user, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profileDAO, documentDAO, registrantDAO, user);
        if (!ctx.hasProfile()) {
            return model;
        }

        List<RegistrantDocumentView> docs = ctx.getDocuments();
        String registrationStatus = ctx.getRegistrationStatus();

        RegistrantDocumentStatusHelper.applyDocumentLabelsFromRegistrationStatus(docs, registrationStatus);
        DocumentUrlResolver.resolveViewUrls(docs, request);

        Map<String, RegistrantDocumentView> slots = mergeDocumentSlots(docs);
        List<RegistrantDocumentView> otherDocs = listOtherDocuments(docs);
        DocumentUrlResolver.resolveViewUrls(otherDocs, request);

        boolean profileApproved = ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus);

        model.put("documentList", docs);
        model.put("documentsByType", slots);
        model.put("otherDocuments", otherDocs);
        model.put("otherDocumentCount", otherDocs.size());
        model.put("profileApproved", profileApproved);
        model.put("hasSupplementAwaitingSubmit", hasSupplementAwaitingSubmit(docs));
        model.put("canRequestApproval", canRequestApproval(registrationStatus, docs));
        model.put("hasPendingReview", ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus));
        RegistrantProfileSupport.applySyncToMap(model, ctx.getSyncResult());
        return model;
    }

    @Override
    public String handleOtherUpload(User user, String reasonNote, List<Part> fileParts,
            HttpServletRequest request) {
        if (reasonNote == null || reasonNote.isBlank()) {
            return "Vui lòng nhập lý do / ghi chú cho hồ sơ khác.";
        }
        if (fileParts == null || fileParts.isEmpty()) {
            return "Vui lòng chọn ít nhất một tệp để tải lên.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        for (Part filePart : fileParts) {
            String error = uploadSingleOther(profile.getId(), reasonNote, filePart, request);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String handleUpload(User user, String documentType, Part filePart, String reasonNote,
            HttpServletRequest request) {
        if (!RegistrantDocumentUploadSupport.isAllowedDocumentType(documentType)) {
            return "Loại tài liệu không hợp lệ.";
        }
        if ("Other".equals(documentType)) {
            Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
            if (profile == null) {
                return "Không tìm thấy hồ sơ.";
            }
            return uploadSingleOther(profile.getId(), reasonNote, filePart, request);
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        String registrationStatus = registrantDAO.findProfileDocumentRegistrationStatus(profile.getId());
        String statusError = validateMandatoryUploadAllowed(registrationStatus);
        if (statusError != null) {
            return statusError;
        }

        String validationError = RegistrantDocumentUploadSupport.validateMandatoryUpload(filePart, documentType);
        if (validationError != null) {
            return validationError;
        }

        try {
            String submitted = filePart.getSubmittedFileName();
            String ext = RegistrantDocumentUploadSupport.extractExtension(submitted);
            String previousUrl = RegistrantProfileSupport.findStoredUrlForType(
                    documentDAO.listByProfileId(profile.getId()), documentType);
            String storedRef = uploadToCloudinary(filePart, profile.getId(), documentType, ext);
            String notes = DocumentDAOImpl.buildUploadNote(documentType, reasonNote, filePart.getSize(), submitted);

            if (!documentDAO.upsertDocument(profile.getId(), documentType, storedRef, notes)) {
                return "Không lưu được thông tin tài liệu.";
            }

            deleteReplacedStoredRef(request, previousUrl, storedRef);
            RegistrantAuditHelper.logDocumentUpload(
                    request.getSession(), profile.getId(), documentType, submitted);
            return null;
        } catch (IOException ex) {
            return "Lỗi khi lưu tệp: " + ex.getMessage();
        }
    }

    @Override
    public String requestApproval(User user, String requestNote, HttpSession session) {
        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        List<RegistrantDocumentView> docs = documentDAO.listByProfileId(profile.getId());
        String registrationStatus = registrantDAO.findProfileDocumentRegistrationStatus(profile.getId());

        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(registrationStatus)) {
            return "Hồ sơ đang chờ duyệt.";
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(registrationStatus)) {
            if (!hasSupplementAwaitingSubmit(docs)) {
                return "Chưa có hồ sơ bổ sung mới. Vui lòng thêm tệp tại mục «Hồ sơ khác» trước khi gửi duyệt.";
            }
        } else if (!hasUploadableDocumentsForReview(docs)) {
            return "Chưa có tài liệu nào để gửi duyệt. Vui lòng tải lên ít nhất một tệp.";
        }

        if (!documentDAO.requestApproval(profile.getId(), requestNote)) {
            return "Không thể gửi yêu cầu duyệt. Vui lòng thử lại.";
        }

        RegistrantProfileRegistrationSync.updateRegistrationStatus(
                profile.getId(), ProfileRegistrationStatus.PENDING, docs, registrantDAO);
        RegistrantAuditHelper.logDocumentApprovalRequest(session, profile.getId(), requestNote);
        return null;
    }

    @Override
    public String deleteDocument(User user, int documentId, HttpServletRequest request) {
        if (documentId <= 0) {
            return "Tài liệu không hợp lệ.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ.";
        }

        RegistrantDocumentView doc = documentDAO.findById(profile.getId(), documentId);
        if (doc == null) {
            return "Không tìm thấy tài liệu.";
        }

        String deleteError = validateDeleteAllowed(profile.getId(), doc);
        if (deleteError != null) {
            return deleteError;
        }

        DocumentUrlResolver.deleteStoredRef(request.getServletContext(), doc.getDocumentUrl());
        if (!documentDAO.deleteDocument(profile.getId(), documentId)) {
            return "Không xóa được tài liệu.";
        }

        RegistrantAuditHelper.logDocumentDelete(
                request.getSession(), profile.getId(), doc.getDocumentType(), doc.getFileName());
        return null;
    }

    private String uploadSingleOther(int profileId, String reasonNote, Part filePart,
            HttpServletRequest request) {
        String validationError = RegistrantDocumentUploadSupport.validateOtherUpload(filePart);
        if (validationError != null) {
            return validationError;
        }

        try {
            String submitted = filePart.getSubmittedFileName();
            String ext = RegistrantDocumentUploadSupport.extractExtension(submitted);
            String storageType = DocumentDAOImpl.newOtherDocumentType();
            String storedRef = uploadToCloudinary(filePart, profileId, storageType, ext);
            String notes = DocumentDAOImpl.buildUploadNote(storageType, reasonNote, filePart.getSize(), submitted);

            if (!documentDAO.insertDocument(profileId, storageType, storedRef, notes)) {
                return "Không lưu được thông tin tài liệu.";
            }

            RegistrantAuditHelper.logDocumentUpload(request.getSession(), profileId, storageType, submitted);
            return null;
        } catch (IOException ex) {
            return "Lỗi khi lưu tệp: " + ex.getMessage();
        }
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
        String registrationStatus = registrantDAO.findProfileDocumentRegistrationStatus(profileId);
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

    private Map<String, RegistrantDocumentView> mergeDocumentSlots(List<RegistrantDocumentView> docs) {
        Map<String, RegistrantDocumentView> slots = documentDAO.defaultDocumentSlots();
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                continue;
            }
            if (slots.containsKey(doc.getDocumentType())) {
                slots.put(doc.getDocumentType(), doc);
            }
        }
        return slots;
    }

    private static List<RegistrantDocumentView> listOtherDocuments(List<RegistrantDocumentView> docs) {
        return docs.stream()
                .filter(doc -> DocumentDAOImpl.isOtherType(doc.getDocumentType()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean hasUploadableDocumentsForReview(List<RegistrantDocumentView> docs) {
        return docs.stream().anyMatch(doc ->
                doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank());
    }

    private static boolean hasSupplementAwaitingSubmit(List<RegistrantDocumentView> docs) {
        return docs.stream().anyMatch(doc ->
                DocumentDAOImpl.isOtherType(doc.getDocumentType())
                        && doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank()
                        && !DocumentDAOImpl.isApproved(doc.getNotes())
                        && !DocumentDAOImpl.isPendingReview(doc.getNotes()));
    }

    private static boolean canRequestApproval(String registrationStatus, List<RegistrantDocumentView> docs) {
        String status = registrationStatus != null ? registrationStatus.trim() : ProfileRegistrationStatus.DRAFT;
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return false;
        }
        if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)) {
            return hasSupplementAwaitingSubmit(docs);
        }
        return hasUploadableDocumentsForReview(docs);
    }
}
