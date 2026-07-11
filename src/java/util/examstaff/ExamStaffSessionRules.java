package util.examstaff;

import dto.SessionDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExamStaffSessionRules {

    private ExamStaffSessionRules() {
    }

    public static List<SessionDTO> sessionsForExam(List<SessionDTO> allSessions, int examId) {
        List<SessionDTO> result = new ArrayList<>();
        if (allSessions == null || examId <= 0) {
            return result;
        }
        for (SessionDTO s : allSessions) {
            if (s != null && s.getExamId() == examId) {
                result.add(s);
            }
        }
        return result;
    }

    public static int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        if (examId > 0) {
            return examId;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            return 0;
        }
        return allSessions.get(0).getId();
    }

    public static SessionDTO findSessionById(List<SessionDTO> allSessions, int sessionId) {
        if (allSessions == null || sessionId <= 0) {
            return null;
        }
        for (SessionDTO s : allSessions) {
            if (s != null && s.getId() == sessionId) {
                return s;
            }
        }
        return null;
    }

    public static int resolveDefaultExamId(List<SessionDTO> allSessions) {
        if (allSessions == null || allSessions.isEmpty()) {
            return 0;
        }
        return allSessions.get(0).getExamId();
    }

    public static int resolveDefaultSessionId(List<SessionDTO> allSessions) {
        if (allSessions == null || allSessions.isEmpty()) {
            return 0;
        }
        return allSessions.get(0).getId();
    }

    public static List<SessionDTO> sortExamDaysForSidebar(List<SessionDTO> options) {
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        List<SessionDTO> sorted = new ArrayList<>(options);
        sorted.sort(Comparator
                .comparing(SessionDTO::getExamDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(s -> s.getLicenseCode() != null ? s.getLicenseCode() : "",
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SessionDTO::getId));
        return sorted;
    }
}
