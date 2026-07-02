package dao;

import model.exam.ExamComputer;
import java.util.List;

public interface ExamComputerDAO {
    List<ExamComputer> getAvailableComputers();
    List<ExamComputer> getAvailableComputersByArea(int areaId);
    boolean updateStatus(int id, String status);
}
