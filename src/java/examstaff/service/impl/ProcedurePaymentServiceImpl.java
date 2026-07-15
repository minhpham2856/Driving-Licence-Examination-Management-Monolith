package examstaff.service.impl;

import examstaff.dao.PaymentDAO;
import examstaff.dao.impl.PaymentDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.enums.PaymentMethod;
import examstaff.enums.PaymentStatus;
import shared.model.Payment;
import examstaff.service.ExamRegistrationService;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.ProcedureFeeQueryService;
import examstaff.service.ProcedurePaymentService;

/** Implementation: preview phí và ghi nhận thanh toán tiền mặt thủ tục. */
public class ProcedurePaymentServiceImpl implements ProcedurePaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();
    private final ProcedureFeeQueryService feeQueryService = new ProcedureFeeQueryServiceImpl();

    /**
     * Xem trước phí thủ tục trước khi xác nhận thanh toán.
     *
     * @param candidateId      mã đăng ký thí sinh
     * @param licenseCode      mã hạng bằng
     * @param requiresRoadTest true nếu cần sát hạch đường trường
     * @return kết quả phí dự kiến
     */
    @Override
    public ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest) {
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
    @Override
    public boolean recordProcedureCashPayment(ExamRegistrationDTO profile) {
        if (profile == null) {
            return false;
        }
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

    /** Insert Payment tiền mặt; fallback cập nhật cờ payment trên đăng ký. */
    private boolean recordCashPayment(int candidateId, int enrollmentId, double totalAmount) {
        Payment payment = new Payment();
        payment.setExamEnrollmentId(enrollmentId);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.HOAN_TAT.getDisplayName());
        payment.setPaymentMethod(PaymentMethod.CASH.getCode());
        payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1_000_000);
        if (paymentDAO.insert(payment)) {
            return true;
        }
        return registrationService.updatePayment(candidateId, true);
    }
}
