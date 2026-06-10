package Controllers.Examiner;

import Services.FileService;
import Services.Impl.FileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet("/examiner/export/candidates")
public class ExportCandidatesExcelServlet extends HttpServlet {

    private final FileService fileService = new FileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Tell the browser this is an .xlsx file to be downloaded (not displayed).
        //    The MIME type is the official one for Excel 2007+ spreadsheets.
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //    "attachment" triggers a download; "filename" is the suggested name.
        response.setHeader("Content-Disposition", "attachment; filename=\"danh-sach-thi-sinh.xlsx\"");

        // 2) Define the columns.
        List<String> headers = Arrays.asList("SBD", "Họ và tên", "Ngày sinh", "Số căn cước", "Địa chỉ");

        // 3) Build the rows. This sample data keeps the endpoint fully functional
        //    on its own; in production replace it with a DAO query, e.g.
        //        for (Person p : personDAO.getAll()) {
        //            rows.add(Arrays.asList(i++, p.getFullName(), p.getGovIdNo(), ...));
        //        }
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList(1, "Nguyễn Văn A", "01/01/2000", "0123456789", "Hà Nội"));
        rows.add(Arrays.asList(2, "Trần Thị B", "15/05/2001", "0987654321", "Hải Phòng"));
        rows.add(Arrays.asList(3, "Lê Văn C", "20/09/1999", "0112233445", "Đà Nẵng"));

        // 4) Stream the workbook straight to the response. We do NOT wrap this in
        //    try-with-resources because the servlet container owns the response
        //    stream; the service closes the workbook internally.
        OutputStream out = response.getOutputStream();
        fileService.exportToExcel("Danh sách thí sinh", headers, rows, out);
        out.flush();
    }
}
