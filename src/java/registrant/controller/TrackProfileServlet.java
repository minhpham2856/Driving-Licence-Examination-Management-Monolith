package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantTrackProfileService;
import registrant.service.impl.RegistrantTrackProfileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Theo dõi hồ sơ — {@code GET /registrant/track-profile}.
 * <p>
 * Lấy Audit + Document + đăng ký → {@code RegistrantProfileProgressBuilder}
 * dựng timeline 5 bước (Tiếp nhận → Duyệt → Bổ sung → Đã duyệt → Cấp SBD) → JSP.
 */
@WebServlet("/registrant/track-profile")
public class TrackProfileServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/track-profile.jsp";

    private final RegistrantTrackProfileService trackProfileService = new RegistrantTrackProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        trackProfileService.copyTrackingToRequest(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }
}
