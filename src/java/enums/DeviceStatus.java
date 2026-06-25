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

    public static String statusLabelVi(String status) {
        if (status == null) return "-";
        for (DeviceStatus ds : values()) {
            if (ds.status.equalsIgnoreCase(status.trim())) {
                return ds.labelVi;
            }
        }
        return status;
    }

    public static String statusCssClass(String status) {
        if (status == null) return "device-grid-card--unknown";
        for (DeviceStatus ds : values()) {
            if (ds.status.equalsIgnoreCase(status.trim())) {
                return ds.cssClass;
            }
        }
        return "device-grid-card--unknown";
    }
}
