package examstaff.service.impl;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import examstaff.service.ExamStaffDashboardService;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.service.ExaminerAllocationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExamStaffDashboardServiceImpl implements ExamStaffDashboardService {

    private final ExamStaffExamQueryService examQuery;
    private final ExaminerAllocationService allocationService;

    public ExamStaffDashboardServiceImpl() {
        this(new ExamStaffExamQueryServiceImpl(), new ExaminerAllocationServiceImpl());
    }

    public ExamStaffDashboardServiceImpl(ExamStaffExamQueryService examQuery,
            ExaminerAllocationService allocationService) {
        this.examQuery = examQuery;
        this.allocationService = allocationService;
    }

    @Override
    public ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allExams, int examId) {
        ExamStaffDashboardViewDTO view = new ExamStaffDashboardViewDTO();
        List<ExamSummaryDTO> dayExams = examQuery.listExamsForDay(allExams, examId);

        Set<Integer> assignedExaminerIds = new HashSet<>();
        for (ExamSummaryDTO daySession : dayExams) {
            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsByExamId(daySession.getId());
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
