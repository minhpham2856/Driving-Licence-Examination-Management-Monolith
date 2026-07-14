package examstaff.service.impl;

import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueService;
import examstaff.util.CandidatePhotoStorageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Implementation: chuẩn hoá đường dẫn và lưu/đọc ảnh chân dung. */
public class CandidatePhotoServiceImpl implements CandidatePhotoService {

    private final CandidateQueueService queueService;

    /**
     * Wiring mặc định cho normalize/ghi file — không tạo Queue (tránh vòng
     * Photo ↔ Queue ↔ Query khi composition root chưa inject).
     */
    public CandidatePhotoServiceImpl() {
        this.queueService = null;
    }

    /** Inject queue để {@link #resolvePhoto} theo SBD. */
    public CandidatePhotoServiceImpl(CandidateQueueService queueService) {
        this.queueService = queueService;
    }

    /** {@inheritDoc} */
    @Override
    public void normalizeQueue(String webRoot, List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO reg : queue) {
            resolveCapturedPhoto(webRoot, reg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void normalizePhotoPaths(String webRoot, List<ExamRegistrationDTO> queue) {
        examstaff.util.CandidatePhotoPathUtil.normalizeQueue(webRoot, queue);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public boolean photoFileExists(String webRoot, String photoUrl) {
        return findPhotoFile(webRoot, photoUrl) != null;
    }

    /** {@inheritDoc} */
    @Override
    public File findPhotoFile(String webRoot, String photoUrl) {
        return CandidatePhotoStorageUtil.findPhotoFile(webRoot, photoUrl);
    }

    /** {@inheritDoc} */
    @Override
    public void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException {
        CandidatePhotoStorageUtil.writePhotoFile(webRoot, fileName, imageBytes);
    }

    /** {@inheritDoc} */
    @Override
    public String toWebPhotoPath(String fileName) {
        return CandidatePhotoStorageUtil.toWebPhotoPath(fileName);
    }

    /** {@inheritDoc} */
    @Override
    public CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int fallbackExamId, String sbd) {
        CandidatePhotoStreamDTO result = new CandidatePhotoStreamDTO();
        if (queueService == null || sbd == null || sbd.isBlank()) {
            return result;
        }

        ExamRegistrationDTO reg = queueService.findByExam(examId, fallbackExamId, sbd.trim());
        if (reg == null || reg.getPhotoUrl() == null || reg.getPhotoUrl().isBlank()) {
            return result;
        }

        File file = findPhotoFile(webRoot, reg.getPhotoUrl());
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
