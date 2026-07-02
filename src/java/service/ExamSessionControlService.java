package service;

import dto.ExaminerSlotDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface ExamSessionControlService {

    StartResult startSession(int sessionId, int staffUserId);

    EndResult endSession(int sessionId);

    void applyRuntimeStart(ServletContext ctx, HttpSession httpSession, int sessionId);

    void applyRuntimeEnd(ServletContext ctx, HttpSession httpSession, int sessionId);

    List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId);

    public static final class StartResult {
        private final boolean success;
        private final String message;
        private final String sessionName;
        private final int examinerCount;

        private StartResult(boolean success, String message, String sessionName, int examinerCount) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
            this.examinerCount = examinerCount;
        }

        public static StartResult ok(String sessionName, int examinerCount) {
            return new StartResult(true,
                    "Đã bắt đầu ca thi \"" + sessionName + "\". "
                            + examinerCount + " sát hạch viên có thể đăng nhập.",
                    sessionName, examinerCount);
        }

        public static StartResult fail(String message) {
            return new StartResult(false, message, null, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSessionName() {
            return sessionName;
        }

        public int getExaminerCount() {
            return examinerCount;
        }
    }

    public static final class EndResult {
        private final boolean success;
        private final String message;

        private EndResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static EndResult ok(String sessionName) {
            return new EndResult(true,
                    "Đã kết thúc ca thi \"" + sessionName + "\". sát hạch viên không thể đăng nhập ca này nữa.");
        }

        public static EndResult fail(String message) {
            return new EndResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

