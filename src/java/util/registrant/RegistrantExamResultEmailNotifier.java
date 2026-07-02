package util.registrant;

import service.RegistrantExamResultEmailService;
import service.impl.RegistrantExamResultEmailServiceImpl;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Gọi sau khi lưu điểm — gửi bảng điểm qua Gmail nếu được bật. */
public final class RegistrantExamResultEmailNotifier {

    private static final Logger LOG = Logger.getLogger(RegistrantExamResultEmailNotifier.class.getName());
    private static final RegistrantExamResultEmailService SERVICE = new RegistrantExamResultEmailServiceImpl();

    private RegistrantExamResultEmailNotifier() {
    }

    public static void trySendAfterScoreSaved(int candidateId) {
        if (candidateId <= 0) {
            return;
        }
        try {
            SERVICE.trySendScoreSheet(candidateId, null);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Lỗi gửi email bảng điểm candidate {0}: {1}",
                    new Object[] { candidateId, e.getMessage() });
        }
    }
}
