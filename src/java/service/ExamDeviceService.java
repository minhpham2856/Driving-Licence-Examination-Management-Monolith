package service;

import dto.ExamDeviceViewDTO;
import dto.ServiceResult;
import dto.payload.DeleteExamDeviceCommand;
import dto.payload.SaveExamDeviceCommand;
import dto.payload.SaveExamDeviceData;

import java.util.List;

public interface ExamDeviceService {

    List<ExamDeviceViewDTO> search(String keyword, String status);

    int countAll();

    int countByStatus(String status);

    ServiceResult<SaveExamDeviceData> save(SaveExamDeviceCommand command);

    ServiceResult<Void> delete(DeleteExamDeviceCommand command);
}
