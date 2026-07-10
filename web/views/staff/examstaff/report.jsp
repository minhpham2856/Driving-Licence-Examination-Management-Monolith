<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo cuối ngày - Ban Sát Hạch</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Báo cáo cuối ngày</span>
        </nav>

        <jsp:include page="/views/layout/header-examstaff.jsp">
            <jsp:param name="pageTitle" value="Báo cáo cuối ngày" />
            <jsp:param name="sectionTitle" value="Ban Sát Hạch" />
        </jsp:include>

        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Báo cáo tổng hợp: <c:out value="${currentSession.sessionLabel}"/></h1>
                <p class="page-subtitle">Tổng hợp số liệu kết quả thi sát hạch trong ngày thi, thống kê tỷ lệ đạt/trượt và lỗi phổ biến.</p>
            </div>

            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <div style="display: flex; align-items: center; gap: 6px; background: #ffffff; padding: 5px 10px; border-radius: 8px; border: 1px solid #e2e8f0;">
                    <span style="font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Ngày ca thi:</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: #0f172a;">
                        <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />
                    </span>
                </div>
            </div>
        </header>

        <!-- Headline KPI Row -->
        <section class="metrics-row" aria-label="Số liệu tổng hợp">
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number">${report.totalCandidates}</span>
                    <span class="stat-label">Tổng thí sinh</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #2563eb;">${report.completedCount}</span>
                    <span class="stat-label">Đã thi xong</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #f59e0b;">${report.testingCount}</span>
                    <span class="stat-label">Đang thi</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #94a3b8;">${report.pendingCount}</span>
                    <span class="stat-label">Chưa thi</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${report.passedCount}</span>
                    <span class="stat-label">Đạt</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #ef4444;">${report.failedCount}</span>
                    <span class="stat-label">Trượt</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-number" style="color: #0052cc;">
                        <fmt:formatNumber value="${report.passRate}" maxFractionDigits="1" />%
                    </span>
                    <span class="stat-label">Tỷ lệ đạt</span>
                </div>
            </div>
        </section>

        <!-- Per-licence breakdown -->
        <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem; display: flex; align-items: center; gap: 6px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M3 3v18h18"/><path d="M7 14l4-4 3 3 5-6"/>
                </svg>
                Thống kê theo hạng bằng
            </h3>
            <div style="display: flex; flex-wrap: wrap; gap: 1.5rem;">
                <div style="flex: 1; min-width: 240px;">
                    <span style="font-size: 0.78rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Hạng A1 / A2</span>
                    <div style="margin-top: 0.5rem; display: flex; gap: 1rem;">
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #0f172a;">${report.a1Count}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Đăng ký</span></div>
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #10b981;">${report.a1Passed}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Đạt</span></div>
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #ef4444;">${report.a1Failed}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Trượt</span></div>
                    </div>
                </div>
                <div style="flex: 1; min-width: 240px;">
                    <span style="font-size: 0.78rem; font-weight: 800; color: #64748b; text-transform: uppercase;">Hạng B2</span>
                    <div style="margin-top: 0.5rem; display: flex; gap: 1rem;">
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #0f172a;">${report.b2Count}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Đăng ký</span></div>
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #10b981;">${report.b2Passed}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Đạt</span></div>
                        <div><span style="font-size: 1.3rem; font-weight: 800; color: #ef4444;">${report.b2Failed}</span><span style="display:block; font-size: 0.72rem; color: #64748b;">Trượt</span></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Top infractions -->
        <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <h3 style="font-size: 1rem; font-weight: 800; color: #ea580c; margin: 0 0 1rem; display: flex; align-items: center; gap: 6px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                    <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
                Lỗi phổ biến (top 3)
            </h3>
            <c:choose>
                <c:when test="${not empty report.topInfractions}">
                    <div class="table-responsive">
                        <table class="audit-table" style="font-size: 0.88rem; width: 100%;">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 60px;" class="col-id">#</th>
                                    <th scope="col">Lý do khấu điểm</th>
                                    <th scope="col" style="width: 120px; text-align: center;">Số lần</th>
                                    <th scope="col" style="width: 200px;">Tỷ lệ</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="inf" items="${report.topInfractions}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td><c:out value="${inf.reason}"/></td>
                                        <td style="text-align: center;">${inf.count}</td>
                                        <td>
                                            <div style="background: #f1f5f9; border-radius: 6px; height: 10px; width: 100%;">
                                                <div style="background: #ea580c; height: 10px; border-radius: 6px; width: <fmt:formatNumber value="${inf.percentage}" maxFractionDigits="0" />%;"></div>
                                            </div>
                                            <span style="font-size: 0.72rem; color: #64748b;">
                                                <fmt:formatNumber value="${inf.percentage}" maxFractionDigits="1" />%
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; color: #94a3b8; font-size: 0.85rem; padding: 1rem;">
                        Không có dữ liệu khấu điểm nào được ghi nhận.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Candidate detail table -->
        <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem; display: flex; align-items: center; gap: 6px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                </svg>
                Danh sách thí sinh trong ca
            </h3>
            <div class="table-responsive">
                <table class="audit-table" style="font-size: 0.88rem; width: 100%;">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 110px;">SBD</th>
                            <th scope="col">Họ và tên</th>
                            <th scope="col" style="width: 90px;">Hạng</th>
                            <th scope="col" style="width: 140px;">Trạng thái</th>
                            <th scope="col" style="width: 140px;">Kết quả</th>
                            <th scope="col" style="width: 90px; text-align: center;">Điểm</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty report.candidateRows}">
                                <c:forEach var="row" items="${report.candidateRows}">
                                    <tr>
                                        <td style="font-family: monospace; font-weight: 700; color: #0f172a;">${row.candidateNumber}</td>
                                        <td style="font-weight: 600; color: #0f172a;">${row.fullName}</td>
                                        <td>${row.licenceClass}</td>
                                        <td>
                                            <span class="action-badge action-badge--info" style="font-size: 0.75rem;">
                                                <c:out value="${row.sectionStatus.value}" default="—" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.passed}">
                                                    <span class="action-badge action-badge--pass" style="font-size: 0.75rem;">Đạt</span>
                                                </c:when>
                                                <c:when test="${row.resultLabel != null && row.resultLabel != '-'}">
                                                    <span class="action-badge action-badge--fail" style="font-size: 0.75rem;">Trượt</span>
                                                </c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-weight: 700;">
                                            <c:choose>
                                                <c:when test="${row.examScore != null}">${row.examScore}</c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 2rem 1rem; color: #64748b;">
                                        Chưa có thí sinh nào trong ca sát hạch này.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
