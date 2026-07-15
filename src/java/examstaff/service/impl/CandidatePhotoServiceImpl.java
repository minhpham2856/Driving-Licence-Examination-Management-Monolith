package examstaff.service.impl;

import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.dto.ExamRegistrationDTO;
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

    /**
     * Chuẩn hóa thông tin ảnh trên cả hàng đợi (đường dẫn + trạng thái còn file).
     *
     * @param webRoot thư mục gốc web
     * @param queue   hàng đợi thí sinh
     */
    @Override
    public void normalizeQueue(String webRoot, List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO reg : queue) {
            resolveCapturedPhoto(webRoot, reg);
        }
    }

    /**
     * Chuẩn hóa đường dẫn ảnh trên hàng đợi về dạng dùng được trên web.
     *
     * @param webRoot thư mục gốc web
     * @param queue   hàng đợi thí sinh
     */
    @Override
    public void normalizePhotoPaths(String webRoot, List<ExamRegistrationDTO> queue) {
        examstaff.util.CandidatePhotoPathUtil.normalizeQueue(webRoot, queue);
    }

    /**
     * Kiểm tra hồ sơ đã có bản ghi ảnh (có URL ảnh) hay chưa.
     *
     * @param reg hồ sơ đăng ký
     * @return true nếu đã có photoUrl
     */
    @Override
    public boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    /**
     * Kiểm tra và gắn trạng thái ảnh đã chụp có file vật lý hay không.
     *
     * @param webRoot thư mục gốc web
     * @param reg     hồ sơ đăng ký
     * @return true nếu tìm thấy file ảnh hợp lệ
     */
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

    /**
     * Kiểm tra file ảnh có tồn tại trên disk theo URL web.
     *
     * @param webRoot  thư mục gốc web
     * @param photoUrl đường dẫn ảnh dạng web
     * @return true nếu file tồn tại
     */
    @Override
    public boolean photoFileExists(String webRoot, String photoUrl) {
        return findPhotoFile(webRoot, photoUrl) != null;
    }

    /**
     * Tìm {@link File} ảnh tương ứng URL web.
     *
     * @param webRoot  thư mục gốc web
     * @param photoUrl đường dẫn ảnh dạng web
     * @return file ảnh, hoặc null nếu không tìm thấy
     */
    @Override
    public File findPhotoFile(String webRoot, String photoUrl) {
        return CandidatePhotoStorageUtil.findPhotoFile(webRoot, photoUrl);
    }

    /**
     * Ghi bytes ảnh ra file dưới web root.
     *
     * @param webRoot    thư mục gốc web
     * @param fileName   tên file đích
     * @param imageBytes dữ liệu ảnh
     * @throws IOException nếu ghi file thất bại
     */
    @Override
    public void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException {
        CandidatePhotoStorageUtil.writePhotoFile(webRoot, fileName, imageBytes);
    }

    /**
     * Đổi tên file ảnh thành đường dẫn web dùng lưu vào hồ sơ.
     *
     * @param fileName tên file
     * @return đường dẫn dạng web
     */
    @Override
    public String toWebPhotoPath(String fileName) {
        return CandidatePhotoStorageUtil.toWebPhotoPath(fileName);
    }

    /**
     * Tra cứu file ảnh để stream theo kỳ thi và SBD.
     *
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return thông tin stream ảnh (status FOUND hoặc mặc định)
     */
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
