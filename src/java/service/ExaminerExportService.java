package service;
import dto.ExaminerExportContext;
import dto.ExaminerExportPayload;


 
 
 
 
 

public interface ExaminerExportService {

    
    ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx);

    
    ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx);

    
    ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx);

    
    ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx);

    
    ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery);
}


