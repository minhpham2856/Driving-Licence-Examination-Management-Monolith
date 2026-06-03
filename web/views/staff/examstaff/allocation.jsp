<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%
    // Retrieve the candidate queue from the session
    java.util.List<Models.ExamRegistration> qList = (java.util.List<Models.ExamRegistration>) session.getAttribute("candidateQueue");
    if (qList == null) {
        DAO.ExamRegistrationDAO regDAO = new DAO.Impl.ExamRegistrationDAOImpl();
        try {
            qList = regDAO.getCandidatesBySession(2); // Load ca thi B2 sáng (ID = 2) mặc định
        } catch (Exception e) {
            e.printStackTrace();
            qList = new java.util.ArrayList<>();
        }
        session.setAttribute("candidateQueue", qList);
    }

    // Fallback self-healing checks to load active rooms, computers, and devices dynamically if accessed directly
    if (request.getAttribute("activeTheoryRooms") == null) {
        DAO.ExamAreaDAO areaDAO = new DAO.Impl.ExamAreaDAOImpl();
        try {
            request.setAttribute("activeTheoryRooms", areaDAO.getActiveTheoryRooms());
        } catch (Exception e) { e.printStackTrace(); }
    }
    if (request.getAttribute("availableComputers") == null) {
        DAO.ExamComputerDAO compDAO = new DAO.Impl.ExamComputerDAOImpl();
        try {
            request.setAttribute("availableComputers", compDAO.getAvailableComputers());
        } catch (Exception e) { e.printStackTrace(); }
    }
    if (request.getAttribute("availableDevices") == null) {
        DAO.ExamDeviceDAO deviceDAO = new DAO.Impl.ExamDeviceDAOImpl();
        try {
            request.setAttribute("availableDevices", deviceDAO.getAvailableDevices(null));
            request.setAttribute("motorbikeDevices", deviceDAO.getAvailableDevicesByCategory("motorbike"));
            request.setAttribute("carDevices",       deviceDAO.getAvailableDevicesByCategory("car"));
        } catch (Exception e) { e.printStackTrace(); }
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quy Trình Phân Bổ Thí Sinh - Ban Sát Hạch</title>
    
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
    <jsp:param name="activeSidebar" value="phan-bo" />
</jsp:include>

<!-- Hidden checkbox hack for expanding/collapsing all rows simultaneously -->
<input type="checkbox" id="expand-all-toggle" class="master-toggle-checkbox" style="display: none !important;">

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Quy trình phân bổ</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Trình tự điều phối thí sinh</h1>
                <p class="page-subtitle">Quản lý và di chuyển thí sinh theo trình tự nghiệp vụ ngang full-width rộng rãi, hỗ trợ Checked Hack thu gọn/mở rộng.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <!-- Real-time Candidate Search Box -->
                <div style="position: relative; display: inline-block;">
                    <input type="text" id="candidateSearch" placeholder="Tìm thí sinh (SBD, Họ tên, CCCD...)" oninput="filterCandidates()" style="width: 260px; height: 38px; padding: 0 12px 0 34px; font-size: 0.82rem; font-weight: 600; border-radius: 8px; border: 1px solid #cbd5e1; outline: none; transition: all 0.2s;">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #64748b; pointer-events: none;">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                </div>

                <!-- Pure CSS Master Expand/Collapse Label -->
                <label for="expand-all-toggle" id="btnExtendAll" class="btn-export" style="height: 38px; padding: 0 1rem; font-size: 0.82rem; border-radius: 8px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; border-color: #cbd5e1; background-color: #ffffff; color: #475569; user-select: none;">
                    <svg class="extend-all-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                        <path d="M6 9l6 6 6-6"/>
                    </svg>
                    <span class="extend-text">Mở rộng tất cả</span>
                    <span class="collapse-text" style="display: none;">Thu gọn tất cả</span>
                </label>
                
            </div>
        </header>

        <!-- Exception / Warning Alert Notification Bars -->
        <c:if test="${not empty requestScope.errorMsg}">
            <div style="background-color: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated shake">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b91c1c;">
                    ${requestScope.errorMsg}
                </span>
            </div>
        </c:if>
        
        <c:if test="${not empty requestScope.warningMsg}">
            <div style="background-color: #fffbeb; border: 1.5px solid #f59e0b; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated pulse">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #f59e0b; flex-shrink: 0;">
                    <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b45309;">
                    ${requestScope.warningMsg}
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.alertMsg}">
            <div style="background-color: #eff6ff; border: 1.5px solid #3b82f6; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #3b82f6; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #1e3a8a;">
                    ${requestScope.alertMsg}
                </span>
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
                    Tổng số: ${fn:length(sessionScope.candidateQueue)} thí sinh
                </span>
            </div>
            
            <div style="display: flex; flex-wrap: wrap; gap: 1.5rem; justify-content: space-between; align-items: flex-end; margin-top: 1rem; border-top: 1.5px solid #f1f5f9; padding-top: 1rem;">
                <!-- Target Session Selection -->
                <form action="allocation" method="GET" style="display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 250px;">
                    <label for="sessionId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Chọn ca sát hạch mục tiêu:</label>
                    <div style="display: flex; gap: 8px;">
                        <select id="sessionId" name="sessionId" onchange="this.form.submit()" style="height: 38px; border-radius: 8px; border: 1.5px solid #cbd5e1; font-weight: 700; color: #1e293b; padding: 0 10px; background: #ffffff; flex-grow: 1; cursor: pointer; outline: none;">
                            <c:forEach var="sess" items="${requestScope.allSessions}">
                                <option value="${sess.id}" ${sessionScope.selectedSessionId eq sess.id ? 'selected' : ''}>
                                    Ca #${sess.id} - ${sess.sessionName} (${sess.examDate} | ${sess.status})
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
            </div>
        </div>

        <!-- Pipeline Grid Layout (Stretched horizontally) -->
        <div class="pipeline-container" style="display: flex; flex-direction: column; gap: 1.5rem; margin-top: 1.5rem;">
            
            <!-- STEP 1: WAITING LOBBY -->
            <div class="pipeline-column">
                <input type="checkbox" id="toggle-row-step-1" class="row-toggle-checkbox" style="display: none !important;">
                <div class="pipeline-header" style="width: 240px; border-right: 1px solid #e2e8f0; padding-right: 1.5rem; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0;">
                    <div>
                        <span class="pipeline-step-badge pipeline-step-badge--1">Bước 1</span>
                        <h3 class="pipeline-title" style="font-size: 1rem; font-weight: 800; color: #ea580c; margin: 0; display: flex; align-items: center; gap: 6px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="color: #ea580c;">
                                <path d="M11 5L6 9H2v6h4l5 4V5z"/>
                                <path d="M15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14"/>
                            </svg>
                            Phòng chờ chính
                        </h3>
                        <div class="area-meta-box" style="margin-top: 8px;">
                            <span><strong>Khu vực:</strong> Phòng Chờ Số 01</span>
                            <span><strong>Sức chứa:</strong> 100 người | <strong>Loại:</strong> Room</span>
                        </div>
                    </div>
                    <label for="toggle-row-step-1" class="btn-expand-row" style="margin-top: 1rem;">
                        <svg class="expand-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                            <path d="M6 9l6 6 6-6"/>
                        </svg>
                    </label>
                </div>
                
                <div class="pipeline-card-list" style="flex-grow: 1; display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 1rem; padding: 0.5rem 0; min-height: 120px; align-items: center;">
                    <c:set var="waitingCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.isPresent and not c.paymentCompleted}">
                            <c:set var="waitingCount" value="${waitingCount + 1}" />
                            <div class="candidate-pipe-card" style="width: 220px; flex-shrink: 0; border-left: 3px solid #ea580c;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #ea580c;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c.clazz}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details">
                                    Đang đợi ở phòng chờ chính...
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${waitingCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 2rem; width: 100%;">
                            Không có thí sinh nào trong phòng chờ chính.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- STEP 2: THEORY EXAM -->
            <div class="pipeline-column">
                <input type="checkbox" id="toggle-row-step-2" class="row-toggle-checkbox" style="display: none !important;">
                <div class="pipeline-header" style="width: 240px; border-right: 1px solid #e2e8f0; padding-right: 1.5rem; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0;">
                    <div>
                        <span class="pipeline-step-badge pipeline-step-badge--2">Bước 2</span>
                        <h3 class="pipeline-title" style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0; display: flex; align-items: center; gap: 6px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="color: #2563eb;">
                                <rect x="2" y="3" width="20" height="14" rx="2"/>
                                <path d="M8 21h8M12 17v4"/>
                            </svg>
                            Phòng thi lý thuyết
                        </h3>
                        <div class="area-meta-box" style="margin-top: 8px;">
                            <span><strong>Khu vực:</strong> Phòng Máy 201</span>
                            <span><strong>Sức chứa:</strong> 30 máy | <strong>Loại:</strong> Room</span>
                        </div>
                    </div>
                    <label for="toggle-row-step-2" class="btn-expand-row" style="margin-top: 1rem;">
                        <svg class="expand-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                            <path d="M6 9l6 6 6-6"/>
                        </svg>
                    </label>
                </div>
                
                <div class="pipeline-card-list" style="flex-grow: 1; display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 1rem; padding: 0.5rem 0; min-height: 120px; align-items: center;">
                    <c:set var="theoryCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.paymentCompleted and c.theoryPassed eq 'none'}">
                            <c:set var="theoryCount" value="${theoryCount + 1}" />
                            <c:set var="hasPhoto" value="${not empty c.photoUrl}" />
                            <c:set var="hasPaid" value="${c.paymentCompleted}" />
                            <c:set var="procedureDone" value="${hasPhoto and hasPaid}" />
                            
                            <div class="candidate-pipe-card" style="width: 220px; flex-shrink: 0; border-left: 3px solid #2563eb;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #2563eb;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c.clazz}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                
                                <div class="badge-grid-status" style="margin-bottom: 4px;">
                                    <span class="badge-pill-status ${hasPhoto ? 'badge-pill-status--success' : 'badge-pill-status--warning'}">CCCD & Ảnh</span>
                                    <span class="badge-pill-status ${hasPaid ? 'badge-pill-status--success' : 'badge-pill-status--warning'}">Lệ phí (200k)</span>
                                </div>
                                
                                <div class="candidate-pipe-details" style="color: #1e3a8a; font-weight: 700; margin-bottom: 4px; display: flex; flex-direction: column; gap: 4px;">
                                    <div style="display: flex; align-items: center; gap: 4px;">
                                        <span style="font-size: 0.72rem; font-weight: 600; color: #64748b;">Phòng:</span>
                                        <span style="color: #0f172a; font-weight: 800; font-size: 0.75rem;">${empty c.allocatedAreaName ? 'Chưa gán' : c.allocatedAreaName}</span>
                                    </div>
                                    <div style="display: flex; align-items: center; gap: 4px;">
                                        <span style="font-size: 0.72rem; font-weight: 600; color: #64748b;">Máy thi:</span>
                                        <span style="background-color: #0052cc; color: #ffffff; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 0.72rem; font-weight: 800;">${empty c.computerCode ? 'Chưa gán' : c.computerCode}</span>
                                    </div>
                                </div>
                                
                                <!-- Override Room and Computer -->
                                <div style="display: flex; flex-direction: column; gap: 2px; margin-top: 4px;">
                                    <form action="allocation" method="GET" style="display: flex; align-items: center; gap: 4px;">
                                        <input type="hidden" name="action" value="allocateRoom">
                                        <input type="hidden" name="id" value="${c.id}">
                                        <span style="font-size: 0.62rem; font-weight: 600; color: #64748b; width: 55px;">Đổi phòng:</span>
                                        <select name="areaId" onchange="this.form.submit()" style="height: 20px; font-size: 0.65rem; font-weight: 700; border-radius: 4px; border: 1px solid #cbd5e1; padding: 0; color: #475569; background: #ffffff; cursor: pointer; outline: none; flex-grow: 1;">
                                            <c:forEach var="room" items="${requestScope.activeTheoryRooms}">
                                                <option value="${room.id}" ${c.allocatedAreaId eq room.id ? 'selected' : ''}>${room.areaName}</option>
                                            </c:forEach>
                                        </select>
                                    </form>
                                    <form action="allocation" method="GET" style="display: flex; align-items: center; gap: 4px;">
                                        <input type="hidden" name="action" value="allocatePC">
                                        <input type="hidden" name="id" value="${c.id}">
                                        <span style="font-size: 0.62rem; font-weight: 600; color: #64748b; width: 55px;">Đổi máy:</span>
                                        <select name="computerCode" onchange="this.form.submit()" style="height: 20px; font-size: 0.65rem; font-weight: 700; border-radius: 4px; border: 1px solid #cbd5e1; padding: 0; color: #475569; background: #ffffff; cursor: pointer; outline: none; flex-grow: 1;">
                                            <%-- Option 1: PC đang được gán (luôn hiện và selected, kể cả InUse) --%>
                                            <c:if test="${not empty c.computerCode}">
                                                <option value="${c.computerCode}" selected>${c.computerCode} ✓</option>
                                            </c:if>
                                            <c:if test="${empty c.computerCode}">
                                                <option value="" selected>-- Chưa gán --</option>
                                            </c:if>
                                            <%-- Option 2+: Các PC khác available trong cùng phòng --%>
                                            <c:forEach var="pc" items="${requestScope.availableComputers}">
                                                <c:if test="${(pc.areaId eq c.allocatedAreaId or empty c.allocatedAreaId) and pc.computerCode ne c.computerCode}">
                                                    <option value="${pc.computerCode}">${pc.computerCode}</option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                    </form>
                                </div>
                                
                                <c:choose>
                                    <c:when test="${procedureDone}">
                                        <a href="allocation?action=submitTheoryScore&id=${c.id}&score=90" class="btn-pipe-action" style="background: linear-gradient(135deg, #2563eb, #1d4ed8); border: none; margin-top: 6px;">
                                            Chấm điểm Lý thuyết (Auto)
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="candidate-pipe-details" style="color: #b45309; font-weight: 500; font-size: 0.68rem; margin-top: 4px; margin-bottom: 4px;">
                                            &bull; Cần hoàn tất chụp ảnh hồ sơ
                                        </div>
                                        <a href="allocation?action=quickComplete&id=${c.id}" class="btn-pipe-action btn-pipe-action--secondary" style="height: 24px; font-size: 0.65rem; margin-top: 2px;">
                                            Mô phỏng Xong hồ sơ
                                        </a>
                                        <span class="btn-pipe-action btn-pipe-action--disabled" style="height: 24px; font-size: 0.65rem; margin-top: 2px; text-align: center;">
                                            Chưa đủ điều kiện thi
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${theoryCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 2rem; width: 100%;">
                            Chưa có thí sinh nào đủ hồ sơ chờ thi Lý thuyết.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- STEP 3: PRACTICAL EXAM -->
            <div class="pipeline-column">
                <input type="checkbox" id="toggle-row-step-3" class="row-toggle-checkbox" style="display: none !important;">
                <div class="pipeline-header" style="width: 240px; border-right: 1px solid #e2e8f0; padding-right: 1.5rem; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0;">
                    <div>
                        <span class="pipeline-step-badge pipeline-step-badge--3">Bước 3</span>
                        <h3 class="pipeline-title" style="font-size: 1rem; font-weight: 800; color: #10b981; margin: 0; display: flex; align-items: center; gap: 6px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="color: #10b981;">
                                <circle cx="12" cy="12" r="10"/>
                                <path d="M12 6v6l4 2"/>
                            </svg>
                            Sân thi thực hành
                        </h3>
                        <div class="area-meta-box" style="margin-top: 8px;">
                            <span><strong>Khu vực:</strong> Sân Sa Hình Số 1</span>
                            <span><strong>Sức chứa:</strong> 15 xe chíp | <strong>Loại:</strong> Ground</span>
                        </div>
                    </div>
                    <label for="toggle-row-step-3" class="btn-expand-row" style="margin-top: 1rem;">
                        <svg class="expand-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                            <path d="M6 9l6 6 6-6"/>
                        </svg>
                    </label>
                </div>
                
                <div class="pipeline-card-list" style="flex-grow: 1; display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 1rem; padding: 0.5rem 0; min-height: 120px; align-items: center;">
                    <c:set var="practicalCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.theoryPassed eq 'passed' and c.practicalPassed eq 'none'}">
                            <c:set var="practicalCount" value="${practicalCount + 1}" />
                            <div class="candidate-pipe-card" style="width: 220px; flex-shrink: 0; border-left: 3px solid #10b981;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #10b981;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c.clazz}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details">
                                    Lý thuyết: <strong style="color: #10b981;">${c.theoryScore} (ĐẠT)</strong> tại máy ${c.computerCode}
                                    <div style="color: #065f46; font-weight: 700; margin-top: 4px; display: flex; align-items: center; gap: 4px; margin-bottom: 4px;">
                                        <span>Xe chíp:</span>
                                        <span style="background-color: #10b981; color: #ffffff; padding: 2px 6px; border-radius: 4px; font-size: 0.72rem; font-weight: 800;">${c.deviceCode}</span>
                                    </div>
                                    
                                    <!-- Manual Vehicle Override — lọc xe theo hạng bằng -->
                                    <form action="allocation" method="GET" style="display: flex; align-items: center; gap: 4px; margin-top: 2px;">
                                        <input type="hidden" name="action" value="allocateDevice">
                                        <input type="hidden" name="id" value="${c.id}">
                                        <span style="font-size: 0.62rem; font-weight: 600; color: #64748b; width: 45px;">Đổi xe:</span>
                                        <select name="deviceCode" onchange="this.form.submit()" style="height: 20px; font-size: 0.65rem; font-weight: 700; border-radius: 4px; border: 1px solid #cbd5e1; padding: 0; color: #475569; background: #ffffff; cursor: pointer; outline: none; flex-grow: 1;">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(c.licenseCode,'A')}">
                                                    <%-- Hạng A → chỉ hiện xe máy --%>
                                                    <c:forEach var="dev" items="${requestScope.motorbikeDevices}">
                                                        <option value="${dev.deviceName}" ${c.deviceCode eq dev.deviceName ? 'selected' : ''}>${dev.deviceName}</option>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <%-- Hạng B/C/... → chỉ hiện ô tô --%>
                                                    <c:forEach var="dev" items="${requestScope.carDevices}">
                                                        <option value="${dev.deviceName}" ${c.deviceCode eq dev.deviceName ? 'selected' : ''}>${dev.deviceName}</option>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </select>
                                    </form>
                                </div>
                                <a href="allocation?action=submitPracticalScore&id=${c.id}&score=90" class="btn-pipe-action" style="background: linear-gradient(135deg, #10b981, #059669); border: none; margin-top: 6px;">
                                    Chấm điểm Sa hình (Auto)
                                </a>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${practicalCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 2rem; width: 100%;">
                            Chưa có thí sinh nào thi đạt lý thuyết chờ sát hạch Sa hình.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- STEP 4: ROAD TEST (THI ĐƯỜNG TRƯỜNG) -->
            <div class="pipeline-column">
                <input type="checkbox" id="toggle-row-step-4" class="row-toggle-checkbox" style="display: none !important;">
                <div class="pipeline-header" style="width: 240px; border-right: 1px solid #e2e8f0; padding-right: 1.5rem; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0;">
                    <div>
                        <span class="pipeline-step-badge pipeline-step-badge--5" style="background-color: rgba(124, 58, 237, 0.1); color: #7c3aed;">Bước 4</span>
                        <h3 class="pipeline-title" style="font-size: 1rem; font-weight: 800; color: #7c3aed; margin: 0; display: flex; align-items: center; gap: 6px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="color: #7c3aed;">
                                <circle cx="12" cy="12" r="10"/>
                                <path d="M12 6v6l4 2"/>
                            </svg>
                            Thi đường trường
                        </h3>
                        <div class="area-meta-box" style="margin-top: 8px;">
                            <span><strong>Khu vực:</strong> Đường trường ngoài sân</span>
                            <span><strong>Đối tượng:</strong> Thí sinh bằng B/C/D/E/F</span>
                        </div>
                    </div>
                    <label for="toggle-row-step-4" class="btn-expand-row" style="margin-top: 1rem;">
                        <svg class="expand-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                            <path d="M6 9l6 6 6-6"/>
                        </svg>
                    </label>
                </div>
                
                <div class="pipeline-card-list" style="flex-grow: 1; display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 1rem; padding: 0.5rem 0; min-height: 120px; align-items: center;">
                    <c:set var="roadCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.requiresRoadTest and c.practicalPassed eq 'passed' and c.roadTestPassed eq 'none'}">
                            <c:set var="roadCount" value="${roadCount + 1}" />
                            <div class="candidate-pipe-card" style="width: 220px; flex-shrink: 0; border-left: 3px solid #7c3aed;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #7c3aed;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c.clazz}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details" style="font-size: 0.72rem; color: #4b5563; line-height: 1.5; margin-bottom: 4px;">
                                    &bull; Lý thuyết: <strong style="color: #2563eb;">${c.theoryScore} (ĐẠT)</strong><br>
                                    &bull; Sa hình: <strong style="color: #10b981;">${c.practicalScore} (ĐẠT)</strong>
                                </div>
                                <a href="allocation?action=submitRoadScore&id=${c.id}&score=90" class="btn-pipe-action" style="background: linear-gradient(135deg, #7c3aed, #6d28d9); border: none; margin-top: 6px;">
                                    Chấm điểm Đường trường (Auto)
                                </a>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${roadCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 2rem; width: 100%;">
                            Chưa có thí sinh nào đạt Sa hình chờ thi Đường trường.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- STEP 5: COMPLETED SESSION (TỔNG HỢP KẾT QUẢ) -->
            <div class="pipeline-column">
                <input type="checkbox" id="toggle-row-step-5" class="row-toggle-checkbox" style="display: none !important;">
                <div class="pipeline-header" style="width: 240px; border-right: 1px solid #e2e8f0; padding-right: 1.5rem; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0;">
                    <div>
                        <span class="pipeline-step-badge pipeline-step-badge--4" style="background-color: rgba(16, 185, 129, 0.1); color: #10b981;">Bước 5</span>
                        <h3 class="pipeline-title" style="font-size: 1rem; font-weight: 800; color: #10b981; margin: 0; display: flex; align-items: center; gap: 6px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="color: #10b981;">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                <path d="M22 4L12 14.01l-3-3"/>
                            </svg>
                            Tổng hợp kết quả
                        </h3>
                        <div class="area-meta-box" style="margin-top: 8px;">
                            <span><strong>Khu vực:</strong> Hoàn thành ca sát hạch</span>
                            <span><strong>Đối tượng:</strong> Thí sinh đã hoàn thành thi</span>
                        </div>
                    </div>
                    <label for="toggle-row-step-5" class="btn-expand-row" style="margin-top: 1rem;">
                        <svg class="expand-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                            <path d="M6 9l6 6 6-6"/>
                        </svg>
                    </label>
                </div>
                
                <div class="pipeline-card-list" style="flex-grow: 1; display: flex; flex-wrap: nowrap; overflow-x: auto; gap: 1rem; padding: 0.5rem 0; min-height: 120px; align-items: center;">
                    <c:set var="completedCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${(c.practicalPassed eq 'failed') or (c.practicalPassed eq 'passed' and not c.requiresRoadTest) or (c.requiresRoadTest and (c.roadTestPassed eq 'passed' or c.roadTestPassed eq 'failed'))}">
                            <c:set var="completedCount" value="${completedCount + 1}" />
                            <c:set var="isPass" value="${(c.practicalPassed eq 'passed' and not c.requiresRoadTest) or (c.requiresRoadTest and c.roadTestPassed eq 'passed')}" />
                            
                            <div class="candidate-pipe-card" style="width: 220px; flex-shrink: 0; background: ${isPass ? '#f0fdf4' : '#fef2f2'}; border-color: ${isPass ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}; border-left: 3px solid ${isPass ? '#10b981' : '#ef4444'};">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: ${isPass ? '#059669' : '#ef4444'};">${c.sbd}</span>
                                    <span style="font-size: 0.6rem; background-color: ${isPass ? '#d1fae5' : '#fee2e2'}; color: ${isPass ? '#065f46' : '#991b1b'}; font-weight: 800; padding: 2px 6px; border-radius: 4px;">
                                        ${isPass ? 'ĐẠT' : 'TRƯỢT'}
                                    </span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details" style="font-size: 0.7rem; color: #374151; line-height: 1.5;">
                                    &bull; Lý thuyết: <strong style="color: #2563eb;">${c.theoryScore} (ĐẠT)</strong><br>
                                    &bull; Sa hình: <strong style="color: ${c.practicalPassed eq 'passed' ? '#10b981' : '#ef4444'};">${c.practicalScore} (${c.practicalPassed eq 'passed' ? 'ĐẠT' : 'TRƯỢT'})</strong><br>
                                    <c:if test="${c.requiresRoadTest and c.practicalPassed eq 'passed'}">
                                        &bull; Đường trường: <strong style="color: ${c.roadTestPassed eq 'passed' ? '#7c3aed' : '#ef4444'};">${c.roadTestScore} (${c.roadTestPassed eq 'passed' ? 'ĐẠT' : 'TRƯỢT'})</strong><br>
                                    </c:if>
                                    &bull; Kết luận: <strong style="color: ${isPass ? '#16a34a' : '#dc2626'};">${isPass ? 'CẤP GPLX' : 'TRƯỢT SÁT HẠCH'}</strong>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <c:if test="${completedCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 2rem; width: 100%;">
                            Chưa có thí sinh nào hoàn thành ca sát hạch.
                        </div>
                    </c:if>
                </div>
            </div>
            
        </div>
    </main>
    
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<!-- Search JavaScript filter function -->
<script>
    function filterCandidates() {
        var query = document.getElementById("candidateSearch").value.toLowerCase().trim();
        var cards = document.querySelectorAll(".candidate-pipe-card");
        
        cards.forEach(function(card) {
            var text = card.textContent.toLowerCase();
            if (text.indexOf(query) > -1) {
                card.style.display = "";
            } else {
                card.style.display = "none";
            }
        });
    }
</script>

</body>
</html>
