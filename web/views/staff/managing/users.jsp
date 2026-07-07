<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý học viên - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <jsp:include page="/views/staff/managing/components/staff-managing-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/staff/managing/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="hoc-vien" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${pageContext.request.contextPath}/views/staff/managing/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Danh sách học viên</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Danh Sách Học Viên</h1>
                <p class="page-subtitle">Danh sách học viên đăng ký chính khóa và thí sinh tự do nộp hồ sơ.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="${pageContext.request.contextPath}/manager/create-user" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #0052cc; border-color: #0052cc;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tạo tài khoản học viên
                </a>
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất danh sách Excel
                </button>
            </div>
        </header>

        <section class="filter-panel">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm học viên
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 1.25fr 1.8fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm học viên</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field" placeholder="Nhập tên, mã học viên, CCCD..." value="${param.searchKeyword}">
                    </div>
                    
                    <div class="input-group">
                        <label for="filterUserType" class="input-label">Loại học viên</label>
                        <select id="filterUserType" name="filterUserType" class="input-field">
                            <option value="">Tất cả</option>
                            <option value="student" ${param.filterUserType eq 'student' ? 'selected' : ''}>Học viên chính khóa</option>
                            <option value="free" ${param.filterUserType eq 'free' ? 'selected' : ''}>Thí sinh tự do</option>
                        </select>
                    </div>
                    
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
                    
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái hồ sơ</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="pending" ${param.filterStatus eq 'pending' ? 'selected' : ''}>Chờ duyệt</option>
                            <option value="approved" ${param.filterStatus eq 'approved' ? 'selected' : ''}>Đã duyệt hồ sơ</option>
                            <option value="rejected" ${param.filterStatus eq 'rejected' ? 'selected' : ''}>Từ chối hồ sơ</option>
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
                            <a href="users.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    Hồ sơ đăng ký học & thi
                </h2>
                
                <span class="action-badge action-badge--info" style="font-weight: 700;">
                    ${empty totalFilteredUsers ? 3 : totalFilteredUsers} học viên được lọc
                </span>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 80px; text-align: center;">Mã học viên</th>
                            <th scope="col">Họ và tên</th>
                            <th scope="col" style="width: 140px;">Số CCCD</th>
                            <th scope="col" style="width: 120px; text-align: center;">Hạng GPLX</th>
                            <th scope="col" style="width: 140px; text-align: center;">Loại hồ sơ</th>
                            <th scope="col" style="width: 150px;">Ngày đăng ký</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="width: 180px; text-align: center;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty usersList}">
                                <c:forEach var="user" items="${usersList}">
                                    <tr>
                                        <td style="text-align: center; font-weight: 700; color: #64748b;">${user.code}</td>
                                        <td>
                                            <div class="user-profile-cell" style="display: flex; align-items: center; gap: 8px;">
                                                <div class="profile-avatar-large profile-avatar--blue" style="width: 32px; height: 32px; font-size: 0.85rem; border: none; box-shadow: none;">
                                                    ${fn:substring(user.fullName, 0, 1)}
                                                </div>
                                                <div style="display: flex; flex-direction: column;">
                                                    <span class="user-name" style="font-weight: 700; color: #0f172a;">${user.fullName}</span>
                                                    <span class="user-username" style="font-size: 0.75rem; color: #64748b;">@${user.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="font-family: monospace; font-size: 0.9rem;">${user.cccd}</td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--coi" style="padding: 2px 8px; font-size: 0.75rem; font-weight: 700;">Hạng ${user.licenseClass}</span>
                                        </td>
                                        <td style="text-align: center; font-weight: 600; color: #475569;">
                                            ${user.type eq 'student' ? 'Học viên chính khóa' : 'Thí sinh tự do'}
                                        </td>
                                        <td style="color: #64748b; font-size: 0.85rem;">${user.registerDate}</td>
                                        <td style="text-align: center;">
                                            <span class="action-badge action-badge--${user.statusKey}">${user.status}</span>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <a href="user-detail.jsp?id=${user.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; text-decoration: none;">Xem chi tiết</a>
                                                <c:if test="${user.statusKey eq 'warning'}">
                                                    <a href="approve.jsp?id=${user.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(217, 119, 6, 0.25); color: #d97706; font-weight: 700; text-decoration: none;">Duyệt</a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 4rem 1.5rem; color: #64748b; font-weight: 500;">
                                        Không tìm thấy danh sách học viên nào.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            
            <footer class="pagination-footer">
                <div class="pagination-info">
                    <c:choose>
                        <c:when test="${not empty usersList}">
                            Hiển thị 1 - ${fn:length(usersList)} trong tổng số ${empty totalFilteredUsers ? fn:length(usersList) : totalFilteredUsers} học viên
                        </c:when>
                        <c:otherwise>
                            Hiển thị 0 học viên
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="pagination-nav">
                    <button class="page-btn page-btn--wide disabled" disabled>Trước</button>
                    <button class="page-btn active">1</button>
                    <button class="page-btn page-btn--wide disabled" disabled>Sau</button>
                </div>
            </footer>
        </section>

    </main>
</div>

</body>
</html>
