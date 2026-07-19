<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${empty requestScope.reviewableCount}"><c:redirect url="/manager/dashboard" /></c:if>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Dashboard Managing Staff - Lái Vui</title><link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css"></head>
<body class="has-side-nav-bar"><jsp:include page="/views/layout/sidebar-managingstaff.jsp"><jsp:param name="activeSidebar" value="dashboard" /></jsp:include>
<div class="dashboard-shell"><main class="main-content">
<nav class="breadcrumbs"><span class="breadcrumbs__current">Dashboard Managing Staff</span></nav>
<header class="page-header"><div class="page-title-wrap"><h1 class="page-title">Dashboard Quản Lý</h1></div><div class="page-actions" style="display:flex;gap:.65rem"><a class="btn-export" href="${ctx}/manager/exam-schedules" style="text-decoration:none">Tạo phiên thi</a><a class="btn-filter" href="${ctx}/manager/exam-schedules/create" style="text-decoration:none">Import danh sách</a></div></header>
<section style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1rem;margin-top:1.25rem">
<a href="${ctx}/manager/dossiers" style="text-decoration:none" class="report-pane"><strong style="font-size:1.5rem;color:#d97706">${reviewableCount}</strong><div style="margin-top:.3rem;color:#475569;font-weight:700">Hồ sơ chờ duyệt</div></a>
<a href="${ctx}/manager/exam-schedules?tab=upcoming" style="text-decoration:none" class="report-pane"><strong style="font-size:1.5rem;color:#0052cc">${upcomingCount}</strong><div style="margin-top:.3rem;color:#475569;font-weight:700">Phiên thi sắp tới</div></a>
<a href="${ctx}/manager/exam-schedules?tab=upcoming" style="text-decoration:none" class="report-pane"><strong style="font-size:1.5rem;color:#dc2626">${emptyRosterCount}</strong><div style="margin-top:.3rem;color:#475569;font-weight:700">Phiên chưa có danh sách</div></a>
</section>
<section class="log-card" style="margin-top:1.25rem"><header class="log-card-header"><h2 class="log-card-title">Hồ sơ chờ duyệt</h2><a href="${ctx}/manager/dossiers" style="text-decoration:none;color:#0052cc;font-weight:700">Xem tất cả (${reviewableCount})</a></header>
<div class="table-responsive"><table class="audit-table"><thead><tr><th>Mã</th><th>Thí sinh</th><th>CCCD</th><th>Hạng</th><th>Giấy tờ</th><th>Trạng thái</th><th></th></tr></thead><tbody>
<c:forEach var="item" items="${recentDossiers}"><tr><td>#${item.registrationId}</td><td><strong><c:out value="${item.profile.fullName}" /></strong></td><td><c:out value="${item.profile.govIdNo}" /></td><td><c:out value="${empty item.licenceDisplayClass ? '—' : item.licenceDisplayClass}" /></td><td>${item.documentCount}/${item.requiredDocumentTotal}</td><td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td><td><a class="btn-filter" href="${ctx}/manager/dossiers?id=${item.registrationId}" style="display:inline-flex;text-decoration:none">Xử lý</a></td></tr></c:forEach>
<c:if test="${empty recentDossiers}"><tr><td colspan="7" style="padding:2rem;text-align:center;color:#64748b">Không có hồ sơ chờ duyệt.</td></tr></c:if>
</tbody></table></div></section>
<section class="log-card" style="margin-top:1.25rem"><header class="log-card-header"><h2 class="log-card-title">Phiên thi sắp tới</h2><a href="${ctx}/manager/exam-schedules?tab=upcoming" style="text-decoration:none;color:#0052cc;font-weight:700">Xem tất cả</a></header>
<div class="table-responsive"><table class="audit-table"><thead><tr><th>Phiên thi</th><th>Ngày thi</th><th>Hạng</th><th>Trung tâm</th><th>Thí sinh</th><th></th></tr></thead><tbody>
<c:forEach var="exam" items="${upcomingSessions}"><tr><td><strong><c:out value="${exam.sessionName}" /></strong></td><td><fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" /></td><td>Hạng <c:out value="${exam.licenseCode}" /></td><td><c:out value="${exam.centreName}" /></td><td><strong>${exam.registeredCount}</strong><c:if test="${exam.registeredCount eq 0}"><br><small style="color:#dc2626">Chưa import danh sách</small></c:if></td><td><a class="btn-export" href="${ctx}/manager/exam-schedules?view=${exam.id}" style="text-decoration:none">Chi tiết</a></td></tr></c:forEach>
<c:if test="${empty upcomingSessions}"><tr><td colspan="6" style="padding:2rem;text-align:center;color:#64748b">Chưa có phiên thi sắp tới.</td></tr></c:if>
</tbody></table></div></section>
</main><jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include></div></body></html>
