package registrant.util;

import registrant.dto.RegistrantDashboardActivity;
import registrant.dto.RegistrantExamSessionOption;
import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Bộ lọc danh sách dùng chung cho dashboard và my-exams — tầng util, gọi {@link RegistrantFilterSupport}.
 * <p>
 * Lọc {@link registrant.dto.RegistrantMyExamRow} và {@link registrant.dto.RegistrantRegisteredExamRow}
 * theo từ khóa, trạng thái, hạng GPLX và khoảng ngày; sinh hoạt động gần đây trên dashboard.
 */
public final class RegistrantListFilter {

    private RegistrantListFilter() {
    }

    /** Lọc my-exams theo từ khóa, trạng thái và hạng. */
    public static List<RegistrantMyExamRow> filterMyExams(List<RegistrantMyExamRow> exams,
            String searchQuery, String statusFilter, String licenceFilter) {
        if (exams == null || exams.isEmpty()) {
            return exams != null ? exams : List.of();
        }
        List<RegistrantMyExamRow> filtered = new ArrayList<>();
        for (RegistrantMyExamRow exam : exams) {
            if (!RegistrantFilterSupport.matchesMyExamStatus(exam, statusFilter)) {
                continue;
            }
            if (!matchesLicence(exam.getLicenceClass(), licenceFilter)) {
                continue;
            }
            if (!matchesExamSearch(exam.getExamTitle(), null, exam.getSbdDisplay(), exam.getRoomName(),
                    exam.getLicenceClass(), exam.getStatusLabel(), searchQuery)) {
                continue;
            }
            filtered.add(exam);
        }
        return filtered;
    }

    /** Lọc danh sách đăng ký dashboard theo query/status/hạng. */
    public static List<RegistrantRegisteredExamRow> filterRegisteredExams(
            List<RegistrantRegisteredExamRow> exams, String searchQuery,
            String statusFilter, String licenceFilter) {
        if (exams == null || exams.isEmpty()) {
            return exams != null ? exams : List.of();
        }
        List<RegistrantRegisteredExamRow> filtered = new ArrayList<>();
        for (RegistrantRegisteredExamRow exam : exams) {
            if (!RegistrantFilterSupport.matchesRegisteredExamStatus(exam, statusFilter)) {
                continue;
            }
            if (!matchesLicence(exam.getLicenceClass(), licenceFilter)) {
                continue;
            }
            if (!matchesExamSearch(exam.getExamName(), exam.getExamCode(), null, exam.getLocation(),
                    exam.getLicenceClass(), exam.getStatusLabel(), searchQuery)) {
                continue;
            }
            filtered.add(exam);
        }
        return filtered;
    }

    /** Lọc hoạt động dashboard theo từ khóa tìm kiếm. */
    public static List<RegistrantDashboardActivity> filterActivities(
            List<RegistrantDashboardActivity> activities, String searchQuery) {
        if (activities == null || activities.isEmpty()) {
            return activities != null ? activities : List.of();
        }
        String q = normalizeQuery(searchQuery);
        if (q == null) {
            return activities;
        }
        List<RegistrantDashboardActivity> filtered = new ArrayList<>();
        for (RegistrantDashboardActivity act : activities) {
            if (containsIgnoreCase(act.getTitle(), q)
                    || containsIgnoreCase(act.getDesc(), q)
                    || containsIgnoreCase(act.getTime(), q)) {
                filtered.add(act);
            }
        }
        return filtered;
    }

