package admin.dao;

/**
 * Kiểm tra một bản ghi danh mục có đang được nơi khác sử dụng hay không,
 * trước khi cho phép Xóa hoặc Khóa (vô hiệu hóa).
 *
 * Mỗi phương thức trả về {@code null} nếu thao tác được phép,
 * hoặc một câu thông báo mô tả nơi đang sử dụng nếu bị chặn.
 */
public interface UsageGuardDAO {

    /** Khu vực thi (ExamZone) — bị chặn khi còn phòng/sân thi trực thuộc. */
    String zoneBlocker(int zoneId);

    /** Phòng/sân thi (ExamArea) — bị chặn khi còn máy thi, kỳ thi, lịch phân công... */
    String areaBlocker(int areaId);

    /** Máy/thiết bị thi (ExamDevice) — bị chặn khi đã gán cho thí sinh trong kỳ thi. */
    String deviceBlocker(int deviceId);

    /** Hạng GPLX (Licence) — bị chặn khi đã gắn biểu phí, kỳ thi, câu hỏi, đăng ký... */
    String licenceBlocker(int licenceId);
}
