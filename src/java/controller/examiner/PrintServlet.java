package controller.examiner;

import dto.ExportContextDTO;
import enums.DocumentName;
import enums.SectionType;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExaminerSchedule;
import service.DocumentService;
import service.impl.DocxServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {
    "/examiner/print",
    "/examiner/print/docx"
})
public class PrintServlet extends HttpServlet {

    private final DocumentService docxService = new DocxServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        String type = request.getParameter("type");
        int sbd = 0;
        String sbdRaw = request.getParameter("sbd");
        if (sbdRaw != null && !sbdRaw.isBlank()) {
            try {
                sbd = Integer.parseInt(sbdRaw.trim());
            } catch (NumberFormatException ex) {
                sbd = 0;
            }
        }
        if (sbd <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu số báo danh.");
            return;
        }
        prepareDocxDownload(response, DocumentName.printCandidate(type, sbd));
        OutputStream out = response.getOutputStream();
        try {
            docxService.print(ctx, type, sbd, out);
        } finally {
            out.flush();
        }
    }

    private ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
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
        SectionType section = (SectionType) session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        boolean isTheory = section == SectionType.THEORY;
        String sectionName = section.getValue();
        return new ExportContextDTO(activeSessionId, schedule, isTheory, sectionName);
    }

    private void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }
}
