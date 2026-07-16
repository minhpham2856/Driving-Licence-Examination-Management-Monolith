package examiner.controller;

import examiner.dto.ExportContextDTO;
import shared.enums.FileName;
import shared.enums.SectionType;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.ExaminerSchedule;
import examiner.service.DocumentService;
import examiner.service.impl.DocxServiceImpl;

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
        prepareDocxDownload(response, buildPrintFilename(type, sbd));
        OutputStream out = response.getOutputStream();
        try {
            docxService.print(ctx, type, sbd, out);
        } finally {
            out.flush();
        }
    }

    private static String buildPrintFilename(String type, int sbd) {
        String base;
        if (type == null || type.isBlank()) {
            base = FileName.DEFAULT.getValue();
        } else {
            String normalized = type.trim().toLowerCase();
            base = switch (normalized) {
                case "candidates" -> FileName.CANDIDATES.getValue();
                case "results" -> FileName.RESULTS.getValue();
                case "minutes" -> FileName.RESULT.getValue();
                case "violations" -> FileName.VIOLATIONS.getValue();
                case "audit" -> FileName.AUDIT.getValue();
                case "bb1", "bb1-ly-thuyet" -> FileName.BB1.getValue();
                case "bb2", "bb2-thuc-hanh-trong-hinh" -> FileName.BB2.getValue();
                default -> {
                    FileName byValue = FileName.fromValue(normalized);
                    yield byValue != null ? byValue.getValue() : FileName.DEFAULT.getValue();
                }
            };
        }
        return base + "-sbd-" + sbd + ".docx";
    }

    private ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        SectionType section = (SectionType) session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        boolean isTheory = section == SectionType.THEORY;
        String sectionName = section.getValue();
        return new ExportContextDTO(activeExamId, schedule, isTheory, sectionName);
    }

    private void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }
}

