package examstaff.service.impl.support.audit;

import examstaff.dto.AuditDTO;
import examstaff.util.ExamStaffLabels;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Xuất nhật ký audit cá nhân của cán bộ ra file Excel (.xlsx).
 * <p>
 * Dùng Apache POI (XSSFWorkbook). Được AuditExportServlet gọi qua
 * AuditServiceImpl; không đọc DAO — nhận list AuditDTO đã tải sẵn.
 *
 * Cấu trúc workbook:
 * - Sheet <b>Tổng quan</b> — KPI: tên cán bộ, phạm vi ngày, tổng thao tác,
 *       số thí sinh hoàn tất thủ tục, tổng lệ phí, thời điểm xuất
 * - Sheet <b>Chi tiết nhật ký</b> — từng dòng: STT, Ngày, Giờ, Thời gian đầy đủ,
 *       Kiểu thao tác, Nghiệp vụ, Thao tác chi tiết (nhãn qua ExamStaffLabels)
 *
 * Định dạng:
 * Style ngày dd/MM/yyyy, giờ HH:mm:ss, datetime đầy đủ; header bold;
 * freeze pane hàng tiêu đề; auto-size cột.
 */
public class StaffAuditExportServiceImpl {

    /**
     * Xuất workbook Excel (sheet Tổng quan + Chi tiết nhật ký) ra stream.
     * @param out                  luồng ghi file
     * @param logs                 danh sách audit (có thể null)
     * @param completedProcedures  số thí sinh đã hoàn tất thủ tục
     * @param totalFees            tổng lệ phí đã thu (đồng)
     * @param staffName            tên cán bộ
     * @param filterDateLabel      nhãn phạm vi ngày lọc
     * @throws IOException nếu ghi workbook thất bại
     */
    public void exportAuditLog(OutputStream out, List<AuditDTO> logs, int completedProcedures,
            double totalFees, String staffName, String filterDateLabel) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Load: style dùng chung
            CellStyle headerStyle = boldStyle(workbook);
            CellStyle dayStyle = createDateStyle(workbook, "dd/MM/yyyy");
            CellStyle timeStyle = createDateStyle(workbook, "HH:mm:ss");
            CellStyle dateTimeStyle = createDateStyle(workbook, "dd/MM/yyyy HH:mm:ss");

            // Mutate: sheet tổng quan KPI
            Sheet overview = workbook.createSheet("Tổng quan");
            int row = 0;
            Row t = overview.createRow(row++);
            Cell tc = t.createCell(0);
            tc.setCellValue("NHẬT KÝ HOẠT ĐỘNG CÁ NHÂN");
            tc.setCellStyle(headerStyle);
            row = kv(overview, row, "Cán bộ", staffName);
            row = kv(overview, row, "Phạm vi", filterDateLabel);
            row = kv(overview, row, "Mẫu file", "Chi tiết nhật ký v2 (Ngày/Giờ/Kiểu thao tác/Thao tác)");
            row = kv(overview, row, "Tổng thao tác", logs != null ? logs.size() : 0);
            row = kv(overview, row, "Thí sinh đã làm thủ tục", completedProcedures);
            row = kv(overview, row, "Tổng lệ phí đã thu (đồng)", totalFees);
            kv(overview, row, "Xuất lúc",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(new Date()));
            overview.autoSizeColumn(0);
            overview.autoSizeColumn(1);

            // Mutate: sheet chi tiết từng dòng audit
            Sheet sheet = workbook.createSheet("Chi tiết nhật ký");
            Row header = sheet.createRow(0);
            String[] cols = {
                    "STT",
                    "Ngày",
                    "Giờ",
                    "Thời gian đầy đủ",
                    "Kiểu thao tác",
                    "Nghiệp vụ",
                    "Thao tác (chi tiết)"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            int stt = 1;
            if (logs != null) {
                for (AuditDTO log : logs) {
                    Row data = sheet.createRow(r++);
                    int col = 0;
                    data.createCell(col++).setCellValue(stt++);

                    if (log.getChangedAt() != null) {
                        Date when = new Date(log.getChangedAt().getTime());
                        Cell dayCell = data.createCell(col++);
                        dayCell.setCellValue(when);
                        dayCell.setCellStyle(dayStyle);

                        Cell timeCell = data.createCell(col++);
                        timeCell.setCellValue(when);
                        timeCell.setCellStyle(timeStyle);

                        Cell fullCell = data.createCell(col++);
                        fullCell.setCellValue(when);
                        fullCell.setCellStyle(dateTimeStyle);
                    } else {
                        data.createCell(col++).setBlank();
                        data.createCell(col++).setBlank();
                        data.createCell(col++).setBlank();
                    }

                    data.createCell(col++).setCellValue(ExamStaffLabels.formatActionType(log));
                    data.createCell(col++).setCellValue(ExamStaffLabels.formatEntityLabel(log.getTableName()));
                    data.createCell(col).setCellValue(ExamStaffLabels.formatOperationDetail(log));
                }
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);

            // Result: ghi ra OutputStream
            workbook.write(out);
        }
    }

    /**
     * Ghi một dòng khóa–giá trị trên sheet tổng quan.
     * @param sheet sheet đích
     * @param row   chỉ số hàng hiện tại
     * @param key   nhãn cột A
     * @param value giá trị cột B (Number hoặc toString)
     * @return chỉ số hàng tiếp theo
     */
    private static int kv(Sheet sheet, int row, String key, Object value) {
        Row r = sheet.createRow(row++);
        r.createCell(0).setCellValue(key);
        if (value instanceof Number) {
            r.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            r.createCell(1).setCellValue(value != null ? value.toString() : "");
        }
        return row;
    }

    /**
     * Tạo style chữ đậm cho tiêu đề.
     * @param wb workbook nguồn
     * @return CellStyle bold
     */
    private static CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * Tạo style định dạng ngày/giờ theo pattern.
     * @param wb      workbook nguồn
     * @param pattern pattern Excel (ví dụ dd/MM/yyyy)
     * @return CellStyle ngày/giờ
     */
    private static CellStyle createDateStyle(Workbook wb, String pattern) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(pattern));
        return style;
    }
}
