package examstaff.service.impl;

import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ReportPaymentSummaryDTO;
import examstaff.service.ReportFeeQueryService;
import examstaff.service.StaffReportExportService;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamSummaryDTO;
import shared.model.Fee;
import shared.model.Payment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import examstaff.util.ReportExportLabels;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Implementation: xuất báo cáo kỳ thi ra file Excel (Apache POI). */
public class StaffReportExportServiceImpl implements StaffReportExportService {

    private final ReportFeeQueryService feeLookup = new ReportFeeQueryServiceImpl();

    /**
     * Ghi báo cáo kỳ thi ra luồng xuất (ví dụ PDF/Excel tùy triển khai).
     *
     * @param out          luồng ghi file
     * @param exam         thông tin tóm tắt kỳ thi
     * @param candidates   danh sách thí sinh trong báo cáo
     * @param stats        thống kê đã tính sẵn
     * @param exporterName tên người xuất báo cáo
     * @throws IOException nếu ghi file thất bại
     */
    @Override
    public void exportExamReport(OutputStream out, ExamSummaryDTO exam,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException {
        List<Map<String, Object>> infractions = stats != null ? stats.getInfractions() : null;

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

            writeOverviewSheet(workbook, headerStyle, exam, stats, exporterName);
            writeLicenseSheet(workbook, headerStyle, stats);
            writeSectionSheet(workbook, headerStyle, stats);
            writeCandidateSheet(workbook, headerStyle, dateStyle, candidates);
    // write overview sheet
            writeInfractionSheet(workbook, headerStyle, infractions);
            writeFeeSheet(workbook, headerStyle, candidates, exam);

            workbook.write(out);
        }
    }

