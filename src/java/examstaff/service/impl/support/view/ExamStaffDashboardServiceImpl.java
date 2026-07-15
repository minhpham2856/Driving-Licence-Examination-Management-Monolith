package examstaff.service.impl.support.view;
import examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Implementation: dựng view dashboard Exam Staff (số giám khảo đã phân công). */
public class ExamStaffDashboardServiceImpl {

    private final ExamStaffExamQueryServiceImpl examQuery;
    private final ExaminerAllocationServiceImpl allocationService;

    /** Wiring mặc định khi không inject từ composition root. */
    public ExamStaffDashboardServiceImpl() {
        this(new ExamStaffExamQueryServiceImpl(), new ExaminerAllocationServiceImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public ExamStaffDashboardServiceImpl(ExamStaffExamQueryServiceImpl examQuery,
            ExaminerAllocationServiceImpl allocationService) {
        this.examQuery = examQuery;
        this.allocationService = allocationService;
    }

    /**
     * Ghép view dashboard theo kỳ thi đang chọn.
     *
     * @param allExams danh sách kỳ thi
     * @param examId   mã kỳ đang xem
     * @return DTO dashboard
     */
    public ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allExams, int examId) {
        ExamStaffDashboardViewDTO view = new ExamStaffDashboardViewDTO();
        // Load: các phiên trong ngày theo kỳ tham chiếu
        List<ExamSummaryDTO> dayExams = examQuery.listExamsForDay(allExams, examId);

        // Mutate: gom giám khảo đã gán (unique) trên mọi phiên ngày
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
        // Result
        view.setAssignedExaminerUniqueCount(assignedExaminerIds.size());
        view.setTotalActiveExaminerCount(allocationService.getActiveExaminers().size());
        return view;
    }
}
