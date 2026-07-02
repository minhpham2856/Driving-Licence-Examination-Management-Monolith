package dto.examiner;

import dto.xml.XmlExportDocument;
import dto.xml.XmlExportTable;
import java.util.List;
import java.util.Map;

public record ExaminerExportPayload(
        String excelSheetName,
        String xmlRootElement,
        Map<String, Object> metadata,
        List<XmlExportTable> tables,
        List<List<Object>> excelPreambleRows) {

    public List<String> primaryHeaders() {
        return tables.isEmpty() ? List.of() : tables.get(0).headers();
    }

    public List<List<Object>> primaryRows() {
        return tables.isEmpty() ? List.of() : tables.get(0).rows();
    }

    public XmlExportDocument toXmlDocument() {
        return new XmlExportDocument(xmlRootElement, metadata, tables);
    }
}
