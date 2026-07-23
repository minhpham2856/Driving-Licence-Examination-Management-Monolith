package examiner.service.impl;

import examiner.dao.CandidateDAO;
import examiner.dao.ExamAreaDAO;
import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.impl.CandidateDAOImpl;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dto.ExamDispatchResult;
import examiner.dto.ServiceResult;
import examiner.dto.EnrollmentDTO;
import shared.enums.CandidateStatus;
import shared.enums.ErrorType;
import shared.enums.ExamAreaType;
import shared.enums.SectionType;
import shared.model.Candidate;
import shared.model.ExamArea;
import shared.model.ExamEnrollment;
import shared.queue.ExamQueueHandoff;
import shared.queue.ExamRoomQueueRegistry;

import java.util.List;
import examiner.service.DispatchService;
import examiner.service.ProgressService;
import examiner.service.EnrollmentService;

// Routes candidates between exam areas and room queues after section completion or errors.
public class DispatchServiceImpl implements DispatchService {

    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamEnrollmentSectionDAO enrollmentSectionDAO = new ExamEnrollmentSectionDAOImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final ProgressService sectionProgressService = new ProgressServiceImpl();

    // Assigns a candidate to the first applicable section queue (theory or layout).
    @Override
    public ServiceResult<ExamDispatchResult> passOn(Candidate candidate, int examId) {
        if (candidate == null || examId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin thí sinh không hợp lệ.");
        }
        boolean takeTheory = Boolean.TRUE.equals(candidate.getTakeTheory());
        boolean takeLayout = Boolean.TRUE.equals(candidate.getTakeLayout());
        if (!takeTheory && !takeLayout) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không có phần thi cần điều phối.");
        }
        SectionType target = takeTheory ? SectionType.THEORY : SectionType.LAYOUT;
        return passOn(candidate, examId, target);
    }

    // Assigns a candidate to a specific target section queue and exam area.
    @Override
    public ServiceResult<ExamDispatchResult> passOn(Candidate candidate, int examId, SectionType targetSection) {
        if (candidate == null || examId <= 0 || targetSection == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin điều phối không hợp lệ.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, candidate.getCandidateId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        int sbd = parseCandidateNumber(candidate.getCandidateNumber());
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        ExamQueueHandoff handoff = new ExamQueueHandoff(
                examId,
                candidate.getCandidateId(),
                sbd,
                enrollment.getExamEnrollmentId(),
                Boolean.TRUE.equals(candidate.getTakeTheory()),
                Boolean.TRUE.equals(candidate.getTakeLayout()));
        return assign(handoff, targetSection);
    }

    // Returns a candidate to the procedure queue via ExamRoomQueueRegistry.
    @Override
    public ServiceResult<Void> passBack(Candidate candidate, int examId) {
        if (candidate == null || examId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin thí sinh không hợp lệ.");
        }
        int sbd = parseCandidateNumber(candidate.getCandidateNumber());
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        // TBD: return candidate to examstaff procedure queue.
        ExamRoomQueueRegistry.passBackCandidate(examId, sbd);
        return ServiceResult.ok(null);
    }

    // Promotes the next waiting candidate when a section completes in an exam area.
    @Override
    public ServiceResult<Integer> onSectionComplete(int examId, int sbd, SectionType sectionType, int examAreaId) {
        if (examId <= 0 || sbd <= 0 || sectionType == null || examAreaId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin hoàn tất không hợp lệ.");
        }
        Integer promoted = ExamRoomQueueRegistry.completeTesting(examId, examAreaId, sectionType, sbd);
        if (promoted != null && promoted > 0) {
            EnrollmentDTO enrollment = enrollmentService.getByExamAndSbd(examId, promoted, sectionType);
            if (sectionType == SectionType.THEORY
                    && enrollment != null && enrollment.getExamEnrollmentId() > 0) {
                sectionProgressService.update(
                        enrollment.getExamEnrollmentId(),
                        sectionType,
                        CandidateStatus.IN_PROGRESS);
            }
            // TBD: ExamStaffPublicCallNotifier.notify(examId, examAreaId, promoted);
        }
        return ServiceResult.ok(promoted);
    }

    // Enqueues a handoff on the least-loaded area for the target section.
    private ServiceResult<ExamDispatchResult> assign(ExamQueueHandoff handoff, SectionType targetSection) {
        List<ExamArea> areas = loadAreasForSection(handoff.getExamId(), targetSection);
        if (areas.isEmpty()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không có phòng/sân thi được gán cho kỳ thi.");
        }
        ExamRoomQueueRegistry.ensureQueues(handoff.getExamId(), targetSection, areas);
        ExamArea chosen = pickLeastLoadedArea(handoff.getExamId(), targetSection, areas);
        if (chosen == null) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể chọn phòng/sân thi.");
        }
        int areaId = chosen.getExamAreaId();
        ExamRoomQueueRegistry.enqueue(handoff.getExamId(), areaId, targetSection, handoff.getSbd());
        Integer promoted = ExamRoomQueueRegistry.tryPromote(handoff.getExamId(), areaId, targetSection);

        sectionProgressService.add(handoff.getEnrollmentId(), targetSection);
        enrollmentSectionDAO.updateExamAreaIdByEnrollmentIdAndSectionType(
                handoff.getEnrollmentId(), targetSection.getValue(), areaId);
        if (targetSection == SectionType.THEORY) {
            ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(
                    handoff.getExamId(), handoff.getCandidateId());
            if (enrollment != null) {
                enrollment.setAllocatedExamAreaId(areaId);
                enrollmentDAO.update(enrollment);
            }
        }

        if (targetSection == SectionType.THEORY
                && promoted != null && promoted == handoff.getSbd()) {
            sectionProgressService.update(
                    handoff.getEnrollmentId(), targetSection, CandidateStatus.IN_PROGRESS);
            // TBD: ExamStaffPublicCallNotifier.notify(handoff.getExamId(), areaId, promoted);
        }

        ExamDispatchResult result = new ExamDispatchResult();
        result.setExamAreaId(areaId);
        result.setSectionType(targetSection);
        result.setPromotedCandidateNumber(promoted);
        result.setQueueLoad(ExamRoomQueueRegistry.load(handoff.getExamId(), areaId, targetSection));
        return ServiceResult.ok(result);
    }

    // Loads exam rooms for theory or exam grounds for practical sections.
    private List<ExamArea> loadAreasForSection(int examId, SectionType sectionType) {
        if (sectionType == SectionType.THEORY) {
            return examAreaDAO.getAreasByExamIdAndType(examId, ExamAreaType.EXAM_ROOM.getValue());
        }
        return examAreaDAO.getAreasByExamIdAndType(examId, ExamAreaType.EXAM_GROUND.getValue());
    }

    // Picks the exam area with the smallest queue load for the section.
    private ExamArea pickLeastLoadedArea(int examId, SectionType sectionType, List<ExamArea> areas) {
        ExamArea best = null;
        int bestLoad = Integer.MAX_VALUE;
        for (ExamArea area : areas) {
            if (area == null || area.getExamAreaId() <= 0) {
                continue;
            }
            int load = ExamRoomQueueRegistry.load(examId, area.getExamAreaId(), sectionType);
            if (load < bestLoad) {
                bestLoad = load;
                best = area;
            }
        }
        return best;
    }

    // Parses candidate number string to int, returning 0 when invalid.
    private static int parseCandidateNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
