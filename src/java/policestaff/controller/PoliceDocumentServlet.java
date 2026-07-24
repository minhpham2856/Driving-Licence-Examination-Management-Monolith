package policestaff.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO.DocumentView;
import managingstaff.util.CloudinaryDocumentReader;
import policestaff.dao.PoliceSubmissionDAO;
import policestaff.dao.impl.PoliceSubmissionDAOImpl;

@WebServlet("/police/document-view")
public class PoliceDocumentServlet extends HttpServlet {
    private final PoliceSubmissionDAO policeDAO=new PoliceSubmissionDAOImpl();
    private final DossierDAO dossierDAO=new DossierDAOImpl();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        int id;try{id=Integer.parseInt(req.getParameter("id"));}catch(Exception ex){id=0;}
        if(id<=0||!policeDAO.canAccessDocument(id)){resp.sendError(404);return;}
        DocumentView d=dossierDAO.findDocumentById(id);
        if(d==null||d.getDocumentUrl()==null){unavailable(resp,"Tài liệu chưa có tệp đính kèm.");return;}
        resp.setHeader("Cache-Control","private, no-store, max-age=0");
        if(CloudinaryDocumentReader.supports(d.getDocumentUrl())){
            try{
                var resource=CloudinaryDocumentReader.read(d.getDocumentUrl());
                resp.setContentType(resource.contentType());
                resp.getOutputStream().write(resource.bytes());
            }catch(IOException ex){
                unavailable(resp,"Không thể tải tài liệu từ kho lưu trữ. Vui lòng thử lại.");
            }
            return;
        }
        String path=d.getDocumentUrl().startsWith("/")?d.getDocumentUrl():"/"+d.getDocumentUrl();
        try(InputStream in=getServletContext().getResourceAsStream(path)){
            if(in==null){unavailable(resp,"Tệp tài liệu không còn tồn tại trên hệ thống.");return;}
            String type=getServletContext().getMimeType(path);
            resp.setContentType(type==null?"application/octet-stream":type);
            in.transferTo(resp.getOutputStream());
        }
    }

    private static void unavailable(HttpServletResponse resp,String message)throws IOException{
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write("<!doctype html><html lang=\"vi\"><head><meta charset=\"UTF-8\">"
                +"<style>body{margin:0;font-family:Arial,sans-serif;background:#f8fafc;color:#475569;"
                +"display:grid;place-items:center;min-height:100vh}.box{text-align:center;padding:24px}"
                +"h2{color:#0f172a;margin:0 0 8px;font-size:18px}p{margin:0;line-height:1.5}</style>"
                +"</head><body><div class=\"box\"><h2>Không thể hiển thị tài liệu</h2><p>"
                +message+"</p></div></body></html>");
    }
}
