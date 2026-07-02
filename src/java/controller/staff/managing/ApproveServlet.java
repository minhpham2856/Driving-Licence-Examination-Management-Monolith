package controller.staff.managing;

import dto.staff.ManagingStaffApprovalView;
import model.user.User;
import service.ManagingStaffApprovalService;
import service.impl.ManagingStaffApprovalServiceImpl;
import util.registrant.DocumentUrlResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/manager/approve")
public class ApproveServlet extends HttpServlet {

    private final ManagingStaffApprovalService approvalService = new ManagingStaffApprovalServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User staff = ManagingStaffAuth.requireManagingStaff(request, response);
        if (staff == null) {
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            showDetail(request, response, parseProfileId(idParam));
            return;
        }

        List<ManagingStaffApprovalView> pending = approvalService.listPendingApprovals();
        request.setAttribute("pendingUsersList", pending);
        request.setAttribute("pendingApprovalsCount", pending.size());
        request.getRequestDispatcher("/views/staff/managingstaff/approve.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User staff = ManagingStaffAuth.requireManagingStaff(request, response);
        if (staff == null) {
            return;
        }

        int profileId = parseProfileId(request.getParameter("id"));
        int workflowErId = parseProfileId(request.getParameter("erId"));
        boolean approved = "approve".equalsIgnoreCase(request.getParameter("decision"));
        String error = approvalService.reviewDocuments(
                staff,
                profileId,
                workflowErId,
                approved,
                request.getParameter("rejectionReason"),
                request.getSession());

        if (error != null) {
            request.setAttribute("error", error);
            showDetail(request, response, profileId);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/manager/approve?success=1");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, int profileId)
            throws ServletException, IOException {
        int workflowErId = parseProfileId(request.getParameter("erId"));
        ManagingStaffApprovalView user = workflowErId > 0
                ? approvalService.loadApprovalDetail(profileId, workflowErId)
                : approvalService.loadApprovalDetail(profileId);
        DocumentUrlResolver.resolveApprovalViewUrls(user, request);
        request.setAttribute("user", user);
        request.getRequestDispatcher("/views/staff/managingstaff/approve.jsp").forward(request, response);
    }

    private static int parseProfileId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
