package Controllers.Staff.ExamStaff;

import DAO.ExaminerAssignmentDAO;
import DAO.Impl.ExaminerAssignmentDAOImpl;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExaminerAssignmentStore {

    private static final ExaminerAssignmentDAO DAO = new ExaminerAssignmentDAOImpl();

    private ExaminerAssignmentStore() {
    }

    public static boolean assign(HttpSession session, ExaminerSlot slot) {
        return DAO.assign(slot);
    }

    public static boolean remove(HttpSession session, String slotKey) {
        return DAO.remove(slotKey);
    }

    public static List<ExaminerSlot> getBySessionId(HttpSession session, int examSessionId) {
        return DAO.getBySessionId(examSessionId);
    }

    public static List<ExaminerSlot> getByExamDate(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return DAO.getByExamDate(examDate, sessionDates);
    }

    public static Set<Integer> getBusyExaminerIds(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return DAO.getBusyExaminerIds(examDate, sessionDates);
    }

    public static List<ExaminerSlot> getByExamId(HttpSession session, int examId) {
        return DAO.getByExamId(examId);
    }

    public static Set<Integer> getBusyExaminerIdsByExamId(HttpSession session, int examId) {
        return DAO.getBusyExaminerIdsByExamId(examId);
    }
}
