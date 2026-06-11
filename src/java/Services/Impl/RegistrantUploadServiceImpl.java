package Services.Impl;

import DAO.CandidateDocumentDAO;
import DAO.PersonDAO;
import DAO.Impl.CandidateDocumentDAOImpl;
import DAO.Impl.PersonDAOImpl;
import Models.CandidateDocument;
import Models.Person;
import Models.UploadDocumentSlot;
import Models.User;
import Services.RegistrantUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Upload ảnh/CCCD/giấy khám — lưu disk + CandidateDocument; tùy chọn gửi duyệt lại. */
public class RegistrantUploadServiceImpl implements RegistrantUploadService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final CandidateDocumentDAO documentDAO = new CandidateDocumentDAOImpl();
    private final PersonDAO personDAO = new PersonDAOImpl();

    /** Build 4 UploadDocumentSlot với preview, trạng thái duyệt và nút upload. */
    @Override
    public void populateUploadPage(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            request.setAttribute("hasProfile", false);
            request.setAttribute("uploadSlots", List.of());
            request.setAttribute("missingProfileMessage",
                    "Bạn cần tạo hồ sơ cá nhân trước khi upload tài liệu.");
            return;
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            request.setAttribute("hasProfile", false);
            request.setAttribute("uploadSlots", List.of());
            request.setAttribute("missingProfileMessage", "Không tìm thấy hồ sơ cá nhân.");
            return;
        }

        request.setAttribute("hasProfile", true);
        request.setAttribute("registrantName", person.getFullName());
        request.setAttribute("uploadSlots", buildUploadSlots(person));
    }

    /**
     * Xử lý multipart: validate PNG/JPG ≤5MB, lưu disk, insert CandidateDocument.
     * Photo → cập nhật Person.photoUrl. Có thể gửi duyệt lại (markPendingReview).
     */
    @Override
    public String uploadDocuments(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            return "Vui lòng hoàn thiện hồ sơ cá nhân trước khi upload tài liệu.";
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }

        boolean uploadedAny = false;
        uploadedAny |= saveSlotFile(request, personId, "photo", "Photo", false, 0);
        uploadedAny |= saveSlotFile(request, personId, "idFront", "ID_Card", true, 0);
        uploadedAny |= saveSlotFile(request, personId, "idBack", "ID_Card", true, 1);
        uploadedAny |= saveSlotFile(request, personId, "healthCert", "Health_Cert", false, 0);

        String submitForReview = request.getParameter("submitForReview");
        if ("true".equals(submitForReview)) {
            if (!uploadedAny && !hasAnyDocument(personId)) {
                return "Vui lòng tải lên ít nhất một tài liệu trước khi gửi duyệt.";
            }
            if (!personDAO.markPendingReview(personId)) {
                return "Không thể gửi hồ sơ để duyệt. Vui lòng thử lại.";
            }
        } else if (!uploadedAny) {
            return "Vui lòng chọn ít nhất một tệp để tải lên.";
        }

        return null;
    }

    @Override
    public String deleteDocument(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            return "Vui lòng hoàn thiện hồ sơ cá nhân trước khi xóa tài liệu.";
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }
        if ("Approved".equals(person.getApprovalStatus())) {
            return "Hồ sơ đã được duyệt — không thể xóa tài liệu.";
        }

        String slotKey = trim(request.getParameter("slotKey"));
        if (slotKey == null || slotKey.isBlank()) {
            return "Thiếu thông tin loại tài liệu cần xóa.";
        }

        CandidateDocument document = resolveDocumentForSlot(personId, slotKey);
        if (document == null) {
            return "Không tìm thấy tài liệu để xóa.";
        }

        deleteStoredFile(request, document.getDocumentUrl());
        if (!documentDAO.deleteById(document.getId())) {
            return "Không thể xóa tài liệu. Vui lòng thử lại.";
        }

        if ("Photo".equals(document.getDocumentType())) {
            personDAO.updatePhotoUrl(personId, null);
        }
        return null;
    }

    private boolean hasAnyDocument(int personId) {
        return documentDAO.countByPersonIdAndType(personId, "Photo") > 0
                || documentDAO.countByPersonIdAndType(personId, "ID_Card") > 0
                || documentDAO.countByPersonIdAndType(personId, "Health_Cert") > 0;
    }

    /**
     * Lưu một Part multipart: thay thế bản ghi cũ cùng loại (CCCD dùng slotIndex 0/1).
     * @return true nếu có file mới được upload thành công
     */
    private boolean saveSlotFile(HttpServletRequest request, int personId, String partName,
            String documentType, boolean idCardSlot, int slotIndex) {
        try {
            Part part = request.getPart(partName);
            if (part == null || part.getSize() <= 0) {
                return false;
            }

            if (part.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Tệp " + partName + " vượt quá 5MB.");
            }

            String originalName = extractFileName(part);
            String extension = getExtension(originalName);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Tệp " + partName + " phải là PNG hoặc JPG/JPEG.");
            }

            Path uploadDir = resolveUploadDir(request, personId);
            Files.createDirectories(uploadDir);

            String storedName = partName + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path target = uploadDir.resolve(storedName);

            try (InputStream input = part.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String documentUrl = "/uploads/documents/" + personId + "/" + storedName;
            replaceExistingDocument(personId, documentType, idCardSlot, slotIndex);

            CandidateDocument document = new CandidateDocument();
            document.setPersonId(personId);
            document.setDocumentType(documentType);
            document.setDocumentUrl(documentUrl);

            if (!documentDAO.insert(document)) {
                Files.deleteIfExists(target);
                throw new IllegalStateException("Không thể lưu thông tin tài liệu vào cơ sở dữ liệu.");
            }

            if ("Photo".equals(documentType)) {
                personDAO.updatePhotoUrl(personId, documentUrl);
            }

            return true;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Không thể tải lên tệp " + partName + ".");
        }
    }

    private void replaceExistingDocument(int personId, String documentType, boolean idCardSlot, int slotIndex) {
        if (idCardSlot) {
            List<CandidateDocument> idCards = documentDAO.findIdCardsByPersonId(personId);
            if (slotIndex < idCards.size()) {
                documentDAO.deleteById(idCards.get(slotIndex).getId());
            }
            return;
        }

        documentDAO.deleteByPersonIdAndType(personId, documentType);
    }

    private Path resolveUploadDir(HttpServletRequest request, int personId) {
        String basePath = request.getServletContext().getRealPath("/uploads/documents/" + personId);
        return Paths.get(basePath);
    }

    private String extractFileName(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted != null && !submitted.isBlank()) {
            return Paths.get(submitted).getFileName().toString();
        }
        return "upload.bin";
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private List<UploadDocumentSlot> buildUploadSlots(Person person) {
        int personId = person.getId();
        String approvalStatus = person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending";

        CandidateDocument photo = documentDAO.findLatestByPersonIdAndType(personId, "Photo");
        List<CandidateDocument> idCards = documentDAO.findIdCardsByPersonId(personId);
        CandidateDocument idFront = idCards.size() > 0 ? idCards.get(0) : null;
        CandidateDocument idBack = idCards.size() > 1 ? idCards.get(1) : null;
        CandidateDocument healthCert = documentDAO.findLatestByPersonIdAndType(personId, "Health_Cert");

        List<UploadDocumentSlot> slots = new ArrayList<>();
        slots.add(buildSlot("photo", "1. Ảnh chân dung 3x4", "photo", photo, approvalStatus, person, true, false));
        slots.add(buildSlot("idFront", "2. Mặt trước CCCD / CMND", "idFront", idFront, approvalStatus, person, false, false));
        slots.add(buildSlot("idBack", "3. Mặt sau CCCD / CMND", "idBack", idBack, approvalStatus, person, false, false));
        slots.add(buildSlot("healthCert", "4. Giấy khám sức khỏe lái xe", "healthCert", healthCert, approvalStatus, person, false, true));
        return slots;
    }

    private UploadDocumentSlot buildSlot(String slotKey, String title, String fileInputName,
            CandidateDocument document, String approvalStatus, Person person,
            boolean imagePreview, boolean healthSlot) {
        UploadDocumentSlot slot = new UploadDocumentSlot();
        slot.setSlotKey(slotKey);
        slot.setTitle(title);
        slot.setFileInputName(fileInputName);
        slot.setImagePreview(imagePreview);

        boolean uploaded = document != null;
        slot.setShowPreview(uploaded);
        slot.setDocumentUrl(uploaded ? document.getDocumentUrl() : null);
        if (uploaded) {
            slot.setDocumentId(document.getId());
            slot.setCanDelete(!"Approved".equals(approvalStatus));
        }

        if (!uploaded) {
            slot.setCardClass("upload-card--rejected");
            slot.setStatusClass("pending");
            slot.setStatusLabel("Chưa có");
            slot.setShowUpload(true);
            slot.setShowFeedback(false);
            return slot;
        }

        if ("Approved".equals(approvalStatus)) {
            slot.setCardClass("upload-card--approved");
            slot.setStatusClass("success");
            slot.setStatusLabel("Đã duyệt");
            slot.setShowUpload(false);
        } else if ("Rejected".equals(approvalStatus) && healthSlot) {
            slot.setCardClass("upload-card--rejected");
            slot.setStatusClass("pending");
            slot.setStatusLabel("Yêu cầu bổ sung");
            slot.setShowUpload(true);
            slot.setShowFeedback(true);
            slot.setFeedbackMessage(person.getRejectionReason() != null
                    ? person.getRejectionReason()
                    : "Tài liệu cần được bổ sung hoặc tải lại.");
        } else {
            slot.setCardClass("upload-card--approved");
            slot.setStatusClass("info");
            slot.setStatusLabel("Đã tải lên");
            slot.setShowUpload(true);
        }

        return slot;
    }

    private CandidateDocument resolveDocumentForSlot(int personId, String slotKey) {
        return switch (slotKey) {
            case "photo" -> documentDAO.findLatestByPersonIdAndType(personId, "Photo");
            case "healthCert" -> documentDAO.findLatestByPersonIdAndType(personId, "Health_Cert");
            case "idFront", "idBack" -> {
                List<CandidateDocument> idCards = documentDAO.findIdCardsByPersonId(personId);
                int index = "idFront".equals(slotKey) ? 0 : 1;
                yield index < idCards.size() ? idCards.get(index) : null;
            }
            default -> null;
        };
    }

    private void deleteStoredFile(HttpServletRequest request, String documentUrl) {
        if (documentUrl == null || documentUrl.isBlank()) {
            return;
        }
        String relative = documentUrl.startsWith("/") ? documentUrl.substring(1) : documentUrl;
        Path filePath = Paths.get(request.getServletContext().getRealPath("/")).resolve(relative);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
