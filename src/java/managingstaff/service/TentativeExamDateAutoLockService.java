package managingstaff.service;

import java.time.format.DateTimeFormatter;
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

/** Khóa các ngày dự kiến đã tới hạn và thông báo cho thí sinh đã chọn ngày đó. */
public class TentativeExamDateAutoLockService {

    private static final Logger LOG = Logger.getLogger(TentativeExamDateAutoLockService.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TentativeExamDateDAO dateDAO = new TentativeExamDateDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    public int lockDueDates() {
        List<Integer> lockedIds = dateDAO.lockDueDates();
        for (Integer dateId : lockedIds) {
            notifyCandidates(dateId);
        }
        return lockedIds.size();
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
}
