package Controllers.Registrant;



import Models.User;

import Services.Impl.RegistrantSettingsServiceImpl;

import Services.RegistrantSettingsService;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



/**
 * Cài đặt tài khoản thí sinh.
 * <p>URL: GET/POST /registrant/settings</p>
 * <p>POST mặc định: đổi mật khẩu | action=deactivate: vô hiệu hóa User.isActive</p>
 */
@WebServlet("/registrant/settings")
public class SettingsServlet extends HttpServlet {



    private final RegistrantSettingsService settingsService = new RegistrantSettingsServiceImpl();



    /** Hiển thị thông tin tài khoản, form đổi mật khẩu và tùy chọn vô hiệu hóa. */
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để truy cập cài đặt.");

        if (user == null) {

            return;

        }



        RegistrantAuth.transferFlash(request, "successMessage", "success");

        settingsService.populateSettings(request, user);

        request.getRequestDispatcher("/views/registrant/settings.jsp").forward(request, response);

    }



    /**
     * action=deactivate → vô hiệu hóa User + invalidate session + redirect login.
     * Mặc định → đổi mật khẩu (validate mật khẩu cũ, độ dài, xác nhận).
     */
    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để truy cập cài đặt.");

        if (user == null) {

            return;

        }



        String error = "deactivate".equals(request.getParameter("action"))

                ? settingsService.deactivateAccount(request, user)

                : settingsService.changePassword(request, user);



        if (error != null) {

            request.setAttribute("error", error);

            settingsService.populateSettings(request, user);

            request.getRequestDispatcher("/views/registrant/settings.jsp").forward(request, response);

            return;

        }



        if ("deactivate".equals(request.getParameter("action"))) {

            request.getSession().invalidate();

            response.sendRedirect(request.getContextPath() + "/login");

            return;

        }



        request.getSession().setAttribute("successMessage", "Mật khẩu đã được cập nhật thành công.");

        response.sendRedirect(request.getContextPath() + "/registrant/settings");

    }

}

