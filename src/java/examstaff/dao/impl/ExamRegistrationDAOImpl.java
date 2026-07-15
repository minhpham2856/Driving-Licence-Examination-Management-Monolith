package examstaff.dao.impl;

import shared.dbconnection.DBContext;
import examstaff.dao.Db2CandidateSql;
import examstaff.dao.ExamRegistrationDAO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.service.impl.support.allocation.AllocationPassRules;
import examstaff.util.ExamStaffFormat;
import examstaff.service.impl.support.shared.ExamEnrollmentMerge;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai JDBC của {@link ExamRegistrationDAO} — đọc/ghi thí sinh, ghi danh,
 * phân phòng, điểm số trên các bảng {@code Candidate}, {@code ExamEnrollment},
 * {@code ExamEnrollmentSection}, {@code Payment}, {@code ExamScore}, ...
 */
public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    /**
     * Lấy đăng ký thí sinh theo mã từ view SQL {@link Db2CandidateSql#CANDIDATE_SELECT}
     * lọc {@code Candidate.CandidateId = ?}.
     *
     * @param id mã thí sinh ({@code CandidateId})
     * @return {@link ExamRegistrationDTO} hoặc {@code null} nếu không tìm thấy
     */
    @Override
    public ExamRegistrationDTO getById(int id) {
        String sql = Db2CandidateSql.CANDIDATE_SELECT + " WHERE c.CandidateId = ?";
        // Chuẩn bị PreparedStatement với SQL SELECT thí sinh theo CandidateId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, id);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    return mapResultSetToExamRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Lấy thí sinh theo kỳ thi và số báo danh (SBD).
     * Ưu tiên tra cứu SQL theo số thứ tự; fallback duyệt danh sách kỳ thi nếu SBD không parse được.
     *
     * @param examId mã kỳ thi ({@code ExamId})
     * @param sbd    số báo danh (chuỗi, có thể dạng {@code 001} hoặc {@code EXAM-001})
     * @return {@link ExamRegistrationDTO} hoặc {@code null} nếu không khớp
     */
    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        int candidateNo = ExamStaffFormat.parseCandidateNo(sbd.trim());
        if (candidateNo <= 0) {
            String trimmed = sbd.trim();
            for (ExamRegistrationDTO c : getCandidatesByExam(examId)) {
                if (trimmed.equals(c.getSbd())) {
                    return c;
                }
            }
            return null;
        }
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + """
                 WHERE ee.ExamId = ?
                   AND COALESCE(
                     TRY_CAST(c.CandidateNumber AS INT),
                     TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
                   ) = ?
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT thí sinh theo ExamId + candidateNo
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examId);
            ps.setInt(2, candidateNo);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    return mapResultSetToExamRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Danh sách thí sinh theo kỳ thi từ {@link Db2CandidateSql}.
     * Fallback {@code CANDIDATE_SELECT_MINIMAL} nếu SELECT đầy đủ rỗng; loại trùng qua {@code ExamEnrollmentMerge}.
     *
     * @param examId mã kỳ thi
     * @return danh sách {@link ExamRegistrationDTO}; rỗng nếu {@code examId <= 0}
     */
    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamRegistrationDTO> list = queryCandidates(Db2CandidateSql.CANDIDATE_SELECT,
                " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId, 0);
        if (list.isEmpty()) {
            list = queryCandidates(Db2CandidateSql.CANDIDATE_SELECT_MINIMAL,
                    " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId, 0);
        }
        if (!list.isEmpty()) {
            return ExamEnrollmentMerge.deduplicateByCandidate(list);
        }
        return list;
    }

    /**
     * Chạy SELECT thí sinh ({@code selectSql} + {@code whereSql}) với bind tối đa 2 tham số int.
     *
     * @param selectSql phần SELECT từ {@link Db2CandidateSql}
     * @param whereSql  mệnh đề WHERE + ORDER BY
     * @param bindInt   giá trị bind placeholder thứ nhất
     * @param bindInt2  giá trị bind placeholder thứ hai (bỏ qua nếu {@code <= 0})
     * @return danh sách DTO; rỗng nếu không có kết nối hoặc lỗi SQL
     */
    private List<ExamRegistrationDTO> queryCandidates(String selectSql, String whereSql, int bindInt, int bindInt2) {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("ExamRegistrationDAO: database connection unavailable");
            return list;
        }
        String sql = selectSql + whereSql;
        // Chuẩn bị PreparedStatement với SQL SELECT danh sách thí sinh
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, bindInt);
            if (bindInt2 > 0) {
                ps.setInt(2, bindInt2);
            }
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(mapResultSetToExamRegistration(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ExamRegistrationDAO query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật cờ có mặt trên bảng {@code Candidate}: xóa đánh dấu vắng ({@code IsAbsent = 0})
     * khi {@code isPresent=true}. Không ghi gì nếu {@code isPresent=false}.
     *
     * @param id        mã thí sinh
     * @param isPresent {@code true} để đánh dấu có mặt (xóa vắng)
     * @return {@code true} nếu thao tác thành công hoặc không cần ghi
     */
    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        if (id <= 0) {
            return false;
        }
        if (!isPresent) {
            return true;
        }
        String sql = """
                UPDATE Candidate
                SET IsAbsent = 0
                WHERE CandidateId = ? AND ISNULL(IsAbsent, 0) = 1
                """;
        // Chuẩn bị PreparedStatement với SQL UPDATE IsAbsent
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, id);
            // Thực thi UPDATE
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật / tạo thanh toán hoàn tất cho thí sinh trên bảng {@code Payment}.
     * Kiểm tra payment Completed/Paid trước; nếu chưa có thì INSERT bản ghi mới.
     *
     * @param id                 mã thí sinh
     * @param isPaymentCompleted {@code true} để đảm bảo có payment hoàn tất
     * @return {@code true} nếu đã có hoặc tạo thành công; {@code false} nếu thiếu enrollment
     */
    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        if (!isPaymentCompleted) {
            return true;
        }
        try {
            // Kiểm tra đã có Payment Completed/Paid cho thí sinh chưa
            String check = """
                    SELECT TOP 1 p.PaymentId
                    FROM Payment p
                    INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                    WHERE ee.CandidateId = ? AND p.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                    """;
            // Chuẩn bị PreparedStatement kiểm tra payment
            try (PreparedStatement ps = getConnection().prepareStatement(check)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            Integer enrollmentId = getExamEnrollmentId(id);
            if (enrollmentId == null) {
                return false;
            }
            String ins = """
                    INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                    VALUES ('Completed', 'Cash', ?, 200000, GETDATE(), ?)
                    """;
            // Chuẩn bị PreparedStatement INSERT Payment mới
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setString(1, "REF-" + System.currentTimeMillis() % 1000000);
                ps.setInt(2, enrollmentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa các giao dịch thanh toán đã hoàn tất của thí sinh từ bảng {@code Payment}
     * (JOIN {@code ExamEnrollment} theo {@code CandidateId}).
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu DELETE thực thi (kể cả 0 dòng); {@code false} nếu lỗi
     */
    @Override
    public boolean clearCompletedPayments(int candidateId) {
        if (candidateId <= 0) {
            return false;
        }
        String sql = """
                DELETE p
                FROM Payment p
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                WHERE ee.CandidateId = ? AND p.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                """;
        // Chuẩn bị PreparedStatement với SQL DELETE Payment hoàn tất
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi DELETE
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật phòng phân bổ lý thuyết qua {@link ExamEnrollmentSectionSupport#updateTheoryAllocation}.
     * Ghi {@code ExamAreaId} trên {@code ExamEnrollmentSection} và {@code ExamEnrollment}.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực/phòng lý thuyết
     * @param areaName    tên khu vực (không dùng trực tiếp ở persistence)
     * @return {@code true} nếu phân phòng thành công
     */
    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        if (candidateId <= 0 || examId <= 0 || areaId <= 0) {
            return false;
        }
        try {
            return ExamEnrollmentSectionSupport.updateTheoryAllocation(
                    getConnection(), candidateId, examId, areaId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật sân/phòng phân bổ thực hành qua {@link ExamEnrollmentSectionSupport#updatePracticalAllocation}.
     * Ghi {@code ExamAreaId} trên {@code ExamEnrollmentSection} phần TH.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực/sân thực hành
     * @param areaName    tên khu vực (không dùng trực tiếp ở persistence)
     * @return {@code true} nếu phân khu vực thành công
     */
    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        if (candidateId <= 0 || examId <= 0 || areaId <= 0) {
            return false;
        }
        try {
            return ExamEnrollmentSectionSupport.updatePracticalAllocation(
                    getConnection(), candidateId, examId, areaId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra thí sinh đã có phòng lý thuyết trong kỳ thi chưa.
     * SELECT từ {@code ExamEnrollmentSection} JOIN {@code ExamSection}, {@code ExamArea}
     * lọc section lý thuyết có {@code ExamAreaId IS NOT NULL}.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return thông báo lỗi tiếng Việt nếu đã phân phòng; {@code null} nếu được phép phân
     */
    @Override
    public String validateUniqueTheoryAllocation(int candidateId, int examId) {
        if (candidateId <= 0 || examId <= 0) {
            return "Không xác định được kỳ thi để phân phòng.";
        }
        String sql = """
                SELECT ea.AreaName
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                LEFT JOIN ExamArea ea ON ea.ExamAreaId = ees.ExamAreaId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND ees.ExamAreaId IS NOT NULL
                  AND es.SectionType IN (""" + examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        // Chuẩn bị PreparedStatement kiểm tra phân phòng lý thuyết hiện tại
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String areaName = rs.getString("AreaName");
                    if (areaName == null || areaName.isBlank()) {
                        areaName = "đã phân";
                    }
                    return "Thí sinh đã được phân phòng \"" + areaName.trim() + "\" trong kỳ thi này.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Không kiểm tra được phân phòng hiện tại của thí sinh.";
        }
        return null;
    }

    /**
     * Cập nhật điểm lý thuyết và/hoặc thực hành (ủy quyền cho overload với {@code examId=0}).
     * Ghi vào {@code ExamScore} qua {@link #upsertSectionScore}.
     *
     * @param id              mã thí sinh
     * @param theoryScore     điểm lý thuyết ({@code null} = bỏ qua)
     * @param theoryPassed    kết quả LT ({@code passed}/{@code failed}/{@code null})
     * @param practicalScore  điểm thực hành ({@code null} = bỏ qua)
     * @param practicalPassed kết quả TH ({@code passed}/{@code failed}/{@code null})
     * @return {@code true} nếu ghi điểm thành công
     */
    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        return updateScores(id, 0, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    /**
     * Ghi điểm LT/TH cho thí sinh; {@code examId=0} → tự resolve {@code ExamEnrollment}.
     * Tính passed the {@link AllocationPassRules} nếu tham số passed null.
     *
     * @param id              mã thí sinh
     * @param examId          mã kỳ thi (0 = enrollment mới nhất)
     * @param theoryScore     điểm LT hoặc null
     * @param theoryPassed    kết quả LT hoặc null
     * @param practicalScore  điểm TH hoặc null
     * @param practicalPassed kết quả TH hoặc null
     * @return {@code true} nếu mọi phần ghi thành công
     */
    private boolean updateScores(int id, int examId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        try {
            boolean ok = true;
            if (theoryScore != null) {
                boolean passed = theoryPassed != null
                        ? "passed".equalsIgnoreCase(theoryPassed)
                        : AllocationPassRules.isTheoryPassed(
                                AllocationPassRules.normalizeLicense(findLicenseClassByCandidate(id), null),
                                theoryScore);
                ok = upsertSectionScore(id, examId, "Theory", theoryScore, passed) && ok;
            }
            if (practicalScore != null) {
                boolean passed = practicalPassed != null
                        ? "passed".equalsIgnoreCase(practicalPassed)
                        : AllocationPassRules.isPracticalPassed(practicalScore);
                ok = upsertSectionScore(id, examId, "Practical", practicalScore, passed) && ok;
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("[updateScores] FAILED candidateId=" + id + " examId=" + examId
                    + " theory=" + theoryScore + " practical=" + practicalScore
                    + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật hồ sơ cơ bản thí sinh trên {@code Candidate}, {@code Profile}, {@code User}
     * trong một transaction (autoCommit=false).
     *
     * @param id       mã thí sinh
     * @param fullName họ tên
     * @param dob      ngày sinh
     * @param govIdNo  CCCD/CMND
     * @param email    email (cập nhật cả bảng User nếu có)
     * @param phoneNo  số điện thoại
     * @return {@code true} nếu commit thành công
     */
    @Override
    public boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?, Email = ?
                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?
                WHERE ProfileId = (
                    SELECT TOP 1 p.ProfileId
                    FROM Profile p
                    INNER JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
                    WHERE c.CandidateId = ?
                )
                """;
        String sqlUser = """
                UPDATE [User] SET Email = ?
                WHERE UserId IN (
                    SELECT TOP 1 p.UserId
                    FROM Profile p
                    INNER JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
                    WHERE c.CandidateId = ?
                )
                """;
        try {
            // Bắt đầu transaction cập nhật Candidate + Profile + User
            getConnection().setAutoCommit(false);
            // UPDATE bảng Candidate
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                if (email != null && !email.isBlank()) {
                    ps.setString(5, email.trim());
                } else {
                    ps.setNull(5, Types.NVARCHAR);
                }
                ps.setInt(6, id);
                ps.executeUpdate();
            }
            // UPDATE bảng Profile liên kết qua GovernmentIdNumber
            try (PreparedStatement ps = getConnection().prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            if (email != null && !email.isBlank()) {
                // UPDATE email trên bảng User
                try (PreparedStatement ps = getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email.trim());
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Cập nhật đường dẫn ảnh thí sinh trên bảng {@code Candidate.PhotoImageUrl}.
     *
     * @param id       mã thí sinh
     * @param photoUrl URL ảnh; {@code null} ghi SQL NULL
     * @return {@code true} nếu UPDATE ảnh hưởng ít nhất một dòng
     */
    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        String sql = "UPDATE Candidate SET PhotoImageUrl = ? WHERE CandidateId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE PhotoImageUrl
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            if (photoUrl != null) {
                ps.setString(1, photoUrl);
            } else {
                ps.setNull(1, Types.NVARCHAR);
            }
            ps.setInt(2, id);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đánh dấu vắng mặt: set {@code Candidate.IsAbsent = 1}.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu UPDATE thành công
     */
    @Override
    public boolean markAbsent(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 1 WHERE CandidateId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE IsAbsent=1
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đánh dấu đình chỉ thi: set {@code Candidate.IsSuspended = 1}.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu UPDATE thành công
     */
    @Override
    public boolean markSuspended(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 1 WHERE CandidateId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE IsSuspended=1
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hủy đình chỉ thi: set {@code Candidate.IsSuspended = 0}.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu UPDATE thành công
     */
    @Override
    public boolean undoSuspension(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 0 WHERE CandidateId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE IsSuspended=0
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi UPDATE
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hủy đánh dấu vắng: xóa kết quả thi liên quan, reset section LT, set {@code IsAbsent=0}
     * trong transaction.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu hủy vắng thành công
     */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        String sql = """
                UPDATE Candidate
                SET IsAbsent = 0
                WHERE CandidateId = ? AND IsAbsent = 1
                """;
        try {
            // Bắt đầu transaction hủy vắng
            getConnection().setAutoCommit(false);
            deleteAbsentExamResults(candidateId);
            resetSectionStatusAfterAbsentUndo(candidateId);
            int rows;
            // UPDATE Candidate IsAbsent=0
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                rows = ps.executeUpdate();
            }
            if (rows <= 0) {
                getConnection().rollback();
                return false;
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Xóa {@code DeductionRecord}, {@code ExamScore}, {@code ExamResult} khi hủy đánh dấu vắng.
     * DELETE theo {@code ExamEnrollmentId} của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @throws SQLException nếu DELETE thất bại
     */
    private void deleteAbsentExamResults(int candidateId) throws SQLException {
        Integer examCandidateId = getExamEnrollmentId(candidateId);
        if (examCandidateId == null) {
            return;
        }
        String delDeductions = """
                DELETE sd FROM DeductionRecord sd
                JOIN ExamScore es ON es.ExamScoreId = sd.ExamScoreId
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ?
                """;
        String delScores = """
                DELETE es FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ?
                """;
        String delResult = "DELETE FROM ExamResult WHERE ExamEnrollmentId = ?";
        // DELETE DeductionRecord trước (phụ thuộc ExamScore)
        try (PreparedStatement ps = getConnection().prepareStatement(delDeductions)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        // DELETE ExamScore
        try (PreparedStatement ps = getConnection().prepareStatement(delScores)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        // DELETE ExamResult
        try (PreparedStatement ps = getConnection().prepareStatement(delResult)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
    }

    /**
     * Reset {@code Status} phần lý thuyết về Pending sau khi hủy vắng.
     *
     * @param candidateId mã thí sinh
     * @throws SQLException nếu UPDATE thất bại
     */
    private void resetSectionStatusAfterAbsentUndo(int candidateId) throws SQLException {
        ExamEnrollmentSectionSupport.resetTheoryStatus(getConnection(), candidateId);
    }

    /**
     * Upsert điểm một phần thi (Theory/Practical) cho thí sinh vào {@code ExamScore}.
     * Resolve {@code ExamEnrollmentId}, {@code ExamSectionId} qua nhiều fallback.
     *
     * @param candidateId    mã thí sinh
     * @param examId         mã kỳ thi (0 = tự resolve)
     * @param sectionKeyword {@code Theory} hoặc {@code Practical}
     * @param score          điểm số
     * @param passed         đã đạt hay chưa
     * @return {@code true} nếu ghi điểm thành công
     * @throws SQLException nếu truy vấn/ghi thất bại
     */
    private boolean upsertSectionScore(int candidateId, int examId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        Integer examEnrollmentId = resolveExamEnrollmentForScore(candidateId, examId);
        if (examEnrollmentId == null) {
            System.err.println("[upsertSectionScore] no ExamEnrollment: candidateId=" + candidateId
                    + " examId=" + examId + " section=" + sectionKeyword);
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examEnrollmentId, sectionKeyword);
        if (sectionId == null && "Theory".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findTheorySectionIdByCandidate(candidateId);
        }
        if (sectionId == null && "Practical".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findPracticalSectionIdByCandidate(candidateId);
        }
        if (sectionId == null && examId > 0) {
            sectionId = findSectionIdByExam(examId, sectionKeyword);
        }
        if (sectionId == null) {
            Integer enrollExamId = getExamIdForEnrollment(examEnrollmentId);
            if (enrollExamId != null && enrollExamId > 0) {
                sectionId = findSectionIdByExam(enrollExamId, sectionKeyword);
            }
        }
        if (sectionId == null) {
            System.err.println("[upsertSectionScore] no ExamSection: candidateId=" + candidateId
                    + " enrollmentId=" + examEnrollmentId + " examId=" + examId
                    + " section=" + sectionKeyword);
            return false;
        }
        int scoreExamId = examId > 0 ? examId : 0;
        if (scoreExamId <= 0) {
            Integer enrollExamId = getExamIdForEnrollment(examEnrollmentId);
            if (enrollExamId != null && enrollExamId > 0) {
                scoreExamId = enrollExamId;
            }
        }
        if (scoreExamId > 0) {
            Integer sessionEnrollment = getExamEnrollmentIdForExam(candidateId, scoreExamId);
            if (sessionEnrollment != null) {
                examEnrollmentId = sessionEnrollment;
            }
        }
        return upsertExamScore(examEnrollmentId, sectionId, score, passed);
    }

    /**
     * Insert hoặc cập nhật {@code ExamScore} theo {@code ExamResultId} + {@code ExamSectionId}.
     *
     * @param examCandidateId mã ghi danh ({@code ExamEnrollmentId})
     * @param sectionId       mã phần thi
     * @param score           điểm số
     * @param passed          cờ đạt/không đạt (cập nhật ExamResult.IsPassed)
     * @return {@code true} nếu ghi thành công
     * @throws SQLException nếu truy vấn/ghi thất bại
     */
    private boolean upsertExamScore(int examCandidateId, int sectionId, int score, boolean passed)
            throws SQLException {
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        String check = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        int scoreId = -1;
        // Chuẩn bị PreparedStatement kiểm tra ExamScore đã tồn tại
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    scoreId = rs.getInt("ExamScoreId");
                }
            }
        }
        if (scoreId == -1) {
            // INSERT ExamScore mới
            String ins = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setInt(1, resultId);
                ps.setInt(2, sectionId);
                ps.setDouble(3, score);
                ps.executeUpdate();
            }
        } else {
            // UPDATE ExamScore hiện có
            String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
                ps.setDouble(1, score);
                ps.setInt(2, scoreId);
                ps.executeUpdate();
            }
        }
        return true;
    }

    /**
     * Tìm {@code ExamSectionId} phần lý thuyết theo thí sinh (resolve ExamId mới nhất).
     *
     * @param candidateId mã thí sinh
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        int examId = resolveExamIdForCandidate(candidateId);
        if (examId <= 0) {
            return null;
        }
        return ExamEnrollmentSectionSupport.findSectionId(
                getConnection(), examId, examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES);
    }

    /**
     * Tìm {@code ExamSectionId} phần thực hành theo thí sinh (resolve ExamId mới nhất).
     *
     * @param candidateId mã thí sinh
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer findPracticalSectionIdByCandidate(int candidateId) throws SQLException {
        int examId = resolveExamIdForCandidate(candidateId);
        if (examId <= 0) {
            return null;
        }
        return ExamEnrollmentSectionSupport.findSectionId(
                getConnection(), examId, examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES);
    }

    /**
     * Lấy {@code ExamId} mới nhất của thí sinh từ bảng {@code ExamEnrollment}.
     *
     * @param candidateId mã thí sinh
     * @return {@code ExamId} hoặc {@code -1} nếu không có
     * @throws SQLException nếu truy vấn thất bại
     */
    private int resolveExamIdForCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ee.ExamId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamId mới nhất
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        }
        return -1;
    }

    /**
     * Ưu tiên ghi danh đúng ca ({@code examId}); nếu không khớp thì fallback
     * sang {@code ExamEnrollment} mới nhất của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi (0 = chỉ dùng enrollment mới nhất)
     * @return {@code ExamEnrollmentId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer resolveExamEnrollmentForScore(int candidateId, int examId) throws SQLException {
        if (examId > 0) {
            Integer forExam = getExamEnrollmentIdForExam(candidateId, examId);
            if (forExam != null) {
                return forExam;
            }
        }
        return getExamEnrollmentId(candidateId);
    }

    /**
     * Tìm hoặc tạo {@code ExamResult} theo {@code ExamEnrollmentId}; cập nhật {@code IsPassed} nếu đã có.
     *
     * @param examCandidateId mã ghi danh
     * @param passed          cờ đạt/không đạt
     * @return {@code ExamResultId}
     * @throws SQLException nếu SELECT/INSERT/UPDATE thất bại
     */
    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";
        // Chuẩn bị PreparedStatement kiểm tra ExamResult đã tồn tại
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int resultId = rs.getInt("ExamResultId");
                    // UPDATE IsPassed trên bản ghi hiện có
                    try (PreparedStatement upd = getConnection().prepareStatement(
                            "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?")) {
                        upd.setBoolean(1, passed);
                        upd.setInt(2, resultId);
                        upd.executeUpdate();
                    }
                    return resultId;
                }
            }
        }
        // INSERT ExamResult mới
        String ins = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examCandidateId);
            ps.setBoolean(2, passed);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        throw new SQLException("Cannot create ExamResult");
    }

    /**
     * Tìm {@code ExamSectionId} theo enrollment + keyword Theory/Practical.
     *
     * @param examEnrollmentId mã ghi danh
     * @param keyword          {@code Theory} hoặc {@code Practical}
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer findSectionIdForCandidate(int examEnrollmentId, String keyword) throws SQLException {
        Integer examId = getExamIdForEnrollment(examEnrollmentId);
        if (examId == null || examId <= 0) {
            return null;
        }
        String types = "Theory".equalsIgnoreCase(keyword)
                ? examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES
                : examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES;
        Integer fromEnrollment = ExamEnrollmentSectionSupport.findSectionIdForEnrollment(
                getConnection(), examEnrollmentId, types);
        if (fromEnrollment != null) {
            return fromEnrollment;
        }
        return ExamEnrollmentSectionSupport.findSectionId(getConnection(), examId, types);
    }

    /**
     * Tìm {@code ExamSectionId} theo {@code ExamId} + keyword Theory/Practical.
     *
     * @param examId  mã kỳ thi
     * @param keyword {@code Theory} hoặc {@code Practical}
     * @return {@code ExamSectionId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer findSectionIdByExam(int examId, String keyword) throws SQLException {
        if (examId <= 0) {
            return null;
        }
        String types = "Theory".equalsIgnoreCase(keyword)
                ? examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES
                : examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES;
        return ExamEnrollmentSectionSupport.findSectionId(getConnection(), examId, types);
    }

    /**
     * Lấy {@code ExamId} từ {@code ExamEnrollmentId} trên bảng {@code ExamEnrollment}.
     *
     * @param examEnrollmentId mã ghi danh
     * @return {@code ExamId} hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer getExamIdForEnrollment(int examEnrollmentId) throws SQLException {
        String sql = "SELECT ExamId FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        // Chuẩn bị PreparedStatement với SQL SELECT ExamId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        }
        return null;
    }

    /**
     * Lấy {@code ExamEnrollmentId} mới nhất theo {@code CandidateId}.
     *
     * @param candidateId mã thí sinh
     * @return mã ghi danh hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer getExamEnrollmentId(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ee.ExamEnrollmentId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamEnrollmentId mới nhất
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    /**
     * Lấy {@code ExamEnrollmentId} theo cặp {@code CandidateId} + {@code ExamId}.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return mã ghi danh hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private Integer getExamEnrollmentIdForExam(int candidateId, int examId) throws SQLException {
        String sql = """
                SELECT ee.ExamEnrollmentId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamEnrollmentId theo ca thi
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    /**
     * Lấy hạng GPLX ({@code LicenceClass}) gắn với thí sinh qua {@code ExamEnrollment} mới nhất.
     *
     * @param candidateId mã thí sinh
     * @return mã hạng bằng hoặc {@code null}
     * @throws SQLException nếu truy vấn thất bại
     */
    private String findLicenseClassByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 l.LicenceClass
                FROM Candidate c
                JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                JOIN Exam ex ON ex.ExamId = ee.ExamId
                JOIN Licence l ON l.LicenceId = ex.LicenceId
                WHERE c.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT LicenceClass
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    /**
     * Đọc cột BIT; giá trị SQL NULL được coi là {@code false}.
     *
     * @param rs     ResultSet nguồn
     * @param column tên cột BIT
     * @return giá trị boolean
     * @throws SQLException nếu đọc cột thất bại
     */
    private static boolean readBit(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return false;
        }
        return value;
    }

    /**
     * Đọc cột BIT nullable, trả {@code null} nếu SQL NULL.
     *
     * @param rs     ResultSet nguồn
     * @param column tên cột BIT
     * @return {@link Boolean} hoặc {@code null}
     * @throws SQLException nếu đọc cột thất bại
     */
    private static Boolean readNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ {@link Db2CandidateSql}) sang {@link ExamRegistrationDTO}.
     * Tính toán trạng thái đạt/không đạt LT/TH qua {@link AllocationPassRules}.
     *
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO đăng ký thí sinh đầy đủ trường hiển thị
     * @throws SQLException nếu đọc cột bắt buộc thất bại
     */
    private ExamRegistrationDTO mapResultSetToExamRegistration(ResultSet rs) throws SQLException {
        ExamRegistrationDTO er = new ExamRegistrationDTO();
        er.setId(rs.getInt("id"));
        er.setExamId(rs.getInt("examId"));
        try {
            er.setExamEnrollmentId(rs.getInt("examEnrollmentId"));
        } catch (SQLException ignored) {
            er.setExamEnrollmentId(0);
        }
        er.setCandidateNo(rs.getInt("candidateNo"));
        er.setRegistrationType(rs.getString("registrationType"));
        er.setIsPaymentCompleted(readBit(rs, "isPaymentCompleted"));
        er.setIsPresent(readBit(rs, "isPresent"));
        er.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        er.setFullName(rs.getString("fullName"));
        er.setGovIdNo(rs.getString("govIdNo"));
        er.setDateOfBirth(rs.getDate("dateOfBirth"));
        er.setPhoneNo(rs.getString("phoneNo"));
        er.setEmail(rs.getString("email"));
        er.setPhotoUrl(rs.getString("photoUrl"));
        er.setLicenseCode(rs.getString("licenseCode"));
        er.setComputerCode(rs.getString("computerCode"));
        try {
            er.setTakeTheory(readNullableBoolean(rs, "takeTheory"));
            er.setTakePractical(readNullableBoolean(rs, "takePractical"));
        } catch (SQLException ignored) {
            er.setTakeTheory(null);
            er.setTakePractical(null);
        }
        er.setExamDate(rs.getDate("examDate"));

        String notes = rs.getString("notes");
        er.setNotes(notes);
        boolean isAbsent = readBit(rs, "isAbsent");
        if (!isAbsent && notes != null && "Absent".equalsIgnoreCase(notes.trim())) {
            isAbsent = true;
        }
        er.setAbsent(isAbsent);
        er.setSuspended(readBit(rs, "isSuspended"));

        if (notes != null && notes.startsWith("AllocatedRoom:")) {
            String[] parts = notes.split(":", 3);
            if (parts.length >= 2) {
                try {
                    er.setAllocatedAreaId(Integer.parseInt(parts[1]));
                    if (parts.length >= 3) {
                        er.setAllocatedAreaName(parts[2]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } else {
            int areaIdVal = rs.getInt("allocatedAreaId");
            if (!rs.wasNull()) {
                er.setAllocatedAreaId(areaIdVal);
                er.setAllocatedAreaName(rs.getString("allocatedAreaName"));
            }
        }

        try {
            int pracAreaId = rs.getInt("practicalAllocatedAreaId");
            if (!rs.wasNull()) {
                er.setPracticalAllocatedAreaId(pracAreaId);
                er.setPracticalAllocatedAreaName(rs.getString("practicalAllocatedAreaName"));
            }
        } catch (SQLException ignored) {
        }

        String licenseForPass = AllocationPassRules.normalizeLicense(er.getLicenseCode(), er.getClazz());

        int tScoreVal = rs.getInt("theoryScore");
        if (isAbsent || rs.wasNull() || er.skipsTheory()) {
            er.setTheoryScore(null);
            er.setTheoryPassed("none");
        } else {
            er.setTheoryScore(tScoreVal);
            er.setTheoryPassed(AllocationPassRules.isTheoryPassed(licenseForPass, tScoreVal) ? "passed" : "failed");
        }

        int pScoreVal = rs.getInt("practicalScore");
        if (isAbsent || rs.wasNull() || er.skipsPractical()) {
            er.setPracticalScore(null);
            er.setPracticalPassed("none");
        } else {
            er.setPracticalScore(pScoreVal);
            er.setPracticalPassed(AllocationPassRules.isPracticalPassed(pScoreVal) ? "passed" : "failed");
        }

        return er;
    }
}
