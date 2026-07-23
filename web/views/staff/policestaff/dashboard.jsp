<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Cổng CSGT - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .police-stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1rem;margin:1.2rem 0}
        .police-stat,.police-card{background:#fff;border:1px solid #dbe3ef;border-radius:14px;padding:1.2rem}
        .police-stat strong{display:block;font-size:1.8rem;color:#064e3b}.police-stat span{color:#64748b;font-weight:700;font-size:.8rem;text-transform:uppercase}
        .police-table{width:100%;border-collapse:collapse}.police-table th,.police-table td{padding:.85rem;border-bottom:1px solid #e2e8f0;text-align:left}
        .police-table th:last-child,.police-table td:last-child{text-align:center}
        .police-table th{background:#f8fafc;color:#475569;font-size:.78rem}.badge{display:inline-flex;padding:.25rem .6rem;border-radius:999px;font-size:.78rem;font-weight:700}
        .badge--pending{background:#fef3c7;color:#92400e}.badge--done{background:#dcfce7;color:#166534}.police-error{padding:1rem;border-radius:10px;background:#fef2f2;color:#b91c1c;margin:1rem 0}
        .row-actions{display:flex;justify-content:center;gap:.5rem;flex-wrap:wrap}.police-action{display:inline-flex;align-items:center;justify-content:center;min-width:150px;padding:.62rem .85rem;border:1px solid #075bd8;border-radius:8px;background:#075bd8;color:#fff;font-weight:700;text-decoration:none;white-space:nowrap}.police-action:hover{background:#064eb9}
        .pager{display:flex;justify-content:space-between;align-items:center;gap:1rem;padding-top:1rem;color:#64748b}.pager-links{display:flex;gap:.4rem}.pager a,.pager span{padding:.45rem .7rem;border:1px solid #cbd5e1;border-radius:8px;text-decoration:none}.pager .is-current{background:#075bd8;color:#fff;border-color:#075bd8}
        @media(max-width:800px){.police-stats{grid-template-columns:1fr}.police-card{overflow:auto}}
    </style>
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-policestaff.jsp"><jsp:param name="activeSidebar" value="dashboard"/></jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <div class="page-header"><div><p class="breadcrumb">Cổng CSGT / Dashboard</p><h1 class="page-title">Tiếp nhận và xử lý hồ sơ</h1><p class="page-subtitle">Chỉ xử lý thí sinh có tài khoản, hồ sơ và đăng ký trên hệ thống Lái Vui.</p></div></div>
    <c:if test="${not empty policeDashboardError}"><div class="police-error"><c:out value="${policeDashboardError}"/></div></c:if>
    <section class="police-stats">
        <article class="police-stat"><strong>${pendingSubmissionCount}</strong><span>Danh sách chờ xử lý</span></article>
        <article class="police-stat"><strong>${pendingCandidateCount}</strong><span>Hồ sơ chờ thẩm định</span></article>
        <article class="police-stat"><strong>${completedSubmissionCount}</strong><span>Danh sách đã hoàn tất</span></article>
    </section>
    <section class="police-card">
        <h2 style="margin-top:0">Danh sách đã tiếp nhận</h2>
        <table class="police-table">
            <thead><tr><th>Mã</th><th>Ngày dự kiến</th><th>Hạng</th><th>Hồ sơ</th><th>Đã duyệt</th><th>Từ chối</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach var="row" items="${submissions}">
                <tr><td>#${row.examDateId}</td><td><fmt:formatDate value="${row.examDate}" pattern="dd/MM/yyyy"/></td><td><strong>${row.licenceClass}</strong></td><td>${row.totalCandidates}</td><td>${row.approvedCandidates}</td><td>${row.rejectedCandidates}</td><td><span class="badge ${row.completed ? 'badge--done' : 'badge--pending'}">${row.completed ? 'Đã ban hành' : 'Đang thẩm định'}</span></td><td><div class="row-actions"><a class="police-action" href="${ctx}/police/submissions?dateId=${row.examDateId}">Thẩm định</a><a class="police-action" href="${ctx}/police/official-rosters?dateId=${row.examDateId}">Danh sách chính thức</a></div></td></tr>
            </c:forEach>
            <c:if test="${empty submissions}"><tr><td colspan="8" style="text-align:center;padding:2rem;color:#64748b">Chưa có danh sách nào được gửi tới CSGT.</td></tr></c:if>
            </tbody>
        </table>
        <c:if test="${totalPages gt 1}">
            <div class="pager">
                <span>Trang ${page}/${totalPages} · ${totalSubmissions} danh sách</span>
                <div class="pager-links">
                    <c:if test="${page gt 1}"><a href="${ctx}/police/dashboard?page=${page - 1}">Trước</a></c:if>
                    <span class="is-current">${page}</span>
                    <c:if test="${page lt totalPages}"><a href="${ctx}/police/dashboard?page=${page + 1}">Sau</a></c:if>
                </div>
            </div>
        </c:if>
    </section>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false"/></jsp:include>
</div>
</body>
</html>
