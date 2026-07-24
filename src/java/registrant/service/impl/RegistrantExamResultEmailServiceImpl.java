package registrant.service.impl;

import auth.dao.ProfileDAO;
import auth.dao.UserDAO;
import auth.dao.impl.ProfileDAOImpl;
import auth.dao.impl.UserDAOImpl;
import auth.dto.UserDTO;
import auth.service.impl.EmailServiceImpl;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.dto.RegistrantExamResultEmailData;
import registrant.dto.RegistrantMyExamRow;
import registrant.service.RegistrantExamResultEmailService;
import registrant.util.RegistrantExamResultEmailFormatter;
import registrant.util.RegistrantExamResultEmailFormatter.FormattedEmail;
import registrant.util.RegistrantProfileSupport;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrantExamResultEmailServiceImpl implements RegistrantExamResultEmailService {

    private static final Logger LOG = Logger.getLogger(RegistrantExamResultEmailServiceImpl.class.getName());

    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final UserDAO userdao = new UserDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final EmailServiceImpl emailService = new EmailServiceImpl();

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

        var stored = userdao.getById(userId);
        UserDTO user = UserDTO.fromUser(stored);
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

    private RegistrantExamResultEmailData toEmailData(UserDTO user, RegistrantMyExamRow exam) {
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

    private String resolveDisplayName(UserDTO user) {
        var profile = user.getProfile();
        if (profile == null) {
            profile = profiledao.getByUserId(user.getUserId());
        }
        return RegistrantProfileSupport.displayName(user, profile);
    }

    private static boolean hasAnyScore(RegistrantMyExamRow exam) {
        return exam.getTheoryScore() != null
                || exam.getPracticalScore() != null
                || exam.getRoadScore() != null;
    }
}
