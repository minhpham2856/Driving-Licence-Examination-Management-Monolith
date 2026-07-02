package service.impl;

import controller.staff.exam.CandidatePhotoHelper;
import dto.exam.ExamRegistrationDTO;
import service.CandidatePhotoService;

import java.util.List;

public class CandidatePhotoServiceImpl implements CandidatePhotoService {

    @Override
    public void normalizeQueue(String appRoot, List<ExamRegistrationDTO> qList) {
        if (qList == null || qList.isEmpty()) {
            return;
        }

        for (ExamRegistrationDTO reg : qList) {
            CandidatePhotoHelper.resolveCapturedPhoto(appRoot, reg);
        }
    }
}
