<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>



<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật ký thao tác quản lý - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-manager.jsp">
    <jsp:param name="activeSidebar" value="nhat-ky" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/manager/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Nhật ký thao tác</span>
        </nav>
        
        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật Ký Thao Tác Quản Lý</h1>
                <p class="page-subtitle">Giám sát vết kiểm toán thao tác nghiệp vụ của Ban quản lý đào tạo, ghi nhận lịch sử duyệt hồ sơ, từ chối hồ sơ, tạo tài khoản và các thao tác nghiệp vụ.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất sổ nhật ký Excel
                </button>
            </div>
        </header>

        <!-- KPI Metrics Row -->
        <section class="metrics-row" aria-label="Số liệu kiểm toán quản lý">
            <!-- Metric 1: Total Operations -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalOperations ? 0 : totalOperations}</span>
                    <span class="stat-label">Tổng tác vụ thao tác</span>
                    <span class="stat-trend stat-trend--up">Vết kiểm toán ghi nhận</span>
                </div>
            </div>
            
            <!-- Metric 2: Approved Count -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty approvedCount ? 0 : approvedCount}</span>
                    <span class="stat-label">Hồ sơ đã phê duyệt</span>
                    <span class="stat-trend stat-trend--up">Học viên / TS tự do</span>
                </div>
            </div>
            
            <!-- Metric 3: Rejected Count -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #dc2626;">${empty rejectedCount ? 0 : rejectedCount}</span>
                    <span class="stat-label">Hồ sơ bị từ chối</span>
                    <span class="stat-trend stat-trend--down" style="color: #dc2626;">Được ghi nhận lý do</span>
                </div>
            </div>
            
            <!-- Metric 4: Created Accounts -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--purple">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty createdAccounts ? 0 : createdAccounts}</span>
                    <span class="stat-label">Tài khoản cấp mới</span>
                    <span class="stat-trend stat-trend--up">Hệ thống phân quyền</span>
                </div>
            </div>
        </section>

        <!-- Filters & Search Form Section -->
        <section class="filter-panel" aria-label="Bộ lọc tìm kiếm">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm nhật ký
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.5fr 2fr 1.5fr;">
                    <!-- Keyword Search -->
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Từ khóa thao tác</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field" 
                               placeholder="Tìm người thao tác, nội dung, IP..." value="${param.searchKeyword}">
                    </div>
                    
                    <!-- Action Type Dropdown -->
                    <div class="input-group">
                        <label for="filterAction" class="input-label">Loại thao tác</label>
                        <select id="filterAction" name="filterAction" class="input-field">
                            <option value="">Tất cả thao tác</option>
                            <option value="Duyệt hồ sơ" ${param.filterAction eq 'Duyệt hồ sơ' ? 'selected' : ''}>Duyệt hồ sơ</option>
                            <option value="Từ chối hồ sơ" ${param.filterAction eq 'Từ chối hồ sơ' ? 'selected' : ''}>Từ chối hồ sơ</option>
                            <option value="Tạo tài khoản" ${param.filterAction eq 'Tạo tài khoản' ? 'selected' : ''}>Tạo tài khoản</option>
                            <option value="Reset mật khẩu" ${param.filterAction eq 'Reset mật khẩu' ? 'selected' : ''}>Reset mật khẩu</option>
                            <option value="Xuất báo cáo" ${param.filterAction eq 'Xuất báo cáo' ? 'selected' : ''}>Xuất báo cáo</option>
                            <option value="Đăng nhập" ${param.filterAction eq 'Đăng nhập' ? 'selected' : ''}>Đăng nhập</option>
                        </select>
                    </div>
                    
                    <!-- Date Range Selection -->
                    <div class="input-group">
                        <label class="input-label">Khoảng thời gian</label>
                        <div class="date-range-inputs" style="display: flex; align-items: center; gap: 6px;">
                            <input type="date" name="startDate" class="input-field" value="${param.startDate}" style="padding: 0.5rem;" aria-label="Từ ngày">
                            <span style="font-size: 0.8rem; color: #64748b;">đến</span>
                            <input type="date" name="endDate" class="input-field" value="${param.endDate}" style="padding: 0.5rem;" aria-label="Đến ngày">
                        </div>
                    </div>
                    
                    <!-- Action Button Row -->
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Tìm kiếm
                            </button>
                            <a href="audit.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Audit Logs Table Section -->
        <section class="log-card" aria-label="Danh sách nhật ký thao tác">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M9 17h6M9 12h6M9 7h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Sổ hoạt động Nhật ký kiểm toán quản lý
                    <c:if test="${not empty logs}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(logs)} bản ghi
                        </span>
                    </c:if>
                </h2>
                <div class="log-card-actions">
                    <button class="btn-export" style="padding: 6px 12px; font-size: 0.82rem; display: inline-flex; align-items: center; gap: 6px;">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        In sổ nhật ký
                    </button>
                </div>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id" style="width: 60px;">STT</th>
                            <th scope="col" style="width: 140px;">Thời gian</th>
                            <th scope="col" style="min-width: 180px;">Người thực hiện</th>
                            <th scope="col" style="width: 130px; text-align: center;">Vai trò</th>
                            <th scope="col" style="width: 140px; text-align: center;">Hành động</th>
                            <th scope="col">Chi tiết nội dung kiểm toán</th>
                            <th scope="col" style="min-width: 160px;">IP & Thiết bị</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty logs}">
                                <c:forEach var="log" items="${logs}" varStatus="status">
                                    <tr>
                                        <td class="col-id" style="text-align: center; font-weight: 700; color: #64748b;">${status.index + 1}</td>
                                        <td class="col-time" style="font-size: 0.8rem; font-weight: 600; color: #475569;">${log.timestamp}</td>
                                        <td>
                                            <div class="user-cell" style="display: flex; align-items: center; gap: 8px;">
                                                <div class="user-avatar ${empty log.avatarClass ? 'profile-avatar--blue' : log.avatarClass}" 
                                                     style="width: 32px; height: 32px; font-size: 0.85rem; display: flex; align-items: center; justify-content: center; border-radius: 999px; font-weight: 700; color: #0052cc; background-color: rgba(0, 82, 204, 0.12);"
                                                     title="${log.role}">
                                                    ${fn:substring(log.fullName, 0, 1)}
                                                </div>
                                                <div class="user-info" style="display: flex; flex-direction: column;">
                                                    <span class="user-name" style="font-weight: 600; color: #0f172a;">${log.fullName}</span>
                                                    <span class="user-username" style="font-size: 0.72rem; color: #64748b;">@${log.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin" style="padding: 2px 8px; font-size: 0.75rem;">${log.role}</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="action-badge action-badge--${log.actionKey}" style="font-weight: 700;">${log.action}</span>
                                        </td>
                                        <td class="details-cell" style="font-size: 0.88rem; color: #334155; font-weight: 500; line-height: 1.4;">
                                            ${log.details}
                                        </td>
                                        <td class="ip-cell" style="font-size: 0.82rem; color: #64748b;">
                                            <div style="font-weight: 600; color: #475569; font-family: monospace;">${log.ip}</div>
                                            <div style="font-size: 0.72rem; color: #94a3b8;">${log.device}</div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Không tìm thấy lịch sử nhật ký thao tác nào trong khoảng thời gian đã chọn.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 440px; margin-left: auto; margin-right: auto;">
                                            Hãy thử thay đổi điều kiện tìm kiếm hoặc đặt lại bộ lọc để tải nhật ký kiểm toán hệ thống.
                                        </p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Table Pagination controls -->
            <footer class="pagination-footer">
                <div class="pagination-info">
                    Hiển thị
                    <c:choose>
                        <c:when test="${not empty logs}">1 - ${fn:length(logs)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty logs}">${fn:length(logs)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    bản ghi thao tác
                </div>
                <div class="pagination-nav">
                    <button class="page-btn page-btn--wide disabled" disabled>Trước</button>
                    <button class="page-btn active">1</button>
                    <button class="page-btn page-btn--wide disabled" disabled>Sau</button>
                </div>
            </footer>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
