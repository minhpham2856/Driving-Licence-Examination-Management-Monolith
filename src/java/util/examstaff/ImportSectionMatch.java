package util.examstaff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Khớp nội dung SH (L / H / Đ) với các phần thi có ca trong kỳ.
 * Quy ước Take*: {@code FALSE} = bảo lưu (không thi); {@code null}/khác = có thi.
 */
public final class ImportSectionMatch {

    public static final String THEORY = "Theory";
    public static final String PRACTICAL = "Practical";
    public static final String ROAD = "Road";

    private ImportSectionMatch() {
    }

    public static boolean wantsSection(Boolean takeFlag) {
        return !Boolean.FALSE.equals(takeFlag);
    }

    public static String resolveSectionKind(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return null;
        }
        String normalized = sectionName.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("đường trường") || normalized.contains("duong truong")
                || normalized.contains("trên đường") || normalized.contains("tren duong")
                || normalized.contains("road")) {
            return ROAD;
        }
        if (normalized.contains("lý thuyết") || normalized.contains("ly thuyet")
                || normalized.contains("theory")) {
            return THEORY;
        }
        if (normalized.contains("sa hình") || normalized.contains("sa hinh")
                || normalized.contains("thực hành trong hình") || normalized.contains("thuc hanh trong hinh")
                || normalized.contains("thực hành") || normalized.contains("thuc hanh")
                || normalized.contains("practical")) {
            return PRACTICAL;
        }
        return null;
    }

    /**
     * @return null nếu phù hợp; message tiếng Việt nếu nội dung SH đòi phần mà kỳ không có ca
     */
    public static String mismatchReason(Boolean takeTheory, Boolean takePractical, Boolean takeOnRoad,
            Collection<String> availableKinds) {
        if (availableKinds == null || availableKinds.isEmpty()) {
            return "Kỳ thi chưa có ca phần thi nào";
        }
        Set<String> available = availableKinds instanceof Set
                ? (Set<String>) availableKinds
                : Set.copyOf(availableKinds);
        List<String> missing = new ArrayList<>(3);
        if (wantsSection(takeTheory) && !available.contains(THEORY)) {
            missing.add("Lý thuyết (L)");
        }
        if (wantsSection(takePractical) && !available.contains(PRACTICAL)) {
            missing.add("Sa hình (H)");
        }
        if (wantsSection(takeOnRoad) && !available.contains(ROAD)) {
            missing.add("Đường trường (Đ)");
        }
        if (missing.isEmpty()) {
            return null;
        }
        return "Nội dung SH yêu cầu " + String.join(", ", missing)
                + " nhưng kỳ hôm nay chưa có ca tương ứng";
    }
}
