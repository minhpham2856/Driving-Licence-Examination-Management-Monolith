package examstaff.service.impl;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import examstaff.service.ExamStaffDashboardService;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.service.ExaminerAllocationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExamStaffDashboardServiceImpl implements ExamStaffDashboardService {

    private final ExamStaffSessionQueryService sessionQuery;
    private final ExaminerAllocationService allocationService;

    public ExamStaffDashboardServiceImpl() {
        this(new ExamStaffSessionQueryServiceImpl(), new ExaminerAllocationServiceImpl());
    }

    public ExamStaffDashboardServiceImpl(ExamStaffSessionQueryService sessionQuery,
            ExaminerAllocationService allocationService) {
        this.sessionQuery = sessionQuery;
        this.allocationService = allocationService;
    }

    @Override
    public ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allSessions, int examId) {
        ExamStaffDashboardViewDTO view = new ExamStaffDashboardViewDTO();
        List<ExamSummaryDTO> daySessions = sessionQuery.listSessionsForExam(allSessions, examId);

        Set<Integer> assignedExaminerIds = new HashSet<>();
        for (ExamSummaryDTO daySession : daySessions) {
            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsBySessionId(daySession.getId());
            if (slots == null) {
                continue;
            }
            for (ExaminerSlotDTO slot : slots) {
                if (slot.getExaminerUserId() > 0) {
                    assignedExaminerIds.add(slot.getExaminerUserId());
                }
            }
        }
        view.setAssignedExaminerUniqueCount(assignedExaminerIds.size());
        view.setTotalActiveExaminerCount(allocationService.getActiveExaminers().size());
        return view;
    }
}
