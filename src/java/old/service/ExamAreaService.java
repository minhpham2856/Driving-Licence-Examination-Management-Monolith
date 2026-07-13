package service;

import dto.SaveResultDTO;
import dto.ServiceResult;
import model.ExamArea;

import java.util.List;

public interface ExamAreaService {

    ExamArea getById(int id);

    List<ExamArea> search(String keyword, String type);

    int countAll();

    List<ExamArea> getActiveTheoryRooms();

    ServiceResult<SaveResultDTO> save(ExamArea area, int adminUserId);

    ServiceResult<Void> delete(int id, int adminUserId);
}
