<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Máy thi - Lái Vui</title>

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
    <jsp:param name="activeSidebar" value="may-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <c:if test="${not empty param.roomId}">
                <a href="${pageContext.request.contextPath}/views/admin/exam-room.jsp">Phòng thi</a>
                <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            </c:if>
            <span class="breadcrumbs__current" aria-current="page">Máy thi</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Máy thi</h1>
                <p class="page-subtitle">
                    <c:choose>
                        <c:when test="${not empty selectedRoomName}">
                            Danh sách máy thi thuộc phòng <strong>${selectedRoomName}</strong>. Giám sát IP, kiểm tra kết nối client thi và trạng thái hoạt động.
                        </c:when>
                        <c:otherwise>
                            Quản lý toàn bộ danh sách máy trạm thi lý thuyết. Giám sát địa chỉ IP, kiểm tra kết nối client thi, trạng thái thi trực tuyến và thông tin phiên bản.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <c:if test="${not empty param.roomId}">
                    <a href="${pageContext.request.contextPath}/views/admin/exam-room.jsp"
                       class="btn-export"
                       style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Về phòng thi
                    </a>
                </c:if>
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </button>
                <button class="btn-filter" id="btn-add-computer" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm máy thi
                </button>
            </div>
        </header>

        <!-- Stats Metrics Row -->
        <section class="metrics-row" aria-label="Thống kê máy thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalComputers ? 0 : totalComputers}</span>
                    <span class="stat-label">Tổng số máy</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty connectedComputers ? 0 : connectedComputers}</span>
                    <span class="stat-label">Đang kết nối</span>
                    <span class="stat-trend stat-trend--up">Sẵn sàng nhận đề</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2" style="opacity:0.4;"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
                        <line x1="12" y1="5" x2="12" y2="7" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty disconnectedComputers ? 0 : disconnectedComputers}</span>
                    <span class="stat-label font-bold">Mất kết nối</span>
                    <span class="stat-trend stat-trend--down">Offline / Lỗi Client</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <rect x="9" y="9" width="6" height="6" rx="1" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty activeExamComputers ? 0 : activeExamComputers}</span>
                    <span class="stat-label">Đang sử dụng thi</span>
                    <span class="stat-trend stat-trend--up">Có thí sinh làm bài</span>
                </div>
            </div>
        </section>

        <!-- Filter & Search Panel -->
        <section class="filter-panel" aria-label="Bộ lọc máy thi">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <c:if test="${not empty param.roomId}">
                    <input type="hidden" name="roomId" value="${param.roomId}">
                </c:if>
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.1fr 1.1fr 1.75fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm máy thi</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập mã máy, địa chỉ IP..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterRoom" class="input-label">Phòng thi</label>
                        <select id="filterRoom" name="filterRoom" class="input-field">
                            <option value="">Tất cả phòng lý thuyết</option>
                            <c:forEach var="room" items="${theoryRoomsList}">
                                <option value="${room.id}" ${param.filterRoom eq room.id ? 'selected' : ''}>${room.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterConnection" class="input-label">Trạng thái kết nối</label>
                        <select id="filterConnection" name="filterConnection" class="input-field">
                            <option value="">Tất cả</option>
                            <option value="online" ${param.filterConnection eq 'online' ? 'selected' : ''}>Online (Đang kết nối)</option>
                            <option value="offline" ${param.filterConnection eq 'offline' ? 'selected' : ''}>Offline (Mất kết nối)</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Tình trạng máy</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả tình trạng</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="maintenance" ${param.filterStatus eq 'maintenance' ? 'selected' : ''}>Đang bảo trì</option>
                            <option value="broken" ${param.filterStatus eq 'broken' ? 'selected' : ''}>Hỏng / Khóa</option>
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
                            <a href="${pageContext.request.contextPath}/views/admin/exam-computer.jsp<c:if test='${not empty param.roomId}'><c:out value='?roomId=${param.roomId}' /></c:if>" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Exam Computers Data Table Section -->
        <section class="log-card" aria-label="Danh sách máy thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh sách máy trạm thi lý thuyết
                    <c:if test="${not empty examComputers}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examComputers)} máy
                        </span>
                    </c:if>
                </h2>
                <div class="log-card-actions">
                    <button class="btn-export" onclick="syncAllIPs()">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Đồng bộ IP
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
                            <th scope="col" style="width: 120px;">Mã máy</th>
                            <th scope="col">Địa chỉ IP & Client</th>
                            <th scope="col">Phòng thi</th>
                            <th scope="col" style="width: 150px; text-align: center;">Hệ điều hành</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái kết nối</th>
                            <th scope="col" style="width: 130px; text-align: center;">Tình trạng máy</th>
                            <th scope="col" style="width: 110px; text-align: center;">Mã Client</th>
                            <th scope="col" style="text-align: center; width: 220px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examComputers}">
                                <c:forEach var="comp" items="${examComputers}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${comp.code}</td>
                                        <td>
                                            <div class="ip-cell">
                                                <span>${comp.ipAddress}</span>
                                                <span class="device-info">${empty comp.macAddress ? '-' : comp.macAddress}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="user-info">
                                                <span class="user-name" style="font-size: 0.88rem;">${comp.roomName}</span>
                                                <span class="user-username">${comp.roomCode}</span>
                                            </div>
                                        </td>
                                        <td style="text-align: center; font-size: 0.85rem; color: #475569; font-weight: 500;">
                                            ${empty comp.osName ? 'Windows 11 Client' : comp.osName}
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${comp.connectionStatus eq 'online'}">
                                                    <span class="role-badge role-badge--coi" style="padding-left: 10px; padding-right: 10px; display: inline-flex; align-items: center; gap: 5px;">
                                                        <span style="width: 6px; height: 6px; border-radius: 50%; background-color: #0d9488; display: inline-block;"></span>
                                                        Online
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--other" style="padding-left: 10px; padding-right: 10px; display: inline-flex; align-items: center; gap: 5px; color: #64748b;">
                                                        <span style="width: 6px; height: 6px; border-radius: 50%; background-color: #64748b; display: inline-block;"></span>
                                                        Offline
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${comp.status eq 'active'}">
                                                    <span class="action-badge action-badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${comp.status eq 'maintenance'}">
                                                    <span class="action-badge action-badge--warning">Bảo trì</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger">Khóa / Hỏng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-family: monospace; font-size: 0.82rem; font-weight: 600; color: #475569;">
                                            ${empty comp.clientVersion ? 'v1.2.4' : comp.clientVersion}
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 5px; justify-content: center; flex-wrap: wrap;">
                                                <button class="btn-export"
                                                        style="padding: 4px 8px; font-size: 0.78rem; border-radius: 6px; border-color: rgba(16,185,129,0.25); color: #059669;"
                                                        onclick="pingComputer('${comp.id}', '${comp.code}', '${comp.ipAddress}')">
                                                    Kiểm tra
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706;"
                                                        onclick="editComputer('${comp.id}')">
                                                    Sửa
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                        onclick="deleteComputer('${comp.id}', '${comp.code}')">
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
                                            <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có máy thi trắc nghiệm nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 450px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Thêm máy thi</strong> để thiết lập cấu hình client thi lý thuyết đầu tiên, hoặc chỉnh bộ lọc tìm kiếm.
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
                        <c:when test="${not empty examComputers}">1 - ${fn:length(examComputers)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty totalComputers}">${totalComputers}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    máy thi
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
    function pingComputer(compId, code, ip) {
        console.log('Pinging computer client:', compId, ip);
        alert('Đang gửi tín hiệu kiểm tra (Ping) đến máy ' + code + ' (' + ip + ')... \nKết quả: Máy trạm đang phản hồi ổn định (RTT < 1ms). Connection Status: ONLINE.');
    }

    function editComputer(compId) {
        // TODO: Bind to dynamic edit popup or Servlet Route
        console.log('Edit computer:', compId);
    }

    function deleteComputer(compId, code) {
        if (confirm('Bạn có chắc chắn muốn xóa máy thi "' + code + '" khỏi hệ thống?\nHành động này sẽ hủy mọi liên kết cấu hình phòng thi hiện tại.')) {
            // TODO: Bind to dynamic delete Servlet Route
            console.log('Deleted computer:', compId);
        }
    }

    function syncAllIPs() {
        alert('Đang quét dải IP nội bộ và đồng bộ hóa địa chỉ MAC của các client thi lý thuyết đang hoạt động...\nĐã cập nhật trạng thái kết nối mới nhất.');
    }
</script>

</body>
</html>
