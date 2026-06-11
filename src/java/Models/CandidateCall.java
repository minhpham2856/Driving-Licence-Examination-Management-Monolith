package Models;

import java.sql.Timestamp;

public class CandidateCall {
    private int id;
    private int examSessionId;
    private int candidateNo;
    private String calledTo;
    private int calledBy;
    private Timestamp calledAt;
    private String result; // 'Present', 'Absent', etc.

    public CandidateCall() {
    }

    public CandidateCall(int id, int examSessionId, int candidateNo, String calledTo, int calledBy, Timestamp calledAt, String result) {
        this.id = id;
        this.examSessionId = examSessionId;
        this.candidateNo = candidateNo;
        this.calledTo = calledTo;
        this.calledBy = calledBy;
        this.calledAt = calledAt;
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
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

    public Timestamp getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(Timestamp calledAt) {
        this.calledAt = calledAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
