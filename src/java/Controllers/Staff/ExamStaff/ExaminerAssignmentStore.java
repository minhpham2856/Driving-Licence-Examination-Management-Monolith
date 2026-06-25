package Controllers.Staff.ExamStaff;

import DAOs.ExaminerAssignmentDAO;
import DAOs.Impl.ExaminerAssignmentDAOImpl;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

 // Static facade over {@link ExaminerAssignmentDAO} for the staff allocation UI.
public final class ExaminerAssignmentStore {

    // Singleton DAO instance shared by all static method calls
    private static final ExaminerAssignmentDAO DAO = new ExaminerAssignmentDAOImpl();

    // Private constructor prevents instantiation — all methods are static
    private ExaminerAssignmentStore() {
    }

         // Assigns an examiner to the given slot.
    public static boolean assign(HttpSession session, ExaminerSlot slot) {
        return DAO.assign(slot);
    }

         // Removes an examiner assignment by slot key.
    public static boolean remove(HttpSession session, String slotKey) {
        return DAO.remove(slotKey);
    }

         // Returns all assignment slots for a given session.
    public static List<ExaminerSlot> getBySessionId(HttpSession session, int examSessionId) {
        return DAO.getBySessionId(examSessionId);
    }

         // Returns assignment slots for sessions occurring on a given date.
    public static List<ExaminerSlot> getByExamDate(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return DAO.getByExamDate(examDate, sessionDates);
    }

         // Returns the set of examiner user IDs who are already assigned on the given date.
    public static Set<Integer> getBusyExaminerIds(HttpSession session, Date examDate,
            Map<Integer, Date> sessionDates) {
        return DAO.getBusyExaminerIds(examDate, sessionDates);
    }
}
