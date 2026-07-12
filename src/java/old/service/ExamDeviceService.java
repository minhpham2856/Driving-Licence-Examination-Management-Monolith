package service;

import dto.DeviceRowDTO;
import dto.SaveResultDTO;
import dto.ServiceResult;

import java.util.List;

public interface ExamDeviceService {

    List<DeviceRowDTO> search(String keyword, String status);

    int countAll();

    int countByStatus(String status);

    ServiceResult<SaveResultDTO> save(DeviceRowDTO device, Integer adminUserId);

    ServiceResult<Void> delete(int deviceId, Integer adminUserId);
}
