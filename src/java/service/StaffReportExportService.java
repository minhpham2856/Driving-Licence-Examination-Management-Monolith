package service;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamReportStatsDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface StaffReportExportService {

    void exportExamReport(OutputStream out, SessionDTO session,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException;
}
