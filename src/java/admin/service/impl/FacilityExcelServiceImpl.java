package admin.service.impl;

import admin.model.AreaView;
import admin.model.DeviceView;
import admin.model.ZoneView;
import admin.service.FacilityExcelService;
import admin.util.ExcelKit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FacilityExcelServiceImpl implements FacilityExcelService {

    private static final String[] ROOM_IMPORT_HEADERS = {
        "Khu vực thi *", "Tên phòng/sân *", "Loại *", "Sức chứa", "Địa điểm *"
    };
    private static final String[] ROOM_EXPORT_HEADERS = {
        "STT", "Mã", "Tên phòng/sân", "Loại", "Khu vực thi", "Sức chứa", "Địa điểm", "Số máy thi"
    };

    private static final String[] DEVICE_IMPORT_HEADERS = {
        "Khu vực thi *", "Phòng/sân thi *", "Tên máy/thiết bị *", "Loại thiết bị *", "Trạng thái"
    };
    private static final String[] DEVICE_EXPORT_HEADERS = {
        "STT", "Mã", "Tên máy/thiết bị", "Loại thiết bị", "Phòng/sân thi", "Khu vực thi", "Trạng thái"
    };

    // ============================================================ PHÒNG THI

    @Override
    public void writeRoomTemplate(List<ZoneView> zones, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Danh sách phòng thi");
            String note = "Điền dữ liệu từ dòng 4. Cột có dấu * là bắt buộc. "
                    + "Khu vực thi phải trùng tên đã có: " + zoneNames(zones) + ". "
                    + "Loại hợp lệ: Phòng thủ tục / Phòng thi / Sân thi. Sức chứa để trống nếu không áp dụng.";
            String[] sample = {
                zones != null && !zones.isEmpty() ? zones.get(0).getZoneName() : "Khu vực A",
                "Phòng thi số 1", "Phòng thi", "30", "Tầng 2, nhà B"
            };
            writeTemplateSheet(wb, sheet, "BIỂU MẪU IMPORT PHÒNG / SÂN THI", note, ROOM_IMPORT_HEADERS, sample);
            wb.write(out);
        }
    }

    @Override
    public void writeRooms(List<AreaView> rooms, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Phòng thi");
            CellStyle text = ExcelKit.dataStyle(wb, false);
            CellStyle center = ExcelKit.dataStyle(wb, true);
            int rowIndex = writeExportHeader(wb, sheet, "DANH SÁCH PHÒNG / SÂN THI", ROOM_EXPORT_HEADERS);

            int stt = 1;
            if (rooms != null) {
                for (AreaView a : rooms) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(24);
                    ExcelKit.writeCell(row, 0, stt++, center);
                    ExcelKit.writeCell(row, 1, a.getCode(), center);
                    ExcelKit.writeCell(row, 2, ExcelKit.safe(a.getAreaName()), text);
                    ExcelKit.writeCell(row, 3, ExcelKit.safe(a.getAreaType()), center);
                    ExcelKit.writeCell(row, 4, ExcelKit.safe(a.getZoneName()), text);
                    ExcelKit.writeCell(row, 5, a.getCapacityText(), center);
                    ExcelKit.writeCell(row, 6, ExcelKit.safe(a.getLocation()), text);
                    ExcelKit.writeCell(row, 7, a.getDeviceCount(), center);
                }
            }
            ExcelKit.autoSize(sheet, ROOM_EXPORT_HEADERS.length);
            wb.write(out);
        }
    }

    @Override
    public List<RoomRow> readRoomImport(InputStream in) throws IOException {
        List<RoomRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return rows;
            int header = ExcelKit.findHeaderRow(sheet, "khu vực thi");
            for (int i = header + 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                RoomRow row = new RoomRow();
                row.rowNumber = i + 1;
                row.zoneName = ExcelKit.str(r, 0);
                row.areaName = ExcelKit.str(r, 1);
                row.areaType = ExcelKit.str(r, 2);
                row.capacity = ExcelKit.str(r, 3);
                row.location = ExcelKit.str(r, 4);
                if (!row.isBlank()) rows.add(row);
            }
        }
        return rows;
    }

    // ============================================================= MÁY THI

    @Override
    public void writeDeviceTemplate(List<AreaView> areas, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Danh sách máy thi");
            String note = "Điền dữ liệu từ dòng 4. Cột có dấu * là bắt buộc. "
                    + "Khu vực thi và Phòng/sân thi phải trùng tên đã có trong hệ thống "
                    + "(xem sheet \"Phòng sân hiện có\"). "
                    + "Loại thiết bị hợp lệ: Máy tính / Mô tô / Mô tô ba bánh. "
                    + "Trạng thái: Hoạt động / Bảo trì (bỏ trống = Hoạt động).";
            String[] sample = {
                areas != null && !areas.isEmpty() ? ExcelKit.safe(areas.get(0).getZoneName()) : "Khu vực A",
                areas != null && !areas.isEmpty() ? ExcelKit.safe(areas.get(0).getAreaName()) : "Phòng thi số 1",
                "Máy 01", "Máy tính", "Hoạt động"
            };
            writeTemplateSheet(wb, sheet, "BIỂU MẪU IMPORT MÁY / THIẾT BỊ THI", note, DEVICE_IMPORT_HEADERS, sample);

            // Sheet tham chiếu: cặp Khu vực - Phòng/sân đang có, để Admin copy cho đúng
            Sheet ref = wb.createSheet("Phòng sân hiện có");
            CellStyle headerStyle = ExcelKit.headerStyle(wb);
            CellStyle text = ExcelKit.dataStyle(wb, false);
            Row h = ref.createRow(0);
            h.setHeightInPoints(26);
            ExcelKit.writeCell(h, 0, "Khu vực thi", headerStyle);
            ExcelKit.writeCell(h, 1, "Phòng/sân thi", headerStyle);
            ExcelKit.writeCell(h, 2, "Loại", headerStyle);
            int i = 1;
            if (areas != null) {
                for (AreaView a : areas) {
                    Row r = ref.createRow(i++);
                    ExcelKit.writeCell(r, 0, ExcelKit.safe(a.getZoneName()), text);
                    ExcelKit.writeCell(r, 1, ExcelKit.safe(a.getAreaName()), text);
                    ExcelKit.writeCell(r, 2, ExcelKit.safe(a.getAreaType()), text);
                }
            }
            ExcelKit.autoSize(ref, 3);

            wb.write(out);
        }
    }

    @Override
    public void writeDevices(List<DeviceView> devices, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Máy thi");
            CellStyle text = ExcelKit.dataStyle(wb, false);
            CellStyle center = ExcelKit.dataStyle(wb, true);
            int rowIndex = writeExportHeader(wb, sheet, "DANH SÁCH MÁY / THIẾT BỊ THI", DEVICE_EXPORT_HEADERS);

            int stt = 1;
            if (devices != null) {
                for (DeviceView d : devices) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(24);
                    ExcelKit.writeCell(row, 0, stt++, center);
                    ExcelKit.writeCell(row, 1, d.getCode(), center);
                    ExcelKit.writeCell(row, 2, ExcelKit.safe(d.getDeviceName()), text);
                    ExcelKit.writeCell(row, 3, ExcelKit.safe(d.getDeviceType()), center);
                    ExcelKit.writeCell(row, 4, ExcelKit.safe(d.getAreaName()), text);
                    ExcelKit.writeCell(row, 5, ExcelKit.safe(d.getZoneName()), text);
                    ExcelKit.writeCell(row, 6, d.getStatusText(), center);
                }
            }
            ExcelKit.autoSize(sheet, DEVICE_EXPORT_HEADERS.length);
            wb.write(out);
        }
    }

    @Override
    public List<DeviceRow> readDeviceImport(InputStream in) throws IOException {
        List<DeviceRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return rows;
            int header = ExcelKit.findHeaderRow(sheet, "khu vực thi");
            for (int i = header + 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                DeviceRow row = new DeviceRow();
                row.rowNumber = i + 1;
                row.zoneName = ExcelKit.str(r, 0);
                row.areaName = ExcelKit.str(r, 1);
                row.deviceName = ExcelKit.str(r, 2);
                row.deviceType = ExcelKit.str(r, 3);
                row.status = ExcelKit.str(r, 4);
                if (!row.isBlank()) rows.add(row);
            }
        }
        return rows;
    }

    // ============================================================== helpers

    /** Dựng phần đầu biểu mẫu import: tiêu đề, ghi chú, header, 1 dòng ví dụ. */
    private void writeTemplateSheet(Workbook wb, Sheet sheet, String title, String note,
                                    String[] headers, String[] sample) {
        sheet.setDisplayGridlines(false);

        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(28);
        Cell t = titleRow.createCell(0);
        t.setCellValue(title);
        t.setCellStyle(ExcelKit.titleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

        Row noteRow = sheet.createRow(1);
        noteRow.setHeightInPoints(34);
        Cell n = noteRow.createCell(0);
        n.setCellValue(note);
        n.setCellStyle(ExcelKit.noteStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

        CellStyle headerStyle = ExcelKit.headerStyle(wb);
        Row header = sheet.createRow(2);
        header.setHeightInPoints(30);
        for (int i = 0; i < headers.length; i++) {
            ExcelKit.writeCell(header, i, headers[i], headerStyle);
        }

        CellStyle sampleStyle = ExcelKit.dataStyle(wb, false);
        Row sampleRow = sheet.createRow(3);
        for (int i = 0; i < sample.length && i < headers.length; i++) {
            ExcelKit.writeCell(sampleRow, i, sample[i], sampleStyle);
        }

        ExcelKit.autoSize(sheet, headers.length);
    }

    /** Dựng tiêu đề + header cho file export; trả về chỉ số dòng bắt đầu ghi dữ liệu. */
    private int writeExportHeader(Workbook wb, Sheet sheet, String title, String[] headers) {
        sheet.setDisplayGridlines(false);
        sheet.createFreezePane(0, 3);
        sheet.getPrintSetup().setLandscape(true);
        sheet.setFitToPage(true);

        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        Cell t = titleRow.createCell(0);
        t.setCellValue(title);
        t.setCellStyle(ExcelKit.titleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

        CellStyle headerStyle = ExcelKit.headerStyle(wb);
        Row header = sheet.createRow(2);
        header.setHeightInPoints(28);
        for (int i = 0; i < headers.length; i++) {
            ExcelKit.writeCell(header, i, headers[i], headerStyle);
        }
        return 3;
    }

    private String zoneNames(List<ZoneView> zones) {
        if (zones == null || zones.isEmpty()) return "(chưa có khu vực thi nào — hãy tạo trước)";
        Set<String> names = new LinkedHashSet<>();
        for (ZoneView z : zones) {
            if (z.getZoneName() != null) names.add(z.getZoneName().trim());
        }
        return String.join(", ", names);
    }
}
