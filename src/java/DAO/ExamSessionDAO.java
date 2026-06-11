package DAO;

import Models.ExamSessionOption;
import java.util.List;
import java.util.Optional;

public interface ExamSessionDAO {

    List<ExamSessionOption> findOpenByLicenseCode(String licenseCode);

    Optional<ExamSessionOption> findById(int sessionId);

    boolean incrementRegisteredCount(int sessionId);

    boolean hasAvailableSlot(int sessionId);
}
