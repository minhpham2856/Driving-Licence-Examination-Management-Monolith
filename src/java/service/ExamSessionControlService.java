package service;

import dto.ExaminerSlotDTO;
import java.util.List;
import java.sql.Date;

public interface ExamSessionControlService {

    StartResult startSession(int sessionId, int staffUserId);

    EndResult endSession(int sessionId);

    List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId);

    public static final class StartResult {
        private final boolean success;
        private final String message;
        private final String sessionName;
        private final Date examDate;
        private final int examinerCount;

        private StartResult(boolean success, String message, String sessionName, Date examDate, int examinerCount) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
            this.examDate = examDate;
            this.examinerCount = examinerCount;
        }

        public static StartResult ok(String sessionName, Date examDate, int examinerCount) {
            return new StartResult(true,
                    "Đã bắt đầu ca thi \"" + sessionName + "\". "
                            + examinerCount + " sát hạch viên có thể đăng nhập.",
                    sessionName, examDate, examinerCount);
        }

        public static StartResult fail(String message) {
            return new StartResult(false, message, null, null, 0);
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

        public Date getExamDate() {
            return examDate;
        }

        public int getExaminerCount() {
            return examinerCount;
        }
    }

    public static final class EndResult {
        private final boolean success;
        private final String message;
        private final String sessionName;
        private final Date examDate;

        private EndResult(boolean success, String message, String sessionName, Date examDate) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
            this.examDate = examDate;
        }

        public static EndResult ok(String sessionName, Date examDate) {
            return new EndResult(true,
                    "Đã kết thúc ca thi \"" + sessionName + "\". sát hạch viên không thể đăng nhập ca này nữa.",
                    sessionName, examDate);
        }

        public static EndResult fail(String message) {
            return new EndResult(false, message, null, null);
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

        public Date getExamDate() {
            return examDate;
        }
    }
}

