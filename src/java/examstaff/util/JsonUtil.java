package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

/**
 * Utility escape và nối JSON thủ công cho payload bảng gọi công khai / polling SSE —
 * không dùng thư viện JSON bên ngoài để kiểm soát chặt output.
 *
 * Vai trò trong luồng examstaff:
 * {@code PublicCallServlet} và {@code PublicCallSnapshotSupport} stream trạng thái gọi thí sinh
 * (SBD, tên, hạng) tới TV/bảng công khai. Helper escape ký tự đặc biệt và build object/mảng
 * rút gọn {@code {sbd,name,clazz}} từ {@link ExamRegistrationDTO}.
 *
 * Cách hoạt động:
 * - {@link #escapeJson} — null → token {@code null}; escape {@code \ " \r \n \t} rồi bọc quote.
 * - {@link #appendJsonField} — overload String/long/boolean với trailing comma tùy chọn.
 * - {@link #appendCandidateJson} / {@link #appendCandidateArrayJson} — object hoặc mảng rút gọn.
 *
 * Ai gọi:
 * {@code PublicCallSnapshotSupport}, {@code PublicCallServlet}, {@code PublicCallStateServlet} —
 * endpoint JSON/SSE bảng gọi công khai ngoài session staff.
 */
public final class JsonUtil {

    /** Không cho khởi tạo — chỉ dùng static. */
    private JsonUtil() {
    }

    /**
     * Escape chuỗi thành literal JSON (null → token {@code null}).
     * <p>
     * Thay lần lượt: {@code \} → {@code \\}, {@code "} → {@code \"},
     * rồi CR/LF/TAB; sau đó bọc dấu ngoặc kép.
     * @param value chuỗi gốc
     * @return literal JSON (đã bọc dấu ngoặc kép nếu không null)
     */
    public static String escapeJson(String value) {
        // Bước 1: null JSON
        if (value == null) {
            return "null";
        }
        // Bước 2: escape ký tự đặc biệt theo thứ tự an toàn (\\ trước ")
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        // Bước 3: bọc string JSON
        return "\"" + escaped + "\"";
    }

    /**
     * Nối field chuỗi vào builder dạng {@code "name":"value"} (value đã escape).
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị chuỗi
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, String value, boolean trailingComma) {
        // Bước 1: ghi tên + giá trị đã escape
        json.append('"').append(name).append("\":").append(escapeJson(value));
        // Bước 2: dấu phẩy tùy chọn giữa các field
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối field số nguyên dài vào builder dạng {@code "name":123} (không quote số).
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị long
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, long value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối field boolean vào builder dạng {@code "name":true|false}.
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị boolean
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, boolean value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối object thí sinh rút gọn {@code {sbd, name, clazz}} vào buffer.
     * <p>
     * null candidate → token {@code null}; ngược lại mở {@code {}}, nối 3 field, đóng.
     * @param json      buffer JSON
     * @param candidate hồ sơ (null → {@code null})
     */
    public static void appendCandidateJson(StringBuilder json, ExamRegistrationDTO candidate) {
        // Bước 1: thiếu thí sinh
        if (candidate == null) {
            json.append("null");
            return;
        }
        // Bước 2: object rút gọn cho bảng gọi
        json.append('{');
        appendJsonField(json, "sbd", candidate.getSbd(), true);
        appendJsonField(json, "name", candidate.getName(), true);
        appendJsonField(json, "clazz", candidate.getClazz(), false);
        json.append('}');
    }

    /**
     * Nối mảng thí sinh rút gọn {@code [...]} vào buffer.
     * <p>
     * null/rỗng → {@code []}; có phần tử → nối từng object, phẩy giữa các phần tử.
     * @param json       buffer JSON
     * @param candidates danh sách (null/rỗng → {@code []})
     */
    public static void appendCandidateArrayJson(StringBuilder json, java.util.List<ExamRegistrationDTO> candidates) {
        // Bước 1: mảng rỗng
        if (candidates == null || candidates.isEmpty()) {
            json.append("[]");
            return;
        }
        // Bước 2: nối từng thí sinh, thêm ',' trừ phần tử cuối
        json.append('[');
        for (int i = 0; i < candidates.size(); i++) {
            appendCandidateJson(json, candidates.get(i));
            if (i < candidates.size() - 1) {
                json.append(',');
            }
        }
        json.append(']');
    }
}
