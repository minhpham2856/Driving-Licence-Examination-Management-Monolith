package admin.util;

import java.util.regex.Pattern;

/**
 * Field-level validation for the Admin module. Every method returns a Vietnamese
 * error message when invalid, or {@code null} when the value is OK — so servlets
 * can do: {@code error = Validator.username(x); if (error != null) {...}}.
 *
 * Self-contained utility (no team dependency). Pair with Sanitize.text(...) which
 * already trims/guards null before these checks.
 */
public final class Validator {

    private Validator() {}

    private static final Pattern USERNAME  = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._]{3,49}$");
    private static final Pattern EMAIL     = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_VN  = Pattern.compile("^0\\d{9}$");
    private static final Pattern GOV_ID    = Pattern.compile("^(\\d{9}|\\d{12})$");
    private static final Pattern FULLNAME  = Pattern.compile("^[\\p{L} ]{2,255}$");
    private static final Pattern LIC_CLASS = Pattern.compile("^[A-Z0-9]{1,10}$");
    // chữ (có dấu) + số + khoảng trắng + . , - / ( )
    private static final Pattern GENERIC_NAME = Pattern.compile("^[\\p{L}\\p{N} .,\\-/()]+$");

    /** Gộp nhiều khoảng trắng thành 1 và trim. */
    public static String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    // ---------- Tài khoản & bảo mật ----------

    public static String username(String s) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập tên đăng nhập.";
        if (!USERNAME.matcher(s).matches())
            return "Tên đăng nhập 4–50 ký tự, bắt đầu bằng chữ cái, chỉ gồm chữ không dấu, số, dấu chấm hoặc gạch dưới.";
        return null;
    }

    /** required=true khi tạo mới; false khi sửa (để trống = giữ nguyên). */
    public static String password(String s, boolean required) {
        if (s == null || s.isEmpty()) return required ? "Vui lòng nhập mật khẩu." : null;
        if (s.length() < 8 || s.length() > 100) return "Mật khẩu phải từ 8 đến 100 ký tự.";
        if (s.contains(" ")) return "Mật khẩu không được chứa khoảng trắng.";
        if (!s.matches(".*[a-zA-Z].*") || !s.matches(".*\\d.*"))
            return "Mật khẩu phải có ít nhất 1 chữ cái và 1 chữ số.";
        return null;
    }

    public static String email(String s) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập email.";
        if (s.length() > 255) return "Email không được vượt quá 255 ký tự.";
        if (!EMAIL.matcher(s).matches()) return "Email không đúng định dạng.";
        return null;
    }

    public static String phone(String s) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập số điện thoại.";
        if (!PHONE_VN.matcher(s).matches()) return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
        return null;
    }

    public static String govId(String s) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập số CCCD/CMND.";
        if (!GOV_ID.matcher(s).matches()) return "Số CCCD/CMND phải gồm 9 hoặc 12 chữ số.";
        return null;
    }

    public static String fullName(String s) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập họ tên.";
        if (!FULLNAME.matcher(s).matches()) return "Họ tên 2–255 ký tự, chỉ gồm chữ cái và khoảng trắng.";
        return null;
    }

    public static String sex(String s) {
        s = normalize(s);
        if (!s.equals("Nam") && !s.equals("Nữ") && !s.equals("Khác"))
            return "Vui lòng chọn giới tính hợp lệ.";
        return null;
    }

    /** DOB phải trong quá khứ, tuổi 16–100. */
    public static String dateOfBirth(java.sql.Date dob) {
        if (dob == null) return "Vui lòng nhập ngày sinh hợp lệ.";
        java.time.LocalDate d = dob.toLocalDate();
        java.time.LocalDate today = java.time.LocalDate.now();
        if (!d.isBefore(today)) return "Ngày sinh phải nằm trong quá khứ.";
        int age = java.time.Period.between(d, today).getYears();
        if (age < 16 || age > 100) return "Tuổi phải nằm trong khoảng 16–100.";
        return null;
    }

    // ---------- Các tên khác ----------

    /** Tên có dấu tiếng Việt: khu vực / phòng / thiết bị / biểu phí. */
    public static String name(String label, String s, int min, int max) {
        s = normalize(s);
        if (s.isEmpty()) return "Vui lòng nhập " + label + ".";
        if (s.length() < min || s.length() > max)
            return label + " phải từ " + min + " đến " + max + " ký tự.";
        if (!GENERIC_NAME.matcher(s).matches())
            return label + " chứa ký tự không hợp lệ.";
        return null;
    }

    public static String licenceClass(String s) {
        s = normalize(s).toUpperCase();
        if (s.isEmpty()) return "Vui lòng nhập hạng GPLX.";
        if (!LIC_CLASS.matcher(s).matches())
            return "Hạng GPLX 1–10 ký tự, chỉ gồm chữ IN HOA và số (vd A1, B2, C).";
        return null;
    }

    /** Số nguyên trong [min,max]. Trả lỗi hoặc null. */
    public static String intRange(String label, Integer v, int min, int max) {
        if (v == null) return "Vui lòng nhập " + label + " hợp lệ.";
        if (v < min || v > max) return label + " phải từ " + min + " đến " + max + ".";
        return null;
    }

    /** Số tiền: ≥ 0 và ≤ 1 tỷ. */
    public static String amount(java.math.BigDecimal v) {
        if (v == null) return "Vui lòng nhập mức thu hợp lệ.";
        if (v.signum() < 0) return "Mức thu không được âm.";
        if (v.compareTo(new java.math.BigDecimal("1000000000")) > 0)
            return "Mức thu không được vượt quá 1.000.000.000 đ.";
        return null;
    }
}