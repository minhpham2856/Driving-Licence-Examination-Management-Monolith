package Controllers.Registrant;

import Models.User;
import Services.RegistrantTrackProfileService;
import Services.Impl.RegistrantTrackProfileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registrant/track-profile")
public class TrackProfileServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/track-profile.jsp";

    private final RegistrantTrackProfileService trackProfileService = new RegistrantTrackProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        trackProfileService.copyTrackingToRequest(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }
}
