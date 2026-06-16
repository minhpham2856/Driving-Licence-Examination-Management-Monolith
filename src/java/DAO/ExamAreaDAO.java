package DAO;

import Models.ExamArea;
import java.util.List;

public interface ExamAreaDAO {
    List<ExamArea> search(String keyword, String areaType);
    ExamArea findById(int examAreaId);
    int insert(ExamArea area);
    boolean update(ExamArea area);
    boolean delete(int examAreaId);
    int countAll();
}
