package Controllers.Public;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PublicCallHelper.Snapshot snapshot = PublicCallHelper.loadSnapshot(request);

        request.setAttribute("currentSession", snapshot.getCurrentSession());
        request.setAttribute("callingCandidate", snapshot.getCallingCandidate());
        request.setAttribute("nextCandidate", snapshot.getNextCandidate());
        request.setAttribute("isCallingActive", snapshot.isCallingActive());
        request.setAttribute("shiftEnded", snapshot.isShiftEnded());
        request.setAttribute("sessionId", snapshot.getSessionId());
        request.setAttribute("boardUpdatedAtMs", snapshot.getUpdatedAtMs());

        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
}
