package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;

/**
 * View-model hồ sơ thí sinh (dossier) trên màn ExamStaff.
 *
 * Vai trò:
 * Gói profile ExamRegistrationDTO, tóm tắt kỳ, lệ phí và tiêu đề hiển thị cho trang dossier.
 *
 * Ai tạo / tiêu thụ:
 * CandidateDossierServiceImpl → CandidateDossierServlet → candidate-dossier.jsp.
 */
public class CandidateDossierViewDTO {

    private ExamRegistrationDTO profile;
    private ExamSummaryDTO examSummary;
    private ProcedureFeeResultDTO fees;
    private boolean hasPhotoFile;
    private String dossierTitle;
    private String dossierSubtitle;

    /** Hồ sơ đăng ký / thí sinh đang xem dossier. */
    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    /** Gán hồ sơ thí sinh cho dossier. */
    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    /** Tóm tắt kỳ thi của enrollment (getter tên exam cho JSP). */
    public ExamSummaryDTO getExam() {
        return examSummary;
    }

    /** Gán tóm tắt kỳ thi. */
    public void setExam(ExamSummaryDTO examSummary) {
        this.examSummary = examSummary;
    }

    /** Chi tiết dòng lệ phí / tổng tiền hiển thị trên dossier. */
    public ProcedureFeeResultDTO getFees() {
        return fees;
    }

    /** Gán kết quả lệ phí. */
    public void setFees(ProcedureFeeResultDTO fees) {
        this.fees = fees;
    }

    /** true nếu có file ảnh vật lý để hiển thị / link. */
    public boolean isHasPhotoFile() {
        return hasPhotoFile;
    }

    /** Gán cờ tồn tại file ảnh. */
    public void setHasPhotoFile(boolean hasPhotoFile) {
        this.hasPhotoFile = hasPhotoFile;
    }

    /** Tiêu đề chính trang dossier (thường họ tên + SBD). */
    public String getDossierTitle() {
        return dossierTitle;
    }

    /** Gán tiêu đề dossier. */
    public void setDossierTitle(String dossierTitle) {
        this.dossierTitle = dossierTitle;
    }

    /** Phụ đề (kỳ thi / hạng bằng…). */
    public String getDossierSubtitle() {
        return dossierSubtitle;
    }

    /** Gán phụ đề dossier. */
    public void setDossierSubtitle(String dossierSubtitle) {
        this.dossierSubtitle = dossierSubtitle;
    }
}
