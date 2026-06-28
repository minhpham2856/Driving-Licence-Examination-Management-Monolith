package enums;

public enum DeviceType {
    COMPUTER("Computer", "Máy thi", "computer", true),
    MOTORCYCLE("Motorcycle", "Xe máy", "two_wheeler", false),
    CAR("Car", "Ô tô", "directions_car", false),
    TRUCK("Truck", "Xe tải", "local_shipping", false);

    private final String typeName;
    private final String labelVi;
    private final String icon;
    private final boolean isComputer;

    DeviceType(String typeName, String labelVi, String icon, boolean isComputer) {
        this.typeName = typeName;
        this.labelVi = labelVi;
        this.icon = icon;
        this.isComputer = isComputer;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getLabelVi() {
        return labelVi;
    }

    public String getIcon() {
        return icon;
    }
}
