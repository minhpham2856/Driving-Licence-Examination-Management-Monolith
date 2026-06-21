package Controllers.Examiner;

import Services.DocxExportService;
import Services.ExaminerExportContext;
import Services.ExaminerViewDataService;
import Services.Impl.DocxExportServiceImpl;
import Services.Impl.ExaminerViewDataServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/examiner/export/docx")
public class ExportResultsDocxServlet extends ExaminerExportServlet {

    private final DocxExportService docxService = new DocxExportServiceImpl();
    private final ExaminerViewDataService viewDataService = new ExaminerViewDataServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) return;

        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(ctx.sessionId());
        List<String> sbds = candidates.stream()
                .map(c -> (String) c.get("sbd"))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (sbds.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Khong co thi sinh nao.");
            return;
        }

        String docType;
        int examTypeId = ctx.slot() != null ? ctx.slot().getExamTypeId() : 1;
        if (examTypeId == 2) {
            docType = "BB2";
        } else if (examTypeId == 4) {
            docType = "BB3";
        } else {
            docType = "BB1";
        }

        prepareZipDownload(response, "bien-ban-thi.zip");
        OutputStream out = response.getOutputStream();
        docxService.batchGenerateDocuments(sbds, ctx.sessionId(), docType, out);
        flush(out);
    }
}
