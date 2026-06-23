<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quy Trình Phân Bổ Thí Sinh - Ban Sát Hạch</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="phan-bo" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Quy trình phân bổ</span>
        </nav>

        <jsp:include page="/views/layout/header-examstaff.jsp">
            <jsp:param name="pageTitle" value="Phân bổ thí sinh" />
            <jsp:param name="sectionTitle" value="Ban Sát Hạch" />
        </jsp:include>

        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Trình tự điều phối thí sinh</h1>
                <p class="page-subtitle">Quản lý danh sách thí sinh theo ca sát hạch và phân bổ vào phòng thi lý thuyết.</p>
            </div>
        </header>

        <!-- Exception / Warning Alert Notification Bars -->
        <c:if test="${not empty requestScope.errorMsg}">
            <div style="background-color: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b91c1c;">${requestScope.errorMsg}</span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.warningMsg}">
            <div style="background-color: #fffbeb; border: 1.5px solid #f59e0b; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" style="color: #f59e0b; flex-shrink: 0;">
                    <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b45309;">${requestScope.warningMsg}</span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.alertMsg}">
            <div style="background-color: #eff6ff; border: 1.5px solid #3b82f6; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" style="color: #3b82f6; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #1e3a8a;">${requestScope.alertMsg}</span>
            </div>
        </c:if>

        <!-- Allocation Control Dashboard -->
        <div class="report-pane" style="margin-top: 1.25rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <h2 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="color: #0052cc;">
                        <rect x="4" y="4" width="16" height="16" rx="2"/>
                        <path d="M9 9h6M9 13h6"/>
                    </svg>
                    Bảng điều phối ca sát hạch
                </h2>
                <span class="role-badge role-badge--admin" style="font-size: 0.78rem; font-weight: 800; padding: 4px 10px; border-radius: 6px;">
                    Tổng số: ${fn:length(requestScope.candidateQueue)} thí sinh
                </span>
            </div>

            <div style="display: flex; flex-wrap: wrap; gap: 1.5rem; justify-content: space-between; align-items: flex-end; margin-top: 1rem; border-top: 1.5px solid #f1f5f9; padding-top: 1rem;">
                <!-- Target Session Selection -->
                <form action="${ctx}/staff/examstaff/allocation" method="GET" style="display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 250px;">
                    <label for="sessionId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Chọn ca sát hạch mục tiêu:</label>
                    <div style="display: flex; gap: 8px;">
                        <select id="sessionId" name="sessionId" class="es-session-selector__select es-session-selector__select--wide">
                            <c:forEach var="sess" items="${requestScope.allSessions}">
                                <option value="${sess.id}" ${sessionScope.selectedSessionId eq sess.id ? 'selected' : ''}>
                                    Ca #${sess.id} - ${sess.sessionLabel} (${sess.licenseCode} | ${sess.status})
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn-batch btn-batch--alt" style="height: 38px; width: 38px; padding: 0; display: inline-flex; align-items: center; justify-content: center; border-radius: 8px;" title="Tải lại">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
                            </svg>
                        </button>
                    </div>
                </form>

                <!-- Auto allocate action -->
                <form action="${ctx}/staff/examstaff/allocation" method="GET" style="display: flex; gap: 8px; align-items: flex-end;">
                    <input type="hidden" name="action" value="autoAllocate">
                    <input type="hidden" name="sessionId" value="${sessionScope.selectedSessionId}">
                    <button type="submit" class="btn-export" style="height: 38px; padding: 0 1rem; font-size: 0.82rem; border-radius: 8px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; background-color: #0052cc; color: #ffffff; border-color: #0052cc;">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                        </svg>
                        Tự động phân bổ
                    </button>
                </form>
            </div>
        </div>

        <!-- Active theory rooms summary -->
        <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <h3 style="font-size: 1rem; font-weight: 800; color: #ea580c; margin: 0 0 1rem; display: flex; align-items: center; gap: 6px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <rect x="2" y="3" width="20" height="14" rx="2"/>
                    <path d="M8 21h8M12 17v4"/>
                </svg>
                Phòng thi lý thuyết đang hoạt động
            </h3>
            <c:choose>
                <c:when test="${not empty requestScope.activeTheoryRooms}">
                    <div style="display: flex; flex-wrap: wrap; gap: 0.75rem;">
                        <c:forEach var="room" items="${requestScope.activeTheoryRooms}">
                            <span class="role-badge role-badge--coi" style="font-size: 0.78rem; padding: 4px 10px; border-radius: 6px;">
                                ${room.areaName} (Sức chứa: ${empty room.capacity ? '—' : room.capacity})
                            </span>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; color: #94a3b8; font-size: 0.85rem; padding: 1rem;">
                        Không có phòng thi lý thuyết nào đang hoạt động.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Candidate queue table -->
        <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem; display: flex; align-items: center; gap: 6px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                </svg>
                Danh sách thí sinh trong ca
            </h3>
            <div class="table-responsive">
                <table class="audit-table" style="font-size: 0.88rem;">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 130px;">SBD</th>
                            <th scope="col">Họ và tên</th>
                            <th scope="col" style="width: 120px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="width: 100px; text-align: center;">Vắng thi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty requestScope.candidateQueue}">
                                <c:forEach var="enr" items="${requestScope.candidateQueue}">
                                    <tr>
                                        <td style="font-family: monospace; font-weight: 700; color: #0f172a;">
                                            <c:out value="${enr.candidate.candidateNumber}" default="—" />
                                        </td>
                                        <td style="font-weight: 600; color: #0f172a;">
                                            <c:out value="${enr.candidate.fullName}" default="—" />
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="action-badge action-badge--info" style="font-size: 0.75rem;">
                                                <c:out value="${enr.sectionStatus}" default="—" />
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${enr.candidate.absent}">
                                                    <span class="action-badge action-badge--fail" style="font-size: 0.75rem;">Vắng</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--pass" style="font-size: 0.75rem;">Có mặt</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <a href="${ctx}/staff/examstaff/allocation?action=checkin&id=${enr.candidateId}&sessionId=${sessionScope.selectedSessionId}"
                                               class="btn-export" style="padding: 4px 10px; font-size: 0.78rem; border-radius: 6px; text-decoration: none; border-color: rgba(37,99,235,0.25); color: #2563eb; font-weight: 700;">
                                                Điểm danh
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center; padding: 2rem 1rem; color: #64748b;">
                                        Chưa có thí sinh nào trong ca sát hạch này.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
