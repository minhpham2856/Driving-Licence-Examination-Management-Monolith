package examstaff.service;

import examstaff.dto.StaffAuditPageViewDTO;

public interface StaffAuditPageService {

    StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged);
}
