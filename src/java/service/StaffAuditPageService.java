package service;

import dto.examstaff.StaffAuditPageViewDTO;

public interface StaffAuditPageService {

    StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged);
}
