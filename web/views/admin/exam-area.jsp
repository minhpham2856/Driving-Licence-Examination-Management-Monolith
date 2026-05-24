<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Khu vực thi - Lái Vui</title>

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
    <jsp:param name="activeSidebar" value="khu-vuc" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Khu vực thi</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Khu vực thi</h1>
                <p class="page-subtitle">Quản lý danh sách khu vực sát hạch, thông tin địa chỉ và cơ sở trực thuộc trung tâm.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </button>
                <button class="btn-filter" id="btn-add-area" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm khu vực
                </button>
            </div>
        </header>

        <!-- Filter & Search Panel -->
        <section class="filter-panel" aria-label="Bộ lọc tìm kiếm">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.5fr 1.5fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm khu vực</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên hoặc mã khu vực..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterProvince" class="input-label">Tỉnh / Thành phố</label>
                        <select id="filterProvince" name="filterProvince" class="input-field">
                            <option value="">Tất cả tỉnh thành</option>
                            <option value="HCM" ${param.filterProvince eq 'HCM' ? 'selected' : ''}>Hồ Chí Minh</option>
                            <option value="HN" ${param.filterProvince eq 'HN' ? 'selected' : ''}>Hà Nội</option>
                            <option value="DN" ${param.filterProvince eq 'DN' ? 'selected' : ''}>Đà Nẵng</option>
                            <option value="BD" ${param.filterProvince eq 'BD' ? 'selected' : ''}>Bình Dương</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="maintenance" ${param.filterStatus eq 'maintenance' ? 'selected' : ''}>Đang bảo trì</option>
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
                            <a href="${pageContext.request.contextPath}/views/admin/exam-area.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Exam Areas Data Table -->
        <section class="log-card" aria-label="Danh sách khu vực thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    Danh sách khu vực thi
                    <c:if test="${not empty examAreas}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examAreas)} khu vực
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
                            <th scope="col" style="width: 120px;">Mã khu vực</th>
                            <th scope="col">Tên khu vực thi</th>
                            <th scope="col">Địa chỉ</th>
                            <th scope="col" style="width: 130px; text-align: center;">Số phòng thi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 200px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examAreas}">
                                <c:forEach var="area" items="${examAreas}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${area.code}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar" style="background: linear-gradient(135deg, #0052cc, #003d9b); border-radius: 8px;">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2"/>
                                                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                                                    </svg>
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${area.name}</span>
                                                    <span class="user-username">${area.province}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="details-cell">${area.address}</td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1.1rem; font-weight: 700; color: #0f172a;">${area.roomCount}</span>
                                            <span style="font-size: 0.8rem; color: #64748b; display: block;">phòng thi</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${area.status eq 'active'}">
                                                    <span class="action-badge action-badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${area.status eq 'maintenance'}">
                                                    <span class="action-badge action-badge--warning">Bảo trì</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger">Tạm dừng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <a href="${pageContext.request.contextPath}/views/admin/exam-room.jsp?areaId=${area.id}"
                                                   class="btn-export"
                                                   style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(0,82,204,0.25); color: #0052cc; text-decoration: none;">
                                                    Xem phòng
                                                </a>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706;"
                                                        onclick="editArea('${area.id}')">
                                                    Sửa
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                        onclick="deleteArea('${area.id}', '${area.name}')">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                                        </svg>
                                        Chưa có khu vực thi nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">
                                            Nhấn <strong>Thêm khu vực</strong> phía trên để bắt đầu cấu hình khu vực thi đầu tiên cho hệ thống.
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
                        <c:when test="${not empty examAreas}">1 - ${fn:length(examAreas)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty totalAreas}">${totalAreas}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    khu vực
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
    function editArea(areaId) {
        // TODO: bind to Edit modal or servlet route
        console.log('Edit area:', areaId);
    }
    function deleteArea(areaId, areaName) {
        if (confirm('Bạn có chắc chắn muốn xóa khu vực "' + areaName + '"?\nThao tác này không thể hoàn tác.')) {
            // TODO: bind to Delete servlet route
            console.log('Delete area:', areaId);
        }
    }
</script>

</body>
</html>
