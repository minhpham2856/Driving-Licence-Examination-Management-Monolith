<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Initialize candidates list in session if not exists
    java.util.List<java.util.Map<String, String>> candidateQueue = (java.util.List<java.util.Map<String, String>>) session.getAttribute("candidateQueue");
    
    // Check if shift is ended
    String shiftEndedVal = (String) session.getAttribute("shiftEnded");
    boolean isShiftEnded = "true".equals(shiftEndedVal);

    if (candidateQueue == null && !isShiftEnded) {
        candidateQueue = new java.util.ArrayList<>();
        
        java.util.Map<String, String> c1 = new java.util.HashMap<>();
        c1.put("sbd", "A1-0024");
        c1.put("name", "Nguyễn Anh Tuấn");
        c1.put("class", "A1");
        c1.put("cccd", "001204008912");
        c1.put("dob", "12/04/2000");
        c1.put("email", "tuanna@gmail.com");
        c1.put("phone", "0912345678");
        c1.put("photoUrl", "");
        c1.put("paymentCompleted", "false");
        c1.put("isPresent", "false");
        c1.put("isCalled", "false");
        c1.put("computerCode", "");
        c1.put("theoryScore", "");
        c1.put("theoryPassed", "none");
        c1.put("deviceCode", "");
        c1.put("practicalScore", "");
        c1.put("practicalPassed", "none");
        candidateQueue.add(c1);
        
        java.util.Map<String, String> c2 = new java.util.HashMap<>();
        c2.put("sbd", "B2-0145");
        c2.put("name", "Trần Thị Mai");
        c2.put("class", "B2");
        c2.put("cccd", "038201004567");
        c2.put("dob", "15/06/1998");
        c2.put("email", "maitt@gmail.com");
        c2.put("phone", "0987654321");
        c2.put("photoUrl", "assets/imgs/candidates/B2-0145_captured.png");
        c2.put("paymentCompleted", "true");
        c2.put("isPresent", "true");
        c2.put("isCalled", "true");
        c2.put("computerCode", "");
        c2.put("theoryScore", "");
        c2.put("theoryPassed", "none");
        c2.put("deviceCode", "");
        c2.put("practicalScore", "");
        c2.put("practicalPassed", "none");
        candidateQueue.add(c2);
        
        java.util.Map<String, String> c3 = new java.util.HashMap<>();
        c3.put("sbd", "B2-0112");
        c3.put("name", "Vũ Huy Hoàng");
        c3.put("class", "B2");
        c3.put("cccd", "038202001112");
        c3.put("dob", "20/09/2002");
        c3.put("email", "hoangvh@gmail.com");
        c3.put("phone", "0945556677");
        c3.put("photoUrl", "");
        c3.put("paymentCompleted", "false");
        c3.put("isPresent", "true");
        c3.put("isCalled", "true");
        c3.put("computerCode", "");
        c3.put("theoryScore", "");
        c3.put("theoryPassed", "none");
        c3.put("deviceCode", "");
        c3.put("practicalScore", "");
        c3.put("practicalPassed", "none");
        candidateQueue.add(c3);
        
        java.util.Map<String, String> c4 = new java.util.HashMap<>();
        c4.put("sbd", "B2-0199");
        c4.put("name", "Đặng Văn Lâm");
        c4.put("class", "B2");
        c4.put("cccd", "030203004455");
        c4.put("dob", "08/08/1996");
        c4.put("email", "lamdv@gmail.com");
        c4.put("phone", "0909998888");
        c4.put("photoUrl", "");
        c4.put("paymentCompleted", "false");
        c4.put("isPresent", "false");
        c4.put("isCalled", "false");
        c4.put("computerCode", "");
        c4.put("theoryScore", "");
        c4.put("theoryPassed", "none");
        c4.put("deviceCode", "");
        c4.put("practicalScore", "");
        c4.put("practicalPassed", "none");
        candidateQueue.add(c4);
        
        java.util.Map<String, String> c5 = new java.util.HashMap<>();
        c5.put("sbd", "A1-0182");
        c5.put("name", "Lê Thị Thanh Huyền");
        c5.put("class", "A1");
        c5.put("cccd", "035201007788");
        c5.put("dob", "11/11/2001");
        c5.put("email", "huyenltt@gmail.com");
        c5.put("phone", "0988776655");
        c5.put("photoUrl", "");
        c5.put("paymentCompleted", "false");
        c5.put("isPresent", "false");
        c5.put("isCalled", "false");
        c5.put("computerCode", "");
        c5.put("theoryScore", "");
        c5.put("theoryPassed", "none");
        c5.put("deviceCode", "");
        c5.put("practicalScore", "");
        c5.put("practicalPassed", "none");
        candidateQueue.add(c5);
        
        java.util.Map<String, String> c6 = new java.util.HashMap<>();
        c6.put("sbd", "A1-0185");
        c6.put("name", "Nguyễn Hoàng Nam");
        c6.put("class", "A1");
        c6.put("cccd", "024201007788");
        c6.put("dob", "05/05/1999");
        c6.put("email", "namnh@gmail.com");
        c6.put("phone", "0977665544");
        c6.put("photoUrl", "");
        c6.put("paymentCompleted", "false");
        c6.put("isPresent", "false");
        c6.put("isCalled", "false");
        c6.put("computerCode", "");
        c6.put("theoryScore", "");
        c6.put("theoryPassed", "none");
        c6.put("deviceCode", "");
        c6.put("practicalScore", "");
        c6.put("practicalPassed", "none");
        candidateQueue.add(c6);
        
        session.setAttribute("candidateQueue", candidateQueue);
        session.setAttribute("callingSbd", null);
    }
    
    // Handle queue operations
    String qAction = request.getParameter("action");
    String qSbd = request.getParameter("sbd");
    
    if ("startCall".equals(qAction)) {
        if (candidateQueue != null) {
            for (java.util.Map<String, String> c : candidateQueue) {
                boolean isDone = c.get("photoUrl") != null && !c.get("photoUrl").isEmpty() && "true".equals(c.get("paymentCompleted"));
                if ("true".equals(c.get("isPresent")) && !isDone) {
                    session.setAttribute("callingSbd", c.get("sbd"));
                    c.put("isCalled", "true");
                    break;
                }
            }
        }
    } else if ("absent".equals(qAction) || "autoAbsent".equals(qAction)) {
        if (candidateQueue != null && qSbd != null) {
            int foundIdx = -1;
            for (int i = 0; i < candidateQueue.size(); i++) {
                if (qSbd.equals(candidateQueue.get(i).get("sbd"))) {
                    foundIdx = i;
                    break;
                }
            }
            if (foundIdx != -1) {
                java.util.Map<String, String> removed = candidateQueue.remove(foundIdx);
                removed.put("isCalled", "false"); // Reset called status so they can be called again later
                candidateQueue.add(removed); // Append to the end of the queue
            }
            
            // Find the next candidate who checked-in but procedures are not finished
            String nextSbd = null;
            if (candidateQueue != null) {
                for (java.util.Map<String, String> c : candidateQueue) {
                    boolean isDone = c.get("photoUrl") != null && !c.get("photoUrl").isEmpty() && "true".equals(c.get("paymentCompleted"));
                    if ("true".equals(c.get("isPresent")) && !isDone && !c.get("sbd").equals(qSbd)) {
                        nextSbd = c.get("sbd");
                        c.put("isCalled", "true");
                        break;
                    }
                }
            }
            session.setAttribute("callingSbd", nextSbd);
            
            if ("autoAbsent".equals(qAction)) {
                request.setAttribute("autoAbsentAlert", qSbd);
            } else {
                request.setAttribute("absentAlert", qSbd);
            }
        }
    } else if ("endShift".equals(qAction)) {
        if (candidateQueue != null) {
            candidateQueue.clear();
        }
        session.setAttribute("callingSbd", null);
        session.setAttribute("shiftEnded", "true");
    } else if ("startShift".equals(qAction)) {
        session.removeAttribute("shiftEnded");
        session.removeAttribute("candidateQueue");
        response.sendRedirect("candidatecall.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <!-- Premium Hybrid Auto-Refresh -->
    <c:choose>
        <c:when test="${not empty sessionScope.callingSbd}">
            <!-- If calling actively, auto-absent timer triggers after 180 seconds -->
            <meta http-equiv="refresh" content="180;url=candidatecall.jsp?action=autoAbsent&sbd=${sessionScope.callingSbd}">
        </c:when>
        <c:when test="${sessionScope.shiftEnded ne 'true'}">
            <!-- If idle, auto-refresh every 10 seconds to sync new examinees -->
            <meta http-equiv="refresh" content="10">
        </c:when>
    </c:choose>
    
    <title>Gọi Làm Thủ Tục - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        .batch-btn-container {
            display: flex;
            gap: 12px;
            margin-bottom: 1.5rem;
        }
        
        .btn-batch {
            width: 100%;
            padding: 0.88rem;
            border-radius: 12px;
            font-size: 0.92rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            cursor: pointer;
            transition: all 0.2s ease;
            text-decoration: none;
            border: 1px solid #0052cc;
            background-color: #0052cc;
            color: #ffffff;
            box-shadow: 0 4px 10px rgba(0, 82, 204, 0.15);
        }
        .btn-batch:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 15px rgba(0, 82, 204, 0.2);
        }
        
        .btn-batch--alt {
            background-color: #ffffff;
            color: #475569;
            border-color: #cbd5e1;
            box-shadow: 0 2px 5px rgba(0,0,0,0.02);
        }
        .btn-batch--alt:hover {
            background-color: #f8fafc;
            border-color: #94a3b8;
            color: #0f172a;
        }
        
        /* Premium Wave Sound Animation */
        .soundwave-container {
            background: linear-gradient(135deg, #1e293b, #0f172a);
            border-radius: 16px;
            padding: 2rem 1.5rem;
            text-align: center;
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 180px;
            box-shadow: inset 0 2px 10px rgba(0,0,0,0.3);
            margin-bottom: 1.25rem;
            border: 1px solid #334155;
        }
        
        .soundwave-animation {
            display: flex;
            align-items: center;
            gap: 4px;
            height: 40px;
            margin-top: 1rem;
        }
        
        .soundwave-bar {
            width: 4px;
            height: 10px;
            background-color: #10b981;
            border-radius: 99px;
            animation: waveBounce 1s infinite alternate;
        }
        .soundwave-bar:nth-child(2) { height: 20px; animation-delay: 0.1s; }
        .soundwave-bar:nth-child(3) { height: 35px; animation-delay: 0.2s; }
        .soundwave-bar:nth-child(4) { height: 45px; animation-delay: 0.3s; }
        .soundwave-bar:nth-child(5) { height: 25px; animation-delay: 0.4s; }
        .soundwave-bar:nth-child(6) { height: 15px; animation-delay: 0.2s; }
        .soundwave-bar:nth-child(7) { height: 30px; animation-delay: 0.1s; }
        .soundwave-bar:nth-child(8) { height: 40px; animation-delay: 0.5s; }
        .soundwave-bar:nth-child(9) { height: 18px; animation-delay: 0.3s; }
        .soundwave-bar:nth-child(10) { height: 8px; animation-delay: 0.1s; }
        
        @keyframes waveBounce {
            0% { transform: scaleY(0.3); }
            100% { transform: scaleY(1.1); }
        }
        
        .waiting-list-pane {
            background-color: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 1.5rem;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.02);
            height: 100%;
        }
        
        .called-status-title {
            font-size: 1rem;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 1rem;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        /* 3-Minute CSS3 Countdown Animation */
        @keyframes countdownAnimation {
            from { width: 100%; background: #10b981; }
            50% { background: #f59e0b; }
            to { width: 0%; background: #ef4444; }
        }
        
        .countdown-bar {
            height: 100%;
            width: 100%;
            animation: countdownAnimation 180s linear forwards;
        }
        
        .active-calling-card {
            background: rgba(15, 23, 42, 0.02);
            border: 2px solid rgba(0, 82, 204, 0.15);
            border-radius: 16px;
            padding: 1.5rem;
            margin-top: 1rem;
        }
    </style>
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
            <span class="breadcrumbs__current" aria-current="page">Gọi làm thủ tục</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Gọi thí sinh vào làm thủ tục</h1>
                <p class="page-subtitle">Hệ thống điều hành phòng chờ chính, gọi loa tự động (TTS) theo hàng đợi danh sách đầy đủ và quản lý vắng mặt.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center; background: #ffffff; padding: 6px 12px; border-radius: 8px; border: 1px solid #e2e8f0;">
                <div style="display: flex; align-items: center; gap: 6px;">
                    <span style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Ca thi:</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: #0f172a;">Ca Sáng 24/05</span>
                </div>
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
        
        <!-- Main grid for calling console -->
        <div class="report-grid" style="grid-template-columns: 1.32fr 1.68fr; gap: 1.5rem;">
            
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
                            
                            <a href="candidatecall.jsp?action=startShift" class="btn-batch" style="background: linear-gradient(135deg, #0052cc, #003d9b); border: none; font-size: 0.88rem; height: 42px; margin-top: 1rem; width: auto; padding: 0 1.5rem;">
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
                                            <a href="candidatecall.jsp?action=startCall" class="btn-batch" style="background: linear-gradient(135deg, #10b981, #059669); border: none; font-size: 0.92rem; height: 46px;">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" fill="currentColor"/>
                                                </svg>
                                                Bắt đầu gọi thi (Tự động)
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                    
                                    <a href="candidatecall.jsp?action=endShift" class="btn-batch btn-batch--alt" style="margin-top: 0.75rem; border-color: rgba(239, 68, 68, 0.2); color: #ef4444; background: rgba(239, 68, 68, 0.01); font-size: 0.85rem; height: 38px;">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <path d="M9 9h6v6H9z" fill="currentColor"/>
                                        </svg>
                                        Kết thúc
                                    </a>
                                </c:when>
                                
                                <c:otherwise>
                                    <!-- ACTIVELY CALLING A CANDIDATE -->
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.sbd eq sessionScope.callingSbd}">
                                            <c:set var="callingCandidate" value="${c}" />
                                        </c:if>
                                    </c:forEach>
                                    
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
                                            <span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 8px;">Hạng ${callingCandidate['class']}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; font-family: monospace;">CCCD: ${callingCandidate.cccd}</span>
                                        </div>
                                        
                                        <!-- Time Limit Countdown Bar -->
                                        <div style="margin-top: 1.25rem; text-align: left; width: 100%;">
                                            <div style="display: flex; justify-content: space-between; font-size: 0.7rem; font-weight: 800; color: #64748b; margin-bottom: 4px;">
                                                <span>GIỚI HẠN THỦ TỤC TRÌNH DIỆN</span>
                                                <span style="font-family: monospace; color: #ef4444; font-weight: 800;">180 Giây</span>
                                            </div>
                                            <div style="background-color: rgba(0,0,0,0.06); border: 1px solid rgba(0,0,0,0.05); height: 6px; border-radius: 99px; overflow: hidden; width: 100%;">
                                                <div class="countdown-bar"></div>
                                            </div>
                                            <span style="font-size: 0.65rem; color: #94a3b8; display: block; margin-top: 4px; line-height: 1.3;">
                                                Hệ thống sẽ tự chuyển người sau 3 phút nếu thí sinh này không có mặt.
                                            </span>
                                        </div>
                                        
                                        <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 1.25rem; border-top: 1px solid #e2e8f0; padding-top: 1rem;">
                                            <a href="procedure.jsp?sbd=${callingCandidate.sbd}" class="btn-batch" style="background-color: #0052cc; border-color: #0052cc; height: 40px; font-size: 0.85rem;">
                                                Tiến hành lập hồ sơ &rarr;
                                            </a>
                                            
                                            <div style="display: flex; gap: 8px; width: 100%;">
                                                <a href="candidatecall.jsp?action=absent&sbd=${callingCandidate.sbd}" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; border-color: rgba(239, 68, 68, 0.3); color: #dc2626; background: rgba(239, 68, 68, 0.01); font-size: 0.82rem;">
                                                    Vắng mặt
                                                </a>
                                                
                                                <a href="candidatecall.jsp?action=endShift" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; font-size: 0.82rem; border-color: #cbd5e1; color: #64748b;">
                                                    Kết thúc
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
                            <!-- Calculate pending count (not completed procedures) -->
                            <c:set var="pendingCount" value="0" />
                            <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                <c:set var="isCdone" value="${not empty c.photoUrl and c.paymentCompleted eq 'true'}" />
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
                            <a href="candidatecall.jsp" class="btn-batch btn-batch--alt" style="width: 32px; height: 32px; padding: 0; display: inline-flex; align-items: center; justify-content: center; border-radius: 6px;" title="Làm mới hàng đợi">
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
                                        <c:set var="cDone" value="${not empty candidate.photoUrl and candidate.paymentCompleted eq 'true'}" />
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
                                                <td style="text-align: center;"><span class="role-badge role-badge--coi" style="font-size: 0.65rem; padding: 1px 4px;">${candidate['class']}</span></td>
                                                <td style="text-align: center; font-family: monospace; color: #475569;">${candidate.cccd}</td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 6px;">
                                                        <a href="procedure.jsp?sbd=${candidate.sbd}" class="btn-filter" style="height: 26px; padding: 0 8px; font-size: 0.72rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center;">Lập hồ sơ</a>
                                                        <a href="candidatecall.jsp?action=absent&sbd=${candidate.sbd}" class="btn-reset" style="height: 26px; padding: 0 8px; font-size: 0.72rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; color: #dc2626; border-color: rgba(220, 38, 38, 0.2); background: rgba(220, 38, 38, 0.02);">Vắng mặt</a>
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

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
