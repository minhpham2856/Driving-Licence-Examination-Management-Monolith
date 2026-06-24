<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${empty requestScope.candidateList}">
    <c:redirect url="/views/staff/examstaff/report"/>
</c:if>

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
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Báo cáo cuối ngày</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">
                    Báo cáo tổng hợp ngày thi<c:if test="${not empty currentSession.examDate}"> <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/></c:if><c:if test="${not empty currentSession.licenseCode}"><span style="font-size: 0.85em; font-weight: 700; color: #475569;"> — Hạng ${currentSession.licenseCode}</span></c:if>
                </h1>
                <p class="page-subtitle">Tổng hợp số liệu kết quả thi sát hạch trong ngày thi hôm nay, thống kê tỷ lệ đạt/trượt và lỗi phổ biến.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <div style="display: flex; align-items: center; gap: 6px; background: #ffffff; padding: 5px 10px; border-radius: 8px; border: 1px solid #e2e8f0;">
                    <span style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Ngày thi:</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: #0f172a;">
                        <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />
                    </span>
                </div>
                
                <a href="${pageContext.request.contextPath}/views/staff/examstaff/report?exportExcel=true"
                   class="btn-filter"
                   style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; ${missingPhotoCount > 0 ? 'background-color: #94a3b8; border-color: #94a3b8; pointer-events: none; opacity: 0.65;' : 'background-color: #10b981; border-color: #10b981; color: #ffffff; box-shadow: 0 4px 10px rgba(16, 185, 129, 0.15);'}"
                   title="${missingPhotoCount > 0 ? 'Còn thí sinh chưa chụp ảnh — không thể xuất hồ sơ' : 'Xuất Excel'}">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M14 2v6h6M8 13h8M8 17h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Xuất Excel
                </a>
                
                <a href="${pageContext.request.contextPath}/views/staff/examstaff/report?exportPdf=true"
                   target="_blank"
                   class="btn-export"
                   style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; border: 1.5px solid; ${missingPhotoCount > 0 ? 'background-color: #f1f5f9; color: #94a3b8; border-color: #e2e8f0; pointer-events: none; opacity: 0.65;' : 'background-color: #ffffff; color: #0052cc; border-color: #0052cc; box-shadow: 0 2px 8px rgba(0, 82, 204, 0.08);'}"
                   title="${missingPhotoCount > 0 ? 'Còn thí sinh chưa chụp ảnh — không thể xuất hồ sơ' : 'Mở bản in để lưu PDF'}">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                        <path d="M6 9V2h12v7" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <rect x="6" y="14" width="12" height="8" rx="1" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    Xuất PDF
                </a>
            </div>
        </header>

        <c:if test="${missingPhotoCount > 0}">
            <div style="background-color: #fffbeb; border: 1px solid #f59e0b; border-radius: 12px; padding: 1rem 1.25rem; margin-bottom: 1.5rem;">
                <h4 style="margin: 0 0 8px; font-size: 0.9rem; font-weight: 700; color: #92400e;">
                    ${missingPhotoCount} thí sinh chưa hoàn thành thủ tục / chưa có ảnh chân dung
                </h4>
                <p style="margin: 0 0 10px; font-size: 0.8rem; color: #b45309;">
                    Không thể xuất Excel/PDF cho đến khi các thí sinh dưới đây làm xong bàn thủ tục (chụp ảnh + thu phí).
                </p>
                <ul style="margin: 0; padding-left: 1.25rem; font-size: 0.85rem; color: #78350f; line-height: 1.6;">
                    <c:forEach var="c" items="${missingPhotoCandidates}">
                        <li>
                            <strong>${c.sbd}</strong> — ${c.name}
                            <span style="color: #a16207;">(Hạng ${c.clazz}<c:if test="${c.paymentCompleted}"> · đã thu phí, thiếu ảnh</c:if><c:if test="${not c.paymentCompleted}"> · chưa xong thủ tục</c:if>)</span>
                            <a href="${pageContext.request.contextPath}/views/staff/examstaff/procedure?sbd=${c.sbd}&amp;step=1#procedure-desk"
                               style="margin-left: 6px; font-weight: 700; color: #0052cc; text-decoration: none;">→ Làm thủ tục</a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.exportBlocked}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 12px; padding: 1rem; display: flex; gap: 10px; align-items: center; margin-bottom: 1.5rem;">
                <div>
                    <h4 style="margin: 0; font-size: 0.9rem; font-weight: 700; color: #991b1b;">Không thể xuất báo cáo</h4>
                    <p style="margin: 4px 0 0; font-size: 0.8rem; color: #b91c1c;">
                        Còn ${missingPhotoCount} thí sinh chưa chụp ảnh. Hoàn tất Bước 2 tại bàn thủ tục trước khi xuất Excel/PDF.
                    </p>
                </div>
            </div>
        </c:if>

        <section class="metrics-row" aria-label="Chỉ số báo cáo ngày thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber" style="background-color: rgba(126, 34, 206, 0.06); color: #7e22ce;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                        <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #7e22ce;">${procedureCompleteCount}</span>
                    <span class="stat-label">Đã xong thủ tục tại bàn</span>
                    <span class="stat-trend stat-trend--up">
                        <c:choose>
                            <c:when test="${procedurePendingCount > 0}">Còn ${procedurePendingCount} chưa làm thủ tục</c:when>
                            <c:otherwise>Tất cả thí sinh (trừ vắng) đã xong</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${rateStr}</span>
                    <span class="stat-label">Tỷ lệ đạt</span>
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

        <div class="report-grid">
            
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
                        <c:forEach var="lic" items="${licenseStats}">
                            <tr>
                                <td style="font-weight: 700; color: #0f172a;">
                                    <span class="role-badge role-badge--coi">Hạng ${lic.code}</span>
                                </td>
                                <td style="text-align: center; font-weight: 600;">${lic.registered}</td>
                                <td style="text-align: center; font-weight: 600;">${lic.completed}</td>
                                <td style="text-align: center; color: #059669; font-weight: 700;">${lic.passed}</td>
                                <td style="text-align: center; color: #dc2626; font-weight: 700;">${lic.failed}</td>
                                <td style="text-align: right; font-weight: 700; color: #0052cc;">
                                    <c:choose>
                                        <c:when test="${lic.completed > 0}">
                                            <fmt:formatNumber value="${lic.passed * 100.0 / lic.completed}" maxFractionDigits="1"/>%
                                        </c:when>
                                        <c:otherwise>0%</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty licenseStats}">
                            <tr>
                                <td colspan="6" style="text-align: center; color: #94a3b8; padding: 1.5rem;">
                                    Chưa có thí sinh đăng ký trong kỳ thi này.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
                
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
                            <td style="font-weight: 600; color: #0f172a;">Thực hành</td>
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
                                <td style="font-weight: 600; color: #0f172a;">Đường trường</td>
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
            
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: space-between;">
                
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
                
                <div>
                    <header class="report-pane__header" style="margin-bottom: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1.5rem;">
                        <h2 class="report-pane__title" style="color: #ef4444;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Lỗi vi phạm thực hành phổ biến nhất
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
                                Chưa ghi nhận lỗi vi phạm thực hành nào trong ngày thi này.
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
