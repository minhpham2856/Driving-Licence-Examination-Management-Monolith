package registrant.service;

import registrant.dto.RegistrantMyExamRow;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface RegistrantMyExamsService {
    List<RegistrantMyExamRow> listExams(UserDTO user);

    void copyMyExamsToRequest(UserDTO user, HttpServletRequest request, String selectedExamId);

    RegistrantMyExamRow findSelectedExam(UserDTO user, String selectedExamId);

    String requestCancellation(UserDTO user, HttpServletRequest request);
}
