package service;

import model.licence.Licence;
import java.util.List;

public interface LicenceService {
    List<Licence> search(String keyword);
    List<Licence> findAll();
    Licence findById(int id);
    int countAll();
    SaveResult save(Licence licence, int adminUserId);

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
}
