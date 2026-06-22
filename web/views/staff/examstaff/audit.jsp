<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Ensure selectedSessionId is loaded
    Integer sessIdObj = (Integer) session.getAttribute("selectedSessionId");
    int sessId = (sessIdObj != null) ? sessIdObj : 2; // Default B2 session

    // Ensure candidate queue is initialized
    java.util.List<Models.ExamRegistration> qList = (java.util.List<Models.ExamRegistration>) session.getAttribute("candidateQueue");
    if (qList == null) {
        DAOs.ExamRegistrationDAO regDAO = new DAOs.Impl.ExamRegistrationDAOImpl();
        try {
            qList = regDAOs.getCandidatesBySession(sessId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new java.util.ArrayList<>();
        }
        session.setAttribute("candidateQueue", qList);
    }

    // Load dynamic audit logs for the current logged-in user from DB SQL Server AuditLog
    Models.User user = (Models.User) session.getAttribute("user");
    int uId = (user != null) ? user.getUserId() : 3; // Default staff Trần Thị Thủ Tục (ID = 3)
    
    DAOs.AuditLogDAO logDAO = new DAOs.Impl.AuditLogDAOImpl();
    java.util.List<Models.AuditLog> personalLogs = null;
    String filterDate = request.getParameter("filterDate");
    try {
        if (filterDate != null && !filterDate.trim().isEmpty()) {
            personalLogs = logDAOs.getLogsByUserAndDate(uId, filterDate);
        } else {
            // Retrieve all logs from the beginning if no date filter is specified
            personalLogs = logDAOs.getLogsByUserAndDate(uId, null);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (personalLogs == null) {
        personalLogs = new java.util.ArrayList<>();
    }
    request.setAttribute("personalLogs", personalLogs);

    DTOs.StaffProcedureKpiDTO procedureKpi = logDAOs.getStaffProcedureKpi(uId, filterDate);
    request.setAttribute("myCompletedProcedures", procedureKpi.getCompletedCount());
    request.setAttribute("myTotalFees", procedureKpi.getTotalFees());
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký Cá Nhân - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="audit" />
</jsp:include>

<!-- Get current date dynamically using JSP useBean -->
<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate var="todayFormatted" value="${now}" pattern="dd/MM/yyyy" />

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Nhật ký cá nhân</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật ký hoạt động cá nhân</h1>
                <p class="page-subtitle">Xem lại lịch sử thao tác nghiệp vụ, đối chiếu hồ sơ học viên do chính bạn thực hiện trong ngày trực.</p>
            </div>
        </header>

        <!-- Personal Staff Profile Header -->
        <div class="staff-profile-card">
            <div class="profile-info-group">
                <!-- Visual initials avatar dynamically parsed from the staff name -->
                <div class="profile-avatar-circle">
                    ${fn:substring(sessionScope.user.profile.fullName, 0, 2)}
                </div>
                <div class="profile-meta-text">
                    <span style="font-size: 1.15rem; font-weight: 800;">${sessionScope.user.profile.fullName}</span>
                    <span style="font-size: 0.82rem; opacity: 0.85; font-family: monospace;">Tài khoản: @${sessionScope.user.username} | Mã cán bộ: CBSH-00${sessionScope.user.id}</span>
                </div>
            </div>
            
            <div style="text-align: right; font-size: 0.82rem; opacity: 0.9;">
                <span style="display: block; font-weight: 700; text-transform: uppercase;">Phạm vi nhật ký</span>
                <span style="font-size: 1.0rem; font-weight: 800;">
                    <c:choose>
                        <c:when test="${not empty param.filterDate}">
                            Ngày: ${param.filterDate}
                        </c:when>
                        <c:otherwise>
                            Tất cả lịch sử
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>

        <!-- Glassmorphic Date Filter Form -->
        <div style="background-color: #ffffff; border: 1px solid #cbd5e1; border-radius: 12px; padding: 15px; margin-top: 1.5rem; margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap;">
            <div style="display: flex; align-items: center; gap: 8px;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: #0052cc;">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span style="font-size: 0.9rem; font-weight: 700; color: #1e293b;">Bộ lọc thời gian nhật ký:</span>
            </div>
            
            <form action="audit.jsp" method="GET" style="display: flex; align-items: center; gap: 10px; margin: 0;">
                <input type="date" name="filterDate" value="${param.filterDate}" style="height: 38px; padding: 0 10px; border-radius: 8px; border: 1.5px solid #cbd5e1; font-weight: 600; color: #334155; outline: none; background-color: #ffffff; cursor: pointer;">
                <button type="submit" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.82rem; border-radius: 8px; font-weight: 700; background: linear-gradient(135deg, #0052cc, #003d9b); border: none; color: #ffffff; cursor: pointer; transition: all 0.2s;">
                    Lọc kết quả
                </button>
                <c:if test="${not empty param.filterDate}">
                    <a href="audit.jsp" style="font-size: 0.8rem; font-weight: 600; color: #ef4444; text-decoration: none; padding: 0 5px;">Xóa bộ lọc</a>
                </c:if>
            </form>
        </div>

        <!-- KPI Metrics Row (from Audit table) -->
        <section class="metrics-row" aria-label="Số liệu hoạt động cá nhân">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${fn:length(personalLogs)}</span>
                    <span class="stat-label">Tổng thao tác cá nhân</span>
                    <span class="stat-trend stat-trend--up">
                        <c:choose>
                            <c:when test="${not empty param.filterDate}">Trong ngày ${param.filterDate}</c:when>
                            <c:otherwise>Lịch sử tất cả thời gian</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber" style="background-color: rgba(126, 34, 206, 0.06); color: #7e22ce;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                        <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #7e22ce;">${requestScope.myCompletedProcedures}</span>
                    <span class="stat-label">Học viên đã làm thủ tục</span>
                    <span class="stat-trend stat-trend--up">Đã chụp ảnh và thanh toán (Payment + ảnh hồ sơ)</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">
                        <fmt:formatNumber value="${myTotalFees}" type="number" /> đ
                    </span>
                    <span class="stat-label">Lệ phí đã xác nhận thu</span>
                    <span class="stat-trend stat-trend--up">Tổng từ bảng Payment (TotalAmount)</span>
                </div>
            </div>
        </section>

        <!-- Audit Table Card -->
        <section class="log-card" style="margin-top: 1.5rem; margin-bottom: 2.5rem;">
            <header class="log-card-header" style="justify-content: space-between; display: flex;">
                <h2 class="log-card-title" style="font-size: 1rem; font-weight: 700; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v8M3 10V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v4" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M7 8h10M7 14h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <c:choose>
                        <c:when test="${not empty param.filterDate}">
                            Nhật ký hoạt động cá nhân ngày ${param.filterDate}
                        </c:when>
                        <c:otherwise>
                            Bảng kiểm toán tất cả hoạt động cá nhân
                        </c:otherwise>
                    </c:choose>
                </h2>
                
                <div class="log-card-actions">
                    <button class="btn-export" style="height: 36px; padding: 0 12px; font-size: 0.8rem; border-radius: 6px;">In nhật ký cá nhân</button>
                </div>
            </header>
            
            <div class="table-responsive" style="margin-top: 1rem;">
                <table class="audit-table" style="font-size: 0.88rem; width: 100%;">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 80px;" class="col-id">STT</th>
                            <th scope="col" style="width: 140px;">Thời gian</th>
                            <th scope="col" style="width: 150px;">Nghiệp vụ</th>
                            <th scope="col">Chi tiết thao tác ghi nhận kiểm toán</th>
                            <th scope="col" style="width: 140px; text-align: center;">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${requestScope.personalLogs}" varStatus="status">
                            <tr>
                                <td class="col-id">${status.index + 1}</td>
                                <td class="col-time">
                                    <fmt:formatDate value="${log.changedAt}" pattern="dd/MM HH:mm" />
                                </td>
                                <td>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 6px;">
                                        ${log.tableName}
                                    </span>
                                    <span style="display:block; font-size:0.68rem; color:#64748b; margin-top:2px;">${log.action}</span>
                                </td>
                                <td class="details-cell">
                                    ${log.details}
                                </td>
                                <td style="text-align: center;">
                                    <span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty requestScope.personalLogs}">
                            <tr>
                                <td colspan="5" style="text-align: center; color: #94a3b8; padding: 3rem;">
                                    Không có hoạt động thao tác nào của bạn được ghi nhận trên cơ sở dữ liệu trong hôm nay.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${pageContext.request.contextPath}/assets/js/audit.js"></script>
</body>
</html>
