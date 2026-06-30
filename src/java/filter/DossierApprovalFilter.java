package filter;

import dao.DossierDAO;
import dao.impl.DossierDAOImpl;
import dto.DossierDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;

@WebFilter(urlPatterns = {"/views/registrant/register-exam.jsp", "/registrant/register-exam"})
public class DossierApprovalFilter implements Filter {

    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User user = currentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        DossierDTO dossier = dossierDAO.findByUserId(user.getUserId());
        if (dossier == null || !"Approved".equalsIgnoreCase(dossier.getStatus())) {
            req.getSession().setAttribute("dossierSuccess",
                    "Hồ sơ phải được Ban quản lý xác minh trước khi đăng ký lịch thi.");
            resp.sendRedirect(req.getContextPath() + "/registrant/dossier?view=track");
            return;
        }
        chain.doFilter(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }
}
