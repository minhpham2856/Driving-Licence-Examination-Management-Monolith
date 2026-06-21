<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Setup rich fallbacks if the controller has not populated the request attributes (for standalone frontend preview)
    if (request.getAttribute("logs") == null && session.getAttribute("logs") == null) {
        java.util.List<java.util.HashMap<String, Object>> mockList = new java.util.ArrayList<>();
        
        java.util.HashMap<String, Object> l1 = new java.util.HashMap<>();
        l1.put("id", "1");
        l1.put("timestamp", "24/05/2026 21:45");
        l1.put("username", "admin.haiqh");
        l1.put("fullName", "Quách Hoàng Hải");
        l1.put("avatarClass", "");
        l1.put("roleKey", "admin");
        l1.put("role", "Quản trị viên");
        l1.put("actionKey", "success");
        l1.put("action", "Đăng nhập");
        l1.put("module", "Bảo mật");
        l1.put("details", "Đăng nhập thành công vào trang quản trị hệ thống.");
        l1.put("ip", "192.168.1.2");
        l1.put("ipAddress", "192.168.1.2");
        l1.put("device", "Windows 11 / Chrome 124");
        l1.put("status", "Thành công");
        l1.put("statusKey", "success");
        mockList.add(l1);
        
        java.util.HashMap<String, Object> l2 = new java.util.HashMap<>();
        l2.put("id", "2");
        l2.put("timestamp", "24/05/2026 21:30");
        l2.put("username", "admin.haiqh");
        l2.put("fullName", "Quách Hoàng Hải");
        l2.put("avatarClass", "");
        l2.put("roleKey", "admin");
        l2.put("role", "Quản trị viên");
        l2.put("actionKey", "warning");
        l2.put("action", "Khóa tài khoản");
        l2.put("module", "Tài khoản");
        l2.put("details", "Khóa tài khoản sát hạch viên Lê Thị Hằng (@examiner.lehang) do nghi vấn quy chế.");
        l2.put("ip", "192.168.1.2");
        l2.put("ipAddress", "192.168.1.2");
        l2.put("device", "Windows 11 / Chrome 124");
        l2.put("status", "Cảnh báo");
        l2.put("statusKey", "warning");
        mockList.add(l2);
        
        java.util.HashMap<String, Object> l3 = new java.util.HashMap<>();
        l3.put("id", "3");
        l3.put("timestamp", "24/05/2026 20:15");
        l3.put("username", "proctor.nguyenan");
        l3.put("fullName", "Nguyễn Văn An");
        l3.put("avatarClass", "user-avatar--teal");
        l3.put("roleKey", "coi");
        l3.put("role", "Cán bộ coi thi");
        l3.put("actionKey", "success");
        l3.put("action", "Kích hoạt máy");
        l3.put("module", "Máy thi");
        l3.put("details", "Đồng bộ IP tĩnh và kích hoạt kết nối client cho máy thi MC-102.");
        l3.put("ip", "192.168.10.15");
        l3.put("ipAddress", "192.168.10.15");
        l3.put("device", "Linux Ubuntu / Firefox 125");
        l3.put("status", "Thành công");
        l3.put("statusKey", "success");
        mockList.add(l3);

        java.util.HashMap<String, Object> l4 = new java.util.HashMap<>();
        l4.put("id", "4");
        l4.put("timestamp", "24/05/2026 19:50");
        l4.put("username", "examiner.lehang");
        l4.put("fullName", "Lê Thị Hằng");
        l4.put("avatarClass", "user-avatar--purple");
        l4.put("roleKey", "cham");
        l4.put("role", "sát hạch viên");
        l4.put("actionKey", "danger");
        l4.put("action", "Sửa đổi điểm");
        l4.put("module", "Sát hạch");
        l4.put("details", "CẢNH BÁO: Phát hiện chỉnh sửa điểm thi thực hành sa hình của thí sinh SBD-045.");
        l4.put("ip", "192.168.15.54");
        l4.put("ipAddress", "192.168.15.54");
        l4.put("device", "Mac OS / Safari 17.2");
        l4.put("status", "Nguy hiểm");
        l4.put("statusKey", "danger");
        mockList.add(l4);

        java.util.HashMap<String, Object> l5 = new java.util.HashMap<>();
        l5.put("id", "5");
        l5.put("timestamp", "24/05/2026 18:20");
        l5.put("username", "admin.haiqh");
        l5.put("fullName", "Quách Hoàng Hải");
        l5.put("avatarClass", "");
        l5.put("roleKey", "admin");
        l5.put("role", "Quản trị viên");
        l5.put("actionKey", "info");
        l5.put("action", "Cấu hình lệ phí");
        l5.put("module", "Lệ phí");
        l5.put("details", "Cập nhật biểu phí sát hạch lý thuyết hạng GPLX B2 lên 100.000 đ.");
        l5.put("ip", "192.168.1.2");
        l5.put("ipAddress", "192.168.1.2");
        l5.put("device", "Windows 11 / Chrome 124");
        l5.put("status", "Thông tin");
        l5.put("statusKey", "info");
        mockList.add(l5);
        
        request.setAttribute("logs", mockList);
        request.setAttribute("totalOperations", 524);
        request.setAttribute("dataCorrections", 42);
        request.setAttribute("riskOperations", 3);
        request.setAttribute("successRate", "99.4%");
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật ký hệ thống - Lái Vui</title>

    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<%-- Inject the admin sidebar template --%>
<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="audit" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Nhật ký hệ thống</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật ký kiểm toán hệ thống</h1>
                <p class="page-subtitle">Giám sát vết kiểm toán toàn diện (Audit Log), ghi nhận đầy đủ lịch sử đăng nhập, thay đổi cấu hình đợt thi, sửa điểm và hoạt động quản trị danh mục.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất báo cáo Excel
                </button>
            </div>
        </header>

        <!-- Stats Metrics Row -->
        <section class="metrics-row" aria-label="Số liệu kiểm toán">
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
                    <span class="stat-trend stat-trend--up">Hoạt động trong tháng</span>
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
                    <span class="stat-label">Thay đổi cấu hình</span>
                    <span class="stat-trend stat-trend--up">Thiết lập dữ liệu</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #dc2626;">${empty riskOperations ? 0 : riskOperations}</span>
                    <span class="stat-label">Sự kiện sửa điểm / Lỗi</span>
                    <span class="stat-trend stat-trend--down" style="color: #dc2626;">Cảnh báo kiểm toán</span>
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
                    <span class="stat-label">Tỷ lệ tác vụ thành công</span>
                    <span class="stat-trend stat-trend--up">Hệ thống an toàn</span>
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
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.1fr 1.75fr 1.5fr;">
                    <!-- Keyword Search -->
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Từ khóa kiểm toán</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field" 
                               placeholder="Tìm tài khoản, hành động, IP..." value="${param.searchKeyword}">
                    </div>
                    
                    <!-- Role Dropdown -->
                    <div class="input-group">
                        <label for="filterRole" class="input-label">Vai trò</label>
                        <select id="filterRole" name="filterRole" class="input-field">
                            <option value="">Tất cả vai trò</option>
                            <option value="admin" ${param.filterRole eq 'admin' ? 'selected' : ''}>Quản trị viên</option>
                            <option value="coi" ${param.filterRole eq 'coi' ? 'selected' : ''}>Cán bộ coi thi</option>
                            <option value="cham" ${param.filterRole eq 'cham' ? 'selected' : ''}>sát hạch viên chấm thi</option>
                        </select>
                    </div>
                    
                    <!-- Action Type Dropdown -->
                    <div class="input-group">
                        <label for="filterAction" class="input-label">Thao tác</label>
                        <select id="filterAction" name="filterAction" class="input-field">
                            <option value="">Tất cả thao tác</option>
                            <option value="login" ${param.filterAction eq 'login' ? 'selected' : ''}>Đăng nhập</option>
                            <option value="config" ${param.filterAction eq 'config' ? 'selected' : ''}>Cấu hình hệ thống</option>
                            <option value="score" ${param.filterAction eq 'score' ? 'selected' : ''}>Chỉnh sửa điểm</option>
                            <option value="account" ${param.filterAction eq 'account' ? 'selected' : ''}>Quản lý tài khoản</option>
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
                            <a href="${pageContext.request.contextPath}/views/admin/audit.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Audit Logs Table Section -->
        <section class="log-card" aria-label="Danh sách nhật ký kiểm toán">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M9 17h6M9 12h6M9 7h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Sổ hoạt động Nhật ký kiểm toán
                    <c:if test="${not empty logs}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(logs)} bản ghi
                        </span>
                    </c:if>
                </h2>
                <div class="log-card-actions">
                    <button class="btn-export" onclick="window.print()">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        In sổ kiểm toán
                    </button>
                </div>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 140px;">Thời gian</th>
                            <th scope="col" style="min-width: 180px;">Người thực hiện</th>
                            <th scope="col" style="width: 130px; text-align: center;">Vai trò</th>
                            <th scope="col" style="width: 130px; text-align: center;">Thao tác</th>
                            <th scope="col" style="width: 130px; text-align: center;">Phân hệ</th>
                            <th scope="col">Chi tiết nội dung kiểm toán</th>
                            <th scope="col" style="min-width: 150px;">IP & Thiết bị</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty logs}">
                                <c:forEach var="log" items="${logs}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td class="col-time" style="font-size: 0.8rem; font-weight: 600;">${log.timestamp}</td>
                                        <td>
                                            <div class="user-cell">
                                                <c:choose>
                                                    <c:when test="${log.roleKey eq 'admin'}">
                                                        <div class="user-avatar" title="Quản trị viên">
                                                            ${fn:substring(log.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${log.roleKey eq 'coi'}">
                                                        <div class="user-avatar user-avatar--teal" title="Cán bộ coi thi">
                                                            ${fn:substring(log.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${log.roleKey eq 'cham'}">
                                                        <div class="user-avatar user-avatar--purple" title="sát hạch viên chấm thi">
                                                            ${fn:substring(log.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="user-avatar user-avatar--gray" title="Người dùng">
                                                            ${fn:substring(log.fullName, 0, 1)}
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div class="user-info">
                                                    <span class="user-name" style="font-weight: 600; color: #0f172a;">${log.fullName}</span>
                                                    <span class="user-username">@${log.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
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
                                        <td style="text-align: center;">
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
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--other" style="font-size: 0.72rem; padding: 2px 8px;">${log.module}</span>
                                        </td>
                                        <td class="details-cell" style="font-size: 0.88rem; color: #334155; font-weight: 500;">
                                            ${log.details}
                                        </td>
                                        <td class="ip-cell">
                                            <span>${log.ip}</span>
                                            <span class="device-info">${log.device}</span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Không tìm thấy lịch sử nhật ký kiểm toán nào trong khoảng thời gian đã chọn.
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
                    bản ghi kiểm toán
                </div>
                <div class="pagination-nav">
                    <button class="page-btn page-btn--wide disabled" disabled>Trước</button>
                    <button class="page-btn active">1</button>
                    <button class="page-btn page-btn--wide disabled" disabled>Sau</button>
                </div>
            </footer>
        </section>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
