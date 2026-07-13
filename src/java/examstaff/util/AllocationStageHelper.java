package examstaff.util;

import dto.exam.ExamRegistrationDTO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AllocationStageHelper {

    public static final String STAGE_OVERVIEW = "overview";
    public static final String STAGE_WAITING = "waiting";
    public static final String STAGE_THEORY = "theory";
    public static final String STAGE_PRACTICAL = "practical";
    public static final String STAGE_RESULTS = "results";

    public static final String RESULT_PASS = "pass";
    public static final String RESULT_FAIL = "fail";
    public static final String RESULT_SUSPENDED = "suspended";

    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 200;

    private AllocationStageHelper() {
    }

    public static final class StageCounts {
        private int waiting;
        private int theory;
        private int practical;
        private int pass;
        private int fail;
        private int suspended;
        private int total;

        // Lay waiting
        public int getWaiting() {
            return waiting;
        }
        // Lay theory

        public int getTheory() {
            return theory;
        // Lay practical
        }

        public int getPractical() {
            return practical;
        }

        public int getPassCount() {
            return pass;
        }

        public int getFailCount() {
            return fail;
        }

        public int getSuspendedCount() {
            return suspended;
        }

        public int getTotal() {
            return total;
        }
    }

    public static final class PageSlice<T> {
        private final List<T> items;
        private final int page;
        private final int pageSize;
        private final int totalItems;
        private final int totalPages;

        // Lay items
        public PageSlice(List<T> items, int page, int pageSize, int totalItems) {
            this.items = items == null ? List.of() : items;
            this.page = Math.max(1, page);
        // Lay page
            this.pageSize = Math.max(1, pageSize);
            this.totalItems = Math.max(0, totalItems);
            this.totalPages = this.totalItems <= 0 ? 0
        // Lay page size
                    : (int) Math.ceil((double) this.totalItems / this.pageSize);
        }

        // Lay total items
        public List<T> getItems() {
            return items;
        }
        // Lay total pages

        public int getPage() {
            return page;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getRowOffset() {
            return (page - 1) * pageSize;
        }
    }

    public static String resolveStageFromServletPath(String servletPath) {
        if (servletPath == null || servletPath.isBlank()) {
            return STAGE_OVERVIEW;
        }
        if (servletPath.endsWith("allocation-waiting")) {
    // Xac dinh result filter from servlet path
            return STAGE_WAITING;
        }
        if (servletPath.endsWith("allocation-theory")) {
            return STAGE_THEORY;
        }
        if (servletPath.endsWith("allocation-practical")) {
            return STAGE_PRACTICAL;
        }
        if (servletPath.endsWith("allocation-results-pass")
                || servletPath.endsWith("allocation-results-fail")
                || servletPath.endsWith("allocation-results-suspended")) {
            return STAGE_RESULTS;
        }
        return STAGE_OVERVIEW;
    }

    public static String resolveResultFilterFromServletPath(String servletPath) {
        if (servletPath != null && servletPath.endsWith("allocation-results-fail")) {
            return RESULT_FAIL;
        }
        if (servletPath != null && servletPath.endsWith("allocation-results-suspended")) {
            return RESULT_SUSPENDED;
        }
        if (servletPath != null && servletPath.endsWith("allocation-results-pass")) {
            return RESULT_PASS;
        }
        return RESULT_PASS;
    }

    public static String resolveJspPath(String servletPath) {
        if (servletPath == null || servletPath.endsWith("/allocation")) {
            return "/views/staff/examstaff/allocation.jsp";
        }
        if (servletPath.endsWith("allocation-waiting")) {
    // Tao extra query
            return "/views/staff/examstaff/allocation-waiting.jsp";
        }
        if (servletPath.endsWith("allocation-theory")) {
    // Tao extra query
            return "/views/staff/examstaff/allocation-theory.jsp";
        }
        if (servletPath.endsWith("allocation-practical")) {
            return "/views/staff/examstaff/allocation-practical.jsp";
        }
        if (servletPath.endsWith("allocation-results-pass")) {
            return "/views/staff/examstaff/allocation-results-pass.jsp";
        }
        if (servletPath.endsWith("allocation-results-fail")) {
            return "/views/staff/examstaff/allocation-results-fail.jsp";
        }
        if (servletPath.endsWith("allocation-results-suspended")) {
            return "/views/staff/examstaff/allocation-results-suspended.jsp";
        }
        return "/views/staff/examstaff/allocation.jsp";
    }

    public static String buildExtraQuery(int page, int pageSize, String searchQuery, String examIdParam,
            String sortColumn, String sortDir, Integer areaFilterId) {
        StringBuilder sb = new StringBuilder();
        if (page > 1) {
            sb.append("&page=").append(page);
        }
        if (pageSize != DEFAULT_PAGE_SIZE) {
            sb.append("&size=").append(pageSize);
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            sb.append("&q=").append(urlEncode(searchQuery.trim()));
        }
        if (examIdParam != null && !examIdParam.isBlank()) {
            sb.append("&examId=").append(urlEncode(examIdParam.trim()));
        }
        if (sortColumn != null && !sortColumn.isBlank()
                && !examstaff.util.ExamRegistrationSort.DEFAULT_COLUMN.equals(sortColumn)) {
            sb.append("&sort=").append(urlEncode(sortColumn.trim()));
        }
        if (sortDir != null && !sortDir.isBlank()
                && !"asc".equalsIgnoreCase(sortDir.trim())) {
            sb.append("&dir=").append(urlEncode(sortDir.trim()));
        }
        if (areaFilterId != null && areaFilterId != 0) {
            sb.append("&areaFilter=").append(areaFilterId);
        }
        return sb.toString();
    }

    /** {@code null}/0 = tất cả; âm = chưa phân phòng/sân; dương = ExamAreaId. */
    public static Integer parseAreaFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if ("none".equalsIgnoreCase(trimmed) || "unassigned".equalsIgnoreCase(trimmed)) {
            return -1;
        }
        try {
            int id = Integer.parseInt(trimmed);
            return id == 0 ? null : id;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<ExamRegistrationDTO> filterByAllocatedArea(List<ExamRegistrationDTO> list,
            Integer areaFilterId, boolean practical) {
        if (list == null || list.isEmpty()) {
            return list == null ? List.of() : list;
        }
        if (areaFilterId == null || areaFilterId == 0) {
            return list;
        }
        boolean unassignedOnly = areaFilterId < 0;
        List<ExamRegistrationDTO> out = new ArrayList<>();
        for (ExamRegistrationDTO c : list) {
            Integer assignedId = practical ? c.getPracticalAllocatedAreaId() : c.getAllocatedAreaId();
            boolean hasRoom = assignedId != null && assignedId > 0;
            if (unassignedOnly) {
                if (!hasRoom) {
                    out.add(c);
                }
            } else if (hasRoom && assignedId.intValue() == areaFilterId.intValue()) {
                out.add(c);
            }
        }
        return out;
    }

    public static String inferServletPathFromAction(String action) {
        if (action == null) {
            return "/views/staff/examstaff/allocation";
        }
        return switch (action) {
            case "allocateRoom" -> "/views/staff/examstaff/allocation-theory";
            case "allocatePracticalRoom" -> "/views/staff/examstaff/allocation-practical";
            default -> "/views/staff/examstaff/allocation";
        };
    }

    public static int parsePage(String raw) {
        if (raw == null || raw.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(raw.trim()));
    // compute counts
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static int parsePageSize(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int size = Integer.parseInt(raw.trim());
            if (size < 10) {
                return 10;
            }
            return Math.min(size, MAX_PAGE_SIZE);
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    // in stage
    public static StageCounts computeCounts(List<ExamRegistrationDTO> all, Set<Integer> practicalStageIds) {
        StageCounts counts = new StageCounts();
        if (all == null) {
            return counts;
        }
        counts.total = all.size();
        for (ExamRegistrationDTO c : all) {
            if (inStage(c, STAGE_WAITING, practicalStageIds, null)) {
                counts.waiting++;
            }
            if (inStage(c, STAGE_THEORY, practicalStageIds, null)) {
                counts.theory++;
            }
            if (inStage(c, STAGE_PRACTICAL, practicalStageIds, null)) {
                counts.practical++;
            }
            if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_PASS)) {
                counts.pass++;
            }
            if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_FAIL)) {
                counts.fail++;
            }
            if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_SUSPENDED)) {
                counts.suspended++;
            }
        }
        return counts;
    }

    // filter for stage
    public static boolean inStage(ExamRegistrationDTO c, String stage,
            Set<Integer> practicalStageIds, String resultFilter) {
        if (c == null || stage == null) {
            return false;
        }
        return switch (stage) {
            case STAGE_WAITING -> !c.isProcedureComplete() && !c.isSuspended()
                    && !c.isExamFinished() && !c.isAbsent();
            case STAGE_THEORY -> c.isProcedureComplete()
                    && "none".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))
                    && !c.isAbsent();
            case STAGE_PRACTICAL -> practicalStageIds != null
                    && practicalStageIds.contains(c.getId());
            case STAGE_RESULTS -> {
                if (RESULT_SUSPENDED.equals(resultFilter)) {
                    yield c.isSuspended();
                }
                if (!c.isExamFinished()) {
                    yield false;
                }
                if (RESULT_FAIL.equals(resultFilter)) {
                    yield !c.isSuspended() && !c.isFinalPass();
                }
                yield !c.isSuspended() && c.isFinalPass();
            }
            default -> false;
        };
    }
    // paginate

    public static List<ExamRegistrationDTO> filterForStage(List<ExamRegistrationDTO> all, String stage,
            Set<Integer> practicalStageIds, String resultFilter) {
        List<ExamRegistrationDTO> out = new ArrayList<>();
        if (all == null) {
            return out;
        }
        for (ExamRegistrationDTO c : all) {
            if (inStage(c, stage, practicalStageIds, resultFilter)) {
                out.add(c);
            }
        }
        return out;
    }
    // matches search

    public static List<ExamRegistrationDTO> filterSearch(List<ExamRegistrationDTO> list, String query) {
        if (list == null || list.isEmpty()) {
            return list == null ? List.of() : list;
        }
        if (query == null || query.isBlank()) {
            return list;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<ExamRegistrationDTO> out = new ArrayList<>();
        for (ExamRegistrationDTO c : list) {
            if (matchesSearch(c, q)) {
                out.add(c);
            }
        }
        return out;
    }

    /**
     * Xác định phần hiện tại của thí sinh (ưu tiên kết quả -> sa hình -> LT -> chờ).
     * Trả về key: waiting | theory | practical | results-pass | results-fail | results-suspended | unknown.
     */
    public static String resolveCurrentStageKey(ExamRegistrationDTO c, Set<Integer> practicalStageIds) {
        if (c == null) {
            return "unknown";
        }
        if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_PASS)) {
            return "results-pass";
        }
        if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_FAIL)) {
            return "results-fail";
        }
        if (inStage(c, STAGE_RESULTS, practicalStageIds, RESULT_SUSPENDED)) {
            return "results-suspended";
        }
        if (inStage(c, STAGE_PRACTICAL, practicalStageIds, null)) {
            return STAGE_PRACTICAL;
        }
        if (inStage(c, STAGE_THEORY, practicalStageIds, null)) {
            return STAGE_THEORY;
        }
        if (inStage(c, STAGE_WAITING, practicalStageIds, null)) {
            return STAGE_WAITING;
        }
        return "unknown";
    }

    public static String stageLabel(String stageKey) {
        if (stageKey == null) {
            return "Chưa xác định";
        }
        return switch (stageKey) {
            case STAGE_WAITING -> "Phòng chờ chính";
            case STAGE_THEORY -> "Phòng thi lý thuyết";
            case STAGE_PRACTICAL -> "Thực hành / Sa hình";
            case "results-pass" -> "Đỗ sát hạch";
            case "results-fail" -> "Trượt / vắng";
            case "results-suspended" -> "Đình chỉ";
            default -> "Chưa xác định";
        };
    }

    public static String stageServletPath(String stageKey) {
        if (stageKey == null) {
            return "/views/staff/examstaff/allocation";
        }
        return switch (stageKey) {
            case STAGE_WAITING -> "/views/staff/examstaff/allocation-waiting";
            case STAGE_THEORY -> "/views/staff/examstaff/allocation-theory";
            case STAGE_PRACTICAL -> "/views/staff/examstaff/allocation-practical";
            case "results-pass" -> "/views/staff/examstaff/allocation-results-pass";
            case "results-fail" -> "/views/staff/examstaff/allocation-results-fail";
            case "results-suspended" -> "/views/staff/examstaff/allocation-results-suspended";
            default -> "/views/staff/examstaff/allocation";
        };
    }

    public static PageSlice<ExamRegistrationDTO> paginate(List<ExamRegistrationDTO> list, int page, int pageSize) {
        if (list == null || list.isEmpty()) {
            return new PageSlice<>(List.of(), page, pageSize, 0);
        }
        int total = list.size();
        int safePage = Math.max(1, page);
        int from = (safePage - 1) * pageSize;
    // digits only
        if (from >= total) {
            safePage = Math.max(1, (int) Math.ceil((double) total / pageSize));
            from = (safePage - 1) * pageSize;
        }
        int to = Math.min(from + pageSize, total);
        return new PageSlice<>(list.subList(from, to), safePage, pageSize, total);
    }

    private static boolean matchesSearch(ExamRegistrationDTO c, String q) {
        String qDigits = digitsOnly(q);
        if (!qDigits.isEmpty() && qDigits.equals(q)) {
    // matches sbd numeric
            if (qDigits.length() <= 4) {
                return matchesSbdNumeric(c.getSbd(), qDigits);
            }
            if (contains(c.getGovIdNo(), q)) {
                return true;
            }
            return contains(c.getPhoneNo(), q);
        }
        if (contains(c.getSbd(), q)) {
            return true;
        }
        if (contains(c.getFullName(), q)) {
            return true;
        }
        if (contains(c.getGovIdNo(), q)) {
            return true;
        }
    // contains
        if (contains(c.getPhoneNo(), q)) {
            return true;
        }
    // null to pass
        return contains(c.getLicenseCode(), q);
    }

    // url encode
    private static String digitsOnly(String q) {
        if (q == null || q.isBlank()) {
            return "";
        }
        for (int i = 0; i < q.length(); i++) {
            if (!Character.isDigit(q.charAt(i))) {
                return "";
            }
        }
        return q;
    }

    private static boolean matchesSbdNumeric(String sbd, String qDigits) {
        if (sbd == null || sbd.isBlank() || qDigits == null || qDigits.isBlank()) {
            return false;
        }
        String sbdDigits = sbd.replaceAll("\\D", "");
        if (sbdDigits.isEmpty()) {
            return false;
        }
        try {
            if (Integer.parseInt(sbdDigits) == Integer.parseInt(qDigits)) {
                return true;
            }
        } catch (NumberFormatException ignored) {

        }
        return sbdDigits.startsWith(qDigits) || sbdDigits.endsWith(qDigits);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private static String nullToPass(String v) {
        return v == null || v.isBlank() ? "none" : v.trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
