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
 * Trang theo dõi tiến trình hồ sơ — GET /registrant/track-profile, forward track-profile.jsp.
 * Luồng: auth → RegistrantTrackProfileService.copyTrackingToRequest → forward.
 * Service gom audit + tài liệu thành profileTrackingLogs, profileProgressSteps (timeline 5 bước), documentSummary và cờ showSupplementAlert; hỗ trợ bộ lọc/paging trên nhật ký.
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
