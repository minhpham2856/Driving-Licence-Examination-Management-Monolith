package Controllers.Registrant;



import Models.User;

import Services.Impl.RegistrantTrackProfileServiceImpl;

import Services.RegistrantTrackProfileService;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



/**
 * Theo dõi tiến trình hồ sơ — timeline 5 bước + nhật ký hoạt động.
 * <p>URL: GET /registrant/track-profile</p>
 * <p>Bước: đăng ký TK → nộp hồ sơ → duyệt → lịch thi → cấp chứng chỉ</p>
 */
@WebServlet("/registrant/track-profile")
public class TrackProfileServlet extends HttpServlet {



    private final RegistrantTrackProfileService trackProfileService = new RegistrantTrackProfileServiceImpl();



    /** Timeline 5 bước tiến trình hồ sơ + nhật ký hoạt động (track-profile.jsp). */
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để theo dõi hồ sơ.");

        if (user == null) {

            return;

        }



        trackProfileService.populateTrackProfile(request, user);

        request.getRequestDispatcher("/views/registrant/track-profile.jsp").forward(request, response);

    }

}

