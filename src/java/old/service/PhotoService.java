package service;
import dto.EnrollmentDTO;
import java.util.List;
public interface PhotoService {
    void normalizeQueue(String appRoot, List<EnrollmentDTO> qList);
}
