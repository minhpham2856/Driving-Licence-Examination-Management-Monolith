<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${requestScope.auditReady ne true}">
    <c:redirect url="/manager/audit" />
</c:if>
<c:url var="exportUrl" value="/manager/audit/export">
    <c:param name="keyword" value="${keyword}" />
    <c:param name="action" value="${action}" />
    <c:param name="startDate" value="${startDate}" />
    <c:param name="endDate" value="${endDate}" />
</c:url>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nhật ký thao tác quản lý - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .audit-details-line { margin-top: .25rem; color: #64748b; font-size: .78rem; }
        .audit-details-line strong { color: #334155; }
        .audit-filter-error { margin: 0 0 1rem; padding: .75rem 1rem; border: 1px solid #fcd34d; border-radius: 8px; background: #fffbeb; color: #92400e; }
        .audit-metrics { grid-template-columns: repeat(4, minmax(0, 1fr)); }
        @media (max-width: 1100px) { .audit-metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
        @media (max-width: 700px) { .audit-metrics { grid-template-columns: 1fr; } }
        @media print {
            .side-nav-bar, .breadcrumbs, .page-actions, .filter-panel, .pagination-footer, .log-card-actions, footer { display: none !important; }
            body.has-side-nav-bar, .dashboard-shell, .main-content { margin: 0 !important; padding: 0 !important; width: 100% !important; }
            .metrics-row { page-break-inside: avoid; }
            .log-card { box-shadow: none !important; border: 1px solid #cbd5e1; }
        }
    </style>
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="audit" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${ctx}/manager/dashboard">Dashboard quản lý</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Nhật ký thao tác</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật ký thao tác quản lý</h1>
                <p class="page-subtitle">Theo dõi lịch sử nghiệp vụ của tài khoản đang đăng nhập. Dữ liệu được tải 15 bản ghi mỗi trang.</p>
            </div>
            <div class="page-actions" style="display:flex;gap:10px;">
                <a class="btn-export" href="${exportUrl}" style="height:42px;padding:0 1.1rem;display:inline-flex;align-items:center;gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </a>
                <button type="button" class="btn-export" onclick="window.print()" style="height:42px;padding:0 1.1rem;display:inline-flex;align-items:center;gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                    In sổ
                </button>
            </div>
        </header>

        <c:if test="${not empty auditFilterError}">
            <div class="audit-filter-error"><c:out value="${auditFilterError}" /></div>
        </c:if>

        <section class="metrics-row audit-metrics">
            <div class="stat-card">
                <div class="stat-info"><span class="stat-number">${totalOperations}</span><span class="stat-label">Thao tác theo bộ lọc</span><span class="stat-trend stat-trend--up">Dữ liệu nhật ký thật</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-info"><span class="stat-number" style="color:#059669">${approvedCount}</span><span class="stat-label">Hồ sơ đã duyệt</span><span class="stat-trend stat-trend--up">Trong khoảng ngày chọn</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-info"><span class="stat-number" style="color:#d97706">${supplementCount}</span><span class="stat-label">Yêu cầu bổ sung</span><span class="stat-trend" style="color:#92400e">Trong khoảng ngày chọn</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-info"><span class="stat-number" style="color:#2563eb">${exportCount}</span><span class="stat-label">Lần xuất dữ liệu</span><span class="stat-trend stat-trend--up">Trong khoảng ngày chọn</span></div>
            </div>
        </section>

        <section class="filter-panel">
            <h2 class="filter-title">Bộ lọc nhật ký</h2>
            <form action="${ctx}/manager/audit" method="get">
                <div class="filter-grid" style="grid-template-columns:2fr 1.3fr 1.25fr 1.25fr auto;">
                    <div class="input-group">
                        <label for="keyword" class="input-label">Từ khóa</label>
                        <input type="search" id="keyword" name="keyword" class="input-field"
                               placeholder="Người thực hiện, nội dung, đối tượng..." value="${fn:escapeXml(keyword)}">
                    </div>
                    <div class="input-group">
                        <label for="action" class="input-label">Hành động</label>
                        <select id="action" name="action" class="input-field">
                            <option value="">Tất cả</option>
                            <option value="APPROVE" ${action eq 'APPROVE' ? 'selected' : ''}>Duyệt</option>
                            <option value="INSERT" ${action eq 'INSERT' ? 'selected' : ''}>Thêm mới</option>
                            <option value="UPDATE" ${action eq 'UPDATE' ? 'selected' : ''}>Cập nhật</option>
                            <option value="DELETE" ${action eq 'DELETE' ? 'selected' : ''}>Xóa</option>
                            <option value="EXPORT" ${action eq 'EXPORT' ? 'selected' : ''}>Xuất dữ liệu</option>
                            <option value="ASSIGN" ${action eq 'ASSIGN' ? 'selected' : ''}>Phân công</option>
                            <option value="IMPORT" ${action eq 'IMPORT' ? 'selected' : ''}>Nhập dữ liệu</option>
                            <option value="WARNING" ${action eq 'WARNING' ? 'selected' : ''}>Cảnh báo</option>
                            <option value="SYSTEM" ${action eq 'SYSTEM' ? 'selected' : ''}>Hệ thống</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="startDate" class="input-label">Từ ngày</label>
                        <input type="date" id="startDate" name="startDate" class="input-field" value="${fn:escapeXml(startDate)}">
                    </div>
                    <div class="input-group">
                        <label for="endDate" class="input-label">Đến ngày</label>
                        <input type="date" id="endDate" name="endDate" class="input-field" value="${fn:escapeXml(endDate)}">
                    </div>
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">Lọc</button>
                            <a href="${ctx}/manager/audit" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card">
            <header class="log-card-header">
                <h2 class="log-card-title">Sổ hoạt động <span style="font-size:.78rem;background:#eff6ff;color:#1d4ed8;padding:3px 10px;border-radius:999px;">${totalFiltered} bản ghi</span></h2>
                <div class="log-card-actions">Trang ${currentPage}/${totalPages}</div>
            </header>
            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th style="width:65px;text-align:center;">STT</th>
                            <th style="width:150px;">Thời gian</th>
                            <th style="min-width:170px;">Người thực hiện</th>
                            <th style="width:130px;text-align:center;">Hành động</th>
                            <th style="min-width:150px;">Đối tượng</th>
                            <th style="min-width:320px;">Nội dung thay đổi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty logs}">
                                <c:forEach var="log" items="${logs}" varStatus="status">
                                    <tr>
                                        <td style="text-align:center;font-weight:700;color:#64748b;">${firstItem + status.index}</td>
                                        <td style="font-size:.8rem;font-weight:600;color:#475569;"><fmt:formatDate value="${log.changedAt}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                        <td>
                                            <div style="font-weight:650;color:#0f172a;"><c:out value="${log.changerName}" default="Hệ thống" /></div>
                                            <div style="font-size:.72rem;color:#64748b;">Nhân viên quản lý</div>
                                        </td>
                                        <td style="text-align:center;">
                                            <c:choose>
                                                <c:when test="${log.action eq 'APPROVE'}"><span class="action-badge action-badge--success">Duyệt</span></c:when>
                                                <c:when test="${log.action eq 'INSERT'}"><span class="action-badge action-badge--success">Thêm</span></c:when>
                                                <c:when test="${log.action eq 'DELETE'}"><span class="action-badge action-badge--danger">Xóa</span></c:when>
                                                <c:when test="${log.action eq 'EXPORT'}"><span class="action-badge action-badge--info">Xuất</span></c:when>
                                                <c:when test="${log.action eq 'ASSIGN'}"><span class="action-badge action-badge--warning">Phân công</span></c:when>
                                                <c:when test="${log.action eq 'IMPORT'}"><span class="action-badge action-badge--info">Nhập</span></c:when>
                                                <c:when test="${log.action eq 'WARNING'}"><span class="action-badge action-badge--danger">Cảnh báo</span></c:when>
                                                <c:when test="${log.action eq 'SYSTEM'}"><span class="action-badge action-badge--warning">Hệ thống</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--info">Cập nhật</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="font-weight:600;"><c:out value="${log.tableName}" default="-" /></div>
                                            <div style="font-size:.72rem;color:#64748b;">Mã: <c:out value="${empty log.recordId ? '-' : log.recordId}" /></div>
                                        </td>
                                        <td style="font-size:.84rem;color:#334155;line-height:1.45;">
                                            <c:choose>
                                                <c:when test="${not empty log.details}"><c:out value="${log.details}" /></c:when>
                                                <c:when test="${not empty log.newValue}"><c:out value="${log.newValue}" /></c:when>
                                                <c:otherwise>Không có nội dung bổ sung</c:otherwise>
                                            </c:choose>
                                            <c:if test="${not empty log.oldValue}">
                                                <div class="audit-details-line"><strong>Cũ:</strong> <c:out value="${log.oldValue}" /></div>
                                            </c:if>
                                            <c:if test="${not empty log.oldValue and not empty log.newValue}">
                                                <div class="audit-details-line"><strong>Mới:</strong> <c:out value="${log.newValue}" /></div>
                                            </c:if>
                                            <c:if test="${not empty log.reason}">
                                                <div class="audit-details-line"><strong>Lý do:</strong> <c:out value="${log.reason}" /></div>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="6" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Không có nhật ký phù hợp với bộ lọc.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <footer class="pagination-footer">
                <div class="pagination-info">Hiển thị ${firstItem} - ${lastItem} trong ${totalFiltered} bản ghi</div>
                <div class="pagination-nav">
                    <c:choose>
                        <c:when test="${currentPage gt 1}">
                            <c:url var="previousUrl" value="/manager/audit"><c:param name="keyword" value="${keyword}"/><c:param name="action" value="${action}"/><c:param name="startDate" value="${startDate}"/><c:param name="endDate" value="${endDate}"/><c:param name="page" value="${currentPage - 1}"/></c:url>
                            <a class="page-btn page-btn--wide" href="${previousUrl}">Trước</a>
                        </c:when>
                        <c:otherwise><span class="page-btn page-btn--wide disabled">Trước</span></c:otherwise>
                    </c:choose>
                    <c:forEach begin="${pageStart}" end="${pageEnd}" var="pageNo">
                        <c:url var="pageUrl" value="/manager/audit"><c:param name="keyword" value="${keyword}"/><c:param name="action" value="${action}"/><c:param name="startDate" value="${startDate}"/><c:param name="endDate" value="${endDate}"/><c:param name="page" value="${pageNo}"/></c:url>
                        <a class="page-btn${pageNo eq currentPage ? ' active' : ''}" href="${pageUrl}" ${pageNo eq currentPage ? 'aria-current="page"' : ''}>${pageNo}</a>
                    </c:forEach>
                    <c:choose>
                        <c:when test="${currentPage lt totalPages}">
                            <c:url var="nextUrl" value="/manager/audit"><c:param name="keyword" value="${keyword}"/><c:param name="action" value="${action}"/><c:param name="startDate" value="${startDate}"/><c:param name="endDate" value="${endDate}"/><c:param name="page" value="${currentPage + 1}"/></c:url>
                            <a class="page-btn page-btn--wide" href="${nextUrl}">Sau</a>
                        </c:when>
                        <c:otherwise><span class="page-btn page-btn--wide disabled">Sau</span></c:otherwise>
                    </c:choose>
                </div>
            </footer>
        </section>
    </main>
    <jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>
</body>
</html>
