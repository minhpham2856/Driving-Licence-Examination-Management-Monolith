package service;

import dto.ExamDeviceViewDTO;
import java.util.List;

public interface ExamDeviceService {
    List<ExamDeviceViewDTO> search(String keyword, String status);
    int countAll();
    int countByStatus(String status);
    SaveResult save(ExamDeviceViewDTO device, Integer adminUserId);
    DeleteResult delete(int id, Integer adminUserId);

    public static class SaveResult {
        public final boolean success;
        public final String message;
        public final int id;
        public SaveResult(boolean success, String message, int id) {
            this.success = success;
            this.message = message;
            this.id = id;
        }
    }

    public static class DeleteResult {
        public final boolean success;
        public final String message;
        public DeleteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
