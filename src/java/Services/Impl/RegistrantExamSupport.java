package Services.Impl;

import Models.ExamListPage;
import Models.MyExamRowView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Logic dùng chung cho dashboard và lịch thi thí sinh. */
public final class RegistrantExamSupport {

    public static final String FILTER_ALL = "all";
    public static final String FILTER_UPCOMING = "upcoming";
    public static final String FILTER_PASSED = "passed";
    public static final String FILTER_CANCELLED = "cancelled";
    public static final String FILTER_PENDING_PAYMENT = "pending_payment";

    public static final int DEFAULT_PAGE_SIZE = 5;

    private RegistrantExamSupport() {
    }

    public static boolean isActive(MyExamRowView row) {
        return row != null && !row.isCancelled();
    }

    public static boolean isUpcoming(MyExamRowView row) {
        if (!isActive(row)) {
            return false;
        }
        String status = row.getStatusLabel();
        return "Chờ thi".equals(status) || "Chờ thanh toán tại quầy".equals(status);
    }

    public static boolean isPassed(MyExamRowView row) {
        return row != null && "Đạt".equals(row.getStatusLabel());
    }

    public static boolean isPendingPayment(MyExamRowView row) {
        return row != null && !row.isCancelled() && "Chờ thanh toán tại quầy".equals(row.getStatusLabel());
    }

    public static int countActive(List<MyExamRowView> rows) {
        return (int) rows.stream().filter(RegistrantExamSupport::isActive).count();
    }

    public static int countUpcoming(List<MyExamRowView> rows) {
        return (int) rows.stream().filter(RegistrantExamSupport::isUpcoming).count();
    }

    public static Optional<MyExamRowView> findNextUpcoming(List<MyExamRowView> rows) {
        return rows.stream()
                .filter(RegistrantExamSupport::isUpcoming)
                .filter(row -> row.getExamDate() != null
                        && !row.getExamDate().toLocalDate().isBefore(LocalDate.now()))
                .min(Comparator
                        .comparing(MyExamRowView::getExamDate)
                        .thenComparing(MyExamRowView::getShiftStartTime,
                                Comparator.nullsLast(Comparator.naturalOrder())));
    }

    public static List<MyExamRowView> filterRows(List<MyExamRowView> rows, String statusFilter, String query) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        String normalizedFilter = normalizeFilter(statusFilter);
        String normalizedQuery = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";

        List<MyExamRowView> filtered = new ArrayList<>();
        for (MyExamRowView row : rows) {
            if (!matchesStatus(row, normalizedFilter)) {
                continue;
            }
            if (!normalizedQuery.isEmpty() && !matchesQuery(row, normalizedQuery)) {
                continue;
            }
            filtered.add(row);
        }
        return filtered;
    }

    public static ExamListPage paginate(List<MyExamRowView> rows, int page, int pageSize) {
        ExamListPage result = new ExamListPage();
        int safePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int totalItems = rows != null ? rows.size() : 0;
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safePageSize);
        int safePage = Math.max(1, Math.min(page, totalPages));

        int from = (safePage - 1) * safePageSize;
        int to = Math.min(from + safePageSize, totalItems);
        List<MyExamRowView> pageItems = totalItems == 0 || rows == null
                ? List.of()
                : rows.subList(from, to);

        result.setItems(pageItems);
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotalItems(totalItems);
        result.setTotalPages(totalPages);
        return result;
    }

    public static int parsePage(String raw) {
        if (raw == null || raw.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    public static String normalizeFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return FILTER_ALL;
        }
        return switch (statusFilter.trim().toLowerCase(Locale.ROOT)) {
            case FILTER_UPCOMING, FILTER_PASSED, FILTER_CANCELLED, FILTER_PENDING_PAYMENT -> statusFilter.trim().toLowerCase(Locale.ROOT);
            default -> FILTER_ALL;
        };
    }

    private static boolean matchesStatus(MyExamRowView row, String filter) {
        return switch (filter) {
            case FILTER_UPCOMING -> isUpcoming(row);
            case FILTER_PASSED -> isPassed(row);
            case FILTER_CANCELLED -> row.isCancelled();
            case FILTER_PENDING_PAYMENT -> isPendingPayment(row);
            default -> true;
        };
    }

    private static boolean matchesQuery(MyExamRowView row, String query) {
        return contains(row.getTitle(), query)
                || contains(row.getLicenceCode(), query)
                || contains(row.getSbd(), query)
                || contains(row.getRoomLabel(), query)
                || contains(row.getStatusLabel(), query);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    public static String translateExamType(String examTypeName) {
        if (examTypeName == null) {
            return "";
        }
        return switch (examTypeName) {
            case "Theory" -> "Lý thuyết";
            case "Practical" -> "Thực hành";
            case "RoadLayout" -> "Sa hình";
            case "OnRoad" -> "Đường trường";
            default -> examTypeName;
        };
    }

    public static String formatCount(int value) {
        return String.format("%02d", value);
    }
}
