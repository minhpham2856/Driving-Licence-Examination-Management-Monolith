package registrant.dto;

/**
 * DTO một lựa chọn trong dropdown bộ lọc (value + nhãn hiển thị + cờ selected).
 * Dùng trên dashboard, my-exams, register-exam và track-profile qua RegistrantFilterSupport.
 */
public class RegistrantFilterOption {

    private final String value;
    private final String label;
    private final boolean selected;

    public RegistrantFilterOption(String value, String label, boolean selected) {
        this.value = value;
        this.label = label;
        this.selected = selected;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSelected() {
        return selected;
    }

    /** Alias cho JSP EL (một số container không resolve isSelected). */
    public boolean getSelected() {
        return selected;
    }
}
