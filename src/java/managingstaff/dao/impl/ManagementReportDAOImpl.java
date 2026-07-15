package managingstaff.dao.impl;

import managingstaff.dao.ManagementReportDAO;
import shared.dbconnection.DBContext;
import managingstaff.dto.ManagementReportExamOptionDTO;
import managingstaff.dto.ManagementReportRowDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagementReportDAOImpl extends DBContext implements ManagementReportDAO {

    @Override
    public List<ManagementReportRowDTO> findReportRows(
            String periodGroup, int examId, int year, String licenceClass) {
        List<ManagementReportRowDTO> rows = new ArrayList<>();
        String group = switch (periodGroup) {
            case "month", "year" -> periodGroup;
            default -> "exam";
        };

        String labelExpression = switch (group) {
            case "month" -> "N'Tháng ' + RIGHT(N'0' + CONVERT(nvarchar(2), MONTH(o.ExamDate)), 2)"
                    + " + N'/' + CONVERT(nvarchar(4), YEAR(o.ExamDate))";
            case "year" -> "N'Năm ' + CONVERT(nvarchar(4), YEAR(o.ExamDate))";
            default -> "o.ExamCode + N' - ' + CONVERT(nvarchar(10), CAST(o.ExamDate AS date), 103)";
        };
        String sortExpression = switch (group) {
            case "month" -> "DATEFROMPARTS(YEAR(o.ExamDate), MONTH(o.ExamDate), 1)";
            case "year" -> "DATEFROMPARTS(YEAR(o.ExamDate), 1, 1)";
            default -> "CAST(o.ExamDate AS date)";
        };

        StringBuilder filters = new StringBuilder(
                " WHERE l.LicenceClass IN ('A1','A','B1')"
                + " AND CAST(e.ExamDate AS date) < CAST(GETDATE() AS date)"
                + " AND e.[Status] NOT IN ('Cancelled',N'Đã hủy')");
        List<Object> parameters = new ArrayList<>();
        if ("exam".equals(group) && examId > 0) {
            filters.append(" AND e.ExamId = ?");
            parameters.add(examId);
        }
        if ("month".equals(group) && year > 0) {
            filters.append(" AND YEAR(e.ExamDate) = ?");
            parameters.add(year);
        }
        if (licenceClass != null && !licenceClass.isBlank()) {
            filters.append(" AND l.LicenceClass = ?");
            parameters.add(licenceClass);
        }

        String sql = """
                WITH CandidateAggregate AS (
                    SELECT e.ExamId, e.ExamCode, e.ExamDate, l.LicenceClass,
                           c.CandidateId,
                           MAX(CASE WHEN c.IsAbsent = 1 THEN 1 ELSE 0 END) AS IsAbsent,
                           COUNT(DISTINCT ee.ExamEnrollmentId) AS AssignmentCount,
                           COUNT(DISTINCT er.ExamResultId) AS ResultCount,
                           MIN(CASE WHEN er.ExamResultId IS NULL THEN NULL
                                    ELSE CAST(er.IsPassed AS int) END) AS MinimumResult
                    FROM Exam e
                    JOIN Licence l ON l.LicenceId = e.LicenceId
                    LEFT JOIN ExamEnrollment ee ON ee.ExamId = e.ExamId
                    LEFT JOIN Candidate c ON c.CandidateId = ee.CandidateId
                    LEFT JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                %s
                    GROUP BY e.ExamId, e.ExamCode, e.ExamDate, l.LicenceClass, c.CandidateId
                ), Outcomes AS (
                    SELECT ExamId, ExamCode, ExamDate, LicenceClass, CandidateId, IsAbsent,
                           CASE WHEN IsAbsent = 0 AND AssignmentCount > 0
                                     AND ResultCount >= AssignmentCount AND MinimumResult = 1
                                THEN 1 ELSE 0 END AS IsPassed,
                           CASE WHEN IsAbsent = 1 OR MinimumResult = 0
                                THEN 1 ELSE 0 END AS IsFailed
                    FROM CandidateAggregate
                    WHERE CandidateId IS NOT NULL
                )
                SELECT %s AS PeriodLabel,
                       o.LicenceClass,
                       COUNT(*) AS TotalCount,
                       0 AS AbsentCount,
                       SUM(o.IsPassed) AS PassCount,
                       SUM(o.IsFailed) AS FailCount,
                       0 AS PendingCount,
                       %s AS PeriodSort
                FROM Outcomes o
                WHERE o.IsPassed = 1 OR o.IsFailed = 1
                GROUP BY %s, %s, o.LicenceClass
                ORDER BY PeriodSort, o.LicenceClass
                """.formatted(filters, labelExpression, sortExpression,
                        labelExpression, sortExpression);

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ManagementReportRowDTO row = new ManagementReportRowDTO();
                    row.setPeriodLabel(rs.getString("PeriodLabel"));
                    row.setLicenceClass(rs.getString("LicenceClass"));
                    row.setTotalCount(rs.getInt("TotalCount"));
                    row.setAbsentCount(rs.getInt("AbsentCount"));
                    row.setPassCount(rs.getInt("PassCount"));
                    row.setFailCount(rs.getInt("FailCount"));
                    row.setPendingCount(rs.getInt("PendingCount"));
                    rows.add(row);
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải dữ liệu báo cáo thống kê", ex);
        }
    }

    @Override
    public List<ManagementReportExamOptionDTO> findExamOptions() {
        List<ManagementReportExamOptionDTO> exams = new ArrayList<>();
        String sql = """
                SELECT e.ExamId, e.ExamCode, e.ExamDate, l.LicenceClass
                FROM Exam e
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE l.LicenceClass IN ('A1','A','B1')
                  AND CAST(e.ExamDate AS date) < CAST(GETDATE() AS date)
                  AND e.[Status] NOT IN ('Cancelled',N'Đã hủy')
                ORDER BY e.ExamDate DESC, e.ExamId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ManagementReportExamOptionDTO option = new ManagementReportExamOptionDTO();
                option.setExamId(rs.getInt("ExamId"));
                option.setExamCode(rs.getString("ExamCode"));
                option.setExamDate(rs.getTimestamp("ExamDate"));
                option.setLicenceClass(rs.getString("LicenceClass"));
                exams.add(option);
            }
            return exams;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách kỳ thi", ex);
        }
    }

    @Override
    public List<Integer> findAvailableYears() {
        List<Integer> years = new ArrayList<>();
        String sql = """
                SELECT DISTINCT YEAR(e.ExamDate) AS ExamYear
                FROM Exam e
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE l.LicenceClass IN ('A1','A','B1')
                  AND CAST(e.ExamDate AS date) < CAST(GETDATE() AS date)
                  AND e.[Status] NOT IN ('Cancelled',N'Đã hủy')
                ORDER BY ExamYear DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("ExamYear"));
            }
            return years;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách năm báo cáo", ex);
        }
    }
}
