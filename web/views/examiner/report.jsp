<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%--
    ========================================================================
    DỮ LIỆU ĐỘNG CHO BÁO CÁO THỐNG KÊ CA THI (SC-050)
    ========================================================================
    Trang này hoàn toàn nhận dữ liệu động từ backend thông qua JSTL.
    Nếu backend chưa cung cấp dữ liệu, hệ thống hiển thị trạng thái trống (Fallback) an toàn.
--%>
<c:set var="rateNum" value="${empty successRateNumeric ? 0 : successRateNumeric}" />
<c:set var="rateStr" value="${empty successRate ? '0%' : successRate}" />
<c:set var="totalEx" value="${empty totalExaminees ? 0 : totalExaminees}" />
<c:set var="passEx" value="${empty passedExaminees ? 0 : passedExaminees}" />
<c:set var="failEx" value="${empty failedExaminees ? 0 : failedExaminees}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo thống kê ca thi - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<%-- Inject the sidebar template --%>
<jsp:include page="/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Báo cáo thống kê</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Báo cáo thống kê</h1>
                <p class="page-subtitle">Phân tích kết quả thi sát hạch, tổng hợp tỷ lệ đạt/trượt và các lỗi vi phạm phổ biến trong ca thi.</p>
            </div>
            
            <!-- Quick Actions on Header -->
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <!-- Chọn ca thi để xem báo cáo (SC-050) -->
                <div style="display: flex; align-items: center; gap: 6px; background: #ffffff; padding: 5px 10px; border-radius: 8px; border: 1px solid #e2e8f0; margin-right: 5px;">
                    <label for="reportSession" style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Đợt báo cáo:</label>
                    <select id="reportSession" class="input-field" style="height: 32px; padding: 2px 8px; font-size: 0.8rem; width: 180px; border-radius: 6px; border-color: #cbd5e1;">
                        <option value="all">Ca Sáng - 24/05/2026</option>
                        <option value="session02">Ca Chiều - 24/05/2026</option>
                        <option value="session03">Đợt thi Tháng 05/2026</option>
                    </select>
                </div>
                
                <a href="${pageContext.request.contextPath}/examiner/report?export=pdf" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #475569; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Xuất PDF
                </a>
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px;" onclick="window.print();">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6v-8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                    In báo cáo
                </button>
            </div>
        </header>

        <!-- Analytical Statistics KPI Summary Cards -->
        <section class="metrics-row" aria-label="Chỉ số báo cáo ca thi">
            <!-- successRate KPI Card -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${rateStr}</span>
                    <span class="stat-label">Tỷ lệ đạt sát hạch</span>
                    <span class="stat-trend stat-trend--up">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        ${empty successRateTrend ? 'Không đổi so với ca trước' : successRateTrend}
                    </span>
                </div>
            </div>
            
            <!-- totalExaminees KPI Card -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${totalEx}</span>
                    <span class="stat-label">Thí sinh đã thi</span>
                    <span class="stat-trend stat-trend--up">
                        100% hoàn thành ca thi
                    </span>
                </div>
            </div>
            
            <!-- passedExaminees KPI Card -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(16, 185, 129, 0.06); color: #10b981;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${passEx}</span>
                    <span class="stat-label">Hồ sơ Đạt (Đỗ)</span>
                    <span class="stat-trend stat-trend--up" style="color: #10b981;">
                        Cấp giấy phép lái xe
                    </span>
                </div>
            </div>
            
            <!-- failedExaminees KPI Card -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${failEx}</span>
                    <span class="stat-label">Hồ sơ Chưa đạt</span>
                    <span class="stat-trend stat-trend--down">
                        Yêu cầu thi lại sát hạch
                    </span>
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
                        Thống kê chi tiết phần thi sát hạch
                    </h2>
                </header>
                
                <!-- Bảng 1: Kết quả theo Hạng Bằng Sát hạch -->
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
                        <c:choose>
                            <c:when test="${not empty licenseClassStats}">
                                <c:forEach var="item" items="${licenseClassStats}">
                                    <tr>
                                        <td style="font-weight: 700; color: #0f172a;">
                                            <c:choose>
                                                <c:when test="${fn:contains(item.licenseClass, 'A1') or fn:contains(item.licenseClass, 'A2')}">
                                                    <span class="role-badge role-badge--coi">${item.licenseClass}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(item.licenseClass, 'B1') or fn:contains(item.licenseClass, 'B2')}">
                                                    <span class="role-badge role-badge--admin">${item.licenseClass}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--cham">${item.licenseClass}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-weight: 600;">${item.registered}</td>
                                        <td style="text-align: center; font-weight: 600;">${item.tested}</td>
                                        <td style="text-align: center; color: #059669; font-weight: 700;">${item.passed}</td>
                                        <td style="text-align: center; color: #dc2626; font-weight: 700;">${item.failed}</td>
                                        <td style="text-align: right; font-weight: 700; color: #0052cc;">${item.passRate}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: #64748b; padding: 2.5rem; font-style: italic;">Không có dữ liệu thống kê hạng bằng.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
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
                        <c:choose>
                            <c:when test="${not empty examPartStats}">
                                <c:forEach var="item" items="${examPartStats}">
                                    <tr>
                                        <td style="font-weight: 600; color: #0f172a;">${item.partName}</td>
                                        <td style="text-align: center; font-weight: 600;">${item.totalTested}</td>
                                        <td style="text-align: center; color: #059669; font-weight: 600;">${item.passed}</td>
                                        <td style="text-align: center; color: #dc2626; font-weight: 700;">${item.disqualified}</td>
                                        <td style="text-align: right; font-weight: 700; color: #ef4444;">${item.disqualifiedRate}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: #64748b; padding: 2.5rem; font-style: italic;">Không có dữ liệu thống kê phần thi.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            
            <!-- RIGHT PANE: Analytical Charts & Common Infractions -->
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: space-between;">
                
                <!-- Khu vực Biểu đồ mô phỏng tròn Conic-Gradient -->
                <div style="margin-bottom: 2rem;">
                    <header class="report-pane__header" style="margin-bottom: 1rem;">
                        <h2 class="report-pane__title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M21.21 15.89A10 10 0 1 1 8 2.83M22 12A10 10 0 0 0 12 2v10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Phân tích tỷ lệ Đạt / Trượt
                        </h2>
                    </header>
                    
                    <!-- Circular simulated chart - RENDERING DYNAMIC CONIC-GRADIENT INLINE STYLE -->
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
                            <span>Đạt (${passEx} HS)</span>
                        </div>
                        <div class="chart-legend__item">
                            <div class="chart-legend__color" style="background-color: #ef4444;"></div>
                            <span>Chưa đạt (${failEx} HS)</span>
                        </div>
                    </div>
                </div>
                
                <!-- Khu vực Thống kê Lỗi vi phạm Sa hình phổ biến (Top Violation Errors) -->
                <div>
                    <header class="report-pane__header" style="margin-bottom: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1.5rem;">
                        <h2 class="report-pane__title" style="color: #ef4444;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Lỗi sa hình phổ biến nhất
                        </h2>
                    </header>
                    
                    <div class="violation-list">
                        <c:choose>
                            <c:when test="${not empty topViolations}">
                                <c:forEach var="item" items="${topViolations}">
                                    <div class="violation-item">
                                        <div class="violation-meta">
                                            <span class="violation-name">${item.name}</span>
                                            <span class="violation-count">${item.countLabel}</span>
                                        </div>
                                        <div class="violation-progress-wrap">
                                            <div class="violation-progress-fill" style="width: ${item.percentage}%;"></div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p style="text-align: center; color: #64748b; padding: 2rem 0; font-size: 0.88rem; font-style: italic;">Không có dữ liệu lỗi vi phạm sa hình.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                
            </div>
            
        </div>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
