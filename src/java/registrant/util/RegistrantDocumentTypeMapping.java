package registrant.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Map mã loại tài liệu UI (registrant) ↔ {@code DocumentType.Type} trong DLEM_DB_2. */
public final class RegistrantDocumentTypeMapping {

    private static final Map<String, String> UI_TO_DB = new LinkedHashMap<>();
    private static final Map<String, String> DB_TO_UI = new LinkedHashMap<>();

    static {
        register("Portrait", "Ảnh chân dung 3x4");
        register("IdFront", "Căn cước công dân (mặt trước)");
        register("IdBack", "Căn cước công dân (mặt sau)");
        register("HealthCertificate", "Giấy khám sức khỏe");
        register("Other", "Hồ sơ khác");
    }

    private RegistrantDocumentTypeMapping() {
    }

    private static void register(String uiCode, String dbType) {
        UI_TO_DB.put(uiCode, dbType);
        DB_TO_UI.put(dbType, uiCode);
    }

    /** Đổi mã loại tài liệu UI sang DocumentType.Type trong DB. */
    public static String toDbType(String uiDocumentType) {
        if (uiDocumentType == null || uiDocumentType.isBlank()) {
            return null;
        }
        if (uiDocumentType.startsWith("Other_")) {
            return UI_TO_DB.get("Other");
        }
        return UI_TO_DB.getOrDefault(uiDocumentType, uiDocumentType);
    }

    /** Đổi DocumentType DB sang mã UI registrant. */
    public static String toUiType(String dbType) {
        if (dbType == null || dbType.isBlank()) {
            return null;
        }
        return DB_TO_UI.getOrDefault(dbType, dbType);
    }

    /** True nếu mã UI thuộc tập đã đăng ký (kể cả Other_*). */
    public static boolean isKnownUiType(String uiDocumentType) {
        if (uiDocumentType == null) {
            return false;
        }
        return uiDocumentType.startsWith("Other_") || UI_TO_DB.containsKey(uiDocumentType);
    }
}
