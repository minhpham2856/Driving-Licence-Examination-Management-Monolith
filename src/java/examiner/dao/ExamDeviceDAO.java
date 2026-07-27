package examiner.dao;
import shared.model.ExamDevice;
import java.util.List;

// DAO contract for ExamDevice persistence; examiner module SQL boundary.
public interface ExamDeviceDAO {

    // Loads one exam device row by primary key.
    ExamDevice get(int examDeviceId);

    // Inserts a new exam device and returns generated id.
    int add(ExamDevice device);

    // Updates an existing exam device row.
    boolean update(ExamDevice device);

    // Deletes an exam device row by primary key.
    boolean delete(int examDeviceId);

    // Returns total count of exam device rows.
    int countAll();

    // Searches exam devices by keyword and active status filter.
    List<ExamDevice> getFiltered(String keyword, boolean isActive);

    // Returns count of devices matching active/inactive status.
    int countByStatus(boolean isActive);

    // Updates only the IsActive flag on one device row.
    boolean updateStatus(int examDeviceId, boolean isActive);

    // Lists devices assigned to one exam area.
    List<ExamDevice> getDevicesByAreaId(int examAreaId);

    // Lists devices for multiple exam area ids in one query.
    List<ExamDevice> getAllByAreaIds(List<Integer> areaIds);
}
