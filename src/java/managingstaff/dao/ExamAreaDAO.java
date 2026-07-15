package managingstaff.dao;

import java.util.List;
import shared.model.ExamArea;

public interface ExamAreaDAO {
    List<ExamArea> search(String keyword, String areaType);
    ExamArea findById(int examAreaId);
}
