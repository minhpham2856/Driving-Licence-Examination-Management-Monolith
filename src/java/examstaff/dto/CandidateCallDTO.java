package examstaff.dto;

/**
 * DTO bản ghi gọi thí sinh (liên quan CandidateCall DAO).
 * Mang kỳ thi, số báo danh, nơi gọi, người gọi và kết quả có mặt/vắng.
 */
public class CandidateCallDTO {
    private int examId;
    private int candidateNo;
    private String calledTo;
    private int calledBy;
    /** Kết quả gọi: Present, Absent, ... */
    private String result;

    public CandidateCallDTO() {
    }

    public CandidateCallDTO(int examId, int candidateNo, String calledTo, int calledBy, String result) {
        this.examId = examId;
        this.candidateNo = candidateNo;
        this.calledTo = calledTo;
        this.calledBy = calledBy;
        this.result = result;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getCandidateNo() {
        return candidateNo;
    }

    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    public String getCalledTo() {
        return calledTo;
    }

    public void setCalledTo(String calledTo) {
        this.calledTo = calledTo;
    }

    public int getCalledBy() {
        return calledBy;
    }

    public void setCalledBy(int calledBy) {
        this.calledBy = calledBy;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
