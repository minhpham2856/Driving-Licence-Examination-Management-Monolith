package examstaff.service.impl.support.procedure;

import examstaff.service.RegistrationService;
import examstaff.service.impl.RegistrationServiceImpl;

import examstaff.dao.PaymentDAO;
import examstaff.dao.impl.PaymentDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.enums.PaymentMethod;
import examstaff.enums.PaymentStatus;
import shared.model.Payment;

/** Implementation: preview phí và ghi nhận thanh toán tiền mặt thủ tục. */
public class ProcedurePaymentServiceImpl {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final ProcedureFeeQueryServiceImpl feeQueryService = new ProcedureFeeQueryServiceImpl();

    /**
     * Xem trước phí thủ tục trước khi xác nhận thanh toán.
     *
     * @param candidateId      mã đăng ký thí sinh
     * @param licenseCode      mã hạng bằng
     * @param requiresRoadTest true nếu cần sát hạch đường trường
     * @return kết quả phí dự kiến
     */
    public ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest) {
        // Load hồ sơ; stub tối thiểu nếu DAO không trả về
        ExamRegistrationDTO profile = registrationService.getById(candidateId);
        if (profile == null) {
            profile = new ExamRegistrationDTO();
            profile.setId(candidateId);
            profile.setLicenseCode(licenseCode);
        }
        return feeQueryService.resolveProcedureFees(profile);
    }

    /**
     * Ghi nhận thanh toán tiền mặt thủ tục cho hồ sơ.
     *
     * @param profile hồ sơ đăng ký thí sinh
     * @return true nếu ghi nhận thành công
     */
    public boolean recordProcedureCashPayment(ExamRegistrationDTO profile) {
        // Validate
        if (profile == null) {
            return false;
        }
        // Load tổng phí (mặc định 200_000 nếu catalog trống)
        ProcedureFeeResultDTO fees = feeQueryService.resolveProcedureFees(profile);
        double total = fees != null ? fees.getFeeTotal() : 0;
        if (total <= 0) {
            total = 200_000;
        }
        int enrollmentId = profile.getExamEnrollmentId();
        if (enrollmentId <= 0) {
            enrollmentId = paymentDAO.resolveEnrollmentId(profile.getId());
        }
        return recordCashPayment(profile.getId(), enrollmentId, total);
    }

    /**
     * Insert Payment tiền mặt; fallback cập nhật cờ payment trên đăng ký.
     *
     * @param candidateId  mã đăng ký
     * @param enrollmentId mã enrollment kỳ thi
     * @param totalAmount  số tiền
     * @return true nếu insert Payment hoặc cập nhật cờ thành công
     */
    private boolean recordCashPayment(int candidateId, int enrollmentId, double totalAmount) {
        // Mutate: dựng Payment CASH hoàn tất
        Payment payment = new Payment();
        payment.setExamEnrollmentId(enrollmentId);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.HOAN_TAT.getDisplayName());
        payment.setPaymentMethod(PaymentMethod.CASH.getCode());
        payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1_000_000);
        if (paymentDAO.insert(payment)) {
            return true;
        }
        // Fallback Result: chỉ cập nhật cờ trên đăng ký
        return registrationService.updatePayment(candidateId, true);
    }
}
