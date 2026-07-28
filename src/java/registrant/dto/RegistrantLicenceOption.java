package registrant.dto;

/**
 * DTO tuỳ chọn hạng GPLX trên wizard đăng ký đợt thi (register-exam.jsp).
 * Chứa mã hạng (khớp seed DB: A, A1, B1…), tên hiển thị và loại phương tiện (icon moto/car).
 */
public class RegistrantLicenceOption {

    /** Mã hạng GPLX hiển thị (khớp DB seed: A, A1, B1, …). */
    private String code;
    private String name;
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

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
