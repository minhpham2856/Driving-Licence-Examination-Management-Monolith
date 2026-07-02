package model.registrant;

import java.util.ArrayList;
import java.util.List;

/**
 * Tóm tắt trạng thái tài liệu đính kèm — dùng chung cho profile.jsp và track-profile.jsp.
 */
public class RegistrantDocumentSummary {

    private int requiredUploaded;
    private int requiredTotal = 4;
    private int otherCount;
    private int pendingReviewCount;
    private int approvedCount;
    private int rejectedCount;
    private int awaitingSubmitCount;
    private String overallStatusClass = "incomplete";
    private String overallStatusLabel = "Chưa tải lên";
    private String overallMessage = "Chưa có tài liệu đính kèm.";
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    public int getRequiredUploaded() {
        return requiredUploaded;
    }

    public void setRequiredUploaded(int requiredUploaded) {
        this.requiredUploaded = requiredUploaded;
    }

    public int getRequiredTotal() {
        return requiredTotal;
    }

    public void setRequiredTotal(int requiredTotal) {
        this.requiredTotal = requiredTotal;
    }

    public int getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(int otherCount) {
        this.otherCount = otherCount;
    }

    public int getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(int pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public int getAwaitingSubmitCount() {
        return awaitingSubmitCount;
    }

    public void setAwaitingSubmitCount(int awaitingSubmitCount) {
        this.awaitingSubmitCount = awaitingSubmitCount;
    }

    public String getOverallStatusClass() {
        return overallStatusClass;
    }

    public void setOverallStatusClass(String overallStatusClass) {
        this.overallStatusClass = overallStatusClass;
    }

    public String getOverallStatusLabel() {
        return overallStatusLabel;
    }

    public void setOverallStatusLabel(String overallStatusLabel) {
        this.overallStatusLabel = overallStatusLabel;
    }

    public String getOverallMessage() {
        return overallMessage;
    }

    public void setOverallMessage(String overallMessage) {
        this.overallMessage = overallMessage;
    }

    public List<ChecklistItem> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<ChecklistItem> checklistItems) {
        this.checklistItems = checklistItems;
    }

    public boolean isHasRejected() {
        return rejectedCount > 0;
    }

    public boolean isHasPendingReview() {
        return pendingReviewCount > 0;
    }

    public int getRequiredProgressPercent() {
        if (requiredTotal <= 0) {
            return 0;
        }
        return Math.min(100, (requiredUploaded * 100) / requiredTotal);
    }

    public static class ChecklistItem {
        private String label;
        private String statusClass;
        private String statusLabel;
        private boolean uploaded;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getStatusClass() {
            return statusClass;
        }

        public void setStatusClass(String statusClass) {
            this.statusClass = statusClass;
        }

        public String getStatusLabel() {
            return statusLabel;
        }

        public void setStatusLabel(String statusLabel) {
            this.statusLabel = statusLabel;
        }

        public boolean isUploaded() {
            return uploaded;
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded = uploaded;
        }
    }
}
