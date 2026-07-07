<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nhật ký thao tác - Lái Vui</title>

        <!-- Google Fonts: Inter & Be Vietnam Pro -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- External Layout Stylesheets (Matching dashboard.jsp stylesheet imports) -->
        <jsp:include page="/views/staff/exam/components/staff-exam-styles.jsp" />
    </head>
    <body class="has-side-nav-bar">

        <jsp:include page="/views/staff/exam/components/sidebar.jsp">
            <jsp:param name="activeSidebar" value="nhat-ky" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content">

                <!-- Breadcrumbs Navigation -->
                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current">Quản lý thi</span>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Nhật ký thao tác</span>
                </nav>

                <!-- Page Header Section -->
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Nhật ký thao tác</h1>
                        <p class="page-subtitle">Giám sát và kiểm tra lịch sử hoạt động, ghi nhận thay đổi dữ liệu của cán bộ quản lý đợt thi.</p>
                    </div>
                </header>

                <!-- Dynamic Metrics (KPI Stat Cards - Bounded to Backend Variables) -->
                <section class="metrics-row" aria-label="Số liệu hoạt động">
                    <div class="stat-card">
                        <div class="stat-icon stat-icon--blue">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="stat-info">
                            <span class="stat-number">${empty totalOperations ? 0 : totalOperations}</span>
                            <span class="stat-label">Tổng thao tác</span>
                            <span class="stat-trend ${totalOperationsTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                                <c:choose>
                                    <c:when test="${totalOperationsTrendDirection eq 'down'}">
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </c:when>
                                    <c:otherwise>
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                    </c:otherwise>
                                </c:choose>
                                ${empty totalOperationsTrend ? '+0% tháng này' : totalOperationsTrend}
                            </span>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon stat-icon--amber">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="stat-info">
                            <span class="stat-number">${empty dataCorrections ? 0 : dataCorrections}</span>
                            <span class="stat-label">Sửa đổi dữ liệu</span>
                            <span class="stat-trend ${dataCorrectionsTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                                <c:choose>
                                    <c:when test="${dataCorrectionsTrendDirection eq 'down'}">
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </c:when>
                                    <c:otherwise>
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                    </c:otherwise>
                                </c:choose>
                                ${empty dataCorrectionsTrend ? '+0 đợt thi mới' : dataCorrectionsTrend}
                            </span>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon stat-icon--red">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 9v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="stat-info">
                            <span class="stat-number">${empty riskOperations ? 0 : riskOperations}</span>
                            <span class="stat-label">Sửa điểm & Lỗi bảo mật</span>
                            <span class="stat-trend ${riskOperationsTrendDirection eq 'up' ? 'stat-trend--down' : 'stat-trend--up'}">
                                ${empty riskOperationsTrend ? 'Không đổi tuần này' : riskOperationsTrend}
                            </span>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon stat-icon--green">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="stat-info">
                            <span class="stat-number">${empty successRate ? '100%' : successRate}</span>
                            <span class="stat-label">Thành công hệ thống</span>
                            <span class="stat-trend ${successRateTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                                ${empty successRateTrend ? 'Hoạt động bình thường' : successRateTrend}
                            </span>
                        </div>
                    </div>
                </section>

                <!-- Filters & Search Form Section -->
                <section class="filter-panel" aria-label="Bộ lọc tìm kiếm">
                    <h2 class="filter-title">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Bộ lọc tìm kiếm
                    </h2>
                    <form action="" method="GET">
                        <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 2fr 1.8fr;">
                            <!-- Search Input -->
                            <div class="input-group">
                                <label for="searchKeyword" class="input-label">Từ khóa</label>
                                <input type="text" id="searchKeyword" name="searchKeyword" class="input-field" placeholder="Tìm tên cán bộ, hành động..." value="${param.searchKeyword}">
                            </div>

                            <!-- Role Dropdown -->
                            <div class="input-group">
                                <label for="filterRole" class="input-label">Vai trò</label>
                                <select id="filterRole" name="filterRole" class="input-field">
                                    <option value="">Tất cả vai trò</option>
                                    <option value="Quản trị viên" ${param.filterRole eq 'Quản trị viên' ? 'selected' : ''}>Quản trị viên</option>
                                    <option value="Cán bộ coi thi" ${param.filterRole eq 'Cán bộ coi thi' ? 'selected' : ''}>Cán bộ coi thi</option>
                                    <option value="Cán bộ chấm thi" ${param.filterRole eq 'Cán bộ chấm thi' ? 'selected' : ''}>Cán bộ chấm thi</option>
                                    <option value="Khách" ${param.filterRole eq 'Khách' ? 'selected' : ''}>Khách vãng lai</option>
                                </select>
                            </div>

                            <!-- Action Type Dropdown -->
                            <div class="input-group">
                                <label for="filterAction" class="input-label">Thao tác</label>
                                <select id="filterAction" name="filterAction" class="input-field">
                                    <option value="">Tất cả thao tác</option>
                                    <option value="login" ${param.filterAction eq 'login' ? 'selected' : ''}>Đăng nhập</option>
                                    <option value="upload" ${param.filterAction eq 'upload' ? 'selected' : ''}>Tải danh sách</option>
                                    <option value="grading" ${param.filterAction eq 'grading' ? 'selected' : ''}>Chấm điểm</option>
                                    <option value="edit-score" ${param.filterAction eq 'edit-score' ? 'selected' : ''}>Sửa điểm</option>
                                    <option value="report" ${param.filterAction eq 'report' ? 'selected' : ''}>Báo cáo</option>
                                    <option value="error" ${param.filterAction eq 'error' ? 'selected' : ''}>Lỗi bảo mật</option>
                                </select>
                            </div>

                            <!-- Date Range Selection -->
                            <div class="input-group">
                                <label class="input-label">Thời gian thao tác</label>
                                <div class="date-range-inputs">
                                    <input type="date" name="startDate" class="input-field" value="${param.startDate}" aria-label="Từ ngày">
                                    <span class="date-range-sep">đến</span>
                                    <input type="date" name="endDate" class="input-field" value="${param.endDate}" aria-label="Đến ngày">
                                </div>
                            </div>

                            <!-- Action Button Row -->
                            <div class="input-group filter-grid__btn-col">
                                <div class="btn-group">
                                    <button type="submit" class="btn-filter">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                        Tìm
                                    </button>
                                    <a href="${pageContext.request.contextPath}/views/staff/exam/audit.jsp" class="btn-reset">Đặt lại</a>
                                </div>
                            </div>
                        </div>
                    </form>
                </section>

                <!-- Main Content Card containing the Log Table (Pure JSTL data-driven table body) -->
                <section class="log-card" aria-label="Bảng nhật ký hoạt động">
                    <header class="log-card-header">
                        <h2 class="log-card-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v8M3 10V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v4" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <path d="M7 8h10M7 14h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Lịch sử hoạt động
                        </h2>

                        <div class="log-card-actions">
                            <button class="btn-export">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Xuất Excel
                            </button>
                            <button class="btn-export">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                </svg>
                                In Nhật ký
                            </button>
                        </div>
                    </header>

                    <div class="table-responsive">
                        <table class="audit-table">
                            <thead>
                                <tr>
                                    <th scope="col" class="col-id">STT</th>
                                    <th scope="col">Thời gian</th>
                                    <th scope="col">Người thực hiện</th>
                                    <th scope="col">Vai trò</th>
                                    <th scope="col">Thao tác</th>
                                    <th scope="col">Chi tiết thao tác</th>
                                    <th scope="col">IP & Thiết bị</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty logs}">
                                        <c:forEach var="log" items="${logs}" varStatus="status">
                                            <tr>
                                                <!-- Row Index -->
                                                <td class="col-id">${status.index + 1}</td>

                                                <!-- Log Time -->
                                                <td class="col-time">${log.timestamp}</td>

                                                <!-- User profile -->
                                                <td>
                                                    <div class="user-cell">
                                                        <div class="user-avatar ${log.avatarClass}">
                                                            ${fn:substring(log.fullName, 0, 1)}
                                                        </div>
                                                        <div class="user-info">
                                                            <span class="user-name">${log.fullName}</span>
                                                            <span class="user-username">@${log.username}</span>
                                                        </div>
                                                    </div>
                                                </td>

                                                <!-- User Role Badge -->
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${log.roleKey eq 'admin'}">
                                                            <span class="role-badge role-badge--admin">${log.role}</span>
                                                        </c:when>
                                                        <c:when test="${log.roleKey eq 'coi'}">
                                                            <span class="role-badge role-badge--coi">${log.role}</span>
                                                        </c:when>
                                                        <c:when test="${log.roleKey eq 'cham'}">
                                                            <span class="role-badge role-badge--cham">${log.role}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="role-badge role-badge--other">${log.role}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <!-- Action Severity Badge -->
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${log.actionKey eq 'info'}">
                                                            <span class="action-badge action-badge--info">${log.action}</span>
                                                        </c:when>
                                                        <c:when test="${log.actionKey eq 'success'}">
                                                            <span class="action-badge action-badge--success">${log.action}</span>
                                                        </c:when>
                                                        <c:when test="${log.actionKey eq 'warning'}">
                                                            <span class="action-badge action-badge--warning">${log.action}</span>
                                                        </c:when>
                                                        <c:when test="${log.actionKey eq 'danger'}">
                                                            <span class="action-badge action-badge--danger">${log.action}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="action-badge">${log.action}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <!-- Action Description details -->
                                                <td class="details-cell">${log.details}</td>

                                                <!-- IP address & Client browser -->
                                                <td class="ip-cell">
                                                    <span>${log.ip}</span>
                                                    <span class="device-info">${log.device}</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 4rem; color: #64748b; font-weight: 500;">
                                                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.25rem; display: block; opacity: 0.35; color: #64748b;">
                                                <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                                <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                </svg>
                                                Không tìm thấy lịch sử thao tác nào.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Table Pagination Area -->
                    <footer class="pagination-footer">
                        <div class="pagination-info">
                            Hiển thị <c:choose><c:when test="${not empty logs}">1 - ${fn:length(logs)}</c:when><c:otherwise>0</c:otherwise></c:choose> trong tổng số <c:choose><c:when test="${not empty logs}">${fn:length(logs)}</c:when><c:otherwise>0</c:otherwise></c:choose> bản ghi
                        </div>
                        <div class="pagination-nav">
                            <button class="page-btn page-btn--wide disabled" disabled aria-label="Trang trước">Trước</button>
                            <button class="page-btn active">1</button>
                            <button class="page-btn disabled" disabled>2</button>
                            <button class="page-btn disabled" disabled>3</button>
                            <button class="page-btn page-btn--wide disabled" disabled aria-label="Trang tiếp theo">Sau</button>
                        </div>
                    </footer>
                </section>

            </main>

        </div>

    </body>
</html>
