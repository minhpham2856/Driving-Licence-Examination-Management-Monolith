package dto.payload;

public class UpdateEnrollmentScoresCommand {

    private int candidateId;
    private Integer theoryScore;
    private String theoryResult;
    private Integer practicalScore;
    private String practicalResult;

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    public String getTheoryResult() {
        return theoryResult;
    }

    public void setTheoryResult(String theoryResult) {
        this.theoryResult = theoryResult;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    public String getPracticalResult() {
        return practicalResult;
    }

    public void setPracticalResult(String practicalResult) {
        this.practicalResult = practicalResult;
    }
}
