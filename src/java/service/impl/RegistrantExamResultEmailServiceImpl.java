package service.impl;

import dao.ProfileDAO;
import dao.RegistrantDAO;
import dao.UserDAO;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantDAOImpl;
import dao.impl.UserDAOImpl;
import dto.registrant.RegistrantExamResultEmailData;
import dto.registrant.RegistrantMyExamRow;
import model.user.User;
import service.EmailService;
import service.RegistrantExamResultEmailService;
import util.registrant.RegistrantExamResultEmailFormatter;
import util.registrant.RegistrantExamResultEmailFormatter.FormattedEmail;
import util.registrant.RegistrantProfileSupport;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrantExamResultEmailServiceImpl implements RegistrantExamResultEmailService {

    private static final Logger LOG = Logger.getLogger(RegistrantExamResultEmailServiceImpl.class.getName());

    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final UserDAO userdao = new UserDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public boolean trySendScoreSheet(int candidateId, HttpSession session) {
        if (candidateId <= 0 || !emailService.isConfigured()) {
            return false;
        }
        if (!RegistrantSettingsServiceImpl.isNotifyExamResults(session)) {
            return false;
        }

        Integer userId = registrantdao.resolveUserIdByCandidateId(candidateId);
        if (userId == null || userId <= 0) {
            return false;
        }

        RegistrantMyExamRow exam = registrantdao.findMyExamByCandidateId(userId, candidateId);
        if (exam == null || !hasAnyScore(exam)) {
            return false;
        }

        User user = userdao.getById(userId);
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
            profile = profiledao.getByUserId(user.getId());
        }
        return RegistrantProfileSupport.displayName(user, profile);
    }

    private static boolean hasAnyScore(RegistrantMyExamRow exam) {
        return exam.getTheoryScore() != null
                || exam.getPracticalScore() != null
                || exam.getRoadScore() != null;
    }
}
