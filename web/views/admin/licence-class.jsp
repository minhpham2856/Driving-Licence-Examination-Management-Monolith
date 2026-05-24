<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Hạng GPLX - Lái Vui</title>

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
    <jsp:param name="activeSidebar" value="hang-gplx" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/admin/dashboard.jsp">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Hạng GPLX</span>
        </nav>

        <!-- Page Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Hạng GPLX</h1>
                <p class="page-subtitle">Cấu hình danh mục hạng giấy phép lái xe, thiết lập thông số đề thi lý thuyết (số câu hỏi, điểm đạt, thời gian thi) và phạm vi điều khiển phương tiện.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất Excel
                </button>
                <button class="btn-filter" id="btn-add-class" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm hạng GPLX
                </button>
            </div>
        </header>

        <!-- Stats Metrics Row -->
        <section class="metrics-row" aria-label="Thống kê hạng giấy phép">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalClasses ? 0 : totalClasses}</span>
                    <span class="stat-label">Tổng số hạng</span>
                    <span class="stat-trend stat-trend--up">Toàn bộ danh mục</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(124, 58, 237, 0.08); color: #7c3aed;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 12c0-5.52-4.48-10-10-10S2 6.48 2 12s4.48 10 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <path d="M5.5 13.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM18.5 13.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty motoClasses ? 0 : motoClasses}</span>
                    <span class="stat-label">Hạng Xe Mô Tô</span>
                    <span class="stat-trend stat-trend--up" style="color: #7c3aed;">A1, A2, A3, A4</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-1.1 0-2 .9-2 2v7h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="7" cy="17" r="2" stroke="currentColor" stroke-width="2"/>
                        <circle cx="17" cy="17" r="2" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty carClasses ? 0 : carClasses}</span>
                    <span class="stat-label">Hạng Xe Ô Tô / Tải</span>
                    <span class="stat-trend stat-trend--up">B1, B2, C, D, E, F...</span>
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
                    <span class="stat-number">${empty activeClasses ? 0 : activeClasses}</span>
                    <span class="stat-label">Đang hoạt động</span>
                    <span class="stat-trend stat-trend--up">Sẵn sàng mở thi</span>
                </div>
            </div>
        </section>

        <!-- Filter & Search Panel -->
        <section class="filter-panel" aria-label="Bộ lọc hạng GPLX">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm hạng giấy phép</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập mã hạng, tên gọi..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterType" class="input-label">Loại phương tiện</label>
                        <select id="filterType" name="filterType" class="input-field">
                            <option value="">Tất cả loại</option>
                            <option value="moto" ${param.filterType eq 'moto' ? 'selected' : ''}>Xe mô tô (A1-A4)</option>
                            <option value="car" ${param.filterType eq 'car' ? 'selected' : ''}>Xe ô tô / tải / rơ-moóc</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Tạm ngưng</option>
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
                            <a href="${pageContext.request.contextPath}/views/admin/licence-class.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <!-- Licence Classes Data Table Section -->
        <section class="log-card" aria-label="Danh sách hạng GPLX">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="1" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <circle cx="6" cy="10" r="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M10 7.5H16M10 10H14M10 12.5H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh mục Hạng Giấy phép lái xe
                    <c:if test="${not empty licenceClasses}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(licenceClasses)} hạng
                        </span>
                    </c:if>
                </h2>
                <div class="log-card-actions">
                    <button class="btn-export">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        In danh mục
                    </button>
                </div>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 100px; text-align: center;">Mã hạng</th>
                            <th scope="col">Tên gọi & Phạm vi điều khiển</th>
                            <th scope="col" style="width: 140px; text-align: center;">Loại xe</th>
                            <th scope="col" style="width: 130px; text-align: center;">Thời gian thi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Số câu hỏi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Yêu cầu đạt</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 160px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty licenceClasses}">
                                <c:forEach var="grade" items="${licenceClasses}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin"
                                                  style="font-size: 0.95rem; font-weight: 800; font-family: 'Inter', sans-serif; padding: 4px 14px; border-radius: 6px; background: rgba(0,82,204,0.06); color: #0052cc; border-color: rgba(0,82,204,0.18);">
                                                ${grade.code}
                                            </span>
                                        </td>
                                        <td>
                                            <div class="user-info" style="white-space: normal;">
                                                <span class="user-name" style="font-size: 0.92rem; font-weight: 600; color: #0f172a; white-space: normal;">
                                                    Hạng ${grade.code} — ${grade.name}
                                                </span>
                                                <span class="user-username" style="font-family: var(--font-body); font-size: 0.78rem; color: #64748b; margin-top: 3px; line-height: 1.45;">
                                                    ${empty grade.description ? 'Phạm vi sát hạch quốc gia theo quy định của Bộ Giao thông Vận tải.' : grade.description}
                                                </span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${grade.vehicleType eq 'moto'}">
                                                    <span class="role-badge role-badge--coi" style="color: #7c3aed; background-color: rgba(124, 58, 237, 0.06); border-color: rgba(124, 58, 237, 0.15);">Xe mô tô</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--other" style="color: #b45309; background-color: rgba(245, 158, 11, 0.06); border-color: rgba(245, 158, 11, 0.15);">Xe ô tô / tải</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${grade.examDuration}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; display: block;">phút</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${grade.theoryQuestions}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; display: block;">câu hỏi</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1.05rem; font-weight: 800; color: #059669;">${grade.minCorrectAnswers}</span>
                                            <span style="font-size: 0.72rem; color: #64748b; display: block;">câu đúng</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${grade.status eq 'active'}">
                                                    <span class="action-badge action-badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger">Tạm ngưng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706;"
                                                        onclick="editClass('${grade.id}')">
                                                    Sửa
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                        onclick="deleteClass('${grade.id}', '${grade.code}')">
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
                                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có hạng giấy phép lái xe nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 440px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Thêm hạng GPLX</strong> để bắt đầu khai báo danh mục sát hạch giấy phép lái xe quốc gia cho hệ thống.
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
                        <c:when test="${not empty licenceClasses}">1 - ${fn:length(licenceClasses)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số
                    <c:choose>
                        <c:when test="${not empty totalClasses}">${totalClasses}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    hạng GPLX
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
    function editClass(classId) {
        // TODO: Bind to dynamic edit popup or Servlet Route
        console.log('Edit licence class:', classId);
    }

    function deleteClass(classId, code) {
        if (confirm('Bạn có chắc chắn muốn xóa hạng giấy phép "' + code + '" khỏi hệ thống?\nHành động này sẽ xóa vĩnh viễn cấu hình các bộ đề và hồ sơ sát hạch tương ứng.')) {
            // TODO: Bind to dynamic delete Servlet Route
            console.log('Deleted licence class:', classId);
        }
    }
</script>

</body>
</html>
