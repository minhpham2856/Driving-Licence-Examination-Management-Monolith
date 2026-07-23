package registrant.controller;

import auth.dao.ProfileDAO;
import auth.dao.impl.ProfileDAOImpl;
import auth.dto.UserDTO;
import registrant.dao.DocumentDAO;
import registrant.dao.impl.DocumentDAOImpl;
import registrant.dto.RegistrantDocumentView;
import registrant.util.DocumentUrlResolver;
import registrant.util.RegistrantProfileSupport;
import shared.model.Profile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet xem tài liệu hồ sơ — {@code GET /registrant/document-view?id=DocumentId}.
 * <p>
 * Sau {@link RegistrantAuth#requireRegistrant}, kiểm tra tài liệu thuộc {@code Profile} của user,
 * resolve {@code DocumentUrl} qua {@link registrant.util.DocumentUrlResolver}
 * (Cloudinary signed / legacy {@code /uploads/registrant/*} / placeholder seed) rồi redirect trình duyệt.
 * Không stream trực tiếp từ DB; chỉ ủy quyền xem file đã upload.
 */
@WebServlet("/registrant/document-view")
public class RegistrantDocumentViewServlet extends HttpServlet {

    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }

        int documentId = RegistrantServletSupport.parsePositiveInt(request.getParameter("id"));
        if (documentId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã tài liệu.");
            return;
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ.");
            return;
        }

        RegistrantDocumentView doc = documentdao.findById(profile.getProfileId(), documentId);
        if (doc == null || doc.getDocumentUrl() == null || doc.getDocumentUrl().isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tệp.");
            return;
        }

        String viewUrl = DocumentUrlResolver.resolveViewUrl(doc.getDocumentUrl(), request);
        if (viewUrl == null || viewUrl.isBlank() || viewUrl.startsWith("cloudinary:")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tạo được liên kết xem tệp. Vui lòng tải lên lại.");
            return;
        }

        response.setHeader("Cache-Control", "private, no-store");
        response.sendRedirect(viewUrl);
    }
}
