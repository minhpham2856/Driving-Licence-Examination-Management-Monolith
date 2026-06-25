package Utils;

import Models.RegistrantDashboardActivity;
import Models.RegistrantExamSessionOption;
import Models.RegistrantMyExamRow;
import Models.RegistrantRegisteredExamRow;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Bộ lọc danh sách dùng chung cho dashboard và my-exams.
 */
public final class RegistrantListFilter {

    private RegistrantListFilter() {
    }

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

    public static List<String> collectLicenceClasses(List<RegistrantMyExamRow> exams) {
        return RegistrantFilterSupport.collectLicenceValuesFromMyExams(exams);
    }

    public static List<String> collectLicenceClassesFromRegistered(List<RegistrantRegisteredExamRow> exams) {
        return RegistrantFilterSupport.collectLicenceValuesFromRegistered(exams);
    }

    public static boolean hasActiveFilters(String q, String status, String licence) {
        return normalizeQuery(q) != null
                || (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status.trim()))
                || (licence != null && !licence.isBlank() && !"all".equalsIgnoreCase(licence.trim()));
    }

    public static List<RegistrantExamSessionOption> filterExamSessions(
            List<RegistrantExamSessionOption> sessions,
            String searchQuery, String locationFilter, String fromDate, String toDate) {
        if (sessions == null || sessions.isEmpty()) {
            return sessions != null ? sessions : List.of();
        }
        String q = normalizeQuery(searchQuery);
        String location = trimParam(locationFilter);
        String from = trimParam(fromDate);
        String to = trimParam(toDate);
        if (q == null && (location.isEmpty() || "all".equalsIgnoreCase(location))
                && from.isEmpty() && to.isEmpty()) {
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
            if (!matchesSessionDateRange(session.getExamDate(), from, to)) {
                continue;
            }
            filtered.add(session);
        }
        return filtered;
    }

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

    public static boolean hasSessionActiveFilters(
            String searchQuery, String locationFilter, String fromDate, String toDate) {
        String location = trimParam(locationFilter);
        return normalizeQuery(searchQuery) != null
                || (!location.isEmpty() && !"all".equalsIgnoreCase(location))
                || !trimParam(fromDate).isEmpty()
                || !trimParam(toDate).isEmpty();
    }

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

    private static boolean matchesSessionDateRange(Date examDate, String fromDate, String toDate) {
        if (examDate == null) {
            return fromDate == null || fromDate.isBlank();
        }
        LocalDate exam = examDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (fromDate != null && !fromDate.isBlank()) {
            try {
                if (exam.isBefore(LocalDate.parse(fromDate.trim()))) {
                    return false;
                }
            } catch (DateTimeParseException ignored) {
                return true;
            }
        }
        if (toDate != null && !toDate.isBlank()) {
            try {
                if (exam.isAfter(LocalDate.parse(toDate.trim()))) {
                    return false;
                }
            } catch (DateTimeParseException ignored) {
                return true;
            }
        }
        return true;
    }
}
