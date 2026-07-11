package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.util.ExamRegistrationSort;

import java.util.List;

public interface AllocationStageViewService {

    AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId);
}
