package admin.service;

import admin.model.AccountView;
import admin.model.RoleOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** Xuất/nhập tài khoản hệ thống bằng Excel (.xlsx). */
public interface AccountExcelService {

    /** File mẫu trống (kèm chú thích + danh sách vai trò hợp lệ) để Admin điền rồi import. */
    void writeTemplate(List<RoleOption> roles, OutputStream out) throws IOException;

    /** Xuất danh sách tài khoản đang hiển thị trên bảng. */
    void writeAccounts(List<AccountView> accounts, OutputStream out) throws IOException;

    /** Đọc file Excel import, trả về từng dòng đã tách (chưa validate nghiệp vụ). */
    List<ImportRow> readImport(InputStream in) throws IOException;

    /** Một dòng dữ liệu đọc từ file import. */
    class ImportRow {
        public int rowNumber;      // số dòng trên Excel (1-based) để báo lỗi
        public String username;
        public String email;
        public String roleName;
        public String fullName;
        public String phone;
        public String dateOfBirth; // yyyy-MM-dd sau khi chuẩn hóa
        public String sex;
        public String govId;
        public String address;
        public String status;      // "Hoạt động" | "Khóa"

        public boolean isBlank() {
            return isEmpty(username) && isEmpty(email) && isEmpty(fullName)
                    && isEmpty(phone) && isEmpty(govId);
        }

        private static boolean isEmpty(String s) {
            return s == null || s.trim().isEmpty();
        }
    }
}
