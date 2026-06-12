package Controllers.Examiner;

import Constants.ExamSectionType;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import Services.ExaminerExportContext;
import Services.ExaminerSessionContextService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

abstract class ExaminerExportServlet extends HttpServlet {

    protected ExaminerExportContext requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập.");
            return null;
        }

        Integer activeSessionId = (Integer) session.getAttribute(ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID);
        if (activeSessionId == null || activeSessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chưa có ca thi đang diễn ra.");
            return null;
        }

        ExaminerSlot slot = (ExaminerSlot) session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        ExamSectionType sectionType = ExamSectionType.THEORY;
        Object sectionTypeObj = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (sectionTypeObj instanceof ExamSectionType) {
            sectionType = (ExamSectionType) sectionTypeObj;
        }
        String sectionName = (String) session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return new ExaminerExportContext(activeSessionId, slot, sectionType, sectionName);
    }

    protected void prepareExcelDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    protected void prepareXmlDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/xml; charset=UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    protected void flush(OutputStream out) throws IOException {
        out.flush();
    }
}
