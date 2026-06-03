package DAO;

import Models.ExamComputer;
import java.util.List;

public interface ExamComputerDAO {
    List<ExamComputer> getAvailableComputers();
    List<ExamComputer> getAvailableComputersByArea(int areaId);
    boolean updateStatus(int id, String status);
}
