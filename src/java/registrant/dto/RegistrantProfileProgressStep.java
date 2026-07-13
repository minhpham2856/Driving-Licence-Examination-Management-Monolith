package registrant.dto;

import java.util.Date;

/** Một mốc trên timeline theo dõi tiến trình hồ sơ (track-profile.jsp). */
public class RegistrantProfileProgressStep {

    public static final String STATE_COMPLETED = "completed";
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_PENDING = "pending";

    private String stepKey;
    private String title;
    private String state;
    private Date timestamp;
    private String description;
    private String footerText;
    /** shield | clock | none */
    private String footerType;
    private String statusHint;
    private boolean placeholder;
    private String icon;

    public String getStepKey() {
        return stepKey;
    }

    public void setStepKey(String stepKey) {
        this.stepKey = stepKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getFooterType() {
        return footerType;
    }

    public void setFooterType(String footerType) {
        this.footerType = footerType;
    }

    public String getStatusHint() {
        return statusHint;
    }

    public void setStatusHint(String statusHint) {
        this.statusHint = statusHint;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isCompleted() {
        return STATE_COMPLETED.equals(state);
    }

    public boolean isActive() {
        return STATE_ACTIVE.equals(state);
    }

    public boolean isPending() {
        return STATE_PENDING.equals(state);
    }
}
