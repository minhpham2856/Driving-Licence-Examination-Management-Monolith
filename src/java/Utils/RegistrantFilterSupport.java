package Utils;

import Models.RegistrantExamSessionOption;
import Models.RegistrantFilterOption;
import Models.RegistrantMyExamRow;
import Models.RegistrantRegisteredExamRow;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Parse tham số bộ lọc, chuẩn hóa giá trị và dựng danh sách option theo từng ngữ cảnh trang.
 */
public final class RegistrantFilterSupport {

    public static final class ExamListFilterState {
        private final String searchQuery;
        private final String statusFilter;
        private final String licenceFilter;
        private final List<RegistrantFilterOption> statusFilterOptions;
        private final List<RegistrantFilterOption> licenceFilterOptions;
        private final boolean searchActive;

        public ExamListFilterState(String searchQuery, String statusFilter, String licenceFilter,
                List<RegistrantFilterOption> statusFilterOptions,
                List<RegistrantFilterOption> licenceFilterOptions, boolean searchActive) {
            this.searchQuery = searchQuery;
            this.statusFilter = statusFilter;
            this.licenceFilter = licenceFilter;
            this.statusFilterOptions = statusFilterOptions;
            this.licenceFilterOptions = licenceFilterOptions;
            this.searchActive = searchActive;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public String getStatusFilter() {
            return statusFilter;
        }

        public String getLicenceFilter() {
            return licenceFilter;
        }

        public List<RegistrantFilterOption> getStatusFilterOptions() {
            return statusFilterOptions;
        }

        public List<RegistrantFilterOption> getLicenceFilterOptions() {
            return licenceFilterOptions;
        }

        public boolean isSearchActive() {
            return searchActive;
        }
    }

    public static final class SessionListFilterState {
        private final String searchQuery;
        private final String locationFilter;
        private final String fromDate;
        private final String toDate;
        private final List<RegistrantFilterOption> locationFilterOptions;
        private final boolean searchActive;

        public SessionListFilterState(String searchQuery, String locationFilter,
                String fromDate, String toDate,
                List<RegistrantFilterOption> locationFilterOptions, boolean searchActive) {
            this.searchQuery = searchQuery;
            this.locationFilter = locationFilter;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.locationFilterOptions = locationFilterOptions;
            this.searchActive = searchActive;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public String getLocationFilter() {
            return locationFilter;
        }

        public String getFromDate() {
            return fromDate;
        }

        public String getToDate() {
            return toDate;
        }

        public List<RegistrantFilterOption> getLocationFilterOptions() {
            return locationFilterOptions;
        }

        public boolean isSearchActive() {
            return searchActive;
        }
    }

    public static final class AuditFilterState {
        private final String searchQuery;
        private final String actionFilter;
        private final String fromDate;
        private final String toDate;
        private final int page;
        private final List<RegistrantFilterOption> actionFilterOptions;
        private final boolean searchActive;

        public AuditFilterState(String searchQuery, String actionFilter,
                String fromDate, String toDate, int page,
                List<RegistrantFilterOption> actionFilterOptions, boolean searchActive) {
            this.searchQuery = searchQuery;
            this.actionFilter = actionFilter;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.page = page;
            this.actionFilterOptions = actionFilterOptions;
            this.searchActive = searchActive;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public String getActionFilter() {
            return actionFilter;
        }

        public String getFromDate() {
            return fromDate;
        }

        public String getToDate() {
            return toDate;
        }

        public int getPage() {
            return page;
        }

        public List<RegistrantFilterOption> getActionFilterOptions() {
            return actionFilterOptions;
        }

        public boolean isSearchActive() {
            return searchActive;
        }
    }

    private static final Map<String, StatusDefinition> DASHBOARD_STATUS = linkedStatusMap(
            registeredStatus("pending", "Chờ xét duyệt",
                    exam -> exam.isSbdPending() || "pending".equals(exam.getStatusClass())),
            registeredStatus("approved_waiting", "Được xét duyệt chờ thi",
                    exam -> "info".equals(exam.getStatusClass()) && !exam.isSbdPending()),
            registeredStatus("passed", "Đã hoàn thành", exam -> "approved".equals(exam.getStatusClass())),
            registeredStatus("failed", "Không đạt / từ chối",
                    exam -> "rejected".equals(exam.getStatusClass()) || "danger".equals(exam.getStatusClass()))
    );

