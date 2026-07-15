package examstaff.dao.impl;

import examstaff.dao.Db2ExamSchemaSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Lớp hỗ trợ CRUD {@code ExamEnrollmentSection} trên schema DLEM_DB_2.
 * Cung cấp các thao tác phân phòng lý thuyết/thực hành, trạng thái section, và TheoryPaper.
 */
final class ExamEnrollmentSectionSupport {

    /** Không cho khởi tạo — chỉ dùng các phương thức static. */
    private ExamEnrollmentSectionSupport() {
    }

    /**
     * Đảm bảo có dòng {@code ExamEnrollmentSection} cho phần lý thuyết và thực hành
     * theo cờ {@code takeTheory}/{@code takePractical}.
     *
     * @param conn             kết nối JDBC dùng chung transaction
     * @param examEnrollmentId mã ghi danh
     * @param examId           mã kỳ thi
     * @param takeTheory       cờ thi lý thuyết; {@code false} bỏ qua LT
     * @param takePractical    cờ thi thực hành; {@code false} bỏ qua TH
     * @throws SQLException nếu truy vấn/ghi CSDL thất bại
     */
    static void ensureSections(Connection conn, int examEnrollmentId, int examId,
            Boolean takeTheory, Boolean takePractical) throws SQLException {
        insertSectionIfNeeded(conn, examEnrollmentId, examId, Db2ExamSchemaSql.THEORY_SECTION_TYPES, takeTheory);
        insertSectionIfNeeded(conn, examEnrollmentId, examId, Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES, takePractical);
    }

