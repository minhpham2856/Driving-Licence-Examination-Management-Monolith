package examstaff.dao;

import shared.model.ExamZone;
import java.util.List;

public interface ExamZoneDAO {

    List<ExamZone> findAllActive();

    ExamZone getById(int examZoneId);
}

