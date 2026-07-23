package examstaff.dao.impl;

import examstaff.dao.Db2ExamSchemaSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hỗ trợ thao tác {@code ExamEnrollmentSection} trên DLEM_DB_2 — <b>Approach B</b>.
 *
 * Vì sao tách Theory / Practical (không CSV splice)?:
 * Trước đây helper nhận chuỗi CSV {@code sectionTypes} rồi ghép vào SQL → khó đọc,
 * dễ nhầm LT/TH. Hiện mỗi method có SQL đầy đủ + {@link Db2ExamSchemaSql#THEORY_SECTION_TYPES}
 * hoặc {@link Db2ExamSchemaSql#PRACTICAL_SECTION_TYPES} nhúng tường minh.
 *
 * Hai lớp bảng liên quan:
 * <pre>
 *   Exam ──&lt; ExamSection (SectionType = LT | TH | …)
 *   Candidate ──&lt; ExamEnrollment ──&lt; ExamEnrollmentSection
 *                                      ├── ExamSectionId
 *                                      ├── ExamAreaId   (phòng / sân đã phân)
 *                                      └── Status       (Pending / … / AwaitingSignature)
 * </pre>
 *
 * Nhóm API:
 * - <b>find*SectionId</b> — tra {@code ExamSectionId} theo kỳ hoặc theo enrollment
 * - <b>updateTheoryAllocation</b> — ghi phòng LT lên section + {@code ExamEnrollment.AllocatedExamAreaId}
 * - <b>updatePracticalAllocation</b> — ghi sân TH chỉ trên section TH
 * - <b>resetTheoryStatus</b> — về {@code Pending} sau hủy đánh vắng
 * <p>Package-private: chỉ {@code ExamRegistrationDAOImpl} (và DAO cùng package) gọi.
 * Caller mở {@link Connection}; class này không quản lý transaction.
 */
final class ExamEnrollmentSectionSupport {

    private ExamEnrollmentSectionSupport() {
    }

    /**
     * Tìm {@code ExamSectionId} phần lý thuyết của kỳ thi.
     * <p>
     * Dùng khi cần biết section LT tồn tại trước khi tạo/cập nhật enrollment section.
     * @param conn   connection đang mở
     * @param examId mã kỳ thi
     * @return ExamSectionId hoặc {@code null} nếu kỳ chưa có section LT
     */
    static Integer findTheorySectionId(Connection conn, int examId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE ExamId = ?
                  AND SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                ORDER BY ExamSectionId
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Tìm {@code ExamSectionId} phần thực hành của kỳ thi
     * (sa hình / trên đường / Practical / TH…).
     * @param conn   connection đang mở
     * @param examId mã kỳ thi
     * @return ExamSectionId hoặc {@code null}
     */
    static Integer findPracticalSectionId(Connection conn, int examId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE ExamId = ?
                  AND SectionType IN (""" + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
                )
                ORDER BY ExamSectionId
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Section LT đã gắn với một {@code ExamEnrollment}
     * (qua {@code ExamEnrollmentSection} JOIN {@code ExamSection}).
     * @param conn             connection đang mở
     * @param examEnrollmentId mã ghi danh
     * @return ExamSectionId hoặc {@code null}
     */
    static Integer findTheorySectionIdForEnrollment(Connection conn, int examEnrollmentId)
            throws SQLException {
        String sql = """
                SELECT TOP 1 ees.ExamSectionId
                FROM ExamEnrollmentSection ees
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                ORDER BY ees.ExamEnrollmentId
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Section TH đã gắn với một {@code ExamEnrollment}.
     * @param conn             connection đang mở
     * @param examEnrollmentId mã ghi danh
     * @return ExamSectionId hoặc {@code null}
     */
    static Integer findPracticalSectionIdForEnrollment(Connection conn, int examEnrollmentId)
            throws SQLException {
        String sql = """
                SELECT TOP 1 ees.ExamSectionId
                FROM ExamEnrollmentSection ees
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
                )
                ORDER BY ees.ExamEnrollmentId
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Phân phòng lý thuyết cho một thí sinh trong kỳ.
     * <p>
     * <b>Bước 1:</b> UPDATE {@code ExamEnrollmentSection} (LT) — set {@code ExamAreaId},
     * clear device, stamp {@code AllocatedAt}.<br>
     * <b>Bước 2:</b> nếu bước 1 thành công, UPDATE {@code ExamEnrollment.AllocatedExamAreaId}
     * (cột “phòng chính” dùng UI/legacy) và clear {@code ExamDeviceId}.
     * @return {@code true} nếu có ít nhất một section LT được cập nhật
     */
    static boolean updateTheoryAllocation(Connection conn, int candidateId, int examId, int areaId)
            throws SQLException {
        String sql = """
                UPDATE ees SET ees.ExamAreaId = ?, ees.ExamDeviceId = NULL, ees.AllocatedAt = GETDATE()
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, areaId);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                try (PreparedStatement eePs = conn.prepareStatement(
                        "UPDATE ExamEnrollment SET AllocatedExamAreaId = ?, ExamDeviceId = NULL "
                                + "WHERE CandidateId = ? AND ExamId = ?")) {
                    eePs.setInt(1, areaId);
                    eePs.setInt(2, candidateId);
                    eePs.setInt(3, examId);
                    eePs.executeUpdate();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Phân khu vực thực hành: chỉ UPDATE section TH ({@code ExamAreaId}).
     * Không đụng {@code ExamEnrollment.AllocatedExamAreaId} (cột đó dành phòng LT).
     * @return {@code true} nếu có section TH được cập nhật
     */
    static boolean updatePracticalAllocation(Connection conn, int candidateId, int examId, int areaId)
            throws SQLException {
        String sql = """
                UPDATE ees SET ees.ExamAreaId = ?, ees.ExamDeviceId = NULL, ees.AllocatedAt = GETDATE()
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, areaId);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reset {@code Status} phần lý thuyết về {@code Pending} và clear {@code CompletedAt}.
     * Gọi sau khi hủy đánh dấu vắng — thí sinh quay lại hàng đợi thủ tục.
     * @param candidateId mã thí sinh (mọi enrollment LT của candidate)
     */
    static void resetTheoryStatus(Connection conn, int candidateId) throws SQLException {
        String sql = """
                UPDATE ees SET ees.Status = N'Pending', ees.CompletedAt = NULL
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
        }
    }
}
