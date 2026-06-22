package Utils;

import Enums.ViolationReason;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViolationUtils {

    public static String violationLabel(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }
        for (ViolationReason reason : ViolationReason.values()) {
            if (reason.getCode().equalsIgnoreCase(code.trim())) {
                return reason.getLabel();
            }
        }
        return code.trim();
    }

    public static Map<String, String> violationMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (ViolationReason reason : ViolationReason.values()) {
            map.put(reason.getCode(), reason.getLabel());
        }
        return map;
    }

    public static List<Map<String, String>> violationOptionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : ViolationReason.values()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.getCode());
            row.put("label", reason.getLabel());
            list.add(row);
        }
        return list;
    }
}
