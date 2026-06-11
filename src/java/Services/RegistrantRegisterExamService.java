package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantRegisterExamService {

    void populateRegisterPage(HttpServletRequest request, User user, String licenceCode, String sessionCode);

    String registerExam(HttpServletRequest request, User user);
}
