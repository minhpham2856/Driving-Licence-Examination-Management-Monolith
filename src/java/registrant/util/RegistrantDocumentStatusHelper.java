package registrant.util;

import registrant.enums.ProfileRegistrationStatus;
import registrant.dao.impl.DocumentDAOImpl;
import registrant.dao.RegistrantDAO;
import registrant.dto.RegistrantDocumentSummary;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantTrackingLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Logic hiển thị và tóm tắt trạng thái tài liệu theo ExamRegistration.RegistrationStatus (nguồn chân lý).
 * Document.Notes chỉ chứa marker tệp (#PENDING#, #APPROVED#, #LICENCE#); hạng A1/A/B1 cần đủ 4 giấy bắt buộc. Dùng trên upload, profile, track-profile và dashboard.
 */
public final class RegistrantDocumentStatusHelper {

    public static final String[] REQUIRED_TYPES = {
            "Portrait", "IdFront", "IdBack", "HealthCertificate"
    };

    /** Chỉ cần 4 giấy tờ bắt buộc — khớp seed DB hạng A, A1 (alias A2 giữ để URL cũ không lỗi). */
    /** Hạng chỉ cần 4 giấy tờ bắt buộc đã duyệt (seed hiện tại: A1, A, B1). */
    public static final Set<String> BASIC_DOCS_ONLY_LICENCE_CODES = Set.of("A1", "A", "B1");

    private RegistrantDocumentStatusHelper() {
    }

    /** Tóm tắt tiến độ tài liệu (đã tải/chờ duyệt/overall) theo RegistrationStatus. */
    public static RegistrantDocumentSummary summarize(List<RegistrantDocumentView> allDocs,
            Map<String, String> typeLabels, String registrationStatus) {
        applyDocumentLabelsFromRegistrationStatus(allDocs, registrationStatus);
        Map<String, RegistrantDocumentView> slots = RegistrantDocumentHelper.buildRequiredSlots(allDocs);
        RegistrantDocumentSummary summary = new RegistrantDocumentSummary();
        summary.setRequiredTotal(REQUIRED_TYPES.length);

        int uploaded = 0;
        List<RegistrantDocumentSummary.ChecklistItem> checklist = new ArrayList<>();

        for (String type : REQUIRED_TYPES) {
            RegistrantDocumentView doc = slots.get(type);
            boolean hasFile = hasUploadedFile(doc);
            if (hasFile) {
                uploaded++;
            }

            RegistrantDocumentSummary.ChecklistItem item = new RegistrantDocumentSummary.ChecklistItem();
            item.setLabel(resolveLabel(type, typeLabels));
            item.setStatusClass(doc.getStatusClass());
            item.setStatusLabel(doc.getStatusLabel());
            item.setUploaded(hasFile);
            checklist.add(item);
        }

        int otherCount = 0;
        for (RegistrantDocumentView doc : allDocs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType()) && hasUploadedFile(doc)) {
                otherCount++;
            }
        }

        summary.setRequiredUploaded(uploaded);
        summary.setOtherCount(otherCount);
        summary.setPendingReviewCount(0);
        summary.setApprovedCount(0);
        summary.setRejectedCount(0);
        summary.setAwaitingSubmitCount(0);
        summary.setChecklistItems(checklist);
        applyOverallStatusFromRegistration(summary, registrationStatus, allDocs);
        return summary;
    }

    /** Gán nhãn từng tài liệu theo RegistrationStatus (nguồn chính). */
    public static void applyDocumentLabelsFromRegistrationStatus(List<RegistrantDocumentView> docs,
            String registrationStatus) {
        if (docs == null) {
            return;
        }
        String status = normalizeRegistrationStatus(registrationStatus);
        for (RegistrantDocumentView doc : docs) {
            applySingleDocumentLabel(doc, status);
        }
    }

    /** Đủ điều kiện đăng ký thi: RegistrationStatus = Approved và đủ 4 giấy tờ bắt buộc. */
    public static boolean isEligibleForExamRegistration(String registrationStatus,
            List<RegistrantDocumentView> allDocs) {
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(normalizeRegistrationStatus(registrationStatus))) {
            return false;
        }
        return hasAllRequiredDocumentsUploaded(allDocs);
    }

    /** True nếu đã tải đủ 4 giấy tờ bắt buộc (Portrait, IdFront, IdBack, HealthCertificate). */
    public static boolean hasAllRequiredDocumentsUploaded(List<RegistrantDocumentView> allDocs) {
        Map<String, RegistrantDocumentView> slots = RegistrantDocumentHelper.buildRequiredSlots(allDocs);
        for (String type : REQUIRED_TYPES) {
            if (!hasUploadedFile(slots.get(type))) {
                return false;
            }
        }
        return true;
    }

    /** Thông báo lý do chưa được đăng ký thi - null nếu đủ điều kiện. */
    public static String examRegistrationBlockMessage(String registrationStatus,
            List<RegistrantDocumentView> allDocs, RegistrantDocumentSummary summary) {
        if (isEligibleForExamRegistration(registrationStatus, allDocs)) {
            return null;
        }
        String status = normalizeRegistrationStatus(registrationStatus);
        if (ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status)) {
            return "Hồ sơ tài liệu cần bổ sung và được duyệt lại trước khi đăng ký thi.";
        }
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return "Tài liệu đang chờ duyệt. Bạn chỉ có thể đăng ký thi sau khi hồ sơ được phê duyệt.";
        }
        if (summary.getRequiredUploaded() < summary.getRequiredTotal()) {
            return String.format(
                    "Vui lòng tải đủ %d giấy tờ bắt buộc và được ban quản lý phê duyệt trước khi đăng ký thi.",
                    summary.getRequiredTotal());
        }
        if (ProfileRegistrationStatus.DRAFT.equalsIgnoreCase(status)) {
            return "Vui lòng gửi yêu cầu duyệt hồ sơ tài liệu trước khi đăng ký thi.";
        }
        return "Tài liệu chưa được phê duyệt. Hoàn tất duyệt hồ sơ trước khi đăng ký thi.";
    }

    /** True nếu có ít nhất một tệp Hồ sơ khác đã upload. */
    public static boolean hasUploadedOtherDocuments(List<RegistrantDocumentView> allDocs) {
        if (allDocs == null) {
            return false;
        }
        for (RegistrantDocumentView doc : allDocs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType()) && hasUploadedFile(doc)) {
                return true;
            }
        }
        return false;
    }

    /** Có request bổ sung đang chờ: ưu tiên ExamRegistration, fallback marker legacy trên Notes. */
    public static boolean hasAnySupplementPendingReview(RegistrantDAO registrantdao, int profileId,
            List<RegistrantDocumentView> allDocs) {
        if (profileId > 0 && registrantdao != null && registrantdao.hasOpenSupplementPending(profileId)) {
            return true;
        }
        return hasSupplementPendingReview(allDocs);
    }

    /** Có ít nhất một tệp Hồ sơ khác đã gửi ban quản lý (marker #PENDING#) khi hồ sơ chính vẫn Approved. */
    public static boolean hasSupplementPendingReview(List<RegistrantDocumentView> allDocs) {
        if (allDocs == null) {
            return false;
        }
        for (RegistrantDocumentView doc : allDocs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())
                    && hasUploadedFile(doc)
                    && DocumentDAOImpl.isPendingReview(doc.getNotes())) {
                return true;
            }
        }
        return false;
    }

    /** True nếu Other đã tải nhưng chưa gửi duyệt (#PENDING#). */
    public static boolean hasSupplementAwaitingSubmit(List<RegistrantDocumentView> allDocs) {
        if (allDocs == null) {
            return false;
        }
        for (RegistrantDocumentView doc : allDocs) {
            if (!DocumentDAOImpl.isOtherType(doc.getDocumentType()) || !hasUploadedFile(doc)) {
                continue;
            }
            String notes = doc.getNotes();
            if (!DocumentDAOImpl.isApproved(notes) && !DocumentDAOImpl.isPendingReview(notes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chỉ enable hạng đã có bản duyệt hồ sơ kèm đúng hạng đó
     * (approvedLicenceCodes từ ExamRegistration Approved).
     */
    public static boolean isLicenceAllowedWithDocuments(String uiLicenceCode,
            List<RegistrantDocumentView> allDocs, Collection<String> approvedLicenceCodes) {
        if (uiLicenceCode == null || uiLicenceCode.isBlank()) {
            return false;
        }
        if (approvedLicenceCodes == null || approvedLicenceCodes.isEmpty()) {
            return false;
        }
        String target = uiLicenceCode.trim().toUpperCase(Locale.ROOT);
        for (String code : approvedLicenceCodes) {
            if (code != null && target.equals(code.trim().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /** Tương thích cũ: không có danh sách hạng duyệt → không mở hạng. */
    public static boolean isLicenceAllowedWithDocuments(String uiLicenceCode,
            List<RegistrantDocumentView> allDocs) {
        return isLicenceAllowedWithDocuments(uiLicenceCode, allDocs, List.of());
    }

    /** Message chặn chọn hạng khi chưa được duyệt kèm hồ sơ. */
    public static String licenceClassBlockMessage(String uiLicenceCode,
            List<RegistrantDocumentView> allDocs, Collection<String> approvedLicenceCodes) {
        if (isLicenceAllowedWithDocuments(uiLicenceCode, allDocs, approvedLicenceCodes)) {
            return null;
        }
        String code = uiLicenceCode != null ? uiLicenceCode.trim().toUpperCase(Locale.ROOT) : "";
        return "Hạng " + code + " chưa được ban quản lý duyệt kèm hồ sơ. "
                + "Vào Quản lý tài liệu → chọn hạng " + code
                + " khi Gửi yêu cầu duyệt (có thể tái sử dụng 4 giấy đã có nếu không cần đổi).";
    }

    /** Overload không có danh sách hạng duyệt (fallback). */
    public static String licenceClassBlockMessage(String uiLicenceCode, List<RegistrantDocumentView> allDocs) {
        return licenceClassBlockMessage(uiLicenceCode, allDocs, List.of());
    }

    /** Map mã hạng → có được phép đăng ký theo hồ sơ đã duyệt. */
    public static Map<String, Boolean> buildLicenceDocumentAllowedMap(
            List<String> licenceCodes, List<RegistrantDocumentView> allDocs,
            Collection<String> approvedLicenceCodes) {
        Map<String, Boolean> allowed = new LinkedHashMap<>();
        if (licenceCodes != null) {
            for (String code : licenceCodes) {
                if (code != null && !code.isBlank()) {
                    allowed.put(code.trim(),
                            isLicenceAllowedWithDocuments(code, allDocs, approvedLicenceCodes));
                }
            }
        }
        return allowed;
    }

    /** Overload map hạng được phép (không truyền approved codes). */
    public static Map<String, Boolean> buildLicenceDocumentAllowedMap(
            List<String> licenceCodes, List<RegistrantDocumentView> allDocs) {
        return buildLicenceDocumentAllowedMap(licenceCodes, allDocs, List.of());
    }

    /** Map mã hạng → thông báo chặn nếu chưa đủ điều kiện. */
    public static Map<String, String> buildLicenceDocumentBlockMessageMap(
            List<String> licenceCodes, List<RegistrantDocumentView> allDocs,
            Collection<String> approvedLicenceCodes) {
        Map<String, String> messages = new LinkedHashMap<>();
        if (licenceCodes != null) {
            for (String code : licenceCodes) {
                if (code != null && !code.isBlank()) {
                    String block = licenceClassBlockMessage(code, allDocs, approvedLicenceCodes);
                    if (block != null) {
                        messages.put(code.trim(), block);
                    }
                }
            }
        }
        return messages;
    }

    /** Overload map thông báo chặn hạng. */
    public static Map<String, String> buildLicenceDocumentBlockMessageMap(
            List<String> licenceCodes, List<RegistrantDocumentView> allDocs) {
        return buildLicenceDocumentBlockMessageMap(licenceCodes, allDocs, List.of());
    }

    /** Sinh dòng tracking giả lập từ trạng thái từng tài liệu. */
    public static List<RegistrantTrackingLog> buildDocumentTrackingLogs(List<RegistrantDocumentView> docs,
            Map<String, String> typeLabels, Date profileCreatedAt, String registrationStatus) {
        List<RegistrantTrackingLog> logs = new ArrayList<>();
        long baseMs = profileCreatedAt != null ? profileCreatedAt.getTime() : System.currentTimeMillis();
        String status = normalizeRegistrationStatus(registrationStatus);

        for (RegistrantDocumentView doc : docs) {
            if (!hasUploadedFile(doc)) {
                continue;
            }

            String label = resolveLabel(doc.getDocumentType(), typeLabels);
            String notes = doc.getNotes() != null ? doc.getNotes() : "";
            RegistrantTrackingLog log = new RegistrantTrackingLog();
            log.setTimestamp(estimateTimestamp(baseMs, doc.getDocumentId()));

            if (isRejected(notes)) {
                log.setEventTitle("Yêu cầu bổ sung: " + label);
                log.setActorRole("Ban quản lý");
                log.setStatusClass("rejected");
                log.setStatusLabel("Từ chối");
                log.setRemarks(extractRejectReason(notes));
                log.setCategory(RegistrantTrackingCategories.DOCUMENT_REJECT);
            } else if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)) {
                log.setEventTitle("Phê duyệt tài liệu: " + label);
                log.setActorRole("Ban quản lý");
                log.setStatusClass("approved");
                log.setStatusLabel("Thành công");
                log.setRemarks(buildFileRemark(doc));
                log.setCategory(RegistrantTrackingCategories.DOCUMENT_APPROVE);
            } else if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
                log.setEventTitle("Gửi duyệt tài liệu: " + label);
                log.setActorRole("Thí sinh");
                log.setStatusClass("pending");
                log.setStatusLabel("Đang xử lý");
                log.setRemarks("Tài liệu đang chờ ban quản lý kiểm duyệt. " + buildFileRemark(doc));
                log.setCategory(RegistrantTrackingCategories.DOCUMENT_SUBMIT);
            } else {
                log.setEventTitle("Tải lên tài liệu: " + label);
                log.setActorRole("Thí sinh");
                log.setStatusClass("info");
                log.setStatusLabel("Chưa gửi duyệt");
                log.setRemarks("Tài liệu đã lưu trên hệ thống. Vui lòng gửi yêu cầu duyệt khi đã tải đủ. "
                        + buildFileRemark(doc));
                log.setCategory(RegistrantTrackingCategories.DOCUMENT_UPLOAD);
            }
            logs.add(log);
        }
        return logs;
    }

    private static void applySingleDocumentLabel(RegistrantDocumentView doc, String registrationStatus) {
        if (!hasUploadedFile(doc)) {
            doc.setStatusClass("pending");
            doc.setStatusLabel("Chưa tải lên");
            return;
        }
        if (isRejected(doc.getNotes())) {
            doc.setStatusClass("danger");
            doc.setStatusLabel("Yêu cầu bổ sung");
            return;
        }
        switch (registrationStatus) {
            case ProfileRegistrationStatus.APPROVED -> {
                if (!DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                    doc.setStatusClass("success");
                    doc.setStatusLabel("Đã duyệt");
                } else if (DocumentDAOImpl.isPendingReview(doc.getNotes())) {
                    doc.setStatusClass("warning");
                    doc.setStatusLabel("Chờ duyệt");
                } else if (!DocumentDAOImpl.isApproved(doc.getNotes())) {
                    doc.setStatusClass("warning");
                    doc.setStatusLabel("Chưa gửi duyệt");
                } else {
                    doc.setStatusClass("success");
                    doc.setStatusLabel("Đã duyệt");
                }
            }
            case ProfileRegistrationStatus.PENDING -> {
                doc.setStatusClass("warning");
                doc.setStatusLabel("Chờ duyệt");
            }
            case ProfileRegistrationStatus.REJECTED -> {
                doc.setStatusClass("danger");
                doc.setStatusLabel("Yêu cầu bổ sung");
            }
            default -> {
                doc.setStatusClass("warning");
                doc.setStatusLabel("Chưa gửi duyệt");
            }
        }
    }

    private static void applyOverallStatusFromRegistration(RegistrantDocumentSummary summary,
            String registrationStatus, List<RegistrantDocumentView> allDocs) {
        String status = normalizeRegistrationStatus(registrationStatus);
        summary.setOverallStatusLabel(ProfileRegistrationStatus.toDisplayLabel(status));
        summary.setOverallStatusClass(mapOverallClass(status));

        if (summary.getRequiredUploaded() < summary.getRequiredTotal()) {
            int missing = summary.getRequiredTotal() - summary.getRequiredUploaded();
            summary.setOverallStatusClass("incomplete");
            summary.setOverallStatusLabel("Chưa đủ giấy tờ");
            summary.setOverallMessage(String.format(
                    "Đã tải %d/%d giấy tờ bắt buộc - còn thiếu %d mục.",
                    summary.getRequiredUploaded(), summary.getRequiredTotal(), missing));
            return;
        }

        summary.setOverallMessage(switch (status) {
            case ProfileRegistrationStatus.APPROVED -> hasSupplementPendingReview(allDocs)
                    ? "Giấy tờ bắt buộc đã được phê duyệt. Hồ sơ bổ sung đang chờ ban quản lý xem xét."
                    : "Tất cả giấy tờ bắt buộc đã được ban quản lý phê duyệt.";
            case ProfileRegistrationStatus.PENDING ->
                    "Hồ sơ đã gửi ban quản lý - đang chờ phê duyệt.";
            case ProfileRegistrationStatus.REJECTED ->
                    "Hồ sơ bị từ chối - vui lòng bổ sung và gửi duyệt lại.";
            default -> String.format(
                    "Đã tải đủ %d giấy tờ bắt buộc%s. Hãy gửi yêu cầu duyệt trên trang tải hồ sơ.",
                    summary.getRequiredTotal(),
                    summary.getOtherCount() > 0 ? " và " + summary.getOtherCount() + " hồ sơ khác" : "");
        });
    }

    private static String mapOverallClass(String status) {
        return switch (status) {
            case ProfileRegistrationStatus.APPROVED -> "complete";
            case ProfileRegistrationStatus.PENDING -> "pending";
            case ProfileRegistrationStatus.REJECTED -> "danger";
            default -> "warning";
        };
    }

    private static String normalizeRegistrationStatus(String registrationStatus) {
        if (registrationStatus == null || registrationStatus.isBlank()) {
            return ProfileRegistrationStatus.DRAFT;
        }
        return registrationStatus.trim();
    }

    private static boolean hasUploadedFile(RegistrantDocumentView doc) {
        return doc != null && doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank();
    }

    private static boolean isRejected(String notes) {
        if (notes == null) {
            return false;
        }
        String lower = notes.toLowerCase();
        return lower.contains("từ chối") || lower.contains("reject");
    }

    private static String resolveLabel(String documentType, Map<String, String> typeLabels) {
        if (DocumentDAOImpl.isOtherType(documentType)) {
            return typeLabels.getOrDefault("Other", "Hồ sơ khác");
        }
        return typeLabels.getOrDefault(documentType, documentType);
    }

    private static String buildFileRemark(RegistrantDocumentView doc) {
        StringBuilder sb = new StringBuilder();
        String fileName = DocumentDAOImpl.stripInternalMarkers(doc.getFileName());
        if (!fileName.isBlank()) {
            sb.append("Tệp: ").append(fileName);
        }
        if (doc.getFileSizeLabel() != null && !doc.getFileSizeLabel().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(doc.getFileSizeLabel());
        }
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private static String extractRejectReason(String notes) {
        if (notes == null || notes.isBlank()) {
            return "Tài liệu cần bổ sung.";
        }
        int idx = notes.toLowerCase().indexOf("từ chối:");
        if (idx >= 0) {
            return notes.substring(idx).trim();
        }
        return notes.trim();
    }

    private static Date estimateTimestamp(long baseMs, int documentId) {
        long offset = Math.max(1, documentId) * 60_000L;
        return new Date(baseMs + offset);
    }
}
