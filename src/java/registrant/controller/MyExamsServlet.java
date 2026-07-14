package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantMyExamsService;
import registrant.service.impl.RegistrantMyExamsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registrant/my-exams")
public class MyExamsServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/my-exams.jsp";

    private final RegistrantMyExamsService myExamsService = new RegistrantMyExamsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        myExamsService.copyMyExamsToRequest(user, request, request.getParameter("examId"));

        if ("registered".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage",
                    "Đăng ký đợt thi thành công. Trạng thái hiện là Chờ xét duyệt - SBD sẽ được cập nhật sau khi Ban sát hạch duyệt và nhập danh sách chính thức.");
        }

        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        if (!"1".equals(request.getParameter("requestCancel"))) {
            response.sendRedirect(request.getContextPath() + "/registrant/my-exams");
            return;
        }

        String error = myExamsService.requestCancellation(user, request);
        if (error != null) {
            RegistrantServletSupport.setFlash(request.getSession(),
                    RegistrantMyExamsServiceImpl.FLASH_CANCEL_ERROR_ATTR, error);
        } else {
            RegistrantServletSupport.setFlash(request.getSession(),
                    RegistrantMyExamsServiceImpl.FLASH_CANCEL_SUCCESS_ATTR,
                    "Đã gửi yêu cầu hủy đăng ký. Ban quản lý sẽ xử lý - bạn có thể theo dõi trạng thái tại đây.");
        }
        response.sendRedirect(RegistrantMyExamsServiceImpl.buildMyExamsRedirect(request));
    }
}
