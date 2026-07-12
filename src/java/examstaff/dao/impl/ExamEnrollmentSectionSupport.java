package examstaff.dao.impl;

import examstaff.dao.Db2ExamSchemaSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/** Thao t�c ExamEnrollmentSection (schema DLEM_DB_2). */
final class ExamEnrollmentSectionSupport {

    private ExamEnrollmentSectionSupport() {
    }

    static void ensureSections(Connection conn, int examEnrollmentId, int examId,
            Boolean takeTheory, Boolean takePractical) throws SQLException {
        insertSectionIfNeeded(conn, examEnrollmentId, examId, Db2ExamSchemaSql.THEORY_SECTION_TYPES, takeTheory);
        insertSectionIfNeeded(conn, examEnrollmentId, examId, Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES, takePractical);
    }

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
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

    private static boolean sectionRowExists(Connection conn, int examEnrollmentId, int sectionId)
            throws SQLException {
        String sql = """
                SELECT 1 FROM ExamEnrollmentSection
                WHERE ExamEnrollmentId = ? AND ExamSectionId = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertSectionRow(Connection conn, int examEnrollmentId, int sectionId)
            throws SQLException {
        String sql = """
                INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
                VALUES (?, ?, N'Pending')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    static Integer findTheoryEnrollmentSectionId(Connection conn, int candidateId, int examId)
            throws SQLException {
        return findEnrollmentSectionId(conn, candidateId, examId, Db2ExamSchemaSql.THEORY_SECTION_TYPES);
    }

    static Integer findSectionId(Connection conn, int examId, String sectionTypesCsv) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE ExamId = ? AND SectionType IN (""" + sectionTypesCsv + """
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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentSectionId");
                }
            }
        }
        return null;
    }

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Status");
                }
            }
        }
        return null;
    }

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            return ps.executeUpdate() > 0;
        }
    }

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            return ps.executeUpdate() > 0;
        }
    }

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

    static int ensureTheoryPaper(Connection conn, int theoryEnrollmentSectionId, int deviceId)
            throws SQLException {
        String check = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentSectionId = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, theoryEnrollmentSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TheoryPaperId");
                }
            }
        }
        String ins = "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, theoryEnrollmentSectionId);
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
