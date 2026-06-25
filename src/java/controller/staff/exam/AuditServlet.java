package Controllers.Staff.ExamStaff;



import DAO.AuditLogDAO;

import DAO.Impl.AuditLogDAOImpl;

import Models.AuditLog;

import Utils.SessionUserHelper;



import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;



import java.io.IOException;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;

import java.util.List;



@WebServlet("/views/staff/examstaff/audit")

public class AuditServlet extends HttpServlet {



    private final AuditLogDAO logDAO = new AuditLogDAOImpl();



    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        HttpSession session = request.getSession();



        if ("true".equals(request.getParameter("exportExcel"))) {

            String filterDate = resolveFilterDate(request);

            String target = request.getContextPath() + "/views/staff/examstaff/audit-export";

            if (filterDate != null && !filterDate.isBlank()) {

                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);

            }

            response.sendRedirect(target);

            return;

        }



        int userId = SessionUserHelper.resolveUserId(session);

        String filterDate = resolveFilterDate(request);



        List<AuditLog> personalLogs = loadLogs(userId, filterDate);

        var procedureKpi = logDAO.getStaffProcedureKpi(userId, filterDate);



        int sessId = ExamStaffViewHelper.resolveSessionId(request, session, 2);

        String webRoot = request.getServletContext().getRealPath("/");

        ExamStaffViewHelper.ensureCandidateQueue(session, sessId, webRoot);



        request.setAttribute("personalLogs", personalLogs);

        request.setAttribute("myCompletedProcedures", procedureKpi.getCompletedCount());

        request.setAttribute("myTotalFees", procedureKpi.getTotalFees());



        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);

    }



    private static String resolveFilterDate(HttpServletRequest request) {

        String filterDate = request.getParameter("filterDate");

        if (filterDate == null || filterDate.isBlank()) {

            filterDate = request.getParameter("date");

        }

        return filterDate;

    }



    private List<AuditLog> loadLogs(int userId, String filterDate) {

        try {

            if (filterDate != null && !filterDate.trim().isEmpty()) {

                return logDAO.getLogsByUserAndDate(userId, filterDate);

            }

            return logDAO.getLogsByUserAndDate(userId, null);

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();

        }

    }

}

