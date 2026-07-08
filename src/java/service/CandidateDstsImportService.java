package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateDstsImportCommitResultDTO;
import dto.examstaff.CandidateDstsImportPreviewDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CandidateDstsImportService {

    CandidateDstsImportPreviewDTO preview(byte[] fileBytes, String fileName,
            String examLicenseCode, int sessionId) throws IOException;

    CandidateDstsImportCommitResultDTO commit(List<ExamRegistrationDTO> previewRows, int sessionId,
            Map<String, String> duplicateActionsByGovId);
}
