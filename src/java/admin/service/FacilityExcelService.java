package admin.service;

import admin.model.AreaView;
import admin.model.DeviceView;
import admin.model.ZoneView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** Xuất/nhập Phòng thi (ExamArea) và Máy thi (ExamDevice) bằng Excel (.xlsx). */
public interface FacilityExcelService {

    // ---- Phòng thi ----

    /** Biểu mẫu trống để nhập nhiều phòng/sân thi cùng lúc. */
    void writeRoomTemplate(List<ZoneView> zones, OutputStream out) throws IOException;

    /** Xuất danh sách phòng/sân thi đang hiển thị. */
    void writeRooms(List<AreaView> rooms, OutputStream out) throws IOException;

    /** Đọc file import phòng/sân thi. */
    List<RoomRow> readRoomImport(InputStream in) throws IOException;

    // ---- Máy thi ----

    /** Biểu mẫu trống để nhập nhiều máy/thiết bị thi cùng lúc. */
    void writeDeviceTemplate(List<AreaView> areas, OutputStream out) throws IOException;

    /** Xuất danh sách máy/thiết bị thi đang hiển thị. */
    void writeDevices(List<DeviceView> devices, OutputStream out) throws IOException;

    /** Đọc file import máy/thiết bị thi. */
    List<DeviceRow> readDeviceImport(InputStream in) throws IOException;

    /** Một dòng phòng/sân thi đọc từ Excel. */
    class RoomRow {
        public int rowNumber;
        public String zoneName;
        public String areaName;
        public String areaType;
        public String capacity;
        public String location;

        public boolean isBlank() {
            return blank(zoneName) && blank(areaName) && blank(areaType) && blank(location);
        }
        private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    }

    /** Một dòng máy/thiết bị thi đọc từ Excel. */
    class DeviceRow {
        public int rowNumber;
        public String zoneName;
        public String areaName;
        public String deviceName;
        public String deviceType;
        public String status;

        public boolean isBlank() {
            return blank(zoneName) && blank(areaName) && blank(deviceName) && blank(deviceType);
        }
        private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    }
}
