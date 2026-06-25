// Forced recompilation trigger
package service.impl;


import dao.ExamRegistrationDAO;

import dao.impl.ExamRegistrationDAOImpl;

import dto.exam.ExamRegistrationDTO;

import service.CandidatePhotoService;
import java.io.File;
import java.util.List;

public class CandidatePhotoServiceImpl implements CandidatePhotoService {

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();

    @Override
    public void normalizeQueue(String appRoot, List<ExamRegistrationDTO> qList) {
        if (qList == null || qList.isEmpty()) return;

        for (ExamRegistrationDTO r : qList) {
            String pUrl = r.getPhotoUrl();
            if (pUrl != null && !pUrl.trim().isEmpty()) {
                File f = new File(appRoot, pUrl.trim());
                if (!f.exists() || !f.isFile()) {
                    regDAO.updatePhoto(r.getId(), null);
                    r.setPhotoUrl(null);
                }
            }
        }
    }
}

