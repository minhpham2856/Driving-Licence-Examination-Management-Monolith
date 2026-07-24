package policestaff.dto;

/** Kết quả chốt danh sách và gửi thông báo. */
public class OfficialRosterPublishResult {
    private final int totalCandidates;
    private final int centreEmailsSent;
    private final int candidateEmailsSent;
    private final boolean emailConfigured;

    public OfficialRosterPublishResult(int totalCandidates, int centreEmailsSent,
            int candidateEmailsSent, boolean emailConfigured) {
        this.totalCandidates = totalCandidates;
        this.centreEmailsSent = centreEmailsSent;
        this.candidateEmailsSent = candidateEmailsSent;
        this.emailConfigured = emailConfigured;
    }

    public int getTotalCandidates() { return totalCandidates; }
    public int getCentreEmailsSent() { return centreEmailsSent; }
    public int getCandidateEmailsSent() { return candidateEmailsSent; }
    public boolean isEmailConfigured() { return emailConfigured; }
}
