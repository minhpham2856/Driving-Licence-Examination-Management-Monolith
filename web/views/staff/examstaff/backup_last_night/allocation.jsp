<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%!
    // Helper to find a free computer code (PC-01 to PC-30)
    public String getFreeComputer(java.util.List<java.util.Map<String, String>> queue) {
        java.util.Set<String> occupied = new java.util.HashSet<>();
        if (queue != null) {
            for (java.util.Map<String, String> c : queue) {
                String pc = c.get("computerCode");
                if (pc != null && !pc.isEmpty() && "none".equals(c.get("theoryPassed"))) {
                    occupied.add(pc);
                }
            }
        }
        for (int i = 1; i <= 30; i++) {
            String pcCode = "PC-" + (i < 10 ? "0" : "") + i;
            if (!occupied.contains(pcCode)) {
                return pcCode;
            }
        }
        return "PC-01";
    }

    // Helper to find a free vehicle code (Xe chíp số 01 to Xe chíp số 15)
    public String getFreeVehicle(java.util.List<java.util.Map<String, String>> queue) {
        java.util.Set<String> occupied = new java.util.HashSet<>();
        if (queue != null) {
            for (java.util.Map<String, String> c : queue) {
                String dev = c.get("deviceCode");
                if (dev != null && !dev.isEmpty() && "none".equals(c.get("practicalPassed"))) {
                    occupied.add(dev);
                }
            }
        }
        for (int i = 1; i <= 15; i++) {
            String devCode = "Xe chíp số " + (i < 10 ? "0" : "") + i;
            if (!occupied.contains(devCode)) {
                return devCode;
            }
        }
        return "Xe chíp số 01";
    }