    /** True nếu đang có bất kỳ bộ lọc danh sách nào active. */
    public static boolean hasActivefilter(String q, String status, String licence) {
        return normalizeQuery(q) != null
                || (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status.trim()))
                || (licence != null && !licence.isBlank() && !"all".equalsIgnoreCase(licence.trim()));
    }

    /** Lọc ExamDates theo search/địa điểm/khoảng ngày. */
    public static List<RegistrantExamSessionOption> filterExamSessions(
            List<RegistrantExamSessionOption> sessions,
            String searchQuery, String locationFilter, LocalDate fromDate, LocalDate toDate) {
        if (sessions == null || sessions.isEmpty()) {
            return sessions != null ? sessions : List.of();
        }
        String q = normalizeQuery(searchQuery);
        String location = trimParam(locationFilter);
        if (q == null && (location.isEmpty() || "all".equalsIgnoreCase(location))
                && fromDate == null && toDate == null) {
            return sessions;
        }
        List<RegistrantExamSessionOption> filtered = new ArrayList<>();
        for (RegistrantExamSessionOption session : sessions) {
            if (!matchesSessionSearch(session, q)) {
                continue;
            }
            if (!matchesSessionLocation(session.getLocation(), location)) {
                continue;
            }
            if (!matchesSessionDateRange(session.getExamDate(), fromDate, toDate)) {
                continue;
            }
            filtered.add(session);
        }
        return filtered;
    }

    /** Danh sách địa điểm distinct từ các đợt thi. */
    public static List<String> collectSessionLocations(List<RegistrantExamSessionOption> sessions) {
        TreeSet<String> locations = new TreeSet<>();
        if (sessions == null) {
            return List.of();
        }
        for (RegistrantExamSessionOption session : sessions) {
            if (session.getLocation() != null && !session.getLocation().isBlank()) {
                locations.add(session.getLocation().trim());
            }
        }
        return new ArrayList<>(locations);
    }

    /** True nếu bộ lọc ca thi đang có tham số khác mặc định. */
    public static boolean hasSessionActivefilter(
            String searchQuery, String locationFilter, String fromDate, String toDate) {
        String location = trimParam(locationFilter);
        return normalizeQuery(searchQuery) != null
                || (!location.isEmpty() && !"all".equalsIgnoreCase(location))
                || !trimParam(fromDate).isEmpty()
                || !trimParam(toDate).isEmpty();
    }

    /** Trim tham số request; rỗng → chuỗi rỗng. */
    public static String trimParam(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private static boolean matchesExamSearch(String title, String examCode, String sbd, String room,
            String licence, String statusLabel, String query) {
        String q = normalizeQuery(query);
        if (q == null) {
            return true;
        }
        return containsIgnoreCase(title, q)
                || containsIgnoreCase(examCode, q)
                || containsIgnoreCase(sbd, q)
                || containsIgnoreCase(room, q)
                || containsIgnoreCase(licence, q)
                || containsIgnoreCase(statusLabel, q);
    }

    private static boolean matchesLicence(String licenceClass, String licenceFilter) {
        if (licenceFilter == null || licenceFilter.isBlank() || "all".equalsIgnoreCase(licenceFilter)) {
            return true;
        }
        return licenceClass != null && licenceClass.equalsIgnoreCase(licenceFilter.trim());
    }

    private static String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private static boolean containsIgnoreCase(String value, String q) {
        if (q == null) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    private static boolean matchesSessionSearch(RegistrantExamSessionOption session, String q) {
        if (q == null) {
            return true;
        }
        return containsIgnoreCase(session.getExamCode(), q)
                || containsIgnoreCase(session.getExamName(), q)
                || containsIgnoreCase(session.getLocation(), q)
                || containsIgnoreCase(session.getLicenceClass(), q);
    }

    private static boolean matchesSessionLocation(String location, String locationFilter) {
        if (locationFilter == null || locationFilter.isBlank() || "all".equalsIgnoreCase(locationFilter)) {
            return true;
        }
        return location != null && location.equalsIgnoreCase(locationFilter.trim());
    }

    private static boolean matchesSessionDateRange(Date examDate, LocalDate fromDate, LocalDate toDate) {
        if (examDate == null) {
            return fromDate == null;
        }
        LocalDate exam = toLocalDate(examDate);
        if (fromDate != null && exam.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && exam.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private static LocalDate toLocalDate(Date examDate) {
        return Instant.ofEpochMilli(examDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
