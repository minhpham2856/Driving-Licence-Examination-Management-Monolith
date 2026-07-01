package service.impl;
import dao.CandidateDAO;
import dao.impl.CandidateDAOImpl;
import dto.CandidateEnrollmentDTO;
import service.CandidatePhotoService;
import java.io.File;
import java.util.List;
import model.Candidate;
public class CandidatePhotoServiceImpl implements CandidatePhotoService {
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    @Override
    public void normalizeQueue(String appRoot, List<CandidateEnrollmentDTO> qList) {
        if (qList == null || qList.isEmpty()) {
            return;
        }
        for (CandidateEnrollmentDTO r : qList) {
            String pUrl = r.getCandidate().getPhotoImageUrl();
            if (pUrl != null && !pUrl.isBlank()) {
                File f = new File(appRoot, pUrl.trim());
                if (!f.exists() || !f.isFile()) {
                    Candidate c = candidateDAO.getById(r.getCandidate().getCandidateId());
                    if (c != null) {
                        c.setPhotoImageUrl(null);
                        candidateDAO.update(c);
                        r.getCandidate().setPhotoImageUrl(null);
                    }
                }
            }
        }
    }
}
