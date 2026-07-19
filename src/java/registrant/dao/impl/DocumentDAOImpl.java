package registrant.dao.impl;

import registrant.util.RegistrantDocumentHelper;
import registrant.enums.ProfileRegistrationStatus;
import registrant.dao.DocumentDAO;
import shared.dbconnection.DBContext;
import registrant.dto.RegistrantDocumentView;
import registrant.util.RegistrantDocumentTypeMapping;
import registrant.util.RegistrantExamSupport;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Document: lưu DocumentTypeId; trạng thái duyệt trong Notes (#PENDING#/#APPROVED#/#LICENCE#). */
public class DocumentDAOImpl extends DBContext implements DocumentDAO {

    private static final Logger LOG = Logger.getLogger(DocumentDAOImpl.class.getName());

    public static final String MARK_PENDING = "#PENDING#";
    public static final String MARK_APPROVED = "#APPROVED#";
    public static final String MARK_LICENCE_PREFIX = "#LICENCE#"; // #LICENCE#B2#

    private static final String DOCUMENT_SELECT = """
                SELECT d.DocumentId, dt.Type AS DocumentType, d.DocumentUrl, d.Notes
                FROM Document d
                INNER JOIN DocumentType dt ON dt.DocumentTypeId = d.DocumentTypeId
                """;

    private static Map<String, String> buildTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("Portrait", "Ảnh chân dung 3x4");
        labels.put("IdFront", "Mặt trước CCCD");
        labels.put("IdBack", "Mặt sau CCCD");
        labels.put("HealthCertificate", "Giấy khám sức khỏe");
        labels.put("Other", "Hồ sơ khác");
        return labels;
    }

    private static final Map<String, String> TYPE_LABELS = buildTypeLabels();

    private Integer resolveDocumentTypeId(String uiDocumentType) throws SQLException {
        String dbType = RegistrantDocumentTypeMapping.toDbType(uiDocumentType);
        if (dbType == null) {
            return null;
        }
        String sql = "SELECT DocumentTypeId FROM DocumentType WHERE Type = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dbType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DocumentTypeId");
                }
            }
        }
        return null;
    }

    /** Map DocumentType UI → nhãn tiếng Việt cho UI. */
    @Override
    public Map<String, String> typeLabels() {
        return TYPE_LABELS;
    }

    /** Tạo slot trống 4 giấy bắt buộc cho form upload. */
    @Override
    public Map<String, RegistrantDocumentView> defaultDocumentSlots() {
        Map<String, RegistrantDocumentView> slots = new HashMap<>();
        for (Map.Entry<String, String> entry : TYPE_LABELS.entrySet()) {
            if (isOtherType(entry.getKey())) {
                continue;
            }
            RegistrantDocumentView view = new RegistrantDocumentView();
            view.setDocumentType(entry.getKey());
            view.setStatusClass("pending");
            view.setStatusLabel("Chưa tải lên");
            slots.put(entry.getKey(), view);
        }
        return slots;
    }

    /** Liệt kê Document của profile (map sang view UI). */
    @Override
    public List<RegistrantDocumentView> listByProfileId(int profileId) {
        String sql = DOCUMENT_SELECT + """
                WHERE d.ProfileId = ?
                ORDER BY d.DocumentId
                """;
        List<RegistrantDocumentView> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải được danh sách tài liệu profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return list;
    }

    /** Như listByProfileId nhưng luôn có DocumentId. */
    @Override
    public List<RegistrantDocumentView> listByProfileIdWithDocumentId(int profileId) {
        String sql = DOCUMENT_SELECT + """
                WHERE d.ProfileId = ?
                ORDER BY d.DocumentId
                """;
        List<RegistrantDocumentView> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistrantDocumentView view = mapRow(rs);
                    view.setDocumentId(rs.getInt("DocumentId"));
                    list.add(view);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải được danh sách tài liệu (có Id) profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return list;
    }

    /** Insert hoặc cập nhật tài liệu theo DocumentType. */
    @Override
    public boolean upsertDocument(int profileId, String documentType, String documentUrl, String notes) {
        Integer existingId = findDocumentId(profileId, documentType);
        if (existingId != null) {
            String updateSql = """
                    UPDATE Document
                    SET DocumentUrl = ?, Notes = ?
                    WHERE DocumentId = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(updateSql)) {
                ps.setString(1, documentUrl);
                ps.setString(2, notes);
                ps.setInt(3, existingId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Cập nhật tài liệu thất bại: {0}", e.getMessage());
            }
            return false;
        }

        String insertSql = """
                INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            Integer typeId = resolveDocumentTypeId(documentType);
            if (typeId == null) {
                return false;
            }
            ps.setInt(1, typeId);
            ps.setString(2, documentUrl);
            ps.setString(3, notes);
            ps.setInt(4, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Thêm tài liệu thất bại: {0}", e.getMessage());
        }
        return false;
    }

    /** Tìm Document thuộc profile theo DocumentId. */
    @Override
    public RegistrantDocumentView findById(int profileId, int documentId) {
        if (documentId <= 0 || profileId <= 0) {
            return null;
        }
        String sql = DOCUMENT_SELECT + """
                WHERE d.DocumentId = ? AND d.ProfileId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, documentId);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RegistrantDocumentView view = mapRow(rs);
                    view.setDocumentId(rs.getInt("DocumentId"));
                    return view;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tìm thấy tài liệu {0}: {1}",
                    new Object[] { documentId, e.getMessage() });
        }
        return null;
    }

    /** Xóa Document nếu thuộc đúng profile. */
    @Override
    public boolean deleteDocument(int profileId, int documentId) {
        if (documentId <= 0 || profileId <= 0) {
            return false;
        }
        String sql = "DELETE FROM Document WHERE DocumentId = ? AND ProfileId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, documentId);
            ps.setInt(2, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Xóa tài liệu {0} thất bại: {1}",
                    new Object[] { documentId, e.getMessage() });
        }
        return false;
    }

    /** Insert Document mới (thường dùng cho Other_*). */
    @Override
    public boolean insertDocument(int profileId, String documentType, String documentUrl, String notes) {
        String insertSql = """
                INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            Integer typeId = resolveDocumentTypeId(documentType);
            if (typeId == null) {
                return false;
            }
            ps.setInt(1, typeId);
            ps.setString(2, documentUrl);
            ps.setString(3, notes);
            ps.setInt(4, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Thêm tài liệu thất bại: {0}", e.getMessage());
        }
        return false;
    }

    /** Cập nhật Notes/trạng thái duyệt theo loại tài liệu. */
    @Override
    public boolean updateDocumentNotes(int profileId, String documentType, String notes) {
        String sql = """
                UPDATE Document
                SET Notes = ?
                WHERE ProfileId = ? AND DocumentTypeId = (
                    SELECT DocumentTypeId FROM DocumentType WHERE Type = ?
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, truncateNotes(notes));
            ps.setInt(2, profileId);
            ps.setString(3, RegistrantDocumentTypeMapping.toDbType(documentType));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Cập nhật ghi chú tài liệu thất bại: {0}", e.getMessage());
        }
        return false;
    }

    /** Gửi duyệt hồ sơ chính: gắn #PENDING# lên mọi tệp đã upload chưa #APPROVED# (RegistrationStatus cập nhật ở service). */
    @Override
    public boolean requestApproval(int profileId, String requestNote) {
        List<RegistrantDocumentView> docs = listByProfileId(profileId);
        boolean updated = false;
        for (RegistrantDocumentView doc : docs) {
            if (!hasUploadedFile(doc)) {
                continue;
            }
            if (isApproved(doc.getNotes())) {
                continue;
            }
            String merged = mergePendingRequestNote(doc.getNotes(), requestNote);
            if (updateDocumentNotes(profileId, doc.getDocumentType(), merged)) {
                updated = true;
            }
        }
        return updated;
    }

    /** Đồng bộ Notes Other theo ER bổ sung đã đóng; legacy #PENDING# chưa gắn ER thì lấy ER đóng gần nhất — trả số doc cập nhật. */
    @Override
    public int reconcileOtherDocumentsWithSupplementEr(int profileId,
            Map<Integer, String> supplementErStatuses) {
        if (profileId <= 0 || supplementErStatuses == null || supplementErStatuses.isEmpty()) {
            return 0;
        }
        List<RegistrantDocumentView> docs = listByProfileId(profileId);
        int updated = 0;
        boolean hasOpenPendingSupplement = supplementErStatuses.values().stream()
                .anyMatch(st -> ProfileRegistrationStatus.PENDING.equalsIgnoreCase(st));
        Integer latestClosedErId = findLatestClosedSupplementErId(supplementErStatuses);

        for (RegistrantDocumentView doc : docs) {
            if (!isOtherType(doc.getDocumentType()) || !hasUploadedFile(doc)) {
                continue;
            }
            if (!isPendingReview(doc.getNotes())) {
                continue;
            }
            Integer linkedEr = RegistrantDocumentHelper.parseSupplementErId(doc.getNotes());
            String erStatus = linkedEr != null ? supplementErStatuses.get(linkedEr) : null;
            if (linkedEr != null && erStatus != null) {
                if (applySupplementErStatusToDocument(profileId, doc, erStatus)) {
                    updated++;
                }
                continue;
            }
            // Legacy: chưa gắn marker → suy từ ER bổ sung đã đóng gần nhất
            if (linkedEr == null && !hasOpenPendingSupplement && latestClosedErId != null) {
                String closedStatus = supplementErStatuses.get(latestClosedErId);
                if (ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(closedStatus)
                        || ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(closedStatus)) {
                    if (applySupplementErStatusToDocument(profileId, doc, closedStatus)) {
                        updated++;
                    }
                }
            }
        }
        return updated;
    }

    private boolean applySupplementErStatusToDocument(int profileId, RegistrantDocumentView doc,
            String erStatus) {
        if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(erStatus)
                && !ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(erStatus)) {
            return false;
        }
        String newNotes = ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(erStatus)
                ? mergeApprovedNote(doc.getNotes())
                : buildRejectNote("Hồ sơ bổ sung không đạt yêu cầu.");
        return updateDocumentNotes(profileId, doc.getDocumentType(), newNotes);
    }

    private static Integer findLatestClosedSupplementErId(Map<Integer, String> supplementErStatuses) {
        Integer latest = null;
        for (Map.Entry<Integer, String> entry : supplementErStatuses.entrySet()) {
            String status = entry.getValue();
            if (!ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)
                    && !ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status)) {
                continue;
            }
            int erId = entry.getKey();
            if (latest == null || erId > latest) {
                latest = erId;
            }
        }
        return latest;
    }

    /** Sinh DocumentType unique Other_uuid8 cho upload bổ sung. */
    public static String newOtherDocumentType() {
        return "Other_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /** Other hoặc Other_xxxxxxxx đều là hồ sơ bổ sung. */
    public static boolean isOtherType(String documentType) {
        return documentType != null && (documentType.equals("Other") || documentType.startsWith("Other_"));
    }

    /** Dựng Notes mặc định khi thí sinh upload. */
    public static String buildUploadNote(String documentType, String reason, long sizeBytes, String originalName) {
        return buildUploadNote(documentType, reason, sizeBytes, originalName, null);
    }

    /** Dựng Notes upload; gắn #LICENCE# nếu Other kèm hạng. */
    public static String buildUploadNote(String documentType, String reason, long sizeBytes, String originalName,
            String supplementLicenceCode) {
        String meta = formatFileMeta(sizeBytes, originalName);
        String body;
        if (isOtherType(documentType) && reason != null && !reason.isBlank()) {
            body = truncateNotes("Lý do: " + reason.trim() + meta);
        } else {
            body = truncateNotes("Thí sinh tải lên " + documentType + meta);
        }
        if (isOtherType(documentType) && supplementLicenceCode != null && !supplementLicenceCode.isBlank()) {
            return encodeLicenceMarker(supplementLicenceCode) + body;
        }
        return body;
    }

    /** Mã hóa #LICENCE#<mã hạng># để gắn Notes. */
    public static String encodeLicenceMarker(String uiLicenceCode) {
        if (uiLicenceCode == null || uiLicenceCode.isBlank()) {
            return "";
        }
        return MARK_LICENCE_PREFIX + uiLicenceCode.trim().toUpperCase(java.util.Locale.ROOT) + "#";
    }

    /** Giải mã mã hạng UI từ marker #LICENCE# trong Notes. */
    private static String parseLicenceCode(String notes) {
        if (notes == null || !notes.contains(MARK_LICENCE_PREFIX)) {
            return null;
        }
        int start = notes.indexOf(MARK_LICENCE_PREFIX) + MARK_LICENCE_PREFIX.length();
        int end = notes.indexOf('#', start);
        if (end <= start) {
            return null;
        }
        return notes.substring(start, end).trim();
    }

    /** Gỡ marker #LICENCE#…# khỏi chuỗi Notes. */
    public static String stripLicenceMarker(String notes) {
        if (notes == null || notes.isBlank()) {
            return notes;
        }
        String code = parseLicenceCode(notes);
        if (code == null) {
            return notes;
        }
        return notes.replace(encodeLicenceMarker(code), "").trim();
    }

    /** Chuỗi meta dung lượng + tên tệp gốc cho Notes. */
    private static String formatFileMeta(long sizeBytes, String originalName) {
        StringBuilder meta = new StringBuilder(" · ");
        meta.append(formatSize(sizeBytes));
        if (originalName != null && !originalName.isBlank()) {
            meta.append(" · ").append(originalName.trim());
        }
        return meta.toString();
    }

    /** Định dạng byte thành B/KB/MB dễ đọc. */
    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return String.format(java.util.Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private RegistrantDocumentView mapRow(ResultSet rs) throws SQLException {
        RegistrantDocumentView view = new RegistrantDocumentView();
        try {
            view.setDocumentId(rs.getInt("DocumentId"));
        } catch (SQLException ignored) {
        }
        view.setDocumentType(RegistrantDocumentTypeMapping.toUiType(rs.getString("DocumentType")));
        view.setDocumentUrl(rs.getString("DocumentUrl"));
        view.setNotes(rs.getString("Notes"));
        enrichFileMeta(view);
        enrichSupplementLicence(view);
        applyStatusFromNotes(view);
        return view;
    }

    private static void enrichSupplementLicence(RegistrantDocumentView view) {
        String notes = view.getNotes();
        if (notes == null) {
            return;
        }
        view.setSupplementLicenceCode(parseLicenceCode(notes));
        String stripped = stripLicenceMarker(notes);
        int metaIdx = stripped.indexOf(" · ");
        String reasonLine = metaIdx >= 0 ? stripped.substring(0, metaIdx) : stripped;
        if (reasonLine.startsWith("Lý do: ")) {
            reasonLine = reasonLine.substring("Lý do: ".length());
        }
        view.setReasonSummary(reasonLine.trim());
    }

    private static void enrichFileMeta(RegistrantDocumentView view) {
        String url = view.getDocumentUrl();
        if (url != null && !url.isBlank()) {
            int slash = Math.max(url.lastIndexOf('/'), url.lastIndexOf('\\'));
            view.setFileName(slash >= 0 ? url.substring(slash + 1) : url);
        }
        String notes = view.getNotes();
        if (notes == null) {
            return;
        }
        String metaSource = stripLicenceMarker(notes);
        int metaIdx = metaSource.indexOf(" · ");
        if (metaIdx < 0) {
            return;
        }
        String meta = metaSource.substring(metaIdx + 3);
        String[] parts = meta.split(" · ");
        if (parts.length >= 1 && !parts[0].isBlank()) {
            view.setFileSizeLabel(parts[0].trim());
        }
        if (parts.length >= 2 && !parts[1].isBlank()) {
            view.setFileName(stripInternalMarkers(parts[1].trim()));
        }
    }

    /** Loại marker nội bộ (#PENDING#, #APPROVED#) khỏi chuỗi hiển thị. */
    public static String stripInternalMarkers(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String cleaned = text
                .replace(MARK_PENDING, "")
                .replace(MARK_APPROVED, "")
                .replace("Gửi yêu cầu duyệt hồ sơ.", "")
                .replace("Ban quản lý đã duyệt.", "");
        int pipe = cleaned.indexOf('|');
        if (pipe >= 0) {
            cleaned = cleaned.substring(0, pipe);
        }
        return cleaned.replaceAll("\\s{2,}", " ").trim();
    }

    private static String mergeApprovedNote(String existingNotes) {
        String base = existingNotes != null ? existingNotes.trim() : "";
        int pendingIdx = base.indexOf(MARK_PENDING);
        if (pendingIdx >= 0) {
            base = base.substring(0, pendingIdx).trim();
        }
        base = base.replace("Gửi yêu cầu duyệt hồ sơ.", "").trim();
        while (base.endsWith("|")) {
            base = base.substring(0, base.length() - 1).trim();
        }
        if (base.contains(MARK_APPROVED)) {
            return truncateNotes(base);
        }
        if (base.isBlank()) {
            return truncateNotes(MARK_APPROVED + " Ban quản lý đã duyệt.");
        }
        return truncateNotes(base + " | " + MARK_APPROVED + " Ban quản lý đã duyệt.");
    }

    private static void applyStatusFromNotes(RegistrantDocumentView view) {
        if (!hasUploadedFile(view)) {
            view.setStatusClass("pending");
            view.setStatusLabel("Chưa tải lên");
            return;
        }

        String notes = view.getNotes();
        if (isApproved(notes)) {
            view.setStatusClass("success");
            view.setStatusLabel("Đã duyệt");
            return;
        }
        if (isRejected(notes)) {
            view.setStatusClass("danger");
            view.setStatusLabel("Yêu cầu bổ sung");
            return;
        }
        if (isPendingReview(notes)) {
            view.setStatusClass("warning");
            view.setStatusLabel("Chờ duyệt");
            return;
        }

        view.setStatusClass("warning");
        view.setStatusLabel("Chưa gửi duyệt");
    }

    private static boolean hasUploadedFile(RegistrantDocumentView view) {
        return view.getDocumentUrl() != null && !view.getDocumentUrl().isBlank();
    }

    /** Approved = có #APPROVED# và không còn #PENDING# (đã qua vòng review). */
    public static boolean isApproved(String notes) {
        return notes != null
                && notes.contains(MARK_APPROVED)
                && !notes.contains(MARK_PENDING);
    }

    /** Pending review = Notes còn chứa #PENDING#. */
    public static boolean isPendingReview(String notes) {
        return notes != null && notes.contains(MARK_PENDING);
    }

    private static boolean isRejected(String notes) {
        if (notes == null) {
            return false;
        }
        String lower = notes.toLowerCase();
        return lower.contains("từ chối") || lower.contains("reject");
    }

    private static String mergePendingRequestNote(String existingNotes, String requestNote) {
        String base = existingNotes;
        if (base == null || base.isBlank()) {
            base = "";
        }
        if (!base.contains(MARK_PENDING)) {
            base = (base.isBlank() ? "" : base + " | ") + MARK_PENDING + " Gửi yêu cầu duyệt hồ sơ.";
        }
        if (requestNote != null && !requestNote.isBlank()) {
            String suffix = " Yêu cầu: " + requestNote.trim();
            if (!base.contains(suffix.trim())) {
                base = base + suffix;
            }
        }
        return truncateNotes(base);
    }

    private static String buildRejectNote(String staffNote) {
        String reason = staffNote != null && !staffNote.isBlank()
                ? staffNote.trim()
                : "Không đạt yêu cầu";
        return truncateNotes("Từ chối: " + reason);
    }

    private static String truncateNotes(String notes) {
        if (notes == null) {
            return null;
        }
        return notes.length() <= 255 ? notes : notes.substring(0, 252) + "...";
    }

    /** Truy vấn cục bộ — tránh phụ thuộc vòng RegistrantDAOImpl ↔ DocumentDAOImpl. */
    private String resolveLatestLicenceClassByProfileId(int profileId) {
        String sql = """
                SELECT TOP 1 l.LicenceClass
                FROM ExamRegistration er
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE er.ProfileId = ?
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass"));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hạng GPLX hồ sơ {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return "B2";
    }

    private Integer findDocumentId(int profileId, String documentType) {
        String sql = """
                SELECT TOP 1 d.DocumentId
                FROM Document d
                INNER JOIN DocumentType dt ON dt.DocumentTypeId = d.DocumentTypeId
                WHERE d.ProfileId = ? AND dt.Type = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setString(2, RegistrantDocumentTypeMapping.toDbType(documentType));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DocumentId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tra được DocumentId: {0}", e.getMessage());
        }
        return null;
    }
}
