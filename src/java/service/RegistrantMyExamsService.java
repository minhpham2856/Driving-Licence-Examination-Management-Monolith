package service;

import dto.registrant.RegistrantMyExamRow;
import model.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface RegistrantMyExamsService {
    List<RegistrantMyExamRow> listExams(User user);

    void copyMyExamsToRequest(User user, HttpServletRequest request, String selectedExamId);

    RegistrantMyExamRow findSelectedExam(User user, String selectedExamId);

    String requestCancellation(User user, HttpServletRequest request);
}
