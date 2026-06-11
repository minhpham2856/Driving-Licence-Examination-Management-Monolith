package Controllers.Registrant;



import Models.User;

import Services.Impl.RegistrantProfileServiceImpl;

import Services.RegistrantProfileService;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



/**
 * Quản lý hồ sơ cá nhân (bảng Person).
 * <p>GET: hiển thị form + trạng thái duyệt + checklist giấy tờ.</p>
 * <p>POST: lưu Person; nếu chưa có personId thì insert Person và UserDAO.updatePersonId.</p>
 * <p>Service: RegistrantProfileServiceImpl | JSP: views/registrant/profile.jsp</p>
 */
@WebServlet("/registrant/profile")
public class ProfileServlet extends HttpServlet {



    private final RegistrantProfileService profileService = new RegistrantProfileServiceImpl();



    /**
     * Hiển thị form hồ sơ Person, checklist giấy tờ và badge trạng thái duyệt.
     * Chuyển flash success từ session (sau POST redirect).
     */
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để truy cập hồ sơ cá nhân.");

        if (user == null) {

            return;

        }



        RegistrantAuth.transferFlash(request, "successMessage", "success");

        profileService.populateProfile(request, user);

        request.getRequestDispatcher("/views/registrant/profile.jsp").forward(request, response);

    }



    /**
     * Lưu/cập nhật Person từ form POST.
     * Lần đầu: insert Person + gán personId vào User; lần sau: update.
     * Thành công → PRG redirect với successMessage.
     */
    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để truy cập hồ sơ cá nhân.");

        if (user == null) {

            return;

        }



        String error = profileService.saveProfile(request, user);

        if (error != null) {

            request.setAttribute("error", error);

            profileService.populateProfile(request, user);

            repopulateFormFromRequest(request);

            request.getRequestDispatcher("/views/registrant/profile.jsp").forward(request, response);

            return;

        }



        User refreshedUser = profileService.reloadUser(user.getId());

        if (refreshedUser != null) {

            request.getSession().setAttribute("user", refreshedUser);

        }



        request.getSession().setAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công.");

        response.sendRedirect(request.getContextPath() + "/registrant/profile");

    }



    /** Giữ lại giá trị form khi validation lỗi — tránh người dùng nhập lại từ đầu. */
    private void repopulateFormFromRequest(HttpServletRequest request) {

        request.setAttribute("registrantName", request.getParameter("fullName"));

        request.setAttribute("birthday", request.getParameter("dob"));

        request.setAttribute("gender", request.getParameter("gender"));

        request.setAttribute("phone", request.getParameter("phone"));

        request.setAttribute("email", request.getParameter("email"));

        request.setAttribute("address", request.getParameter("address"));

        request.setAttribute("idCardNumber", request.getParameter("idCard"));

    }

}

