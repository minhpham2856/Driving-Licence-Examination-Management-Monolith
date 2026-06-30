<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Theo dõi hồ sơ - Lái Vui</title><link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css"></head>
<body class="has-side-nav-bar"><jsp:include page="/views/layout/sidebar-registrant.jsp"><jsp:param name="activeSidebar" value="track-profile"/></jsp:include>
<div class="dashboard-shell"><main class="main-content">
<header class="page-header"><div class="page-title-wrap"><h1 class="page-title">Theo dõi hồ sơ</h1>
<p class="page-subtitle">Trạng thái xử lý hồ sơ sát hạch hiện tại.</p></div></header>
<section class="report-pane" style="padding:2rem">
<h2>${dossier.status}</h2>
<c:choose>
<c:when test="${dossier.status eq 'Approved'}"><p>Hồ sơ đã được xác minh. Bạn có thể đăng ký lịch thi.</p><a class="btn-filter" href="${ctx}/views/registrant/register-exam.jsp">Đăng ký lịch thi</a></c:when>
<c:when test="${dossier.status eq 'Submitted'}"><p>Ban quản lý đang kiểm tra thông tin và giấy tờ bạn đã nộp.</p></c:when>
<c:when test="${dossier.status eq 'NeedSupplement'}"><p>Hồ sơ cần bổ sung: ${dossier.notes}</p><a class="btn-filter" href="${ctx}/registrant/dossier">Bổ sung hồ sơ</a></c:when>
<c:when test="${dossier.status eq 'Rejected'}"><p>Hồ sơ bị từ chối: ${dossier.notes}</p><a class="btn-filter" href="${ctx}/registrant/dossier">Nộp lại hồ sơ</a></c:when>
<c:otherwise><p>Hồ sơ đang ở bản nháp. Hãy tải đủ giấy tờ và gửi duyệt.</p><a class="btn-filter" href="${ctx}/registrant/dossier">Hoàn thiện hồ sơ</a></c:otherwise>
</c:choose>
</section></main><jsp:include page="/views/layout/footer.jsp"/></div></body></html>
