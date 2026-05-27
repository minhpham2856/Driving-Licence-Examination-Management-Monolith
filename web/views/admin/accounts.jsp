<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Setup rich fallbacks if the controller has not populated the request attributes (for standalone frontend preview)
    if (request.getAttribute("accounts") == null && session.getAttribute("accounts") == null) {
        java.util.List<java.util.HashMap<String, Object>> mockList = new java.util.ArrayList<>();
        
        java.util.HashMap<String, Object> u1 = new java.util.HashMap<>();
        u1.put("id", "1");
        u1.put("username", "admin.haiqh");
        u1.put("fullName", "Quách Hoàng Hải");
        u1.put("email", "haiqh.admin@laivui.gov.vn");
        u1.put("phone", "0987.654.321");
        u1.put("role", "admin");
        u1.put("department", "Phòng Quản lý Sát hạch");
        u1.put("createdAt", new java.util.Date(System.currentTimeMillis() - 1000L*60*60*24*90)); // 90 days ago
        u1.put("status", "active");
        mockList.add(u1);
        
        java.util.HashMap<String, Object> u2 = new java.util.HashMap<>();
        u2.put("id", "2");
        u2.put("username", "proctor.nguyenan");
        u2.put("fullName", "Nguyễn Văn An");
        u2.put("email", "annv.coithi@laivui.gov.vn");
        u2.put("phone", "0912.345.678");
        u2.put("role", "coi_thi");
        u2.put("department", "Trung tâm Sát hạch Miền Bắc");
        u2.put("createdAt", new java.util.Date(System.currentTimeMillis() - 1000L*60*60*24*45)); // 45 days ago
        u2.put("status", "active");
        mockList.add(u2);
        
        java.util.HashMap<String, Object> u3 = new java.util.HashMap<>();
        u3.put("id", "3");
        u3.put("username", "examiner.lehang");
        u3.put("fullName", "Lê Thị Hằng");
        u3.put("email", "hanglt.chamthi@laivui.gov.vn");
        u3.put("phone", "0904.888.999");
        u3.put("role", "cham_thi");
        u3.put("department", "Hội đồng Sát hạch Sở GTVT");
        u3.put("createdAt", new java.util.Date(System.currentTimeMillis() - 1000L*60*60*24*30)); // 30 days ago
        u3.put("status", "locked");
        mockList.add(u3);

        java.util.HashMap<String, Object> u4 = new java.util.HashMap<>();
        u4.put("id", "4");
        u4.put("username", "proctor.hoangnam");
        u4.put("fullName", "Trần Hoàng Nam");
        u4.put("email", "namth.coithi@laivui.gov.vn");
        u4.put("phone", "0977.123.456");
        u4.put("role", "coi_thi");
        u4.put("department", "Trung tâm Sát hạch Miền Nam");
        u4.put("createdAt", new java.util.Date(System.currentTimeMillis() - 1000L*60*60*24*15)); // 15 days ago
        u4.put("status", "active");
        mockList.add(u4);

        java.util.HashMap<String, Object> u5 = new java.util.HashMap<>();
        u5.put("id", "5");
        u5.put("username", "candidate.thuytrang");
        u5.put("fullName", "Nguyễn Thủy Trang");
        u5.put("email", "trangnt.candidate@gmail.com");
        u5.put("phone", "0868.999.888");
        u5.put("role", "candidate");
        u5.put("department", "Thí sinh Tự do");
        u5.put("createdAt", new java.util.Date(System.currentTimeMillis() - 1000L*60*60*24*5)); // 5 days ago
        u5.put("status", "inactive");
        mockList.add(u5);
        
        request.setAttribute("accounts", mockList);
        request.setAttribute("totalAccounts", 5);
        request.setAttribute("adminCount", 1);
        request.setAttribute("coiThiCount", 2);
        request.setAttribute("chamThiCount", 1);
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tài khoản - Lái Vui</title>

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
    <jsp:param name="activeSidebar" value="tai-khoan" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tài khoản</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Tài khoản hệ thống</h1>
                <p class="page-subtitle">Cấp phát tài khoản mới, quản lý thông tin cá nhân, phân quyền truy cập và kiểm soát trạng thái hoạt động của các nhóm người dùng trong hệ thống sát hạch.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </button>
                <button class="btn-filter" id="btn-add-account" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tạo tài khoản mới
                </button>
            </div>
        </header>

        <!-- Stats Metrics Row -->
        <section class="metrics-row" aria-label="Thống kê tài khoản hệ thống">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalAccounts ? (empty totalUsers ? 0 : totalUsers) : totalAccounts}</span>
                    <span class="stat-label">Tổng số tài khoản</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(0, 82, 204, 0.08); color: #0052cc;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty adminCount ? 0 : adminCount}</span>
                    <span class="stat-label">Admin hệ thống</span>
                    <span class="stat-trend stat-trend--up" style="color: #0052cc;">Quản trị tối cao</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(13, 148, 136, 0.08); color: #0d9488;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M2 22s2-4 10-4 10 4 10 4M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty coiThiCount ? 0 : coiThiCount}</span>
                    <span class="stat-label">Cán bộ coi thi</span>
                    <span class="stat-trend stat-trend--up" style="color: #0d9488;">Giám sát phòng thi</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(124, 58, 237, 0.08); color: #7c3aed;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v8M3 10V6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M7 14h.01M7 7h.01" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty chamThiCount ? 0 : chamThiCount}</span>
                    <span class="stat-label">Giám khảo chấm thi</span>
                    <span class="stat-trend stat-trend--up" style="color: #7c3aed;">Đánh giá sát hạch</span>
                </div>
            </div>
        </section>

        <!-- Filter & Search Panel -->
        <section class="filter-panel" aria-label="Bộ lọc tài khoản">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm kiếm tài khoản</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên đăng nhập, họ tên, email, sđt..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterRole" class="input-label">Vai trò phân quyền</label>
                        <select id="filterRole" name="filterRole" class="input-field">
                            <option value="">Tất cả vai trò</option>
                            <option value="admin" ${param.filterRole eq 'admin' ? 'selected' : ''}>Quản trị viên (Admin)</option>
                            <option value="coi_thi" ${param.filterRole eq 'coi_thi' ? 'selected' : ''}>Cán bộ coi thi</option>
                            <option value="cham_thi" ${param.filterRole eq 'cham_thi' ? 'selected' : ''}>Giám khảo chấm thi</option>
                            <option value="candidate" ${param.filterRole eq 'candidate' ? 'selected' : ''}>Thí sinh tự do</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái tài khoản</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="locked" ${param.filterStatus eq 'locked' ? 'selected' : ''}>Đang bị khóa</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Vô hiệu hóa</option>
                        </select>
                    </div>
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Lọc
                            </button>
                            <a href="${pageContext.request.contextPath}/views/admin/accounts.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Accounts Data Table Section -->
        <section class="log-card" aria-label="Danh sách tài khoản hệ thống">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="17" viewBox="0 0 20 17" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="2"/>
                        <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh sách tài khoản hệ thống
                    <c:if test="${not empty accounts}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(accounts)} tài khoản
                        </span>
                    </c:if>
                </h2>
                <div class="log-card-actions">
                    <button class="btn-export">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        In danh sách
                    </button>
                </div>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="min-width: 200px;">Tên tài khoản</th>
                            <th scope="col" style="min-width: 180px;">Email & SĐT</th>
                            <th scope="col" style="width: 150px; text-align: center;">Vai trò</th>
                            <th scope="col" style="min-width: 180px;">Trung tâm / Đơn vị</th>
                            <th scope="col" style="width: 140px; text-align: center;">Ngày tạo</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 220px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty accounts}">
                                <c:forEach var="acc" items="${accounts}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td>
                                            <div class="user-cell">
                                                <c:choose>
                                                    <c:when test="${acc.role eq 'admin'}">
                                                        <div class="user-avatar" title="Quản trị viên">
                                                            ${fn:substring(acc.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${acc.role eq 'coi_thi'}">
                                                        <div class="user-avatar user-avatar--teal" title="Cán bộ coi thi">
                                                            ${fn:substring(acc.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${acc.role eq 'cham_thi'}">
                                                        <div class="user-avatar user-avatar--purple" title="Giám khảo chấm thi">
                                                            ${fn:substring(acc.fullName, 0, 1)}
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="user-avatar user-avatar--orange" title="Thí sinh">
                                                            ${fn:substring(acc.fullName, 0, 1)}
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div class="user-info">
                                                    <span class="user-name" style="font-weight: 600; color: #0f172a;">${acc.fullName}</span>
                                                    <span class="user-username">@${acc.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500; color: #334155; font-size: 0.88rem;">${acc.email}</div>
                                            <div style="font-size: 0.75rem; color: #64748b; margin-top: 2px;">
                                                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="display: inline-block; vertical-align: middle; margin-right: 3px;">
                                                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                </svg>${acc.phone}
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${acc.role eq 'admin'}">
                                                    <span class="role-badge role-badge--admin">Admin</span>
                                                </c:when>
                                                <c:when test="${acc.role eq 'coi_thi'}">
                                                    <span class="role-badge role-badge--coi">Cán bộ coi thi</span>
                                                </c:when>
                                                <c:when test="${acc.role eq 'cham_thi'}">
                                                    <span class="role-badge role-badge--cham">Giám khảo</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--other">Thí sinh</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span style="font-weight: 500; color: #475569; font-size: 0.88rem;">${acc.department}</span>
                                        </td>
                                        <td style="text-align: center; font-size: 0.82rem; color: #64748b; font-weight: 500;">
                                            <fmt:formatDate value="${acc.createdAt}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${acc.status eq 'active'}">
                                                    <span class="action-badge action-badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${acc.status eq 'locked'}">
                                                    <span class="action-badge action-badge--warning">Bị khóa</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger">Vô hiệu</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706;"
                                                        onclick="editAccount('${acc.id}', '${acc.fullName}')">
                                                    Sửa
                                                </button>
                                                <c:choose>
                                                    <c:when test="${acc.status eq 'locked'}">
                                                        <button class="btn-export"
                                                                style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(16,185,129,0.25); color: #059669;"
                                                                onclick="toggleLockAccount('${acc.id}', '${acc.fullName}', false)">
                                                            Mở khóa
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button class="btn-export"
                                                                style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                                onclick="toggleLockAccount('${acc.id}', '${acc.fullName}', true)">
                                                            Khóa
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(100,116,139,0.25); color: #475569;"
                                                        onclick="deleteAccount('${acc.id}', '${acc.fullName}')">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="2"/>
                                            <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có tài khoản nào được khai báo trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 440px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Tạo tài khoản mới</strong> để cấp phát thông tin đăng nhập và phân quyền cho người dùng hệ thống đầu tiên.
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
                        <c:when test="${not empty accounts}">1 - ${fn:length(accounts)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty totalAccounts}">${totalAccounts}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    tài khoản người dùng
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

<!-- Interactive Interactions Script -->
<script>
    function editAccount(accId, fullName) {
        console.log('Edit account:', accId, fullName);
        alert('Đang mở form chỉnh sửa thông tin cho tài khoản: ' + fullName + '\n(Tính năng liên kết với servlet cập nhật thông tin)');
    }

    function toggleLockAccount(accId, fullName, shouldLock) {
        const actionText = shouldLock ? 'khóa' : 'mở khóa';
        const confirmMsg = 'Bạn có chắc chắn muốn ' + actionText + ' tài khoản của "' + fullName + '"?\n' + 
            (shouldLock ? 'Người dùng này sẽ không thể đăng nhập vào hệ thống cho đến khi được mở lại.' : 'Người dùng sẽ có thể đăng nhập bình thường.');
        
        if (confirm(confirmMsg)) {
            console.log('Toggle lock account:', accId, shouldLock);
            alert('Đã thực hiện ' + actionText + ' tài khoản "' + fullName + '" thành công!');
            window.location.reload();
        }
    }

    function deleteAccount(accId, fullName) {
        if (confirm('CẢNH BÁO: Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản của "' + fullName + '" khỏi hệ thống?\nHành động này không thể phục hồi và sẽ làm mất lịch sử thi/giám sát liên quan.')) {
            console.log('Deleted account:', accId);
            alert('Đã xóa tài khoản "' + fullName + '" thành công!');
            window.location.reload();
        }
    }
    
    // Add account interaction
    document.getElementById('btn-add-account').addEventListener('click', function() {
        alert('Đang chuyển hướng đến biểu mẫu tạo tài khoản hệ thống mới...\n(Tính năng liên kết với Servlet tạo tài khoản)');
    });
</script>

</body>
</html>
