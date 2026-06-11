package Services.Impl;

import DAO.ExamRegistrationDAO;
import DAO.ExamSectionDAO;
import DAO.ExamSessionDAO;
import DAO.LicenseTypeDAO;
import DAO.PersonDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSectionDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import DAO.Impl.LicenseTypeDAOImpl;
import DAO.Impl.PersonDAOImpl;
import Models.ExamSessionOption;
import Models.LicenceClassOption;
import Models.LicenseType;
import Models.Person;
import Models.User;
import Services.RegistrantRegisterExamService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Nghiệp vụ đăng ký đợt thi — <b>không gắn thanh toán SEPay</b>.
 *
 * <p>Thí sinh chỉ tạo {@code ExamRegistration} (isPaymentCompleted=0).
 * SBD đọc từ bảng {@code Candidate} sau khi staff import danh sách Công an.
 * Thu lệ phí do actor khác xử lý (quầy thu ngân, module Payment/SEPay riêng).</p>
 *
 * <p>Module SEPay tái sử dụng: xem {@code Integration.Sepay} package-info.</p>
 */
public class RegistrantRegisterExamServiceImpl implements RegistrantRegisterExamService {

    private static final Logger LOG = Logger.getLogger(RegistrantRegisterExamServiceImpl.class.getName());
    private static final NumberFormat VND_FORMAT = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final LicenseTypeDAO licenseTypeDAO = new LicenseTypeDAOImpl();
    private final ExamSectionDAO examSectionDAO = new ExamSectionDAOImpl();
    private final ExamSessionDAO examSessionDAO = new ExamSessionDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final PersonDAO personDAO = new PersonDAOImpl();

    @Override
    public void populateRegisterPage(HttpServletRequest request, User user, String licenceCode, String sessionCode) {
        List<LicenceClassOption> licenceClassesList = buildLicenceOptions();
        request.setAttribute("licenceClassesList", licenceClassesList);

        String selectedClassCode = pickLicenceCode(licenceCode, licenceClassesList);
        request.setAttribute("selectedClassCode", selectedClassCode);

        List<ExamSessionOption> examSessionsList = selectedClassCode.isEmpty()
                ? List.of()
                : examSessionDAO.findOpenByLicenseCode(selectedClassCode);
        request.setAttribute("examSessionsList", examSessionsList);

        String selectedSessionCode = pickSessionId(sessionCode, examSessionsList);
        request.setAttribute("selectedSessionCode", selectedSessionCode);

        applyFeeSummary(request, selectedClassCode);
        applyRegisterBlock(request, user);
    }

    /**
     * Đăng ký đợt thi — chỉ ghi ExamRegistration, redirect my-exams khi thành công.
     *
     * @return null nếu OK; message lỗi nếu không đăng ký được
     */
    @Override
    public String registerExam(HttpServletRequest request, User user) {
        String blockMessage = resolveRegisterBlock(user);
        if (blockMessage != null) {
            return blockMessage;
        }

        int personId = user.getPersonId();
        String licenceCode = trim(request.getParameter("licenceSelect"));
        String sessionRaw = trim(request.getParameter("sessionSelect"));

        if (licenceCode == null || licenceCode.isEmpty()) {
            return "Vui lòng chọn hạng bằng lái.";
        }
        if (sessionRaw == null || sessionRaw.isEmpty()) {
            return "Vui lòng chọn đợt thi.";
        }

        int sessionId;
        try {
            sessionId = Integer.parseInt(sessionRaw);
        } catch (NumberFormatException ex) {
            return "Đợt thi không hợp lệ.";
        }

        ExamSessionOption session = examSessionDAO.findById(sessionId).orElse(null);
        if (session == null) {
            return "Không tìm thấy đợt thi đã chọn.";
        }
        if (!licenceCode.equalsIgnoreCase(session.getLicenceClass())) {
            return "Đợt thi không khớp với hạng GPLX đã chọn.";
        }
        if (!examSessionDAO.hasAvailableSlot(sessionId)) {
            return "Đợt thi đã hết chỗ hoặc không còn mở đăng ký.";
        }
        if (examRegistrationDAO.existsActiveByPersonAndSession(personId, sessionId)) {
            return "Bạn đã đăng ký đợt thi này rồi.";
        }

        LicenseType licenseType = licenseTypeDAO.findByCode(licenceCode);
        if (licenseType == null) {
            return "Hạng GPLX không hợp lệ.";
        }

        BigDecimal totalFee = examSectionDAO.sumActiveFeesByLicenseTypeId(licenseType.getId());
        if (totalFee == null || totalFee.compareTo(BigDecimal.ZERO) <= 0) {
            return "Không xác định được lệ phí thi.";
        }

        int registrationId = examRegistrationDAO.insertRegistration(sessionId, personId);
        if (registrationId <= 0) {
            LOG.severe("insertRegistration failed personId=" + personId + " sessionId=" + sessionId);
            return "Không thể tạo đăng ký thi. Vui lòng thử lại.";
        }

        LOG.info("Exam registration created id=" + registrationId + " personId=" + personId + " sessionId=" + sessionId);
        return null;
    }

