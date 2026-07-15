package examstaff.dto;

/**
 * DTO bản ghi gọi thí sinh dùng trong workflow/DAO CandidateCall.
 *
 * <h2>Vai trò</h2>
 * Mang kỳ thi, số báo danh, nơi gọi, người gọi và kết quả (Present / Absent / …)
 * khi persist hoặc xử lý trong {@code CandidateCallWorkflowServiceImpl}.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * Tạo/tiêu thụ nội bộ BLL call workflow và DAO liên quan; không bind trực tiếp JSP.
 */
public class CandidateCallDTO {
    private int examId;
    private int candidateNo;
    private String calledTo;
    private int calledBy;
    /** Kết quả gọi: Present, Absent, ... */
    private String result;

    /** Bản ghi rỗng — set từng field trước khi persist. */
    public CandidateCallDTO() {
    }

    /**
     * Khởi tạo đủ field lõi của một lần gọi.
     *
     * @param examId      kỳ thi
     * @param candidateNo số báo danh số
     * @param calledTo    nơi / kênh gọi (desk code…)
     * @param calledBy    userId staff gọi
     * @param result      kết quả Present/Absent/…
     */
    public CandidateCallDTO(int examId, int candidateNo, String calledTo, int calledBy, String result) {
        this.examId = examId;
        this.candidateNo = candidateNo;
        this.calledTo = calledTo;
        this.calledBy = calledBy;
        this.result = result;
    }

    /** Mã kỳ thi của lần gọi. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Số báo danh dạng số của thí sinh được gọi. */
    public int getCandidateNo() {
        return candidateNo;
    }

    /** Gán số báo danh. */
    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    /** Đích gọi (khu vực / bàn / mã nơi gọi). */
    public String getCalledTo() {
        return calledTo;
    }

    /** Gán nơi gọi. */
    public void setCalledTo(String calledTo) {
        this.calledTo = calledTo;
    }

    /** UserId staff thực hiện gọi. */
    public int getCalledBy() {
        return calledBy;
    }

    /** Gán id staff gọi. */
    public void setCalledBy(int calledBy) {
        this.calledBy = calledBy;
    }

    /** Kết quả lần gọi (Present, Absent, … theo quy ước DB). */
    public String getResult() {
        return result;
    }

    /** Gán kết quả gọi. */
    public void setResult(String result) {
        this.result = result;
    }
}
