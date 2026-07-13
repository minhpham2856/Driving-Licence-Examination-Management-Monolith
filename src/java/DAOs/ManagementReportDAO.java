package DAOs;

import DTOs.ManagementReportExamOptionDTO;
import DTOs.ManagementReportRowDTO;
import java.util.List;

public interface ManagementReportDAO {

    List<ManagementReportRowDTO> findReportRows(
            String periodGroup, int examId, int year, String licenceClass);

    List<ManagementReportExamOptionDTO> findExamOptions();

    List<Integer> findAvailableYears();
}
