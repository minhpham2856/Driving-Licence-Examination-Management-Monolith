package examstaff.service;

public interface ExamControlService {

    StartResult startExam(int examId, int staffUserId);

    EndResult endExam(int examId);

    PauseResult pauseExam(int examId);

    ResumeResult resumeExam(int examId);

    public static final class StartResult {
        private final boolean success;
        private final String message;
        private final String examName;
        private final int examinerCount;

        private StartResult(boolean success, String message, String examName, int examinerCount) {
            this.success = success;
            this.message = message;
            this.examName = examName;
            this.examinerCount = examinerCount;
        }

        public static StartResult ok(String examName, int examinerCount) {
            return new StartResult(true,
                    "Đã bắt đầu kỳ thi \"" + examName + "\". "
                            + examinerCount + " sát hạch viên có thể đăng nhập.",
                    examName, examinerCount);
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

        public String getExamName() {
            return examName;
        }

        public int getExaminerCount() {
            return examinerCount;
        }
    }

    public static final class EndResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private EndResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        public static EndResult ok(String examName) {
            return new EndResult(true,
                    "Đã kết thúc kỳ thi \"" + examName + "\". Sát hạch viên không thể đăng nhập kỳ thi này nữa.",
                    examName);
        }

        public static EndResult fail(String message) {
            return new EndResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getExamName() {
            return examName;
        }
    }

    public static final class PauseResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private PauseResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        public static PauseResult ok(String examName) {
            return new PauseResult(true,
                    "Đã tạm dừng kỳ thi \"" + examName
                            + "\". Hàng đợi gọi số được giữ nguyên; sát hạch viên không thể đăng nhập khi đang tạm dừng.",
                    examName);
        }

        public static PauseResult fail(String message) {
            return new PauseResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getExamName() {
            return examName;
        }
    }

    public static final class ResumeResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private ResumeResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        public static ResumeResult ok(String examName) {
            return new ResumeResult(true,
                    "Đã tiếp tục kỳ thi \"" + examName + "\". Sát hạch viên có thể đăng nhập lại.",
                    examName);
        }

        public static ResumeResult fail(String message) {
            return new ResumeResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getExamName() {
            return examName;
        }
    }
}
