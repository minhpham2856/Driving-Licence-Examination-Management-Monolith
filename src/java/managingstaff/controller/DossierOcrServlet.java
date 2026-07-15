package managingstaff.controller;

import auth.dto.UserDTO;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.DossierDTO.DocumentView;
import managingstaff.dto.OcrResultDTO;
import managingstaff.service.OcrService;
import managingstaff.service.impl.OcrSpaceServiceImpl;
import managingstaff.util.DossierFileResolver;
import managingstaff.util.SessionUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@WebServlet("/manager/dossiers/ocr")
public class DossierOcrServlet extends HttpServlet {

    private static final Set<String> OCR_DOCUMENT_TYPES = Set.of(
            "ID_FRONT", "ID_BACK", "HEALTH_CERTIFICATE");
    private static final Map<String, String> TYPE_LABELS = Map.of(
            "ID_FRONT", "CCCD mặt trước",
            "ID_BACK", "CCCD mặt sau",
            "HEALTH_CERTIFICATE", "Giấy khám sức khỏe");

    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final OcrService ocrService = new OcrSpaceServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO reviewer = SessionUtil.getCurrentUser(request);
        String role = reviewer == null || reviewer.getRole() == null
                ? "" : reviewer.getRole().getRoleName();
        if (reviewer == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int registrationId = parseInt(request.getParameter("id"));
        String documentType = normalizeType(request.getParameter("documentType"));
        String redirect = request.getContextPath() + "/manager/dossiers?id=" + registrationId;
        if (registrationId <= 0 || !OCR_DOCUMENT_TYPES.contains(documentType)) {
            request.getSession().setAttribute("ocrError", "Yêu cầu OCR không hợp lệ.");
            response.sendRedirect(redirect);
            return;
        }

        DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
        DocumentView document = dossier == null ? null : dossier.getDocuments().get(documentType);
        if (document == null) {
            request.getSession().setAttribute("ocrError", "Hồ sơ chưa có " + TYPE_LABELS.get(documentType) + ".");
            response.sendRedirect(redirect);
            return;
        }

        String webRoot = getServletContext().getRealPath("/");
        if (webRoot == null) {
            request.getSession().setAttribute("ocrError", "Không xác định được thư mục tài liệu trên máy chủ.");
            response.sendRedirect(redirect);
            return;
        }

        try {
            Path file = DossierFileResolver.resolve(Path.of(webRoot), document.getDocumentUrl());
            OcrResultDTO result = ocrService.recognize(file);
            if (result.success()) {
                request.getSession().setAttribute("ocrText", result.text());
                request.getSession().setAttribute("ocrDocumentLabel", TYPE_LABELS.get(documentType));
            } else {
                request.getSession().setAttribute("ocrError", result.errorMessage());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            request.getSession().setAttribute("ocrError", "Yêu cầu OCR bị gián đoạn. Vui lòng thử lại.");
        } catch (Exception ex) {
            request.getSession().setAttribute("ocrError", "Không thể đọc OCR: " + ex.getMessage());
        }
        response.sendRedirect(redirect);
    }

    private static String normalizeType(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return 0;
        }
    }
}
