package model.registrant;

/**
 * Một mục trên timeline "Hoạt động gần đây" của dashboard thí sinh.
 */
public class RegistrantDashboardActivity {

    private String colorClass;
    private String iconPath;
    private String title;
    private String desc;
    /** Nhãn hiển thị trên UI (vd: "Hôm nay, 09:45"). */
    private String time;
    /** Mốc thời gian thực — dùng nội bộ để sắp xếp, không hiển thị trên JSP. */
    private java.util.Date occurredAt;

    public String getColorClass() {
        return colorClass;
    }

    public void setColorClass(String colorClass) {
        this.colorClass = colorClass;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public java.util.Date getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(java.util.Date occurredAt) {
        this.occurredAt = occurredAt;
    }
}
