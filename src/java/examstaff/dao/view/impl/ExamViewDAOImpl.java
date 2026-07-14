package examstaff.dao.view.impl;

import examstaff.dao.view.ExamViewDAO;
import shared.dbconnection.DBContext;
import examstaff.dto.view.ExamSummaryRow;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model kỳ thi (một hàng / Exam).
 * {@code examId} trên view = {@code ExamId} để tương thích UI cũ.
 */
public class ExamViewDAOImpl extends DBContext implements ExamViewDAO {

    /** SELECT tóm tắt kỳ thi + hạng GPLX. */
    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS examId, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Hạng ' + l.LicenceClass + N' — ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName, "
            + "1 AS examTypeId, "
            + "CAST(e.ExamDate AS DATE) AS examDate, "
            + "CAST(e.StartTime AS TIME) AS shiftStartTime, "
            + "CAST(e.EndTime AS TIME) AS shiftEndTime, "
            + "e.StartTime AS scheduledStartAt, "
            + "e.EndTime AS scheduledEndAt, "
            + "e.[Status] AS status, "
            + "e.StartTime AS createdAt, "
            + "l.LicenceClass AS licenseCode, "
            + "e.ExamCode AS examCode, "
            + "N'Lý thuyết + Thực hành' AS examTypeName "
            + "FROM Exam e "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId";

    /** {@inheritDoc} */
    @Override
    public List<ExamSummaryRow> findAllOrdered() {
        return fetchList(EXAM_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, e.StartTime DESC");
    }

    /** {@inheritDoc} */
    @Override
    public ExamSummaryRow findByExamId(int examId) {
        if (examId <= 0) {
            return null;
        }
        return fetchOne(EXAM_SELECT + " WHERE e.ExamId = ?", examId);
    }

    /** Chạy SELECT không tham số, map toàn bộ hàng. */
    private List<ExamSummaryRow> fetchList(String sql) {
        List<ExamSummaryRow> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Chạy SELECT một hàng theo {@code examId}. */
    private ExamSummaryRow fetchOne(String sql, int examId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Ánh xạ ResultSet → {@link ExamSummaryRow}. */
    private static ExamSummaryRow mapRow(ResultSet rs) throws SQLException {
        ExamSummaryRow row = new ExamSummaryRow();
        row.setExamId(rs.getInt("examId"));
        row.setExamName(rs.getString("examName"));
        row.setExamTypeId(rs.getInt("examTypeId"));
        row.setExamDate(rs.getDate("examDate"));
        row.setShiftStartTime(rs.getTime("shiftStartTime"));
        row.setShiftEndTime(rs.getTime("shiftEndTime"));
        Timestamp scheduledStart = rs.getTimestamp("scheduledStartAt");
        row.setScheduledStartAt(scheduledStart);
        row.setScheduledEndAt(rs.getTimestamp("scheduledEndAt"));
        row.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        row.setCreatedAt(createdAt != null ? createdAt : scheduledStart);
        row.setLicenseCode(rs.getString("licenseCode"));
        row.setExamCode(rs.getString("examCode"));
        row.setExamTypeName(rs.getString("examTypeName"));
        return row;
    }
}
