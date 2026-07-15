package managingstaff.controller;

import auth.dto.UserDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO.DocumentView;
import managingstaff.util.CloudinaryDocumentReader;
import managingstaff.util.SessionUtil;

@WebServlet("/manager/document-view")
public class DocumentViewServlet extends HttpServlet {
    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int documentId = parsePositiveInt(request.getParameter("id"));
        DocumentView document = documentId > 0 ? dossierDAO.findDocumentById(documentId) : null;
        if (document == null || document.getDocumentUrl() == null || document.getDocumentUrl().isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu.");
            return;
        }
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (CloudinaryDocumentReader.supports(document.getDocumentUrl())) {
            CloudinaryDocumentReader.Resource resource = CloudinaryDocumentReader.read(document.getDocumentUrl());
            response.setContentType(resource.contentType());
            response.setContentLength(resource.bytes().length);
            response.getOutputStream().write(resource.bytes());
            return;
        }
        String localPath = document.getDocumentUrl().startsWith("/")
                ? document.getDocumentUrl() : "/" + document.getDocumentUrl();
        try (InputStream input = getServletContext().getResourceAsStream(localPath)) {
            if (input == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tệp tài liệu.");
                return;
            }
            String contentType = getServletContext().getMimeType(localPath);
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            input.transferTo(response.getOutputStream());
        }
    }

    private static int parsePositiveInt(String value) {
        try { int id = Integer.parseInt(value); return id > 0 ? id : 0; }
        catch (Exception ignored) { return 0; }
    }
}
