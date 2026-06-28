<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Duyá»‡t há»“ sÆ¡ - LÃ¡i Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="duyet-ho-so" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Duyá»‡t há»“ sÆ¡</span>
    </nav>
    <c:if test="${not empty sessionScope.reviewSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857">${sessionScope.reviewSuccess}</div>
        <c:remove var="reviewSuccess" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.reviewError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b">${sessionScope.reviewError}</div>
        <c:remove var="reviewError" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${not empty dossier}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title"><c:out value="${dossier.profile.fullName}" /></h1>
                    <p class="page-subtitle">CCCD: <c:out value="${dossier.profile.govIdNo}" /> Â· Háº¡ng <c:out value="${dossier.licenceDisplayClass}" /></p>
                </div>
                <div class="page-actions" style="display:flex;gap:.75rem">
                    <a class="btn-export" href="${ctx}/manager/dossier-detail?registrationId=${dossier.registrationId}"
                       style="display:inline-flex;text-decoration:none">Xem chi tiáº¿t</a>
                    <a class="btn-export" href="${ctx}/manager/dossiers"
                       style="display:inline-flex;text-decoration:none">Quay láº¡i danh sÃ¡ch</a>
                </div>
            </header>
            <div class="profile-grid">
                <section class="profile-main-content">
                    <div class="log-card">
                        <div class="log-card-header"><h2 class="log-card-title">TÃ i liá»‡u Ä‘Ã£ ná»™p (${dossier.documentCount}/${dossier.requiredDocumentTotal})</h2></div>
                        <div class="report-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));padding:1.5rem">
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
                        <strong>Ghi chÃº há»‡ thá»‘ng:</strong>
                        <p><c:out value="${empty dossier.reviewMessage ? 'ChÆ°a cÃ³ ghi chÃº.' : dossier.reviewMessage}" /></p>
                    </div>
                </section>
                <aside class="profile-sidebar">
                    <div class="profile-sidebar-card">
                        <h3>Quyáº¿t Ä‘á»‹nh tháº©m Ä‘á»‹nh</h3>
                        <c:if test="${not dossier.complete}">
                            <div class="p-alert-banner" style="border-color:#f59e0b;color:#92400e;margin:1rem 0">
                                Há»“ sÆ¡ chÆ°a Ä‘á»§ 4 tÃ i liá»‡u nÃªn chÆ°a thá»ƒ duyá»‡t.
                            </div>
                        </c:if>
                        <form action="${ctx}/manager/dossiers" method="post" style="width:100%">
                            <input type="hidden" name="id" value="${dossier.registrationId}">
                            <label><input type="radio" name="decision" value="approve"
                                          ${dossier.complete ? 'checked' : 'disabled'}> Duyá»‡t há»“ sÆ¡</label><br><br>
                            <label><input type="radio" name="decision" value="supplement"
                                          ${not dossier.complete ? 'checked' : ''}> YÃªu cáº§u bá»• sung</label><br><br>
                            <label><input type="radio" name="decision" value="reject"> Tá»« chá»‘i</label><br><br>
                            <label class="input-label" for="reason">LÃ½ do/Ghi chÃº</label>
                            <textarea class="input-field" id="reason" name="reason" rows="5"
                                      style="height:auto"></textarea>
                            <button class="btn-filter" type="submit" style="width:100%;margin-top:1rem">XÃ¡c nháº­n</button>
                        </form>
                    </div>
                </aside>
            </div>
        </c:when>
        <c:otherwise>
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title">Há»“ sÆ¡ chÆ°a Ä‘Æ°á»£c duyá»‡t</h1>
                    <p class="page-subtitle">Dá»¯ liá»‡u láº¥y trá»±c tiáº¿p tá»« database, gá»“m há»“ sÆ¡ nhÃ¡p, chá» duyá»‡t, cáº§n bá»• sung vÃ  Ä‘Ã£ tá»« chá»‘i.</p>
                </div>
            </header>
            <section class="log-card">
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>MÃ£</th><th>Há» tÃªn</th><th>CCCD</th><th>Háº¡ng</th><th>TÃ i liá»‡u</th><th>Tráº¡ng thÃ¡i</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="item" items="${dossiers}">
                            <tr>
                                <td>#${item.registrationId}</td>
                                <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                <td><c:out value="${item.profile.govIdNo}" /></td>
                                <td><c:out value="${item.licenceDisplayClass}" /></td>
                                <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                                <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                                <td>
                                    <div style="display:flex;gap:.5rem;justify-content:flex-end">
                                        <a class="btn-export" href="${ctx}/manager/dossier-detail?registrationId=${item.registrationId}"
                                           style="display:inline-flex;text-decoration:none">Chi tiáº¿t</a>
                                        <a class="btn-filter" href="${ctx}/manager/dossiers?id=${item.registrationId}"
                                           style="display:inline-flex;text-decoration:none">Tháº©m Ä‘á»‹nh</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty dossiers}">
                            <tr><td colspan="7" style="text-align:center;padding:2rem">KhÃ´ng cÃ³ há»“ sÆ¡ nÃ o chÆ°a Ä‘Æ°á»£c duyá»‡t.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/layout/footer.jsp" />
</div>
</body>
</html>

