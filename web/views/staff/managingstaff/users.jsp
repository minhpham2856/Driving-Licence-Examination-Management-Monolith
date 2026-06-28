<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${empty requestScope.totalRegistrants}">
    <c:redirect url="/manager/registrants" />
</c:if>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quáº£n lÃ½ thÃ­ sinh - LÃ¡i Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="hoc-vien" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Quáº£n lÃ½ thÃ­ sinh</span>
    </nav>
    <header class="page-header">
        <div class="page-title-wrap">
            <h1 class="page-title">Quáº£n LÃ½ ThÃ­ Sinh</h1>
            <p class="page-subtitle">Quáº£n lÃ½ tÃ i khoáº£n, há»“ sÆ¡ vÃ  tráº¡ng thÃ¡i Ä‘Äƒng kÃ½ cá»§a toÃ n bá»™ Registrant.</p>
        </div>
        <div class="page-actions" style="display:flex;gap:.75rem">
            <a class="btn-export"
               href="${ctx}/manager/registrants?export=csv&amp;keyword=${fn:escapeXml(param.keyword)}&amp;licence=${fn:escapeXml(param.licence)}&amp;dossierStatus=${fn:escapeXml(param.dossierStatus)}&amp;accountStatus=${fn:escapeXml(param.accountStatus)}"
               style="display:inline-flex;text-decoration:none">Xuáº¥t danh sÃ¡ch Excel</a>
            <a class="btn-filter" href="${ctx}/manager/create-user"
               style="display:inline-flex;text-decoration:none">Táº¡o tÃ i khoáº£n &amp; há»“ sÆ¡</a>
        </div>
    </header>

    <c:if test="${not empty sessionScope.registrantSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857"><c:out value="${sessionScope.registrantSuccess}" /></div>
        <c:remove var="registrantSuccess" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.registrantError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b"><c:out value="${sessionScope.registrantError}" /></div>
        <c:remove var="registrantError" scope="session" />
    </c:if>

    <div class="report-grid" style="grid-template-columns:repeat(4,minmax(0,1fr));gap:1rem;margin:1.25rem 0">
        <div class="profile-score-card"><span class="score-card-part">Tá»”NG THÃ SINH</span><strong style="font-size:1.7rem">${totalRegistrants}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">Há»’ SÆ  ÄÃƒ DUYá»†T</span><strong style="font-size:1.7rem;color:#059669">${approvedCount}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">Cáº¦N Xá»¬ LÃ</span><strong style="font-size:1.7rem;color:#d97706">${pendingCount}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">TÃ€I KHOáº¢N ÄÃƒ KHÃ“A</span><strong style="font-size:1.7rem;color:#dc2626">${lockedCount}</strong></div>
    </div>

    <section class="filter-panel">
        <h2 class="filter-title">TÃ¬m kiáº¿m vÃ  lá»c dá»¯ liá»‡u</h2>
        <form action="${ctx}/manager/registrants" method="get">
            <div class="filter-grid" style="grid-template-columns:2fr 1fr 1.25fr 1fr 1.5fr">
                <div class="input-group">
                    <label class="input-label" for="keyword">TÃªn, CCCD, email, SÄT hoáº·c username</label>
                    <input class="input-field" id="keyword" name="keyword" value="<c:out value='${param.keyword}' />" placeholder="Nháº­p tá»« khÃ³a">
                </div>
                <div class="input-group">
                    <label class="input-label" for="licence">Háº¡ng GPLX</label>
                    <select class="input-field" id="licence" name="licence">
                        <option value="">Táº¥t cáº£</option>
                        <option value="A1" ${param.licence eq 'A1' ? 'selected' : ''}>Háº¡ng A1</option>
                        <option value="A2" ${param.licence eq 'A2' ? 'selected' : ''}>Háº¡ng A2</option>
                        <option value="B1" ${param.licence eq 'B1' ? 'selected' : ''}>Háº¡ng B1</option>
                        <option value="B2" ${param.licence eq 'B2' ? 'selected' : ''}>Háº¡ng B2</option>
                        <option value="C" ${param.licence eq 'C' ? 'selected' : ''}>Háº¡ng C</option>
                        <option value="C1" ${param.licence eq 'C1' ? 'selected' : ''}>Háº¡ng C1</option>
                    </select>
                </div>
                <div class="input-group">
                    <label class="input-label" for="dossierStatus">Tráº¡ng thÃ¡i há»“ sÆ¡</label>
                    <select class="input-field" id="dossierStatus" name="dossierStatus">
                        <option value="">Táº¥t cáº£</option>
                        <option value="Draft" ${param.dossierStatus eq 'Draft' ? 'selected' : ''}>Báº£n nhÃ¡p</option>
                        <option value="Pending" ${param.dossierStatus eq 'Pending' ? 'selected' : ''}>Chá» duyá»‡t</option>
                        <option value="Submitted" ${param.dossierStatus eq 'Submitted' ? 'selected' : ''}>ÄÃ£ gá»­i duyá»‡t</option>
                        <option value="NeedSupplement" ${param.dossierStatus eq 'NeedSupplement' ? 'selected' : ''}>Cáº§n bá»• sung</option>
                        <option value="Approved" ${param.dossierStatus eq 'Approved' ? 'selected' : ''}>ÄÃ£ duyá»‡t</option>
                        <option value="Rejected" ${param.dossierStatus eq 'Rejected' ? 'selected' : ''}>ÄÃ£ tá»« chá»‘i</option>
                        <option value="Present" ${param.dossierStatus eq 'Present' ? 'selected' : ''}>Äang tham gia thi</option>
                    </select>
                </div>
                <div class="input-group">
                    <label class="input-label" for="accountStatus">TÃ i khoáº£n</label>
                    <select class="input-field" id="accountStatus" name="accountStatus">
                        <option value="">Táº¥t cáº£</option>
                        <option value="active" ${param.accountStatus eq 'active' ? 'selected' : ''}>Hoáº¡t Ä‘á»™ng</option>
                        <option value="locked" ${param.accountStatus eq 'locked' ? 'selected' : ''}>ÄÃ£ khÃ³a</option>
                    </select>
                </div>
                <div class="input-group filter-grid__btn-col">
                    <div class="btn-group">
                        <button class="btn-filter" type="submit">Ãp dá»¥ng</button>
                        <a class="btn-reset" href="${ctx}/manager/registrants">Äáº·t láº¡i</a>
                    </div>
                </div>
            </div>
        </form>
    </section>

    <section class="log-card">
        <header class="log-card-header">
            <h2 class="log-card-title">Danh sÃ¡ch thÃ­ sinh tá»« database</h2>
            <span class="action-badge action-badge--info">${fn:length(registrants)} káº¿t quáº£</span>
        </header>
        <div class="table-responsive">
            <table class="audit-table">
                <thead><tr><th>MÃ£</th><th>ThÃ­ sinh</th><th>CCCD / LiÃªn há»‡</th><th>Háº¡ng</th><th>Nguá»“n há»“ sÆ¡</th><th>Giáº¥y tá»</th><th>Há»“ sÆ¡</th><th>TÃ i khoáº£n</th><th style="text-align:center">Thao tÃ¡c</th></tr></thead>
                <tbody>
                    <c:forEach var="item" items="${registrants}">
                        <tr>
                            <td>#${item.user.id}</td>
                            <td><strong><c:out value="${item.profile.fullName}" /></strong><br><small>@<c:out value="${item.user.username}" /> Â· <c:out value="${item.user.email}" /></small></td>
                            <td><c:out value="${item.profile.govIdNo}" /><br><small><c:out value="${item.profile.phoneNo}" /></small></td>
                            <td><c:out value="${empty item.licenceDisplayClass ? 'â€”' : item.licenceDisplayClass}" /></td>
                            <td><c:out value="${item.sourceLabel}" /></td>
                            <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                            <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                            <td><span class="action-badge action-badge--${item.user.active ? 'success' : 'danger'}">${item.user.active ? 'Hoáº¡t Ä‘á»™ng' : 'ÄÃ£ khÃ³a'}</span></td>
                            <td>
                                <div style="display:flex;flex-wrap:wrap;gap:.4rem;justify-content:center">
                                    <a class="btn-export" href="${ctx}/manager/dossier-detail?id=${item.user.id}" style="padding:.35rem .6rem;text-decoration:none">Chi tiáº¿t</a>
                                    <c:if test="${item.reviewable}">
                                        <a class="btn-export" href="${ctx}/manager/dossiers?id=${item.registrationId}" style="padding:.35rem .6rem;text-decoration:none;color:#d97706">Duyá»‡t</a>
                                    </c:if>
                                    <form action="${ctx}/manager/registrants" method="post" style="margin:0" onsubmit="return confirm('${item.user.active ? 'KhÃ³a' : 'Má»Ÿ khÃ³a'} tÃ i khoáº£n nÃ y?');">
                                        <input type="hidden" name="id" value="${item.user.id}">
                                        <input type="hidden" name="action" value="${item.user.active ? 'lock' : 'activate'}">
                                        <button class="btn-export" type="submit" style="padding:.35rem .6rem;color:${item.user.active ? '#dc2626' : '#059669'}">${item.user.active ? 'KhÃ³a' : 'Má»Ÿ khÃ³a'}</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty registrants}">
                        <tr><td colspan="9" style="padding:3rem;text-align:center;color:#64748b">KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh phÃ¹ há»£p.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>
</body>
</html>

