package Models;

public class ProfileDocumentItem {

    private String label;
    private boolean uploaded;
    private String statusLabel;
    private String statusClass;
    private String dotClass;
    private boolean showUploadLink;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
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

    public String getDotClass() {
        return dotClass;
    }

    public void setDotClass(String dotClass) {
        this.dotClass = dotClass;
    }

    public boolean isShowUploadLink() {
        return showUploadLink;
    }

    public void setShowUploadLink(boolean showUploadLink) {
        this.showUploadLink = showUploadLink;
    }
}
