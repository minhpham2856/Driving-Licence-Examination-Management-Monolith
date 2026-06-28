<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${empty requestScope.totalRegistrants}">
    <c:redirect url="/manager/dashboard" />
</c:if>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard Managing Staff - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <span class="breadcrumbs__current">Dashboard Managing Staff</span>
    </nav>
    <header class="page-header">
        <div class="page-title-wrap">
            <h1 class="page-title">Dashboard Quản Lý</h1>
            <p class="page-subtitle">Tổng quan tài khoản Registrant, hồ sơ, phiên thi và thao tác quản lý từ database.</p>
        </div>
        <div class="page-actions" style="display:flex;gap:.75rem">
            <a class="btn-export" href="${ctx}/manager/dossiers" style="display:inline-flex;text-decoration:none">
                Duyệt hồ sơ (${reviewableCount})
            </a>
            <a class="btn-filter" href="${ctx}/manager/create-user" style="display:inline-flex;text-decoration:none">
                Tạo tài khoản &amp; hồ sơ
            </a>
        </div>
    </header>

    <section class="metrics-row">
        <div class="stat-card"><div class="stat-info"><span class="stat-number">${totalRegistrants}</span><span class="stat-label">Tổng thí sinh</span><span class="stat-trend stat-trend--up">Registrant trong DB</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#d97706">${reviewableCount}</span><span class="stat-label">Hồ sơ cần xử lý</span><span class="stat-trend" style="color:#d97706;background:#fffbeb">Chưa Approved</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#059669">${approvedCount}</span><span class="stat-label">Hồ sơ đã duyệt</span><span class="stat-trend stat-trend--up">Được chọn phiên thi</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#dc2626">${lockedCount}</span><span class="stat-label">Tài khoản đã khóa</span><span class="stat-trend" style="color:#dc2626;background:#fef2f2">Cần theo dõi</span></div></div>
    </section>

    <div class="report-grid" style="grid-template-columns:1.6fr 1fr;gap:1.5rem;margin-top:1.5rem">
        <div style="display:flex;flex-direction:column;gap:1.5rem">
            <section class="log-card">
                <header class="log-card-header">
                    <h2 class="log-card-title">Hồ sơ cần Managing Staff xử lý</h2>
                    <a href="${ctx}/manager/dossiers" style="text-decoration:none;color:#0052cc;font-weight:700">Xem tất cả</a>
                </header>
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>Mã</th><th>Thí sinh</th><th>CCCD</th><th>Hạng</th><th>Giấy tờ</th><th>Trạng thái</th><th></th></tr></thead>
                        <tbody>
                            <c:forEach var="item" items="${recentDossiers}">
                                <tr>
                                    <td>#${item.registrationId}</td>
                                    <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                    <td><c:out value="${item.profile.govIdNo}" /></td>
                                    <td><c:out value="${empty item.licenceClass ? '—' : item.licenceClass}" /></td>
                                    <td>${item.documentCount}/4</td>
                                    <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                                    <td><a class="btn-filter" href="${ctx}/manager/dossiers?id=${item.registrationId}" style="display:inline-flex;text-decoration:none">Xử lý</a></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentDossiers}">
                                <tr><td colspan="7" style="padding:2rem;text-align:center">Không có hồ sơ cần xử lý.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="log-card">
                <header class="log-card-header">
                    <h2 class="log-card-title">Phiên thi đang cấu hình trong hệ thống</h2>
                    <span class="action-badge action-badge--${upcomingCount gt 0 ? 'success' : 'warning'}">
                        ${upcomingCount} phiên chưa quá ngày
                    </span>
                </header>
                <c:if test="${upcomingCount eq 0 and not empty activeSessions}">
                    <div class="p-alert-banner" style="margin:1rem;border-color:#f59e0b;color:#92400e">
                        Các phiên đang mang trạng thái hoạt động nhưng ngày thi đã qua. Cần cập nhật lịch trước khi mở đăng ký mới.
                    </div>
                </c:if>
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>Phiên thi</th><th>Hạng</th><th>Ngày giờ</th><th>Khu vực</th><th>Thí sinh</th><th>Trạng thái</th></tr></thead>
                        <tbody>
                            <c:forEach var="exam" items="${activeSessions}">
                                <tr>
                                    <td><strong><c:out value="${exam.sessionName}" /></strong></td>
                                    <td><c:out value="${exam.licenseCode}" /></td>
                                    <td><fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" /> · <fmt:formatDate value="${exam.shiftStartTime}" pattern="HH:mm" /></td>
                                    <td><c:out value="${empty exam.areaName ? 'Chưa phân khu' : exam.areaName}" /></td>
                                    <td>${exam.registeredCount}/${exam.maxCandidates}</td>
                                    <td><span class="action-badge action-badge--info"><c:out value="${exam.status}" /></span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty activeSessions}">
                                <tr><td colspan="6" style="padding:2rem;text-align:center">Chưa có phiên thi hoạt động.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>

        <div style="display:flex;flex-direction:column;gap:1.5rem">
            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">Phân bố theo hạng GPLX</h2>
                <div style="display:flex;flex-direction:column;gap:1rem;margin-top:1.25rem">
                    <c:forEach var="entry" items="${licenceCounts}">
                        <div>
                            <div style="display:flex;justify-content:space-between;margin-bottom:.35rem">
                                <strong>Hạng <c:out value="${entry.key}" /></strong><span>${entry.value} thí sinh</span>
                            </div>
                            <div style="height:8px;background:#e2e8f0;border-radius:999px;overflow:hidden">
                                <div style="height:100%;width:${totalRegistrants gt 0 ? entry.value * 100 / totalRegistrants : 0}%;background:#0052cc;border-radius:999px"></div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid #e2e8f0;color:#64748b">
                    ${completeCount}/${totalRegistrants} thí sinh đã có đủ bốn loại giấy tờ chuẩn.
                </div>
            </section>

            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">Thao tác gần đây của bạn</h2>
                <div style="display:flex;flex-direction:column;gap:.75rem;margin-top:1rem">
                    <c:forEach var="log" items="${recentAudits}">
                        <div style="padding:.75rem;border:1px solid #e2e8f0;border-radius:8px">
                            <strong><c:out value="${log.action}" /></strong>
                            <div style="color:#64748b;font-size:.82rem;margin-top:.2rem">
                                <c:out value="${empty log.newValue ? log.reason : log.newValue}" />
                            </div>
                            <small><fmt:formatDate value="${log.changedAt}" pattern="dd/MM/yyyy HH:mm" /></small>
                        </div>
                    </c:forEach>
                    <c:if test="${empty recentAudits}"><span style="color:#64748b">Chưa có thao tác được ghi nhận.</span></c:if>
                </div>
            </section>

            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">Lối tắt Managing Staff</h2>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;margin-top:1rem">
                    <a class="btn-export" href="${ctx}/manager/registrants" style="padding:1rem;text-decoration:none;text-align:center">Quản lý thí sinh</a>
                    <a class="btn-export" href="${ctx}/manager/dossier-detail" style="padding:1rem;text-decoration:none;text-align:center">Chi tiết hồ sơ</a>
                    <a class="btn-export" href="${ctx}/manager/dossiers" style="padding:1rem;text-decoration:none;text-align:center">Duyệt hồ sơ</a>
                    <a class="btn-export" href="${ctx}/manager/create-user" style="padding:1rem;text-decoration:none;text-align:center">Tiếp nhận hồ sơ</a>
                </div>
            </section>
        </div>
    </div>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>
</body>
</html>
