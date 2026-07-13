package examstaff.dao;

import examstaff.model.ExamZone;
import java.util.List;

public interface ExamZoneDAO {

    List<ExamZone> findAllActive();

    ExamZone getById(int examZoneId);
}
