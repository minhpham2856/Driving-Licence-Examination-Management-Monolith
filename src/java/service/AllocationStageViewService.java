package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationStageViewDTO;
import util.ExamRegistrationSort;

import java.util.List;

public interface AllocationStageViewService {

    AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec);

    AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId);
}
