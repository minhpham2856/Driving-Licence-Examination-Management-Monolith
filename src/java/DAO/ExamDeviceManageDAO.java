package DAO;

import Models.ExamDeviceView;
import java.util.List;

/**
 * Admin CRUD for ExamDevice (MÃ¡y thi). Named distinctly so it does NOT clash
 * with the team's existing DAO.ExamDeviceDAO (device-allocation use case).
 */
public interface ExamDeviceManageDAO {
    List<ExamDeviceView> search(String keyword, Integer roomId, String status);
    ExamDeviceView findById(int examDeviceId);
    int insert(ExamDeviceView device, Integer createdBy);
    boolean update(ExamDeviceView device, Integer updatedBy);
    boolean delete(int examDeviceId);
    int countAll();
    int countByStatus(String status);
}
