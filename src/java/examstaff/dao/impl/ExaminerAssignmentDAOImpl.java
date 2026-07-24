package examstaff.dao.impl;



import shared.dbconnection.DBContext;

import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dto.ExaminerSlotDTO;

import examstaff.enums.UserRole;
import shared.model.Profile;
import examstaff.dto.UserDTO;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai JDBC của ExaminerAssignmentDAO — phân công giám thị
 * trên bảng ExaminerSchedule và ghi nhật ký trên Audit.
 *
 * Hai SELECT chính:
 * - EXAMINER_SELECT — danh sách user role Examiner đang active (+ Profile)
 * - SLOT_SELECT — ca phân công theo kỳ: ExaminerSchedule JOIN ExamArea
 *       (text block đầy đủ, không ghép runtime) → map ExaminerSlotDTO
 * Dùng cho màn /examstaff/examiner-allocation và điều kiện auto-allocate
 * (chỉ phân thí sinh vào phòng/sân đã có giám khảo).
 */
public class ExaminerAssignmentDAOImpl extends DBContext implements ExaminerAssignmentDAO {

    /** Tên entity dùng trong bảng Audit cho ánh xạ giám thị–khu vực. */
    private static final String ROOM_MAPPING_ENTITY = "ExaminerSchedule";

    /** SELECT danh sách giám thị đang hoạt động kèm Profile và Role. */
    private static final String EXAMINER_SELECT = """
            SELECT u.UserId,
                   u.Username,
                   u.Email,
                   u.PasswordHash,
                   u.RoleId,
                   u.IsActive,
                   r.RoleName,
                   p.ProfileId,
                   p.FullName,
                   p.DateOfBirth,
                   p.PhoneNumber,
                   p.Sex,
                   p.GovernmentIdNumber,
                   p.Address
            FROM [User] u
            INNER JOIN [Role] r ON r.RoleId = u.RoleId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            WHERE r.RoleName IN (?, N'Examiner') AND u.IsActive = 1
            ORDER BY p.FullName, u.Username
            """;

    /**
     * SELECT ca phân công giám thị theo kỳ (text block đầy đủ).
     * JOIN ExaminerSchedule → Exam/Licence → User/Profile → ExamArea → ExamSection.
     * Caller gắn WHERE esch.ExamId = ?. Alias map sang ExaminerSlotDTO.
     */
    private static final String SLOT_SELECT = """
            SELECT esch.ExaminerScheduleId AS ExamExaminerId,
                   esch.ExamId AS examId,
                   esch.ExaminerId AS examinerUserId,
                   COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''),
                     N'Hạng ' + l.LicenceClass + N' - ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName,
                   eu.Username AS examinerUsername,
                   ep.FullName AS examinerName,
                   CAST(esch.ExamAreaId AS VARCHAR(20)) AS mappingEntityId,
                   ea.ExamAreaId AS areaId,
                   ea.AreaName AS areaName,
                   ea.AreaType AS areaType,
                   CASE
                       WHEN COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Thực hành%'
                         OR COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Sa hình%'
                         OR COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Sân thi%'
                         OR COALESCE(esect.SectionType, ea.AreaType) LIKE '%Practical%' THEN 2
                       WHEN COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Đường%'
                         OR COALESCE(esect.SectionType, ea.AreaType) LIKE '%Road%' THEN 4
                       ELSE 1
                   END AS examTypeId,
                   COALESCE(esect.SectionType, ea.AreaType) AS examTypeName
            FROM ExaminerSchedule esch
            JOIN Exam e ON e.ExamId = esch.ExamId
            JOIN Licence l ON l.LicenceId = e.LicenceId
            JOIN [User] eu ON eu.UserId = esch.ExaminerId
            LEFT JOIN Profile ep ON ep.UserId = eu.UserId
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = esch.ExamAreaId
            LEFT JOIN ExamSection esect ON esect.ExamSectionId = esch.ExamSectionId
            """;

