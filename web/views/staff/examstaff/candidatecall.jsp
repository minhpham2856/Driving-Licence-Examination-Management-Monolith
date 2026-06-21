<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Retrieve the candidate queue from the session dynamically
    java.util.List<Models.ExamRegistration> qList = (java.util.List<Models.ExamRegistration>) session.getAttribute("candidateQueue");
    
    // Check if shift is ended
    String shiftEndedVal = (String) session.getAttribute("shiftEnded");
    boolean isShiftEnded = "true".equals(shiftEndedVal);

    // Retrieve active session ID from session
    Integer sessIdObj = (Integer) session.getAttribute("selectedSessionId");
    int sessId = (sessIdObj != null) ? sessIdObj : 2; // Default to B2 session

    DAOs.ExamSessionDAO sessDAO = new DAOs.Impl.ExamSessionDAOImpl();
    Models.ExamSession currentSession = null;
    try {
        currentSession = sessDAOs.getById(sessId);
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (currentSession != null) {
        request.setAttribute("currentSession", currentSession);
    }

    if (qList == null && !isShiftEnded) {
        DAOs.ExamRegistrationDAO regDAO = new DAOs.Impl.ExamRegistrationDAOImpl();
        try {
            qList = regDAOs.getCandidatesBySession(sessId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new java.util.ArrayList<>();
        }
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("callingSbd", null);
    }
    if (qList != null) {
        Controllers.Staff.ExamStaff.CandidatePhotoHelper.normalizeQueue(
            application.getRealPath("/"), qList, new DAOs.Impl.ExamRegistrationDAOImpl());
    }
    
    // Find active candidate (bỏ qua thí sinh đã hoàn tất thủ tục)
    String sbdParam = (String) session.getAttribute("callingSbd");
    Models.ExamRegistration callingCandidate = null;
    if (sbdParam != null && !sbdParam.trim().isEmpty() && qList != null) {
        for (Models.ExamRegistration c : qList) {
            if (sbdParam.equals(c.getSbd())) {
                boolean procedureDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                if (!procedureDone) {
                    callingCandidate = c;
                } else {
                    String nextSbd = null;
                    for (Models.ExamRegistration pending : qList) {
                        if (!(pending.isPaymentCompleted() && pending.isValidCapturedPhoto())) {
                            nextSbd = pending.getSbd();
                            break;
                        }
                    }
                    session.setAttribute("callingSbd", nextSbd);
                    sbdParam = nextSbd;
                    if (nextSbd != null) {
                        for (Models.ExamRegistration pending : qList) {
                            if (nextSbd.equals(pending.getSbd())) {
                                callingCandidate = pending;
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    if (callingCandidate != null) {
        request.setAttribute("callingCandidate", callingCandidate);
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gọi Làm Thủ Tục - Ban Sát Hạch</title>
    
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
    <jsp:param name="activeSidebar" value="goi-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <c:choose>
                <c:when test="${requestScope.deskMode}">
                    <span class="breadcrumbs__current">Gọi làm thủ tục</span>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Bàn làm thủ tục</span>
                </c:when>
                <c:otherwise>
                    <span class="breadcrumbs__current" aria-current="page">Gọi làm thủ tục</span>
                </c:otherwise>
            </c:choose>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Gọi thí sinh vào làm thủ tục</h1>
                <p class="page-subtitle">Điều hành loa gọi hàng đợi và làm thủ tục 3 bước trên cùng một màn hình.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <div style="display: flex; align-items: center; gap: 6px; background: #ffffff; padding: 6px 12px; border-radius: 8px; border: 1px solid #e2e8f0;">
                    <span style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Ca thi:</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: #0f172a;">
                        <c:out value="${currentSession.sessionName}" /> (<c:out value="${currentSession.licenseCode}" />)
                    </span>
                </div>
                <a href="procedure#procedure-desk" class="btn-export" style="height: 38px; padding: 0 1rem; font-size: 0.82rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    Bàn làm thủ tục
                </a>
                <a href="${pageContext.request.contextPath}/views/public/public-call?sessionId=${sessionScope.selectedSessionId != null ? sessionScope.selectedSessionId : 2}"
                   target="_blank" rel="noopener"
                   class="btn-filter" style="height: 38px; padding: 0 1rem; font-size: 0.82rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background: #0f172a; border-color: #0f172a; color: #ffffff;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                    Mở màn hình TV
                </a>
            </div>
        </header>

        <!-- Dynamic Alerts for Absent Operations -->
        <c:if test="${not empty requestScope.absentAlert}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b91c1c;">
                    Đã đánh dấu thí sinh <strong style="color: #7f1d1d;">${requestScope.absentAlert}</strong> vắng mặt! Hệ thống đã xếp người này xuống cuối danh sách chờ và tự động gọi thí sinh kế tiếp.
                </span>
            </div>
        </c:if>
        
        <c:if test="${not empty requestScope.autoAbsentAlert}">
            <div style="background-color: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #d97706; flex-shrink: 0;">
                    <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b45309;">
                    [Tự Động Hết Giờ] Thí sinh <strong style="color: #78350f;">${requestScope.autoAbsentAlert}</strong> đã quá 3 phút chưa trình diện! Hệ thống tự động chuyển xuống cuối hàng đợi.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.permanentAbsentAlert}">
            <div style="background-color: #fef2f2; border: 1px solid #ef4444; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated shake">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b91c1c;">
                    Đã xác nhận thí sinh <strong style="color: #7f1d1d;">${requestScope.permanentAbsentAlert}</strong> vắng thi! Kết quả thi của thí sinh được ghi nhận là TRƯỢT và khóa hồ sơ.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.undoAlert}">
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated bounceIn">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #065f46;">
                    Đã hoàn tác trạng thái vắng mặt của thí sinh <strong style="color: #047857;">${requestScope.undoAlert}</strong> thành công! Đưa thí sinh trở về đầu hàng đợi để tiếp tục gọi.
                </span>
            </div>
        </c:if>
        
        <!-- Main grid for calling console -->
        <div class="report-grid" style="grid-template-columns: 1.32fr 1.68fr; gap: 1.5rem; display: grid;">
            
            <!-- Left Pane: Call Queue Console Controller -->
            <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                
                <c:choose>
                    <c:when test="${sessionScope.shiftEnded eq 'true'}">
                        <!-- SHIFT ENDED PANE -->
                        <div class="waiting-list-pane" style="text-align: center; padding: 3rem 1.5rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;">
                            <div style="background-color: #fee2e2; border-radius: 50%; width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2);">
                                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" fill="currentColor"/>
                                </svg>
                            </div>
                            <h3 style="margin: 0; font-size: 1.2rem; font-weight: 800; color: #991b1b;">Ca làm việc đã kết thúc</h3>
                            <p style="margin: 0; font-size: 0.85rem; color: #64748b; max-width: 290px; line-height: 1.5;">Hàng đợi điều hành phòng chờ chính đã được dọn dẹp sạch sẽ. Tất cả thí sinh còn lại đã được giải phóng.</p>
                            
                            <a href="candidatecall?action=startShift" class="btn-batch" style="background: linear-gradient(135deg, #0052cc, #003d9b); border: none; font-size: 0.88rem; height: 42px; margin-top: 1rem; width: auto; padding: 0 1.5rem;">
                                Khởi động ca làm việc mới
                            </a>
                        </div>
                    </c:when>
                    
                    <c:otherwise>
                        <!-- SHIFT IS ACTIVE -->
                        <div class="waiting-list-pane">
                            <h3 class="called-status-title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                                    <path d="M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                </svg>
                                Bảng điều hành loa gọi thi
                            </h3>
                            
                            <c:choose>
                                <c:when test="${empty sessionScope.callingSbd}">
                                    <!-- IDLE STATE: WAITING FOR START COMMAND -->
                                    <div style="text-align: center; padding: 2rem 1rem; background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; margin-bottom: 1.25rem;">
                                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #94a3b8; margin: 0 auto 0.5rem; display: block;">
                                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z" fill="currentColor"/>
                                        </svg>
                                        <span style="font-weight: 700; font-size: 0.85rem; color: #475569; display: block; text-transform: uppercase; margin-bottom: 4px;">Hàng đợi đang dừng gọi</span>
                                        <span style="font-size: 0.78rem; color: #64748b;">Nhấn Bắt đầu gọi bên dưới để tự động gọi người đứng đầu hàng đợi.</span>
                                    </div>
                                    
                                    <c:choose>
                                        <c:when test="${empty sessionScope.candidateQueue}">
                                            <!-- QUEUE EMPTY -->
                                            <button class="btn-batch" style="background-color: #e2e8f0; border-color: #cbd5e1; color: #94a3b8; cursor: not-allowed; font-size: 0.9rem;" disabled>
                                                Hàng đợi trống - Không thể gọi
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <!-- QUEUE HAS CANDIDATES -->
                                            <a href="candidatecall?action=startCall" class="btn-batch" style="background: linear-gradient(135deg, #10b981, #059669); border: none; font-size: 0.92rem; height: 46px;">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" fill="currentColor"/>
                                                </svg>
                                                Bắt đầu gọi thi (Tự động)
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                    
                                    <a href="candidatecall?action=endShift" class="btn-batch btn-batch--alt" style="margin-top: 0.75rem; border-color: rgba(239, 68, 68, 0.2); color: #ef4444; background: rgba(239, 68, 68, 0.01); font-size: 0.85rem; height: 38px;">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <path d="M9 9h6v6H9z" fill="currentColor"/>
                                        </svg>
                                        Kết thúc ca trực
                                    </a>
                                </c:when>
                                
                                <c:otherwise>
                                    <!-- ACTIVELY CALLING A CANDIDATE -->
                                    <!-- Audio TTS wave sound broadcaster -->
                                    <div class="soundwave-container" style="margin-bottom: 0px;">
                                        <span style="color: #94a3b8; font-size: 0.68rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em; display: block; margin-bottom: 8px;">Audio TTS Broadcaster</span>
                                        
                                        <span style="color: #ffffff; font-size: 0.95rem; font-weight: 700; line-height: 1.4; max-width: 280px; display: block;">
                                            "Mời thí sinh có số báo danh ${callingCandidate.sbd}, ${callingCandidate.name} nhanh chóng di chuyển vào bàn làm thủ tục!"
                                        </span>
                                        
                                        <!-- Sound Wave Visual Effects -->
                                        <div class="soundwave-animation">
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                            <div class="soundwave-bar"></div>
                                        </div>
                                    </div>
                                    
                                    <div class="active-calling-card">
                                        <span style="font-size: 0.72rem; font-weight: 800; color: #0052cc; text-transform: uppercase; letter-spacing: 0.05em; display: block; margin-bottom: 6px;">Học viên đang gọi lên bàn:</span>
                                        <div style="font-family: monospace; font-size: 2.25rem; font-weight: 900; color: #0f172a; letter-spacing: 0.02em; line-height: 1.1;">
                                            ${callingCandidate.sbd}
                                        </div>
                                        <div style="font-size: 1.15rem; font-weight: 800; color: #1e293b; margin-top: 4px;">
                                            ${callingCandidate.name}
                                        </div>
                                        <div style="display: flex; gap: 8px; align-items: center; margin-top: 8px;">
                                            <span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 8px;">Hạng ${callingCandidate.clazz}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; font-family: monospace;">CCCD: ${callingCandidate.cccd}</span>
                                        </div>

                                        <c:if test="${not empty requestScope.nextCallingCandidate}">
                                            <div style="margin-top: 1rem; padding: 10px 12px; background: rgba(16, 185, 129, 0.06); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: 10px; text-align: left; width: 100%;">
                                                <span style="font-size: 0.68rem; font-weight: 800; color: #047857; text-transform: uppercase; letter-spacing: 0.05em;">Chuẩn bị tiếp theo (hiển thị TV)</span>
                                                <div style="margin-top: 4px; font-family: monospace; font-weight: 800; color: #059669; font-size: 1rem;">${requestScope.nextCallingCandidate.sbd}</div>
                                                <div style="font-size: 0.85rem; font-weight: 700; color: #1e293b;">${requestScope.nextCallingCandidate.name} &mdash; Hạng ${requestScope.nextCallingCandidate.clazz}</div>
                                            </div>
                                        </c:if>
                                        
                                        <!-- Time Limit Countdown Bar -->
                                        <div style="margin-top: 1.25rem; text-align: left; width: 100%;">
                                            <div style="display: flex; justify-content: space-between; font-size: 0.7rem; font-weight: 800; color: #64748b; margin-bottom: 4px;">
                                                <span>GIỚI HẠN THỦ TỤC TRÌNH DIỆN</span>
                                                <span id="countdownText" style="font-family: monospace; color: #10b981; font-weight: 800;">180 Giây</span>
                                            </div>
                                            <div style="background-color: rgba(0,0,0,0.06); border: 1px solid rgba(0,0,0,0.05); height: 6px; border-radius: 99px; overflow: hidden; width: 100%;">
                                                <div id="countdownBar" class="countdown-bar" style="width: 100%; height: 100%; background: #10b981;"></div>
                                            </div>
                                            <span style="font-size: 0.65rem; color: #94a3b8; display: block; margin-top: 4px; line-height: 1.3;">
                                                Hệ thống sẽ tự chuyển người sau 3 phút nếu thí sinh này không có mặt.
                                            </span>
                                        </div>
                                        
                                        <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 1.25rem; border-top: 1px solid #e2e8f0; padding-top: 1rem;">
                                            <a href="procedure?sbd=${callingCandidate.sbd}#procedure-desk" class="btn-batch" style="background-color: #0052cc; border-color: #0052cc; height: 40px; font-size: 0.85rem;">
                                                Tiến hành lập hồ sơ &rarr;
                                            </a>
                                            
                                            <div style="display: flex; gap: 8px; width: 100%;">
                                                <a href="candidatecall?action=absent&sbd=${callingCandidate.sbd}" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; border-color: rgba(245, 158, 11, 0.3); color: #d97706; background: rgba(245, 158, 11, 0.01); font-size: 0.8rem;" title="Đẩy xuống cuối hàng đợi">
                                                    Vắng tạm thời
                                                </a>
                                                
                                                <a href="candidatecall?action=endShift" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; font-size: 0.8rem; border-color: #cbd5e1; color: #64748b;" title="Đóng ca thi hiện tại">
                                                    Đóng ca
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>
                
            </div>
            
            <!-- Right Pane: List of Called Queue Candidates -->
            <div class="waiting-list-pane">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <h3 class="called-status-title" style="margin: 0; font-size: 0.95rem;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                                <path d="M7 8h10M7 12h10M7 16h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Hàng đợi thí sinh đang chờ sát hạch
                        </h3>
                        <c:if test="${sessionScope.shiftEnded ne 'true'}">
                            <!-- Calculate pending count (not completed procedures) dynamically -->
                            <c:set var="pendingCount" value="0" />
                            <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                <c:set var="isCdone" value="${c.validCapturedPhoto and c.paymentCompleted}" />
                                <c:if test="${not isCdone}">
                                    <c:set var="pendingCount" value="${pendingCount + 1}" />
                                </c:if>
                            </c:forEach>
                            <span style="background: rgba(16, 185, 129, 0.1); color: #047857; border: 1px solid rgba(16, 185, 129, 0.2); font-size: 0.65rem; font-weight: 800; padding: 2px 6px; border-radius: 4px;">
                                ${pendingCount} Người
                            </span>
                        </c:if>
                    </div>
                    
                    <c:if test="${sessionScope.shiftEnded ne 'true'}">
                        <div style="display: flex; gap: 6px; align-items: center;">
                            <span style="font-size: 0.68rem; font-weight: 700; color: #94a3b8; animation: pulse-green 2s infinite;">Tự refresh (10s)</span>
                            <!-- Manual refresh button -->
                            <a href="candidatecall" class="btn-batch btn-batch--alt" style="width: 32px; height: 32px; padding: 0; display: inline-flex; align-items: center; justify-content: center; border-radius: 6px;" title="Làm mới hàng đợi">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </a>
                        </div>
                    </c:if>
                </div>
                
                <c:choose>
                    <c:when test="${sessionScope.shiftEnded eq 'true' or pendingCount eq 0}">
                        <!-- Empty Queue State -->
                        <div style="text-align: center; padding: 5rem 1rem; color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 8px;">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="opacity: 0.4;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            <span style="font-weight: 700; font-size: 0.9rem; color: #475569;">Hàng đợi trống</span>
                            <span style="font-size: 0.78rem; max-width: 250px;">Không có thí sinh nào trong hàng đợi của ca thi này.</span>
                        </div>
                    </c:when>
                    
                    <c:otherwise>
                        <!-- Render Queue List -->
                        <div class="table-responsive" style="max-height: 480px; overflow-y: auto;">
                            <table class="audit-table" style="font-size: 0.85rem; width: 100%;">
                                <thead>
                                    <tr>
                                        <th scope="col" style="width: 80px;">SBD</th>
                                        <th scope="col">Họ tên</th>
                                        <th scope="col" style="width: 60px; text-align: center;">Hạng</th>
                                        <th scope="col" style="width: 110px; text-align: center;">CCCD</th>
                                        <th scope="col" style="width: 140px; text-align: right;">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="candidate" items="${sessionScope.candidateQueue}" varStatus="status">
                                        <c:set var="cDone" value="${candidate.validCapturedPhoto and candidate.paymentCompleted}" />
                                        <c:if test="${not cDone}">
                                            <c:set var="isCurrentCalling" value="${candidate.sbd eq sessionScope.callingSbd}" />
                                            <!-- Highlight the active candidate being called -->
                                            <tr style="${isCurrentCalling ? 'background-color: rgba(0, 82, 204, 0.04); border-left: 3px solid #0052cc;' : ''}">
                                                <td style="font-weight: 800; color: #0052cc; font-family: monospace; padding-left: ${isCurrentCalling ? '8px' : '0px'};">
                                                    ${candidate.sbd}
                                                    <c:if test="${isCurrentCalling}">
                                                        <span style="background: #10b981; width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-left: 4px;" title="Đang phát loa"></span>
                                                    </c:if>
                                                </td>
                                                <td style="font-weight: 700; color: #0f172a;">${candidate.name}</td>
                                                <td style="text-align: center;"><span class="role-badge role-badge--coi" style="font-size: 0.65rem; padding: 1px 4px;">${candidate.clazz}</span></td>
                                                <td style="text-align: center; font-family: monospace; color: #475569;">${candidate.cccd}</td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 4px;">
                                                        <a href="procedure?sbd=${candidate.sbd}#procedure-desk" class="btn-filter" style="height: 26px; padding: 0 8px; font-size: 0.7rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center;">Hồ sơ</a>
                                                        <a href="candidatecall?action=absent&sbd=${candidate.sbd}" class="btn-reset" style="height: 26px; padding: 0 8px; font-size: 0.7rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; color: #d97706; border-color: rgba(245, 158, 11, 0.3); background: rgba(245, 158, 11, 0.02);" title="Đẩy xuống cuối hàng chờ">Vắng tạm</a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            
        </div>

        <c:if test="${requestScope.deskMode}">
            <jsp:include page="/views/staff/examstaff/procedure.jsp"/>
        </c:if>

        <!-- Permanent Absents List & Undo Option (Exception Safety) (UC-03) -->
        <c:if test="${not empty sessionScope.permanentAbsents}">
            <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
                <div style="border-bottom: 1.5px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                    <h3 style="font-size: 0.95rem; font-weight: 800; color: #dc2626; margin: 0; display: flex; align-items: center; gap: 8px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M18.36 6.64a9 9 0 1 1-12.73 0"/>
                            <line x1="12" y1="2" x2="12" y2="12"/>
                        </svg>
                        Danh sách vắng - Có thể Hoàn tác
                    </h3>
                    <span class="role-badge role-badge--admin" style="font-size: 0.72rem; background-color: #fee2e2; color: #b91c1c; font-weight: 800; padding: 2px 8px;">
                        Đã vắng: ${fn:length(sessionScope.permanentAbsents)} Người
                    </span>
                </div>
                
                <div class="table-responsive" style="max-height: 250px; overflow-y: auto;">
                    <table class="audit-table" style="font-size: 0.85rem; width: 100%;">
                        <thead>
                            <tr>
                                <th scope="col" style="width: 100px;">SBD</th>
                                <th scope="col">Họ tên</th>
                                <th scope="col" style="width: 80px; text-align: center;">Hạng</th>
                                <th scope="col" style="width: 150px; text-align: center;">CCCD</th>
                                <th scope="col" style="width: 150px; text-align: center;">Trạng thái khóa</th>
                                <th scope="col" style="width: 120px; text-align: right;">Khôi phục</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${sessionScope.permanentAbsents}">
                                <tr>
                                    <td style="font-weight: 800; color: #dc2626; font-family: monospace;">${c.sbd}</td>
                                    <td style="font-weight: 700; color: #0f172a;">${c.name}</td>
                                    <td style="text-align: center;"><span class="role-badge role-badge--admin" style="font-size: 0.65rem; padding: 1px 4px; background-color: #fee2e2; color: #991b1b;">${c.clazz}</span></td>
                                    <td style="text-align: center; font-family: monospace; color: #475569;">${c.cccd}</td>
                                    <td style="text-align: center;">
                                        <span class="action-badge action-badge--danger" style="font-weight: 700;">Vắng mặt (Trượt)</span>
                                    </td>
                                    <td style="text-align: right;">
                                        <a href="candidatecall?action=undoAbsent&sbd=${c.sbd}" class="btn-filter" style="height: 26px; padding: 0 10px; font-size: 0.72rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; border-color: #10b981; color: #10b981; background: rgba(16, 185, 129, 0.02);" title="Khôi phục về hàng đợi ban đầu">
                                            Hoàn tác
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<div id="candidateCallConfig"
     data-sbd="${not empty callingCandidate ? callingCandidate.sbd : ''}"
     hidden></div>
<c:if test="${requestScope.deskMode}">
    <c:set var="currentStep" value="${not empty param.step ? param.step : requestScope.step}" />
    <c:if test="${empty currentStep}"><c:set var="currentStep" value="1" /></c:if>
    <div id="procedureCameraConfig"
         data-enabled="${currentStep eq '2' and not requestScope.hasValidPhoto ? 'true' : 'false'}"
         data-ctx-path="${pageContext.request.contextPath}"
         data-sbd="${not empty requestScope.profile ? requestScope.profile.sbd : ''}"
         data-msg-live="LIVE - Camera sẵn sàng"
         data-msg-starting="Đang khởi động camera..."
         data-msg-unavailable="Camera không khả dụng"
         data-msg-no-api="Trình duyệt không hỗ trợ camera. Dùng Chrome/Edge/Firefox trên localhost hoặc HTTPS."
         data-msg-denied="Quyền camera bị từ chối. Cho phép camera trong trình duyệt rồi tải lại trang."
         data-msg-not-found="Không tìm thấy camera trên thiết bị."
         data-msg-open-fail="Không thể mở camera."
         data-msg-not-ready="Camera chưa sẵn sàng. Đợi vài giây rồi thử lại."
         data-msg-frame-fail="Không đọc được khung hình từ camera."
         data-msg-save-fail="Lưu ảnh thất bại: "
         hidden></div>
    <script src="${pageContext.request.contextPath}/assets/js/procedure.js" charset="UTF-8"></script>
</c:if>
<script src="${pageContext.request.contextPath}/assets/js/candidatecall.js" charset="UTF-8"></script>
</body>
</html>
