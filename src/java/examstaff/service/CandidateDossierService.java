package examstaff.service;

import examstaff.dto.CandidateDossierViewDTO;

/**
 * Nạp hồ sơ chi tiết (dossier) của thí sinh cho nhân viên xem/xử lý.
 */
public interface CandidateDossierService {

    /**
     * Tải view hồ sơ thí sinh theo kỳ thi và SBD.
     *
     * @param examId  mã kỳ thi
     * @param sbd     số báo danh
     * @param webRoot thư mục gốc web (ảnh, tài liệu)
     * @return DTO hồ sơ hiển thị, hoặc null nếu không có
     */
    CandidateDossierViewDTO loadDossier(int examId, String sbd, String webRoot);
}