    private static final Map<String, StatusDefinition> MY_EXAMS_STATUS = linkedStatusMap(
            myExamStatus("pending", "Chờ xét duyệt",
                    exam -> exam.isSbdPending() || "Chờ xét duyệt".equals(exam.getStatusLabel())),
            myExamStatus("approved_waiting", "Được xét duyệt chờ thi",
                    exam -> "Được xét duyệt".equals(exam.getStatusLabel())),
            myExamStatus("exam_taken", "Đã thi",
                    exam -> "Đã thi".equals(exam.getStatusLabel())),
            myExamStatus("passed", "Đã đạt", exam -> "Đạt".equals(exam.getStatusLabel())),
            myExamStatus("failed", "Không đạt",
                    exam -> "Trượt".equals(exam.getStatusLabel())
                            || "Bị từ chối".equals(exam.getStatusLabel())
                            || "rejected".equals(exam.getStatusClass())
                            || "danger".equals(exam.getStatusClass()))
    );

    private RegistrantFilterSupport() {
    }

    public static ExamListFilterState parseRegisteredExamFilter(HttpServletRequest request,
            List<RegistrantRegisteredExamRow> allExams) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> licenceValues = collectLicenceValuesFromRegistered(allExams);
        String licenceFilter = normalizeLicenceFilter(
                RegistrantListFilter.trimParam(request.getParameter("licence")), licenceValues);

        List<String> availableStatusValues = examStatusKeys(DASHBOARD_STATUS);
        String rawStatus = RegistrantListFilter.trimParam(request.getParameter("status"));
        String statusFilter = normalizeExamStatusFilter(rawStatus, availableStatusValues, "all");
        List<RegistrantFilterOption> statusOptions = buildRegisteredStatusOptions(allExams, null);

        statusOptions = buildRegisteredStatusOptions(allExams, statusFilter);
        List<RegistrantFilterOption> licenceOptions = buildLicenceOptions(licenceValues, licenceFilter);
        boolean searchActive = RegistrantListFilter.hasActiveFilters(searchQuery, statusFilter, licenceFilter);

        return new ExamListFilterState(searchQuery, statusFilter, licenceFilter,
                statusOptions, licenceOptions, searchActive);
    }

    public static ExamListFilterState parseMyExamFilter(HttpServletRequest request,
            List<RegistrantMyExamRow> allExams) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> licenceValues = collectLicenceValuesFromMyExams(allExams);
        String licenceFilter = normalizeLicenceFilter(
                RegistrantListFilter.trimParam(request.getParameter("licence")), licenceValues);

        List<String> availableStatusValues = examStatusKeys(MY_EXAMS_STATUS);
        String rawStatus = RegistrantListFilter.trimParam(request.getParameter("status"));
        String statusFilter = normalizeExamStatusFilter(rawStatus, availableStatusValues, "all");
        List<RegistrantFilterOption> statusOptions = buildMyExamStatusOptions(allExams, null);

        statusOptions = buildMyExamStatusOptions(allExams, statusFilter);
        List<RegistrantFilterOption> licenceOptions = buildLicenceOptions(licenceValues, licenceFilter);
        boolean searchActive = RegistrantListFilter.hasActiveFilters(searchQuery, statusFilter, licenceFilter);

        return new ExamListFilterState(searchQuery, statusFilter, licenceFilter,
                statusOptions, licenceOptions, searchActive);
    }

    public static SessionListFilterState parseSessionFilter(HttpServletRequest request,
            List<RegistrantExamSessionOption> allSessions) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> locationValues = RegistrantListFilter.collectSessionLocations(allSessions);
        String locationFilter = normalizeLocationFilter(
                RegistrantListFilter.trimParam(request.getParameter("location")), locationValues);
        String fromDate = normalizeDateParam(request.getParameter("fromDate"));
        String toDate = normalizeDateParam(request.getParameter("toDate"));

        List<RegistrantFilterOption> locationOptions = buildLocationOptions(locationValues, locationFilter);
        boolean searchActive = RegistrantListFilter.hasSessionActiveFilters(
                searchQuery, locationFilter, fromDate, toDate);

        return new SessionListFilterState(searchQuery, locationFilter, fromDate, toDate,
                locationOptions, searchActive);
    }

    public static AuditFilterState parseAuditFilter(HttpServletRequest request, List<String> validActions) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        String fromDate = normalizeDateParam(request.getParameter("fromDate"));
        String toDate = normalizeDateParam(request.getParameter("toDate"));
        int page = parsePage(request.getParameter("page"));

        List<String> actionValues = normalizeActionValues(validActions);
        String actionFilter = normalizeActionFilter(
                RegistrantListFilter.trimParam(request.getParameter("action")), actionValues);
        List<RegistrantFilterOption> actionOptions = buildActionOptions(actionValues, actionFilter);
        boolean searchActive = hasAuditActiveFilters(searchQuery, actionFilter, fromDate, toDate);

