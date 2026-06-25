package Services.Impl;

import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.UserDAO;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.RegistrantExamResultEmailData;
import Models.RegistrantMyExamRow;
import Models.User;
import Services.EmailService;
import Services.RegistrantExamResultEmailService;
import Utils.RegistrantExamResultEmailFormatter;
import Utils.RegistrantExamResultEmailFormatter.FormattedEmail;
import Utils.RegistrantProfileSupport;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrantExamResultEmailServiceImpl implements RegistrantExamResultEmailService {

    private static final Logger LOG = Logger.getLogger(RegistrantExamResultEmailServiceImpl.class.getName());

    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public boolean trySendScoreSheet(int candidateId, HttpSession session) {
        if (candidateId <= 0 || !emailService.isConfigured()) {
            return false;
        }
        if (!RegistrantSettingsServiceImpl.isNotifyExamResults(session)) {
            return false;
        }

        Integer userId = registrantDAO.resolveUserIdByCandidateId(candidateId);
        if (userId == null || userId <= 0) {
            return false;
        }

        RegistrantMyExamRow exam = registrantDAO.findMyExamByCandidateId(userId, candidateId);
        if (exam == null || !hasAnyScore(exam)) {
            return false;
        }

        User user = userDAO.getById(userId);
        if (user == null || RegistrantProfileSupport.isBlank(user.getEmail())) {
            return false;
        }

        RegistrantExamResultEmailData data = toEmailData(user, exam);
        FormattedEmail formatted = RegistrantExamResultEmailFormatter.format(data);
        if (formatted == null) {
            return false;
        }

        boolean sent = emailService.sendHtmlEmail(
                user.getEmail().trim(), formatted.subject(), formatted.htmlBody());
        if (!sent) {
            LOG.log(Level.WARNING, "Không gửi được email bảng điểm candidate {0}", candidateId);
        }
        return sent;
    }

    private RegistrantExamResultEmailData toEmailData(User user, RegistrantMyExamRow exam) {
        RegistrantExamResultEmailData data = new RegistrantExamResultEmailData();
        data.setRecipientEmail(user.getEmail());
        data.setRecipientName(resolveDisplayName(user));
        data.setExamTitle(exam.getExamTitle());
        data.setLicenceClass(exam.getLicenceClass());
        data.setSbdDisplay(exam.getSbdDisplay());
        data.setExamDate(exam.getExamDate());
        data.setExamSectionName(exam.getExamSectionName());
        data.setTheoryScore(exam.getTheoryScore());
        data.setTheoryResultLabel(exam.getTheoryResultLabel());
        data.setPracticalScore(exam.getPracticalScore());
        data.setPracticalResultLabel(exam.getPracticalResultLabel());
        data.setRoadScore(exam.getRoadScore());
        data.setOverallResultLabel(exam.getOverallResultLabel());
        return data;
    }

    private String resolveDisplayName(User user) {
        var profile = user.getProfile();
        if (profile == null && user.getProfileId() != null && user.getProfileId() > 0) {
            profile = profileDAO.getByUserId(user.getId());
        }
        return RegistrantProfileSupport.displayName(user, profile);
    }

    private static boolean hasAnyScore(RegistrantMyExamRow exam) {
        return exam.getTheoryScore() != null
                || exam.getPracticalScore() != null
                || exam.getRoadScore() != null;
    }
}
