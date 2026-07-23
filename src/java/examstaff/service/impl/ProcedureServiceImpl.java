package examstaff.service.impl;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ProcedureActionOutcome;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.SePayProcedureCheckoutDTO;
import examstaff.dto.ServiceResult;
import examstaff.service.ProcedureService;
import examstaff.service.impl.support.view.CandidatePhotoServiceImpl;
import examstaff.service.impl.support.call.CandidateQueueServiceImpl;
import examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl;
import examstaff.service.impl.support.procedure.ProcedureFeeQueryServiceImpl;
import examstaff.service.impl.support.procedure.ProcedurePaymentServiceImpl;
import examstaff.service.impl.support.procedure.ProcedureWorkflowServiceImpl;
import payment.service.SePayPaymentService;
import payment.service.impl.SePayPaymentServiceImpl;
import shared.enums.ErrorType;

import java.sql.Date;
import java.util.List;

/**
 * Implementation {@link ProcedureService}: facade bàn thủ tục thí sinh kỳ thi.
 *
 * Ủy quyền support services:
 * - {@link ProcedureWorkflowServiceImpl} — hồ sơ, ảnh, present, reset
 *       (inject {@link RegistrationServiceImpl}, {@link CandidatePhotoServiceImpl},
 *       {@link CandidateQueueServiceImpl}, {@link ExaminerAllocationServiceImpl})
 * - {@link ProcedureFeeQueryServiceImpl} — {@code resolveProcedureFees}, {@code previewFees}
 * - {@link ProcedurePaymentServiceImpl} — {@code confirmPayment}, tích hợp SePay
 *       qua {@link SePayPaymentService}
 * Constructor mặc định tự wiring toàn bộ dependency; constructor inject dùng cho test.
 */
public class ProcedureServiceImpl implements ProcedureService {

    private final ProcedureWorkflowServiceImpl workflow;
    private final ProcedureFeeQueryServiceImpl feeQuery;
    private final ProcedurePaymentServiceImpl payment;
    private final SePayPaymentService sePayPaymentService;

    /** Wiring mặc định (composition root). */
    public ProcedureServiceImpl() {
        this.payment = new ProcedurePaymentServiceImpl();
        this.feeQuery = new ProcedureFeeQueryServiceImpl();
        this.sePayPaymentService = new SePayPaymentServiceImpl();
        CandidateQueueServiceImpl queue = new CandidateQueueServiceImpl();
        this.workflow = new ProcedureWorkflowServiceImpl(
                new RegistrationServiceImpl(),
                this.payment,
                new CandidatePhotoServiceImpl(queue),
                queue,
                new ExaminerAllocationServiceImpl());
    }

    /**
     * Inject dependencies (test / composition).
     */
    public ProcedureServiceImpl(ProcedureWorkflowServiceImpl workflow,
            ProcedureFeeQueryServiceImpl feeQuery,
            ProcedurePaymentServiceImpl payment) {
        this.workflow = workflow;
        this.feeQuery = feeQuery;
        this.payment = payment;
        this.sePayPaymentService = new SePayPaymentServiceImpl();
    }

    /**
     * Ủy quyền sang {@link ProcedureWorkflowServiceImpl#findProfile}.
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @param queue          hàng đợi
     * @return hồ sơ hoặc {@code null}
     */
    @Override
    public ExamRegistrationDTO findProfile(String webRoot, int examId, int fallbackExamId,
            String sbd, List<ExamRegistrationDTO> queue) {
        return workflow.findProfile(webRoot, examId, fallbackExamId, sbd, queue);
    }

    /**
     * Ủy quyền sang {@link ProcedureWorkflowServiceImpl#prepareProfileForDesk}.
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param profile        hồ sơ hiện tại
     * @param queue          hàng đợi
     * @return {@link ServiceResult} kèm hồ sơ
     */
    @Override
    public ServiceResult<ExamRegistrationDTO> prepareProfileForDesk(String webRoot, int examId, int fallbackExamId,
            ExamRegistrationDTO profile, List<ExamRegistrationDTO> queue) {
        return workflow.prepareProfileForDesk(webRoot, examId, fallbackExamId, profile, queue);
    }

    /**
     * Ủy quyền sang {@link ProcedureWorkflowServiceImpl#reloadProfile}.
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param candidateId mã thí sinh
     * @param sbd         số báo danh
     * @param queue       hàng đợi
     * @return hồ sơ mới nhất hoặc {@code null}
     */
    @Override
    public ExamRegistrationDTO reloadProfile(String webRoot, int examId, int candidateId,
            String sbd, List<ExamRegistrationDTO> queue) {
        return workflow.reloadProfile(webRoot, examId, candidateId, sbd, queue);
    }

    /**
     * Lưu hồ sơ rồi bọc kết quả boolean thành {@link ServiceResult}.
     * @param candidateId mã thí sinh
     * @param fullName    họ và tên
     * @param dob         ngày sinh
     * @param govIdNo     CCCD/CMND
     * @param email       email
     * @param phoneNo     số điện thoại
     * @return thành công hoặc lỗi validation
     */
    @Override
    public ServiceResult<Boolean> saveProfile(int candidateId, String fullName, Date dob,
            String govIdNo, String email, String phoneNo) {
        // Mutate
        boolean ok = workflow.saveProfile(candidateId, fullName, dob, govIdNo, email, phoneNo);
        // Result
        if (ok) {
            return ServiceResult.ok(Boolean.TRUE, "Đã lưu hồ sơ.");
        }
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không lưu được hồ sơ.");
    }

