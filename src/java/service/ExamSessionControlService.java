package service;

import dto.examiner.ExaminerSlotDTO;
import java.util.List;

public interface ExamSessionControlService {
    class StartResult {
        private final boolean success;
        private final String message;
        private final String sessionName;
        private final int examinerCount;

        private StartResult(boolean success, String message, String sessionName, int count) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
            this.examinerCount = count;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSessionName() { return sessionName; }
        public int getExaminerCount() { return examinerCount; }

        public static StartResult ok(String name, int count) { return new StartResult(true, "BAAA,AA,A_t A?zA,EoAAA,AA,A u ca thi thAAnh cA'ng.", name, count); }
        public static StartResult fail(String msg) { return new StartResult(false, msg, null, 0); }
    }

    class EndResult {
        private final boolean success;
        private final String message;
        private final String sessionName;

        private EndResult(boolean success, String message, String sessionName) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSessionName() { return sessionName; }

        public static EndResult ok(String name) { return new EndResult(true, "KAAA,AA,At thA'A,Ac ca thi thAAnh cA'ng.", name); }
        public static EndResult fail(String msg) { return new EndResult(false, msg, null); }
    }

    StartResult startSession(int sessionId, int staffUserId);
    EndResult endSession(int sessionId);
    List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId);
    dto.exam.SessionDTO getSessionById(int id);
    List<dto.exam.SessionDTO> getAllSessions();
    List<dto.exam.SessionDTO> getActiveSessions();
}
