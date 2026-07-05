package dto.payload;

public class SaveExamDeviceData {

    private final int deviceId;
    private final String message;

    public SaveExamDeviceData(int deviceId, String message) {
        this.deviceId = deviceId;
        this.message = message;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getMessage() {
        return message;
    }
}
