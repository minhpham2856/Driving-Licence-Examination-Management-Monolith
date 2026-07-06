package dto.payload;

import dto.ExaminerCandidateRowDTO;
import java.util.ArrayList;
import java.util.List;

public class CandidateCallDataDTO {

    private List<ExaminerCandidateRowDTO> candidates = new ArrayList<>();
    private List<ExaminerCandidateRowDTO> candidateQueue = new ArrayList<>();
    private ExaminerCandidateRowDTO candidate;
    private boolean searchActive;
    private String searchQuery;

    public List<ExaminerCandidateRowDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<ExaminerCandidateRowDTO> candidates) {
        this.candidates = candidates != null ? candidates : new ArrayList<>();
    }

    public List<ExaminerCandidateRowDTO> getCandidateQueue() {
        return candidateQueue;
    }

    public void setCandidateQueue(List<ExaminerCandidateRowDTO> candidateQueue) {
        this.candidateQueue = candidateQueue != null ? candidateQueue : new ArrayList<>();
    }

    public ExaminerCandidateRowDTO getCandidate() {
        return candidate;
    }

    public void setCandidate(ExaminerCandidateRowDTO candidate) {
        this.candidate = candidate;
    }

    public boolean isSearchActive() {
        return searchActive;
    }

    public void setSearchActive(boolean searchActive) {
        this.searchActive = searchActive;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void applyTo(jakarta.servlet.http.HttpServletRequest request) {
        request.setAttribute("candidates", candidates);
        request.setAttribute("candidateQueue", candidateQueue);
        if (candidate != null) {
            request.setAttribute("candidate", candidate);
        }
        if (searchActive) {
            request.setAttribute("searchActive", true);
            request.setAttribute("searchQuery", searchQuery);
        }
    }
}
