package managingstaff.dao;

import managingstaff.dto.ManagementReportExamOptionDTO;
import managingstaff.dto.ManagementReportRowDTO;
import java.util.List;

public interface ManagementReportDAO {

    List<ManagementReportRowDTO> findReportRows(
            String periodGroup, int examId, int year, String licenceClass);

    List<ManagementReportExamOptionDTO> findExamOptions();

    List<Integer> findAvailableYears();
}