    /**
     * Lấy danh sách giám thị đang hoạt động từ User JOIN Role, Profile.
     * @return danh sách UserDTO kèm profile giám thị
     */
    @Override
    public List<UserDTO> getActiveExaminers() {
        List<UserDTO> list = new ArrayList<>();
        // Chuẩn bị PreparedStatement với SQL SELECT giám thị active
        try (PreparedStatement ps = getConnection().prepareStatement(EXAMINER_SELECT)) {
            // Gán tham số truy vấn (tên role Sát hạch viên)
            ps.setString(1, UserRole.SAT_HACH_VIEN.getDisplayName());
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(mapExaminer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Phân công giám thị vào khu vực/kỳ thi: INSERT ExaminerSchedule
     * và ghi/xóa bản ghi Audit trong một transaction.
     * @param slot thông tin ca phân công (examId, examinerUserId, areaId, ...)
     * @return true nếu phân công thành công; false nếu trùng hoặc lỗi
     */
    @Override
    public boolean assign(ExaminerSlotDTO slot) {
        if (slot == null || slot.getExamId() <= 0 || slot.getExaminerUserId() <= 0) {
            return false;
        }
        String entityId = buildMappingEntityId(slot.getExamId(), slot.getAreaId(), slot.getExaminerUserId());
        String existingAssignmentSql = """
                SELECT TOP 1 esch.ExaminerScheduleId
                FROM ExaminerSchedule esch
                WHERE esch.ExaminerId = ?
                  AND esch.ExamId = ?
                  AND esch.ExamAreaId <> ?
                """;
        String insertAssignment = """
                INSERT INTO ExaminerSchedule (ExamId, ExaminerId, ExamAreaId, ExamSectionId, AssignedBy, AssignedAt)
                VALUES (?, ?, ?, COALESCE((
                    SELECT TOP 1 es.ExamSectionId
                    FROM ExamSection es
                    WHERE es.ExamId = ?
                      AND (
                        (? = N'Lý thuyết' AND es.SectionType = N'Lý thuyết')
                        OR (? = N'Thực hành' AND (
                            es.SectionType LIKE N'%Thực hành%'
                            OR es.SectionType LIKE N'%Sa hình%'
                            OR es.SectionType LIKE N'%Đường%'
                        ))
                      )
                    ORDER BY es.ExamSectionId
                ), (
                    SELECT TOP 1 es.ExamSectionId
                    FROM ExamSection es
                    WHERE es.ExamId = ?
                    ORDER BY es.ExamSectionId
                )), ?, GETDATE())
                """;
        String deleteMapping = """
                DELETE FROM Audit
                WHERE EntityName = ? AND EntityId = ?
                """;
        String insertMapping = """
                INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'ASSIGN', ?, ?, ?, GETDATE())
                """;
        try {
            // Bắt đầu transaction — phân công và audit phải cùng thành công
            getConnection().setAutoCommit(false);
            // Kiểm tra giám thị đã được phân khu vực khác trong cùng kỳ thi
            try (PreparedStatement ps = getConnection().prepareStatement(existingAssignmentSql)) {
                ps.setInt(1, slot.getExaminerUserId());
                ps.setInt(2, slot.getExamId());
                ps.setInt(3, slot.getAreaId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        getConnection().rollback();
                        return false;
                    }
                }
            }
            // Bước 1: INSERT ExaminerSchedule
            try (PreparedStatement ps = getConnection().prepareStatement(insertAssignment)) {
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                String areaType = slot.getAreaType() != null ? slot.getAreaType().trim() : "";
                ps.setInt(1, slot.getExamId());
                ps.setInt(2, slot.getExaminerUserId());
                ps.setInt(3, slot.getAreaId());
                ps.setInt(4, slot.getExamId());
                ps.setString(5, areaType);
                ps.setString(6, areaType);
                ps.setInt(7, slot.getExamId());
                ps.setInt(8, assignedBy);
                ps.executeUpdate();
            }
            // Bước 2: xóa audit mapping cũ (idempotent)
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }
            // Bước 3: INSERT audit mapping mới
            try (PreparedStatement ps = getConnection().prepareStatement(insertMapping)) {
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                ps.setInt(1, assignedBy);
                ps.setString(2, ROOM_MAPPING_ENTITY);
                ps.setString(3, entityId);
                ps.setString(4, slot.getAreaName() != null ? slot.getAreaName() : String.valueOf(slot.getAreaId()));
                ps.executeUpdate();
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
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
     * Hủy phân công giám thị: DELETE ExaminerSchedule và bản ghi Audit tương ứng.
     * @param slotKey khóa composite examId:areaId:examinerId
     * @return true nếu xóa thành công; false nếu khóa không hợp lệ hoặc lỗi
     */
    @Override
    public boolean remove(String slotKey) {
        int[] parts = parseSlotKey(slotKey);
        if (parts == null) {
            return false;
        }
        int examId = parts[0];
        int areaId = parts[1];
        int examinerId = parts[2];
        String entityId = buildMappingEntityId(examId, areaId, examinerId);
        String deleteAssignment = "DELETE FROM ExaminerSchedule WHERE ExamId = ? AND ExaminerId = ?";
        String deleteMapping = "DELETE FROM Audit WHERE EntityName = ? AND EntityId = ?";
        try {
            // Bắt đầu transaction — xóa phân công và audit cùng lúc
            getConnection().setAutoCommit(false);
            // Bước 1: DELETE ExaminerSchedule
            try (PreparedStatement ps = getConnection().prepareStatement(deleteAssignment)) {
                ps.setInt(1, examId);
                ps.setInt(2, examinerId);
                ps.executeUpdate();
            }
            // Bước 2: DELETE Audit mapping
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
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
     * Lấy tất cả ca phân công giám thị của một kỳ thi từ ExaminerSchedule.
     * @param examId mã kỳ thi
     * @return danh sách ExaminerSlotDTO sắp theo tên khu vực
     */
    @Override
    public List<ExaminerSlotDTO> getByExamId(int examId) {
        String sql = SLOT_SELECT + " WHERE esch.ExamId = ? ORDER BY ea.AreaName, esch.ExaminerScheduleId";
        return querySlots(sql, ps -> ps.setInt(1, examId));
    }

    /**
     * Chạy SELECT ca phân công với binder tùy chỉnh và ánh xạ danh sách slot.
     * @param sql    câu SELECT (từ SLOT_SELECT + WHERE)
     * @param binder lambda gán tham số PreparedStatement
     * @return danh sách ExaminerSlotDTO
     */
    private List<ExaminerSlotDTO> querySlots(String sql, SqlBinder binder) {
        List<ExaminerSlotDTO> list = new ArrayList<>();
        // Chuẩn bị PreparedStatement với SQL SELECT ca phân công
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn qua binder
            binder.bind(ps);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ SLOT_SELECT) sang ExaminerSlotDTO.
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO ca phân công giám thị
     * @throws SQLException nếu đọc cột thất bại
     */
    private ExaminerSlotDTO mapSlot(ResultSet rs) throws SQLException {
        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setExamId(rs.getInt("examId"));
        slot.setExaminerUserId(rs.getInt("examinerUserId"));
        slot.setExamName(rs.getString("examName"));
        slot.setExaminerUsername(rs.getString("examinerUsername"));
        String examinerName = rs.getString("examinerName");
        if (examinerName == null || examinerName.isBlank()) {
            examinerName = rs.getString("examinerUsername");
        }
        slot.setExaminerName(examinerName);

        int areaId = rs.getInt("areaId");
        if (!rs.wasNull()) {
            slot.setAreaId(areaId);
            slot.setAreaName(rs.getString("areaName"));
            slot.setAreaType(rs.getString("areaType"));
        } else {
            String mappingEntityId = rs.getString("mappingEntityId");
            int[] parsed = parseMappingEntityId(mappingEntityId);
            if (parsed != null) {
                slot.setAreaId(parsed[1]);
            }
        }

        int examTypeId = rs.getInt("examTypeId");
        slot.setExamTypeId(examTypeId);
        String examTypeName = rs.getString("examTypeName");
        slot.setExamTypeName(examTypeName != null && !examTypeName.isBlank()
                ? examTypeName
                : examTypeFromId(examTypeId));
        return slot;
    }

    /**
     * Tạo entity ID cho Audit dạng examId:areaId:examinerId.
     * @param examId     mã kỳ thi
     * @param areaId     mã khu vực
     * @param examinerId mã giám thị
     * @return chuỗi khóa composite
     */
    static String buildMappingEntityId(int examId, int areaId, int examinerId) {
        return examId + ":" + areaId + ":" + examinerId;
    }

    /**
     * Phân tích entity ID audit; ủy quyền cho parseSlotKey.
     * @param entityId chuỗi examId:areaId:examinerId
     * @return mảng 3 phần tử hoặc null
     */
    static int[] parseMappingEntityId(String entityId) {
        return parseSlotKey(entityId);
    }

    /**
     * Chuyển mã loại kỳ thi sang tên hiển thị tiếng Việt.
     * @param examTypeId 1=Lý thuyết, 2=Thực hành, 4=Đường trường
     * @return tên loại phần thi
     */
    static String examTypeFromId(int examTypeId) {
        return switch (examTypeId) {
            case 2 ->
                examstaff.enums.ExamSection.THUC_HANH_TRONG_HINH.getDisplayName();
            case 4 ->
                examstaff.enums.ExamSection.THUC_HANH_TREN_DUONG.getDisplayName();
            default ->
                examstaff.enums.ExamSection.LY_THUYET.getDisplayName();
        };
    }

    /**
     * Phân tích khóa slot dạng examId:areaId:examinerId.
     * @param slotKey chuỗi khóa composite
     * @return mảng [examId, areaId, examinerId] hoặc null nếu không hợp lệ
     */
    static int[] parseSlotKey(String slotKey) {
        if (slotKey == null) {
            return null;
        }
        String[] parts = slotKey.split(":");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Ánh xạ một dòng ResultSet giám thị sang UserDTO kèm Profile.
     * @param rs ResultSet từ EXAMINER_SELECT
     * @return DTO người dùng giám thị
     * @throws SQLException nếu đọc cột thất bại
     */
    private UserDTO mapExaminer(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO();
        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));

        Integer profileId = (Integer) rs.getObject("ProfileId");
        if (profileId != null) {
            Profile profile = new Profile();
            profile.setProfileId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            Date dob = rs.getDate("DateOfBirth");
            profile.setDateOfBirth(dob != null ? new java.sql.Timestamp(dob.getTime()) : null);
            profile.setPhoneNumber(rs.getString("PhoneNumber"));
            profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setSex(rs.getBoolean("Sex"));
            user.setProfile(profile);
        }

        return user;
    }

    /** Giao diện functional gán tham số cho PreparedStatement. */
    @FunctionalInterface
    private interface SqlBinder {

        /**
         * Gán các placeholder ? trên PreparedStatement.
         * @param ps PreparedStatement cần bind
         * @throws SQLException nếu set tham số thất bại
         */
        void bind(PreparedStatement ps) throws SQLException;
    }
}

