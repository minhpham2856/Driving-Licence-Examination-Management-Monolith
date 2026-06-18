package Utils;

import Models.Role;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ExamConstants {

    private ExamConstants() {
    }

    // ===================== Roles =====================

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_EXAMINER = "Examiner";
    public static final String ROLE_EXAM_STAFF = "ExamStaff";
    public static final String ROLE_MANAGING_STAFF = "ManagingStaff";
    public static final String ROLE_REGISTRANT = "Registrant";

    // ===================== Db2Mappings =====================

    public static final Map<String, Integer> ROLE_NAME_TO_ID = Map.of(
            "Admin", 1,
            "Examiner", 2,
            "ManagingStaff", 3,
            "ExamStaff", 4,
            "Candidate", 5,
            "Registrant", 6
    );

    public static int roleIdFromName(String roleName) {
        if (roleName == null) {
            return 0;
        }
        return ROLE_NAME_TO_ID.getOrDefault(roleName, 0);
    }

    public static Role roleFromName(String roleName) {
        return new Role(roleIdFromName(roleName), roleName);
    }

    public static String sexFromGender(boolean gender) {
        return gender ? "Nữ" : "Nam";
    }

    public static boolean genderFromSex(String sex) {
        if (sex == null) {
            return false;
        }
        String s = sex.trim();
        return !(s.equalsIgnoreCase("Nam") || s.equalsIgnoreCase("Male") || s.equals("M"));
    }

    public static int parseCandidateNo(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return 0;
        }
        String trimmed = candidateNumber.trim();
        if (trimmed.contains("-")) {
            try {
                return Integer.parseInt(trimmed.substring(trimmed.indexOf('-') + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String buildCandidateNumber(String licenseCode, int candidateNo) {
        if (candidateNo <= 0) {
            return "000";
        }
        return candidateNo < 1000
                ? String.format("%03d", candidateNo)
                : String.valueOf(candidateNo);
    }

    public static String formatSbd(int candidateNo) {
        return buildCandidateNumber(null, candidateNo);
    }

    public static boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null) {
            return false;
        }
        return switch (registrationStatus) {
            case "CheckedIn", "Present", "Completed" ->
                true;
            default ->
                false;
        };
    }

    // ===================== ExamSectionType =====================

    public enum SectionType {
        THEORY,
        SCORE_BASED
    }

    // ===================== ExamSessionStatus =====================

    public static final String SESSION_SCHEDULED = "Scheduled";
    public static final String SESSION_OPEN = "Open";
    public static final String SESSION_IN_PROGRESS = "InProgress";
    public static final String SESSION_COMPLETED = "Completed";
    public static final String SESSION_CANCELLED = "Cancelled";

    public static boolean canStartSession(String status) {
        return SESSION_SCHEDULED.equalsIgnoreCase(status) || SESSION_OPEN.equalsIgnoreCase(status);
    }

    public static boolean isSessionInProgress(String status) {
        return SESSION_IN_PROGRESS.equalsIgnoreCase(status);
    }

    public static boolean isSessionEnded(String status) {
        return SESSION_COMPLETED.equalsIgnoreCase(status) || SESSION_CANCELLED.equalsIgnoreCase(status);
    }

    // ===================== CandidateSectionStatus =====================

    public static final String CANDIDATE_PENDING = "Pending";
    public static final String CANDIDATE_TESTING = "Testing";
    public static final String CANDIDATE_AWAITING_SIGNATURE = "AwaitingSignature";
    public static final String CANDIDATE_DONE = "Done";

    public static String candidateStatusLabel(String status) {
        if (status == null) {
            return "Chưa thi";
        }
        return switch (status) {
            case CANDIDATE_TESTING -> "Đang thi";
            case CANDIDATE_AWAITING_SIGNATURE -> "chờ ký";
            case CANDIDATE_DONE -> "Đã thi";
            default -> "Chưa thi";
        };
    }

    public static boolean isCandidateAwaitingSignature(String status) {
        return CANDIDATE_AWAITING_SIGNATURE.equals(status);
    }

    public static boolean isCandidateDone(String status) {
        return CANDIDATE_DONE.equals(status);
    }

    // ===================== ExamTypes =====================

    public static final String EXAM_THEORY = "Theory";
    public static final String EXAM_PRACTICAL = "Practical";
    public static final String EXAM_ON_ROAD = "OnRoad";
    public static final String EXAM_ROAD_LAYOUT = "RoadLayout";

    private static final Set<String> ACTIVE_EXAM_TYPES = Set.of(EXAM_THEORY, EXAM_PRACTICAL, EXAM_ON_ROAD);

    public static boolean isExamTypeActive(String typeName) {
        return typeName != null && ACTIVE_EXAM_TYPES.contains(typeName);
    }

    public static String examTypeToVietnamese(String typeName) {
        if (typeName == null) {
            return "";
        }
        return switch (typeName) {
            case EXAM_THEORY -> "Lý thuyết";
            case EXAM_PRACTICAL -> "Thực hành";
            case EXAM_ON_ROAD -> "Đường trường";
            default -> typeName;
        };
    }

    public static String examAreaTypeFor(String typeName) {
        if (EXAM_THEORY.equals(typeName)) {
            return "Room";
        }
        if (EXAM_PRACTICAL.equals(typeName)) {
            return "Ground";
        }
        if (EXAM_ON_ROAD.equals(typeName)) {
            return "Road";
        }
        return "";
    }

    // ===================== ExamDevicePresentation =====================

    public static final String DEVICE_COMPUTER = "Computer";
    public static final String DEVICE_MOTORCYCLE = "Motorcycle";
    public static final String DEVICE_CAR = "Car";
    public static final String DEVICE_TRUCK = "Truck";

    private static final Set<String> VEHICLE_TYPES = Set.of(
            DEVICE_MOTORCYCLE, DEVICE_CAR, DEVICE_TRUCK);

    public static boolean isComputer(String deviceType) {
        return deviceType != null && DEVICE_COMPUTER.equalsIgnoreCase(deviceType.trim());
    }

    public static boolean isVehicle(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return VEHICLE_TYPES.contains(deviceType.trim());
    }

    public static List<String> vehicleTypesForLicence(String licenceClass) {
        String lc = licenceClass != null ? licenceClass.trim().toUpperCase(Locale.ROOT) : "";
        List<String> types = new ArrayList<>();
        if ("A1".equals(lc) || "A".equals(lc)) {
            types.add(DEVICE_MOTORCYCLE);
            return types;
        }
        if ("C".equals(lc) || lc.startsWith("D") || "FC".equals(lc)) {
            types.add(DEVICE_CAR);
            types.add(DEVICE_TRUCK);
            return types;
        }
        types.add(DEVICE_CAR);
        return types;
    }

    public static boolean matchesLicence(String licenceClass, String deviceType) {
        if (deviceType == null) {
            return false;
        }
        String normalized = deviceType.trim();
        for (String allowed : vehicleTypesForLicence(licenceClass)) {
            if (allowed.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static String iconFor(String deviceType) {
        if (deviceType == null) {
            return "devices";
        }
        return switch (deviceType.trim().toLowerCase(Locale.ROOT)) {
            case "computer" -> "computer";
            case "motorcycle" -> "two_wheeler";
            case "car" -> "directions_car";
            case "truck" -> "local_shipping";
            default -> "devices";
        };
    }

    public static String typeLabelVi(String deviceType) {
        if (deviceType == null) {
            return "Thiết bị";
        }
        return switch (deviceType.trim().toLowerCase(Locale.ROOT)) {
            case "computer" -> "Máy thi";
            case "motorcycle" -> "Xe máy";
            case "car" -> "Ô tô";
            case "truck" -> "Xe tải";
            default -> deviceType;
        };
    }

    public static String statusLabelVi(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status.trim()) {
            case "Available", "Operational" -> "Sẵn sàng";
            case "InUse" -> "Đang dùng";
            case "Maintenance" -> "Bảo trì";
            default -> status;
        };
    }

    public static String statusCssClass(String status) {
        if (status == null) {
            return "device-grid-card--unknown";
        }
        return switch (status.trim()) {
            case "Available", "Operational" -> "device-grid-card--available";
            case "InUse" -> "device-grid-card--inuse";
            case "Maintenance" -> "device-grid-card--maintenance";
            default -> "device-grid-card--unknown";
        };
    }

    public static void enrichDeviceRow(Map<String, Object> row, String licenceClass) {
        if (row == null) {
            return;
        }
        String type = row.get("type") != null ? String.valueOf(row.get("type")) : "";
        row.put("icon", iconFor(type));
        row.put("typeLabel", typeLabelVi(type));
        row.put("statusLabel", statusLabelVi(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("statusClass", statusCssClass(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("vehicle", isVehicle(type));
        row.put("computer", isComputer(type));
        row.put("licenceMatch", isComputer(type) || matchesLicence(licenceClass, type));
    }

    // ===================== ExamSectionProfiles =====================

    public static SectionType resolveSectionType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
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
        return "nhap-diem".equals(menuKey) || "sua-ket-qua".equals(menuKey);
    }

    // ===================== AuditEntityLabels =====================

    private static final Map<String, String> AUDIT_LABELS = Map.ofEntries(
            Map.entry("CANDIDATE", "Thí sinh"),
            Map.entry("THÍ SINH", "Thí sinh"),
            Map.entry("EXAMREGISTRATION", "Thí sinh"),
            Map.entry("HỒ SƠ ĐĂNG KÝ", "Thí sinh"),
            Map.entry("PROFILE", "Hồ sơ"),
            Map.entry("PAYMENT", "Thanh toán"),
            Map.entry("EXAMSCORE", "Điểm thi"),
            Map.entry("EXAMDEVICE", "Thiết bị thi"),
            Map.entry("SESSION", "Ca thi"),
            Map.entry("SESSION_EXAMINER", "Phân công giám khảo"),
            Map.entry("SESSION_EXAMINERAREA", "Phân công phòng giám khảo"),
            Map.entry("CANDIDATECALL", "Gọi thí sinh"),
            Map.entry("KẾT QUẢ THI", "Kết quả thi"),
            Map.entry("PHÒNG THI", "Phòng thi"),
            Map.entry("SCOREENTRYQUEUE", "Hàng đợi nhập điểm")
    );

    public static String auditLabel(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT);
        return AUDIT_LABELS.getOrDefault(key, trimmed);
    }

    // ===================== ViolationReasonCodes =====================

    public record ViolationReason(String code, String label) {
    }

    private static final List<ViolationReason> VIOLATION_REASONS = List.of(
            new ViolationReason("quy-che", "Vi phạm quy chế phòng thi"),
            new ViolationReason("gian-lan", "Gian lận / sao chép"),
            new ViolationReason("thiet-bi", "Sử dụng thiết bị cấm"),
            new ViolationReason("ra-vao", "Ra vào phòng thi trái quy định"),
            new ViolationReason("khac", "Lý do khác"));

    public static List<ViolationReason> violationReasons() {
        return VIOLATION_REASONS;
    }

    public static String violationLabel(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        for (ViolationReason reason : VIOLATION_REASONS) {
            if (reason.code().equalsIgnoreCase(code.trim())) {
                return reason.label();
            }
        }
        return code.trim();
    }

    public static Map<String, String> violationMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (ViolationReason reason : VIOLATION_REASONS) {
            map.put(reason.code(), reason.label());
        }
        return map;
    }

    public static List<Map<String, String>> violationOptionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : VIOLATION_REASONS) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.code());
            row.put("label", reason.label());
            list.add(row);
        }
        return list;
    }
}
