package examstaff.service;

/**
 * Điều khiển vòng đời kỳ thi (bắt đầu, tạm dừng, tiếp tục, kết thúc) từ phía nhân viên kỳ thi.
 */
public interface ExamControlService {

    /**
     * Bắt đầu kỳ thi và cho phép sát hạch viên đăng nhập theo phân công.
     *
     * @param examId      mã kỳ thi
     * @param staffUserId mã nhân viên thực hiện
     * @return kết quả thành công/thất bại kèm thông báo
     */
    StartResult startExam(int examId, int staffUserId);

    /**
     * Kết thúc kỳ thi; sát hạch viên không còn đăng nhập được kỳ này.
     *
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    EndResult endExam(int examId);

    /**
     * Tạm dừng kỳ thi; giữ hàng đợi gọi số, khóa đăng nhập sát hạch viên.
     *
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    PauseResult pauseExam(int examId);

    /**
     * Tiếp tục kỳ thi sau khi tạm dừng.
     *
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    ResumeResult resumeExam(int examId);

    /**
     * Kết quả nghiệp vụ khi bắt đầu kỳ thi.
     */
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

        /**
         * Tạo kết quả thành công khi bắt đầu kỳ thi.
         *
         * @param examName      tên kỳ thi
         * @param examinerCount số sát hạch viên được phép đăng nhập
         * @return kết quả thành công
         */
        public static StartResult ok(String examName, int examinerCount) {
            return new StartResult(true,
                    "Đã bắt đầu kỳ thi \"" + examName + "\". "
                            + examinerCount + " sát hạch viên có thể đăng nhập.",
                    examName, examinerCount);
        }

        /**
         * Tạo kết quả thất bại khi bắt đầu kỳ thi.
         *
         * @param message thông báo lỗi
         * @return kết quả thất bại
         */
        public static StartResult fail(String message) {
            return new StartResult(false, message, null, 0);
        }

        /** @return true nếu thao tác thành công */
        public boolean isSuccess() {
            return success;
        }

        /** @return thông báo nghiệp vụ */
        public String getMessage() {
            return message;
        }

        /** @return tên kỳ thi (null nếu thất bại) */
        public String getExamName() {
            return examName;
        }

        /** @return số sát hạch viên được mở đăng nhập */
        public int getExaminerCount() {
            return examinerCount;
        }
    }

    /**
     * Kết quả nghiệp vụ khi kết thúc kỳ thi.
     */
    public static final class EndResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private EndResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        /**
         * Tạo kết quả thành công khi kết thúc kỳ thi.
         *
         * @param examName tên kỳ thi
         * @return kết quả thành công
         */
        public static EndResult ok(String examName) {
            return new EndResult(true,
                    "Đã kết thúc kỳ thi \"" + examName + "\". Sát hạch viên không thể đăng nhập kỳ thi này nữa.",
                    examName);
        }

        /**
         * Tạo kết quả thất bại khi kết thúc kỳ thi.
         *
         * @param message thông báo lỗi
         * @return kết quả thất bại
         */
        public static EndResult fail(String message) {
            return new EndResult(false, message, null);
        }

        /** @return true nếu thao tác thành công */
        public boolean isSuccess() {
            return success;
        }

        /** @return thông báo nghiệp vụ */
        public String getMessage() {
            return message;
        }

        /** @return tên kỳ thi (null nếu thất bại) */
        public String getExamName() {
            return examName;
        }
    }

    /**
     * Kết quả nghiệp vụ khi tạm dừng kỳ thi.
     */
    public static final class PauseResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private PauseResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        /**
         * Tạo kết quả thành công khi tạm dừng kỳ thi.
         *
         * @param examName tên kỳ thi
         * @return kết quả thành công
         */
        public static PauseResult ok(String examName) {
            return new PauseResult(true,
                    "Đã tạm dừng kỳ thi \"" + examName
                            + "\". Hàng đợi gọi số được giữ nguyên; sát hạch viên không thể đăng nhập khi đang tạm dừng.",
                    examName);
        }

        /**
         * Tạo kết quả thất bại khi tạm dừng kỳ thi.
         *
         * @param message thông báo lỗi
         * @return kết quả thất bại
         */
        public static PauseResult fail(String message) {
            return new PauseResult(false, message, null);
        }

        /** @return true nếu thao tác thành công */
        public boolean isSuccess() {
            return success;
        }

        /** @return thông báo nghiệp vụ */
        public String getMessage() {
            return message;
        }

        /** @return tên kỳ thi (null nếu thất bại) */
        public String getExamName() {
            return examName;
        }
    }

    /**
     * Kết quả nghiệp vụ khi tiếp tục kỳ thi.
     */
    public static final class ResumeResult {
        private final boolean success;
        private final String message;
        private final String examName;

        private ResumeResult(boolean success, String message, String examName) {
            this.success = success;
            this.message = message;
            this.examName = examName;
        }

        /**
         * Tạo kết quả thành công khi tiếp tục kỳ thi.
         *
         * @param examName tên kỳ thi
         * @return kết quả thành công
         */
        public static ResumeResult ok(String examName) {
            return new ResumeResult(true,
                    "Đã tiếp tục kỳ thi \"" + examName + "\". Sát hạch viên có thể đăng nhập lại.",
                    examName);
        }

        /**
         * Tạo kết quả thất bại khi tiếp tục kỳ thi.
         *
         * @param message thông báo lỗi
         * @return kết quả thất bại
         */
        public static ResumeResult fail(String message) {
            return new ResumeResult(false, message, null);
        }

        /** @return true nếu thao tác thành công */
        public boolean isSuccess() {
            return success;
        }

        /** @return thông báo nghiệp vụ */
        public String getMessage() {
            return message;
        }

        /** @return tên kỳ thi (null nếu thất bại) */
        public String getExamName() {
            return examName;
        }
    }
}
