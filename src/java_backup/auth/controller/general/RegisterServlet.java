package auth.controller.general;

import auth.dto.RegisterResultDTO;
import shared.model.Profile;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import static auth.util.FormatUtil.formatString;
import auth.dto.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import auth.util.ValidationUtil;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/general/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get form values
        String govIdNo = formatString(request.getParameter("govIdNo"));
        String fullName = formatString(request.getParameter("fullName"));
        String phoneNo = formatString(request.getParameter("phoneNo"));
        String dateOfBirth = formatString(request.getParameter("dateOfBirth"));
        String address = formatString(request.getParameter("address"));
        String email = formatString(request.getParameter("email"));
        String sexParam = request.getParameter("sex");
        String terms = request.getParameter("terms");

        // validate required fields
        if (ValidationUtil.isBlank(govIdNo)
                || ValidationUtil.isBlank(fullName)
                || ValidationUtil.isBlank(phoneNo)
                || ValidationUtil.isBlank(dateOfBirth)
                || ValidationUtil.isBlank(address)
                || ValidationUtil.isBlank(email)) {
            forwardWithError(request, response, "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ thÃ´ng tin.");
            return;
        }

        // validate terms agreement
        if (terms == null) {
            forwardWithError(request, response, "Báº¡n pháº£i Ä‘á»“ng Ã½ vá»›i Äiá»u khoáº£n vÃ  ChÃ­nh sÃ¡ch báº£o máº­t.");
            return;
        }

        // validate government id
        if (!ValidationUtil.isValidCccd(govIdNo)) {
            forwardWithError(request, response, "Sá»‘ CCCD pháº£i gá»“m Ä‘Ãºng 12 chá»¯ sá»‘.");
            return;
        }

        // validate phone number
        if (!ValidationUtil.isValidPhone(phoneNo)) {
            forwardWithError(request, response, "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡.");
            return;
        }

        // validate email
        if (!ValidationUtil.isValidEmail(email)) {
            forwardWithError(request, response, "Äá»‹a chá»‰ email khÃ´ng há»£p lá»‡.");
            return;
        }

        // parse date of birth
        LocalDate dob = ValidationUtil.parseDate(dateOfBirth);

        // validate date format
        if (dob == null) {
            forwardWithError(request, response, "NgÃ y sinh khÃ´ng há»£p lá»‡.");
            return;
        }

        // validate future date
        if (dob.isAfter(LocalDate.now())) {
            forwardWithError(request, response, "NgÃ y sinh khÃ´ng thá»ƒ chá»n.");
            return;
        }

        // build profile
        Profile profile = new Profile();
        profile.setGovernmentIdNumber(govIdNo);
        profile.setFullName(fullName);
        profile.setPhoneNumber(phoneNo);
        profile.setAddress(address);
        profile.setSex("1".equals(sexParam));
        profile.setDateOfBirth(Timestamp.valueOf(dob.atStartOfDay()));

        // register account
        ServiceResult<RegisterResultDTO> result = authService.register(profile, email);

        // registration failed
        if (!result.isSuccess()) {
            forwardWithError(request, response, result.getMessage());
            return;
        }

        // get registration result
        RegisterResultDTO data = result.getData();

        // store success message
        HttpSession session = request.getSession();

        if (data.isEmailSent()) {
            session.setAttribute("successMessage",
                    "ÄÄƒng kÃ½ thÃ nh cÃ´ng! Kiá»ƒm tra email Ä‘á»ƒ láº¥y thÃ´ng tin Ä‘Äƒng nháº­p.");
        } else {
            session.setAttribute(
                    "successMessage",
                    "ÄÄƒng kÃ½ thÃ nh cÃ´ng! KhÃ´ng gá»­i Ä‘Æ°á»£c email - vui lÃ²ng lÆ°u thÃ´ng tin Ä‘Äƒng nháº­p bÃªn dÆ°á»›i.");
            session.setAttribute("registrationUsername", data.getUsername());
            session.setAttribute("registrationPassword", data.getPassword());
        }

        // redirect to login page
        response.sendRedirect(request.getContextPath() + "/login");
    }

    // forward back to register page with an error
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/views/auth/general/register.jsp").forward(request, response);
    }
}

