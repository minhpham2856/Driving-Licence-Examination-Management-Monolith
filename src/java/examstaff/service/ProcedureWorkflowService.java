package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;

import java.sql.Date;
import java.util.List;

/**
 * Điều phối luồng nghiệp vụ bàn thủ tục: hồ sơ, ảnh, thanh toán và reset.
 */
public interface ProcedureWorkflowService {

    /**
     * Tìm hồ sơ thí sinh theo SBD trong kỳ thi / hàng đợi.
     *
     * @param webRoot        thư mục gốc web để chuẩn hóa ảnh nếu cần
     * @param examId         mã kỳ thi ưu tiên
     * @param fallbackExamId mã kỳ thi dự phòng
     * @param sbd            số báo danh
     * @param queue          hàng đợi hiện tại
     * @return hồ sơ tìm được, hoặc null nếu không có
     */
    ExamRegistrationDTO findProfile(String webRoot, int examId, int fallbackExamId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Chuẩn bị hồ sơ để mở bàn thủ tục (làm giàu dữ liệu, kiểm tra điều kiện).
     *
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ thi ưu tiên
     * @param fallbackExamId mã kỳ thi dự phòng
     * @param profile        hồ sơ đang chọn
     * @param queue          hàng đợi hiện tại
     * @return kết quả chuẩn bị hồ sơ cho bàn
     */
    ProcedureProfilePrepareResultDTO prepareProfileForDesk(String webRoot, int examId, int fallbackExamId,
            ExamRegistrationDTO profile, List<ExamRegistrationDTO> queue);

    /**
     * Tải lại hồ sơ sau thao tác, đồng bộ với hàng đợi.
     *
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param candidateId mã đăng ký thí sinh
     * @param sbd         số báo danh
     * @param queue       hàng đợi hiện tại
     * @return hồ sơ đã reload, hoặc null nếu không còn
     */
    ExamRegistrationDTO reloadProfile(String webRoot, int examId, int candidateId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Lưu thông tin hồ sơ cơ bản của thí sinh.
     *
     * @param candidateId mã đăng ký thí sinh
     * @param fullName    họ tên
     * @param dob         ngày sinh
     * @param govIdNo     số CCCD/CMND
     * @param email       email
     * @param phoneNo     số điện thoại
     * @return true nếu lưu thành công
     */
    boolean saveProfile(int candidateId, String fullName, Date dob,
            String govIdNo, String email, String phoneNo);

    /**
     * Chuẩn bị/đánh dấu chụp lại ảnh và trả hồ sơ cập nhật.
     *
     * @param candidateId mã đăng ký thí sinh
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param sbd         số báo danh
     * @param queue       hàng đợi hiện tại
     * @return hồ sơ sau khi chuyển sang trạng thái chụp lại ảnh
     */
    ExamRegistrationDTO recapturePhoto(int candidateId, String webRoot, int examId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Lưu ảnh vừa chụp (base64) vào hồ sơ / hàng đợi.
     *
     * @param webRoot    thư mục gốc web
     * @param sbd        số báo danh
     * @param examId     mã kỳ thi
     * @param base64Data dữ liệu ảnh base64
     * @param queue      hàng đợi hiện tại
     * @return kết quả lưu ảnh
     */
    ProcedurePhotoSaveOutcomeDTO saveCapturedPhoto(String webRoot, String sbd, int examId,
            String base64Data, List<ExamRegistrationDTO> queue);

    /**
     * Xác nhận thanh toán thủ tục và cập nhật trạng thái liên quan.
     *
     * @param profile  hồ sơ thí sinh
     * @param sbd      số báo danh
     * @param examId   mã kỳ thi
     * @param webRoot  thư mục gốc web
     * @param allExams danh sách kỳ thi (ngữ cảnh chọn kỳ)
     * @return kết quả xác nhận thanh toán
     */
    ProcedurePaymentOutcomeDTO confirmPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams);

    /**
     * Reset trạng thái thủ tục của thí sinh về bước đầu phù hợp nghiệp vụ.
     *
     * @param sbd     số báo danh
     * @param examId  mã kỳ thi
     * @param webRoot thư mục gốc web
     * @return kết quả reset
     */
    ProcedureResetOutcomeDTO resetProcedure(String sbd, int examId, String webRoot);
}
