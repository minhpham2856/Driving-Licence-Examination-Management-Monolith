package service;

import dto.xml.XmlExportDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

 // Service interface for file export operations.
public interface XmlService {

    // Writes a simple table (headers + rows) to an Excel worksheet on the given output stream
    void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException;

    // Writes a table with preamble header rows followed by column headers and data rows to Excel
    void exportToExcel(String sheetName, List<List<Object>> preambleRows, List<String> headers,
            List<List<Object>> rows, OutputStream out) throws IOException;

    // Serialises an XML document model to the given output stream
    void exportToXml(XmlExportDocument document, OutputStream out) throws IOException;
}
