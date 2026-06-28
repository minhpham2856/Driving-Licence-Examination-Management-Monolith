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
    <title>Dashboard Managing Staff - LÃ¡i Vui</title>
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
            <h1 class="page-title">Dashboard Quáº£n LÃ½</h1>
            <p class="page-subtitle">Tá»•ng quan tÃ i khoáº£n Registrant, há»“ sÆ¡, phiÃªn thi vÃ  thao tÃ¡c quáº£n lÃ½ tá»« database.</p>
        </div>
        <div class="page-actions" style="display:flex;gap:.75rem">
            <a class="btn-export" href="${ctx}/manager/dossiers" style="display:inline-flex;text-decoration:none">
                Duyá»‡t há»“ sÆ¡ (${reviewableCount})
            </a>
            <a class="btn-filter" href="${ctx}/manager/create-user" style="display:inline-flex;text-decoration:none">
                Táº¡o tÃ i khoáº£n &amp; há»“ sÆ¡
            </a>
        </div>
    </header>

    <section class="metrics-row">
        <div class="stat-card"><div class="stat-info"><span class="stat-number">${totalRegistrants}</span><span class="stat-label">Tá»•ng thÃ­ sinh</span><span class="stat-trend stat-trend--up">Registrant trong DB</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#d97706">${reviewableCount}</span><span class="stat-label">Há»“ sÆ¡ cáº§n xá»­ lÃ½</span><span class="stat-trend" style="color:#d97706;background:#fffbeb">ChÆ°a Approved</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#059669">${approvedCount}</span><span class="stat-label">Há»“ sÆ¡ Ä‘Ã£ duyá»‡t</span><span class="stat-trend stat-trend--up">ÄÆ°á»£c chá»n phiÃªn thi</span></div></div>
        <div class="stat-card"><div class="stat-info"><span class="stat-number" style="color:#dc2626">${lockedCount}</span><span class="stat-label">TÃ i khoáº£n Ä‘Ã£ khÃ³a</span><span class="stat-trend" style="color:#dc2626;background:#fef2f2">Cáº§n theo dÃµi</span></div></div>
    </section>

    <div class="report-grid" style="grid-template-columns:1.6fr 1fr;gap:1.5rem;margin-top:1.5rem">
        <div style="display:flex;flex-direction:column;gap:1.5rem">
            <section class="log-card">
                <header class="log-card-header">
                    <h2 class="log-card-title">Há»“ sÆ¡ cáº§n Managing Staff xá»­ lÃ½</h2>
                    <a href="${ctx}/manager/dossiers" style="text-decoration:none;color:#0052cc;font-weight:700">Xem táº¥t cáº£</a>
                </header>
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>MÃ£</th><th>ThÃ­ sinh</th><th>CCCD</th><th>Háº¡ng</th><th>Giáº¥y tá»</th><th>Tráº¡ng thÃ¡i</th><th></th></tr></thead>
                        <tbody>
                            <c:forEach var="item" items="${recentDossiers}">
                                <tr>
                                    <td>#${item.registrationId}</td>
                                    <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                    <td><c:out value="${item.profile.govIdNo}" /></td>
                                    <td><c:out value="${empty item.licenceDisplayClass ? 'â€”' : item.licenceDisplayClass}" /></td>
                                    <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                                    <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                                    <td><a class="btn-filter" href="${ctx}/manager/dossiers?id=${item.registrationId}" style="display:inline-flex;text-decoration:none">Xá»­ lÃ½</a></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentDossiers}">
                                <tr><td colspan="7" style="padding:2rem;text-align:center">KhÃ´ng cÃ³ há»“ sÆ¡ cáº§n xá»­ lÃ½.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="log-card">
                <header class="log-card-header">
                    <h2 class="log-card-title">PhiÃªn thi Ä‘ang cáº¥u hÃ¬nh trong há»‡ thá»‘ng</h2>
                    <span class="action-badge action-badge--${upcomingCount gt 0 ? 'success' : 'warning'}">
                        ${upcomingCount} phiÃªn chÆ°a quÃ¡ ngÃ y
                    </span>
                </header>
                <c:if test="${upcomingCount eq 0 and not empty activeSessions}">
                    <div class="p-alert-banner" style="margin:1rem;border-color:#f59e0b;color:#92400e">
                        CÃ¡c phiÃªn Ä‘ang mang tráº¡ng thÃ¡i hoáº¡t Ä‘á»™ng nhÆ°ng ngÃ y thi Ä‘Ã£ qua. Cáº§n cáº­p nháº­t lá»‹ch trÆ°á»›c khi má»Ÿ Ä‘Äƒng kÃ½ má»›i.
                    </div>
                </c:if>
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>PhiÃªn thi</th><th>Háº¡ng</th><th>NgÃ y giá»</th><th>Khu vá»±c</th><th>ThÃ­ sinh</th><th>Tráº¡ng thÃ¡i</th></tr></thead>
                        <tbody>
                            <c:forEach var="exam" items="${activeSessions}">
                                <tr>
                                    <td><strong><c:out value="${exam.sessionName}" /></strong></td>
                                    <td><c:out value="${exam.licenseCode}" /></td>
                                    <td><fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" /> Â· <fmt:formatDate value="${exam.shiftStartTime}" pattern="HH:mm" /></td>
                                    <td><c:out value="${empty exam.areaName ? 'ChÆ°a phÃ¢n khu' : exam.areaName}" /></td>
                                    <td>${exam.registeredCount}/${exam.maxCandidates}</td>
                                    <td><span class="action-badge action-badge--info"><c:out value="${exam.status}" /></span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty activeSessions}">
                                <tr><td colspan="6" style="padding:2rem;text-align:center">ChÆ°a cÃ³ phiÃªn thi hoáº¡t Ä‘á»™ng.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>

        <div style="display:flex;flex-direction:column;gap:1.5rem">
            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">PhÃ¢n bá»‘ theo háº¡ng GPLX</h2>
                <div style="display:flex;flex-direction:column;gap:1rem;margin-top:1.25rem">
                    <c:forEach var="entry" items="${licenceCounts}">
                        <div>
                            <div style="display:flex;justify-content:space-between;margin-bottom:.35rem">
                                <strong>Háº¡ng <c:out value="${entry.key}" /></strong><span>${entry.value} thÃ­ sinh</span>
                            </div>
                            <div style="height:8px;background:#e2e8f0;border-radius:999px;overflow:hidden">
                                <div style="height:100%;width:${totalRegistrants gt 0 ? entry.value * 100 / totalRegistrants : 0}%;background:#0052cc;border-radius:999px"></div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid #e2e8f0;color:#64748b">
                    ${completeCount}/${totalRegistrants} thÃ­ sinh Ä‘Ã£ cÃ³ Ä‘á»§ bá»‘n loáº¡i giáº¥y tá» chuáº©n.
                </div>
            </section>

            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">Thao tÃ¡c gáº§n Ä‘Ã¢y cá»§a báº¡n</h2>
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
                    <c:if test="${empty recentAudits}"><span style="color:#64748b">ChÆ°a cÃ³ thao tÃ¡c Ä‘Æ°á»£c ghi nháº­n.</span></c:if>
                </div>
            </section>

            <section class="report-pane" style="padding:1.5rem">
                <h2 class="log-card-title">Lá»‘i táº¯t Managing Staff</h2>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;margin-top:1rem">
                    <a class="btn-export" href="${ctx}/manager/registrants" style="padding:1rem;text-decoration:none;text-align:center">Quáº£n lÃ½ thÃ­ sinh</a>
                    <a class="btn-export" href="${ctx}/manager/dossier-detail" style="padding:1rem;text-decoration:none;text-align:center">Chi tiáº¿t há»“ sÆ¡</a>
                    <a class="btn-export" href="${ctx}/manager/dossiers" style="padding:1rem;text-decoration:none;text-align:center">Duyá»‡t há»“ sÆ¡</a>
                    <a class="btn-export" href="${ctx}/manager/create-user" style="padding:1rem;text-decoration:none;text-align:center">Tiáº¿p nháº­n há»“ sÆ¡</a>
                </div>
            </section>
        </div>
    </div>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>
</body>
</html>

