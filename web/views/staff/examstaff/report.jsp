<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Ensure selectedSessionId is loaded
    Integer sessIdObj = (Integer) session.getAttribute("selectedSessionId");
    int sessId = (sessIdObj != null) ? sessIdObj : 2; // Default to ca thi B2 sáng (ID = 2)
    
    DAO.ExamSessionDAO sessDAO = new DAO.Impl.ExamSessionDAOImpl();
    Models.ExamSession currentSession = null;
    try {
        currentSession = sessDAO.getById(sessId);
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (currentSession != null) {
        request.setAttribute("currentSession", currentSession);
    }

    DAO.ExamRegistrationDAO regDAO = new DAO.Impl.ExamRegistrationDAOImpl();
    java.util.List<Models.ExamRegistration> qList = null;
    try {
        qList = regDAO.getCandidatesBySession(sessId);
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (qList == null) {
        qList = new java.util.ArrayList<>();
    }
    request.setAttribute("candidateList", qList);

    // Calculate statistics dynamically
    int totalCandidates = qList.size();
    int passedCount = 0;
    int failedCount = 0;
    int absentCount = 0;
    
    // Counts per class — đăng ký vs. đã thi xong
    int a1Count = 0;    // Tổng đăng ký hạng A1
    int a1Completed = 0; // Đã thi xong (có kết quả cuối)
    int a1Passed = 0;
    int a1Failed = 0;
    int b2Count = 0;    // Tổng đăng ký hạng B2
    int b2Completed = 0; // Đã thi xong (có kết quả cuối)
    int b2Passed = 0;
    int b2Failed = 0;
    
    // Counts per exam section
    int theoryCount = 0;
    int theoryPassed = 0;
    int theoryFailed = 0;
    
    int practicalCount = 0;
    int practicalPassed = 0;
    int practicalFailed = 0;

    int roadCount = 0;
    int roadPassed = 0;
    int roadFailed = 0;

    // examCompletedCount = số thí sinh đã có kết quả thi cuối cùng
    int examCompletedCount = 0;

    for (Models.ExamRegistration reg : qList) {
        String licCode = reg.getLicenseCode();
        boolean isA1 = "A1".equalsIgnoreCase(licCode) || "A2".equalsIgnoreCase(licCode);
        boolean isB2 = "B2".equalsIgnoreCase(licCode);
        boolean requiresRoad = reg.isRequiresRoadTest();
        
        if (isA1) a1Count++;
        if (isB2) b2Count++;
        
        // Vắng thi vĩnh viễn
        boolean isAbsent = "Absent".equalsIgnoreCase(reg.getNotes());
        if (isAbsent) {
            absentCount++;
            failedCount++;
            examCompletedCount++;
            if (isA1) { a1Completed++; a1Failed++; }
            if (isB2) { b2Completed++; b2Failed++; }
            continue;
        }
        
        // Tính thống kê phần thi Lý thuyết (chỉ những người đã thi)
        String tPass = reg.getTheoryPassed();
        if ("passed".equalsIgnoreCase(tPass)) {
            theoryCount++;
            theoryPassed++;
        } else if ("failed".equalsIgnoreCase(tPass)) {
            theoryCount++;
            theoryFailed++;
        }
        
        // Tính thống kê phần thi Sa hình (chỉ những người đã thi)
        String pPass = reg.getPracticalPassed();
        if ("passed".equalsIgnoreCase(pPass)) {
            practicalCount++;
            practicalPassed++;
        } else if ("failed".equalsIgnoreCase(pPass)) {
            practicalCount++;
            practicalFailed++;
        }

        // Tính thống kê phần thi Đường trường (chỉ những người đã thi)
        String rPass = reg.getRoadTestPassed();
        if ("passed".equalsIgnoreCase(rPass)) {
            roadCount++;
            roadPassed++;
        } else if ("failed".equalsIgnoreCase(rPass)) {
            roadCount++;
            roadFailed++;
        }

        // Xác định thí sinh đã có kết quả cuối cùng chưa
        boolean hasFinalResult;
        if (requiresRoad) {
            // Hạng ô tô: phải trượt sa hình HOẶC đã có kết quả đường trường
            hasFinalResult = "failed".equalsIgnoreCase(pPass)
                          || "passed".equalsIgnoreCase(rPass)
                          || "failed".equalsIgnoreCase(rPass);
        } else {
            // Hạng xe máy: có kết quả sa hình là xong
            hasFinalResult = "passed".equalsIgnoreCase(pPass)
                          || "failed".equalsIgnoreCase(pPass);
        }

        if (!hasFinalResult) {
            // Chưa thi xong — bỏ qua không tính vào kết quả
            continue;
        }

        // Đã có kết quả cuối — đếm vào examCompletedCount
        examCompletedCount++;
        if (isA1) a1Completed++;
        if (isB2) b2Completed++;

        // Đánh giá kết quả cuối cùng
        boolean finalPass;
        if (requiresRoad) {
            finalPass = "passed".equalsIgnoreCase(tPass)
                     && "passed".equalsIgnoreCase(pPass)
                     && "passed".equalsIgnoreCase(rPass);
        } else {
            finalPass = "passed".equalsIgnoreCase(tPass)
                     && "passed".equalsIgnoreCase(pPass);
        }
        
        if (finalPass) {
            passedCount++;
            if (isA1) a1Passed++;
            if (isB2) b2Passed++;
        } else {
            failedCount++;
            if (isA1) a1Failed++;
            if (isB2) b2Failed++;
        }
    }
    
    // Tỷ lệ đạt = số đạt / số đã thi xong (không phải tổng đăng ký)
    double passRate = examCompletedCount > 0 ? ((double) passedCount / examCompletedCount) * 100.0 : 0.0;
    
    request.setAttribute("totalCandidates", totalCandidates);
    request.setAttribute("examCompletedCount", examCompletedCount);
    request.setAttribute("passedCount", passedCount);
    request.setAttribute("failedCount", failedCount);
    request.setAttribute("absentCount", absentCount);
    request.setAttribute("passRate", passRate);
    
    request.setAttribute("a1Count", a1Count);
    request.setAttribute("a1Completed", a1Completed);
    request.setAttribute("a1Passed", a1Passed);
    request.setAttribute("a1Failed", a1Failed);
    
    request.setAttribute("b2Count", b2Count);
    request.setAttribute("b2Completed", b2Completed);
    request.setAttribute("b2Passed", b2Passed);
    request.setAttribute("b2Failed", b2Failed);
    
    request.setAttribute("theoryCount", theoryCount);
    request.setAttribute("theoryPassed", theoryPassed);
    request.setAttribute("theoryFailed", theoryFailed);
    
    request.setAttribute("practicalCount", practicalCount);
    request.setAttribute("practicalPassed", practicalPassed);
    request.setAttribute("practicalFailed", practicalFailed);

    request.setAttribute("roadCount", roadCount);
    request.setAttribute("roadPassed", roadPassed);
    request.setAttribute("roadFailed", roadFailed);

    // Fetch real infractions from database
    java.util.List<java.util.Map<String, Object>> infractions = new java.util.ArrayList<>();
    try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
         java.sql.PreparedStatement ps = conn.prepareStatement(
             "select top 3 deductionReason, count(*) as countVal " +
             "from ScoreDeduction " +
             "group by deductionReason " +
             "order by countVal desc")) {
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            int totalInfractions = 0;
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("reason", rs.getString("deductionReason"));
                int cnt = rs.getInt("countVal");
                map.put("count", cnt);
                totalInfractions += cnt;
                infractions.add(map);
            }
            // calculate percentage
            for (java.util.Map<String, Object> map : infractions) {
                int cnt = (int) map.get("count");
                double pct = totalInfractions > 0 ? ((double) cnt / totalInfractions) * 100.0 : 0.0;
                map.put("percentage", pct);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    request.setAttribute("infractions", infractions);
%>

<c:set var="rateNum" value="${passRate}" />
<fmt:formatNumber var="rateStr" value="${passRate}" maxFractionDigits="1"/>%
<c:set var="totalEx" value="${totalCandidates}" />
<c:set var="completedEx" value="${examCompletedCount}" />
<c:set var="passEx" value="${passedCount}" />
<c:set var="failEx" value="${failedCount}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo cuối ngày - Ban Sát Hạch</title>
    
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
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Báo cáo cuối ngày</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Báo cáo tổng hợp: <c:out value="${currentSession.sessionName}"/></h1>
                <p class="page-subtitle">Tổng hợp số liệu kết quả thi sát hạch trong ngày thi hôm nay, thống kê tỷ lệ đạt/trượt và lỗi phổ biến.</p>
            </div>
            
            <!-- Quick Actions on Header -->
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <div style="display: flex; align-items: center; gap: 6px; background: #ffffff; padding: 5px 10px; border-radius: 8px; border: 1px solid #e2e8f0;">
                    <span style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Ngày ca thi:</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: #0f172a;">
                        <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />
                    </span>
                </div>
                
                <!-- Premium Export Excel Button -->
                <a href="report.jsp?exportExcel=true" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #10b981; border-color: #10b981; color: #ffffff; box-shadow: 0 4px 10px rgba(16, 185, 129, 0.15);">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M14 2v6h6M8 13h8M8 17h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Xuất file Excel báo cáo ca thi
                </a>
                
                <a href="report.jsp?exportPdf=true" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #ef4444; border-color: rgba(239, 68, 68, 0.2); text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất PDF
                </a>
            </div>
        </header>

        <!-- Dynamic param notifications -->
        <c:if test="${param.exportExcel eq 'true'}">
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 12px; padding: 1rem; display: flex; gap: 10px; align-items: center; margin-bottom: 1.5rem;" class="animated slideInUp">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <div>
                    <h4 style="margin: 0; font-size: 0.9rem; font-weight: 700; color: #065f46;">Đã xuất báo cáo thành công!</h4>
                    <p style="margin: 4px 0 0; font-size: 0.8rem; color: #047857;">Tệp tin Excel chứa danh sách kết quả ca thi **danh_sach_ket_qua_24_05.xlsx** đã được tải xuống máy chủ lưu trữ.</p>
                </div>
            </div>
        </c:if>

        <c:if test="${param.exportPdf eq 'true'}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 12px; padding: 1rem; display: flex; gap: 10px; align-items: center; margin-bottom: 1.5rem;" class="animated slideInUp">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <div>
                    <h4 style="margin: 0; font-size: 0.9rem; font-weight: 700; color: #991b1b;">Xuất báo cáo PDF thành công!</h4>
                    <p style="margin: 4px 0 0; font-size: 0.8rem; color: #b91c1c;">Tệp tin PDF **bao_cao_tong_hop_24_05.pdf** đã được tạo lập thành công và sẵn sàng để lưu trữ biên bản thi.</p>
                </div>
            </div>
        </c:if>

        <!-- KPI Summary Cards -->
        <section class="metrics-row" aria-label="Chỉ số báo cáo ca thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${rateStr}</span>
                    <span class="stat-label">Tỷ lệ đạt ca thi</span>
                    <span class="stat-trend stat-trend--up">${passEx} đạt / ${completedEx} đã thi xong</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${completedEx}</span>
                    <span class="stat-label">Thí sinh đã thi xong</span>
                    <span class="stat-trend stat-trend--up">${totalEx} đăng ký · còn lại chưa thi</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(16, 185, 129, 0.06); color: #10b981;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${passEx}</span>
                    <span class="stat-label">Hồ sơ ĐẠT</span>
                    <span class="stat-trend stat-trend--up" style="color: #10b981;">Đủ điều kiện cấp bằng</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${failEx}</span>
                    <span class="stat-label">Hồ sơ CHƯA ĐẠT</span>
                    <span class="stat-trend stat-trend--down">Yêu cầu đăng ký thi lại</span>
                </div>
            </div>
        </section>

        <!-- Main Report Double-Pane Grid Section -->
        <div class="report-grid">
            
            <!-- LEFT PANE: Tabular statistics for License Classes & Exam Parts -->
            <div class="report-pane">
                <header class="report-pane__header">
                    <h2 class="report-pane__title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M3 9h18M9 21V9" stroke="currentColor" stroke-width="2"/>
                        </svg>
                        Thống kê chi tiết phần thi sát hạch hôm nay
                    </h2>
                </header>
                
                <!-- Bảng 1: Kết quả theo Hạng Bằng -->
                <h3 style="font-size: 0.95rem; font-weight: 700; color: #003d9b; margin-top: 0; margin-bottom: 0.75rem; text-transform: uppercase; letter-spacing: 0.02em;">1. Thống kê theo hạng bằng sát hạch</h3>
                <table class="report-table">
                    <thead>
                        <tr>
                            <th scope="col">Hạng bằng</th>
                            <th scope="col" style="text-align: center;">Đăng ký</th>
                            <th scope="col" style="text-align: center;">Đã thi</th>
                            <th scope="col" style="text-align: center; color: #059669;">Đạt (Đỗ)</th>
                            <th scope="col" style="text-align: center; color: #dc2626;">Chưa đạt</th>
                            <th scope="col" style="text-align: right;">Tỷ lệ Đạt</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="font-weight: 700; color: #0f172a;"><span class="role-badge role-badge--coi">Hạng A1</span></td>
                            <td style="text-align: center; font-weight: 600;">${a1Count}</td>
                            <td style="text-align: center; font-weight: 600;">${a1Completed}</td>
                            <td style="text-align: center; color: #059669; font-weight: 700;">${a1Passed}</td>
                            <td style="text-align: center; color: #dc2626; font-weight: 700;">${a1Failed}</td>
                            <td style="text-align: right; font-weight: 700; color: #0052cc;">
                                <c:choose>
                                    <c:when test="${a1Completed > 0}">
                                        <fmt:formatNumber value="${a1Passed * 100.0 / a1Completed}" maxFractionDigits="1"/>%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td style="font-weight: 700; color: #0f172a;"><span class="role-badge role-badge--admin">Hạng B2</span></td>
                            <td style="text-align: center; font-weight: 600;">${b2Count}</td>
                            <td style="text-align: center; font-weight: 600;">${b2Completed}</td>
                            <td style="text-align: center; color: #059669; font-weight: 700;">${b2Passed}</td>
                            <td style="text-align: center; color: #dc2626; font-weight: 700;">${b2Failed}</td>
                            <td style="text-align: right; font-weight: 700; color: #0052cc;">
                                <c:choose>
                                    <c:when test="${b2Completed > 0}">
                                        <fmt:formatNumber value="${b2Passed * 100.0 / b2Completed}" maxFractionDigits="1"/>%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </tbody>
                </table>
                
                <!-- Bảng 2: Tỷ lệ loại/trượt theo từng phần thi -->
                <h3 style="font-size: 0.95rem; font-weight: 700; color: #003d9b; margin-top: 1.5rem; margin-bottom: 0.75rem; text-transform: uppercase; letter-spacing: 0.02em;">2. Thống kê tỷ lệ loại theo từng phần thi</h3>
                <table class="report-table" style="margin-bottom: 0;">
                    <thead>
                        <tr>
                            <th scope="col">Phần thi sát hạch</th>
                            <th scope="col" style="text-align: center;">Tổng số thi</th>
                            <th scope="col" style="text-align: center; color: #059669;">Đạt điều kiện</th>
                            <th scope="col" style="text-align: center; color: #dc2626;">Bị loại trực tiếp</th>
                            <th scope="col" style="text-align: right;">Tỷ lệ loại</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="font-weight: 600; color: #0f172a;">Lý thuyết sát hạch</td>
                            <td style="text-align: center; font-weight: 600;">${theoryCount}</td>
                            <td style="text-align: center; color: #059669; font-weight: 600;">${theoryPassed}</td>
                            <td style="text-align: center; color: #dc2626; font-weight: 700;">${theoryFailed}</td>
                            <td style="text-align: right; font-weight: 700; color: #ef4444;">
                                <c:choose>
                                    <c:when test="${theoryCount > 0}">
                                        <fmt:formatNumber value="${theoryFailed * 100.0 / theoryCount}" maxFractionDigits="1"/>%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td style="font-weight: 600; color: #0f172a;">Sa hình thực hành</td>
                            <td style="text-align: center; font-weight: 600;">${practicalCount}</td>
                            <td style="text-align: center; color: #059669; font-weight: 600;">${practicalPassed}</td>
                            <td style="text-align: center; color: #dc2626; font-weight: 700;">${practicalFailed}</td>
                            <td style="text-align: right; font-weight: 700; color: #ef4444;">
                                <c:choose>
                                    <c:when test="${practicalCount > 0}">
                                        <fmt:formatNumber value="${practicalFailed * 100.0 / practicalCount}" maxFractionDigits="1"/>%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <c:if test="${roadCount > 0}">
                            <tr>
                                <td style="font-weight: 600; color: #0f172a;">Sát hạch đường trường</td>
                                <td style="text-align: center; font-weight: 600;">${roadCount}</td>
                                <td style="text-align: center; color: #059669; font-weight: 600;">${roadPassed}</td>
                                <td style="text-align: center; color: #dc2626; font-weight: 700;">${roadFailed}</td>
                                <td style="text-align: right; font-weight: 700; color: #ef4444;">
                                    <fmt:formatNumber value="${roadFailed * 100.0 / roadCount}" maxFractionDigits="1"/>%
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <!-- RIGHT PANE: Analytical Charts & Common Infractions -->
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: space-between;">
                
                <!-- Simulated Donut Chart using conic-gradient -->
                <div style="margin-bottom: 2rem;">
                    <header class="report-pane__header" style="margin-bottom: 1rem;">
                        <h2 class="report-pane__title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M21.21 15.89A10 10 0 1 1 8 2.83M22 12A10 10 0 0 0 12 2v10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Phân tích tỷ lệ Đạt / Trượt hôm nay
                        </h2>
                    </header>
                    
                    <div class="chart-donut" style="background: conic-gradient(#10b981 0% ${rateNum}%, #ef4444 ${rateNum}% 100%);">
                        <div class="chart-donut__inner">
                            <span class="chart-donut__value">${rateStr}</span>
                            <span class="chart-donut__label">Đạt sát hạch</span>
                        </div>
                    </div>
                    
                    <!-- Chart Legends -->
                    <div class="chart-legend">
                        <div class="chart-legend__item">
                            <div class="chart-legend__color" style="background-color: #10b981;"></div>
                            <span>Đạt (${passEx} học viên)</span>
                        </div>
                        <div class="chart-legend__item">
                            <div class="chart-legend__color" style="background-color: #ef4444;"></div>
                            <span>Chưa đạt (${failEx} học viên)</span>
                        </div>
                    </div>
                </div>
                
                <!-- Common Infractions List with Progress Bars -->
                <div>
                    <header class="report-pane__header" style="margin-bottom: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1.5rem;">
                        <h2 class="report-pane__title" style="color: #ef4444;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Lỗi vi phạm sa hình phổ biến nhất
                        </h2>
                    </header>
                    
                    <div class="violation-list">
                        <c:forEach var="inf" items="${infractions}" varStatus="status">
                            <div class="violation-item">
                                <div class="violation-meta">
                                    <span class="violation-name">${inf.reason}</span>
                                    <span class="violation-count">${inf.count} lỗi (<fmt:formatNumber value="${inf.percentage}" maxFractionDigits="0"/>%)</span>
                                </div>
                                <div class="violation-progress-wrap">
                                    <div class="violation-progress-fill" style="width: ${inf.percentage}%; background-color: ${status.index eq 0 ? '#ef4444' : (status.index eq 1 ? '#ea580c' : '#f59e0b')};"></div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty infractions}">
                            <div style="font-size: 0.8rem; color: #94a3b8; text-align: center; padding: 1.5rem 0;">
                                Chưa ghi nhận lỗi vi phạm sa hình nào trong ca thi này.
                            </div>
                        </c:if>
                    </div>
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
