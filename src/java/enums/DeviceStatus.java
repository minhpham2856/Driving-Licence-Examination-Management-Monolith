package enums;

public enum DeviceStatus {
    AVAILABLE("Available", "Sẵn sàng", "device-grid-card--available"),
    OPERATIONAL("Operational", "Sẵn sàng", "device-grid-card--available"),
    IN_USE("InUse", "Đang dùng", "device-grid-card--inuse"),
    MAINTENANCE("Maintenance", "Bảo trì", "device-grid-card--maintenance");

    private final String status;
    private final String labelVi;
    private final String cssClass;

    DeviceStatus(String status, String labelVi, String cssClass) {
        this.status = status;
        this.labelVi = labelVi;
        this.cssClass = cssClass;
    }

    public String getStatus() {
        return status;
    }

    public String getLabelVi() {
        return labelVi;
    }

    public String getCssClass() {
        return cssClass;
    }
}
