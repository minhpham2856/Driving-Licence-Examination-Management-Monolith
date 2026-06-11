package Services.Impl;

import DAO.ExamRegistrationDAO;
import DAO.PersonDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.PersonDAOImpl;
import Models.Person;
import Models.ProfileTrackingLog;
import Models.ProfileTrackingStep;
import Models.User;
import Services.RegistrantTrackProfileService;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Timeline 5 bước tiến trình hồ sơ + log hoạt động cho trang track-profile. */
public class RegistrantTrackProfileServiceImpl implements RegistrantTrackProfileService {

    private static final SimpleDateFormat STEP_TIME_FORMAT = new SimpleDateFormat("HH:mm — dd/MM/yyyy");

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();

    /**
     * Tính bước active (1–5) từ approvalStatus, số giấy tờ, đăng ký thi, kết quả;
     * build timeline steps và log hoạt động giả lập từ Person/ExamRegistration.
     */
    @Override
    public void populateTrackProfile(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            setEmpty(request);
            return;
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            setEmpty(request);
            return;
        }

        int documentCount = examRegistrationDAO.countDocumentsByPersonId(personId);
        int registrationCount = examRegistrationDAO.countByPersonId(personId);
        int resultCount = examRegistrationDAO.countResultsByPersonId(personId);
        String approvalStatus = person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending";

        int activeStep = resolveActiveStep(approvalStatus, documentCount, registrationCount, resultCount);
        request.setAttribute("trackingSteps",
                buildSteps(person, approvalStatus, documentCount, registrationCount, resultCount, activeStep));
        request.setAttribute("timelineFillStep", activeStep);
        request.setAttribute("profileTrackingLogs",
                buildLogs(person, approvalStatus, documentCount, registrationCount));
        request.setAttribute("showSupplementAlert", "Rejected".equals(approvalStatus));
        if ("Rejected".equals(approvalStatus) && person.getRejectionReason() != null) {
            request.setAttribute("supplementAlertMessage", person.getRejectionReason());
        }
    }

    private void setEmpty(HttpServletRequest request) {
        request.setAttribute("trackingSteps", List.of());
        request.setAttribute("profileTrackingLogs", List.of());
        request.setAttribute("timelineFillStep", 1);
        request.setAttribute("showSupplementAlert", false);
    }

    /**
     * Xác định bước đang thực hiện: 1=đăng ký TK, 2=nộp HS, 3=duyệt, 4=lịch thi, 5=chứng chỉ.
     */
    private int resolveActiveStep(String approvalStatus, int documentCount, int registrationCount, int resultCount) {
        if (resultCount > 0) {
            return 5;
        }
        if (registrationCount > 0 || "Approved".equals(approvalStatus)) {
            return 4;
        }
        if (documentCount > 0) {
            return 3;
        }
        return 1;
    }

    private List<ProfileTrackingStep> buildSteps(Person person, String approvalStatus,
            int documentCount, int registrationCount, int resultCount, int activeStep) {
        List<ProfileTrackingStep> steps = new ArrayList<>();
        steps.add(step(1, "Đăng ký thành công", formatStepTime(person.getCreatedAt()), stateFor(1, activeStep)));
        steps.add(step(2, "Xác minh tài liệu",
                documentCount > 0 ? "Đã nộp " + documentCount + " tài liệu" : "Chưa có tài liệu",
                stateFor(2, activeStep)));
        steps.add(step(3, "Duyệt hồ sơ gốc", approvalDescription(approvalStatus), stateFor(3, activeStep)));
        steps.add(step(4, "Lập lịch dự thi",
                registrationCount > 0 ? "Đã đăng ký " + registrationCount + " đợt thi" : "Chưa đăng ký đợt thi",
                stateFor(4, activeStep)));
        steps.add(step(5, "Cấp chứng chỉ",
                resultCount > 0 ? "Đã có kết quả thi" : "Chờ kết quả",
                stateFor(5, activeStep)));
        return steps;
    }

    private String approvalDescription(String approvalStatus) {
        return switch (approvalStatus) {
            case "Approved" -> "Hồ sơ đã được duyệt";
            case "Rejected" -> "Yêu cầu bổ sung";
            default -> "Đang chờ duyệt";
        };
    }

    private ProfileTrackingStep step(int number, String title, String description, String state) {
        ProfileTrackingStep step = new ProfileTrackingStep();
        step.setStepNumber(number);
        step.setTitle(title);
        step.setDescription(description);
        step.setState(state);
        return step;
    }

    private String stateFor(int stepNumber, int activeStep) {
        if (stepNumber < activeStep) {
            return "completed";
        }
        if (stepNumber == activeStep) {
            return "active";
        }
        return "pending";
    }

    private List<ProfileTrackingLog> buildLogs(Person person, String approvalStatus,
            int documentCount, int registrationCount) {
        List<ProfileTrackingLog> logs = new ArrayList<>();

        if (person.getCreatedAt() != null) {
            logs.add(entry("Tải lên hồ sơ gốc", person.getCreatedAt(), "Thí sinh",
                    "Thành công", "approved", "Thí sinh gửi hồ sơ đăng ký dự thi trực tuyến."));
        }

        if (documentCount > 0 && person.getUpdatedAt() != null) {
            logs.add(entry("Nộp tài liệu đính kèm", person.getUpdatedAt(), "Thí sinh",
                    "Thành công", "approved", "Đã nộp " + documentCount + " tài liệu trên hệ thống."));
        }

        Timestamp reviewTime = person.getUpdatedAt() != null ? person.getUpdatedAt() : person.getCreatedAt();
        if ("Rejected".equals(approvalStatus)) {
            logs.add(entry("Kiểm duyệt hồ sơ", reviewTime, "Cán bộ hồ sơ", "Từ chối", "rejected",
                    person.getRejectionReason() != null ? person.getRejectionReason() : "Hồ sơ cần bổ sung."));
        } else if ("Approved".equals(approvalStatus)) {
            logs.add(entry("Kiểm duyệt hồ sơ", reviewTime, "Cán bộ hồ sơ", "Đã duyệt", "approved",
                    "Hồ sơ đủ điều kiện đăng ký thi."));
        } else if (reviewTime != null) {
            logs.add(entry("Kiểm duyệt hồ sơ", reviewTime, "Cán bộ hồ sơ", "Chờ duyệt", "pending",
                    "Hồ sơ đang trong hàng đợi xét duyệt."));
        }

        if (registrationCount > 0) {
            logs.add(entry("Đăng ký đợt thi", reviewTime, "Thí sinh",
                    "Thành công", "approved", "Đã ghi nhận đăng ký đợt thi trên hệ thống."));
        }

        logs.sort(Comparator.comparing(ProfileTrackingLog::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return logs;
    }

    private ProfileTrackingLog entry(String title, Timestamp ts, String actor, String statusLabel,
            String statusClass, String remarks) {
        ProfileTrackingLog log = new ProfileTrackingLog();
        log.setEventTitle(title);
        log.setTimestamp(ts);
        log.setActorRole(actor);
        log.setStatusLabel(statusLabel);
        log.setStatusClass(statusClass);
        log.setRemarks(remarks);
        return log;
    }

    private String formatStepTime(Timestamp ts) {
        return ts == null ? "—" : STEP_TIME_FORMAT.format(ts);
    }
}
