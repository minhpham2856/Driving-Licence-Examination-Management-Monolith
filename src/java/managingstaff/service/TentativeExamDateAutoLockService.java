package managingstaff.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.TentativeExamDateDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dao.impl.TentativeExamDateDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.TentativeExamDateDTO;
import managingstaff.service.impl.EmailServiceImpl;
import shared.util.TentativeExamDatePolicy;

/** Khóa các ngày dự kiến đã tới hạn và thông báo cho thí sinh đã chọn ngày đó. */
public class TentativeExamDateAutoLockService {

    private static final Logger LOG = Logger.getLogger(TentativeExamDateAutoLockService.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TentativeExamDateDAO dateDAO = new TentativeExamDateDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    public int lockDueDates() {
        int cancelled = cancelInsufficientDueDates();
        List<Integer> lockedIds = dateDAO.lockDueDates();
        for (Integer dateId : lockedIds) {
            notifyCandidates(dateId);
        }
        return cancelled + lockedIds.size();
    }

    private int cancelInsufficientDueDates() {
        int cancelled = 0;
        String reason = "Không đủ tối thiểu " + TentativeExamDatePolicy.MIN_REGISTRATIONS
                + " thí sinh khi đến hạn khóa trước 07 ngày làm việc.";
        for (TentativeExamDateDTO date : dateDAO.findDeadlineReviewDates()) {
            if (date == null || date.getExamDate() == null
                    || !TentativeExamDatePolicy.shouldBeLocked(
                            date.getExamDate().toLocalDate(), java.time.LocalDate.now())
                    || date.getRegisteredCount() >= TentativeExamDatePolicy.MIN_REGISTRATIONS) {
                continue;
            }
            List<DossierDTO> recipients = new ArrayList<>();
            for (Integer registrationId : dateDAO.findAllRegistrationIds(date.getId())) {
                DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
                if (dossier != null) {
                    recipients.add(dossier);
                }
            }
            try {
                dateDAO.autoCancelInsufficient(date.getId(), reason);
                notifyCancellation(date, recipients, reason);
                cancelled++;
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Không thể tự động hủy ngày dự kiến #{0}: {1}",
                        new Object[]{date.getId(), ex.getMessage()});
            }
        }
        return cancelled;
    }

    private void notifyCandidates(int dateId) {
        if (!emailService.isConfigured()) {
            return;
        }
        TentativeExamDateDTO date = dateDAO.findById(dateId);
        if (date == null || date.getExamDate() == null) {
            return;
        }
        String formattedDate = date.getExamDate().toLocalDate().format(DATE_FORMAT);
        for (Integer registrationId : dateDAO.findAllRegistrationIds(dateId)) {
            DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
            if (dossier == null || dossier.getUser() == null
                    || dossier.getUser().getEmail() == null || dossier.getUser().getEmail().isBlank()) {
                continue;
            }
            String fullName = dossier.getProfile() == null ? "thí sinh" : dossier.getProfile().getFullName();
            String body = "Xin chào " + fullName + ",\n\n"
                    + "Ngày thi dự kiến " + formattedDate + " đã đóng đăng ký trước 07 ngày làm việc "
                    + "để trung tâm hoàn thiện danh sách gửi cơ quan CSGT. "
                    + "Lựa chọn của bạn đã được ghi nhận và không thể thay đổi ở giai đoạn này.\n\n"
                    + "Trung tâm sẽ thông báo khi có lịch thi chính thức.";
            try {
                emailService.sendTextEmail(dossier.getUser().getEmail(),
                        "Ngày thi dự kiến đã đóng đăng ký", body);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Không gửi được email khóa ngày dự kiến #{0}: {1}",
                        new Object[]{dateId, ex.getMessage()});
            }
        }
    }

    private void notifyCancellation(TentativeExamDateDTO date,
            List<DossierDTO> recipients, String reason) {
        if (!emailService.isConfigured() || date == null || date.getExamDate() == null) {
            return;
        }
        String formattedDate = date.getExamDate().toLocalDate().format(DATE_FORMAT);
        for (DossierDTO dossier : recipients) {
            if (dossier == null || dossier.getUser() == null
                    || dossier.getUser().getEmail() == null
                    || dossier.getUser().getEmail().isBlank()) {
                continue;
            }
            String fullName = dossier.getProfile() == null
                    || dossier.getProfile().getFullName() == null
                    || dossier.getProfile().getFullName().isBlank()
                    ? "thí sinh" : dossier.getProfile().getFullName();
            String body = "Xin chào " + fullName + ",\n\n"
                    + "Ngày thi dự kiến " + formattedDate + ", hạng "
                    + date.getLicenceClass() + " đã tự động bị hủy.\n"
                    + "Lý do: " + reason + "\n\n"
                    + "Lựa chọn ngày cũ của bạn đã được hủy. Hồ sơ đã duyệt và tài liệu "
                    + "vẫn được giữ nguyên; vui lòng đăng nhập và chọn một ngày thi dự kiến khác.";
            try {
                emailService.sendTextEmail(dossier.getUser().getEmail(),
                        "Thông báo hủy ngày thi dự kiến", body);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING,
                        "Không gửi được email tự động hủy ngày dự kiến #{0}: {1}",
                        new Object[]{date.getId(), ex.getMessage()});
            }
        }
    }
}
