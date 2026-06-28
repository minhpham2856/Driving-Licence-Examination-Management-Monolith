<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết hồ sơ học viên - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="chi-tiet-hoc-vien" />
</jsp:include>

<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <a href="${ctx}/manager/registrants">Danh sách học viên</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Chi tiết hồ sơ</span>
    </nav>

    <c:choose>
        <c:when test="${listMode}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title">Quản lý hồ sơ thí sinh</h1>
                    <p class="page-subtitle">Toàn bộ tài khoản Registrant được tải trực tiếp từ database.</p>
                </div>
                <span class="action-badge action-badge--info" style="font-weight:700">
                    ${fn:length(dossiers)} thí sinh
                </span>
            </header>

            <section class="log-card">
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead>
                            <tr>
                                <th>Mã tài khoản</th>
                                <th>Họ và tên</th>
                                <th>CCCD</th>
                                <th>Liên hệ</th>
                                <th>Hạng GPLX</th>
                                <th>Giấy tờ</th>
                                <th>Trạng thái hồ sơ</th>
                                <th style="text-align:center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${dossiers}">
                                <tr>
                                    <td>#${item.user.id}<br><small>@<c:out value="${item.user.username}" /></small></td>
                                    <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                    <td><c:out value="${item.profile.govIdNo}" /></td>
                                    <td>
                                        <c:out value="${item.profile.phoneNo}" /><br>
                                        <small><c:out value="${item.user.email}" /></small>
                                    </td>
                                    <td><c:out value="${empty item.licenceClass ? 'Chưa chọn' : item.licenceClass}" /></td>
                                    <td>${item.documentCount}/4</td>
                                    <td>
                                        <span class="action-badge action-badge--${item.statusKey}">
                                            ${item.statusLabel}
                                        </span>
                                    </td>
                                    <td style="text-align:center">
                                        <a class="btn-export"
                                           href="${ctx}/manager/dossier-detail?id=${item.user.id}"
                                           style="display:inline-flex;text-decoration:none">Xem chi tiết</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty dossiers}">
                                <tr>
                                    <td colspan="8" style="padding:3rem;text-align:center;color:#64748b">
                                        Chưa có tài khoản Registrant trong database.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:when>
        <c:when test="${not empty dossier}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title"><c:out value="${dossier.profile.fullName}" /></h1>
                    <p class="page-subtitle">
                        Tài khoản @<c:out value="${dossier.user.username}" />
                        · Hồ sơ #${dossier.registrationId}
                        · Hạng <c:out value="${empty dossier.licenceClass ? 'Chưa chọn' : dossier.licenceClass}" />
                    </p>
                </div>
                <div class="page-actions" style="display:flex;gap:.75rem">
                    <a class="btn-export" href="${ctx}/manager/registrants"
                       style="display:inline-flex;text-decoration:none">Quay lại</a>
                    <c:if test="${dossier.status eq 'Submitted' or dossier.status eq 'NeedSupplement'}">
                        <a class="btn-filter" href="${ctx}/manager/dossiers?id=${dossier.registrationId}"
                           style="display:inline-flex;text-decoration:none">Thẩm định hồ sơ</a>
                    </c:if>
                </div>
            </header>

            <div class="profile-grid">
                <aside class="profile-sidebar">
                    <div class="profile-sidebar-card">
                        <div class="profile-avatar-large profile-avatar--blue"
                             style="margin:auto;background:linear-gradient(135deg,#0052cc,#6366f1)">
                            ${fn:substring(dossier.profile.fullName, 0, 1)}
                        </div>
                        <h2 class="profile-name"><c:out value="${dossier.profile.fullName}" /></h2>
                        <span class="action-badge action-badge--${dossier.statusKey}">${dossier.statusLabel}</span>
                        <div class="profile-quick-info" style="margin-top:1.5rem">
                            <div class="quick-info-item"><span class="quick-info-label">CCCD</span><span class="quick-info-value"><c:out value="${dossier.profile.govIdNo}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Ngày sinh</span><span class="quick-info-value"><fmt:formatDate value="${dossier.profile.dateOfBirth}" pattern="dd/MM/yyyy" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Giới tính</span><span class="quick-info-value"><c:out value="${dossier.profile.sex}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Điện thoại</span><span class="quick-info-value"><c:out value="${dossier.profile.phoneNo}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Email</span><span class="quick-info-value"><c:out value="${dossier.user.email}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Địa chỉ</span><span class="quick-info-value" style="text-align:right"><c:out value="${dossier.profile.address}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Tài khoản</span><span class="quick-info-value">${dossier.user.active ? 'Đang hoạt động' : 'Đã khóa'}</span></div>
                        </div>
                    </div>
                </aside>

                <section class="profile-main-content" style="gap:1.5rem">
                    <div class="log-card">
                        <div class="log-card-header">
                            <h2 class="log-card-title">Giấy tờ hồ sơ (${dossier.documentCount}/4)</h2>
                            <span class="action-badge action-badge--${dossier.complete ? 'success' : 'warning'}">
                                ${dossier.complete ? 'ĐÃ ĐỦ HỒ SƠ' : 'CHƯA ĐỦ HỒ SƠ'}
                            </span>
                        </div>
                        <div class="report-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));padding:1.5rem;gap:1rem">
                            <c:set var="documentTypes" value="PORTRAIT,ID_FRONT,ID_BACK,HEALTH_CERTIFICATE" />
                            <c:forTokens var="type" items="${documentTypes}" delims=",">
                                <c:set var="document" value="${dossier.documents[type]}" />
                                <div class="profile-score-card" style="align-items:flex-start;min-height:120px">
                                    <strong>
                                        <c:choose>
                                            <c:when test="${type eq 'PORTRAIT'}">Ảnh chân dung 3x4</c:when>
                                            <c:when test="${type eq 'ID_FRONT'}">CCCD mặt trước</c:when>
                                            <c:when test="${type eq 'ID_BACK'}">CCCD mặt sau</c:when>
                                            <c:otherwise>Giấy khám sức khỏe</c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">Đã tải lên</span>
                                            <a class="btn-export" target="_blank" rel="noopener"
                                               href="${ctx}${document.documentUrl}"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Mở tài liệu</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">Còn thiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forTokens>
                        </div>
                    </div>

                    <div class="report-pane" style="padding:1.5rem">
                        <h2 class="log-card-title">Kết quả xử lý hồ sơ</h2>
                        <div style="display:grid;grid-template-columns:180px 1fr;gap:.75rem;margin-top:1rem">
                            <strong>Trạng thái hiện tại</strong><span>${dossier.statusLabel}</span>
                            <strong>Hạng GPLX</strong><span><c:out value="${empty dossier.licenceClass ? 'Chưa chọn' : dossier.licenceClass}" /></span>
                            <strong>Ghi chú gần nhất</strong><span><c:out value="${empty dossier.reviewMessage ? 'Chưa có ghi chú' : dossier.reviewMessage}" /></span>
                        </div>
                    </div>
                </section>
            </div>
        </c:when>
        <c:otherwise>
            <div class="report-pane" style="padding:4rem 1.5rem;text-align:center;margin-top:1.5rem">
                Không tìm thấy hồ sơ thí sinh.
            </div>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/layout/footer.jsp">
    <jsp:param name="standalone" value="false" />
</jsp:include>
</div>
</body>
</html>
