package enums;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ExamTypes {

    public static final String THEORY = "Theory";
    public static final String PRACTICAL = "Practical";
    public static final String ON_ROAD = "OnRoad";
    public static final String ROAD_LAYOUT = "RoadLayout";

    private static final Set<String> ACTIVE = Set.of(THEORY, PRACTICAL, ON_ROAD);

    private static final Map<String, String> VI_LABELS = new LinkedHashMap<>();

    static {
        VI_LABELS.put(THEORY, "Lý thuyết");
        VI_LABELS.put(PRACTICAL, "Thực hành");
        VI_LABELS.put(ON_ROAD, "Đường trường");
    }

    private ExamTypes() {
    }

    public static boolean isActive(String typeName) {
        return typeName != null && ACTIVE.contains(typeName);
    }

    public static String toVietnamese(String typeName) {
        if (typeName == null) {
            return "";
        }
        return VI_LABELS.getOrDefault(typeName, typeName);
    }

    public static String areaTypeFor(String typeName) {
        if (THEORY.equals(typeName)) {
            return "Room";
        }
        if (PRACTICAL.equals(typeName)) {
            return "Ground";
        }
        if (ON_ROAD.equals(typeName)) {
            return "Road";
        }
        return "";
    }
}
