<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%
    // Retrieve the candidate queue from the session
    java.util.List<java.util.Map<String, String>> qList = (java.util.List<java.util.Map<String, String>>) session.getAttribute("candidateQueue");
    
    // If not exists (e.g. they visited procedure.jsp directly), initialize it
    if (qList == null) {
        qList = new java.util.ArrayList<>();
        
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
        qList.add(c1);
        
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
        qList.add(c2);
        
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
        qList.add(c3);
        
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
        qList.add(c4);
        
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
        qList.add(c5);
        
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
        qList.add(c6);
        
        session.setAttribute("candidateQueue", qList);
    }
    
    // Find active candidate
    String sbdParam = request.getParameter("sbd");
    if (sbdParam == null || sbdParam.trim().isEmpty()) {
        sbdParam = (String) session.getAttribute("callingSbd");
    }
    java.util.Map<String, String> profile = null;
    if (sbdParam != null && !sbdParam.trim().isEmpty()) {
        for (java.util.Map<String, String> c : qList) {
            if (sbdParam.equals(c.get("sbd"))) {
                profile = c;
                break;
            }
        }
    }
    
    // Process actions
    String pAction = request.getParameter("action");
    if ("saveProfile".equals(pAction) && profile != null) {
        profile.put("name", request.getParameter("fullName"));
        profile.put("dob", request.getParameter("dateOfBirth"));
        profile.put("cccd", request.getParameter("govIdNo"));
        profile.put("email", request.getParameter("email"));
        profile.put("phone", request.getParameter("phoneNo"));
        request.setAttribute("profileUpdatedAlert", "true");
        
        // Save audit log to session audit list
        java.util.List<java.util.Map<String, String>> sessionAuditLogs = (java.util.List<java.util.Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new java.util.ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        java.util.Map<String, String> audit = new java.util.HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", "UPDATE on Person");
        audit.put("details", "Sửa đổi lý lịch SBD " + sbdParam);
        sessionAuditLogs.add(0, audit);
    }
    
    // Process photo captured
    String photoCapturedParam = request.getParameter("photoCaptured");
    if ("true".equals(photoCapturedParam) && profile != null) {
        profile.put("photoUrl", "assets/imgs/candidates/" + sbdParam + "_captured.png");
        
        java.util.List<java.util.Map<String, String>> sessionAuditLogs = (java.util.List<java.util.Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new java.util.ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        java.util.Map<String, String> audit = new java.util.HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", "UPDATE on Person");
        audit.put("details", "Chụp ảnh FaceID thành công SBD " + sbdParam);
        sessionAuditLogs.add(0, audit);
    }
    
    // Process payment
    String paymentSuccessParam = request.getParameter("paymentSuccess");
    if ("true".equals(paymentSuccessParam) && profile != null) {
        profile.put("paymentCompleted", "true");
        
        java.util.List<java.util.Map<String, String>> sessionAuditLogs = (java.util.List<java.util.Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new java.util.ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        java.util.Map<String, String> audit = new java.util.HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", "INSERT on Payment");
        audit.put("details", "Thu lệ phí thi 200,000 đ SBD " + sbdParam);
        sessionAuditLogs.add(0, audit);
    }
    
    if (profile != null) {
        request.setAttribute("profile", profile);
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bàn Làm Thủ Tục - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        .procedure-steps-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background-color: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 1rem;
            margin-bottom: 1.5rem;
        }
        
        .procedure-step-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.88rem;
            font-weight: 600;
            color: #94a3b8;
        }
        
        .procedure-step-item--active {
            color: #0052cc;
        }
        .procedure-step-item--done {
            color: #10b981;
        }
        
        .step-number-badge {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background-color: #e2e8f0;
            color: #475569;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
            font-weight: 800;
        }
        .procedure-step-item--active .step-number-badge {
            background-color: #0052cc;
            color: #ffffff;
        }
        .procedure-step-item--done .step-number-badge {
            background-color: #10b981;
            color: #ffffff;
        }
        
        .camera-live-frame {
            border: 2px solid #cbd5e1;
            border-radius: 16px;
            aspect-ratio: 4/3;
            background-color: #0f172a;
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #ffffff;
        }
        .camera-live-frame--active {
            border-color: #3b82f6;
        }
        .camera-live-frame--captured {
            border-color: #10b981;
        }
        
        .camera-live-reticle {
            width: 180px;
            height: 240px;
            border: 2px dashed rgba(255, 255, 255, 0.4);
            border-radius: 50%;
            position: absolute;
            z-index: 2;
        }
        .camera-live-frame--captured .camera-live-reticle {
            border-color: #10b981;
            border-style: solid;
        }
        
        .scanline-effect {
            width: 100%;
            height: 2px;
            background-color: rgba(59, 130, 246, 0.5);
            position: absolute;
            top: 0;
            animation: scanAnimation 2.5s infinite linear;
            z-index: 3;
        }
        @keyframes scanAnimation {
            0% { top: 0%; }
            100% { top: 100%; }
        }
        
        .photo-avatar-placeholder {
            width: 120px;
            height: 120px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #1d4ed8);
            color: #ffffff;
            font-size: 3rem;
            font-weight: 800;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        
        .qr-card {
            background-color: #ffffff;
            border: 1px solid #cbd5e1;
            border-radius: 12px;
            padding: 1rem;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 8px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.02);
            width: 180px;
            margin: 0 auto;
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="lam-thu-tuc" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Bàn làm thủ tục</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Bàn tiếp đón làm thủ tục</h1>
                <p class="page-subtitle">Quy trình 3 bước nghiệp vụ khép kín: Xác minh thông tin &rarr; Chụp ảnh chân dung &rarr; Xác nhận đóng lệ phí.</p>
            </div>
            
            <div class="page-actions">
                <a href="candidatecall.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #0052cc; border-color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M3 21v-2a7 7 0 0 1 14 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Quay lại điều hành loa gọi thi
                </a>
            </div>
        </header>

        <!-- Active Candidate Status Bar (Replaces Redundant Giant Selection Panel) -->
        <c:if test="${not empty requestScope.profile}">
            <div style="background-color: rgba(0, 82, 204, 0.05); border: 1px solid rgba(0, 82, 204, 0.15); border-radius: 12px; padding: 10px 16px; margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center; backdrop-filter: blur(10px);">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="background-color: #0052cc; color: #ffffff; font-family: monospace; font-weight: 800; font-size: 0.78rem; padding: 2px 8px; border-radius: 6px;">SBD: ${profile.sbd}</span>
                    <span style="font-size: 0.88rem; font-weight: 700; color: #1e293b;">Đang lập hồ sơ cho: <strong style="color: #0f172a;">${profile.name}</strong> (Hạng ${profile['class']})</span>
                </div>
                
                <form action="procedure.jsp" method="GET" style="display: flex; align-items: center; gap: 6px; margin: 0;">
                    <span style="font-size: 0.72rem; font-weight: 600; color: #64748b;">Chuyển học viên:</span>
                    <select name="sbd" onchange="this.form.submit()" style="height: 30px; font-size: 0.78rem; font-weight: 600; border-radius: 6px; border: 1px solid #cbd5e1; padding: 0 4px; color: #475569; background: #ffffff;">
                        <option value="">-- Chọn --</option>
                        <c:forEach var="c" items="${sessionScope.candidateQueue}">
                            <c:if test="${c.isCalled eq 'true'}">
                                <option value="${c.sbd}" ${profile.sbd eq c.sbd ? 'selected' : ''}>${c.sbd} - ${c.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </form>
            </div>
        </c:if>

        <!-- Check if SBD is loaded -->
        <c:choose>
            <c:when test="${not empty requestScope.profile}">
                
                <c:set var="currentSbd" value="${profile.sbd}" />
                <c:set var="cName" value="${profile.name}" />
                <c:set var="cDob" value="${profile.dob}" />
                <c:set var="cCccd" value="${profile.cccd}" />
                <c:set var="cClass" value="${profile['class']}" />
                
                <c:set var="currentStep" value="${param.step}" />
                <c:if test="${empty currentStep}">
                    <c:set var="currentStep" value="1" />
                </c:if>

                <!-- Step progress indicator -->
                <div class="procedure-steps-bar">
                    <div class="procedure-step-item ${currentStep eq '1' ? 'procedure-step-item--active' : (currentStep > 1 ? 'procedure-step-item--done' : '')}">
                        <div class="step-number-badge">1</div>
                        <span>Xác minh & Sửa lỗi</span>
                    </div>
                    
                    <div style="flex: 1; height: 1px; background-color: #e2e8f0; margin: 0 1rem;"></div>
                    
                    <div class="procedure-step-item ${currentStep eq '2' ? 'procedure-step-item--active' : (currentStep > 2 ? 'procedure-step-item--done' : '')}">
                        <div class="step-number-badge">2</div>
                        <span>Chụp ảnh chân dung</span>
                    </div>
                    
                    <div style="flex: 1; height: 1px; background-color: #e2e8f0; margin: 0 1rem;"></div>
                    
                    <div class="procedure-step-item ${currentStep eq '3' ? 'procedure-step-item--active' : (currentStep > 3 ? 'procedure-step-item--done' : '')}">
                        <div class="step-number-badge">3</div>
                        <span>Lệ phí & QR chuyển khoản</span>
                    </div>
                </div>

                <!-- Step Content Area -->
                <div class="report-grid" style="grid-template-columns: 1.5fr 1fr; gap: 1.5rem; margin-bottom: 2.5rem;">
                    
                    <!-- Left Column: Step Content Panels -->
                    <div class="report-pane">
                        
                        <!-- STEP 1: Verify & Edit Info -->
                        <c:if test="${currentStep eq '1'}">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 1: Tra cứu, đối chiếu và sửa đổi hồ sơ học viên</h3>
                            </div>
                            
                            <!-- Profile Updated Alert -->
                            <c:if test="${requestScope.profileUpdatedAlert eq 'true'}">
                                <div style="background-color: #fffbeb; border: 1px solid #f59e0b; border-radius: 8px; padding: 10px; margin-bottom: 1rem; font-size: 0.8rem; color: #b45309; display: flex; gap: 8px; align-items: center;">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #f59e0b; flex-shrink: 0;">
                                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    </svg>
                                    <span>
                                        <strong>Đã ghi nhận thay đổi kiểm toán:</strong> Sửa đổi thông tin nhân thân của học viên thành công! Dữ liệu đã được cập nhật vào phiên làm việc và ghi nhận lịch sử thay đổi.
                                    </span>
                                </div>
                            </c:if>

                            <form action="procedure.jsp" method="GET" id="procedureForm" style="display: flex; flex-direction: column; gap: 1.25rem;">
                                <input type="hidden" name="sbd" value="${currentSbd}">
                                <input type="hidden" name="step" value="2">
                                <input type="hidden" name="action" id="formAction" value="">
                                
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Họ và tên thí sinh:</label>
                                        <input type="text" name="fullName" class="input-field" value="${cName}" style="font-weight: 700;">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số báo danh (SBD):</label>
                                        <input type="text" class="input-field" value="${currentSbd}" readonly style="background-color: #f1f5f9; font-weight: 800; color: #0052cc; font-family: monospace;">
                                    </div>
                                </div>
                                
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Ngày tháng năm sinh:</label>
                                        <input type="text" name="dateOfBirth" class="input-field" value="${cDob}">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số định danh CCCD:</label>
                                        <input type="text" name="govIdNo" class="input-field" value="${cCccd}" style="font-family: monospace;">
                                    </div>
                                </div>

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Email:</label>
                                        <input type="text" name="email" class="input-field" value="${profile.email}">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số điện thoại:</label>
                                        <input type="text" name="phoneNo" class="input-field" value="${profile.phone}">
                                    </div>
                                </div>
                                
                                <div class="input-group">
                                    <label class="input-label">Hạng bằng sát hạch:</label>
                                    <input type="text" class="input-field" value="Hạng ${cClass}" readonly style="background-color: #f1f5f9; font-weight: 700; color: #334155;">
                                </div>
                                
                                <button type="submit" id="submitBtn" class="btn-filter" style="height: 42px; border-radius: 8px; justify-content: center; font-weight: 700; margin-top: 1rem; transition: all 0.3s; background: linear-gradient(135deg, #0052cc, #003d9b); border-color: #003d9b;">
                                    Xác nhận & Sang Bước 2 (Chụp ảnh) &rarr;
                                </button>
                            </form>
                        </c:if>
                        
                        <!-- STEP 2: Live Camera Capture -->
                        <c:if test="${currentStep eq '2'}">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 2: Máy quét Camera live chụp ảnh tại bàn thủ tục</h3>
                            </div>
                            
                            <c:choose>
                                <c:when test="${param.photoCaptured eq 'true' or not empty profile.photoUrl}">
                                    <!-- Photo captured preview -->
                                    <div class="camera-live-frame camera-live-frame--captured">
                                        <div class="camera-live-reticle"></div>
                                        <div class="photo-avatar-placeholder">${fn:substring(cName, 0, 1)}${fn:substring(cName, 1, 2)}</div>
                                        
                                        <div style="position: absolute; bottom: 12px; background: rgba(16, 185, 129, 0.9); color: #ffffff; padding: 4px 10px; border-radius: 6px; font-size: 0.72rem; font-weight: bold; z-index: 2;">
                                            ẢNH CHỤP ĐÃ XÁC THỰC THÀNH CÔNG (LƯU DB)
                                        </div>
                                    </div>
                                    
                                    <div style="display: flex; gap: 10px; margin-top: 1.25rem;">
                                        <a href="procedure.jsp?sbd=${currentSbd}&step=2" class="btn-reset" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1;">Chụp lại ảnh</a>
                                        <a href="procedure.jsp?sbd=${currentSbd}&step=3" class="btn-filter" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #10b981; border-color: #10b981;">Xác nhận & Chuyển sang Bước 3</a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <!-- Camera streaming simulation -->
                                    <div class="camera-live-frame camera-live-frame--active">
                                        <div class="scanline-effect"></div>
                                        <div class="camera-live-reticle"></div>
                                        
                                        <span style="font-size: 0.8rem; font-family: monospace; color: rgba(255, 255, 255, 0.6); position: absolute; top: 12px; left: 12px;">REC LIVE [FPS: 30]</span>
                                        
                                        <span style="z-index: 1; font-weight: 700; font-size: 0.85rem; color: rgba(255, 255, 255, 0.8); text-transform: uppercase;">Căn chỉnh mặt vào tiêu cự chính</span>
                                        
                                        <a href="procedure.jsp?sbd=${currentSbd}&step=2&photoCaptured=true" class="btn-filter" style="position: absolute; bottom: 15px; height: 38px; border-radius: 6px; padding: 0 1.25rem; font-size: 0.82rem; text-decoration: none; z-index: 4; display: inline-flex; align-items: center; gap: 6px;">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                                            </svg>
                                            Bấm chụp ảnh chân dung học viên
                                        </a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        
                        <!-- STEP 3: Lệ phí & Thanh toán QR -->
                        <c:if test="${currentStep eq '3'}">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 3: Lệ phí sát hạch & Thanh toán QR Code ngân hàng</h3>
                            </div>
                            
                            <c:choose>
                                <c:when test="${param.paymentSuccess eq 'true' or profile.paymentCompleted eq 'true'}">
                                    <div style="text-align: center; padding: 2.5rem 1rem; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                        
                                        <h4 style="margin: 0; font-size: 1.15rem; font-weight: 800; color: #047857;">Xác nhận thu lệ phí hoàn tất!</h4>
                                        <p style="margin: 0; font-size: 0.88rem; color: #475569; max-width: 380px;">Đã nhận **200,000 đ** bằng Tiền Mặt thành công. Hóa đơn đã tạo trong hệ thống và cập nhật trạng thái thi sát hạch.</p>
                                        
                                        <a href="procedure.jsp" class="btn-filter" style="height: 40px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; margin-top: 1rem; padding: 0 1.5rem;">
                                            Đón tiếp học viên tiếp theo &rarr;
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <!-- Display Fee Breakdown and QR Code -->
                                    <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 1rem; align-items: start;">
                                        
                                        <div>
                                            <table class="report-table" style="font-size: 0.85rem; width: 100%;">
                                                <thead>
                                                    <tr>
                                                        <th scope="col">Khoản lệ phí thi</th>
                                                        <th scope="col" style="text-align: right; width: 100px;">Thành tiền</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td>Lệ phí sát hạch lý thuyết</td>
                                                        <td style="text-align: right; font-weight: 600;">80,000 đ</td>
                                                    </tr>
                                                    <tr>
                                                        <td>Lệ phí sát hạch mô phỏng</td>
                                                        <td style="text-align: right; font-weight: 600;">100,000 đ</td>
                                                    </tr>
                                                    <tr>
                                                        <td>Lệ phí cấp phôi bằng nhựa PET</td>
                                                        <td style="text-align: right; font-weight: 600;">20,000 đ</td>
                                                    </tr>
                                                    <tr style="border-top: 2px solid #cbd5e1; background-color: #f8fafc;">
                                                        <td style="font-weight: 800; color: #0f172a;">TỔNG CỘNG LỆ PHÍ:</td>
                                                        <td style="text-align: right; font-weight: 800; color: #0052cc; font-size: 0.95rem;">200,000 đ</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                            
                                            <div style="display: flex; gap: 10px; margin-top: 1.5rem;">
                                                <a href="procedure.jsp?sbd=${currentSbd}&step=3&paymentSuccess=true" class="btn-filter" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #10b981; border-color: #10b981;">
                                                    Đóng Tiền Mặt
                                                </a>
                                            </div>
                                        </div>
                                        
                                        <!-- QR Code scanner mock card -->
                                        <div class="qr-card">
                                            <div style="border: 1px solid #cbd5e1; border-radius: 8px; padding: 6px; background-color: #ffffff; display: flex; align-items: center; justify-content: center; width: 110px; height: 110px;">
                                                <!-- Mock QR Code visually -->
                                                <div style="width: 100%; height: 100%; display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; border: 2px solid #000000; padding: 4px; box-sizing: border-box; background-color: #ffffff;">
                                                    <div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div>
                                                    <div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div><div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div>
                                                    <div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div><div style="background-color: #000000;"></div>
                                                    <div style="background-color: #000000;"></div><div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div>
                                                </div>
                                            </div>
                                            
                                            <span style="font-size: 0.7rem; font-weight: 800; color: #475569; text-transform: uppercase;">VIETQR Chuyển Khoản</span>
                                            <span style="font-size: 0.65rem; color: #64748b; text-align: center;">Tự động xác nhận khi nhận tiền</span>
                                        </div>
                                        
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        
                    </div>
                    
                    <!-- Right Column: Brief profile summary card -->
                    <div class="report-pane" style="height: fit-content;">
                        <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.5rem; margin-bottom: 0.75rem;">
                            <h3 style="font-size: 0.95rem; font-weight: 700; color: #0f172a; margin: 0;">Sơ đồ tóm tắt học viên</h3>
                        </div>
                        
                        <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 1rem 0;">
                            <div class="photo-avatar-placeholder" style="width: 80px; height: 80px; font-size: 2rem;">
                                ${fn:substring(cName, 0, 1)}
                            </div>
                            
                            <h4 style="margin: 0; font-size: 1rem; font-weight: 800; color: #0f172a;">${cName}</h4>
                            <span style="font-family: monospace; font-weight: 800; color: #0052cc; font-size: 0.9rem;">SBD: ${currentSbd}</span>
                            
                            <div style="width: 100%; border-top: 1px solid #f1f5f9; margin-top: 8px; padding-top: 8px; display: flex; flex-direction: column; gap: 6px; font-size: 0.8rem;">
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">Hạng sát hạch:</span>
                                    <span style="font-weight: 700; color: #0f172a;">Hạng ${cClass}</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">CCCD:</span>
                                    <span style="font-weight: 600; color: #0f172a; font-family: monospace;">${cCccd}</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">Ngày sinh:</span>
                                    <span style="font-weight: 600; color: #0f172a;">${cDob}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                </div>
            </c:when>
            <c:otherwise>
                <!-- Empty desk state -->
                <div class="report-pane" style="text-align: center; padding: 4rem 1rem; color: #64748b;">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1rem; display: block; opacity: 0.35; color: #64748b;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span style="font-weight: 700; font-size: 1rem; color: #334155; display: block; margin-bottom: 0.5rem;">Bàn làm thủ tục trống</span>
                    Chưa có học viên nào được chọn làm thủ tục. 
                    <p style="font-size: 0.82rem; color: #94a3b8; max-width: 380px; margin: 0.5rem auto 1.5rem;">Vui lòng chọn học viên bên dưới hoặc đợi loa tự động gọi để bắt đầu quy trình làm thủ tục 3 bước khép kín.</p>

                    <!-- Beautiful interactive dropdown inside the empty state to select candidate -->
                    <div style="max-width: 520px; margin: 1.5rem auto 0; padding: 1.5rem; background: rgba(255, 255, 255, 0.9); border: 1.5px solid #e2e8f0; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05); backdrop-filter: blur(8px);">
                        <form action="procedure.jsp" method="GET" style="display: flex; flex-direction: column; gap: 12px; text-align: left; margin: 0;">
                            <label for="emptySbdInput" style="font-size: 0.85rem; font-weight: 700; color: #334155; display: flex; align-items: center; gap: 8px;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #0052cc;">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="9" cy="7" r="4"></circle>
                                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                </svg>
                                CHỌN THÍ SINH ĐÃ ĐƯỢC GỌI VÀO PHÒNG LÀM THỦ TỤC:
                            </label>
                            <div style="position: relative; display: flex; width: 100%;">
                                <select id="emptySbdInput" name="sbd" style="width: 100%; height: 46px; padding: 0 1rem; font-size: 0.9rem; font-weight: 600; border-radius: 10px; border: 1.5px solid #cbd5e1; outline: none; transition: all 0.2s; background: #ffffff; color: #1e293b; appearance: none; -webkit-appearance: none; cursor: pointer;" onchange="this.form.submit()">
                                    <option value="">-- Click để chọn học viên đã được gọi --</option>
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.isCalled eq 'true'}">
                                            <c:set var="isDone" value="${not empty c.photoUrl and c.paymentCompleted eq 'true'}" />
                                            <option value="${c.sbd}">
                                                ${c.sbd} - ${c.name} (Hạng ${c['class']})${isDone ? ' [ĐÃ XONG]' : ''}
                                            </option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                                <div style="position: absolute; right: 15px; top: 50%; transform: translateY(-50%); pointer-events: none; color: #0052cc; display: flex; align-items: center;">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<!-- Dynamic button text transformation micro-script -->
<script>
    document.addEventListener("DOMContentLoaded", function() {
        const form = document.querySelector("#procedureForm");
        if (!form) return;
        
        const btn = document.querySelector("#submitBtn");
        const initialValues = {};
        // Select only editable inputs that have a name attribute (ignoring readonly ones)
        const inputs = form.querySelectorAll("input[name]:not([type=hidden]):not([readonly])");
        
        inputs.forEach(input => {
            initialValues[input.name] = input.value;
            input.addEventListener("input", checkChanges);
            input.addEventListener("change", checkChanges);
        });
        
        function checkChanges() {
            let changed = false;
            inputs.forEach(input => {
                if (initialValues[input.name] !== input.value) {
                    changed = true;
                }
            });
            
            if (changed) {
                document.querySelector("#formAction").value = "saveProfile";
                btn.innerHTML = 'Lưu thay đổi & Sang Bước 2 (Chụp ảnh) &rarr;';
                btn.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
                btn.style.borderColor = '#d97706';
                btn.style.boxShadow = '0 4px 14px rgba(245, 158, 11, 0.2)';
            } else {
                document.querySelector("#formAction").value = "";
                btn.innerHTML = 'Xác nhận & Sang Bước 2 (Chụp ảnh) &rarr;';
                btn.style.background = 'linear-gradient(135deg, #0052cc, #003d9b)';
                btn.style.borderColor = '#003d9b';
                btn.style.boxShadow = 'none';
            }
        }
    });
</script>

</body>
</html>
