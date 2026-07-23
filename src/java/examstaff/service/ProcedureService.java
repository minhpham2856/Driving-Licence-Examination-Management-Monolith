package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ProcedureActionOutcome;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.SePayProcedureCheckoutDTO;
import examstaff.dto.ServiceResult;

import java.sql.Date;
import java.util.List;

/**
 * Facade bàn thủ tục: hồ sơ thí sinh, ảnh, phí, thanh toán và reset trạng thái.
 *
 * Luồng chính trên desk:
 * - <b>Hồ sơ</b> — {@code findProfile}, {@code prepareProfileForDesk},
 *       {@code reloadProfile}, {@code saveProfile}
 * - <b>Ảnh</b> — {@code saveCapturedPhoto}, {@code recapturePhoto}
 * - <b>Phí / thanh toán</b> — {@code resolveProcedureFees}, {@code previewFees},
 *       {@code confirmPayment}, SePay ({@code startSePayCheckout}, {@code finalizeAfterSePayPayment})
 * - <b>Reset</b> — {@code resetProcedure} gỡ ảnh / thanh toán theo SBD
 * Thao tác trả {@link ServiceResult} kèm {@link ProcedureActionOutcome} khi cần cập nhật UI desk.
 */
public interface ProcedureService {

    /**
     * Tìm hồ sơ thí sinh theo SBD (ưu tiên hàng đợi, fallback DB / kỳ dự phòng).
     * @param webRoot        thư mục gốc web (ảnh)
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @param queue          hàng đợi thí sinh (có thể null)
     * @return hồ sơ hoặc {@code null}
     */
    ExamRegistrationDTO findProfile(String webRoot, int examId, int fallbackExamId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Chuẩn bị hồ sơ trên bàn thủ tục (validate, gắn ảnh, trạng thái).
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param profile        hồ sơ hiện tại
     * @param queue          hàng đợi
     * @return {@link ServiceResult} kèm hồ sơ đã chuẩn bị
     */
    ServiceResult<ExamRegistrationDTO> prepareProfileForDesk(String webRoot, int examId, int fallbackExamId,
            ExamRegistrationDTO profile, List<ExamRegistrationDTO> queue);

    /**
     * Tải lại hồ sơ sau khi lưu / thao tác (theo candidateId hoặc SBD).
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param candidateId mã thí sinh
     * @param sbd         số báo danh
     * @param queue       hàng đợi
     * @return hồ sơ mới nhất hoặc {@code null}
     */
    ExamRegistrationDTO reloadProfile(String webRoot, int examId, int candidateId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Lưu thông tin hồ sơ cơ bản (họ tên, ngày sinh, CCCD, liên hệ).
     * @param candidateId mã thí sinh
     * @param fullName    họ và tên
     * @param dob         ngày sinh
     * @param govIdNo     số CMND/CCCD
     * @param email       email
     * @param phoneNo     số điện thoại
     * @return {@link ServiceResult} thành công/thất bại
     */
    ServiceResult<Boolean> saveProfile(int candidateId, String fullName, Date dob,
            String govIdNo, String email, String phoneNo);

    /**
     * Xóa ảnh đã chụp để cho phép chụp lại; trả hồ sơ cập nhật.
     * @param candidateId mã thí sinh
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param sbd         số báo danh
     * @param queue       hàng đợi
     * @return hồ sơ sau khi gỡ ảnh hoặc {@code null}
     */
    ExamRegistrationDTO recapturePhoto(int candidateId, String webRoot, int examId,
            String sbd, List<ExamRegistrationDTO> queue);

    /**
     * Lưu ảnh thủ tục từ dữ liệu base64.
     * @param webRoot    thư mục gốc web
     * @param sbd        số báo danh
     * @param examId     mã kỳ thi
     * @param base64Data ảnh base64
     * @param queue      hàng đợi
     * @return {@link ServiceResult} kèm {@link ProcedureActionOutcome}
     */
    ServiceResult<ProcedureActionOutcome> saveCapturedPhoto(String webRoot, String sbd, int examId,
            String base64Data, List<ExamRegistrationDTO> queue);

    /**
     * Xác nhận thu phí / thanh toán thủ tục cho thí sinh.
     * @param profile  hồ sơ thí sinh
     * @param sbd      số báo danh
     * @param examId   mã kỳ thi
     * @param webRoot  thư mục gốc web
     * @param allExams danh sách kỳ thi (context)
     * @return {@link ServiceResult} kèm {@link ProcedureActionOutcome}
     */
    ServiceResult<ProcedureActionOutcome> confirmPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams);

    /** Tạo checkout SePay (QR) — không ghi Payment. */
    SePayProcedureCheckoutDTO startSePayCheckout(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot);

    /** Sau IPN: present + allocate nếu đã có Payment. */
    ServiceResult<ProcedureActionOutcome> finalizeAfterSePayPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams);

    /** URL IPN để khai báo SePay / hiển thị trên desk. */
    String sePayIpnCallbackUrl();

    boolean isSePayConfigured();

    boolean isSePaySandbox();

    /**
     * Reset trạng thái thủ tục (ảnh / thanh toán) theo SBD.
     * @param sbd     số báo danh
     * @param examId  mã kỳ thi
     * @param webRoot thư mục gốc web
     * @return {@link ServiceResult} kèm {@link ProcedureActionOutcome}
     */
    ServiceResult<ProcedureActionOutcome> resetProcedure(String sbd, int examId, String webRoot);

    /**
     * Tính / lấy bảng phí thủ tục theo hồ sơ đã load.
     * @param profile hồ sơ thí sinh
     * @return kết quả phí
     */
    ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile);

    /**
     * Xem trước phí theo hạng bằng và có cần thi đường hay không.
     * @param candidateId      mã thí sinh
     * @param licenseCode      mã hạng GPLX
     * @param requiresRoadTest có thi đường
     * @return kết quả phí xem trước
     */
    ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest);
}
