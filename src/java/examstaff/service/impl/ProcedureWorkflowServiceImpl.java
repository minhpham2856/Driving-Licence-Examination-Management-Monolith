package examstaff.service.impl;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamRegistrationService;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.ExaminerAllocationService;
import examstaff.service.ProcedurePaymentService;
import examstaff.service.ProcedureWorkflowService;
import examstaff.enums.ExamStatus;
import examstaff.util.ExamStaffExamRules;
import examstaff.util.ProcedurePaymentLabels;

import java.sql.Date;
import java.util.List;

/** Implementation: điều phối luồng thủ tục thí sinh (hồ sơ, ảnh, thu phí, reset). */
public class ProcedureWorkflowServiceImpl implements ProcedureWorkflowService {

    private final ExamRegistrationService regService;
    private final ProcedurePaymentService paymentService;
    private final CandidatePhotoService photoService;
    private final CandidateQueueService queueService;
    private final ExaminerAllocationService allocationService;

    /** Wiring mặc định khi không inject từ composition root. */
    public ProcedureWorkflowServiceImpl() {
        this(new ExamRegistrationServiceImpl(), new ProcedurePaymentServiceImpl(),
                new CandidatePhotoServiceImpl(), new CandidateQueueServiceImpl(),
                new ExaminerAllocationServiceImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public ProcedureWorkflowServiceImpl(ExamRegistrationService regService,
            ProcedurePaymentService paymentService,
            CandidatePhotoService photoService,
            CandidateQueueService queueService,
            ExaminerAllocationService allocationService) {
        this.regService = regService;
        this.paymentService = paymentService;
        this.photoService = photoService;
        this.queueService = queueService;
        this.allocationService = allocationService;
    }

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
    @Override
    public ExamRegistrationDTO findProfile(String webRoot, int examId, int fallbackExamId,
            String sbd, List<ExamRegistrationDTO> queue) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        ExamRegistrationDTO profile = queueService.findBySbd(queue, trimmed);
        if (profile == null) {
            profile = queueService.findByExam(examId, fallbackExamId, trimmed);
        }
        if (profile != null) {
            photoService.resolveCapturedPhoto(webRoot, profile);
            syncProfileInQueue(queue, profile);
        }
        return profile;
    }

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
    @Override
    public ProcedureProfilePrepareResultDTO prepareProfileForDesk(String webRoot, int examId, int fallbackExamId,
            ExamRegistrationDTO profile, List<ExamRegistrationDTO> queue) {
        ProcedureProfilePrepareResultDTO result = new ProcedureProfilePrepareResultDTO();
        if (profile == null) {
            return result;
        }

        ExamRegistrationDTO current = profile;
        if (photoService.hasPhotoRecord(current)) {
            photoService.resolveCapturedPhoto(webRoot, current);
            if (!current.isValidCapturedPhoto()) {
                regService.updatePhoto(current.getId(), null);
                current.setPhotoUrl(null);
                current.setValidCapturedPhoto(false);
                result.setPhotoStaleMessage(
                        "Ảnh trong hồ sơ không tìm thấy trên máy chủ - vui lòng chụp lại ảnh chân dung.");
                syncProfileInQueue(queue, current);
            }
        }

        if (!current.isPresent()) {
            boolean updatedPresent = regService.updatePresent(current.getId(), true);
            if (updatedPresent) {
                current = reloadProfile(webRoot, examId, current.getId(), current.getSbd(), queue);
            }
        }

        result.setProfile(current);
        return result;
    }

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
    @Override
    public ExamRegistrationDTO reloadProfile(String webRoot, int examId, int candidateId,
            String sbd, List<ExamRegistrationDTO> queue) {
        ExamRegistrationDTO fresh = regService.getById(candidateId);
        if (fresh == null) {
            if (sbd == null || sbd.isBlank()) {
                return null;
            }
            fresh = regService.getByExamAndSbd(examId, sbd.trim());
            if (fresh == null) {
                return null;
            }
        }
        photoService.resolveCapturedPhoto(webRoot, fresh);
        syncProfileInQueue(queue, fresh);
        return fresh;
    }

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
    @Override
    public boolean saveProfile(int candidateId, String fullName, Date dob,
            String govIdNo, String email, String phoneNo) {
        return regService.updateProfile(candidateId, fullName, dob, govIdNo, email, phoneNo);
    }

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
    @Override
    public ExamRegistrationDTO recapturePhoto(int candidateId, String webRoot, int examId,
            String sbd, List<ExamRegistrationDTO> queue) {
        regService.updatePhoto(candidateId, null);
        return reloadProfile(webRoot, examId, candidateId, sbd, queue);
    }

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
    @Override
    public ProcedurePhotoSaveOutcomeDTO saveCapturedPhoto(String webRoot, String sbd, int examId,
            String base64Data, List<ExamRegistrationDTO> queue) {
        ProcedurePhotoSaveOutcomeDTO outcome = new ProcedurePhotoSaveOutcomeDTO();

        ExamRegistrationDTO profile = findProfile(webRoot, examId, 0, sbd, queue);
        if (profile == null) {
            outcome.setStatus(ProcedurePhotoSaveOutcomeDTO.Status.CANDIDATE_NOT_FOUND);
            outcome.setMessage("Không tìm thấy thí sinh.");
            return outcome;
        }

        String ext = null;
        if (base64Data != null && base64Data.startsWith("data:image/png;base64,")) {
            ext = "png";
        } else if (base64Data != null && base64Data.startsWith("data:image/jpeg;base64,")) {
            ext = "jpg";
        }
        if (ext == null) {
            outcome.setStatus(ProcedurePhotoSaveOutcomeDTO.Status.INVALID_IMAGE);
            outcome.setMessage("Dữ liệu ảnh không hợp lệ.");
            return outcome;
        }

        try {
            String base64Image = base64Data.substring(base64Data.indexOf(',') + 1);
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("Ảnh rỗng");
            }

            String safeSbd = sbd.replaceAll("[^A-Za-z0-9\\-]", "_");
            String fileName = safeSbd + "_captured." + ext;
            photoService.writePhotoFile(webRoot, fileName, imageBytes);

            String photoPath = photoService.toWebPhotoPath(fileName);
            boolean updated = regService.updatePhoto(profile.getId(), photoPath);
            if (!updated) {
                throw new java.io.IOException("Không cập nhật được photoUrl trong DB");
            }

            profile = reloadProfile(webRoot, examId, profile.getId(), sbd, queue);
            if (profile != null) {
                profile.setValidCapturedPhoto(true);
            }

            outcome.setStatus(ProcedurePhotoSaveOutcomeDTO.Status.SUCCESS);
            outcome.setPhotoPath(photoPath);
            outcome.setProfile(profile);
            return outcome;
        } catch (Exception e) {
            outcome.setStatus(ProcedurePhotoSaveOutcomeDTO.Status.ERROR);
            outcome.setMessage(e.getMessage() != null ? e.getMessage() : "Lỗi lưu ảnh");
            return outcome;
        }
    }

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
    @Override
    public ProcedurePaymentOutcomeDTO confirmPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allExams) {
        ProcedurePaymentOutcomeDTO outcome = new ProcedurePaymentOutcomeDTO();

        if (profile == null) {
            outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.PROFILE_NOT_FOUND);
            return outcome;
        }

        profile = reloadProfile(webRoot, examId, profile.getId(), sbd, null);
        if (profile == null) {
            outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.PROFILE_NOT_FOUND);
            return outcome;
        }

        if (!profile.isValidCapturedPhoto()) {
            outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.NO_PHOTO);
            outcome.setProfile(profile);
            return outcome;
        }

        if (profile.isPaymentCompleted()) {
            outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.ALREADY_PAID);
            outcome.setProfile(profile);
            return outcome;
        }

        ProcedureFeeResultDTO feePreview = paymentService.previewFees(
                profile.getId(), profile.getLicenseCode(), false);
        boolean updatedPay = paymentService.recordProcedureCashPayment(profile);
        if (!updatedPay) {
            outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.PAYMENT_FAILED);
            outcome.setProfile(profile);
            return outcome;
        }

        profile.setIsPaymentCompleted(true);
        profile.setIsPresent(true);
        regService.updatePresent(profile.getId(), true);
        if (profile.isAbsent()) {
            clearAbsentAfterPayment(profile);
        }

        int allocExamId = profile.getExamId();
        if (allocExamId <= 0) {
            allocExamId = ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
        }
        AutoAllocateResultDTO allocResult = allocationService.autoAllocateCandidate(
                allocExamId, profile.getId());

        List<ExamRegistrationDTO> qList = regService.getCandidatesByExam(examId);
        photoService.normalizeQueue(webRoot, qList);

        int boardExamId = profile.getExamId() > 0
                ? profile.getExamId()
                : ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);

        String allocDetail = ProcedurePaymentLabels.formatAutoAllocateDetail(allocResult);
        String feeLabel = ProcedurePaymentLabels.formatFeeAmount(feePreview);

        outcome.setStatus(ProcedurePaymentOutcomeDTO.Status.SUCCESS);
        outcome.setProfile(profile);
        outcome.setQueue(qList);
        outcome.setBoardExamId(boardExamId);
        outcome.setPaymentAuditDetail("Thu lệ phí thi " + feeLabel + allocDetail + " cho SBD " + sbd);
        outcome.setAuditAllocate(allocResult != null && allocResult.allocatedCount > 0);
        return outcome;
    }

    /**
     * Reset trạng thái thủ tục của thí sinh về bước đầu phù hợp nghiệp vụ.
     *
     * @param sbd     số báo danh
     * @param examId  mã kỳ thi
     * @param webRoot thư mục gốc web
     * @return kết quả reset
     */
    @Override
    public ProcedureResetOutcomeDTO resetProcedure(String sbd, int examId, String webRoot) {
        ProcedureResetOutcomeDTO outcome = new ProcedureResetOutcomeDTO();
        if (sbd == null || sbd.isBlank()) {
            return outcome;
        }

        ExamSummaryDTO exam = allocationService.getExamById(examId);
        if (exam != null && ExamStatus.isLockedForStaffMutation(exam.getStatus())) {
            return outcome;
        }

        String trimmed = sbd.trim();
        ExamRegistrationDTO target = regService.getByExamAndSbd(examId, trimmed);
        if (target == null) {
            return outcome;
        }

        regService.updatePhoto(target.getId(), null);
        regService.clearCompletedPayments(target.getId());

        List<ExamRegistrationDTO> qList = regService.getCandidatesByExam(examId);
        photoService.normalizeQueue(webRoot, qList);

        outcome.setSuccess(true);
        outcome.setSbd(trimmed);
        outcome.setCandidateId(target.getId());
        outcome.setQueue(qList);
        return outcome;
    }

    /** Xóa đánh dấu vắng sau khi thu phí thành công. */
    private void clearAbsentAfterPayment(ExamRegistrationDTO profile) {
        regService.clearAbsentMarking(profile.getId());
        profile.setAbsent(false);
        profile.setTheoryPassed("none");
        profile.setPracticalPassed("none");
        profile.setTheoryScore(null);
        profile.setPracticalScore(null);
    }

    /** Đồng bộ profile đã refresh vào list hàng đợi. */
    private static void syncProfileInQueue(List<ExamRegistrationDTO> qList, ExamRegistrationDTO refreshed) {
        if (qList == null || refreshed == null) {
            return;
        }
        for (int i = 0; i < qList.size(); i++) {
            if (qList.get(i).getId() == refreshed.getId()) {
                qList.set(i, refreshed);
                return;
            }
        }
    }
}
