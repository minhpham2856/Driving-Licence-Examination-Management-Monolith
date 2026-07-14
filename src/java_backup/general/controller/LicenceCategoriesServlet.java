package general.controller;

import general.dto.ServiceResult;
import shared.model.Licence;
import general.service.impl.LicenseServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import general.service.LicenceService;

@WebServlet(name = "LicenceCategoriesServlet", urlPatterns = {"/public/licence-categories"})
public class LicenceCategoriesServlet extends HttpServlet {

    private final LicenceService generalService = new LicenseServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get licence categories
        ServiceResult<List<Licence>> result = generalService.getLicenceCategories();

        // validate result
        if (result.isSuccess()) {
            // set data to request
            request.setAttribute("categories", result.getData());
        } else {
            // set error to request
            request.setAttribute("error", result.getMessage());
        }

        // forward to view
        request.getRequestDispatcher("/views/public/license-categories.jsp").forward(request, response);
    }
}

