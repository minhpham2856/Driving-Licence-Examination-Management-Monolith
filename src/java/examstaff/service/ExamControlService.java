package examstaff.service;

import examstaff.dto.ServiceResult;

/**
 * Điều khiển vòng đời kỳ thi (bắt đầu, tạm dừng, tiếp tục, kết thúc) từ phía nhân viên kỳ thi.
 */
public interface ExamControlService {

    /**
     * Bắt đầu kỳ thi và cho phép sát hạch viên đăng nhập theo phân công.
     *
     * @param examId      mã kỳ thi
     * @param staffUserId mã nhân viên thực hiện
     * @return {@link ServiceResult} kèm {@link StartExamData} khi thành công
     */
    ServiceResult<StartExamData> startExam(int examId, int staffUserId);

    /**
     * Kết thúc kỳ thi; sát hạch viên không còn đăng nhập được kỳ này.
     *
     * @param examId mã kỳ thi
     * @return {@link ServiceResult} với data = nhãn kỳ thi khi thành công
     */
    ServiceResult<String> endExam(int examId);

    /**
     * Tạm dừng kỳ thi; giữ hàng đợi gọi số, khóa đăng nhập sát hạch viên.
     *
     * @param examId mã kỳ thi
     * @return {@link ServiceResult} với data = nhãn kỳ thi khi thành công
     */
    ServiceResult<String> pauseExam(int examId);

    /**
     * Tiếp tục kỳ thi sau khi tạm dừng.
     *
     * @param examId mã kỳ thi
     * @return {@link ServiceResult} với data = nhãn kỳ thi khi thành công
     */
    ServiceResult<String> resumeExam(int examId);

    /**
     * Payload thành công khi bắt đầu kỳ thi (nhãn + số SHV).
     */
    public static final class StartExamData {
        private final String examName;
        private final int examinerCount;

        /**
         * Tạo payload bắt đầu kỳ thi.
         *
         * @param examName      nhãn kỳ thi (tên + ngày)
         * @param examinerCount số sát hạch viên được phân công
         */
        public StartExamData(String examName, int examinerCount) {
            this.examName = examName;
            this.examinerCount = examinerCount;
        }

        /**
         * Nhãn hiển thị kỳ thi đã bắt đầu.
         *
         * @return tên / nhãn kỳ thi
         */
        public String getExamName() {
            return examName;
        }

        /**
         * Số sát hạch viên được phép đăng nhập theo phân công.
         *
         * @return số SHV
         */
        public int getExaminerCount() {
            return examinerCount;
        }
    }
}
