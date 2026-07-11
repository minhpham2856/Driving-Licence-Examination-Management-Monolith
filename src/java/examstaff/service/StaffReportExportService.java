package examstaff.service;

import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface StaffReportExportService {

    void exportExamReport(OutputStream out, ExamSummaryDTO session,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException;
}
