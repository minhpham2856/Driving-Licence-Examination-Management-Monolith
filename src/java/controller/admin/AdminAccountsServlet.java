package controller.admin;

import service.UserService;
import service.impl.UserServiceImpl;
import util.FormatUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminAccountsServlet", urlPatterns = {"/admin/accounts"})
public class AdminAccountsServlet extends HttpServlet {

    private UserService accountService;

    @Override
    public void init() {
        accountService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = FormatUtil.text(req.getParameter("searchKeyword"));
        String roleFilter = FormatUtil.text(req.getParameter("filterRole"));
        String statusFilter = FormatUtil.text(req.getParameter("filterStatus"));
        List<Map<String, Object>> accounts = accountService.searchAccounts(keyword, roleFilter, statusFilter);
        req.setAttribute("accounts", accounts);
        req.setAttribute("totalAccounts", accounts.size());
        req.setAttribute("adminCount", accountService.countByRoleKey("admin"));
        req.setAttribute("coiThiCount", accountService.countByRoleKey("coi_thi"));
        req.setAttribute("chamThiCount", accountService.countByRoleKey("cham_thi"));
        req.getRequestDispatcher("/views/admin/accounts.jsp").forward(req, resp);
    }
}
