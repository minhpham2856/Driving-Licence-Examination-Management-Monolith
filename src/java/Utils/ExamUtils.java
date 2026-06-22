package Utils;

import Enums.*;
import Enums.SectionType;

import java.util.Locale;

public class ExamUtils {

    public static String sexFromGender(boolean gender) {
        return gender ? "Nam" : "Nữ";
    }

    public static boolean genderFromSex(String sex) {
        if (sex == null || sex.trim().isEmpty()) {
            return false;
        }
        return "nam".equalsIgnoreCase(sex.trim());
    }

    public static SectionType resolveSectionType(String sectionName) {
        if (sectionName == null || sectionName.trim().isEmpty()) {
            return SectionType.THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("lý thuyết")
                || normalized.contains("ly thuyet")
                || normalized.contains("theory")) {
            return SectionType.THEORY;
        }
        return SectionType.SCORE_BASED;
    }

    public static boolean isSidebarMenuDisabled(SectionType type, String menuKey) {
        if (type != SectionType.THEORY || menuKey == null) {
            return false;
        }
        return "score-entry".equals(menuKey) || "result-details".equals(menuKey);
    }

    public static boolean isExamTypeActive(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return false;
        }
        String t = typeName.trim();
        return ExamType.THEORY.getValue().equalsIgnoreCase(t)
                || ExamType.PRACTICAL.getValue().equalsIgnoreCase(t)
                || ExamType.ON_ROAD.getValue().equalsIgnoreCase(t)
                || ExamType.ROAD_LAYOUT.getValue().equalsIgnoreCase(t);
    }

    public static String examTypeToVietnamese(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return "-";
        }
        String t = typeName.trim();
        if (ExamType.THEORY.getValue().equalsIgnoreCase(t)) {
            return "Lý thuyết";
        }
        if (ExamType.PRACTICAL.getValue().equalsIgnoreCase(t)) {
            return "Thực hành / Sa hình";
        }
        if (ExamType.ON_ROAD.getValue().equalsIgnoreCase(t)) {
            return "Đường trường";
        }
        if (ExamType.ROAD_LAYOUT.getValue().equalsIgnoreCase(t)) {
            return "Lý thuyết mô phỏng";
        }
        return typeName;
    }

    public static String examAreaTypeFor(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return null;
        }
        String t = typeName.trim();
        if (ExamType.THEORY.getValue().equalsIgnoreCase(t)) {
            return "Room";
        }
        if (ExamType.PRACTICAL.getValue().equalsIgnoreCase(t)) {
            return "Ground";
        }
        if (ExamType.ON_ROAD.getValue().equalsIgnoreCase(t)) {
            return "Route";
        }
        if (ExamType.ROAD_LAYOUT.getValue().equalsIgnoreCase(t)) {
            return "Room";
        }
        return null;
    }

    public static String auditLabel(String entityName) {
        if (entityName == null || entityName.trim().isEmpty()) {
            return "-";
        }
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT);
        for (AuditEntity entity : AuditEntity.values()) {
            if (entity.getKey().equals(key)) {
                return entity.getLabel();
            }
        }
        return trimmed;
    }
}
