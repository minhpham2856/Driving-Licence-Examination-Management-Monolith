package service;
import dto.XmlExportDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
public interface XmlService {
    void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException;
    void exportToExcel(String sheetName, List<List<Object>> preambleRows, List<String> headers,
            List<List<Object>> rows, OutputStream out) throws IOException;
    void exportToXml(XmlExportDocument document, OutputStream out) throws IOException;
}
