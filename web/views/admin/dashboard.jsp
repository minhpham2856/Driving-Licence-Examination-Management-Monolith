<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Quản trị - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">

</head>
<body class="has-side-nav-bar">

<%-- Inject the admin sidebar template --%>
<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản trị</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Dashboard</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Dashboard Quản trị</h1>
                <p class="page-subtitle">Tổng quan trạng thái toàn hệ thống, cấu hình và quản trị các module chức năng chính.</p>
            </div>
            
            <!-- Quick Actions on Header -->
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất báo cáo
                </button>
                <a href="${pageContext.request.contextPath}/views/examiner/audit.jsp" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; text-decoration: none; background-color: #0052cc; border-color: #0052cc; display: inline-flex; align-items: center; justify-content: center; gap: 5px; color: #ffffff;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Nhật ký kiểm toán
                </a>
            </div>
        </header>

        <!-- Dynamic Metrics (KPI Stat Cards - Bounded to Backend Variables with Fallbacks) -->
        <section class="metrics-row" aria-label="Thống kê hệ thống">
            <!-- Card 1: Total Centers -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 21h18M3 7V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v2M5 21V7M19 21V7M9 7h6M9 11h6M9 15h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalExamCenters ? 0 : totalExamCenters}</span>
                    <span class="stat-label">Trung tâm thi</span>
                    <span class="stat-trend stat-trend--up">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Trạng thái hoạt động
                    </span>
                </div>
            </div>
            
            <!-- Card 2: Total Sessions -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalExamSessions ? 0 : totalExamSessions}</span>
                    <span class="stat-label">Kỳ thi đã mở</span>
                    <span class="stat-trend stat-trend--up">
                        Số lượng kỳ thi
                    </span>
                </div>
            </div>
            
            <!-- Card 3: Total Users -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalUsers ? 0 : totalUsers}</span>
                    <span class="stat-label">Tài khoản hệ thống</span>
                    <span class="stat-trend stat-trend--up">
                        Người dùng hệ thống
                    </span>
                </div>
            </div>
            
            <!-- Card 4: Rooms and Computers -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="font-size: 1.5rem; line-height: 1.8rem;">
                        ${empty totalExamRooms ? 0 : totalExamRooms} / ${empty totalComputers ? 0 : totalComputers}
                    </span>
                    <span class="stat-label">Phòng thi / Máy thi</span>
                    <span class="stat-trend stat-trend--up">
                        Thiết bị phần cứng
                    </span>
                </div>
            </div>
        </section>

        <!-- Quick Administration Modules Navigation Grid -->
        <h2 class="section-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Quản trị phân hệ chức năng
        </h2>
        
        <section class="admin-grid" aria-label="Phân hệ quản trị">
            
            <!-- Card 1: Khu vực thi (SC-071) -->
            <a href="${pageContext.request.contextPath}/views/admin/exam-area.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Khu vực thi</h3>
                </div>
                <p class="admin-nav-card__desc">Quản lý khu vực sát hạch, thông tin địa chỉ chi tiết và các cơ sở trực thuộc trung tâm.</p>
                <div class="admin-nav-card__footer">
                    Quản lý khu vực
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 2: Phòng thi (SC-072) -->
            <a href="${pageContext.request.contextPath}/views/admin/exam-room.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Phòng thi</h3>
                </div>
                <p class="admin-nav-card__desc">Cấu hình phòng thi lý thuyết và thực hành, thiết lập sức chứa tối đa và trạng thái hoạt động.</p>
                <div class="admin-nav-card__footer">
                    Quản lý phòng thi
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 3: Máy thi (SC-073) -->
            <a href="${pageContext.request.contextPath}/views/admin/exam-computer.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="2" y="4" width="20" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M8 20H16M12 17V20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Máy thi</h3>
                </div>
                <p class="admin-nav-card__desc">Quản lý hệ thống máy tính thi lý thuyết, cấu hình địa chỉ IP tĩnh và kiểm tra kết nối mạng.</p>
                <div class="admin-nav-card__footer">
                    Quản lý máy thi
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 4: Hạng GPLX (SC-074) -->
            <a href="${pageContext.request.contextPath}/views/admin/licence-class.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="1" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                            <circle cx="6" cy="10" r="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M10 7.5H16M10 10H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Hạng GPLX</h3>
                </div>
                <p class="admin-nav-card__desc">Cấu hình các hạng bằng lái xe (A1, A2, B2, C...), số câu hỏi thi, thời gian và điểm chuẩn đậu.</p>
                <div class="admin-nav-card__footer">
                    Quản lý hạng bằng
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 5: Lệ phí thi (SC-075) -->
            <a href="${pageContext.request.contextPath}/views/admin/exam-fee.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M2 10H22M6 15H8M10 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Lệ phí thi</h3>
                </div>
                <p class="admin-nav-card__desc">Thiết lập biểu phí đăng ký, phí thi lý thuyết và phí thi thực hành cho từng loại bằng lái xe.</p>
                <div class="admin-nav-card__footer">
                    Quản lý lệ phí
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 6: Tài khoản hệ thống (SC-076) -->
            <a href="${pageContext.request.contextPath}/views/admin/accounts.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 20 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="2"/>
                            <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Tài khoản</h3>
                </div>
                <p class="admin-nav-card__desc">Cấp mới, quản lý thông tin, phân quyền và khóa tài khoản các nhóm: Giám thị, Admin, Thí sinh.</p>
                <div class="admin-nav-card__footer">
                    Quản lý tài khoản
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>

            <!-- Card 7: Nhật ký hệ thống (SC-077) -->
            <a href="${pageContext.request.contextPath}/views/examiner/audit.jsp" class="admin-nav-card">
                <div class="admin-nav-card__header">
                    <div class="admin-nav-card__icon">
                        <svg width="20" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M8 1L2 3.5V9.5C2 13.64 4.69 17.44 8 18.5C11.31 17.44 14 13.64 14 9.5V3.5L8 1Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h3 class="admin-nav-card__title">Nhật ký hệ thống</h3>
                </div>
                <p class="admin-nav-card__desc">Theo dõi nhật ký kiểm toán, ghi nhận toàn bộ hoạt động đăng nhập, sửa đổi thông tin trong hệ thống.</p>
                <div class="admin-nav-card__footer">
                    Xem nhật ký
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
            </a>
            
        </section>

        <!-- Main Audit Logs Table Section (SC-077) -->
        <section class="log-card" aria-label="Bảng danh sách hoạt động gần đây">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Hoạt động hệ thống gần đây
                </h2>
                
                <div class="log-card-actions">
                    <a href="${pageContext.request.contextPath}/views/examiner/audit.jsp" class="btn-export" style="text-decoration: none; line-height: 24px; font-weight: 500;">
                        Xem toàn bộ nhật ký
                    </a>
                </div>
            </header>
            
            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 160px;">Thời gian</th>
                            <th scope="col">Tài khoản</th>
                            <th scope="col">Hành động tác vụ</th>
                            <th scope="col" style="width: 140px;">Phân hệ</th>
                            <th scope="col">Địa chỉ IP</th>
                            <th scope="col" style="text-align: center; width: 120px;">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty auditLogs}">
                                <c:forEach var="log" items="${auditLogs}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td class="col-time">${log.timestamp}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar" style="background-color: #0052cc; color: white;">
                                                    ${fn:substring(log.username, 0, 1)}
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${log.fullName}</span>
                                                    <span class="user-username">@${log.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>${log.action}</td>
                                        <td>
                                            <span class="role-badge role-badge--admin">${log.module}</span>
                                        </td>
                                        <td class="ip-cell">${log.ipAddress}</td>
                                        <td style="text-align: center;">
                                            <span class="action-badge ${log.statusKey eq 'success' ? 'action-badge--success' : 'action-badge--danger'}">
                                                ${log.status}
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Không tìm thấy nhật ký hoạt động nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">Hệ thống chưa ghi nhận thao tác hoặc lịch sử kiểm toán nào từ quản trị viên.</p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
