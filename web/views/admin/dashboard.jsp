<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Quản trị - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="dashboard" /></jsp:include>
<div class="dashboard-shell"><main class="main-content">
    <nav class="breadcrumbs"><a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current" aria-current="page">Quản trị</span></nav>

    <header class="page-header"><div class="page-title-wrap">
        <h1 class="page-title">Bảng điều khiển Quản trị</h1>
        <p class="page-subtitle">Tổng quan cấu hình hệ thống thi sát hạch GPLX.</p></div></header>

    <section class="metrics-row">
        <a href="${ctx}/admin/exam-area" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--blue"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty zoneCount ? 0 : zoneCount}</span><span class="stat-label">Khu vực thi</span><span class="stat-trend stat-trend--up">Khuôn viên</span></div></div></a>
        <a href="${ctx}/admin/exam-room" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--blue" style="background:rgba(13,148,136,.08);color:#0d9488;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M3 21V7l9-4 9 4v14" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty areaCount ? 0 : areaCount}</span><span class="stat-label">Phòng / Sân thi</span><span class="stat-trend stat-trend--up" style="color:#0d9488;">Địa điểm</span></div></div></a>
        <a href="${ctx}/admin/exam-computer" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--blue" style="background:rgba(124,58,237,.08);color:#7c3aed;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="2" y="4" width="20" height="13" rx="2" stroke="currentColor" stroke-width="2"/><path d="M8 20h8M12 17v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty deviceCount ? 0 : deviceCount}</span><span class="stat-label">Máy thi / Thiết bị</span><span class="stat-trend stat-trend--up" style="color:#7c3aed;">Máy tính, mô tô</span></div></div></a>
        <a href="${ctx}/admin/licence-class" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--blue" style="background:rgba(2,132,199,.08);color:#0284c7;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="1" y="4" width="22" height="16" rx="2" stroke="currentColor" stroke-width="2"/><circle cx="7" cy="12" r="2.5" stroke="currentColor" stroke-width="2"/><path d="M12 9h8M12 12h6M12 15h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty licenceCount ? 0 : licenceCount}</span><span class="stat-label">Hạng GPLX</span><span class="stat-trend stat-trend--up" style="color:#0284c7;">Danh mục</span></div></div></a>
    </section>

    <section class="metrics-row" style="margin-top:1rem;">
        <a href="${ctx}/admin/exam-fee" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--amber"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/><path d="M2 10h20" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty feeCount ? 0 : feeCount}</span><span class="stat-label">Loại phí</span><span class="stat-trend stat-trend--up">Lệ phí thi</span></div></div></a>
        <a href="${ctx}/admin/accounts" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--green"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/><path d="M3 21c0-4 3-7 6-7s6 3 6 7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty accountCount ? 0 : accountCount}</span><span class="stat-label">Tài khoản hoạt động</span><span class="stat-trend stat-trend--up">Người dùng</span></div></div></a>
        <a href="${ctx}/admin/audit" style="text-decoration:none;"><div class="stat-card">
            <div class="stat-icon stat-icon--red"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M14 2v6h6" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty auditCount ? 0 : auditCount}</span><span class="stat-label">Bản ghi nhật ký</span><span class="stat-trend stat-trend--up">Kiểm toán</span></div></div></a>
    </section>

    <section class="log-card" style="margin-top:1.5rem;"><header class="log-card-header">
        <h2 class="log-card-title">Nhật ký gần đây</h2>
        <div class="log-card-actions"><a href="${ctx}/admin/audit" class="btn-export">Xem tất cả</a></div>
    </header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr><th style="width:160px;">Thời gian</th><th>Người thực hiện</th><th style="width:120px;text-align:center;">Thao tác</th><th>Nội dung</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty recentLogs}">
            <c:forEach var="log" items="${recentLogs}"><tr>
                <td style="font-size:.82rem;color:#475569;"><fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                <td style="font-weight:600;">${log.displayName}</td>
                <td style="text-align:center;"><span class="role-badge role-badge--coi">${log.actionLabel}</span></td>
                <td style="font-size:.85rem;color:#334155;">${log.detail}</td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="4" style="text-align:center;padding:3rem;color:#64748b;">Chưa có nhật ký nào.</td></tr></c:otherwise></c:choose>
        </tbody>
    </table></div></section>
</main>
</div>
</body></html>
