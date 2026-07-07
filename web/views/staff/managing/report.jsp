<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo thống kê đào tạo - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <jsp:include page="/views/staff/managing/components/staff-managing-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/staff/managing/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${pageContext.request.contextPath}/views/staff/managing/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Báo cáo thống kê đào tạo</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Báo Cáo Thống Kê Đào Tạo</h1>
                <p class="page-subtitle">Thống kê chỉ số đỗ/trượt, tình hình cấp phát GPLX và biểu đồ số lượng hồ sơ học viên theo từng đợt.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xuất báo cáo PDF
                </button>
            </div>
        </header>

        <section class="metrics-row">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty passRateAll ? '0.0%' : passRateAll}%</span>
                    <span class="stat-label">Tỷ lệ đạt chung</span>
                    <span class="stat-trend stat-trend--up">Hệ thống đào tạo</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v8M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalSubmissions ? 0 : totalSubmissions}</span>
                    <span class="stat-label">Tổng hồ sơ nộp</span>
                    <span class="stat-trend stat-trend--up">Học viên tham dự</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--purple">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalPassed ? 0 : totalPassed}</span>
                    <span class="stat-label">Đã cấp chứng chỉ</span>
                    <span class="stat-trend stat-trend--up">Hiệu lực quốc gia</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalRejected ? 0 : totalRejected}</span>
                    <span class="stat-label">Hồ sơ bị từ chối</span>
                    <span class="stat-trend stat-trend--down" style="color: #ef4444; background-color: rgba(239, 68, 68, 0.1);">Vết phê duyệt</span>
                </div>
            </div>
        </section>

        <section class="filter-panel">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Chọn đợt báo cáo & Hạng xe
            </h2>
            <form action="" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2.5fr 2fr 1.5fr;">
                    <div class="input-group">
                        <label for="filterSession" class="input-label">Đợt thi sát hạch</label>
                        <select id="filterSession" name="filterSession" class="input-field">
                            <option value="">Tất cả các đợt thi</option>
                            <option value="k240" ${param.filterSession eq 'k240' ? 'selected' : ''}>Khóa thi A1 - K240 (28/05/2026)</option>
                            <option value="k115" ${param.filterSession eq 'k115' ? 'selected' : ''}>Khóa thi B2 - K115 (15/06/2026)</option>
                            <option value="k30" ${param.filterSession eq 'k30' ? 'selected' : ''}>Khóa thi A2 - K30 (28/06/2026)</option>
                        </select>
                    </div>

                    <div class="input-group">
                        <label for="filterClass" class="input-label">Hạng bằng sát hạch</label>
                        <select id="filterClass" name="filterClass" class="input-field">
                            <option value="">Tất cả hạng bằng</option>
                            <option value="A1" ${param.filterClass eq 'A1' ? 'selected' : ''}>Hạng A1 (Xe máy dưới 175cc)</option>
                            <option value="A2" ${param.filterClass eq 'A2' ? 'selected' : ''}>Hạng A2 (Xe máy trên 175cc)</option>
                            <option value="B1" ${param.filterClass eq 'B1' ? 'selected' : ''}>Hạng B1 (Ô tô tự động)</option>
                            <option value="B2" ${param.filterClass eq 'B2' ? 'selected' : ''}>Hạng B2 (Ô tô số sàn)</option>
                        </select>
                    </div>

                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Lọc dữ liệu
                            </button>
                            <a href="report.jsp" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="1" y="1" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M5 12V9M9 12V6M13 12V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Bảng tổng hợp kết quả sát hạch theo đợt
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col">Tên đợt thi sát hạch</th>
                            <th scope="col" style="width: 110px; text-align: center;">Hạng GPLX</th>
                            <th scope="col" style="width: 140px; text-align: center;">Tổng số học viên</th>
                            <th scope="col" style="width: 140px; text-align: center;">Vắng thi</th>
                            <th scope="col" style="width: 140px; text-align: center;">Số lượng Đạt</th>
                            <th scope="col" style="width: 140px; text-align: center;">Số lượng Trượt</th>
                            <th scope="col" style="width: 150px; text-align: center;">Tỷ lệ Đạt (%)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty reportData}">
                                <c:forEach var="row" items="${reportData}">
                                    <tr>
                                        <td style="font-weight: 700; color: #0052cc;">${row.shiftLabel}</td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin" style="padding: 2px 8px; font-size: 0.75rem;">Hạng ${row.licenseClass}</span>
                                        </td>
                                        <td style="text-align: center; font-weight: 600; color: #0f172a;">${row.totalCount}</td>
                                        <td style="text-align: center; color: #64748b;">${row.absentCount}</td>
                                        <td style="text-align: center; font-weight: 700; color: #10b981;">${row.passCount}</td>
                                        <td style="text-align: center; font-weight: 700; color: #ef4444;">${row.failCount}</td>
                                        <td style="text-align: center;">
                                            <div style="display: flex; flex-direction: column; align-items: center; gap: 4px;">
                                                <span style="font-weight: 800; color: #0f172a;">${row.passRate}%</span>
                                                <div class="progress-bar-container" style="height: 5px; width: 80px; background-color: #f1f5f9; border-radius: 99px; overflow: hidden;">
                                                    <div class="progress-bar-fill" style="width: ${row.passRate}%; background: #10b981; height: 100%;"></div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 4rem 1.5rem; color: #64748b; font-weight: 500;">
                                        Không có dữ liệu báo cáo thống kê kết quả sát hạch nào.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </main>
</div>

</body>
</html>
