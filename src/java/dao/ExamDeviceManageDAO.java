package dao;


import model.exam.ExamDevice;

import java.util.List;

public interface ExamDeviceManageDAO {

    List<ExamDevice> search(String keyword, String status);

    ExamDevice findById(int examDeviceId);

    int insert(ExamDevice device, Integer createdBy);

    boolean update(ExamDevice device, Integer updatedBy);

    boolean delete(int examDeviceId);

    int countAll();

    int countByStatus(String status);
}
