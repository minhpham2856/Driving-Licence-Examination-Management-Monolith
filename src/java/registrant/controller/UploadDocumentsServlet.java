package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantUploadService;
import registrant.service.impl.RegistrantUploadServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Upload giấy tờ — GET/POST /registrant/upload-documents (multipart, tối đa 5MB/file).
 * GET: load Document + ExamRegistration status → JSP (4 loại bắt buộc + Other).
 * POST actions: upload file → ghi Document (DocumentTypeId, DocumentUrl, Notes, ProfileId);
 * action=requestApproval → ER Pending + Notes #PROFILE_DOC# / Document #PENDING#;
 * action=deleteDocument → xóa khi status cho phép (Draft/Rejected).
 * Không tạo Payment / Candidate — chỉ hồ sơ trước ngày thi.
 */
@WebServlet("/registrant/upload-documents")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 30)
public class UploadDocumentsServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/upload-documents.jsp";

    private final RegistrantUploadService uploadService = new RegistrantUploadServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        renderPage(user, request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }

        String action = request.getParameter("action");
        if ("requestApproval".equals(action)) {
            handleRequestApproval(user, request, response);
            return;
        }
        if ("deleteDocument".equals(action)) {
            handleDeleteDocument(user, request, response);
            return;
        }

        String documentType = request.getParameter("documentType");
        String error;
        if ("Other".equals(documentType)) {
            error = uploadService.handleOtherUpload(
                    user, request.getParameter("reasonNote"),
                    collectFileParts(request), request);
        } else {
            error = uploadService.handleUpload(
                    user, documentType, request.getPart("documentFile"),
                    request.getParameter("reasonNote"), request);
        }

        if (error != null) {
            renderPage(user, request, response, error);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/registrant/upload-documents?success=upload");
    }

    private void handleRequestApproval(UserDTO user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String error = uploadService.requestApproval(
                user,
                request.getParameter("requestNote"),
                request.getParameter("approvalLicenceCode"),
                request.getSession());
        if (error != null) {
            renderPage(user, request, response, error);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/registrant/upload-documents?success=request");
    }

    private void handleDeleteDocument(UserDTO user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int documentId = RegistrantServletSupport.parsePositiveInt(request.getParameter("documentId"));
        String error = uploadService.deleteDocument(user, documentId, request);
        if (error != null) {
            renderPage(user, request, response, error);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/registrant/upload-documents?success=delete");
    }

    private void renderPage(UserDTO user, HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        if (error != null) {
            request.setAttribute("error", error);
        }
        RegistrantServletSupport.copyModelToRequest(uploadService.loadUploadPage(user, request), request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    private static List<Part> collectFileParts(HttpServletRequest request) throws IOException, ServletException {
        List<Part> files = new ArrayList<>();
        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            if ("documentFile".equals(part.getName()) && part.getSize() > 0) {
                files.add(part);
            }
        }
        return files;
    }
}
