package Services;

import Models.RegistrantMyExamRow;
import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface RegistrantMyExamsService {
    List<RegistrantMyExamRow> listExams(User user);

    void copyMyExamsToRequest(User user, HttpServletRequest request, String selectedExamId);

    RegistrantMyExamRow findSelectedExam(User user, String selectedExamId);

    String requestCancellation(User user, HttpServletRequest request);
}
