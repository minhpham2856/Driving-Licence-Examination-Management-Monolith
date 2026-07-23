package registrant.dto;

/**
 * DTO một mục trong panel "Việc cần làm" trên dashboard thí sinh.
 * <p>
 * Chứa tiêu đề, mô tả, nhãn nút hành động, {@code href} servlet (vd. {@code /registrant/upload-documents})
 * và {@code tone} màu (warning/danger/info/…). Sinh bởi {@link registrant.util.RegistrantDashboardActionItemsBuilder}.
 */
public class RegistrantDashboardActionItem {

    private String title;
    private String description;
    private String actionLabel;
    /** Đường dẫn servlet (không gồm context path), vd. /registrant/profile */
    private String href;
    /** warning | danger | info | success | neutral */
    private String tone;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
