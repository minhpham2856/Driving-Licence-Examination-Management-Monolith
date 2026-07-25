package registrant.util;

import registrant.dto.RegistrantExamSessionOption;
import registrant.dto.RegistrantFilterOption;
import registrant.dto.RegistrantLicenceOption;
import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Parse tham số bộ lọc HTTP và dựng danh sách RegistrantFilterOption theo từng trang.
 * Hỗ trợ dashboard (đợt thi đã đăng ký), my-exams, register-exam (ExamDates) và track-profile; state nội bộ ExamListFilterState, SessionListFilterState.
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
        private final LocalDate fromDateParsed;
        private final LocalDate toDateParsed;
        private final String filterDateError;
        private final List<RegistrantFilterOption> locationFilterOptions;
        private final boolean searchActive;

        public SessionListFilterState(String searchQuery, String locationFilter,
                String fromDate, String toDate,
                LocalDate fromDateParsed, LocalDate toDateParsed, String filterDateError,
                List<RegistrantFilterOption> locationFilterOptions, boolean searchActive) {
            this.searchQuery = searchQuery;
            this.locationFilter = locationFilter;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.fromDateParsed = fromDateParsed;
            this.toDateParsed = toDateParsed;
            this.filterDateError = filterDateError;
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

        public LocalDate getFromDateParsed() {
            return fromDateParsed;
        }

        public LocalDate getToDateParsed() {
            return toDateParsed;
        }

        public String getFilterDateError() {
            return filterDateError;
        }

        public boolean hasFilterDateError() {
            return filterDateError != null && !filterDateError.isBlank();
        }

        public List<RegistrantFilterOption> getLocationFilterOptions() {
            return locationFilterOptions;
        }

        public boolean isSearchActive() {
            return searchActive;
        }
    }

    private static final Map<String, StatusDefinition> DASHBOARD_STATUS = linkedStatusMap(
            registeredStatus("pending", "Nguyện vọng / chờ xét duyệt",
                    exam -> exam.isPreferredDate()
                            || exam.isSbdPending()
                            || "pending".equals(exam.getStatusClass())),
            registeredStatus("approved_waiting", "Đã xếp lịch — chờ ngày thi",
                    exam -> !exam.isPreferredDate()
                            && "info".equals(exam.getStatusClass())
                            && !exam.isSbdPending()),
            registeredStatus("passed", "Đã hoàn thành", exam -> "approved".equals(exam.getStatusClass())),
            registeredStatus("failed", "Không đạt / từ chối",
                    exam -> "rejected".equals(exam.getStatusClass()) || "danger".equals(exam.getStatusClass()))
    );

    private static final Map<String, StatusDefinition> MY_EXAMS_STATUS = linkedStatusMap(
            myExamStatus("pending", "Nguyện vọng / chờ xét duyệt",
                    exam -> exam.isPreferredDate()
                            || exam.isSbdPending()
                            || "Chờ xét duyệt".equals(exam.getStatusLabel())
                            || RegistrantExamSupport.PREFERRED_DATE_STATUS_LABEL.equals(exam.getStatusLabel())),
            myExamStatus("approved_waiting", "Đã xếp lịch — chờ ngày thi",
                    exam -> !exam.isPreferredDate()
                            && (RegistrantExamSupport.SCHEDULED_WAITING_STATUS_LABEL.equals(exam.getStatusLabel())
                                    || "Được xét duyệt".equals(exam.getStatusLabel()))),
            myExamStatus("awaiting_result", "Đã thi — chờ công bố kết quả",
                    exam -> RegistrantExamSupport.AWAITING_RESULT_STATUS_LABEL.equals(exam.getStatusLabel())
                            || "Chờ công bố".equals(exam.getStatusLabel())
                            || "Đã thi".equals(exam.getStatusLabel())),
            myExamStatus("passed", "Đạt", exam -> "Đạt".equals(exam.getStatusLabel())),
            myExamStatus("failed", "Trượt / Từ chối",
                    exam -> "Trượt".equals(exam.getStatusLabel())
                            || "Bị từ chối".equals(exam.getStatusLabel())
                            || "rejected".equals(exam.getStatusClass())
                            || "danger".equals(exam.getStatusClass()))
    );

    private RegistrantFilterSupport() {
    }

    /** Parse bộ lọc danh sách đăng ký từ request. */
    public static ExamListFilterState parseRegisteredExamFilter(HttpServletRequest request,
            List<RegistrantRegisteredExamRow> allExams) {
        return parseRegisteredExamFilter(request, allExams, List.of());
    }

    /** Parse bộ lọc đăng ký kèm danh sách hạng từ catalogue. */
    public static ExamListFilterState parseRegisteredExamFilter(HttpServletRequest request,
            List<RegistrantRegisteredExamRow> allExams, List<String> allLicenceValues) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> licenceValues = resolveLicenceFilterValues(allLicenceValues,
                collectLicenceValuesFromRegistered(allExams));
        String licenceFilter = normalizeLicenceFilter(
                RegistrantListFilter.trimParam(request.getParameter("licence")), licenceValues);

        List<String> availableStatusValues = examStatusKeys(DASHBOARD_STATUS);
        String rawStatus = RegistrantListFilter.trimParam(request.getParameter("status"));
        String statusFilter = normalizeExamStatusFilter(rawStatus, availableStatusValues, "all");
        List<RegistrantFilterOption> statusOptions = buildRegisteredStatusOptions(allExams, statusFilter);
        List<RegistrantFilterOption> licenceOptions = buildLicenceOptions(licenceValues, licenceFilter);
        boolean searchActive = RegistrantListFilter.hasActivefilter(searchQuery, statusFilter, licenceFilter);

        return new ExamListFilterState(searchQuery, statusFilter, licenceFilter,
                statusOptions, licenceOptions, searchActive);
    }

    /** Parse bộ lọc my-exams từ request. */
    public static ExamListFilterState parseMyExamFilter(HttpServletRequest request,
            List<RegistrantMyExamRow> allExams) {
        return parseMyExamFilter(request, allExams, List.of());
    }

    /** Parse bộ lọc my-exams kèm catalogue hạng. */
    public static ExamListFilterState parseMyExamFilter(HttpServletRequest request,
            List<RegistrantMyExamRow> allExams, List<String> allLicenceValues) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> licenceValues = resolveLicenceFilterValues(allLicenceValues,
                collectLicenceValuesFromMyExams(allExams));
        String licenceFilter = normalizeLicenceFilter(
                RegistrantListFilter.trimParam(request.getParameter("licence")), licenceValues);

        List<String> availableStatusValues = examStatusKeys(MY_EXAMS_STATUS);
        String rawStatus = RegistrantListFilter.trimParam(request.getParameter("status"));
        String statusFilter = normalizeExamStatusFilter(rawStatus, availableStatusValues, "all");
        List<RegistrantFilterOption> statusOptions = buildMyExamStatusOptions(statusFilter);
        List<RegistrantFilterOption> licenceOptions = buildLicenceOptions(licenceValues, licenceFilter);
        boolean searchActive = RegistrantListFilter.hasActivefilter(searchQuery, statusFilter, licenceFilter);

        return new ExamListFilterState(searchQuery, statusFilter, licenceFilter,
                statusOptions, licenceOptions, searchActive);
    }

    /** Lấy mã hạng từ danh sách RegistrantLicenceOption. */
    public static List<String> collectLicenceCodesFromCatalogue(List<RegistrantLicenceOption> catalogue) {
        TreeSet<String> codes = new TreeSet<>();
        if (catalogue != null) {
            for (RegistrantLicenceOption option : catalogue) {
                if (option.getCode() != null && !option.getCode().isBlank()) {
                    codes.add(option.getCode().trim());
                }
            }
        }
        return new ArrayList<>(codes);
    }

    private static List<String> resolveLicenceFilterValues(List<String> catalogue, List<String> fallback) {
        if (catalogue != null && !catalogue.isEmpty()) {
            return catalogue;
        }
        return fallback != null ? fallback : List.of();
    }

    /** Parse bộ lọc đợt thi (search/location/from-to). */
    public static SessionListFilterState parseSessionFilter(HttpServletRequest request,
            List<RegistrantExamSessionOption> allSessions) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        List<String> locationValues = RegistrantListFilter.collectSessionLocations(allSessions);
        String locationFilter = normalizeLocationFilter(
                RegistrantListFilter.trimParam(request.getParameter("location")), locationValues);

        String fromRaw = RegistrantListFilter.trimParam(request.getParameter("fromDate"));
        String toRaw = RegistrantListFilter.trimParam(request.getParameter("toDate"));
        String filterDateError = RegistrantDateSupport.validateDateRange(fromRaw, toRaw);

        LocalDate fromParsed = filterDateError == null ? RegistrantDateSupport.parse(fromRaw) : null;
        LocalDate toParsed = filterDateError == null ? RegistrantDateSupport.parse(toRaw) : null;

        String fromDisplay = RegistrantDateSupport.displayValue(fromRaw);
        String toDisplay = RegistrantDateSupport.displayValue(toRaw);

        List<RegistrantFilterOption> locationOptions = buildLocationOptions(locationValues, locationFilter);
        boolean searchActive = RegistrantListFilter.hasSessionActivefilter(
                searchQuery, locationFilter, fromDisplay, toDisplay);

        return new SessionListFilterState(searchQuery, locationFilter, fromDisplay, toDisplay,
                fromParsed, toParsed, filterDateError, locationOptions, searchActive);
    }

    /** Gắn state lọc danh sách thi lên request attributes. */
    public static void applyExamListFilter(HttpServletRequest request, ExamListFilterState state) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("statusFilter", state.getStatusFilter());
        request.setAttribute("licenceFilter", state.getLicenceFilter());
        request.setAttribute("statusFilterOptions", state.getStatusFilterOptions());
        request.setAttribute("licenceFilterOptions", state.getLicenceFilterOptions());
        request.setAttribute("searchActive", state.isSearchActive());
    }

    /** Gắn state lọc danh sách thi vào map model. */
    public static void applyExamListFilter(Map<String, Object> model, ExamListFilterState state) {
        model.put("searchQuery", state.getSearchQuery());
        model.put("statusFilter", state.getStatusFilter());
        model.put("licenceFilter", state.getLicenceFilter());
        model.put("statusFilterOptions", state.getStatusFilterOptions());
        model.put("licenceFilterOptions", state.getLicenceFilterOptions());
        model.put("searchActive", state.isSearchActive());
    }

    /** Gắn state lọc ca/ngày thi lên request. */
    public static void applySessionListFilter(HttpServletRequest request, SessionListFilterState state) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("sessionSearchQuery", state.getSearchQuery());
        request.setAttribute("locationFilter", state.getLocationFilter());
        request.setAttribute("locationFilterOptions", state.getLocationFilterOptions());
        request.setAttribute("fromDate", state.getFromDate());
        request.setAttribute("toDate", state.getToDate());
        request.setAttribute("fromDateIso", RegistrantDateSupport.toIsoValue(state.getFromDateParsed()));
        request.setAttribute("toDateIso", RegistrantDateSupport.toIsoValue(state.getToDateParsed()));
        request.setAttribute("filterDateError", state.getFilterDateError());
        request.setAttribute("searchActive", state.isSearchActive());
    }

    /** Kiểm tra row đăng ký có khớp statusFilter không. */
    public static boolean matchesRegisteredExamStatus(RegistrantRegisteredExamRow exam, String statusFilter) {
        if (isAllChoice(statusFilter)) {
            return true;
        }
        StatusDefinition def = DASHBOARD_STATUS.get(statusFilter.toLowerCase(Locale.ROOT));
        return def != null && def.registeredMatcher.test(exam);
    }

    /** Kiểm tra my-exam row có khớp statusFilter không. */
    public static boolean matchesMyExamStatus(RegistrantMyExamRow exam, String statusFilter) {
        if (isAllChoice(statusFilter)) {
            return true;
        }
        StatusDefinition def = MY_EXAMS_STATUS.get(statusFilter.toLowerCase(Locale.ROOT));
        return def != null && def.myExamMatcher.test(exam);
    }

    /** Collect mã hạng distinct từ registered exams. */
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

    /** Collect mã hạng distinct từ my-exams. */
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

    private static List<RegistrantFilterOption> buildMyExamStatusOptions(String selectedStatus) {
        return buildExamStatusOptions(MY_EXAMS_STATUS, normalizeMyExamStatusKey(selectedStatus));
    }

    private static String normalizeMyExamStatusKey(String raw) {
        if (raw == null || raw.isBlank() || isAllChoice(raw)) {
            return "all";
        }
        String trimmed = raw.trim();
        if ("exam_taken".equalsIgnoreCase(trimmed)) {
            return "awaiting_result";
        }
        return trimmed;
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
        if ("exam_taken".equalsIgnoreCase(trimmed)) {
            trimmed = "awaiting_result";
        }
        return normalizeChoice(trimmed, validValues, defaultValue);
    }

    private static List<RegistrantFilterOption> buildLicenceOptions(
            List<String> licenceValues, String selectedLicence) {
        String selected = selectedLicence != null ? selectedLicence : "all";
        TreeSet<String> values = new TreeSet<>();
        if (licenceValues != null) {
            for (String licence : licenceValues) {
                if (licence != null && !licence.isBlank()) {
                    values.add(licence.trim());
                }
            }
        }
        if (!isAllChoice(selected)) {
            values.add(selected.trim());
        }
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selected)));
        for (String licence : values) {
            options.add(new RegistrantFilterOption(licence, "Hạng " + licence, licence.equals(selected)));
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

    private static String normalizeDateParam(String raw) {
        String trimmed = RegistrantListFilter.trimParam(raw);
        if (trimmed.isEmpty()) {
            return "";
        }
        try {
            return RegistrantDateSupport.format(RegistrantDateSupport.parse(trimmed));
        } catch (Exception ex) {
            return trimmed;
        }
    }

    private static boolean isAllChoice(String value) {
        return value == null || value.isBlank() || "all".equalsIgnoreCase(value.trim());
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