    /**
     * Insert {@code ExamEnrollmentSection} nếu cờ {@code takeFlag} cho phép và chưa tồn tại.
     * SELECT {@code ExamSectionId} từ {@code ExamSection} theo {@code examId} và loại section.
     *
     * @param conn             kết nối JDBC
     * @param examEnrollmentId mã ghi danh
     * @param examId           mã kỳ thi
     * @param sectionTypesCsv  danh sách SectionType (CSV cho IN clause)
     * @param takeFlag         cờ có thi phần này hay không
     * @throws SQLException nếu truy vấn/ghi thất bại
     */
    private static void insertSectionIfNeeded(Connection conn, int examEnrollmentId, int examId,
            String sectionTypesCsv, Boolean takeFlag) throws SQLException {
        if (takeFlag != null && !takeFlag) {
            return;
        }
        String sql = """
                SELECT es.ExamSectionId
                FROM ExamSection es
                WHERE es.ExamId = ?
                  AND es.SectionType IN (""" + sectionTypesCsv + """
                )
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamSectionId theo kỳ thi
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sectionId = rs.getInt("ExamSectionId");
                    if (!sectionRowExists(conn, examEnrollmentId, sectionId)) {
                        insertSectionRow(conn, examEnrollmentId, sectionId);
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra đã có dòng {@code ExamEnrollmentSection} cho cặp enrollment/section chưa.
     *
     * @param conn             kết nối JDBC
     * @param examEnrollmentId mã ghi danh
     * @param sectionId        mã phần thi ({@code ExamSectionId})
     * @return {@code true} nếu đã tồn tại
     * @throws SQLException nếu truy vấn thất bại
     */
    private static boolean sectionRowExists(Connection conn, int examEnrollmentId, int sectionId)
            throws SQLException {
        String sql = """
                SELECT 1 FROM ExamEnrollmentSection
                WHERE ExamEnrollmentId = ? AND ExamSectionId = ?
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT kiểm tra tồn tại
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, sectionId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm dòng {@code ExamEnrollmentSection} với {@code Status='Pending'}.
     *
     * @param conn             kết nối JDBC
     * @param examEnrollmentId mã ghi danh
     * @param sectionId        mã phần thi
     * @throws SQLException nếu INSERT thất bại
     */
    private static void insertSectionRow(Connection conn, int examEnrollmentId, int sectionId)
            throws SQLException {
        String sql = """
                INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
                VALUES (?, ?, N'Pending')
                """;
        // Chuẩn bị PreparedStatement với SQL INSERT ExamEnrollmentSection
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, sectionId);
            // Thực thi INSERT
            ps.executeUpdate();
        }
    }

    /**
     * Tìm {@code ExamEnrollmentSectionId} của phần lý thuyết theo thí sinh và kỳ thi.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return mã enrollment section hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    static Integer findTheoryEnrollmentSectionId(Connection conn, int candidateId, int examId)
            throws SQLException {
        return findEnrollmentSectionId(conn, candidateId, examId, Db2ExamSchemaSql.THEORY_SECTION_TYPES);
    }

    /**
     * Tìm {@code ExamSectionId} theo kỳ thi và danh sách {@code SectionType}.
     *
     * @param conn            kết nối JDBC
     * @param examId          mã kỳ thi
     * @param sectionTypesCsv danh sách SectionType (CSV)
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    static Integer findSectionId(Connection conn, int examId, String sectionTypesCsv) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE ExamId = ? AND SectionType IN (""" + sectionTypesCsv + """
                )
                ORDER BY ExamSectionId
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamSectionId
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Tìm {@code ExamSectionId} đã gắn với một {@code ExamEnrollment} cụ thể.
     *
     * @param conn             kết nối JDBC
     * @param examEnrollmentId mã ghi danh
     * @param sectionTypesCsv  danh sách SectionType (CSV)
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    static Integer findSectionIdForEnrollment(Connection conn, int examEnrollmentId, String sectionTypesCsv)
            throws SQLException {
        String sql = """
                SELECT TOP 1 ees.ExamSectionId
                FROM ExamEnrollmentSection ees
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + sectionTypesCsv + """
                )
                ORDER BY ees.ExamEnrollmentId
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT section theo enrollment
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examEnrollmentId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Tìm {@code ExamEnrollmentSectionId} theo thí sinh, kỳ thi và loại section.
     *
     * @param conn            kết nối JDBC
     * @param candidateId     mã thí sinh
     * @param examId          mã kỳ thi
     * @param sectionTypesCsv danh sách SectionType (CSV)
     * @return mã enrollment section hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private static Integer findEnrollmentSectionId(Connection conn, int candidateId, int examId,
            String sectionTypesCsv) throws SQLException {
        String sql = """
                SELECT TOP 1 ees.ExamEnrollmentSectionId
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND es.SectionType IN (""" + sectionTypesCsv + """
                )
                ORDER BY ees.ExamEnrollmentSectionId
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamEnrollmentSectionId
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentSectionId");
                }
            }
        }
        return null;
    }

    /**
     * Đọc {@code Status} phần lý thuyết của thí sinh trong kỳ thi.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return chuỗi trạng thái (ví dụ Pending, Completed) hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    static String getTheoryStatus(Connection conn, int candidateId, int examId) throws SQLException {
        String sql = """
                SELECT TOP 1 ees.Status
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                ORDER BY ees.ExamEnrollmentSectionId
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT Status lý thuyết
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Status");
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật {@code Status} phần lý thuyết trên {@code ExamEnrollmentSection}.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param status      trạng thái mới
     * @return {@code true} nếu UPDATE ảnh hưởng ít nhất một dòng
     * @throws SQLException nếu UPDATE thất bại
     */
    static boolean updateTheoryStatus(Connection conn, int candidateId, int examId, String status)
            throws SQLException {
        String sql = """
                UPDATE ees SET ees.Status = ?
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        // Chuẩn bị PreparedStatement với SQL UPDATE Status lý thuyết
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đánh dấu đã in chữ ký: set {@code CompletedAt} khi {@code Status='AwaitingSignature'}.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return {@code true} nếu UPDATE thành công
     * @throws SQLException nếu UPDATE thất bại
     */
    static boolean markSignaturePrinted(Connection conn, int candidateId, int examId) throws SQLException {
        String sql = """
                UPDATE ees SET ees.CompletedAt = COALESCE(ees.CompletedAt, GETDATE())
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND ees.Status = N'AwaitingSignature'
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        // Chuẩn bị PreparedStatement với SQL UPDATE CompletedAt
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Phân phòng lý thuyết: cập nhật {@code ExamAreaId} trên {@code ExamEnrollmentSection}
     * và {@code AllocatedExamAreaId} trên {@code ExamEnrollment}.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực/phòng lý thuyết
     * @return {@code true} nếu phân phòng thành công
     * @throws SQLException nếu UPDATE thất bại
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
        // Chuẩn bị PreparedStatement với SQL UPDATE phân phòng lý thuyết
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, areaId);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            // Thực thi UPDATE section
            int updated = ps.executeUpdate();
            if (updated > 0) {
                // Đồng bộ AllocatedExamAreaId trên ExamEnrollment
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
     * Phân khu vực thực hành: cập nhật {@code ExamAreaId} trên {@code ExamEnrollmentSection}.
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực/sân thực hành
     * @return {@code true} nếu UPDATE thành công
     * @throws SQLException nếu UPDATE thất bại
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
        // Chuẩn bị PreparedStatement với SQL UPDATE phân khu vực thực hành
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, areaId);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reset {@code Status} phần lý thuyết về {@code Pending} (sau hủy đánh dấu vắng).
     *
     * @param conn        kết nối JDBC
     * @param candidateId mã thí sinh
     * @throws SQLException nếu UPDATE thất bại
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
        // Chuẩn bị PreparedStatement với SQL UPDATE reset Status
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi UPDATE
            ps.executeUpdate();
        }
    }

    /**
     * Đảm bảo có bản ghi {@code TheoryPaper} cho enrollment section lý thuyết.
     * SELECT hoặc INSERT vào bảng {@code TheoryPaper}.
     *
     * @param conn                      kết nối JDBC
     * @param theoryEnrollmentSectionId mã {@code ExamEnrollmentSectionId} phần LT
     * @param deviceId                  mã thiết bị (tham số giữ tương thích API)
     * @return {@code TheoryPaperId} hoặc {@code -1} nếu thất bại
     * @throws SQLException nếu truy vấn/ghi thất bại
     */
    static int ensureTheoryPaper(Connection conn, int theoryEnrollmentSectionId, int deviceId)
            throws SQLException {
        String check = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentSectionId = ?";
        // Chuẩn bị PreparedStatement kiểm tra TheoryPaper đã tồn tại
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            // Gán tham số truy vấn
            ps.setInt(1, theoryEnrollmentSectionId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TheoryPaperId");
                }
            }
        }
        String ins = "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())";
        // Chuẩn bị PreparedStatement INSERT TheoryPaper mới
        try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            // Gán tham số truy vấn
            ps.setInt(1, theoryEnrollmentSectionId);
            // Thực thi INSERT và lấy khóa sinh
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        return -1;
    }
}
