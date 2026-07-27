package registrant.controller;

import shared.model.Profile;
import auth.dto.UserDTO;
import registrant.service.RegistrantProfileService;
import registrant.service.impl.RegistrantProfileServiceImpl;
import registrant.util.RegistrantExamSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Hồ sơ cá nhân Registrant — GET/POST /registrant/profile.
 * GET — lấy data: session USER → RegistrantProfileService.copyProfileToRequest đọc bảng Profile (+ tiến độ / status từ ExamRegistration document-workflow) → set request attributes → forward profile.jsp.
 * POST — nhận form → ghi DB: bind FullName, DOB, Sex, Phone, Address, CCCD → validate (CCCD khóa khi status Approved) → INSERT/UPDATE Profile → redirect ?success=1 hoặc forward kèm error.
 * CCCD (GovernmentIdNumber) là cầu nối sau này với Candidate để đọc Payment.
 */
@WebServlet("/registrant/profile")
public class ProfileServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/profile.jsp";

    private final RegistrantProfileService profileService = new RegistrantProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        profileService.copyProfileToRequest(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
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
            updated.setDateOfBirth(new Timestamp(Date.valueOf(dob.trim()).getTime()));
        }
        // Profile.Sex: true/1 = Nam, false/0 = Nữ (khớp seed DB + managing staff)
        String gender = request.getParameter("gender");
        updated.setSex("Nam".equalsIgnoreCase(gender)
                || "1".equals(gender));
        updated.setPhoneNumber(request.getParameter("phone"));
        updated.setAddress(request.getParameter("address"));
        updated.setGovernmentIdNumber(RegistrantExamSupport.normalizeGovIdNumber(request.getParameter("idCard")));
        return updated;
    }

    private void forwardWithError(UserDTO user, HttpServletRequest request, HttpServletResponse response, String error)
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
