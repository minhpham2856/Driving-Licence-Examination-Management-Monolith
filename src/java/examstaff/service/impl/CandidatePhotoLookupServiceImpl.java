package examstaff.service.impl;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.service.CandidatePhotoLookupService;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueService;

import java.io.File;

public class CandidatePhotoLookupServiceImpl implements CandidatePhotoLookupService {

    private final CandidateQueueService queueService = new CandidateQueueServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    @Override
    public CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int fallbackExamId, String sbd) {
        CandidatePhotoStreamDTO result = new CandidatePhotoStreamDTO();
        if (sbd == null || sbd.isBlank()) {
            return result;
        }

        ExamRegistrationDTO reg = queueService.findByExam(examId, fallbackExamId, sbd.trim());
        if (reg == null || reg.getPhotoUrl() == null || reg.getPhotoUrl().isBlank()) {
            return result;
        }

        File file = photoService.findPhotoFile(webRoot, reg.getPhotoUrl());
        if (file == null || !file.isFile()) {
            return result;
        }

        String name = file.getName().toLowerCase();
        result.setStatus(CandidatePhotoStreamDTO.Status.FOUND);
        result.setPhotoFile(file);
        result.setContentType(name.endsWith(".png") ? "image/png" : "image/jpeg");
        return result;
    }
}
