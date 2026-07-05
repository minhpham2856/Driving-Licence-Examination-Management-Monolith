package dto.payload;

import dto.ExamDeviceViewDTO;

public class SaveExamDeviceCommand {

    private ExamDeviceViewDTO device;
    private Integer adminUserId;

    public ExamDeviceViewDTO getDevice() {
        return device;
    }

    public void setDevice(ExamDeviceViewDTO device) {
        this.device = device;
    }

    public Integer getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Integer adminUserId) {
        this.adminUserId = adminUserId;
    }
}
