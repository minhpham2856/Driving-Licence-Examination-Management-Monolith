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
    <title>Chi tiáº¿t há»“ sÆ¡ há»c viÃªn - LÃ¡i Vui</title>
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
        <a href="${ctx}/manager/registrants">Danh sÃ¡ch há»c viÃªn</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Chi tiáº¿t há»“ sÆ¡</span>
    </nav>

    <c:choose>
        <c:when test="${listMode}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title">Quáº£n lÃ½ há»“ sÆ¡ thÃ­ sinh</h1>
                    <p class="page-subtitle">ToÃ n bá»™ tÃ i khoáº£n Registrant Ä‘Æ°á»£c táº£i trá»±c tiáº¿p tá»« database.</p>
                </div>
                <span class="action-badge action-badge--info" style="font-weight:700">
                    ${fn:length(dossiers)} thÃ­ sinh
                </span>
            </header>

            <section class="log-card">
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead>
                            <tr>
                                <th>MÃ£ tÃ i khoáº£n</th>
                                <th>Há» vÃ  tÃªn</th>
                                <th>CCCD</th>
                                <th>LiÃªn há»‡</th>
                                <th>Háº¡ng GPLX</th>
                                <th>Giáº¥y tá»</th>
                                <th>Tráº¡ng thÃ¡i há»“ sÆ¡</th>
                                <th style="text-align:center">Thao tÃ¡c</th>
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
                                    <td><c:out value="${empty item.licenceDisplayClass ? 'ChÆ°a chá»n' : item.licenceDisplayClass}" /></td>
                                    <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                                    <td>
                                        <span class="action-badge action-badge--${item.statusKey}">
                                            ${item.statusLabel}
                                        </span>
                                    </td>
                                    <td style="text-align:center">
                                        <a class="btn-export"
                                           href="${ctx}/manager/dossier-detail?id=${item.user.id}"
                                           style="display:inline-flex;text-decoration:none">Xem chi tiáº¿t</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty dossiers}">
                                <tr>
                                    <td colspan="8" style="padding:3rem;text-align:center;color:#64748b">
                                        ChÆ°a cÃ³ tÃ i khoáº£n Registrant trong database.
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
                        TÃ i khoáº£n @<c:out value="${dossier.user.username}" />
                        Â· Há»“ sÆ¡ #${dossier.registrationId}
                        Â· Háº¡ng <c:out value="${empty dossier.licenceDisplayClass ? 'ChÆ°a chá»n' : dossier.licenceDisplayClass}" />
                    </p>
                </div>
                <div class="page-actions" style="display:flex;gap:.75rem">
                    <a class="btn-export" href="${ctx}/manager/registrants"
                       style="display:inline-flex;text-decoration:none">Quay láº¡i</a>
                    <c:if test="${dossier.status eq 'Submitted' or dossier.status eq 'NeedSupplement'}">
                        <a class="btn-filter" href="${ctx}/manager/dossiers?id=${dossier.registrationId}"
                           style="display:inline-flex;text-decoration:none">Tháº©m Ä‘á»‹nh há»“ sÆ¡</a>
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
                            <div class="quick-info-item"><span class="quick-info-label">NgÃ y sinh</span><span class="quick-info-value"><fmt:formatDate value="${dossier.profile.dateOfBirth}" pattern="dd/MM/yyyy" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Giá»›i tÃ­nh</span><span class="quick-info-value"><c:out value="${dossier.profile.sex}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Äiá»‡n thoáº¡i</span><span class="quick-info-value"><c:out value="${dossier.profile.phoneNo}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Email</span><span class="quick-info-value"><c:out value="${dossier.user.email}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Äá»‹a chá»‰</span><span class="quick-info-value" style="text-align:right"><c:out value="${dossier.profile.address}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">TÃ i khoáº£n</span><span class="quick-info-value">${dossier.user.active ? 'Äang hoáº¡t Ä‘á»™ng' : 'ÄÃ£ khÃ³a'}</span></div>
                        </div>
                    </div>
                </aside>

                <section class="profile-main-content" style="gap:1.5rem">
                    <div class="log-card">
                        <div class="log-card-header">
                            <h2 class="log-card-title">Giáº¥y tá» há»“ sÆ¡ (${dossier.documentCount}/${dossier.requiredDocumentTotal})</h2>
                            <span class="action-badge action-badge--${dossier.complete ? 'success' : 'warning'}">
                                ${dossier.complete ? 'ÄÃƒ Äá»¦ Há»’ SÆ ' : 'CHÆ¯A Äá»¦ Há»’ SÆ '}
                            </span>
                        </div>
                        <div class="report-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));padding:1.5rem;gap:1rem">
                            <c:set var="documentTypes" value="PORTRAIT,ID_FRONT,ID_BACK,HEALTH_CERTIFICATE" />
                            <c:forTokens var="type" items="${documentTypes}" delims=",">
                                <c:set var="document" value="${dossier.documents[type]}" />
                                <div class="profile-score-card" style="align-items:flex-start;min-height:120px">
                                    <strong>
                                        <c:choose>
                                            <c:when test="${type eq 'PORTRAIT'}">áº¢nh chÃ¢n dung 3x4</c:when>
                                            <c:when test="${type eq 'ID_FRONT'}">CCCD máº·t trÆ°á»›c</c:when>
                                            <c:when test="${type eq 'ID_BACK'}">CCCD máº·t sau</c:when>
                                            <c:otherwise>Giáº¥y khÃ¡m sá»©c khá»e</c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">ÄÃ£ táº£i lÃªn</span>
                                            <a class="btn-export" target="_blank" rel="noopener"
                                               href="${ctx}${document.documentUrl}"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Má»Ÿ tÃ i liá»‡u</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">CÃ²n thiáº¿u</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forTokens>
                            <c:if test="${dossier.graduationCertificateRequired}">
                                <c:set var="document" value="${dossier.documents['GRADUATION_CERTIFICATE']}" />
                                <div class="profile-score-card" style="align-items:flex-start;min-height:120px">
                                    <strong>Giấy tốt nghiệp / chứng chỉ đào tạo</strong>
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
                            </c:if>
                        </div>
                    </div>

                    <div class="report-pane" style="padding:1.5rem">
                        <h2 class="log-card-title">Káº¿t quáº£ xá»­ lÃ½ há»“ sÆ¡</h2>
                        <div style="display:grid;grid-template-columns:180px 1fr;gap:.75rem;margin-top:1rem">
                            <strong>Tráº¡ng thÃ¡i hiá»‡n táº¡i</strong><span>${dossier.statusLabel}</span>
                            <strong>Háº¡ng GPLX</strong><span><c:out value="${empty dossier.licenceDisplayClass ? 'ChÆ°a chá»n' : dossier.licenceDisplayClass}" /></span>
                            <strong>Ghi chÃº gáº§n nháº¥t</strong><span><c:out value="${empty dossier.reviewMessage ? 'ChÆ°a cÃ³ ghi chÃº' : dossier.reviewMessage}" /></span>
                        </div>
                    </div>
                </section>
            </div>
        </c:when>
        <c:otherwise>
            <div class="report-pane" style="padding:4rem 1.5rem;text-align:center;margin-top:1.5rem">
                KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ thÃ­ sinh.
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

