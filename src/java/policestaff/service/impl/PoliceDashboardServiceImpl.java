package policestaff.service.impl;

import java.util.List;
import policestaff.dao.PoliceSubmissionDAO;
import policestaff.dao.impl.PoliceSubmissionDAOImpl;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.dto.PoliceCandidateDTO;
import policestaff.service.PoliceDashboardService;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.service.EmailService;
import managingstaff.service.impl.EmailServiceImpl;
import java.util.ArrayList;
import policestaff.dto.OfficialExamCandidateDTO;
import policestaff.dto.OfficialRosterPublishResult;
import java.text.SimpleDateFormat;

public class PoliceDashboardServiceImpl implements PoliceDashboardService {
    private final PoliceSubmissionDAO submissionDAO = new PoliceSubmissionDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public List<PoliceSubmissionDTO> loadSubmissions(int limit) {
        return submissionDAO.findRecentSubmissions(limit);
    }
    @Override public List<PoliceSubmissionDTO> loadSubmissions(String status, Integer year, int page, int size) {
        return submissionDAO.findSubmissions(status, year, (Math.max(1, page) - 1) * size, size);
    }
    @Override public int countSubmissions(String status, Integer year) {
        return submissionDAO.countSubmissions(status, year);
    }
    @Override public int countPendingCandidates() { return submissionDAO.countPendingCandidates(); }
    @Override public List<Integer> loadCompletedYears() { return submissionDAO.findCompletedYears(); }

    @Override public PoliceSubmissionDTO findSubmission(int id) { return submissionDAO.findById(id); }

    @Override
    public List<PoliceCandidateDTO> loadCandidates(int examDateId) {
        return enrich(submissionDAO.findCandidates(examDateId));
    }
    @Override
    public List<PoliceCandidateDTO> loadCandidates(int examDateId, int page, int size) {
        return enrich(submissionDAO.findCandidates(examDateId, (Math.max(1, page) - 1) * size, size));
    }
    private List<PoliceCandidateDTO> enrich(List<PoliceCandidateDTO> source) {
        List<PoliceCandidateDTO> rows = new ArrayList<>();
        for (PoliceCandidateDTO row : source) {
            DossierDTO dossier = dossierDAO.findByRegistrationId(row.getExamRegistrationId());
            if (dossier != null) { row.setDossier(dossier); rows.add(row); }
        }
        return rows;
    }
    @Override public int countCandidates(int id) { return submissionDAO.countCandidates(id); }

    @Override
    public boolean review(int registrationDateId, String decision, String reason,
            String participationType) {
        int registrationId = submissionDAO.reviewCandidate(
                registrationDateId, decision, reason, participationType);
        if (registrationId <= 0) return false;
        DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
        boolean approved = "APPROVED".equalsIgnoreCase(decision);
        if (emailService.isConfigured() && dossier != null) {
            String name = dossier.getProfile() == null ? "thí sinh" : dossier.getProfile().getFullName();
            if (dossier.getUser() != null && dossier.getUser().getEmail() != null
                    && !dossier.getUser().getEmail().isBlank()) {
                String body = "Xin chào " + name + ",\n\nCơ quan CSGT đã "
                        + (approved ? "chấp thuận" : "từ chối") + " hồ sơ đề nghị sát hạch của bạn."
                        + (approved ? "\nNội dung thi được duyệt: "
                        + participationLabel(participationType)
                        + ".\nTrung tâm sẽ thông báo lịch thi chính thức sau khi nhận danh sách."
                        : "\nLý do: " + reason
                        + "\n\nVui lòng liên hệ trung tâm để được hướng dẫn hoàn thiện lại hồ sơ.");
                emailService.sendTextEmail(dossier.getUser().getEmail(),
                        approved ? "Hồ sơ sát hạch đã được CSGT chấp thuận"
                                : "Hồ sơ sát hạch bị CSGT từ chối", body);
            }
            if (!approved) {
                String centreBody = "CSGT đã từ chối hồ sơ sát hạch của thí sinh " + name
                        + ".\nLý do: " + reason
                        + "\n\nHồ sơ này không được đưa vào danh sách thi chính thức."
                        + " Vui lòng liên hệ thí sinh để hướng dẫn xử lý.";
                for (String email : submissionDAO.findActiveManagingStaffEmails()) {
                    emailService.sendTextEmail(email,
                            "CSGT từ chối một hồ sơ sát hạch", centreBody);
                }
            }
        }
        return true;
    }

    private static String participationLabel(String value) {
        return "PRACTICAL_ONLY".equalsIgnoreCase(value)
                ? "chỉ thi thực hành" : "lý thuyết và thực hành";
    }

    @Override
    public OfficialRosterPublishResult complete(int examDateId) {
        int total = submissionDAO.completeSubmission(examDateId);
        PoliceSubmissionDTO submission = submissionDAO.findById(examDateId);
        List<OfficialExamCandidateDTO> candidates = submissionDAO.findOfficialCandidates(examDateId);
        boolean configured = emailService.isConfigured();
        int candidateSent = 0;
        int centreSent = 0;
        if (configured && submission != null) {
            String examDate = submission.getExamDate() == null ? "chưa xác định"
                    : new SimpleDateFormat("dd/MM/yyyy").format(submission.getExamDate());
            for (OfficialExamCandidateDTO candidate : candidates) {
                if (candidate.getEmail() == null || candidate.getEmail().isBlank()) continue;
                String body = "Xin chào " + candidate.getFullName() + ",\n\n"
                        + "Cơ quan CSGT đã ban hành danh sách sát hạch chính thức ngày " + examDate
                        + ", hạng " + submission.getLicenceClass() + ".\n"
                        + "Số báo danh: " + candidate.getCandidateNumber() + ".\n\n"
                        + "Nội dung thi: " + candidate.getExamParticipationLabel() + ".\n"
                        + "Trung tâm sát hạch sẽ cập nhật phiên thi chính thức và thông báo các nội dung tiếp theo.";
                if (emailService.sendTextEmail(candidate.getEmail(),
                        "Thông báo danh sách sát hạch chính thức", body)) candidateSent++;
            }
            String centreBody = "Cơ quan CSGT đã ban hành danh sách sát hạch chính thức ngày "
                    + examDate + ", hạng " + submission.getLicenceClass() + ", gồm " + total
                    + " thí sinh.\n\nVui lòng đăng nhập hệ thống và tạo phiên thi chính thức. "
                    + "Danh sách thí sinh sẽ được hệ thống tiếp nhận tự động.";
            for (String email : submissionDAO.findActiveManagingStaffEmails()) {
                if (emailService.sendTextEmail(email,
                        "CSGT đã trả danh sách thi chính thức", centreBody)) centreSent++;
            }
        }
        return new OfficialRosterPublishResult(total, centreSent, candidateSent, configured);
    }
    @Override public List<OfficialExamCandidateDTO> loadOfficialCandidates(int id) {
        return submissionDAO.findOfficialCandidates(id);
    }
    @Override public List<OfficialExamCandidateDTO> loadOfficialCandidates(int id, int page, int size) {
        return submissionDAO.findOfficialCandidates(id, (Math.max(1, page) - 1) * size, size);
    }
    @Override public int countOfficialCandidates(int id) { return submissionDAO.countOfficialCandidates(id); }
}