    /** Sheet tổng quan ca thi và thống kê. */
    private void writeOverviewSheet(Workbook wb, CellStyle headerStyle,
            ExamSummaryDTO exam, ExamReportStatsDTO stats, String exporterName) {
        Sheet sheet = wb.createSheet("Tổng quan");
        int row = 0;
        row = writeTitleBlock(sheet, row, headerStyle, "BÁO CÁO TỔNG HỢP CA THI");
        row = writeKv(sheet, row, "Ca thi", exam != null ? exam.getExamName() : "");
        row = writeKv(sheet, row, "Ngày sát hạch", formatSqlDate(exam != null ? exam.getExamDate() : null));
        row = writeKv(sheet, row, "Tổng đăng ký", stats != null ? stats.getTotalCandidates() : 0);
        row = writeKv(sheet, row, "Đã thi xong", stats != null ? stats.getExamCompletedCount() : 0);
        row = writeKv(sheet, row, "Đạt", stats != null ? stats.getPassedCount() : 0);
        row = writeKv(sheet, row, "Trượt", stats != null ? stats.getFailedCount() : 0);
        row = writeKv(sheet, row, "Vắng", stats != null ? stats.getAbsentCount() : 0);
        row = writeKv(sheet, row, "Đình chỉ", stats != null ? stats.getSuspendedCount() : 0);
        row = writeKv(sheet, row, "Tỷ lệ đạt (%)", round1(stats != null ? stats.getPassRate() : 0));
        row = writeKv(sheet, row, "Người xuất", exporterName != null ? exporterName : "");
        row = writeKv(sheet, row, "Thời gian xuất", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(new Date()));
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /** Sheet thống kê theo hạng bằng. */
    private static void writeLicenseSheet(Workbook wb, CellStyle headerStyle, ExamReportStatsDTO stats) {
        Sheet sheet = wb.createSheet("Theo hạng bằng");
        Row header = sheet.createRow(0);
        String[] cols = {"Hạng bằng", "Đăng ký", "Đã thi", "Đạt", "Trượt", "Tỷ lệ đạt (%)"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        List<Map<String, Object>> licenseStats = stats != null ? stats.getLicenseStats() : null;
        int row = 1;
        if (licenseStats != null) {
            for (Map<String, Object> lic : licenseStats) {
                int completed = toInt(lic.get("completed"));
                int passed = toInt(lic.get("passed"));
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(String.valueOf(lic.getOrDefault("code", "")));
                r.createCell(1).setCellValue(toInt(lic.get("registered")));
                r.createCell(2).setCellValue(completed);
                r.createCell(3).setCellValue(passed);
                r.createCell(4).setCellValue(toInt(lic.get("failed")));
                double rate = completed > 0 ? (passed * 100.0 / completed) : 0;
                r.createCell(5).setCellValue(round1(rate));
            }
        }
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** Sheet thống kê theo phần thi. */
    private void writeSectionSheet(Workbook wb, CellStyle headerStyle, ExamReportStatsDTO stats) {
        Sheet sheet = wb.createSheet("Theo phần thi");
        Row header = sheet.createRow(0);
        String[] cols = {"Phần thi", "Tổng số thi", "Đạt", "Bị loại", "Tỷ lệ loại (%)"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }
        int row = 1;
        if (stats != null) {
            row = writeSectionRow(sheet, row, "Lý thuyết", stats.getTheoryCount(), stats.getTheoryPassed(), stats.getTheoryFailed());
            row = writeSectionRow(sheet, row, "Sa hình / Thực hành", stats.getPracticalCount(), stats.getPracticalPassed(), stats.getPracticalFailed());
        }
        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // write candidate sheet
    /** Ghi một dòng phần thi (LT / TH). */
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

    /** Sheet danh sách kết quả từng thí sinh. */
    private static void writeCandidateSheet(Workbook wb, CellStyle headerStyle, CellStyle dateStyle,
            List<ExamRegistrationDTO> candidates) {
        Sheet sheet = wb.createSheet("Danh sách kết quả");
        Row header = sheet.createRow(0);
        String[] cols = {
                "STT", "SBD", "Họ và tên", "Ngày sinh", "CCCD", "Hạng", "Phòng LT",
                "Điểm LT", "KQ LT", "Điểm SH", "KQ SH",
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
        int suspendedCount = 0;
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
            if (reg.skipsTheory()) {
                r.createCell(col++).setBlank();
                r.createCell(col++).setCellValue(ReportExportLabels.formatTheoryResult(reg));
            } else {
                writeScoreCell(r.createCell(col++), reg.getTheoryScore());
                r.createCell(col++).setCellValue(ReportExportLabels.formatTheoryResult(reg));
            }
            if (reg.skipsPractical()) {
                r.createCell(col++).setBlank();
                r.createCell(col++).setCellValue(ReportExportLabels.formatPracticalResult(reg));
            } else {
                writeScoreCell(r.createCell(col++), reg.getPracticalScore());
                r.createCell(col++).setCellValue(ReportExportLabels.formatPracticalResult(reg));
            }
            String finalResult = ReportExportLabels.formatFinalResult(reg);
            r.createCell(col++).setCellValue(finalResult);
            r.createCell(col++).setCellValue(ReportExportLabels.yesNo(reg.isIsPaymentCompleted()));
            r.createCell(col++).setCellValue(ReportExportLabels.yesNo(reg.isValidCapturedPhoto()));
            r.createCell(col++).setCellValue(notesLabel(reg));

            if (reg.isSuspended()) {
                suspendedCount++;
            } else if (reg.isAbsent()) {
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
        total.createCell(1).setCellValue("Đạt: " + passCount + " | Trượt: " + failCount
                + " | Vắng: " + absentCount + " | Đình chỉ: " + suspendedCount);
        for (int i = 0; i < Math.min(cols.length, 12); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** Sheet lỗi phổ biến / lý do trừ điểm. */
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

    /** Sheet thu phí thủ tục. */
    private void writeFeeSheet(Workbook wb, CellStyle headerStyle,
            List<ExamRegistrationDTO> candidates, ExamSummaryDTO exam) {
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
            ReportPaymentSummaryDTO summary = feeLookup.findPaymentSummary(reg.getId());
            Payment payment = summary.getPayment();
            if (payment == null || payment.getPaymentId() <= 0) {
                continue;
            }
            List<Fee> feeLines = summary.getFeeLines();
            double lineTotal = summary.getLineTotal();
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

    /** Ghép chi tiết các khoản phí thành chuỗi. */
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

    // bold style
    /** Ghi tiêu đề đậm ở đầu sheet. */
    private static int writeTitleBlock(Sheet sheet, int row, CellStyle headerStyle, String title) {
        Row r = sheet.createRow(row++);
        Cell c = r.createCell(0);
        c.setCellValue(title);
        c.setCellStyle(headerStyle);
        return row;
    }

    /** Ghi một cặp khóa-giá trị. */
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

    /** CellStyle font đậm. */
    private static CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /** Ghi ô ngày sinh / ngày tháng. */
    private static void writeDateCell(Cell cell, java.sql.Date date, CellStyle dateStyle) {
        if (date == null) {
            cell.setBlank();
    // notes label
            return;
        }
        cell.setCellValue(new Date(date.getTime()));
        cell.setCellStyle(dateStyle);
    }

    /** Ghi ô điểm hoặc để trống nếu null. */
    private static void writeScoreCell(Cell cell, Integer score) {
        if (score == null) {
            cell.setBlank();
        } else {
    // null to empty
            cell.setCellValue(score);
        }
    }
    // round1

    /** Định dạng java.sql.Date thành dd/MM/yyyy. */
    private static String formatSqlDate(java.sql.Date date) {
        if (date == null) {
    // to int
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date);
    }

    /** Nhãn ghi chú cột (đình chỉ / vắng / đã phân phòng). */
    private static String notesLabel(ExamRegistrationDTO reg) {
    // to double
        if (reg.isSuspended()) {
            return "Đình chỉ";
        }
        if (reg.isAbsent()) {
            return "Vắng";
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

    /** Chuỗi null thành rỗng. */
    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    /** Làm tròn 1 chữ số thập phân. */
    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /** Ép Object Number thành int. */
    private static int toInt(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return 0;
    }

    /** Ép Object Number thành double. */
    private static double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return 0;
    }

}
