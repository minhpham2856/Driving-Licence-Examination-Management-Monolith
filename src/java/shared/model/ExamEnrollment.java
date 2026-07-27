package shared.model;

// One candidate enrolled in one exam day (status lives on ExamEnrollmentSection).
public class ExamEnrollment {

    private int examEnrollmentId;
    private int candidateId;
    private int examId;
    private Integer examDeviceId;
    private Integer allocatedExamAreaId;
    private Candidate candidate;
    private Exam exam;
    private ExamDevice examDevice;

    public ExamEnrollment() {
    }

    public ExamEnrollment(int examEnrollmentId, int candidateId, int examId, Integer examDeviceId,
            Integer allocatedExamAreaId) {
        this.examEnrollmentId = examEnrollmentId;
        this.candidateId = candidateId;
        this.examId = examId;
        this.examDeviceId = examDeviceId;
        this.allocatedExamAreaId = allocatedExamAreaId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getSessionId() {
        return examId;
    }

    public void setSessionId(int sessionId) {
        this.examId = sessionId;
    }

    public Integer getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(Integer examDeviceId) {
        this.examDeviceId = examDeviceId;
    }

    public Integer getAllocatedExamAreaId() {
        return allocatedExamAreaId;
    }

    public void setAllocatedExamAreaId(Integer allocatedExamAreaId) {
        this.allocatedExamAreaId = allocatedExamAreaId;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public ExamDevice getExamDevice() {
        return examDevice;
    }

    public void setExamDevice(ExamDevice examDevice) {
        this.examDevice = examDevice;
    }
}
