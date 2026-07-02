package controller.staff.exam;

import dao.ExaminerAssignmentDAO;
import dao.impl.ExaminerAssignmentDAOImpl;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExaminerAssignmentStore {

    private static final ExaminerAssignmentDAO dao = new ExaminerAssignmentDAOImpl();

    private ExaminerAssignmentStore() {
    }

    public static boolean assign(HttpSession session, ExaminerSlot slot) {
        return dao.assign(slot);
    }

    public static boolean remove(HttpSession session, String slotKey) {
        return dao.remove(slotKey);
    }

    public static List<ExaminerSlot> getBySessionId(HttpSession session, int examSessionId) {
        return dao.getBySessionId(examSessionId);
    }

    public static List<ExaminerSlot> getByExamDate(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return dao.getByExamDate(examDate, sessionDates);
    }

    public static Set<Integer> getBusyExaminerIds(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return dao.getBusyExaminerIds(examDate, sessionDates);
    }
}
