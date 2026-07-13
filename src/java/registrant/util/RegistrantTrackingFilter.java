package registrant.util;

import registrant.dto.RegistrantFilterOption;
import registrant.dto.RegistrantTrackingLog;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Parse và áp dụng bộ lọc nhật ký track-profile trên danh sách thống nhất (Audit + tài liệu).
 */
public final class RegistrantTrackingFilter {

    public static final class TrackingFilterState {
        private final String searchQuery;
        private final String categoryFilter;
        private final String statusFilter;
        private final String fromDate;
        private final String toDate;
        private final LocalDate fromDateParsed;
        private final LocalDate toDateParsed;
        private final String filterDateError;
        private final int page;
        private final List<RegistrantFilterOption> categoryFilterOptions;
        private final List<RegistrantFilterOption> statusFilterOptions;
        private final boolean searchActive;

        public TrackingFilterState(String searchQuery, String categoryFilter, String statusFilter,
                String fromDate, String toDate,
                LocalDate fromDateParsed, LocalDate toDateParsed, String filterDateError,
                int page,
                List<RegistrantFilterOption> categoryFilterOptions,
                List<RegistrantFilterOption> statusFilterOptions, boolean searchActive) {
            this.searchQuery = searchQuery;
            this.categoryFilter = categoryFilter;
            this.statusFilter = statusFilter;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.fromDateParsed = fromDateParsed;
            this.toDateParsed = toDateParsed;
            this.filterDateError = filterDateError;
            this.page = page;
            this.categoryFilterOptions = categoryFilterOptions;
            this.statusFilterOptions = statusFilterOptions;
            this.searchActive = searchActive;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public String getCategoryFilter() {
            return categoryFilter;
        }

        public String getStatusFilter() {
            return statusFilter;
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

        public int getPage() {
            return page;
        }

        public List<RegistrantFilterOption> getCategoryFilterOptions() {
            return categoryFilterOptions;
        }

        public List<RegistrantFilterOption> getStatusFilterOptions() {
            return statusFilterOptions;
        }

        public boolean isSearchActive() {
            return searchActive;
        }
    }

    private RegistrantTrackingFilter() {
    }

    public static TrackingFilterState parse(HttpServletRequest request, List<RegistrantTrackingLog> allLogs) {
        String searchQuery = RegistrantListFilter.trimParam(request.getParameter("q"));
        String fromRaw = RegistrantListFilter.trimParam(request.getParameter("fromDate"));
        String toRaw = RegistrantListFilter.trimParam(request.getParameter("toDate"));
        String filterDateError = RegistrantDateSupport.validateDateRange(fromRaw, toRaw);

        LocalDate fromParsed = filterDateError == null ? RegistrantDateSupport.parse(fromRaw) : null;
        LocalDate toParsed = filterDateError == null ? RegistrantDateSupport.parse(toRaw) : null;

        String fromDate = RegistrantDateSupport.displayValue(fromRaw);
        String toDate = RegistrantDateSupport.displayValue(toRaw);
        int page = parsePage(request.getParameter("page"));

        List<String> availableCategories = collectCategories(allLogs);
        String categoryFilter = normalizeChoice(
                firstNonBlank(request.getParameter("category"), request.getParameter("action")),
                availableCategories, "all");

        List<String> availableStatuses = collectStatuses(allLogs);
        String statusFilter = normalizeChoice(
                RegistrantListFilter.trimParam(request.getParameter("status")),
                availableStatuses, "all");

        List<RegistrantFilterOption> categoryOptions =
                buildCategoryOptions(availableCategories, categoryFilter);
        List<RegistrantFilterOption> statusOptions =
                buildStatusOptions(availableStatuses, statusFilter);
        boolean searchActive = hasActivefilter(searchQuery, categoryFilter, statusFilter, fromDate, toDate);

        return new TrackingFilterState(searchQuery, categoryFilter, statusFilter, fromDate, toDate,
                fromParsed, toParsed, filterDateError, page,
                categoryOptions, statusOptions, searchActive);
    }

    public static List<RegistrantTrackingLog> apply(List<RegistrantTrackingLog> logs, TrackingFilterState state) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        String q = normalizeQuery(state.getSearchQuery());
        LocalDate fromDate = state.hasFilterDateError() ? null : state.getFromDateParsed();
        LocalDate toDate = state.hasFilterDateError() ? null : state.getToDateParsed();
        List<RegistrantTrackingLog> filtered = new ArrayList<>();
        for (RegistrantTrackingLog log : logs) {
            if (!RegistrantTrackingCategories.matchesCategory(log, state.getCategoryFilter())) {
                continue;
            }
            if (!RegistrantTrackingCategories.matchesStatusFilter(log, state.getStatusFilter())) {
                continue;
            }
            if (!matchesDateRange(log, fromDate, toDate)) {
                continue;
            }
            if (q != null && !matchesSearch(log, q)) {
                continue;
            }
            filtered.add(log);
        }
        filtered.sort(Comparator.comparing(RegistrantTrackingLog::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return filtered;
    }

    public static List<RegistrantTrackingLog> paginate(List<RegistrantTrackingLog> logs, int page, int pageSize) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, pageSize);
        int from = (safePage - 1) * safeSize;
        if (from >= logs.size()) {
            return List.of();
        }
        int to = Math.min(from + safeSize, logs.size());
        return new ArrayList<>(logs.subList(from, to));
    }

