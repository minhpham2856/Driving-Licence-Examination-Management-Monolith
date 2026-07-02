package controller.registrant;

import model.user.Profile;
import model.user.User;
import service.RegistrantProfileService;
import service.impl.RegistrantProfileServiceImpl;
import util.registrant.RegistrantExamSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/registrant/profile")
public class ProfileServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/profile.jsp";

    private final RegistrantProfileService profileService = new RegistrantProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        profileService.copyProfileToRequest(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }

        Profile updated = bindProfileFromRequest(request);
        if (updated.getFullName() == null || updated.getFullName().isBlank()) {
            forwardWithError(user, request, response, "Họ và tên không được để trống.");
            return;
        }

        String validationError = profileService.validateProfileUpdate(user, updated);
        if (validationError != null) {
            forwardWithError(user, request, response, validationError);
            return;
        }

        if (profileService.updateProfile(user, updated, request.getSession())) {
            response.sendRedirect(request.getContextPath() + "/registrant/profile?success=1");
        } else {
            forwardWithError(user, request, response, "Không thể cập nhật hồ sơ.");
        }
    }

    private static Profile bindProfileFromRequest(HttpServletRequest request) {
        Profile updated = new Profile();
        updated.setFullName(trim(request.getParameter("fullName")));
        String dob = request.getParameter("dob");
        if (dob != null && !dob.isBlank()) {
            updated.setDateOfBirth(Date.valueOf(dob));
        }
        updated.setGender("Nữ".equalsIgnoreCase(request.getParameter("gender"))
                || "Nu".equalsIgnoreCase(request.getParameter("gender")));
        updated.setPhoneNo(request.getParameter("phone"));
        updated.setAddress(request.getParameter("address"));
        updated.setGovIdNo(RegistrantExamSupport.normalizeGovIdNumber(request.getParameter("idCard")));
        return updated;
    }

    private void forwardWithError(User user, HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("openEditModal", Boolean.TRUE);
        profileService.copyProfileToRequest(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    private static String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
