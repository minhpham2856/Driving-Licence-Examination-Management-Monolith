package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface StaffReportExportService {

    void exportExamReport(OutputStream out, ExamSummaryDTO exam,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException;
}
