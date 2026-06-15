<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    DAO.ExamSessionDAO sessionDAO = new DAO.Impl.ExamSessionDAOImpl();
    java.util.List<Models.ExamSession> allSessions = null;
    try {
        allSessions = sessionDAO.getAllSessions();
    } catch (Exception e) {
        e.printStackTrace();
        allSessions = new java.util.ArrayList<>();
    }
    pageContext.setAttribute("allSessions", allSessions);

    java.util.LinkedHashMap<Integer, Models.ExamSession> examOptionMap = new java.util.LinkedHashMap<>();
    for (Models.ExamSession s : allSessions) {
        if (s.getExamId() > 0 && !examOptionMap.containsKey(s.getExamId())) {
            examOptionMap.put(s.getExamId(), s);
        }
    }
    pageContext.setAttribute("examOptions", new java.util.ArrayList<>(examOptionMap.values()));

    String sessIdParam = request.getParameter("sessionId");
    int sessionId = 2;
    if (sessIdParam != null && !sessIdParam.isEmpty()) {
        try {
            sessionId = Integer.parseInt(sessIdParam);
        } catch (Exception e) {}
    } else if (session.getAttribute("selectedSessionId") != null) {
        sessionId = (Integer) session.getAttribute("selectedSessionId");
    }
    session.setAttribute("selectedSessionId", sessionId);

    Models.ExamSession currentSession = null;
    for (Models.ExamSession s : allSessions) {
        if (s.getId() == sessionId) {
            currentSession = s;
            break;
        }
    }
    pageContext.setAttribute("currentSession", currentSession);

    int examId = (currentSession != null && currentSession.getExamId() > 0) ? currentSession.getExamId() : sessionId;
    pageContext.setAttribute("selectedExamId", examId);

    java.util.List<Models.ExamSession> examSessions = new java.util.ArrayList<>();
    if (currentSession != null) {
        try {
            examSessions = sessionDAO.getSessionsByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    pageContext.setAttribute("examSessions", examSessions);

    DAO.ExamRegistrationDAO regDAO = new DAO.Impl.ExamRegistrationDAOImpl();
    java.util.List<Models.ExamRegistration> qList;
    try {
        qList = regDAO.getCandidatesByExamId(examId);
    } catch (Exception e) {
        e.printStackTrace();
        qList = new java.util.ArrayList<>();
    }
    Controllers.Staff.ExamStaff.CandidatePhotoHelper.normalizeQueue(
        application.getRealPath("/"), qList, regDAO);
    session.setAttribute("candidateQueue", qList);
    session.setAttribute("lastLoadedExamId", examId);

    java.util.List<Controllers.Staff.ExamStaff.ExaminerSlot> assignedExaminers =
            Controllers.Staff.ExamStaff.ExaminerAssignmentStore.getByExamId(session, examId);
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
    <title>Tổng Quan Ngày Thi - Ban Sát Hạch</title>
    
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
            <span class="breadcrumbs__current" aria-current="page">Tổng quan ngày thi</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tổng quan ngày thi sát hạch</h1>
                <p class="page-subtitle">Một kỳ thi gồm các ca lý thuyết → sa hình → đường trường. Giám sát toàn bộ thí sinh và tiến độ thủ tục trên cùng một màn hình.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
                <!-- Modern Glassmorphic Shift Selector -->
                <form action="dashboard.jsp" method="GET" style="margin: 0; display: inline-flex; align-items: center;">
                    <div style="background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(226, 232, 240, 0.8); border-radius: 8px; padding: 4px 10px; display: flex; align-items: center; gap: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.02); height: 42px;">
                        <span style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em; white-space: nowrap;">Kỳ thi:</span>
                        <select id="sessionId" name="sessionId" class="es-session-selector__select">
                            <c:forEach var="exam" items="${examOptions}">
                                <option value="${exam.id}" ${selectedExamId eq exam.examId ? 'selected' : ''}>
                                    Hạng ${exam.licenseCode} — <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
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

        <!-- Điều khiển các ca trong ngày thi -->
        <c:if test="${not empty currentSession}">
        <section class="report-pane" style="margin-top: 1rem; border-radius: 16px; padding: 1.25rem 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1rem; font-weight: 800; color: #0f172a; margin: 0 0 6px 0;">Các ca trong ngày thi</h2>
                    <p style="font-size: 0.82rem; color: #64748b; margin: 0 0 10px 0;">
                        Kỳ thi hạng <strong>${currentSession.licenseCode}</strong> —
                        <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/>.
                        <strong>${assignedExaminerCount}</strong> phân công giám khảo
                        (<a href="examiner-allocation?sessionId=${sessionScope.selectedSessionId}" style="color: #0052cc; font-weight: 700;">Phân bổ giám khảo</a>).
                    </p>
                    <c:forEach var="ds" items="${examSessions}">
                        <div style="display: inline-flex; align-items: center; gap: 8px; flex-wrap: wrap; margin: 4px 8px 4px 0; padding: 6px 10px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 0.78rem;">
                            <span>
                                <strong>${ds.sessionName}</strong>
                                (<fmt:formatDate value="${ds.shiftStartTime}" pattern="HH:mm"/>–<fmt:formatDate value="${ds.shiftEndTime}" pattern="HH:mm"/>)
                                — <c:choose>
                                    <c:when test="${fn:contains(ds.examTypeName, 'Lý thuyết') or ds.examTypeName eq 'Theory'}">Lý thuyết</c:when>
                                    <c:when test="${fn:contains(ds.examTypeName, 'Sa hình') or fn:contains(ds.examTypeName, 'Thực hành') or ds.examTypeName eq 'Practical'}">Sa hình</c:when>
                                    <c:when test="${fn:contains(ds.examTypeName, 'Đường') or ds.examTypeName eq 'OnRoad'}">Đường trường</c:when>
                                    <c:otherwise>${ds.examTypeName}</c:otherwise>
                                </c:choose>
                                — <em>${ds.status}</em>
                            </span>
                            <c:if test="${ds.status ne 'InProgress' and ds.status ne 'Completed' and ds.status ne 'Cancelled'}">
                                <form action="session-control" method="POST" style="margin: 0; display: inline;" onsubmit="return confirm('Bắt đầu ca ${ds.sessionName}?');">
                                    <input type="hidden" name="action" value="startSession">
                                    <input type="hidden" name="sessionId" value="${ds.id}">
                                    <input type="hidden" name="redirect" value="dashboard">
                                    <button type="submit" class="btn-filter" style="height: 26px; padding: 0 0.6rem; border-radius: 6px; font-size: 0.7rem; font-weight: 700;">Bắt đầu</button>
                                </form>
                            </c:if>
                            <c:if test="${ds.status eq 'InProgress'}">
                                <form action="session-control" method="POST" style="margin: 0; display: inline;" onsubmit="return confirm('Kết thúc ca ${ds.sessionName}?');">
                                    <input type="hidden" name="action" value="endSession">
                                    <input type="hidden" name="sessionId" value="${ds.id}">
                                    <input type="hidden" name="redirect" value="dashboard">
                                    <button type="submit" class="btn-export" style="height: 26px; padding: 0 0.6rem; border-radius: 6px; font-size: 0.7rem; font-weight: 700; color: #b91c1c; border-color: #fecaca;">Kết thúc</button>
                                </form>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </section>
        </c:if>

        <!-- Thống kê: thủ tục (ảnh+lệ phí) vs kết quả thi cuối cùng -->
        <c:set var="totalCandidatesCount" value="${fn:length(sessionScope.candidateQueue)}" />
        <c:set var="procedureDoneCount" value="0" />
        <c:set var="processingCount" value="0" />
        <c:set var="waitingCount" value="0" />
        <c:set var="examFinishedCount" value="0" />
        <c:set var="examPassedCount" value="0" />

        <c:forEach var="c" items="${sessionScope.candidateQueue}">
            <c:set var="isExamFinished" value="${false}" />
            <c:if test="${(c.notes eq 'Absent') or 
                          (c.notes ne 'Absent' and c.paymentCompleted and (
                              (c.theoryPassed eq 'failed') or 
                              (c.practicalPassed eq 'failed') or 
                              (c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and not c.requiresRoadTest) or 
                              (c.requiresRoadTest and c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and (c.roadTestPassed eq 'passed' or c.roadTestPassed eq 'failed'))
                          ))}">
                <c:set var="isExamFinished" value="${true}" />
                <c:set var="examFinishedCount" value="${examFinishedCount + 1}" />
                <c:if test="${c.notes ne 'Absent' and ((c.practicalPassed eq 'passed' and not c.requiresRoadTest) or (c.requiresRoadTest and c.roadTestPassed eq 'passed'))}">
                    <c:set var="examPassedCount" value="${examPassedCount + 1}" />
                </c:if>
            </c:if>

            <%-- Xong thủ tục: đã chụp ảnh + thu lệ phí (import CSV không cần ảnh) --%>
            <c:set var="procedureComplete" value="${c.procedureComplete}" />
            <c:if test="${procedureComplete}">
                <c:set var="procedureDoneCount" value="${procedureDoneCount + 1}" />
            </c:if>
            <c:if test="${not procedureComplete and sessionScope.callingSbd eq c.sbd}">
                <c:set var="processingCount" value="${processingCount + 1}" />
            </c:if>
            <c:if test="${not procedureComplete and sessionScope.callingSbd ne c.sbd}">
                <c:set var="waitingCount" value="${waitingCount + 1}" />
            </c:if>
        </c:forEach>

        <c:set var="completedCount" value="${procedureDoneCount}" />
        <c:set var="pendingCount" value="${waitingCount}" />
        
        <c:set var="completedPercent" value="${totalCandidatesCount gt 0 ? (completedCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="processingPercent" value="${totalCandidatesCount gt 0 ? (processingCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="pendingPercent" value="${totalCandidatesCount gt 0 ? (pendingCount * 100.0) / totalCandidatesCount : 0.0}" />

        <!-- KPI Metrics Row -->
        <section class="metrics-row" aria-label="Chỉ số ngày thi">
            <!-- Card 1: Exam day overview -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin-bottom: 0.15rem;">
                        Kỳ thi hạng ${not empty currentSession ? currentSession.licenseCode : '—'}
                    </span>
                    <span class="stat-label">
                        <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/>
                        — ${fn:length(examSessions)} ca thi
                    </span>
                    <span class="stat-trend stat-trend--up">Lý thuyết → Sa hình → Đường trường</span>
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
                    <span class="stat-label">Tổng thí sinh kỳ thi</span>
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
            
            <!-- Card 4: Waiting / procedure -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #d97706;">${waitingCount}</span>
                    <span class="stat-label">Chờ / chưa xong thủ tục</span>
                    <span class="stat-trend stat-trend--up">${processingCount} đang tại quầy thủ tục</span>
                </div>
            </div>

            <!-- Card 5: Final exam results -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green" style="background-color: rgba(16, 185, 129, 0.08);">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${examPassedCount}<span style="font-size: 0.85rem; color: #64748b;"> / ${examFinishedCount}</span></span>
                    <span class="stat-label">Kết quả thi (Đạt / đã chấm xong)</span>
                    <span class="stat-trend stat-trend--up">${examFinishedCount - examPassedCount} trượt hoặc vắng</span>
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
                    Tiến độ thủ tục &amp; kết quả thi
                </h2>
                <p style="font-size: 0.78rem; color: #64748b; margin: 4px 0 0 0;">
                    Thanh xanh = đã thu phí + ảnh. Vàng = chờ thủ tục. Xanh dương = đang tại quầy.
                    Kết quả thi chỉ tính khi xong <strong>toàn bộ</strong> phần thi (hạng B: lý thuyết + sa hình + đường trường).
                </p>
            </div>
            
            <div class="progress-indicator-bar">
                <div class="progress-indicator-segment progress-indicator-segment--success" style="width: ${completedPercent}%" title="Xong thủ tục: ${procedureDoneCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--info" style="width: ${processingPercent}%" title="Đang thủ tục: ${processingCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--pending" style="width: ${pendingPercent}%" title="Chờ: ${waitingCount}"></div>
            </div>
            
            <div class="progress-legend">
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #10b981;"></span>
                    <span>Xong thủ tục (ảnh + lệ phí): <strong>${procedureDoneCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #3b82f6;"></span>
                    <span>Đang tại quầy thủ tục: <strong>${processingCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #f59e0b;"></span>
                    <span>Chờ / chưa xong thủ tục: <strong>${waitingCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #7c3aed;"></span>
                    <span>Đã chấm xong kỳ thi: <strong>${examFinishedCount}</strong> (Đạt: ${examPassedCount}, Trượt/vắng: ${examFinishedCount - examPassedCount})</span>
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
                    <span class="room-badge room-badge--orange">Chờ gọi (${waitingCount})</span>
                </div>
                
                <div class="room-candidate-list">
                    <c:set var="waitRenderCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:set var="waitFinished" value="${false}" />
                        <c:if test="${(c.notes eq 'Absent') or 
                                      (c.notes ne 'Absent' and c.paymentCompleted and (
                                          (c.theoryPassed eq 'failed') or (c.practicalPassed eq 'failed') or 
                                          (c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and not c.requiresRoadTest) or 
                                          (c.requiresRoadTest and c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and (c.roadTestPassed eq 'passed' or c.roadTestPassed eq 'failed'))
                                      ))}">
                            <c:set var="waitFinished" value="${true}" />
                        </c:if>
                        <c:set var="waitProcedureDone" value="${c.procedureComplete}" />
                        <c:if test="${c.isPresent and sessionScope.callingSbd ne c.sbd and not waitFinished and not waitProcedureDone and waitRenderCount lt 6}">
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
                        <c:if test="${sessionScope.callingSbd eq c.sbd and not c.procedureComplete and activeCalledCount lt 3}">
                            <c:set var="activeCalledCount" value="${activeCalledCount + 1}" />
                            <div class="room-candidate-item" style="border-left: 3px solid #2563eb;">
                                <div class="candidate-meta">
                                    <span style="font-weight: 700; color: #1e293b;">SBD: ${c.sbd}</span>
                                    <span class="candidate-step candidate-step--payment">Đang ở quầy thủ tục</span>
                                </div>
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                                    <span class="candidate-name">${c.name}</span>
                                    <span style="font-size: 0.72rem; color: #64748b;">Hạng ${c.clazz}</span>
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
            
            <!-- Column 3: Kết quả thi cuối cùng -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2"/>
                            <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"/>
                        </svg>
                        Kết quả thi cuối cùng
                    </h3>
                    <span class="room-badge room-badge--green">${examFinishedCount} thí sinh</span>
                </div>
                
                <div style="max-height: 280px; overflow-y: auto;">
                    <table style="width: 100%; font-size: 0.78rem; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0;">
                                <th style="text-align: left; padding: 8px 6px; color: #475569;">SBD</th>
                                <th style="text-align: left; padding: 8px 6px; color: #475569;">Họ tên</th>
                                <th style="text-align: center; padding: 8px 6px; color: #475569;">Kết quả</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="resultRenderCount" value="0" />
                            <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                <c:if test="${(c.notes eq 'Absent') or 
                                              (c.notes ne 'Absent' and c.paymentCompleted and (
                                                  (c.theoryPassed eq 'failed') or 
                                                  (c.practicalPassed eq 'failed') or 
                                                  (c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and not c.requiresRoadTest) or 
                                                  (c.requiresRoadTest and c.theoryPassed eq 'passed' and c.practicalPassed eq 'passed' and (c.roadTestPassed eq 'passed' or c.roadTestPassed eq 'failed'))
                                              ))}">
                                    <c:set var="resultRenderCount" value="${resultRenderCount + 1}" />
                                    <c:set var="finalPass" value="${c.notes ne 'Absent' and ((c.practicalPassed eq 'passed' and not c.requiresRoadTest) or (c.requiresRoadTest and c.roadTestPassed eq 'passed'))}" />
                                    <tr style="border-bottom: 1px solid #f1f5f9;">
                                        <td style="padding: 8px 6px; font-weight: 800; color: #0052cc; font-family: monospace;">${c.sbd}</td>
                                        <td style="padding: 8px 6px; font-weight: 600; color: #0f172a;">${c.name}</td>
                                        <td style="padding: 8px 6px; text-align: center;">
                                            <c:choose>
                                                <c:when test="${c.notes eq 'Absent'}">
                                                    <span style="font-weight: 800; color: #ef4444; background: #fef2f2; padding: 2px 8px; border-radius: 4px;">VẮNG</span>
                                                </c:when>
                                                <c:when test="${finalPass}">
                                                    <span style="font-weight: 800; color: #047857; background: #ecfdf5; padding: 2px 8px; border-radius: 4px;">ĐẠT</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-weight: 800; color: #991b1b; background: #fee2e2; padding: 2px 8px; border-radius: 4px;">TRƯỢT</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${resultRenderCount eq 0}">
                                <tr>
                                    <td colspan="3" style="padding: 1.5rem; text-align: center; color: #94a3b8; font-style: italic;">
                                        Chưa có thí sinh nào hoàn thành toàn bộ kỳ thi.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
                
                <a href="report.jsp" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s; margin-top: 8px; display: block;" class="hover-elevate">
                    Xem báo cáo chi tiết &rarr;
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
