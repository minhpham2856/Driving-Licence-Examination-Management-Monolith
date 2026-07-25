package admin.dao.impl;

import admin.dao.UsageGuardDAO;
import shared.dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Đếm số bản ghi đang tham chiếu tới một danh mục để chặn Xóa/Khóa.
 * Bảng nào không tồn tại trong CSDL thì bỏ qua (ràng buộc khóa ngoại vẫn là chốt chặn cuối).
 */
public class UsageGuardDAOImpl extends DBContext implements UsageGuardDAO {

    /** Một điểm tham chiếu: bảng con, cột khóa ngoại, nhãn hiển thị cho người dùng. */
    private static final class Ref {
        final String table, column, label;
        Ref(String table, String column, String label) {
            this.table = table; this.column = column; this.label = label;
        }
    }

    @Override
    public String zoneBlocker(int zoneId) {
        List<Ref> refs = new ArrayList<>();
        refs.add(new Ref("ExamArea", "ExamZoneId", "phòng/sân thi"));
        return firstBlocker(zoneId, refs, "khu vực thi");
    }

    @Override
    public String areaBlocker(int areaId) {
        List<Ref> refs = new ArrayList<>();
        refs.add(new Ref("ExamDevice", "ExamAreaId", "máy/thiết bị thi"));
        refs.add(new Ref("Exam_ExamArea", "ExamAreaId", "kỳ thi"));
        refs.add(new Ref("ExamEnrollment", "AllocatedExamAreaId", "thí sinh đã phân phòng"));
        refs.add(new Ref("ExamEnrollmentSection", "ExamAreaId", "phần thi của thí sinh"));
        refs.add(new Ref("ExaminerSchedule", "ExamAreaId", "lịch phân công sát hạch viên"));
        return firstBlocker(areaId, refs, "phòng/sân thi");
    }

    @Override
    public String deviceBlocker(int deviceId) {
        List<Ref> refs = new ArrayList<>();
        refs.add(new Ref("ExamEnrollment", "ExamDeviceId", "thí sinh đã gán máy"));
        refs.add(new Ref("ExamEnrollmentSection", "ExamDeviceId", "phần thi của thí sinh"));
        return firstBlocker(deviceId, refs, "máy/thiết bị thi");
    }

    @Override
    public String licenceBlocker(int licenceId) {
        List<Ref> refs = new ArrayList<>();
        refs.add(new Ref("ExamRegistration", "LicenceId", "hồ sơ đăng ký thi"));
        refs.add(new Ref("Exam", "LicenceId", "kỳ thi"));
        refs.add(new Ref("ExamDates", "LicenceId", "lịch thi dự kiến"));
        refs.add(new Ref("ExamSection", "LicenceId", "phần thi"));
        refs.add(new Ref("Licence_Fee", "LicenceId", "biểu lệ phí"));
        refs.add(new Ref("Licence_Question", "LicenceId", "câu hỏi thi"));
        refs.add(new Ref("ScoreDeduction", "LicenceId", "lỗi trừ điểm"));
        refs.add(new Ref("Licence", "UpgradeFromLicenceId", "hạng GPLX nâng hạng từ hạng này"));
        return firstBlocker(licenceId, refs, "hạng GPLX");
    }

    /**
     * Trả về thông báo chặn đầu tiên tìm thấy, hoặc null nếu không nơi nào đang dùng.
     */
    private String firstBlocker(int id, List<Ref> refs, String subject) {
        if (id <= 0) return "Không tìm thấy " + subject + " cần thao tác.";
        for (Ref r : refs) {
            int n = count(r.table, r.column, id);
            if (n > 0) {
                return "Không thể thao tác: " + subject + " này đang được sử dụng bởi "
                        + n + " " + r.label + ". Hãy gỡ liên kết trước khi xóa/khóa.";
            }
        }
        return null;
    }

    /** Đếm bản ghi tham chiếu; trả về 0 nếu bảng/cột không tồn tại. */
    private int count(String table, String column, int id) {
        if (!tableExists(table) || !columnExists(table, column)) return 0;
        String sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [" + column + "] = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean tableExists(String table) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, table);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean columnExists(String table, String column) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, table);
            st.setString(2, column);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
