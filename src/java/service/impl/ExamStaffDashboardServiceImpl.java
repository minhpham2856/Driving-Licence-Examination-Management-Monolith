package service.impl;

import dto.ExaminerSlotDTO;
import dto.SessionDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;
import service.ExamStaffDashboardService;
import service.ExamStaffSessionQueryService;
import service.ExaminerAllocationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExamStaffDashboardServiceImpl implements ExamStaffDashboardService {

    private final ExamStaffSessionQueryService sessionQuery = new ExamStaffSessionQueryServiceImpl();
    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

    @Override
    public ExamStaffDashboardViewDTO buildView(List<SessionDTO> allSessions, int examId) {
        ExamStaffDashboardViewDTO view = new ExamStaffDashboardViewDTO();
        List<SessionDTO> daySessions = sessionQuery.listSessionsForExam(allSessions, examId);
        view.setDaySessions(daySessions);

        Set<Integer> assignedExaminerIds = new HashSet<>();
        for (SessionDTO daySession : daySessions) {
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
