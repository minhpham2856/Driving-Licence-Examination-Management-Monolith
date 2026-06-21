package Services.Impl;

import Services.DocxExportService;
import Services.ExaminerViewDataService;
import DAOs.ExamSessionDAO;
import DAOs.Impl.ExamSessionDAOImpl;
import DTOs.CandidateDTO;
import DTOs.SessionDTO;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocxExportServiceImpl implements DocxExportService {

    private final ExaminerViewDataService viewDataService;
    private final ExamSessionDAO sessionDAO;

    public DocxExportServiceImpl() {
        this.viewDataService = new ExaminerViewDataServiceImpl();
        this.sessionDAO = new ExamSessionDAOImpl();
    }

    public DocxExportServiceImpl(ExaminerViewDataService viewDataService) {
        this.viewDataService = viewDataService;
        this.sessionDAO = new ExamSessionDAOImpl();
    }

    public DocxExportServiceImpl(ExaminerViewDataService viewDataService, ExamSessionDAO sessionDAO) {
        this.viewDataService = viewDataService;
        this.sessionDAO = sessionDAO;
    }

    @Override
    public String determineTemplateName(String documentType, String licenseClass) {
        if (documentType == null || documentType.isBlank() || licenseClass == null || licenseClass.isBlank()) {
            return null;
        }

        String ucLicense = licenseClass.toUpperCase().trim();
        String ucType = documentType.toUpperCase().trim();

        if (!ucType.startsWith("BB")) {
            return null;
        }

        String groupSuffix;
        if ("A1".equals(ucLicense) || "A".equals(ucLicense)) {
            if ("BB2".equals(ucType)) {
                groupSuffix = "(A1-A)";
            } else if ("BB1".equals(ucType)) {
                groupSuffix = "(A1-A-B1)";
            } else {
                return null;
            }
        } else if ("B1".equals(ucLicense)) {
            if ("BB2".equals(ucType)) {
                groupSuffix = "(B1)";
            } else if ("BB1".equals(ucType)) {
                groupSuffix = "(A1-A-B1)";
            } else {
                return null;
            }
        } else {
            groupSuffix = "(B-C1-C-D1-D2-D)";
        }

        return ucType + groupSuffix + ".docx";
    }

    @Override
    public InputStream getTemplateStream(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return null;
        }
        String resourcePath = "/docx-template/examiner/" + templateName;
        return getClass().getResourceAsStream(resourcePath);
    }

    @Override
    public byte[] cloneTemplate(String templateName) {
        try (InputStream is = getTemplateStream(templateName)) {
            if (is == null) return null;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > -1) {
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
                Map<String, String> data = buildCandidateDataMap(candidate, documentType, sessionId);
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
                        Map<String, String> data = buildCandidateDataMap(candidate, documentType, sessionId);
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

    private Map<String, String> buildCandidateDataMap(CandidateDTO candidate, String documentType, int sessionId) {
        Map<String, String> data = new HashMap<>();

        data.put("<<FNAME>>", candidate.getFullName() != null ? candidate.getFullName() : "");
        data.put("<<CNO>>", candidate.getSbd() != null ? candidate.getSbd() : "");
        data.put("<<IDNO>>", candidate.getGovIdNo() != null ? candidate.getGovIdNo() : "");
        data.put("<<CLASS>>", candidate.getLicenseCode() != null ? candidate.getLicenseCode() : "");

        if (candidate.getDateOfBirth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            data.put("<<DOB>>", sdf.format(candidate.getDateOfBirth()));
        } else {
            data.put("<<DOB>>", "");
        }

        String upperType = documentType != null ? documentType.toUpperCase().trim() : "";
        Integer score = null;
        String result = null;

        if ("BB1".equals(upperType)) {
            score = candidate.getTheoryScore();
            result = normalizeResult(candidate.getTheoryPassed());
            data.put("<<VNO>>", "");
        } else if ("BB2".equals(upperType)) {
            score = candidate.getPracticalScore();
            result = normalizeResult(candidate.getPracticalPassed());
            data.put("<<VNO>>", candidate.getDeviceCode() != null ? candidate.getDeviceCode() : "");
        } else if ("BB3".equals(upperType)) {
            score = candidate.getRoadTestScore();
            result = normalizeResult(candidate.getRoadTestPassed());
            data.put("<<VNO>>", candidate.getDeviceCode() != null ? candidate.getDeviceCode() : "");
        } else {
            data.put("<<VNO>>", "");
        }

        String scoreStr = score != null ? String.valueOf(score) : "";
        String resultStr = result != null && !"none".equalsIgnoreCase(result) ? result : "";

        data.put("<<A>>", scoreStr);
        data.put("<<B>>", resultStr);
        data.put("<<SCORE>>", scoreStr);

        boolean isPass = "Đạt".equals(resultStr);
        data.put("<<P>>", isPass ? "Đạt" : "");
        data.put("<<F>>", isPass ? "" : "Không đạt");

        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateTimeFmt = new SimpleDateFormat("HH:mm dd/MM/yyyy");

        SessionDTO sess = null;
        try {
            sess = sessionDAO.getById(sessionId);
        } catch (Exception e) {
            // fallback: session not found, use today's date
        }

        if (sess != null && sess.getExamDate() != null) {
            data.put("<<DATE>>", dateFmt.format(sess.getExamDate()));
            if (sess.getShiftStartTime() != null) {
                java.util.Date startDt = new java.util.Date(sess.getExamDate().getTime() + sess.getShiftStartTime().getTime());
                data.put("<<START>>", dateTimeFmt.format(startDt));
            } else {
                data.put("<<START>>", dateFmt.format(sess.getExamDate()));
            }
            if (sess.getShiftEndTime() != null) {
                java.util.Date endDt = new java.util.Date(sess.getExamDate().getTime() + sess.getShiftEndTime().getTime());
                data.put("<<END>>", dateTimeFmt.format(endDt));
            } else {
                data.put("<<END>>", dateFmt.format(sess.getExamDate()));
            }
        } else {
            data.put("<<DATE>>", dateFmt.format(new java.util.Date()));
            data.put("<<START>>", dateTimeFmt.format(new java.util.Date()));
            data.put("<<END>>", dateTimeFmt.format(new java.util.Date()));
        }

        data.put("<<DEPT>>", "SỞ GIAO THÔNG VẬN TẢI");
        data.put("<<EXAM>>", "");
        data.put("<<<EXAM>>", "");
        data.put("<<TIME>>", "");

        data.put("<<PIC>>", "");
        data.put("<<TAKENO>>", "1");

        Random rand = new Random();
        data.put("<<RAND1>>", String.format("%04d", rand.nextInt(10000)));
        data.put("<<RAND2>>", String.format("%04d", rand.nextInt(10000)));
        data.put("<<RAND3>>", String.format("%04d", rand.nextInt(10000)));

        return data;
    }

    private String normalizeResult(String result) {
        if (result == null || result.isBlank() || "none".equalsIgnoreCase(result)) {
            return null;
        }
        if ("true".equalsIgnoreCase(result) || "passed".equalsIgnoreCase(result)
                || "yes".equalsIgnoreCase(result) || "1".equals(result)) {
            return "Đạt";
        }
        if ("false".equalsIgnoreCase(result) || "failed".equalsIgnoreCase(result)
                || "no".equalsIgnoreCase(result) || "0".equals(result)) {
            return "Không đạt";
        }
        return result;
    }

    private void replacePlaceholders(XWPFDocument doc, Map<String, String> data) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, data);
        }
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
        String fullText = p.getText();
        boolean hasAny = false;
        for (String key : data.keySet()) {
            if (fullText.contains(key)) {
                hasAny = true;
                break;
            }
        }
        if (!hasAny) return;

        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        XWPFRun firstRun = null;
        for (XWPFRun r : runs) {
            if (firstRun == null) firstRun = r;
            String t = r.getText(0);
            if (t != null) sb.append(t);
        }

        String text = sb.toString();
        boolean changed = false;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
                changed = true;
            }
        }

        if (changed && firstRun != null) {
            firstRun.setText(text, 0);
            for (int i = 1; i < runs.size(); i++) {
                runs.get(i).setText("", 0);
            }
        }
    }
}
