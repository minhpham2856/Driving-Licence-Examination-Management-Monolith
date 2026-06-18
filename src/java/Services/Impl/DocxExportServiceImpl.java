package Services.Impl;

import Services.DocxExportService;
import Services.ExaminerViewDataService;
import DTOs.CandidateDTO;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocxExportServiceImpl implements DocxExportService {

    private final ExaminerViewDataService viewDataService;

    public DocxExportServiceImpl() {
        this.viewDataService = new ExaminerViewDataServiceImpl();
    }

    public DocxExportServiceImpl(ExaminerViewDataService viewDataService) {
        this.viewDataService = viewDataService;
    }

    @Override
    public String determineTemplateName(String documentType, String licenseClass) {
        if (documentType == null || documentType.isBlank() || licenseClass == null || licenseClass.isBlank()) {
            return null;
        }

        // Example mapping based on resources/docx-template/examiner contents
        // e.g. VB1(B1).docx, VB1(B-C1-C-D1-D2-D).docx
        String ucLicense = licenseClass.toUpperCase().trim();
        String ucType = documentType.toUpperCase().trim();
        
        String groupSuffix;
        if ("A1".equals(ucLicense) || "A".equals(ucLicense)) {
            groupSuffix = "(A1-A)";
        } else if ("B1".equals(ucLicense)) {
            groupSuffix = "(B1)";
        } else {
            // Includes B2, C1, C, D1, D2, D, etc.
            // B2 is commonly grouped with B-C1-C-D1-D2-D for standard form, though templates might vary
            // Adjust this logic when exact mappings are confirmed.
            groupSuffix = "(B-C1-C-D1-D2-D)";
        }

        return ucType + groupSuffix + ".docx";
    }

    @Override
    public InputStream getTemplateStream(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return null;
        }
        
        // This relies on the context class loader.
        // During web app execution, resources in WEB-INF/classes or packaged JARs can be read this way.
        // Assuming 'resources/docx-template/examiner' is mapped to classpath or accessed via ServletContext.
        // For a generic Servlet setup, this might need ServletContext.getResourceAsStream(),
        // so you might need to adjust this depending on how 'resources/' is deployed.
        
        String resourcePath = "/docx-template/examiner/" + templateName;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        
        // If not found in classpath, it might need to be resolved via absolute path or ServletContext
        if (is == null) {
            // System.err.println("Template not found: " + resourcePath);
        }
        
        return is;
    }

    @Override
    public byte[] cloneTemplate(String templateName) {
        try (InputStream is = getTemplateStream(templateName)) {
            if (is == null) return null;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > -1 ) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                return baos.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean generateCandidateDocument(String sbd, int sessionId, String documentType, OutputStream outputStream) {
        if (sbd == null || sbd.isBlank() || outputStream == null) {
            return false;
        }

        CandidateDTO candidate = viewDataService.findRegistration(sessionId, sbd);
        if (candidate == null) {
            return false;
        }

        String licenseClass = candidate.getLicenseCode() != null ? candidate.getLicenseCode().trim() : "B2"; 

        String templateName = determineTemplateName(documentType, licenseClass);
        if (templateName == null) {
            return false;
        }

        try (InputStream templateStream = getTemplateStream(templateName)) {
            if (templateStream == null) {
                return false;
            }

            try (XWPFDocument doc = new XWPFDocument(templateStream)) {
                Map<String, String> data = buildCandidateDataMap(candidate);
                replacePlaceholders(doc, data);
                doc.write(outputStream);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean batchGenerateDocuments(List<String> sbds, int sessionId, String documentType, OutputStream outputStream) {
        if (sbds == null || sbds.isEmpty() || outputStream == null) {
            return false;
        }

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (String sbd : sbds) {
                CandidateDTO candidate = viewDataService.findRegistration(sessionId, sbd);
                if (candidate == null) continue;

                String licenseClass = candidate.getLicenseCode() != null ? candidate.getLicenseCode().trim() : "B2";
                String templateName = determineTemplateName(documentType, licenseClass);
                if (templateName == null) continue;

                try (InputStream templateStream = getTemplateStream(templateName)) {
                    if (templateStream == null) continue;

                    try (XWPFDocument doc = new XWPFDocument(templateStream)) {
                        Map<String, String> data = buildCandidateDataMap(candidate);
                        replacePlaceholders(doc, data);
                        
                        ZipEntry entry = new ZipEntry(sbd + "_" + templateName);
                        zos.putNextEntry(entry);
                        doc.write(zos);
                        zos.closeEntry();
                    }
                }
            }
            zos.finish();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, String> buildCandidateDataMap(CandidateDTO candidate) {
        Map<String, String> data = new HashMap<>();
        // Assuming placeholders like <<FullName>>, <<SBD>>, etc.
        // You should match these exactly with how they appear in the Word templates.
        data.put("<<FullName>>", candidate.getFullName() != null ? candidate.getFullName() : "");
        data.put("<<SBD>>", candidate.getSbd() != null ? candidate.getSbd() : "");
        data.put("<<CCCD>>", candidate.getGovIdNo() != null ? candidate.getGovIdNo() : "");
        data.put("<<Address>>", candidate.getAddress() != null ? candidate.getAddress() : "");
        data.put("<<Sex>>", candidate.isGender() ? "Nữ" : "Nam");
        data.put("<<License>>", candidate.getLicenseCode() != null ? candidate.getLicenseCode() : "");
        data.put("<<Reason>>", candidate.getReasonForTaking() != null ? candidate.getReasonForTaking() : "");
        
        if (candidate.getDateOfBirth() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            data.put("<<DOB>>", sdf.format(candidate.getDateOfBirth()));
        } else {
            data.put("<<DOB>>", "");
        }
        
        data.put("<<TheoryScore>>", candidate.getTheoryScore() != null ? String.valueOf(candidate.getTheoryScore()) : "");
        data.put("<<TheoryResult>>", candidate.getTheoryPassed() != null ? candidate.getTheoryPassed() : "");
        data.put("<<PracticalScore>>", candidate.getPracticalScore() != null ? String.valueOf(candidate.getPracticalScore()) : "");
        data.put("<<PracticalResult>>", candidate.getPracticalPassed() != null ? candidate.getPracticalPassed() : "");
        data.put("<<RoadScore>>", candidate.getRoadTestScore() != null ? String.valueOf(candidate.getRoadTestScore()) : "");
        data.put("<<RoadResult>>", candidate.getRoadTestPassed() != null ? candidate.getRoadTestPassed() : "");
        
        java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        data.put("<<Date>>", dateFmt.format(new java.util.Date()));
        data.put("<<Organization>>", "CƠ QUAN QUẢN LÝ SÁT HẠCH");
        data.put("<<Department>>", "SỞ GIAO THÔNG VẬN TẢI");
        
        return data;
    }

    private void replacePlaceholders(XWPFDocument doc, Map<String, String> data) {
        // Replace in paragraphs
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, data);
        }
        // Replace in tables
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, data);
                    }
                }
            }
        }
    }

    private void replaceInParagraph(XWPFParagraph p, Map<String, String> data) {
        List<XWPFRun> runs = p.getRuns();
        if (runs != null) {
            for (XWPFRun r : runs) {
                String text = r.getText(0);
                if (text != null) {
                    boolean changed = false;
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        if (text.contains(entry.getKey())) {
                            text = text.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
                            changed = true;
                        }
                    }
                    if (changed) {
                        r.setText(text, 0);
                    }
                }
            }
        }
    }
}
