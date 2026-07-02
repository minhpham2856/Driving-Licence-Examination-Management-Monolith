package dto.registrant;

/**
 * Tuỳ chọn hạng GPLX trên màn hình đăng ký đợt thi (register-exam.jsp).
 */
public class RegistrantLicenceOption {

    /** Mã hiển thị trên UI (A1, A2, B2, C1, …). */
    private String code;
    private String name;
    private long examFee;
    /** Gợi ý loại phương tiện để chọn icon (moto / car). */
    private String vehicleType;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getExamFee() {
        return examFee;
    }

    public void setExamFee(long examFee) {
        this.examFee = examFee;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
