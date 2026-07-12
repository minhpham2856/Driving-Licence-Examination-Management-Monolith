package examstaff.dao.view;

import java.util.List;
import java.util.Map;

public interface ReportInfractionViewDAO {

    List<Map<String, Object>> findTopInfractions(int examId, int limit);
}