    /**
     * Ủy quyền sang {@link ProcedureWorkflowServiceImpl#recapturePhoto}.
     * @param candidateId mã thí sinh
     * @param webRoot     thư mục gốc web
     * @param examId      mã kỳ thi
     * @param sbd         số báo danh
     * @param queue       hàng đợi
     * @return hồ sơ sau khi gỡ ảnh
     */
    @Override
    public ExamRegistrationDTO recapturePhoto(int candidateId, String webRoot, int examId,
            String sbd, List<ExamRegistrationDTO> queue) {
        return workflow.recapturePhoto(candidateId, webRoot, examId, sbd, queue);
    }

    /**
     * Lưu ảnh thủ tục rồi map {@link ProcedureActionOutcome} → {@link ServiceResult}.
     * @param webRoot    thư mục gốc web
     * @param sbd        số báo danh
     * @param examId     mã kỳ thi
     * @param base64Data ảnh base64
     * @param queue      hàng đợi
     * @return kết quả thành công/thất bại
     */
    @Override
    public ServiceResult<ProcedureActionOutcome> saveCapturedPhoto(String webRoot, String sbd, int examId,
            String base64Data, List<ExamRegistrationDTO> queue) {
        // Mutate
        ProcedureActionOutcome data = workflow.saveCapturedPhoto(webRoot, sbd, examId, base64Data, queue);
        // Result
        if (data != null && data.getPhotoStatus() == ProcedureActionOutcome.PhotoStatus.SUCCESS) {
            return ServiceResult.ok(data, data.getMessage());
        }
        String message = data != null && data.getMessage() != null ? data.getMessage() : "Lưu ảnh thất bại.";
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message, data);
    }

    /**
     * Xác nhận thanh toán rồi map trạng thái outcome → {@link ServiceResult}.
     * @param profile  hồ sơ thí sinh
     * @param sbd      số báo danh
     * @param examId   mã kỳ thi
     * @param webRoot  thư mục gốc web
     * @param allExams danh sách kỳ
     * @return kết quả thanh toán
     */
    @Override
    public ServiceResult<ProcedureActionOutcome> confirmPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams) {
        // Mutate
        ProcedureActionOutcome data = workflow.confirmPayment(profile, sbd, examId, webRoot, allExams);
        // Result
        if (data != null && (data.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.SUCCESS
                || data.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.ALREADY_PAID)) {
            return ServiceResult.ok(data);
        }
        String message = "Thanh toán thất bại.";
        if (data != null) {
            message = switch (data.getPaymentStatus()) {
                case NO_PHOTO -> "Thiếu ảnh thủ tục — không thu phí.";
                case PROFILE_NOT_FOUND -> "Không tìm thấy hồ sơ thí sinh.";
                case PAYMENT_FAILED -> "Không ghi được thanh toán. Vui lòng thử lại.";
                default -> message;
            };
        }
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message, data);
    }

    @Override
    public SePayProcedureCheckoutDTO startSePayCheckout(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot) {
        return workflow.startSePayCheckout(profile, sbd, examId, webRoot);
    }

    @Override
    public ServiceResult<ProcedureActionOutcome> finalizeAfterSePayPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams) {
        ProcedureActionOutcome data = workflow.finalizeAfterSePayPayment(profile, sbd, examId, webRoot, allExams);
        if (data != null && (data.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.SUCCESS
                || data.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.ALREADY_PAID)) {
            return ServiceResult.ok(data);
        }
        String message = data != null && data.getMessage() != null
                ? data.getMessage() : "Đang chờ xác nhận SePay.";
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message, data);
    }

    @Override
    public String sePayIpnCallbackUrl() {
        return sePayPaymentService.ipnCallbackUrl();
    }

    @Override
    public boolean isSePayConfigured() {
        return sePayPaymentService.isConfigured();
    }

    @Override
    public boolean isSePaySandbox() {
        return sePayPaymentService.sandbox();
    }

    /**
     * Reset thủ tục rồi map outcome → {@link ServiceResult}.
     * @param sbd     số báo danh
     * @param examId  mã kỳ thi
     * @param webRoot thư mục gốc web
     * @return kết quả reset
     */
    @Override
    public ServiceResult<ProcedureActionOutcome> resetProcedure(String sbd, int examId, String webRoot) {
        // Mutate
        ProcedureActionOutcome data = workflow.resetProcedure(sbd, examId, webRoot);
        // Result
        String message = data != null && data.isSuccess() ? null : "Reset thủ tục thất bại.";
        if (data != null && data.isSuccess()) {
            return ServiceResult.ok(data);
        }
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message != null ? message : "Reset thủ tục thất bại.", data);
    }

    /**
     * Ủy quyền sang {@link ProcedureFeeQueryServiceImpl#resolveProcedureFees}.
     * @param profile hồ sơ thí sinh
     * @return kết quả phí
     */
    @Override
    public ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile) {
        return feeQuery.resolveProcedureFees(profile);
    }

    /**
     * Ủy quyền sang {@link ProcedurePaymentServiceImpl#previewFees}.
     * @param candidateId      mã thí sinh
     * @param licenseCode      mã hạng
     * @param requiresRoadTest có thi đường
     * @return phí xem trước
     */
    @Override
    public ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest) {
        return payment.previewFees(candidateId, licenseCode, requiresRoadTest);
    }
}
