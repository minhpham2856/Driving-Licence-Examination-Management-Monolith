package Controllers.Registrant;

import Models.User;
import Services.Impl.RegistrantUploadServiceImpl;
import Services.RegistrantUploadService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Upload hồ sơ bổ sung: ảnh 3x4, CCCD 2 mặt, giấy khám sức khỏe.
 * <p>URL: GET/POST /registrant/upload-documents (multipart, tối đa 5MB/file)</p>
 * <p>Lưu file: web/uploads/documents/{personId}/ | DB: CandidateDocument</p>
 * <p>submitForReview=true → PersonDAO.markPendingReview (chờ staff duyệt)</p>
 */
@WebServlet("/registrant/upload-documents")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 25 * 1024 * 1024
)
public class UploadDocumentsServlet extends HttpServlet {

    private final RegistrantUploadService uploadService = new RegistrantUploadServiceImpl();

    /** Hiển thị 4 slot upload (ảnh 3x4, CCCD 2 mặt, giấy khám) và trạng thái từng tài liệu. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để upload hồ sơ.");
        if (user == null) {
            return;
        }

        RegistrantAuth.transferFlash(request, "successMessage", "success");
        RegistrantAuth.transferFlash(request, "errorMessage", "error");
        uploadService.populateUploadPage(request, user);
        request.getRequestDispatcher("/views/registrant/upload-documents.jsp").forward(request, response);
    }

    /**
     * Nhận multipart POST, lưu file vào uploads/documents/{personId}/ và ghi CandidateDocument.
     * submitForReview=true → PersonDAO.markPendingReview (chờ staff duyệt).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để upload hồ sơ.");
        if (user == null) {
            return;
        }

        if ("delete".equals(request.getParameter("action"))) {
            String deleteError = uploadService.deleteDocument(request, user);
            if (deleteError != null) {
                request.getSession().setAttribute("errorMessage", deleteError);
            } else {
                request.getSession().setAttribute("successMessage", "Đã xóa tài liệu thành công.");
            }
            response.sendRedirect(request.getContextPath() + "/registrant/upload-documents");
            return;
        }

        try {
            String error = uploadService.uploadDocuments(request, user);
            if (error != null) {
                request.setAttribute("error", error);
                uploadService.populateUploadPage(request, user);
                request.getRequestDispatcher("/views/registrant/upload-documents.jsp").forward(request, response);
                return;
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            uploadService.populateUploadPage(request, user);
            request.getRequestDispatcher("/views/registrant/upload-documents.jsp").forward(request, response);
            return;
        }

        String message = "true".equals(request.getParameter("submitForReview"))
                ? "Hồ sơ bổ sung đã được gửi duyệt thành công."
                : "Tải lên tài liệu thành công.";
        request.getSession().setAttribute("successMessage", message);
        response.sendRedirect(request.getContextPath() + "/registrant/upload-documents");
    }
}