    private void applyRegisterBlock(HttpServletRequest request, User user) {
        String message = resolveRegisterBlock(user);
        if (message != null) {
            request.setAttribute("registerBlocked", true);
            request.setAttribute("registerBlockedMessage", message);
        }
    }

    private String resolveRegisterBlock(User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            return "Vui lòng hoàn thiện hồ sơ cá nhân trước khi đăng ký thi.";
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }

        String approvalStatus = person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending";
        if (!"Approved".equals(approvalStatus)) {
            return "Hồ sơ cá nhân chưa được duyệt. Vui lòng chờ phê duyệt hoặc bổ sung hồ sơ.";
        }

        return null;
    }

    private List<LicenceClassOption> buildLicenceOptions() {
        List<LicenceClassOption> options = new ArrayList<>();
        for (LicenseType type : licenseTypeDAO.findAll()) {
            LicenceClassOption option = new LicenceClassOption();
            String code = type.getLicenseCode();
            option.setCode(code);
            option.setName(describeLicense(code));
            option.setVehicleType(code != null && code.toUpperCase().startsWith("A") ? "moto" : "car");
            option.setExamFee(examSectionDAO.sumActiveFeesByLicenseTypeId(type.getId()));
            options.add(option);
        }
        return options;
    }

    private void applyFeeSummary(HttpServletRequest request, String licenceCode) {
        LicenseType licenseType = licenseTypeDAO.findByCode(licenceCode);
        if (licenseType == null) {
            request.setAttribute("feeBreakdownItems", List.of());
            request.setAttribute("feeTotal", "0đ");
            return;
        }

        var feeLines = examSectionDAO.findFeeLinesByLicenseTypeId(licenseType.getId());
        BigDecimal total = examSectionDAO.sumActiveFeesByLicenseTypeId(licenseType.getId());

        request.setAttribute("feeBreakdownItems", feeLines);
        request.setAttribute("feeTotal", formatVnd(total));
        if (!feeLines.isEmpty()) {
            request.setAttribute("feeSathachName", feeLines.get(0).getLabel());
            request.setAttribute("feeSathachValue", formatVnd(feeLines.get(0).getAmount()));
        }
    }

    private String pickLicenceCode(String licenceCode, List<LicenceClassOption> options) {
        if (licenceCode != null && !licenceCode.isBlank()) {
            for (LicenceClassOption option : options) {
                if (licenceCode.equalsIgnoreCase(option.getCode())) {
                    return option.getCode();
                }
            }
        }
        return options.isEmpty() ? "" : options.get(0).getCode();
    }

    private String pickSessionId(String sessionCode, List<ExamSessionOption> sessions) {
        if (sessionCode != null && !sessionCode.isBlank()) {
            for (ExamSessionOption session : sessions) {
                if (sessionCode.equals(String.valueOf(session.getId()))) {
                    return sessionCode;
                }
            }
        }
        return sessions.isEmpty() ? "" : String.valueOf(sessions.get(0).getId());
    }

    private String describeLicense(String code) {
        return switch (code != null ? code.toUpperCase() : "") {
            case "A1" -> "Mô tô 2 bánh dưới 175cc";
            case "A2" -> "Mô tô 2 bánh trên 175cc";
            case "B1" -> "Ô tô số tự động gia đình";
            case "B", "B2" -> "Ô tô số sàn kinh doanh";
            case "C1" -> "Xe tải hạng trung";
            default -> "Hạng " + code;
        };
    }

    private String formatVnd(BigDecimal amount) {
        return VND_FORMAT.format(amount != null ? amount : BigDecimal.ZERO) + "đ";
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
