package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantMyExamsService {

    void populateExamList(HttpServletRequest request, User user);

    void populateExamDetail(HttpServletRequest request, User user);
}
