package controller.examiner;

import model.ExaminerSchedule;
import dto.ExportContextDTO;
import enums.DocumentName;
import filter.ExaminerFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

abstract class BaseExaminerExportServlet extends HttpServlet {

    protected ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        Integer activeSessionId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID);
        if (activeSessionId == null || activeSessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        boolean isTheory = ExaminerFilter.isTheory(session);
        String sectionName = (String) session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);

        return new ExportContextDTO(activeSessionId, schedule, isTheory, sectionName);
    }

    protected DocumentName parseDocumentName(String alias) {
        return DocumentName.parseAlias(alias);
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

    protected void prepareZipDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/zip");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    protected void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    protected void prepareDocxInline(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    protected void flush(OutputStream out) throws IOException {
        out.flush();
    }
}
