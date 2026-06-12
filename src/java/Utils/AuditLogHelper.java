package Utils;



import DAO.AuditLogDAO;

import DAO.Impl.AuditLogDAOImpl;

import Models.AuditLog;

import Models.User;

import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;



public final class AuditLogHelper {



    private static final AuditLogDAO DAO = new AuditLogDAOImpl();



    private AuditLogHelper() {

    }



    public static void persist(HttpSession session, String action, String details) {

        persist(session, action, details, 0);

    }



    public static void persist(HttpSession session, String action, String details, int recordId) {

        try {

            User user = (User) session.getAttribute("user");

            int userId = (user != null && user.getId() > 0) ? user.getId() : 3;



            AuditLog log = new AuditLog();

            log.setTableName(resolveEntityName(action, details));

            log.setRecordId(recordId > 0 ? recordId : 0);

            log.setAction(normalizeAction(action));

            log.setNewValue(details);

            log.setChangedBy(userId);

            log.setChangedAt(new Timestamp(System.currentTimeMillis()));

            DAO.insert(log);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void persistWarning(HttpSession session, String details, String reason, int recordId) {

        try {

            User user = (User) session.getAttribute("user");

            int userId = (user != null && user.getId() > 0) ? user.getId() : 3;

            AuditLog log = new AuditLog();

            log.setTableName("Candidate");

            log.setRecordId(recordId > 0 ? recordId : 0);

            log.setAction("WARNING");

            log.setNewValue(details);

            log.setReason(reason);

            log.setChangedBy(userId);

            log.setChangedAt(new Timestamp(System.currentTimeMillis()));

            DAO.insert(log);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }



    static String resolveEntityName(String action, String details) {

        String upper = action != null ? action.toUpperCase() : "";

        String detailUpper = details != null ? details.toUpperCase() : "";



        if (upper.contains("IMPORT")) {

            return "ExamRegistration";

        }

        if (upper.contains("PAYMENT")) {

            return "Payment";

        }

        if (upper.contains("PERSON") || upper.contains("PROFILE")) {

            return "Profile";

        }

        if (upper.contains("EXAMINER") || upper.contains("ASSIGN") || upper.contains("REMOVE")) {

            return "Session_Examiner";

        }

        if (detailUpper.contains("ĐIỂM") || detailUpper.contains("DIEM")

                || upper.contains("EXAMSCORE") || detailUpper.contains("LÝ THUYẾT")

                || detailUpper.contains("THỰC HÀNH") || detailUpper.contains("ĐƯỜNG TRƯỜNG")) {

            return "ExamScore";

        }

        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {

            return "ExamRegistration";

        }

        if (upper.contains("SESSION")) {

            return "Session";

        }

        return "Candidate";

    }



    static String normalizeAction(String rawAct) {

        if (rawAct == null) {

            return "UPDATE";

        }

        String upper = rawAct.toUpperCase();

        if (upper.contains("IMPORT")) {

            return "IMPORT";

        }

        if (upper.contains("INSERT")) {

            return "INSERT";

        }

        if (upper.contains("DELETE") || upper.contains("REMOVE")) {

            return "DELETE";

        }

        if (upper.contains("EXPORT")) {

            return "EXPORT";

        }

        if (upper.contains("ASSIGN")) {

            return "ASSIGN";

        }

        return "UPDATE";

    }

}


