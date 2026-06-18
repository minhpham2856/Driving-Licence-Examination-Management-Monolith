<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Phòng thi - Lái Vui</title>

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
    <jsp:param name="activeSidebar" value="phong-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <c:if test="${not empty param.areaId}">
                <a href="${pageContext.request.contextPath}/views/admin/exam-area.jsp">Khu vực thi</a>
                <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            </c:if>
            <span class="breadcrumbs__current" aria-current="page">Phòng thi</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Phòng thi</h1>
                <p class="page-subtitle">
                    <c:choose>
                        <c:when test="${not empty selectedAreaName}">
                            Danh sách phòng thi thuộc khu vực <strong>${selectedAreaName}</strong>. Cấu hình sức chứa, loại phòng và trạng thái hoạt động.
                        </c:when>
                        <c:otherwise>
                            Cấu hình phòng thi lý thuyết và thực hành, thiết lập sức chứa tối đa và trạng thái hoạt động toàn hệ thống.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <c:if test="${not empty param.areaId}">
                    <a href="${pageContext.request.contextPath}/views/admin/exam-area.jsp"
                       class="btn-export"
                       style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Về khu vực thi
                    </a>
                </c:if>
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </button>
                <button class="btn-filter" id="btn-add-room" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm phòng thi
                </button>
            </div>
        </header>

        <!-- Stats Row -->
        <section class="metrics-row" aria-label="Thống kê phòng thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalRooms ? 0 : totalRooms}</span>
                    <span class="stat-label">Tổng phòng thi</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty activeRooms ? 0 : activeRooms}</span>
                    <span class="stat-label">Đang hoạt động</span>
                    <span class="stat-trend stat-trend--up">Sẵn sàng sử dụng</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty theoryRooms ? 0 : theoryRooms}</span>
                    <span class="stat-label">Phòng lý thuyết</span>
                    <span class="stat-trend stat-trend--up">Thi trắc nghiệm</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty practicalRooms ? 0 : practicalRooms}</span>
                    <span class="stat-label">Phòng thực hành</span>
                    <span class="stat-trend stat-trend--up">Sân thi sa hình</span>
                </div>
            </div>
        </section>

        <!-- Filter & Search Panel -->
        <section class="filter-panel" aria-label="Bộ lọc phòng thi">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <c:if test="${not empty param.areaId}">
                    <input type="hidden" name="areaId" value="${param.areaId}">
                </c:if>
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.1fr 1.1fr 1.75fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm phòng thi</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên hoặc mã phòng thi..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterArea" class="input-label">Khu vực thi</label>
                        <select id="filterArea" name="filterArea" class="input-field">
                            <option value="">Tất cả khu vực</option>
                            <c:forEach var="area" items="${examAreas}">
                                <option value="${area.id}" ${param.filterArea eq area.id ? 'selected' : ''}>${area.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterType" class="input-label">Loại phòng</label>
                        <select id="filterType" name="filterType" class="input-field">
                            <option value="">Tất cả loại</option>
                            <option value="theory" ${param.filterType eq 'theory' ? 'selected' : ''}>Lý thuyết</option>
                            <option value="practical" ${param.filterType eq 'practical' ? 'selected' : ''}>Thực hành</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Hoạt động</option>
                            <option value="maintenance" ${param.filterStatus eq 'maintenance' ? 'selected' : ''}>Bảo trì</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Tạm dừng</option>
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
                            <a href="${pageContext.request.contextPath}/views/admin/exam-room.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Exam Rooms Data Table -->
        <section class="log-card" aria-label="Danh sách phòng thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                    Danh sách phòng thi
                    <c:if test="${not empty examRooms}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examRooms)} phòng
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
                            <th scope="col" style="width: 120px;">Mã phòng</th>
                            <th scope="col">Tên phòng thi</th>
                            <th scope="col">Khu vực</th>
                            <th scope="col" style="width: 130px; text-align: center;">Loại phòng</th>
                            <th scope="col" style="width: 100px; text-align: center;">Sức chứa</th>
                            <th scope="col" style="width: 110px; text-align: center;">Số máy thi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 210px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examRooms}">
                                <c:forEach var="room" items="${examRooms}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${room.code}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar"
                                                     style="background: ${room.type eq 'theory' ? 'linear-gradient(135deg,#0052cc,#003d9b)' : 'linear-gradient(135deg,#10b981,#059669)'}; border-radius: 8px;">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2"/>
                                                    </svg>
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${room.name}</span>
                                                    <span class="user-username">Tầng ${empty room.floor ? '-' : room.floor}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="user-info">
                                                <span class="user-name" style="font-size: 0.88rem;">${room.areaName}</span>
                                                <span class="user-username">${room.areaCode}</span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.type eq 'theory'}">
                                                    <span class="role-badge role-badge--admin">Lý thuyết</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--coi">Thực hành</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${empty room.capacity ? '-' : room.capacity}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; display: block;">người</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.type eq 'theory'}">
                                                    <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${empty room.computerCount ? 0 : room.computerCount}</span>
                                                    <span style="font-size: 0.75rem; color: #64748b; display: block;">máy thi</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #94a3b8; font-size: 0.85rem;">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.status eq 'active'}">
                                                    <span class="action-badge action-badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${room.status eq 'maintenance'}">
                                                    <span class="action-badge action-badge--warning">Bảo trì</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger">Tạm dừng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 5px; justify-content: center; flex-wrap: wrap;">
                                                <c:if test="${room.type eq 'theory'}">
                                                    <a href="${pageContext.request.contextPath}/views/admin/exam-computer.jsp?roomId=${room.id}"
                                                       class="btn-export"
                                                       style="padding: 4px 8px; font-size: 0.78rem; border-radius: 6px; border-color: rgba(139,92,246,0.25); color: #7c3aed; text-decoration: none;">
                                                        Máy thi
                                                    </a>
                                                </c:if>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706;"
                                                        onclick="editRoom('${room.id}')">
                                                    Sửa
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                        onclick="deleteRoom('${room.id}', '${room.name}')">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                        </svg>
                                        Chưa có phòng thi nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 420px; margin-left: auto; margin-right: auto;">
                                            Nhấn <strong>Thêm phòng thi</strong> để cấu hình phòng thi đầu tiên, hoặc điều chỉnh bộ lọc để tìm phòng phù hợp.
                                        </p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <footer class="pagination-footer">
                <div class="pagination-info">
                    Hiển thị
                    <c:choose>
                        <c:when test="${not empty examRooms}">1 - ${fn:length(examRooms)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty totalRooms}">${totalRooms}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    phòng thi
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

<script>
    function editRoom(roomId) {
        // TODO: bind to Edit modal or servlet route
        console.log('Edit room:', roomId);
    }
    function deleteRoom(roomId, roomName) {
        if (confirm('Bạn có chắc chắn muốn xóa phòng thi "' + roomName + '"?\nThao tác này không thể hoàn tác.')) {
            // TODO: bind to Delete servlet route
            console.log('Delete room:', roomId);
        }
    }
</script>

</body>
</html>
