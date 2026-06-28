package controller.staff.managing;

import service.UserManagementService;
import service.impl.UserManagementServiceImpl;
import model.user.User;
import dto.user.CreateUserResultDTO;
import service.RoleService;
import service.impl.RoleServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/manager/create-user")
public class CreateUserServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managing/create-user.jsp";

    private final UserManagementService userManagementService = new UserManagementServiceImpl();
    private final RoleService roleService = new RoleServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        moveFlashAttribute(session, request, "createUserSuccess");
        moveFlashAttribute(session, request, "createdUsername");
        moveFlashAttribute(session, request, "createdPassword");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String cccd = trim(request.getParameter("cccd"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email")).toLowerCase();
        String dob = trim(request.getParameter("dob"));
        String gender = trim(request.getParameter("gender"));
        String address = trim(request.getParameter("address"));
        String userType = trim(request.getParameter("userType"));
        String licenseClass = trim(request.getParameter("licenseClass")).toUpperCase();

        CreateUserResultDTO result = userManagementService.createUser(
                fullName, cccd, phone, email, dob, gender, address, userType, licenseClass);

        if (!result.isSuccess()) {
            request.setAttribute("createUserError", result.getMessage());
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("createUserSuccess", result.getMessage());
        if (result.getUsername() != null) {
            session.setAttribute("createdUsername", result.getUsername());
            session.setAttribute("createdPassword", result.getPassword());
        }

        response.sendRedirect(request.getContextPath() + "/manager/create-user");
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            request.getSession(true).setAttribute("errorMessage",
                    "Bạn cần đăng nhập để truy cập chức năng này.");
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String roleName = roleService.getRoleNameById(user.getRoleId());
        if (!"ManagingStaff".equalsIgnoreCase(roleName)
                && !"Admin".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền tạo tài khoản học viên.");
            return false;
        }
        return true;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static void moveFlashAttribute(HttpSession session, HttpServletRequest request,
            String attributeName) {
        Object value = session.getAttribute(attributeName);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(attributeName);
        }
    }
}
