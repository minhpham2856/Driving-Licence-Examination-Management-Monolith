package examstaff.dao.impl;

import shared.dbconnection.DBContext;
import examstaff.dao.FeeDAO;
import shared.model.Fee;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Triển khai JDBC của FeeDAO — đọc Fee, Licence_Fee, Payment_Fee.
 *
 * Biểu phí thủ tục:
 * getProcedureFees dùng OUTER APPLY chọn mức phí theo hạng LicenceClass,
 * lọc FeeType thủ tục (không học phí), tách logic xe máy (isMotorcycleGroup)
 * vs ô tô khi requiresRoadTest.
 *
 * Phí theo payment:
 * getFeesByPaymentId JOIN Payment_Fee → Fee → Licence_Fee
 * để suy ra Amount đúng hạng của kỳ thi gắn payment đó.
 *
 * Ai gọi?:
 * Luồng thu lệ phí trên /examstaff/procedure — hiển thị checklist phí trước khi ghi Payment.
 */
public class FeeDAOImpl extends DBContext implements FeeDAO {

    /**
     * Lấy danh sách lệ phí thủ tục áp dụng theo hạng GPLX.
     * Truy vấn Fee OUTER APPLY Licence_Fee để lấy mức phí theo hạng,
     * sau đó lọc theo loại phí thủ tục (không gồm học phí).
     * @param licenseCode      mã hạng bằng (ví dụ: B1, A1)
     * @param requiresRoadTest có phần thi đường trường hay không (ảnh hưởng lọc phí TH)
     * @return danh sách Fee phù hợp thủ tục
     */
    @Override
    public List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return List.of();
        }
        String licenceClass = licenseCode.trim().toUpperCase(Locale.ROOT);
        boolean motorcycle = isMotorcycleGroup(licenceClass);
        List<Fee> applicable = new ArrayList<>();
        String sql = """
                SELECT f.FeeId, f.FeeName, f.FeeType, f.IsActive,
                       COALESCE(lf_pick.Amount, 0) AS Amount
                FROM Fee f
                OUTER APPLY (
                    SELECT TOP 1 lf.Amount
                    FROM Licence_Fee lf
                    LEFT JOIN Licence l ON lf.LicenceId = l.LicenceId
                    WHERE lf.FeeId = f.FeeId
                      AND (lf.LicenceId IS NULL OR l.LicenceClass = ?)
                    ORDER BY CASE
                        WHEN l.LicenceClass = ? THEN 0
                        WHEN lf.LicenceId IS NULL THEN 1
                        ELSE 2
                    END
                ) lf_pick
                WHERE f.IsActive = 1
                ORDER BY f.FeeType, f.FeeName
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT lệ phí theo hạng GPLX
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn (hạng GPLX dùng hai lần trong OUTER APPLY)
            ps.setString(1, licenceClass);
            ps.setString(2, licenceClass);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → Fee và lọc theo quy tắc thủ tục
                    Fee fee = mapRow(rs);
                    if (appliesToProcedure(fee, motorcycle, requiresRoadTest)) {
                        applicable.add(fee);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applicable;
    }

    /**
     * Lấy các khoản phí gắn với một thanh toán từ Payment_Fee
     * JOIN Fee, Payment, ExamEnrollment, Exam, Licence.
     * @param paymentId mã thanh toán (PaymentId)
     * @return danh sách Fee kèm số tiền theo hạng GPLX của kỳ thi
     */
    @Override
    public List<Fee> getFeesByPaymentId(int paymentId) {
        List<Fee> fees = new ArrayList<>();
        String sql = """
                SELECT f.FeeId, f.FeeName, f.FeeType, f.IsActive,
                       COALESCE(lf_spec.Amount, lf_common.Amount, 0) AS Amount
                FROM Payment_Fee pf
                INNER JOIN Fee f ON f.FeeId = pf.FeeId
                INNER JOIN Payment p ON p.PaymentId = pf.PaymentId
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                INNER JOIN Exam e ON e.ExamId = ee.ExamId
                INNER JOIN Licence lic ON lic.LicenceId = e.LicenceId
                LEFT JOIN Licence_Fee lf_spec
                    ON lf_spec.FeeId = f.FeeId AND lf_spec.LicenceId = lic.LicenceId
                LEFT JOIN Licence_Fee lf_common
                    ON lf_common.FeeId = f.FeeId AND lf_common.LicenceId IS NULL
                WHERE pf.PaymentId = ?
                ORDER BY f.FeeType, f.FeeName
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT phí theo PaymentId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, paymentId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng Fee
                    fees.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fees;
    }

    /**
     * Lọc phí thủ tục theo loại/tên (loại trừ học phí, áp dụng quy tắc xe máy/TH).
     * @param fee               entity phí cần kiểm tra
     * @param motorcycle        true nếu hạng thuộc nhóm xe máy (A/A1)
     * @param requiresRoadTest  có phần thi đường trường hay không
     * @return true nếu phí thuộc nhóm lệ phí thủ tục áp dụng
     */
    private boolean appliesToProcedure(Fee fee, boolean motorcycle, boolean requiresRoadTest) {
        String name = normalize(fee.getFeeName());
        String type = normalize(fee.getFeeType());

        if (type.contains("hoc phi") || name.startsWith("hoc phi")) {
            return false;
        }
        if (type.contains("phi cap bang") || name.contains("gplx")) {
            return true;
        }
        if (type.contains("phi hanh chinh") || name.contains("ho so") || name.contains("dang ky truc tuyen")) {
            return true;
        }
        if (type.contains("le phi thi") || "exam".equals(type)) {
            if (containsAny(name, "ly thuyet")) {
                return true;
            }
            if (containsAny(name, "trong hinh", "sa hinh", "thuc hanh trong")) {
                return !motorcycle;
            }
            return true;
        }
        return "admin".equals(type) || "license".equals(type);
    }

    /**
     * Chuẩn hóa chuỗi (bỏ dấu Unicode, chuyển lower-case) để so khớp loại/tên phí.
     * @param value chuỗi gốc
     * @return chuỗi đã chuẩn hóa, hoặc rỗng nếu null
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Kiểm tra haystack có chứa bất kỳ needle nào trong danh sách.
     * @param haystack chuỗi cần tìm
     * @param needles  các mẫu con
     * @return true nếu khớp ít nhất một needle
     */
    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xác định hạng GPLX thuộc nhóm xe máy (A hoặc A1).
     * @param licenseCode mã hạng bằng
     * @return true nếu là A hoặc A1
     */
    static boolean isMotorcycleGroup(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String lc = licenseCode.toUpperCase(Locale.ROOT).trim();
        return lc.equals("A1") || lc.equals("A");
    }

    /**
     * Ánh xạ một dòng ResultSet sang Fee.
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return entity phí đã điền đủ trường
     * @throws SQLException nếu đọc cột thất bại
     */
    private Fee mapRow(ResultSet rs) throws SQLException {
        Fee fee = new Fee();
        fee.setFeeId(rs.getInt("FeeId"));
        fee.setFeeName(rs.getString("FeeName"));
        fee.setFeeType(rs.getString("FeeType"));
        fee.setAmount(rs.getDouble("Amount"));
        fee.setActive(rs.getBoolean("IsActive"));
        return fee;
    }
}
