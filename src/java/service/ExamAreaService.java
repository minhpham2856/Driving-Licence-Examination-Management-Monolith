package service;

import model.exam.ExamArea;
import java.util.List;

public interface ExamAreaService {
    ExamArea findById(int id);
    List<ExamArea> search(String keyword, String type);
    int countAll();
    List<ExamArea> getActiveTheoryRooms();
    SaveResult save(ExamArea area, int adminUserId);
    DeleteResult delete(int id, int adminUserId);

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
