package Filters;

import DAOs.DossierDAO;
import DAOs.Impl.DossierDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Utils.SessionUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/views/registrant/register-exam.jsp", "/registrant/register-exam"})
public class DossierApprovalFilter implements Filter {
    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        DossierDTO dossier = dossierDAO.findByUserId(user.getId());
        if (dossier == null || !"Approved".equalsIgnoreCase(dossier.getStatus())) {
            req.getSession().setAttribute("dossierSuccess",
                    "Hồ sơ phải được Ban quản lý xác minh trước khi đăng ký lịch thi.");
            resp.sendRedirect(req.getContextPath() + "/registrant/dossier?view=track");
            return;
        }
        chain.doFilter(request, response);
    }
}
