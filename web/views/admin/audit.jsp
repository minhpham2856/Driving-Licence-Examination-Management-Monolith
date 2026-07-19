<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật ký hệ thống - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="nhat-ky" /></jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Nhật ký hệ thống</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật ký kiểm toán hệ thống</h1>
                <p class="page-subtitle">Theo dõi toàn bộ thao tác của người dùng: đăng nhập, thay đổi dữ liệu, phân quyền và các cảnh báo bất thường.</p>
            </div>
        </header>

        <section class="metrics-row" aria-label="Thống kê nhật ký">
            <div class="stat-card"><div class="stat-icon stat-icon--blue"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
                <div class="stat-info"><span class="stat-number">${empty totalActions ? 0 : totalActions}</span><span class="stat-label">Tổng tác vụ thao tác</span><span class="stat-trend stat-trend--up">Toàn hệ thống</span></div></div>
            <div class="stat-card"><div class="stat-icon stat-icon--amber"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
                <div class="stat-info"><span class="stat-number">${empty updateCount ? 0 : updateCount}</span><span class="stat-label">Thao tác cập nhật</span><span class="stat-trend stat-trend--up">Thay đổi dữ liệu</span></div></div>
            <div class="stat-card"><div class="stat-icon stat-icon--red"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
                <div class="stat-info"><span class="stat-number">${empty warningCount ? 0 : warningCount}</span><span class="stat-label">Cảnh báo / sự cố</span><span class="stat-trend stat-trend--down">Cần kiểm tra</span></div></div>
            <div class="stat-card"><div class="stat-icon stat-icon--green"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M22 4 12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
                <div class="stat-info"><span class="stat-number">${empty successRate ? '100.0' : successRate}%</span><span class="stat-label">Tỷ lệ tác vụ bình thường</span><span class="stat-trend stat-trend--up">Hệ thống an toàn</span></div></div>
        </section>

        <section class="filter-panel" aria-label="Bộ lọc nhật ký">
            <h2 class="filter-title"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg> Bộ lọc tìm kiếm nhật ký</h2>
            <form action="${ctx}/admin/audit" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.2fr 1.2fr 1fr 1fr 1.4fr;">
                    <div class="input-group"><label for="searchKeyword" class="input-label">Từ khóa kiểm toán</label><input type="text" id="searchKeyword" name="searchKeyword" class="input-field" placeholder="Tìm tài khoản, hành động, nội dung..." value="${param.searchKeyword}"></div>
                    <div class="input-group"><label for="filterRole" class="input-label">Vai trò</label>
                        <select id="filterRole" name="filterRole" class="input-field"><option value="">Tất cả vai trò</option>
                            <option value="admin" ${param.filterRole eq 'admin' ? 'selected' : ''}>Quản trị viên</option>
                            <option value="coi_thi" ${param.filterRole eq 'coi_thi' ? 'selected' : ''}>Cán bộ coi thi</option>
                            <option value="cham_thi" ${param.filterRole eq 'cham_thi' ? 'selected' : ''}>Giám khảo</option>
                            <option value="managing" ${param.filterRole eq 'managing' ? 'selected' : ''}>Cán bộ quản lý</option>
                            <option value="candidate" ${param.filterRole eq 'candidate' ? 'selected' : ''}>Thí sinh</option>
                        </select></div>
                    <div class="input-group"><label for="filterAction" class="input-label">Thao tác</label>
                        <select id="filterAction" name="filterAction" class="input-field"><option value="">Tất cả thao tác</option>
                            <option value="INSERT" ${param.filterAction eq 'INSERT' ? 'selected' : ''}>Thêm mới</option>
                            <option value="UPDATE" ${param.filterAction eq 'UPDATE' ? 'selected' : ''}>Cập nhật</option>
                            <option value="DELETE" ${param.filterAction eq 'DELETE' ? 'selected' : ''}>Xóa</option>
                            <option value="WARNING" ${param.filterAction eq 'WARNING' ? 'selected' : ''}>Cảnh báo</option>
                            <option value="IMPORT" ${param.filterAction eq 'IMPORT' ? 'selected' : ''}>Nhập dữ liệu</option>
                            <option value="EXPORT" ${param.filterAction eq 'EXPORT' ? 'selected' : ''}>Xuất dữ liệu</option>
                            <option value="ASSIGN" ${param.filterAction eq 'ASSIGN' ? 'selected' : ''}>Phân công</option>
                        </select></div>
                    <div class="input-group"><label for="dateFrom" class="input-label">Từ ngày</label><input type="date" id="dateFrom" name="dateFrom" class="input-field" value="${param.dateFrom}"></div>
                    <div class="input-group"><label for="dateTo" class="input-label">Đến ngày</label><input type="date" id="dateTo" name="dateTo" class="input-field" value="${param.dateTo}"></div>
                    <div class="input-group filter-grid__btn-col"><div class="btn-group">
                        <button type="submit" class="btn-filter"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg> Tìm kiếm</button>
                        <a href="${ctx}/admin/audit" class="btn-reset">Đặt lại</a>
                    </div></div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Sổ hoạt động nhật ký kiểm toán">
            <header class="log-card-header">
                <h2 class="log-card-title"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M14 2v6h6M9 13h6M9 17h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                    Sổ hoạt động Nhật ký kiểm toán
                    <c:if test="${not empty auditLogs}"><span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">${empty filteredTotal ? fn:length(auditLogs) : filteredTotal} bản ghi</span></c:if>
                </h2>
                <div class="log-card-actions"><button class="btn-export"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/></svg> In sổ kiểm toán</button></div>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 150px;">Thời gian</th>
                            <th scope="col" style="min-width: 180px;">Người thực hiện</th>
                            <th scope="col" style="width: 130px; text-align: center;">Vai trò</th>
                            <th scope="col" style="width: 120px; text-align: center;">Thao tác</th>
                            <th scope="col" style="width: 120px; text-align: center;">Phân hệ</th>
                            <th scope="col">Chi tiết nội dung kiểm toán</th>
                            <th scope="col" style="width: 150px;">IP & Thiết bị</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty auditLogs}">
                                <c:forEach var="log" items="${auditLogs}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${(currentPage-1)*15 + status.index + 1}</td>
                                        <td style="font-size: 0.82rem; color: #475569; font-weight: 500;"><fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>
                                            <div class="user-cell">
                                                <c:choose>
                                                    <c:when test="${log.roleCode eq 'admin'}"><div class="user-avatar">${log.initial}</div></c:when>
                                                    <c:when test="${log.roleCode eq 'coi_thi'}"><div class="user-avatar user-avatar--teal">${log.initial}</div></c:when>
                                                    <c:when test="${log.roleCode eq 'cham_thi'}"><div class="user-avatar user-avatar--purple">${log.initial}</div></c:when>
                                                    <c:otherwise><div class="user-avatar user-avatar--orange">${log.initial}</div></c:otherwise>
                                                </c:choose>
                                                <div class="user-info"><span class="user-name" style="font-weight: 600; color: #0f172a;">${log.displayName}</span><c:if test="${not empty log.username}"><span class="user-username">@${log.username}</span></c:if></div>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${log.roleCode eq 'admin'}"><span class="role-badge role-badge--admin">Quản trị viên</span></c:when>
                                                <c:when test="${log.roleCode eq 'coi_thi'}"><span class="role-badge role-badge--coi">Cán bộ coi thi</span></c:when>
                                                <c:when test="${log.roleCode eq 'cham_thi'}"><span class="role-badge role-badge--cham">Giám khảo</span></c:when>
                                                <c:when test="${log.roleCode eq 'managing'}"><span class="role-badge role-badge--admin">Cán bộ quản lý</span></c:when>
                                                <c:otherwise><span class="role-badge role-badge--other">Thí sinh</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${log.action eq 'INSERT'}"><span class="action-badge action-badge--success">${log.actionLabel}</span></c:when>
                                                <c:when test="${log.action eq 'UPDATE'}"><span class="role-badge role-badge--coi">${log.actionLabel}</span></c:when>
                                                <c:when test="${log.action eq 'DELETE' or log.action eq 'WARNING'}"><span class="action-badge action-badge--danger">${log.actionLabel}</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--warning">${log.actionLabel}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;"><span class="user-username" style="font-weight: 600;">${log.module}</span></td>
                                        <td style="font-size: 0.85rem; color: #334155; white-space: normal;">
                                            <c:choose>
                                                <c:when test="${log.action eq 'WARNING'}"><span style="color:#b91c1c; font-weight:600;">${log.detail}</span></c:when>
                                                <c:otherwise>${log.detail}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size: 0.8rem; color: #94a3b8;">${log.ip}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M14 2v6h6M9 13h6M9 17h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                    Không có bản ghi nhật ký nào khớp với bộ lọc.
                                </td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <footer class="pagination-footer">
                <c:set var="qs" value="searchKeyword=${param.searchKeyword}&filterRole=${param.filterRole}&filterAction=${param.filterAction}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}" />
                <div class="pagination-info">Hiển thị <c:choose><c:when test="${not empty auditLogs}">${(currentPage-1)*15 + 1} - ${(currentPage-1)*15 + fn:length(auditLogs)}</c:when><c:otherwise>0</c:otherwise></c:choose> trong tổng số ${empty filteredTotal ? 0 : filteredTotal} bản ghi kiểm toán</div>
                <div class="pagination-nav">
                    <c:choose><c:when test="${currentPage > 1}"><a class="page-btn page-btn--wide" href="${ctx}/admin/audit?${qs}&page=${currentPage-1}">Trước</a></c:when><c:otherwise><button class="page-btn page-btn--wide disabled" disabled>Trước</button></c:otherwise></c:choose>
                    <button class="page-btn active">${empty currentPage ? 1 : currentPage}</button>
                    <c:choose><c:when test="${currentPage < totalPages}"><a class="page-btn page-btn--wide" href="${ctx}/admin/audit?${qs}&page=${currentPage+1}">Sau</a></c:when><c:otherwise><button class="page-btn page-btn--wide disabled" disabled>Sau</button></c:otherwise></c:choose>
                </div>
            </footer>
        </section>
    </main>
    <jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>

</body>
</html>
