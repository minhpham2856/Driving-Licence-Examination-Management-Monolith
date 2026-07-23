package examstaff.dao.impl;


import shared.dbconnection.DBContext;
import examstaff.dao.ExamAreaDAO;
import examstaff.enums.ExamSection;
import shared.enums.ExamAreaType;
import shared.model.ExamArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai JDBC của {@link ExamAreaDAO} — đọc bảng {@code ExamArea} và {@code Exam_ExamArea}.
 *
 * Cách hoạt động:
 * Mỗi method mở {@link Connection} riêng qua {@code DBContext}, SELECT đơn giản,
 * map {@link ResultSet} → {@link ExamArea} qua {@code map(rs)}. Không cache — luôn đọc DB.
 *
 * Phòng lý thuyết — hai schema AreaType:
 * {@link #getActiveTheoryRooms} gộp {@code ExamSection.LY_THUYET} và {@code ExamAreaType.EXAM_ROOM}
 * vào {@code LinkedHashMap} theo {@code ExamAreaId} để loại trùng khi DB có cả hai tên loại
 * (schema Clean vs SWP/DLEM).
 *
 * Ai gọi?:
 * Allocation, examiner-allocation, UI chọn phòng — cần {@link #getAreasByExamId} theo kỳ
 * hoặc {@link #getAvailableAreasByType} theo loại sân/phòng.
 */
public class ExamAreaDAOImpl implements ExamAreaDAO {

    /**
     * Ánh xạ một dòng ResultSet (bảng {@code ExamArea}) sang {@link ExamArea}.
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return entity khu vực thi
     * @throws SQLException nếu đọc cột thất bại
     */
    private ExamArea map(ResultSet rs) throws SQLException {
        ExamArea a = new ExamArea();
        a.setExamAreaId(rs.getInt("ExamAreaId"));
        a.setAreaName(rs.getString("AreaName"));
        a.setAreaType(rs.getString("AreaType"));
        int capacity = rs.getInt("Capacity");
        a.setCapacity(rs.wasNull() ? null : capacity);
        a.setLocation(rs.getString("Location"));
        try {
            a.setExamZoneId(rs.getInt("ExamZoneId"));
        } catch (SQLException ignored) {
            // cột có thể thiếu trên schema cũ
        }
        return a;
    }

    /**
     * Lấy một khu vực thi theo mã từ bảng {@code ExamArea}.
     * @param examAreaId mã khu vực ({@code ExamAreaId})
     * @return entity {@link ExamArea} hoặc {@code null} nếu không tìm thấy
     */
    @Override
    public ExamArea getById(int examAreaId) {
        String sql = "SELECT * FROM ExamArea WHERE ExamAreaId = ?";
        // Chuẩn bị PreparedStatement với SQL SELECT khu vực theo ExamAreaId
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examAreaId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    return map(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Lấy danh sách phòng lý thuyết đang dùng được, gộp theo hai schema
     * ({@code Lý thuyết} và {@code Phòng thi}), loại trùng theo {@code ExamAreaId}.
     * @return danh sách phòng lý thuyết không trùng lặp
     */
    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        // Schema Clean: "Lý thuyết" - schema SWP/DLEM: "Phòng thi"
        Map<Integer, ExamArea> byId = new LinkedHashMap<>();
        for (ExamArea a : getAvailableAreasByType(ExamSection.LY_THUYET.getDisplayName())) {
            byId.put(a.getExamAreaId(), a);
        }
        for (ExamArea a : getAvailableAreasByType(ExamAreaType.EXAM_ROOM.getValue())) {
            byId.putIfAbsent(a.getExamAreaId(), a);
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * Lấy danh sách khu vực theo loại ({@code AreaType}) từ bảng {@code ExamArea}.
     * @param areaType loại khu vực (ví dụ: {@code Lý thuyết}, {@code Phòng thi})
     * @return danh sách khu vực sắp theo tên; rỗng nếu {@code areaType} trống
     */
    @Override
    public List<ExamArea> getAvailableAreasByType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return List.of();
        }
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName";
        // Chuẩn bị PreparedStatement với SQL SELECT khu vực theo AreaType
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setString(1, areaType.trim());
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các khu vực được gán cho một kỳ thi qua bảng liên kết {@code Exam_ExamArea}.
     * @param examId mã kỳ thi
     * @return danh sách khu vực của kỳ thi, sắp theo tên
     */
    @Override
    public List<ExamArea> getAreasByExamId(int examId) {
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT ea.* FROM ExamArea ea "
                   + "JOIN Exam_ExamArea exa ON ea.ExamAreaId = exa.ExamAreaId "
                   + "WHERE exa.ExamId = ? ORDER BY ea.AreaName";
        // Chuẩn bị PreparedStatement với SQL SELECT khu vực theo ExamId
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
