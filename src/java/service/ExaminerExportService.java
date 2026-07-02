package service;

import dto.examiner.ExaminerExportContext;
import dto.examiner.ExaminerExportPayload;

public interface ExaminerExportService {

    ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx);

    ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx);

    ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx);

    ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx);

    ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery);
}
