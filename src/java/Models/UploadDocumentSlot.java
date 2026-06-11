package Models;

public class UploadDocumentSlot {

    private String slotKey;
    private String title;
    private String fileInputName;
    private String documentUrl;
    private String statusLabel;
    private String statusClass;
    private String cardClass;
    private boolean showUpload;
    private boolean showPreview;
    private boolean imagePreview;
    private boolean showFeedback;
    private String feedbackMessage;
    private Integer documentId;
    private boolean canDelete;

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileInputName() {
        return fileInputName;
    }

    public void setFileInputName(String fileInputName) {
        this.fileInputName = fileInputName;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatusClass() {
        return statusClass;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public String getCardClass() {
        return cardClass;
    }

    public void setCardClass(String cardClass) {
        this.cardClass = cardClass;
    }

    public boolean isShowUpload() {
        return showUpload;
    }

    public void setShowUpload(boolean showUpload) {
        this.showUpload = showUpload;
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }

    public boolean isImagePreview() {
        return imagePreview;
    }

    public void setImagePreview(boolean imagePreview) {
        this.imagePreview = imagePreview;
    }

    public boolean isShowFeedback() {
        return showFeedback;
    }

    public void setShowFeedback(boolean showFeedback) {
        this.showFeedback = showFeedback;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}
