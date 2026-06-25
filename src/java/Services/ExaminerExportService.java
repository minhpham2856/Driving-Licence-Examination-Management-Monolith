package Services;

 // Service interface for building export payloads (Excel / XML) from examiner session data.
 // Each method produces an {@link ExaminerExportPayload} that contains both
 // the structured table data and metadata required by the Excel and XML export servlets.
 // The actual serialisation to XLSX / XML is handled by the servlet layer using
 // the payload returned here.
// Contract for assembling structured export payloads for all five report types
public interface ExaminerExportService {

    // Builds the candidate-list export (danh sach thi sinh) — all personal info, scores, and results
    ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx);

    // Builds the results export (ket qua sat hach) — pass/fail outcomes per candidate
    ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx);

    // Builds the exam minutes export (bien ban) — metadata, statistics, and candidate data
    ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx);

    // Builds the violations export (vi pham) — audit violations and score deductions
    ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx);

    // Builds the audit-log export (nhat ky) — all audit trail entries, optionally filtered
    ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery);
}
