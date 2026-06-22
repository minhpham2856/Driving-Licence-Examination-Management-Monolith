<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    DAOs.ExamSessionDAO sessionDAO = new DAOs.Impl.ExamSessionDAOImpl();
    java.util.List<Models.ExamSession> allSessions = null;
    try {
        allSessions = sessionDAOs.getAllSessions();
    } catch (Exception e) {
        e.printStackTrace();
        allSessions = new java.util.ArrayList<>();
    }
    pageContext.setAttribute("allSessions", allSessions);

    // Retrieve or load selected sessionId
    String sessIdParam = request.getParameter("sessionId");
    int sessionId = 2; // Default session
    if (sessIdParam != null && !sessIdParam.isEmpty()) {
        try {
            sessionId = Integer.parseInt(sessIdParam);
        } catch (Exception e) {}
    } else if (session.getAttribute("selectedSessionId") != null) {
        sessionId = (Integer) session.getAttribute("selectedSessionId");
    }
    session.setAttribute("selectedSessionId", sessionId);

    // Retrieve current session details for display
    Models.ExamSession currentSession = null;
    for (Models.ExamSession s : allSessions) {
        if (s.getSessionId() == sessionId) {
            currentSession = s;
            break;
        }
    }
    pageContext.setAttribute("currentSession", currentSession);

    // Load queue for this session if session changed or first time
    java.util.List<Models.ExamRegistration> qList = (java.util.List<Models.ExamRegistration>) session.getAttribute("candidateQueue");
    Integer lastLoadedSessId = (Integer) session.getAttribute("lastLoadedSessionId");
    if (qList == null || lastLoadedSessId == null || lastLoadedSessId != sessionId) {
        DAOs.ExamRegistrationDAO regDAO = new DAOs.Impl.ExamRegistrationDAOImpl();
        try {
            qList = regDAOs.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new java.util.ArrayList<>();
        }
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", sessionId);
    }
    if (qList != null) {
        Controllers.Staff.ExamStaff.CandidatePhotoHelper.normalizeQueue(
            application.getRealPath("/"), qList, new DAOs.Impl.ExamRegistrationDAOImpl());
    }

    java.util.List<Controllers.Staff.ExamStaff.ExaminerSlot> assignedExaminers =
            Controllers.Staff.ExamStaff.ExaminerAssignmentStore.getBySessionId(session, sessionId);
    int assignedWithArea = 0;
    if (assignedExaminers != null) {
        for (Controllers.Staff.ExamStaff.ExaminerSlot slot : assignedExaminers) {
            if (slot.getAreaId() > 0) assignedWithArea++;
        }
    }
    pageContext.setAttribute("assignedExaminerCount", assignedWithArea);

    String sessionControlMsg = (String) session.getAttribute("sessionControlMsg");
    String sessionControlError = (String) session.getAttribute("sessionControlError");
    if (sessionControlMsg != null) {
        request.setAttribute("sessionControlMsg", sessionControlMsg);
        session.removeAttribute("sessionControlMsg");
    }
    if (sessionControlError != null) {
        request.setAttribute("sessionControlError", sessionControlError);
        session.removeAttribute("sessionControlError");
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng Quan Ca Thi - Ban Sát Hạch</title>
    
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
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">

    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tổng quan ca thi</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tổng quan ca thi sát hạch</h1>
                <p class="page-subtitle">Giám sát trực quan tiến độ đón tiếp, làm thủ tục hồ sơ và trạng thái thi của thí sinh trong ngày từ cơ sở dữ liệu thực.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
                <!-- Modern Glassmorphic Shift Selector -->
                <form action="dashboard.jsp" method="GET" style="margin: 0; display: inline-flex; align-items: center;">
                    <div style="background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(226, 232, 240, 0.8); border-radius: 8px; padding: 4px 10px; display: flex; align-items: center; gap: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.02); height: 42px;">
                        <span style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em; white-space: nowrap;">Ca sát hạch:</span>
                        <select id="sessionId" name="sessionId" class="es-session-selector__select">
                            <c:forEach var="sess" items="${allSessions}">
                                <option value="${sess.id}" ${sessionScope.selectedSessionId eq sess.id ? 'selected' : ''}>
                                    Ca #${sess.id} - ${sess.sessionName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </form>
                
                <a href="allocation" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #475569; border-color: #e2e8f0; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="3" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="14" y="3" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="14" y="12" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="3" y="16" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    Phân bổ khu vực
                </a>
                <a href="procedure" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; text-decoration: none; background-color: #0052cc; border-color: #0052cc; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="3" width="20" height="18" rx="2.5" stroke="currentColor" stroke-width="1.5"/>
                        <circle cx="7.5" cy="10" r="3" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M3.5 18c0-2 2-3.5 4-3.5s4 1.5 4 3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="8" x2="19" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="16" x2="17" y2="16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Mở Bàn thủ tục (3 Bước)
                </a>
            </div>
        </header>

        <c:if test="${not empty requestScope.sessionControlMsg}">
            <div style="background-color: #ecfdf5; border: 1.5px solid #10b981; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <span style="font-size: 0.85rem; font-weight: 700; color: #047857;">${requestScope.sessionControlMsg}</span>
            </div>
        </c:if>
        <c:if test="${not empty requestScope.sessionControlError}">
            <div style="background-color: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <span style="font-size: 0.85rem; font-weight: 700; color: #b91c1c;">${requestScope.sessionControlError}</span>
            </div>
        </c:if>

        <!-- Điều khiển bắt đầu / kết thúc ca thi -->
        <section class="report-pane" style="margin-top: 1rem; border-radius: 16px; padding: 1.25rem 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1rem; font-weight: 800; color: #0f172a; margin: 0 0 6px 0;">Điều khiển ca thi</h2>
                    <p style="font-size: 0.82rem; color: #64748b; margin: 0;">
                        sát hạch viên chỉ đăng nhập được sau khi ca ở trạng thái <strong>Đang diễn ra</strong>
                        và đã được phân vào khu vực thi
                        (<a href="examiner-allocation?sessionId=${sessionScope.selectedSessionId}" style="color: #0052cc; font-weight: 700;">Phân bổ sát hạch viên</a>).
                        Hiện có <strong>${assignedExaminerCount}</strong> sát hạch viên đã phân phòng.
                    </p>
                </div>
                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <c:choose>
                        <c:when test="${currentSession.status eq 'InProgress'}">
                            <span class="role-badge role-badge--admin" style="background: #dcfce7; color: #166534; border: 1px solid #86efac;">Đang diễn ra</span>
                            <form action="session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Kết thúc ca thi? sát hạch viên sẽ không đăng nhập được nữa.');">
                                <input type="hidden" name="action" value="endSession">
                                <input type="hidden" name="sessionId" value="${sessionScope.selectedSessionId}">
                                <input type="hidden" name="redirect" value="dashboard">
                                <button type="submit" class="btn-export" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; background: #fef2f2; color: #b91c1c; border-color: #fecaca; font-weight: 700;">
                                    Kết thúc ca thi
                                </button>
                            </form>
                        </c:when>
                        <c:when test="${currentSession.status eq 'Completed' or currentSession.status eq 'Cancelled'}">
                            <span class="role-badge" style="background: #f1f5f9; color: #64748b;">Đã kết thúc</span>
                        </c:when>
                        <c:otherwise>
                            <span class="role-badge role-badge--coi" style="background: #fffbeb; color: #b45309; border: 1px solid #fde68a;">Chưa bắt đầu</span>
                            <c:choose>
                                <c:when test="${assignedExaminerCount gt 0}">
                                    <form action="session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Bắt đầu ca thi? sát hạch viên đã phân công có thể đăng nhập.');">
                                        <input type="hidden" name="action" value="startSession">
                                        <input type="hidden" name="sessionId" value="${sessionScope.selectedSessionId}">
                                        <input type="hidden" name="redirect" value="dashboard">
                                        <button type="submit" class="btn-filter" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; font-weight: 700;">
                                            Bắt đầu ca thi
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <a href="examiner-allocation?sessionId=${sessionScope.selectedSessionId}" class="btn-filter" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; opacity: 0.85;">
                                        Phân sát hạch viên trước
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <!-- Dynamic parameters from JSTL connected to DB session candidateQueue -->
        <c:set var="totalCandidatesCount" value="${fn:length(sessionScope.candidateQueue)}" />
        <c:set var="completedCount" value="0" />
        <c:set var="processingCount" value="0" />
        <c:set var="pendingCount" value="0" />

        <c:forEach var="c" items="${sessionScope.candidateQueue}">
            <c:choose>
                <c:when test="${c.validCapturedPhoto and c.paymentCompleted}">
                    <c:set var="completedCount" value="${completedCount + 1}" />
                </c:when>
                <c:when test="${sessionScope.callingSbd eq c.sbd}">
                    <c:set var="processingCount" value="${processingCount + 1}" />
                </c:when>
                <c:otherwise>
                    <c:set var="pendingCount" value="${pendingCount + 1}" />
                </c:otherwise>
            </c:choose>
        </c:forEach>
        
        <c:set var="completedPercent" value="${totalCandidatesCount gt 0 ? (completedCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="processingPercent" value="${totalCandidatesCount gt 0 ? (processingCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="pendingPercent" value="${totalCandidatesCount gt 0 ? (pendingCount * 100.0) / totalCandidatesCount : 0.0}" />

        <!-- KPI Metrics Row -->
        <section class="metrics-row" aria-label="Chỉ số ca thi">
            <!-- Card 1: Active Exam Session -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin-bottom: 0.15rem;">
                        <c:choose>
                            <c:when test="${not empty currentSession}">
                                ${currentSession.sessionName}
                            </c:when>
                            <c:otherwise>
                                Ca Sát Hạch #${sessionScope.selectedSessionId}
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="stat-label">Hạng ${not empty currentSession ? currentSession.licenseCode : 'Đang tải'} | ${not empty currentSession ? currentSession.examDate : 'N/A'}</span>
                    <span class="stat-trend stat-trend--up">Trạng thái: ${not empty currentSession ? currentSession.status : 'Active'}</span>
                </div>
            </div>
            
            <!-- Card 2: Total Candidates in Session -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(59, 130, 246, 0.06); color: #2563eb;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${totalCandidatesCount}</span>
                    <span class="stat-label">Tổng thí sinh ca thi</span>
                    <span class="stat-trend stat-trend--up">Hàng đợi động</span>
                </div>
            </div>
            
            <!-- Card 3: Completed Procedures -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${completedCount}</span>
                    <span class="stat-label">Đã xong thủ tục</span>
                    <span class="stat-trend stat-trend--up"><fmt:formatNumber value="${completedPercent}" maxFractionDigits="1"/>% hoàn thành</span>
                </div>
            </div>
            
            <!-- Card 4: Undergoing Procedure -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #d97706;">${processingCount}</span>
                    <span class="stat-label">Đang làm thủ tục</span>
                    <span class="stat-trend stat-trend--up">Tại quầy / Bàn chờ</span>
                </div>
            </div>
        </section>

        <!-- Procedure Progress Visualization Section -->
        <div class="report-pane" style="margin-top: 1.5rem;">
            <div class="grading-pane__header" style="border-bottom: none; padding-bottom: 0; margin-bottom: 0.5rem;">
                <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v8M13 20V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tiến độ hoàn thiện thủ tục trong ngày
                </h2>
            </div>
            
            <div class="progress-indicator-bar">
                <div class="progress-indicator-segment progress-indicator-segment--success" style="width: ${completedPercent}%" title="Đã xong thủ tục: ${completedCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--info" style="width: ${processingPercent}%" title="Đang làm thủ tục: ${processingCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--pending" style="width: ${pendingPercent}%" title="Chưa đến / Đang chờ: ${pendingCount}"></div>
            </div>
            
            <div class="progress-legend">
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #10b981;"></span>
                    <span>Đã hoàn thành: <strong>${completedCount}</strong> học viên (<fmt:formatNumber value="${completedPercent}" maxFractionDigits="1"/>%)</span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #3b82f6;"></span>
                    <span>Đang làm hồ sơ / Đối chiếu: <strong>${processingCount}</strong> học viên (<fmt:formatNumber value="${processingPercent}" maxFractionDigits="1"/>%)</span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #f59e0b;"></span>
                    <span>Chưa đến / Chờ gọi: <strong>${pendingCount}</strong> học viên (<fmt:formatNumber value="${pendingPercent}" maxFractionDigits="1"/>%)</span>
                </div>
            </div>
        </div>

        <!-- Room Monitoring Dashboard (Replaces Machine Grid) -->
        <div class="room-monitor-grid">
            
            <!-- Column 1: Waiting Room (Phòng Chờ) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Phòng chờ chính
                    </h3>
                    <span class="room-badge room-badge--orange">Chờ gọi</span>
                </div>
                
                <div class="room-candidate-list">
                    <c:set var="waitRenderCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.isPresent and sessionScope.callingSbd ne c.sbd and c.theoryPassed eq 'none' and waitRenderCount lt 4}">
                            <c:set var="waitRenderCount" value="${waitRenderCount + 1}" />
                            <div class="room-candidate-item">
                                <div class="candidate-meta">
                                    <span class="candidate-sbd">SBD: ${c.sbd}</span>
                                    <span class="candidate-step candidate-step--waiting">Hạng ${c.clazz}</span>
                                </div>
                                <div class="candidate-name">${c.name}</div>
                                <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                                    <span>Trạng thái: Đang chờ</span>
                                    <span style="font-weight: 600; color: #475569;">SĐT: ${c.phoneNo}</span>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${waitRenderCount eq 0}">
                        <div class="empty-room-state">
                            Không có thí sinh nào đang chờ ở phòng chờ.
                        </div>
                    </c:if>
                </div>
                
                <a href="candidatecall" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Xem phòng điều hành gọi thi &rarr;
                </a>
            </div>
            
            <!-- Column 2: Procedure Room (Phòng Làm Thủ Tục) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #2563eb;">
                            <rect x="3" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 20h.01M16 20h.01M8 20h.01M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Bàn thủ tục khép kín
                    </h3>
                    <span class="room-badge room-badge--blue">Đang xử lý</span>
                </div>
                
                <div class="room-candidate-list">
                    <c:set var="activeCalledCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${sessionScope.callingSbd eq c.sbd and (not c.validCapturedPhoto or not c.paymentCompleted) and activeCalledCount lt 3}">
                            <c:set var="activeCalledCount" value="${activeCalledCount + 1}" />
                            <c:set var="isPhotoDone" value="${c.validCapturedPhoto}" />
                            <c:set var="isPayDone" value="${c.paymentCompleted}" />
                            <c:set var="stepNum" value="${not isPhotoDone ? '2' : '3'}" />
                            <c:set var="stepName" value="${not isPhotoDone ? 'Chụp ảnh' : 'Lệ phí'}" />
                            
                            <div class="room-candidate-item" style="border-left: 3px solid ${not isPhotoDone ? '#7e22ce' : '#b45309'};">
                                <div class="candidate-meta">
                                    <span style="font-weight: 700; color: #1e293b;">SBD: ${c.sbd}</span>
                                    <span class="candidate-step ${not isPhotoDone ? 'candidate-step--photo' : 'candidate-step--payment'}">Bước ${stepNum}: ${stepName}</span>
                                </div>
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                                    <span class="candidate-name">${c.name}</span>
                                    <span style="font-size: 0.72rem; color: #64748b;">Hạng ${c.clazz}</span>
                                </div>
                                <div style="font-size: 0.72rem; color: #64748b; margin-top: 2px;">
                                    <c:choose>
                                        <c:when test="${not isPhotoDone}">Đang tiến hành chụp ảnh live chân dung FaceID.</c:when>
                                        <c:otherwise>Đang đối chiếu hồ sơ và đóng lệ phí thi.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${activeCalledCount eq 0}">
                        <div class="empty-room-state">
                            Bàn làm thủ tục đang trống.
                        </div>
                    </c:if>
                </div>
                
                <a href="procedure" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Vào quầy làm thủ tục &rarr;
                </a>
            </div>
            
            <!-- Column 3: Test Field (Sân Sát Hạch) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Sân sát hạch thực hành
                    </h3>
                    <span class="room-badge room-badge--green">Đang thi</span>
                </div>
                
                <div class="room-candidate-list">
                    <c:set var="fieldRenderCount" value="0" />
                    <!-- Show candidates currently thi thực hành -->
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.theoryPassed eq 'passed' and c.practicalPassed eq 'none' and fieldRenderCount lt 4}">
                            <c:set var="fieldRenderCount" value="${fieldRenderCount + 1}" />
                            <div class="room-candidate-item" style="border-left: 3px solid #10b981;">
                                <div class="candidate-meta">
                                    <span class="candidate-sbd" style="color: #10b981;">${c.sbd}</span>
                                    <span class="candidate-step candidate-step--ready">Đang thi</span>
                                </div>
                                <div class="candidate-name">${c.name}</div>
                                <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                                    <span>SBD: ${c.sbd} (${c.clazz})</span>
                                    <span style="font-weight: 800; color: #10b981;">Đang thực hiện...</span>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- If room monitor is not full, show completed candidates -->
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.isPaymentCompleted and (c.practicalPassed eq 'passed' or c.practicalPassed eq 'failed') and fieldRenderCount lt 4}">
                            <c:set var="fieldRenderCount" value="${fieldRenderCount + 1}" />
                            <c:set var="isPass" value="${c.practicalPassed eq 'passed'}" />
                            <div class="room-candidate-item" style="border-left: 3px solid ${isPass ? '#10b981' : '#ef4444'};">
                                <div class="candidate-meta">
                                    <span class="candidate-sbd" style="color: ${isPass ? '#10b981' : '#ef4444'};">${c.sbd} (${c.clazz})</span>
                                    <span class="candidate-step ${isPass ? 'candidate-step--ready' : 'candidate-step--waiting'}" style="background-color: ${isPass ? '#ecfdf5' : '#fef2f2'}; color: ${isPass ? '#047857' : '#991b1b'};">${isPass ? 'Đạt' : 'Trượt'}</span>
                                </div>
                                <div class="candidate-name">${c.name}</div>
                                <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                                    <span>Thi thực hành: ${c.practicalScore}</span>
                                    <span style="font-weight: 800; color: ${isPass ? '#10b981' : '#ef4444'};">${isPass ? 'HOÀN THÀNH' : 'HỎNG'}</span>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${fieldRenderCount eq 0}">
                        <div class="empty-room-state">
                            Chưa có thí sinh nào ra sân thi thực hành.
                        </div>
                    </c:if>
                </div>
                
                <a href="report.jsp" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Xem báo cáo kết quả thi sát hạch &rarr;
                </a>
            </div>
            
        </div>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
</body>
</html>
