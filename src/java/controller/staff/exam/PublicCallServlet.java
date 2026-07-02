package controller.staff.exam;

import controller.pub.PublicCallHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import util.Utf8EncodingHelper;

@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Utf8EncodingHelper.apply(request, response);

        PublicCallHelper.Snapshot snapshot = PublicCallHelper.loadSnapshot(request);
        int sessionId = snapshot.getSessionId();
        boolean hasSession = sessionId > 0;

        request.setAttribute("noActiveSession", !hasSession);
        request.setAttribute("sessionId", hasSession ? sessionId : null);
        request.setAttribute("currentSession", snapshot.getCurrentSession());
        request.setAttribute("callingCandidate", snapshot.getCallingCandidate());
        request.setAttribute("nextCandidate", snapshot.getNextCandidate());
        request.setAttribute("isCallingActive", snapshot.isCallingActive());
        request.setAttribute("shiftEnded", snapshot.isShiftEnded());
        request.setAttribute("waitingQueue", snapshot.getWaitingQueue());

        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
}