%>
<%
    // Unify state by initializing the rich candidate list in session if not exists
    java.util.List<java.util.Map<String, String>> candidateQueue = (java.util.List<java.util.Map<String, String>>) session.getAttribute("candidateQueue");
    
    if (candidateQueue == null) {
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
    }
    
    // Process sequential pipeline actions
    String act = request.getParameter("action");
    String sbd = request.getParameter("sbd");
    
    if (act != null && sbd != null) {
        for (java.util.Map<String, String> c : candidateQueue) {
            if (sbd.equals(c.get("sbd"))) {
                if ("checkin".equals(act)) {
                    c.put("isPresent", "true");
                    request.setAttribute("alertMsg", "Điểm danh thí sinh **" + c.get("name") + "** thành công! Học viên đã bước vào phòng chờ.");
                } 
                else if ("callCandidate".equals(act)) {
                    c.put("isCalled", "true");
                    session.setAttribute("callingSbd", c.get("sbd")); // Sync Megaphone + TV
                    request.setAttribute("alertMsg", "Đã phát loa gọi học viên **" + c.get("name") + "** vào Bàn làm hồ sơ!");
                } 
                else if ("simProcedure".equals(act)) {
                    c.put("photoUrl", "assets/imgs/candidates/" + c.get("sbd") + "_captured.png");
                    c.put("paymentCompleted", "true");
                    String autoPC = getFreeComputer(candidateQueue);
                    c.put("computerCode", autoPC);
                    request.setAttribute("alertMsg", "Mô phỏng hoàn thành thủ tục cho học viên **" + c.get("name") + "** thành công. Hệ thống tự gán máy thi **" + autoPC + "**!");
                } 
                else if ("assignTheory".equals(act)) {
                    int randScore = 21 + (int)(Math.random() * 5); // 21 -> 25
                    c.put("theoryScore", randScore + (c.get("class").equals("A1") ? "/25" : "/35"));
                    boolean passed = randScore >= (c.get("class").equals("A1") ? 21 : 32);
                    c.put("theoryPassed", passed ? "true" : "false");
                    
                    String autoVehicle = "";
                    if (passed) {
                        autoVehicle = getFreeVehicle(candidateQueue);
                        c.put("deviceCode", autoVehicle);
                    }
                    request.setAttribute("alertMsg", "Chấm điểm thi Lý thuyết cho học viên **" + c.get("name") + "**. Kết quả: **" + c.get("theoryScore") + "** (" + (passed ? "ĐẠT" : "TRƯỢT") + ")!" + (passed ? " Hệ thống tự cấp **" + autoVehicle + "** thi thực hành sa hình." : ""));
                } 
                else if ("assignPractical".equals(act)) {
                    int randScore = 80 + (int)(Math.random() * 21); // 80 -> 100
                    c.put("practicalScore", randScore + "/100");
                    boolean passed = randScore >= 80;
                    c.put("practicalPassed", passed ? "true" : "false");
                    request.setAttribute("alertMsg", "Chấm điểm Thực hành sa hình cho học viên **" + c.get("name") + "**. Kết quả: **" + c.get("practicalScore") + "** (" + (passed ? "ĐẠT" : "TRƯỢT") + ")!");
                }
                else if ("changePC".equals(act)) {
                    String newPC = request.getParameter("newPC");
                    c.put("computerCode", newPC);
                    request.setAttribute("alertMsg", "Thay đổi thiết bị: Học viên **" + c.get("name") + "** được chuyển sang thi máy **" + newPC + "** thành công!");
                }
                else if ("changeVehicle".equals(act)) {
                    String newVehicle = request.getParameter("newVehicle");
                    c.put("deviceCode", newVehicle);
                    request.setAttribute("alertMsg", "Thay đổi thiết bị: Học viên **" + c.get("name") + "** được chuyển sang xe chíp **" + newVehicle + "** thành công!");
                }
                break;
            }
        }
        session.setAttribute("candidateQueue", candidateQueue);
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
    
    <style>
        .pipeline-container {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 1.25rem;
            margin-top: 1.5rem;
            align-items: start;
        }
        
        .pipeline-column {
            background: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(226, 232, 240, 0.8);
            border-radius: 16px;
            box-shadow: 0 4px 20px -2px rgba(15, 23, 42, 0.04);
            padding: 1.25rem 1rem;
            display: flex;
            flex-direction: column;
            gap: 1rem;
            min-height: 580px;
        }
        
        .pipeline-header {
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 0.75rem;
            position: relative;
        }
        
        .pipeline-step-badge {
            font-size: 0.65rem;
            font-weight: 800;
            padding: 2px 8px;
            border-radius: 99px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            display: inline-block;
            margin-bottom: 6px;
        }
        .pipeline-step-badge--1 { background-color: #f1f5f9; color: #475569; }
        .pipeline-step-badge--2 { background-color: rgba(234, 88, 12, 0.1); color: #ea580c; }
        .pipeline-step-badge--3 { background-color: rgba(37, 99, 235, 0.1); color: #2563eb; }
        .pipeline-step-badge--4 { background-color: rgba(16, 185, 129, 0.1); color: #10b981; }
        
        .pipeline-title {
            font-size: 0.95rem;
            font-weight: 800;
            color: #0f172a;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        
        .area-meta-box {
            font-size: 0.7rem;
            color: #64748b;
            margin-top: 4px;
            background: #f8fafc;
            border-radius: 6px;
            padding: 4px 8px;
            border: 1px solid #e2e8f0;
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
        
        .pipeline-card-list {
            display: flex;
            flex-direction: column;
            gap: 0.88rem;
            overflow-y: auto;
            max-height: 460px;
            padding-right: 4px;
        }
        
        .candidate-pipe-card {
            background: #ffffff;
            border: 1px solid #f1f5f9;
            border-radius: 12px;
            padding: 0.85rem;
            display: flex;
            flex-direction: column;
            gap: 6px;
            transition: all 0.2s ease;
            position: relative;
            box-shadow: 0 2px 6px rgba(15, 23, 42, 0.01);
        }
        .candidate-pipe-card:hover {
            border-color: #cbd5e1;
            box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
            transform: translateY(-1px);
        }
        
        .candidate-pipe-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .candidate-sbd-tag {
            font-family: monospace;
            font-weight: 800;
            color: #0052cc;
            font-size: 0.82rem;
        }
        
        .candidate-pipe-name {
            font-size: 0.88rem;
            font-weight: 700;
            color: #0f172a;
            margin: 0;
        }
        
        .candidate-pipe-details {
            font-size: 0.72rem;
            color: #64748b;
            line-height: 1.4;
        }
        
        .badge-grid-status {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            margin-top: 2px;
        }
        
        .badge-pill-status {
            font-size: 0.65rem;
            font-weight: 700;
            padding: 1px 6px;
            border-radius: 4px;
            border: 1px solid transparent;
        }
        .badge-pill-status--success { background-color: #ecfdf5; color: #047857; border-color: rgba(16, 185, 129, 0.2); }
        .badge-pill-status--warning { background-color: #fffbeb; color: #b45309; border-color: rgba(245, 158, 11, 0.2); }
        
        .btn-pipe-action {
            width: 100%;
            height: 30px;
            border-radius: 6px;
            font-size: 0.72rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            transition: all 0.2s;
            margin-top: 4px;
            border: 1px solid #0052cc;
            background-color: #0052cc;
            color: #ffffff;
            cursor: pointer;
        }
        .btn-pipe-action:hover {
            background-color: #003d9b;
            border-color: #003d9b;
        }
        
        .btn-pipe-action--disabled {
            background-color: #f1f5f9;
            border-color: #e2e8f0;
            color: #94a3b8;
            cursor: not-allowed;
            pointer-events: none;
        }
        
        .btn-pipe-action--secondary {
            background-color: #ffffff;
            color: #475569;
            border-color: #cbd5e1;
        }
        .btn-pipe-action--secondary:hover {
            background-color: #f8fafc;
            color: #0f172a;
            border-color: #94a3b8;
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="phan-bo" />
</jsp:include>

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
                <h1 class="page-title">Trình tự điều phối thí sinh (Sequential Pipeline)</h1>
                <p class="page-subtitle">Quản lý và di chuyển thí sinh theo trình tự nghiệp vụ nghiêm ngặt từ Điểm danh &rarr; Phòng chờ &rarr; Phòng thi lý thuyết &rarr; Sân thi sa hình.</p>
            </div>
        </header>

        <!-- Alert Notification Bar -->
        <c:if test="${not empty requestScope.alertMsg}">
            <div style="background-color: #eff6ff; border: 1px solid #3b82f6; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #3b82f6; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 600; color: #1e3a8a;">
                    ${requestScope.alertMsg}
                </span>
            </div>
        </c:if>

        <!-- Pipeline Grid Layout -->
        <div class="pipeline-container">
            
            <!-- COLUMN 1: CHECK-IN DESK -->
            <div class="pipeline-column">
                <div class="pipeline-header">
                    <span class="pipeline-step-badge pipeline-step-badge--1">Bước 1</span>
                    <h3 class="pipeline-title">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #475569;">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        </svg>
                        Điểm danh & Tiếp nhận
                    </h3>
                    <div class="area-meta-box">
                        <span><strong>Vị trí:</strong> Bàn đón tiếp sảnh A</span>
                        <span><strong>Đối tượng:</strong> Toàn bộ đăng ký ca thi</span>
                    </div>
                </div>
                
                <div class="pipeline-card-list">
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.isPresent eq 'false'}">
                            <div class="candidate-pipe-card">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c['class']}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details">
                                    CCCD: ${c.cccd}<br>
                                    Điện thoại: ${c.phone}
                                </div>
                                <a href="allocation.jsp?action=checkin&sbd=${c.sbd}" class="btn-pipe-action">
                                    Điểm danh có mặt
                                </a>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- If no candidates pending check-in -->
                    <c:set var="checkinCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}"><c:if test="${c.isPresent eq 'false'}"><c:set var="checkinCount" value="${checkinCount + 1}" /></c:if></c:forEach>
                    <c:if test="${checkinCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 3rem 0;">
                            Đã điểm danh toàn bộ danh sách thí sinh.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- COLUMN 2: WAITING ROOM & MEGACALL -->
            <div class="pipeline-column">
                <div class="pipeline-header">
                    <span class="pipeline-step-badge pipeline-step-badge--2">Bước 2</span>
                    <h3 class="pipeline-title" style="color: #ea580c;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <path d="M15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Phòng chờ chính
                    </h3>
                    <div class="area-meta-box" style="background: rgba(234, 88, 12, 0.02); border-color: rgba(234, 88, 12, 0.15);">
                        <span><strong>Khu vực:</strong> Phòng Chờ Số 01 (`ExamArea`)</span>
                        <span><strong>Sức chứa:</strong> 100 người | <strong>Loại:</strong> Room</span>
                    </div>
                </div>
                
                <div class="pipeline-card-list">
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.isPresent eq 'true' and c.isCalled eq 'false'}">
                            <div class="candidate-pipe-card" style="border-left: 3px solid #ea580c;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #ea580c;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c['class']}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details">
                                    Đang đợi ở phòng chờ chính...
                                </div>
                                <a href="allocation.jsp?action=callCandidate&sbd=${c.sbd}" class="btn-pipe-action" style="background: linear-gradient(135deg, #ea580c, #c2410c); border: none;">
                                    Gọi vào bàn thủ tục
                                </a>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- If no candidates waiting in lobby -->
                    <c:set var="waitingCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}"><c:if test="${c.isPresent eq 'true' and c.isCalled eq 'false'}"><c:set var="waitingCount" value="${waitingCount + 1}" /></c:if></c:forEach>
                    <c:if test="${waitingCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 3rem 0;">
                            Không có thí sinh nào trong phòng chờ chính.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- COLUMN 3: THEORY EXAM ROOM -->
            <div class="pipeline-column">
                <div class="pipeline-header">
                    <span class="pipeline-step-badge pipeline-step-badge--3">Bước 3</span>
                    <h3 class="pipeline-title" style="color: #2563eb;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Phòng thi lý thuyết
                    </h3>
                    <div class="area-meta-box" style="background: rgba(37, 99, 235, 0.02); border-color: rgba(37, 99, 235, 0.15);">
                        <span><strong>Khu vực:</strong> Phòng Máy 201 (`ExamArea`)</span>
                        <span><strong>Sức chứa:</strong> 30 máy thi | <strong>Loại:</strong> Room</span>
                    </div>
                </div>
                
                <div class="pipeline-card-list">
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <!-- Display candidates who are called, and not yet passed theory -->
                        <c:if test="${c.isCalled eq 'true' and c.theoryPassed eq 'none'}">
                            <c:set var="hasPhoto" value="${not empty c.photoUrl}" />
                            <c:set var="hasPaid" value="${c.paymentCompleted eq 'true'}" />
                            <c:set var="procedureDone" value="${hasPhoto and hasPaid}" />
                            
                            <div class="candidate-pipe-card" style="border-left: 3px solid #2563eb;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #2563eb;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c['class']}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                
                                <div class="badge-grid-status">
                                    <span class="badge-pill-status ${hasPhoto ? 'badge-pill-status--success' : 'badge-pill-status--warning'}">CCCD & Ảnh</span>
                                    <span class="badge-pill-status ${hasPaid ? 'badge-pill-status--success' : 'badge-pill-status--warning'}">Lệ phí (200k)</span>
                                </div>
                                
                                <c:choose>
                                    <c:when test="${procedureDone}">
                                        <div class="candidate-pipe-details" style="color: #1e3a8a; font-weight: 700; margin-top: 4px; display: flex; align-items: center; gap: 4px;">
                                            <span>Máy thi:</span>
                                            <span style="background-color: #0052cc; color: #ffffff; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 0.75rem; font-weight: 800;">${c.computerCode}</span>
                                        </div>
                                        
                                        <!-- Re-assign PC (Manual Override) -->
                                        <form action="allocation.jsp" method="GET" style="margin: 4px 0 0; display: flex; align-items: center; gap: 4px;">
                                            <input type="hidden" name="action" value="changePC">
                                            <input type="hidden" name="sbd" value="${c.sbd}">
                                            <span style="font-size: 0.68rem; font-weight: 600; color: #64748b;">Đổi máy:</span>
                                            <select name="newPC" onchange="this.form.submit()" style="height: 22px; font-size: 0.68rem; font-weight: 700; border-radius: 4px; border: 1px solid #cbd5e1; padding: 0 2px; color: #475569; background: #ffffff; cursor: pointer; outline: none;">
                                                <c:forEach var="i" begin="1" end="30">
                                                    <c:set var="pcItem" value="PC-${i < 10 ? '0' : ''}${i}" />
                                                    <option value="${pcItem}" ${c.computerCode eq pcItem ? 'selected' : ''}>${pcItem}</option>
                                                </c:forEach>
                                            </select>
                                        </form>
                                        
                                        <a href="allocation.jsp?action=assignTheory&sbd=${c.sbd}" class="btn-pipe-action" style="background: linear-gradient(135deg, #2563eb, #1d4ed8); border: none; margin-top: 8px;">
                                            Chấm điểm Lý thuyết (Auto)
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="candidate-pipe-details" style="color: #b45309; font-weight: 500;">
                                            &bull; Cần hoàn tất 3 bước hồ sơ tại `procedure.jsp`
                                        </div>
                                        <!-- Test simulation helper button -->
                                        <a href="allocation.jsp?action=simProcedure&sbd=${c.sbd}" class="btn-pipe-action btn-pipe-action--secondary">
                                            Mô phỏng Xong hồ sơ
                                        </a>
                                        <span class="btn-pipe-action btn-pipe-action--disabled">
                                            Xếp máy thi lý thuyết
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- If no candidates in theory step -->
                    <c:set var="theoryCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}"><c:if test="${c.isCalled eq 'true' and c.theoryPassed eq 'none'}"><c:set var="theoryCount" value="${theoryCount + 1}" /></c:if></c:forEach>
                    <c:if test="${theoryCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 3rem 0;">
                            Chưa có thí sinh nào đủ hồ sơ chờ thi Lý thuyết.
                        </div>
                    </c:if>
                </div>
            </div>
            
            <!-- COLUMN 4: PRACTICAL TEST GROUND -->
            <div class="pipeline-column">
                <div class="pipeline-header">
                    <span class="pipeline-step-badge pipeline-step-badge--4">Bước 4</span>
                    <h3 class="pipeline-title" style="color: #10b981;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Sân sát hạch thực hành
                    </h3>
                    <div class="area-meta-box" style="background: rgba(16, 185, 129, 0.02); border-color: rgba(16, 185, 129, 0.15);">
                        <span><strong>Khu vực:</strong> Sân Sa Hình Số 1 (`ExamArea`)</span>
                        <span><strong>Sức chứa:</strong> 15 xe chíp | <strong>Loại:</strong> Ground</span>
                    </div>
                </div>
                
                <div class="pipeline-card-list">
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <!-- Display candidates who passed theory and not yet finished practical -->
                        <c:if test="${c.theoryPassed eq 'true' and c.practicalPassed eq 'none'}">
                            <div class="candidate-pipe-card" style="border-left: 3px solid #10b981;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #10b981;">${c.sbd}</span>
                                    <span class="role-badge role-badge--coi" style="font-size: 0.6rem; padding: 1px 4px;">Hạng ${c['class']}</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details">
                                    Thi lý thuyết: <strong style="color: #10b981;">${c.theoryScore} (ĐẠT)</strong> tại máy ${c.computerCode}
                                    <div style="color: #065f46; font-weight: 700; margin-top: 4px; display: flex; align-items: center; gap: 4px;">
                                        <span>Xe chíp:</span>
                                        <span style="background-color: #10b981; color: #ffffff; padding: 2px 6px; border-radius: 4px; font-size: 0.72rem; font-weight: 800;">${c.deviceCode}</span>
                                    </div>
                                </div>
                                
                                <!-- Re-assign Vehicle (Manual Override) -->
                                <form action="allocation.jsp" method="GET" style="margin: 4px 0 0; display: flex; align-items: center; gap: 4px;">
                                    <input type="hidden" name="action" value="changeVehicle">
                                    <input type="hidden" name="sbd" value="${c.sbd}">
                                    <span style="font-size: 0.68rem; font-weight: 600; color: #64748b;">Đổi xe:</span>
                                    <select name="newVehicle" onchange="this.form.submit()" style="height: 22px; font-size: 0.68rem; font-weight: 700; border-radius: 4px; border: 1px solid #cbd5e1; padding: 0 2px; color: #475569; background: #ffffff; cursor: pointer; outline: none;">
                                        <c:forEach var="i" begin="1" end="15">
                                            <c:set var="vehicleItem" value="Xe chíp số ${i < 10 ? '0' : ''}${i}" />
                                            <option value="${vehicleItem}" ${c.deviceCode eq vehicleItem ? 'selected' : ''}>${vehicleItem}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                                
                                <a href="allocation.jsp?action=assignPractical&sbd=${c.sbd}" class="btn-pipe-action" style="background: linear-gradient(135deg, #10b981, #059669); border: none; margin-top: 8px;">
                                    Chấm điểm Sa hình (Auto)
                                </a>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- Display candidates who are fully completed the whole exam session -->
                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                        <c:if test="${c.practicalPassed eq 'true'}">
                            <div class="candidate-pipe-card" style="background: #f0fdf4; border-color: rgba(16, 185, 129, 0.3); border-left: 3px solid #059669;">
                                <div class="candidate-pipe-header">
                                    <span class="candidate-sbd-tag" style="color: #059669;">${c.sbd}</span>
                                    <span style="font-size: 0.6rem; background-color: #d1fae5; color: #065f46; font-weight: 800; padding: 2px 6px; border-radius: 4px;">HOÀN THÀNH</span>
                                </div>
                                <h4 class="candidate-pipe-name">${c.name}</h4>
                                <div class="candidate-pipe-details" style="font-size: 0.7rem; color: #374151;">
                                    &bull; Lý thuyết: <strong style="color: #059669;">${c.theoryScore}</strong> tại máy ${c.computerCode}<br>
                                    &bull; Thực hành: <strong style="color: #059669;">${c.practicalScore}</strong> trên ${c.deviceCode}<br>
                                    &bull; Kết luận: <strong style="color: #16a34a;">ĐỦ ĐIỀU KIỆN CẤP GPLX</strong>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    
                    <!-- If no candidates in practical step -->
                    <c:set var="practicalCount" value="0" />
                    <c:forEach var="c" items="${sessionScope.candidateQueue}"><c:if test="${c.theoryPassed eq 'true'}"><c:set var="practicalCount" value="${practicalCount + 1}" /></c:if></c:forEach>
                    <c:if test="${practicalCount eq 0}">
                        <div style="text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 3rem 0;">
                            Chưa có thí sinh nào thi đạt lý thuyết chờ sát hạch Sa hình.
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

</body>
</html>
