<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách thí sinh - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="ds-thi-sinh" />
</jsp:include>

<div class="dashboard-shell">

    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Danh sách thí sinh</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Danh sách thí sinh</h1>
                <p class="page-subtitle">Quản lý thông tin thí sinh, tra cứu hồ sơ đăng ký, trạng thái và kết quả sát hạch lái xe.</p>
            </div>
            
            <!-- Quick Actions on Header -->
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="candidate-call.jsp" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; text-decoration: none; background-color: #0052cc; border-color: #0052cc;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Điều hành gọi thi
                </a>
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Nhập từ Excel
                </button>
                <button class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm thí sinh
                </button>
            </div>
        </header>

        <!-- Dynamic Metrics (KPI Stat Cards - Bounded to Backend Variables with Demo Fallbacks) -->
        <section class="metrics-row" aria-label="Thống kê thí sinh">
            <!-- Card 1: Total Candidates -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalExaminees ? 0 : totalExaminees}</span>
                    <span class="stat-label">Tổng thí sinh</span>
                    <span class="stat-trend ${totalExamineesTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                        <c:choose>
                            <c:when test="${totalExamineesTrendDirection eq 'down'}">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:when>
                            <c:otherwise>
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:otherwise>
                        </c:choose>
                        ${empty totalExamineesTrend ? '+0 đợt này' : totalExamineesTrend}
                    </span>
                </div>
            </div>
            
            <!-- Card 2: Passed Candidates -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty passedExaminees ? 0 : passedExaminees}</span>
                    <span class="stat-label">Đạt sát hạch</span>
                    <span class="stat-trend ${passedExamineesTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                        <c:choose>
                            <c:when test="${passedExamineesTrendDirection eq 'down'}">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:when>
                            <c:otherwise>
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:otherwise>
                        </c:choose>
                        ${empty passedExamineesTrend ? '0% tỷ lệ đạt' : passedExamineesTrend}
                    </span>
                </div>
            </div>
            
            <!-- Card 3: Failed Candidates -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty failedExaminees ? 0 : failedExaminees}</span>
                    <span class="stat-label">Không đạt / Trượt</span>
                    <span class="stat-trend ${failedExamineesTrendDirection eq 'up' ? 'stat-trend--down' : 'stat-trend--up'}">
                        <c:choose>
                            <c:when test="${failedExamineesTrendDirection eq 'up'}">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:when>
                            <c:otherwise>
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </c:otherwise>
                        </c:choose>
                        ${empty failedExamineesTrend ? '0% tỷ lệ trượt' : failedExamineesTrend}
                    </span>
                </div>
            </div>
            
            <!-- Card 4: Pending Candidates -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty pendingExaminees ? 0 : pendingExaminees}</span>
                    <span class="stat-label">Chờ thi sát hạch</span>
                    <span class="stat-trend ${pendingExamineesTrendDirection eq 'down' ? 'stat-trend--down' : 'stat-trend--up'}">
                        ${empty pendingExamineesTrend ? 'Không đổi tuần này' : pendingExamineesTrend}
                    </span>
                </div>
            </div>
        </section>

        <!-- Filters & Search Form Section (Optimized grid columns to prevent button overflow) -->
        <section class="filter-panel" aria-label="Bộ lọc tìm kiếm">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 1.5fr 1.8fr;">
                    <!-- Search Keyword -->
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm thí sinh</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field" placeholder="Nhập tên, SBD, CCCD..." value="${param.searchKeyword}">
                    </div>
                    
                    <!-- Exam Session Dropdown -->
                    <div class="input-group">
                        <label for="filterSession" class="input-label">Đợt thi</label>
                        <select id="filterSession" name="filterSession" class="input-field">
                            <option value="">Tất cả đợt thi</option>
                            <option value="Khóa 01" ${param.filterSession eq 'Khóa 01' ? 'selected' : ''}>Khóa thi A1 - 24/05/2026</option>
                            <option value="Khóa 02" ${param.filterSession eq 'Khóa 02' ? 'selected' : ''}>Khóa thi B2 - 15/06/2026</option>
                            <option value="Khóa 03" ${param.filterSession eq 'Khóa 03' ? 'selected' : ''}>Khóa thi A2 - 28/06/2026</option>
                        </select>
                    </div>
                    
                    <!-- License Class Dropdown -->
                    <div class="input-group">
                        <label for="filterClass" class="input-label">Hạng bằng</label>
                        <select id="filterClass" name="filterClass" class="input-field">
                            <option value="">Tất cả hạng bằng</option>
                            <option value="A1" ${param.filterClass eq 'A1' ? 'selected' : ''}>Hạng A1</option>
                            <option value="A2" ${param.filterClass eq 'A2' ? 'selected' : ''}>Hạng A2</option>
                            <option value="B1" ${param.filterClass eq 'B1' ? 'selected' : ''}>Hạng B1</option>
                            <option value="B2" ${param.filterClass eq 'B2' ? 'selected' : ''}>Hạng B2</option>
                            <option value="C" ${param.filterClass eq 'C' ? 'selected' : ''}>Hạng C</option>
                        </select>
                    </div>
                    
                    <!-- Status Dropdown -->
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="Đạt" ${param.filterStatus eq 'Đạt' ? 'selected' : ''}>Đạt</option>
                            <option value="Chưa đạt" ${param.filterStatus eq 'Chưa đạt' ? 'selected' : ''}>Chưa đạt</option>
                            <option value="Chờ thi" ${param.filterStatus eq 'Chờ thi' ? 'selected' : ''}>Chờ thi</option>
                            <option value="Đang thi" ${param.filterStatus eq 'Đang thi' ? 'selected' : ''}>Đang thi</option>
                        </select>
                    </div>
                    
                    <!-- Search Button -->
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Lọc
                            </button>
                            <a href="dashboard.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Main Candidates Table Section -->
        <section class="log-card" aria-label="Bảng danh sách thí sinh">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Hồ sơ thí sinh đăng ký
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
                        In danh sách
                    </button>
                </div>
            </header>
            
            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 120px;">SBD</th>
                            <th scope="col">Họ và tên thí sinh</th>
                            <th scope="col">CCCD / Hộ chiếu</th>
                            <th scope="col" style="width: 110px;">Hạng đăng ký</th>
                            <th scope="col">Đợt thi sát hạch</th>
                            <th scope="col">Trạng thái thi</th>
                            <th scope="col" style="text-align: center; width: 180px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examinees}">
                                <!-- KHI CÓ DỮ LIỆU THỰC TẾ TỪ BACKEND -->
                                <c:forEach var="candidate" items="${examinees}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td class="col-time" style="color: #0052cc; font-weight: 600;">${candidate.sbd}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar ${candidate.avatarClass}">
                                                    ${fn:substring(candidate.fullName, 0, 1)}
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${candidate.fullName}</span>
                                                    <span class="user-username">@${candidate.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="ip-cell" style="font-family: inherit; font-size: 0.9rem;">
                                            <span>${candidate.cccd}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${candidate.licenseClass eq 'A1'}">
                                                    <span class="role-badge role-badge--coi">Hạng A1</span>
                                                </c:when>
                                                <c:when test="${candidate.licenseClass eq 'A2'}">
                                                    <span class="role-badge role-badge--coi" style="background-color: rgba(13, 148, 136, 0.12); color: #0d9488;">Hạng A2</span>
                                                </c:when>
                                                <c:when test="${candidate.licenseClass eq 'B1'}">
                                                    <span class="role-badge role-badge--admin" style="background-color: rgba(0, 82, 204, 0.12); color: #0052cc;">Hạng B1</span>
                                                </c:when>
                                                <c:when test="${candidate.licenseClass eq 'B2'}">
                                                    <span class="role-badge role-badge--admin">Hạng B2</span>
                                                </c:when>
                                                <c:when test="${candidate.licenseClass eq 'C'}">
                                                    <span class="role-badge role-badge--cham">Hạng C</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--other">${candidate.licenseClass}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="details-cell" style="max-width: 240px;">${candidate.sessionName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${candidate.statusKey eq 'success'}">
                                                    <span class="action-badge action-badge--success">${candidate.status}</span>
                                                </c:when>
                                                <c:when test="${candidate.statusKey eq 'danger'}">
                                                    <span class="action-badge action-badge--danger">${candidate.status}</span>
                                                </c:when>
                                                <c:when test="${candidate.statusKey eq 'warning'}">
                                                    <span class="action-badge action-badge--warning">${candidate.status}</span>
                                                </c:when>
                                                <c:when test="${candidate.statusKey eq 'info'}">
                                                    <span class="action-badge action-badge--info">${candidate.status}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge">${candidate.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <a href="candidate-call.jsp?sbd=${candidate.sbd}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(16, 185, 129, 0.25); color: #059669; font-weight: 700;">Gọi thi</a>
                                                <a href="candidate-detail.jsp?id=${candidate.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px;">Xem</a>
                                                <a href="edit-candidate.jsp?id=${candidate.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(0, 82, 204, 0.25); color: #0052cc;">Sửa</a>
                                                <a href="delete-candidate.jsp?id=${candidate.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239, 68, 68, 0.25); color: #dc2626;">Xóa</a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <!-- TRANG FRONT-END TRỐNG HOÀN TOÀN KHI CHƯA CÓ BACKEND TRUYỀN DỮ LIỆU (TƯƠNG TỰ AUDIT.JSP) -->
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <img src="${pageContext.request.contextPath}/assets/imgs/empty-candidates.svg" alt="Không có thí sinh" style="width: 64px; height: 64px; margin: 0 auto 1.5rem; display: block; opacity: 0.25;">
                                        Không tìm thấy hồ sơ thí sinh nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">Vui lòng thêm mới thí sinh thủ công hoặc nhập danh sách thí sinh trực tiếp từ file Excel bằng các nút chức năng phía trên.</p>
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
                    Hiển thị <c:choose><c:when test="${not empty examinees}">1 - ${fn:length(examinees)}</c:when><c:otherwise>0</c:otherwise></c:choose> trong tổng số <c:choose><c:when test="${not empty examinees}">${fn:length(examinees)}</c:when><c:otherwise>0</c:otherwise></c:choose> thí sinh
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

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
