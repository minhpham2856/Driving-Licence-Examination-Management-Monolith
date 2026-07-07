package controller.staff.exam;

import dao.FeeDAO;
import dao.PaymentDAO;
import dao.impl.FeeDAOImpl;
import dao.impl.PaymentDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.SessionDTO;
import model.Fee;
import model.Payment;
import util.ProcedureFeeTotals;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ReportExcelExporter {

    private static final PaymentDAO PAY_DAO = new PaymentDAOImpl();
    private static final FeeDAO FEE_DAO = new FeeDAOImpl();

    private ReportExcelExporter() {
    }

    // export exam report
    public static void exportExamReport(OutputStream out, SessionDTO session,
            List<ExamRegistrationDTO> candidates, ReportExportStats stats,
            List<Map<String, Object>> infractions, String exporterName) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = boldStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));
            // write overview sheet
            // write license sheet
            // write section sheet
            // write candidate sheet
            // write infraction sheet
            // write fee sheet

            writeOverviewSheet(workbook, headerStyle, session, stats, exporterName);
            writeLicenseSheet(workbook, headerStyle, candidates);
            writeSectionSheet(workbook, headerStyle, stats);
            writeCandidateSheet(workbook, headerStyle, dateStyle, candidates);
    // write overview sheet
            writeInfractionSheet(workbook, headerStyle, infractions);
            writeFeeSheet(workbook, headerStyle, candidates, session);

            workbook.write(out);
        }
    }

    private static void writeOverviewSheet(Workbook wb, CellStyle headerStyle,
            SessionDTO session, ReportExportStats stats, String exporterName) {
        Sheet sheet = wb.createSheet("Tổng quan");
        int row = 0;
        row = writeTitleBlock(sheet, row, headerStyle, "BÁO CÁO TỔNG HỢP CA THI");
        row = writeKv(sheet, row, "Ca thi", session != null ? session.getSessionName() : "");
        row = writeKv(sheet, row, "Ngày sát hạch", formatSqlDate(session != null ? session.getExamDate() : null));
        row = writeKv(sheet, row, "Tổng đăng ký", stats.totalCandidates);
        row = writeKv(sheet, row, "Đã thi xong", stats.examCompletedCount);
        row = writeKv(sheet, row, "Đạt", stats.passedCount);
        row = writeKv(sheet, row, "Chưa đạt", stats.failedCount);
    // write license sheet
        row = writeKv(sheet, row, "Vắng/đình chỉ", stats.absentCount);
        row = writeKv(sheet, row, "Tỷ lệ đạt (%)", round1(stats.passRate));
        row = writeKv(sheet, row, "Người xuất", exporterName != null ? exporterName : "");
        row = writeKv(sheet, row, "Thời gian xuất", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(new Date()));
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void writeLicenseSheet(Workbook wb, CellStyle headerStyle, List<ExamRegistrationDTO> candidates) {
        Sheet sheet = wb.createSheet("Theo hạng bằng");
        Row header = sheet.createRow(0);
        String[] cols = {"Hạng bằng", "Đăng ký", "Đã thi", "Đạt", "Chưa đạt", "Tỷ lệ đạt (%)"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        Map<String, LicenseAgg> agg = aggregateByLicense(candidates);
        int row = 1;
        for (Map.Entry<String, LicenseAgg> e : agg.entrySet()) {
            LicenseAgg a = e.getValue();
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(a.registered);
            r.createCell(2).setCellValue(a.completed);
            r.createCell(3).setCellValue(a.passed);
    // write section sheet
            r.createCell(4).setCellValue(a.failed);
            double rate = a.completed > 0 ? (a.passed * 100.0 / a.completed) : 0;
            r.createCell(5).setCellValue(round1(rate));
        }
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeSectionSheet(Workbook wb, CellStyle headerStyle, ReportExportStats stats) {
        Sheet sheet = wb.createSheet("Theo phần thi");
        Row header = sheet.createRow(0);
        String[] cols = {"Phần thi", "Tổng số thi", "Đạt", "Bị loại", "Tỷ lệ loại (%)"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        int row = 1;
    // write section row
        row = writeSectionRow(sheet, row, "Lý thuyết", stats.theoryCount, stats.theoryPassed, stats.theoryFailed);
        row = writeSectionRow(sheet, row, "Sa hình / Thực hành", stats.practicalCount, stats.practicalPassed, stats.practicalFailed);
        if (stats.roadCount > 0) {
            writeSectionRow(sheet, row, "Đường trường", stats.roadCount, stats.roadPassed, stats.roadFailed);
        }
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // write candidate sheet
    private static int writeSectionRow(Sheet sheet, int row, String name, int total, int passed, int failed) {
        Row r = sheet.createRow(row);
        r.createCell(0).setCellValue(name);
        r.createCell(1).setCellValue(total);
        r.createCell(2).setCellValue(passed);
        r.createCell(3).setCellValue(failed);
        double failRate = total > 0 ? (failed * 100.0 / total) : 0;
        r.createCell(4).setCellValue(round1(failRate));
        return row + 1;
    }

    private static void writeCandidateSheet(Workbook wb, CellStyle headerStyle, CellStyle dateStyle,
            List<ExamRegistrationDTO> candidates) {
        Sheet sheet = wb.createSheet("Danh sách kết quả");
        Row header = sheet.createRow(0);
        String[] cols = {
                "STT", "SBD", "Họ và tên", "Ngày sinh", "CCCD", "Hạng", "Phòng LT",
                "Điểm LT", "KQ LT", "Điểm SH", "KQ SH", "Điểm ĐT", "KQ ĐT",
                "KQ cuối", "Thu phí", "Ảnh", "Ghi chú"
        };
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        int row = 1;
        int stt = 1;
        int passCount = 0;
        int failCount = 0;
        int absentCount = 0;
        for (ExamRegistrationDTO reg : candidates) {
            Row r = sheet.createRow(row++);
            int col = 0;
            r.createCell(col++).setCellValue(stt++);
            r.createCell(col++).setCellValue(reg.getSbd());
            r.createCell(col++).setCellValue(nullToEmpty(reg.getName()));
            writeDateCell(r.createCell(col++), reg.getDob(), dateStyle);
            r.createCell(col++).setCellValue(nullToEmpty(reg.getCccd()));
            r.createCell(col++).setCellValue(nullToEmpty(reg.getClazz()));
            r.createCell(col++).setCellValue(nullToEmpty(reg.getAllocatedAreaName()));
            writeScoreCell(r.createCell(col++), reg.getTheoryScore());
            r.createCell(col++).setCellValue(ReportExportLabels.formatSectionResult(reg.getTheoryPassed()));
            writeScoreCell(r.createCell(col++), reg.getPracticalScore());
            r.createCell(col++).setCellValue(ReportExportLabels.formatSectionResult(reg.getPracticalPassed()));
            writeScoreCell(r.createCell(col++), reg.getRoadTestScore());
            r.createCell(col++).setCellValue(ReportExportLabels.formatSectionResult(reg.getRoadTestPassed()));
            String finalResult = ReportExportLabels.formatFinalResult(reg);
            r.createCell(col++).setCellValue(finalResult);
            r.createCell(col++).setCellValue(ReportExportLabels.yesNo(reg.isIsPaymentCompleted()));
            r.createCell(col++).setCellValue(ReportExportLabels.yesNo(reg.isValidCapturedPhoto()));
            r.createCell(col++).setCellValue(notesLabel(reg));

            if (reg.isAbsent()) {
                absentCount++;
            } else if (reg.isExamFinished()) {
                if (reg.isFinalPass()) {
                    passCount++;
                } else {
    // write infraction sheet
                    failCount++;
                }
            }
        }
        Row total = sheet.createRow(row + 1);
        total.createCell(0).setCellValue("Tổng hợp");
        total.createCell(1).setCellValue("Đạt: " + passCount + " | Chưa đạt: " + failCount + " | Vắng: " + absentCount);
        for (int i = 0; i < Math.min(cols.length, 12); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeInfractionSheet(Workbook wb, CellStyle headerStyle,
            List<Map<String, Object>> infractions) {
        Sheet sheet = wb.createSheet("Lỗi phổ biến");
        Row header = sheet.createRow(0);
        String[] cols = {"STT", "Lý do trừ điểm", "Số lần", "Tỷ lệ (%)"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        int row = 1;
        if (infractions != null) {
            int stt = 1;
    // write fee sheet
            for (Map<String, Object> inf : infractions) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(stt++);
                r.createCell(1).setCellValue(String.valueOf(inf.getOrDefault("reason", "")));
                r.createCell(2).setCellValue(toInt(inf.get("count")));
                r.createCell(3).setCellValue(round1(toDouble(inf.get("percentage"))));
            }
        }
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeFeeSheet(Workbook wb, CellStyle headerStyle,
            List<ExamRegistrationDTO> candidates, SessionDTO session) {
        Sheet sheet = wb.createSheet("Thu phí thủ tục");
        Row header = sheet.createRow(0);
        String[] cols = {
                "STT", "SBD", "Họ và tên", "Hạng", "Thời gian thu", "Hình thức",
                "Mã GD", "Chi tiết khoản thu", "Tổng (đồng)"
        };
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        int row = 1;
        int stt = 1;
        double grandTotal = 0;
        for (ExamRegistrationDTO reg : candidates) {
            if (reg.isAbsent() || !reg.isIsPaymentCompleted()) {
                continue;
            }
            Payment payment = PAY_DAO.getByCandidateId(reg.getId());
            if (payment == null || payment.getPaymentId() <= 0 || !isActiveProcedurePayment(payment)) {
                continue;
            }
            List<Fee> feeLines = FEE_DAO.getFeesByPaymentId(payment.getPaymentId());
            double lineTotal = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
            grandTotal += lineTotal;

            Row r = sheet.createRow(row++);
            int col = 0;
            r.createCell(col++).setCellValue(stt++);
            r.createCell(col++).setCellValue(reg.getSbd());
            r.createCell(col++).setCellValue(nullToEmpty(reg.getName()));
            r.createCell(col++).setCellValue(nullToEmpty(reg.getClazz()));
            if (payment.getPaidAt() != null) {
                r.createCell(col++).setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))
                        .format(payment.getPaidAt()));
            } else {
                r.createCell(col++).setBlank();
    // Kiem tra active procedure payment
            }
            r.createCell(col++).setCellValue(nullToEmpty(payment.getPaymentMethod()));
            r.createCell(col++).setCellValue(nullToEmpty(payment.getTransactionReference()));
            r.createCell(col++).setCellValue(formatFeeDetail(feeLines));
            r.createCell(col++).setCellValue(lineTotal);
        }
        Row totalRow = sheet.createRow(row + 1);
    // format fee detail
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.createCell(8).setCellValue(grandTotal);
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static boolean isActiveProcedurePayment(Payment payment) {
        String status = payment.getPaymentStatus();
        if (status == null) {
            return false;
        }
        return "Completed".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(status);
    // aggregate by license
    }

    private static String formatFeeDetail(List<Fee> feeLines) {
        if (feeLines == null || feeLines.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Fee f : feeLines) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(f.getFeeName()).append(": ").append((long) f.getAmount());
        }
        return sb.toString();
    }

    private static Map<String, LicenseAgg> aggregateByLicense(List<ExamRegistrationDTO> candidates) {
        Map<String, LicenseAgg> map = new LinkedHashMap<>();
        if (candidates == null) {
            return map;
        }
        for (ExamRegistrationDTO reg : candidates) {
            String lic = reg.getLicenseCode() != null ? reg.getLicenseCode().trim().toUpperCase(Locale.ROOT) : "N/A";
            LicenseAgg a = map.computeIfAbsent(lic, k -> new LicenseAgg());
            a.registered++;
            if (reg.isAbsent()) {
    // write title block
                a.completed++;
                a.failed++;
                continue;
            }
            if (!reg.isExamFinished()) {
                continue;
            }
    // write kv
            a.completed++;
            if (reg.isFinalPass()) {
                a.passed++;
            } else {
                a.failed++;
            }
        }
        return map;
    }

    // bold style
    private static int writeTitleBlock(Sheet sheet, int row, CellStyle headerStyle, String title) {
        Row r = sheet.createRow(row++);
        Cell c = r.createCell(0);
        c.setCellValue(title);
        c.setCellStyle(headerStyle);
        return row;
    }

    private static int writeKv(Sheet sheet, int row, String key, Object value) {
        Row r = sheet.createRow(row++);
        r.createCell(0).setCellValue(key);
        if (value instanceof Number) {
            r.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            r.createCell(1).setCellValue(value != null ? value.toString() : "");
        }
    // write score cell
        return row;
    }

    private static CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static void writeDateCell(Cell cell, java.sql.Date date, CellStyle dateStyle) {
        if (date == null) {
            cell.setBlank();
    // notes label
            return;
        }
        cell.setCellValue(new Date(date.getTime()));
        cell.setCellStyle(dateStyle);
    }

    private static void writeScoreCell(Cell cell, Integer score) {
        if (score == null) {
            cell.setBlank();
        } else {
    // null to empty
            cell.setCellValue(score);
        }
    }
    // round1

    private static String formatSqlDate(java.sql.Date date) {
        if (date == null) {
    // to int
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date);
    }

    private static String notesLabel(ExamRegistrationDTO reg) {
    // to double
        if (reg.isAbsent()) {
            return "Vắng/Đình chỉ";
        }
        String notes = reg.getNotes();
        if (notes != null && notes.startsWith("AllocatedRoom:")) {
            return "Đã phân phòng";
        }
        if (reg.getAllocatedAreaId() != null && reg.getAllocatedAreaId() > 0) {
            return "Đã phân phòng";
        }
        return "";
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static int toInt(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return 0;
    }

    private static double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return 0;
    }

    private static final class LicenseAgg {
        int registered;
        int completed;
        int passed;
        int failed;
    }
}