    public static void applyToRequest(HttpServletRequest request, TrackingFilterState state,
            int filteredCount, int totalCount, int totalPages) {
        request.setAttribute("searchQuery", state.getSearchQuery());
        request.setAttribute("categoryFilter", state.getCategoryFilter());
        request.setAttribute("statusFilter", state.getStatusFilter());
        request.setAttribute("categoryFilterOptions", state.getCategoryFilterOptions());
        request.setAttribute("statusFilterOptions", state.getStatusFilterOptions());
        request.setAttribute("actionFilter", state.getCategoryFilter());
        request.setAttribute("actionFilterOptions", state.getCategoryFilterOptions());
        request.setAttribute("fromDate", state.getFromDate());
        request.setAttribute("toDate", state.getToDate());
        request.setAttribute("fromDateIso", RegistrantDateSupport.toIsoValue(state.getFromDateParsed()));
        request.setAttribute("toDateIso", RegistrantDateSupport.toIsoValue(state.getToDateParsed()));
        request.setAttribute("filterDateError", state.getFilterDateError());
        request.setAttribute("auditPage", state.getPage());
        request.setAttribute("auditTotalCount", filteredCount);
        request.setAttribute("trackingTotalCount", totalCount);
        request.setAttribute("filteredTrackingCount", filteredCount);
        request.setAttribute("auditTotalPages", totalPages);
        request.setAttribute("searchActive", state.isSearchActive());
    }

    private static List<String> collectCategories(List<RegistrantTrackingLog> logs) {
        Set<String> categories = new LinkedHashSet<>();
        if (logs != null) {
            for (RegistrantTrackingLog log : logs) {
                categories.add(RegistrantTrackingCategories.resolveCategory(log));
            }
        }
        categories.retainAll(RegistrantTrackingCategories.orderedCategoryKeys());
        List<String> ordered = new ArrayList<>();
        for (String key : RegistrantTrackingCategories.orderedCategoryKeys()) {
            if (categories.contains(key)) {
                ordered.add(key);
            }
        }
        return ordered;
    }

    private static List<String> collectStatuses(List<RegistrantTrackingLog> logs) {
        Set<String> statuses = new LinkedHashSet<>();
        if (logs != null) {
            for (RegistrantTrackingLog log : logs) {
                for (String key : RegistrantTrackingCategories.orderedStatusKeys()) {
                    if (RegistrantTrackingCategories.matchesStatusFilter(log, key)) {
                        statuses.add(key);
                    }
                }
            }
        }
        List<String> ordered = new ArrayList<>();
        for (String key : RegistrantTrackingCategories.orderedStatusKeys()) {
            if (statuses.contains(key)) {
                ordered.add(key);
            }
        }
        return ordered;
    }

    private static List<RegistrantFilterOption> buildCategoryOptions(List<String> values, String selected) {
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selected)));
        for (String value : values) {
            options.add(new RegistrantFilterOption(value,
                    RegistrantTrackingCategories.categoryLabel(value), value.equals(selected)));
        }
        return options;
    }

    private static List<RegistrantFilterOption> buildStatusOptions(List<String> values, String selected) {
        List<RegistrantFilterOption> options = new ArrayList<>();
        options.add(new RegistrantFilterOption("all", "Tất cả", "all".equals(selected)));
        for (String value : values) {
            options.add(new RegistrantFilterOption(value,
                    RegistrantTrackingCategories.statusFilterLabel(value), value.equals(selected)));
        }
        return options;
    }

    private static boolean matchesSearch(RegistrantTrackingLog log, String q) {
        return containsIgnoreCase(log.getEventTitle(), q)
                || containsIgnoreCase(log.getActorRole(), q)
                || containsIgnoreCase(log.getStatusLabel(), q)
                || containsIgnoreCase(log.getRemarks(), q)
                || containsIgnoreCase(RegistrantTrackingCategories.categoryLabel(
                        RegistrantTrackingCategories.resolveCategory(log)), q);
    }

    private static boolean matchesDateRange(RegistrantTrackingLog log, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return true;
        }
        if (log.getTimestamp() == null) {
            return fromDate == null;
        }
        LocalDate eventDate = toLocalDate(log.getTimestamp());
        if (fromDate != null && eventDate.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && eventDate.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private static LocalDate toLocalDate(Date value) {
        return Instant.ofEpochMilli(value.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private static String normalizeChoice(String raw, List<String> validValues, String defaultValue) {
        if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw.trim())) {
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

    private static String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private static boolean containsIgnoreCase(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    private static boolean hasActivefilter(String q, String category, String status, String from, String to) {
        return !RegistrantListFilter.trimParam(q).isEmpty()
                || (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category))
                || (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status))
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

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback != null ? fallback.trim() : "";
    }
}
