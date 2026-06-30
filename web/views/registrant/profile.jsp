<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Hồ sơ cá nhân - Lái Vui</title><link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css"></head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-registrant.jsp"><jsp:param name="activeSidebar" value="profile"/></jsp:include>
<div class="dashboard-shell"><main class="main-content">
<header class="page-header"><div class="page-title-wrap"><h1 class="page-title">Hồ sơ cá nhân</h1>
<p class="page-subtitle">Thông tin định danh đang được sử dụng trong hồ sơ sát hạch.</p></div>
<span class="action-badge action-badge--warning">${dossier.status}</span></header>
<div class="profile-grid">
<section class="profile-main-content"><div class="report-pane" style="padding:1.5rem">
<div class="p-form-grid">
<div><strong>Họ và tên</strong><p>${dossier.profile.fullName}</p></div>
<div><strong>Ngày sinh</strong><p>${dossier.profile.dateOfBirth}</p></div>
<div><strong>CCCD</strong><p>${dossier.profile.govIdNo}</p></div>
<div><strong>Số điện thoại</strong><p>${dossier.profile.phoneNo}</p></div>
<div><strong>Email</strong><p>${dossier.user.email}</p></div>
<div><strong>Địa chỉ</strong><p>${dossier.profile.address}</p></div>
<div><strong>Hạng GPLX</strong><p>${empty dossier.licenceClass ? 'Chưa chọn' : dossier.licenceClass}</p></div>
<div><strong>Số tài liệu</strong><p>${dossier.documentCount}/4</p></div>
</div></div></section>
<aside class="profile-sidebar"><div class="profile-sidebar-card"><h3>Trạng thái hồ sơ</h3>
<p>${dossier.status}</p><p>${dossier.notes}</p>
<a class="btn-filter" href="${ctx}/registrant/dossier">Bổ sung/Nộp hồ sơ</a></div></aside>
</div></main><jsp:include page="/views/layout/footer.jsp"/></div></body></html>
