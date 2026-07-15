package examstaff.service;

public interface ExamControlService {

    StartResult startExam(int examId, int staffUserId);

    EndResult endExam(int examId);

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
}
