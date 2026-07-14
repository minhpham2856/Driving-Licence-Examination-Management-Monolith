package examstaff.service;

import examstaff.dto.StaffAuditPageViewDTO;

/**
 * Ghép dữ liệu trang nhật ký thao tác (audit) cho nhân viên.
 */
public interface StaffAuditPageService {

    /**
     * Xây dựng view trang audit theo lọc ngày và phân trang.
     *
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc (chuỗi nghiệp vụ; có thể rỗng)
     * @param page                 trang hiện tại (1-based)
     * @param pageSize             số dòng mỗi trang
     * @param filterContextChanged true nếu bộ lọc vừa đổi (reset ngữ cảnh phân trang)
     * @return DTO hiển thị trang audit
     */
    StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged);
}
