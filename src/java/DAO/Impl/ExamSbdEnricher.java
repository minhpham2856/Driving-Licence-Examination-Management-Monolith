package DAO.Impl;

import DAO.CandidateDAO;
import Models.MyExamDetailView;
import Models.MyExamRowView;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gắn SBD (số báo danh) từ bảng {@code Candidate} vào view lịch thi.
 * <p>Mọi thông tin lịch thi khác (ngày, ca, hạng, phòng, trạng thái…) lấy từ
 * {@code ExamRegistration} + join — không đọc từ {@code Candidate}.</p>
 */
final class ExamSbdEnricher {

    private final CandidateDAO candidateDAO;
    private Boolean candidateTableAvailable;

    ExamSbdEnricher(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO;
    }

    void enrichRows(List<MyExamRowView> rows, int personId) {
        if (!isCandidateTableAvailable() || rows == null || rows.isEmpty()) {
            return;
        }
        Map<Integer, String> sbdByRegistration = candidateDAO.findSbdMapByRegistrationForPerson(personId);
        Map<Integer, String> sbdBySession = candidateDAO.findSbdMapBySessionForPerson(personId);

        for (MyExamRowView row : rows) {
            String sbd = sbdByRegistration.get(row.getRegistrationId());
            if (sbd == null) {
                sbd = sbdBySession.get(row.getExamSessionId());
            }
            if (sbd != null) {
                row.setSbd(sbd);
            }
        }
    }

    void enrichDetail(MyExamDetailView detail, int registrationId, int personId, int examSessionId) {
        if (!isCandidateTableAvailable() || detail == null) {
            return;
        }
        resolveSbd(registrationId, personId, examSessionId).ifPresent(detail::setSbd);
    }

    private Optional<String> resolveSbd(int registrationId, int personId, int examSessionId) {
        Optional<String> byRegistration = candidateDAO.findSbdByRegistrationId(registrationId);
        if (byRegistration.isPresent()) {
            return byRegistration;
        }
        return candidateDAO.findSbdByPersonAndSession(personId, examSessionId);
    }

    private boolean isCandidateTableAvailable() {
        if (candidateTableAvailable != null) {
            return candidateTableAvailable;
        }
        candidateTableAvailable = candidateDAO.isTableAvailable();
        return candidateTableAvailable;
    }
}
