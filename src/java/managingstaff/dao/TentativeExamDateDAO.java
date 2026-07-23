package managingstaff.dao;

import java.sql.Date;
import java.util.List;
import managingstaff.dto.TentativeExamDateDTO;

public interface TentativeExamDateDAO {
    List<TentativeExamDateDTO> findPage(String tab, int page, int pageSize);
    int countAll(String tab);
    TentativeExamDateDTO findById(int id);
    int create(Date examDate, int licenceId);
    List<Integer> findRegistrationIds(int examDateId, int page, int pageSize);
    List<Integer> findAllRegistrationIds(int examDateId);
    int countRegistrations(int examDateId);
    int cancel(int examDateId, String reason, int cancelledByUserId);
    List<Integer> lockDueDates();
    int submitToPolice(int examDateId);
    List<TentativeExamDateDTO> findPoliceCompletedUnlinked();
}
