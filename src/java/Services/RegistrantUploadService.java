package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantUploadService {

    void populateUploadPage(HttpServletRequest request, User user);

    String uploadDocuments(HttpServletRequest request, User user);

    /** Xóa tài liệu theo slotKey (photo, idFront, idBack, healthCert). */
    String deleteDocument(HttpServletRequest request, User user);
}
