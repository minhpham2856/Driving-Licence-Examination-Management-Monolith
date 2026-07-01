package examstaff.service.impl;

import dto.exam.ExamRegistrationDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.util.CandidatePhotoStorageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CandidatePhotoServiceImpl implements CandidatePhotoService {

    @Override
    public void normalizeQueue(String webRoot, List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO reg : queue) {
            resolveCapturedPhoto(webRoot, reg);
        }
    }

    @Override
    public void normalizePhotoPaths(String webRoot, List<ExamRegistrationDTO> queue) {
        examstaff.util.CandidatePhotoPathUtil.normalizeQueue(webRoot, queue);
    }

    @Override
    public boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    @Override
    public boolean resolveCapturedPhoto(String webRoot, ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String normalized = examstaff.util.CandidatePhotoPathUtil.normalizePhotoUrl(webRoot, reg.getPhotoUrl());
        if (normalized != null) {
            reg.setPhotoUrl(normalized);
        }
        boolean valid = findPhotoFile(webRoot, reg.getPhotoUrl()) != null;
        reg.setValidCapturedPhoto(valid);
        return valid;
    }

    @Override
    public boolean photoFileExists(String webRoot, String photoUrl) {
        return findPhotoFile(webRoot, photoUrl) != null;
    }

    @Override
    public File findPhotoFile(String webRoot, String photoUrl) {
        return CandidatePhotoStorageUtil.findPhotoFile(webRoot, photoUrl);
    }

    @Override
    public void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException {
        CandidatePhotoStorageUtil.writePhotoFile(webRoot, fileName, imageBytes);
    }

    @Override
    public String toWebPhotoPath(String fileName) {
        return CandidatePhotoStorageUtil.toWebPhotoPath(fileName);
    }

    @Override
    public String extractFileName(String photoUrl) {
        return CandidatePhotoStorageUtil.extractFileName(photoUrl);
    }

    @Override
    public File resolveUploadDir(String webRoot) {
        return CandidatePhotoStorageUtil.resolveUploadDir(webRoot);
    }
}