        return new AuditFilterState(searchQuery, actionFilter, fromDate, toDate, page,
                actionOptions, searchActive);
    }

    public static void applyExamListFilter(HttpServletRequest request, ExamListFilterState state) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("statusFilter", state.getStatusFilter());
        request.setAttribute("licenceFilter", state.getLicenceFilter());
        request.setAttribute("statusFilterOptions", state.getStatusFilterOptions());
        request.setAttribute("licenceFilterOptions", state.getLicenceFilterOptions());
        request.setAttribute("searchActive", state.isSearchActive());
    }

    public static void applyExamListFilter(Map<String, Object> model, ExamListFilterState state) {
        model.put("searchQuery", state.getSearchQuery());
        model.put("statusFilter", state.getStatusFilter());
        model.put("licenceFilter", state.getLicenceFilter());
        model.put("statusFilterOptions", state.getStatusFilterOptions());
        model.put("licenceFilterOptions", state.getLicenceFilterOptions());
        model.put("searchActive", state.isSearchActive());
    }

    public static void applySessionListFilter(HttpServletRequest request, SessionListFilterState state) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("sessionSearchQuery", state.getSearchQuery());
        request.setAttribute("locationFilter", state.getLocationFilter());
        request.setAttribute("locationFilterOptions", state.getLocationFilterOptions());
        request.setAttribute("fromDate", state.getFromDate());
        request.setAttribute("toDate", state.getToDate());
        request.setAttribute("searchActive", state.isSearchActive());
    }

    public static void applyAuditFilter(HttpServletRequest request, AuditFilterState state) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("actionFilter", state.getActionFilter());
        request.setAttribute("actionFilterOptions", state.getActionFilterOptions());
        request.setAttribute("fromDate", state.getFromDate());
        request.setAttribute("toDate", state.getToDate());
        request.setAttribute("auditPage", state.getPage());
        request.setAttribute("searchActive", state.isSearchActive());
    }

    public static boolean matchesRegisteredExamStatus(RegistrantRegisteredExamRow exam, String statusFilter) {
        if (isAllChoice(statusFilter)) {
            return true;
        }
        StatusDefinition def = DASHBOARD_STATUS.get(statusFilter.toLowerCase(Locale.ROOT));
        return def != null && def.registeredMatcher.test(exam);
    }

    public static boolean matchesMyExamStatus(RegistrantMyExamRow exam, String statusFilter) {
        if (isAllChoice(statusFilter)) {
            return true;
        }
        StatusDefinition def = MY_EXAMS_STATUS.get(statusFilter.toLowerCase(Locale.ROOT));
        return def != null && def.myExamMatcher.test(exam);
    }

    public static String auditActionLabel(String action) {
        if (action == null || action.isBlank()) {
            return "Khác";
        }
        return switch (action.toUpperCase(Locale.ROOT)) {
            case "UPLOAD" -> "Tải lên";
            case "REQUEST" -> "Gửi duyệt";
            case "APPROVE" -> "Duyệt";
            case "REJECT" -> "Từ chối";
            case "INSERT" -> "Thêm mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "EXPORT" -> "Xuất";
            case "WARNING" -> "Cảnh báo";
            default -> action;
        };
    }

    public static List<String> collectLicenceValuesFromRegistered(List<RegistrantRegisteredExamRow> exams) {
        TreeSet<String> licences = new TreeSet<>();
        if (exams != null) {
            for (RegistrantRegisteredExamRow exam : exams) {
                if (exam.getLicenceClass() != null && !exam.getLicenceClass().isBlank()) {
                    licences.add(exam.getLicenceClass().trim());
                }
            }
        }
        return new ArrayList<>(licences);
    }

    public static List<String> collectLicenceValuesFromMyExams(List<RegistrantMyExamRow> exams) {
        TreeSet<String> licences = new TreeSet<>();
        if (exams != null) {
            for (RegistrantMyExamRow exam : exams) {
                if (exam.getLicenceClass() != null && !exam.getLicenceClass().isBlank()) {
                    licences.add(exam.getLicenceClass().trim());
                }
            }
        }
        return new ArrayList<>(licences);
    }

    private static List<RegistrantFilterOption> buildRegisteredStatusOptions(
            List<RegistrantRegisteredExamRow> exams, String selectedStatus) {
        return buildExamStatusOptions(DASHBOARD_STATUS, selectedStatus);
    }

    private static List<RegistrantFilterOption> buildMyExamStatusOptions(
            List<RegistrantMyExamRow> exams, String selectedStatus) {
        return buildExamStatusOptions(MY_EXAMS_STATUS, selectedStatus);
    }

    private static List<RegistrantFilterOption> buildExamStatusOptions(
            Map<String, StatusDefinition> definitions, String selectedStatus) {
        String selected = selectedStatus != null ? selectedStatus : "all";
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selected)));
        for (StatusDefinition def : definitions.values()) {
            options.add(new RegistrantFilterOption(def.value, def.label, def.value.equals(selected)));
        }
        return options;
    }

    private static List<String> examStatusKeys(Map<String, StatusDefinition> definitions) {
        return new ArrayList<>(definitions.keySet());
    }

    private static String normalizeExamStatusFilter(String raw, List<String> validValues, String defaultValue) {
        if (raw == null || raw.isBlank() || isAllChoice(raw)) {
            return defaultValue;
        }
        String trimmed = raw.trim();
        if ("upcoming".equalsIgnoreCase(trimmed)) {
            trimmed = "approved_waiting";
        }
        return normalizeChoice(trimmed, validValues, defaultValue);
    }

    private static List<RegistrantFilterOption> buildLicenceOptions(
            List<String> licenceValues, String selectedLicence) {
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selectedLicence)));
        for (String licence : licenceValues) {
            options.add(new RegistrantFilterOption(licence, "Hạng " + licence, licence.equals(selectedLicence)));
        }
        return options;
    }

    private static List<RegistrantFilterOption> buildLocationOptions(
            List<String> locationValues, String selectedLocation) {
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selectedLocation)));
        for (String location : locationValues) {
            options.add(new RegistrantFilterOption(location, location, location.equals(selectedLocation)));
        }
        return options;
    }

    private static List<RegistrantFilterOption> buildActionOptions(
            List<String> actionValues, String selectedAction) {
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selectedAction)));
        for (String action : actionValues) {
            options.add(new RegistrantFilterOption(action, auditActionLabel(action), action.equals(selectedAction)));
        }
        return options;
    }

    private static List<String> normalizeActionValues(List<String> validActions) {
        TreeSet<String> actions = new TreeSet<>();
        if (validActions != null) {
            for (String action : validActions) {
                if (action != null && !action.isBlank()) {
                    actions.add(action.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        return new ArrayList<>(actions);
    }

    private static String normalizeChoice(String raw, List<String> validValues, String defaultValue) {
        if (raw == null || raw.isBlank() || isAllChoice(raw)) {
            return defaultValue;
        }
        String trimmed = raw.trim();
        for (String valid : validValues) {
            if (valid.equalsIgnoreCase(trimmed)) {
                return valid;
            }
        }
        return defaultValue;
    }

    private static String normalizeLicenceFilter(String raw, List<String> validLicences) {
        return normalizeChoice(raw, validLicences, "all");
    }

    private static String normalizeLocationFilter(String raw, List<String> validLocations) {
        return normalizeChoice(raw, validLocations, "all");
    }

    private static String normalizeActionFilter(String raw, List<String> validActions) {
        if (raw == null || raw.isBlank() || isAllChoice(raw)) {
            return "all";
        }
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        return validActions.contains(upper) ? upper : "all";
    }

    private static String normalizeDateParam(String raw) {
        String trimmed = RegistrantListFilter.trimParam(raw);
        if (trimmed.isEmpty()) {
            return "";
        }
        try {
            LocalDate.parse(trimmed);
            return trimmed;
        } catch (DateTimeParseException ex) {
            return "";
        }
    }

    private static boolean isAllChoice(String value) {
        return value == null || value.isBlank() || "all".equalsIgnoreCase(value.trim());
    }

    private static boolean hasAuditActiveFilters(String q, String action, String from, String to) {
        return !RegistrantListFilter.trimParam(q).isEmpty()
                || (!isAllChoice(action))
                || !RegistrantListFilter.trimParam(from).isEmpty()
                || !RegistrantListFilter.trimParam(to).isEmpty();
    }

    private static int parsePage(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(pageParam.trim()));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    @SafeVarargs
    private static Map<String, StatusDefinition> linkedStatusMap(StatusDefinition... defs) {
        Map<String, StatusDefinition> map = new LinkedHashMap<>();
        for (StatusDefinition def : defs) {
            map.put(def.value, def);
        }
        return map;
    }

    private static StatusDefinition registeredStatus(String value, String label,
            Predicate<RegistrantRegisteredExamRow> registeredMatcher) {
        return new StatusDefinition(value, label, registeredMatcher, null);
    }

    private static StatusDefinition myExamStatus(String value, String label,
            Predicate<RegistrantMyExamRow> myExamMatcher) {
        return new StatusDefinition(value, label, null, myExamMatcher);
    }

    private static final class StatusDefinition {
        private final String value;
        private final String label;
        private final Predicate<RegistrantRegisteredExamRow> registeredMatcher;
        private final Predicate<RegistrantMyExamRow> myExamMatcher;

        private StatusDefinition(String value, String label,
                Predicate<RegistrantRegisteredExamRow> registeredMatcher,
                Predicate<RegistrantMyExamRow> myExamMatcher) {
            this.value = value;
            this.label = label;
            this.registeredMatcher = registeredMatcher;
            this.myExamMatcher = myExamMatcher;
        }
    }
}
