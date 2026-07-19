package examstaff.service.impl.support.view;
import examstaff.service.impl.support.call.CandidateQueueServiceImpl;

import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.util.CandidatePhotoFiles;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Implementation: chuẩn hoá / lưu / đọc ảnh chân dung (thư mục data runtime, không webRoot). */
public class CandidatePhotoServiceImpl {

    private final CandidateQueueServiceImpl queueService;

    /**
     * Wiring mặc định — không tạo Queue (tránh vòng Photo ↔ Queue khi composition root chưa inject).
     */
    public CandidatePhotoServiceImpl() {
        this.queueService = null;
    }

    /** Inject queue để {@link #resolvePhoto} theo SBD. */
    public CandidatePhotoServiceImpl(CandidateQueueServiceImpl queueService) {
        this.queueService = queueService;
    }

    /**
     * Chuẩn hóa thông tin ảnh trên hàng đợi (đường dẫn + cờ còn file).
     *
     * @param queue hàng đợi thí sinh
     */
    public void normalizeQueue(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO reg : queue) {
            resolveCapturedPhoto(reg);
        }
    }

    /**
     * Chuẩn hóa tham chiếu ảnh trên hàng đợi (resolve absolute path nếu có file).
     *
     * @param queue hàng đợi thí sinh
     */
    public void normalizePhotoPaths(List<ExamRegistrationDTO> queue) {
        CandidatePhotoFiles.normalizeQueue(queue);
    }

    /**
     * Đã có bản ghi ảnh (photoUrl) hay chưa.
     *
     * @param reg hồ sơ đăng ký
     * @return true nếu đã có photoUrl
     */
    public boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    /**
     * Gắn cờ ảnh đã chụp có file vật lý trên đĩa data.
     *
     * @param reg hồ sơ đăng ký
     * @return true nếu tìm thấy file hợp lệ
     */
    public boolean resolveCapturedPhoto(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String normalized = CandidatePhotoFiles.normalizePhotoUrl(reg.getPhotoUrl());
        if (normalized != null) {
            reg.setPhotoUrl(normalized);
        }
        boolean valid = findPhotoFile(reg.getPhotoUrl()) != null;
        reg.setValidCapturedPhoto(valid);
        return valid;
    }

    /**
     * File ảnh tồn tại trên đĩa data theo tham chiếu DB.
     *
     * @param photoUrl {@code candidate-photos/...} / basename / legacy
     * @return true nếu file tồn tại
     */
    public boolean photoFileExists(String photoUrl) {
        return findPhotoFile(photoUrl) != null;
    }

    /**
     * Tìm file ảnh theo basename của tham chiếu DB.
     *
     * @param photoUrl tham chiếu trên hồ sơ
     * @return file hoặc null
     */
    public File findPhotoFile(String photoUrl) {
        return CandidatePhotoFiles.findPhotoFile(photoUrl);
    }

    /**
     * Ghi bytes ảnh vào thư mục data runtime (Tomcat {@code dlem-data} / JVM).
     *
     * @param fileName   basename
     * @param imageBytes dữ liệu ảnh
     * @throws IOException nếu ghi thất bại
     */
    public void writePhotoFile(String fileName, byte[] imageBytes) throws IOException {
        CandidatePhotoFiles.writePhotoFile(fileName, imageBytes);
    }

    /**
     * Tham chiếu lưu DB: {@code candidate-photos/{fileName}}.
     *
     * @param fileName tên file
     * @return tham chiếu logic
     */
    public String toWebPhotoPath(String fileName) {
        return CandidatePhotoFiles.toWebPhotoPath(fileName);
    }

    /**
     * Tra cứu file ảnh để stream theo kỳ thi và SBD.
     *
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return thông tin stream ảnh
     */
    public CandidatePhotoStreamDTO resolvePhoto(int examId, int fallbackExamId, String sbd) {
        CandidatePhotoStreamDTO result = new CandidatePhotoStreamDTO();
        if (queueService == null || sbd == null || sbd.isBlank()) {
            return result;
        }

        ExamRegistrationDTO reg = queueService.findByExam(examId, fallbackExamId, sbd.trim());
        if (reg == null || reg.getPhotoUrl() == null || reg.getPhotoUrl().isBlank()) {
            return result;
        }

        File file = findPhotoFile(reg.getPhotoUrl());
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
