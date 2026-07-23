<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="u" value="${requestScope.accountUser}" />
<c:set var="p" value="${requestScope.accountProfile}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />
<c:set var="useExaminerShell" value="${requestScope.accountShell eq 'examiner'}" />
<c:set var="useAdminShell" value="${requestScope.accountShell eq 'admin'}" />
<c:set var="usePoliceShell" value="${requestScope.accountShell eq 'police'}" />
<c:set var="headerTitle" value="Hồ sơ cá nhân" scope="request" />
<c:set var="accountCssVer" value="20260714d" />

<c:choose>
    <c:when test="${useAdminShell}">
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
        <title>Hồ sơ cá nhân - Lái Vui</title>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
    </head>
    <body class="has-side-nav-bar">
        <jsp:include page="/views/layout/sidebar-admin.jsp">
            <jsp:param name="activeSidebar" value="ho-so" />
        </jsp:include>
        <div class="dashboard-shell">
            <main class="main-content">
                <div class="account-page account-page--portal account-page--profile">
</c:when>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="ho-so" />
            <jsp:param name="pageTitle" value="Hồ sơ cá nhân" />
            <jsp:param name="noCache" value="true" />
            <jsp:param name="mainClass" value="examstaff-main--scroll" />
            <jsp:param name="resolveQueue" value="false" />
            <jsp:param name="pageCss" value="account.css" />
        </jsp:include>
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        <div class="account-page account-page--portal account-page--profile">
    </c:when>
    <c:when test="${useExaminerShell}">
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
            <title>Hồ sơ cá nhân - SÁT HẠCH</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
            <jsp:include page="/views/examiner/components/examiner-styles.jsp" />
            <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        </head>
        <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">
            <jsp:include page="/views/layout/sidebar-examiner.jsp">
                <jsp:param name="activeSidebar" value="ho-so" />
            </jsp:include>
            <div class="examiner-shell">
                <jsp:include page="/views/layout/header-examiner.jsp" />
                <main class="examiner-main examiner-main--scroll">
                    <div class="account-page account-page--portal account-page--profile">
    </c:when>
    <c:when test="${usePoliceShell}">
        <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Hồ sơ cán bộ CSGT</title><link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css"><link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}"></head><body class="has-side-nav-bar"><jsp:include page="/views/layout/sidebar-policestaff.jsp"><jsp:param name="activeSidebar" value="profile"/></jsp:include><div class="dashboard-shell"><main class="main-content"><div class="account-page account-page--portal account-page--profile">
    </c:when>
    <c:otherwise>
        <jsp:include page="/views/layout/header.jsp">
            <jsp:param name="title" value="Lái Vui - Hồ sơ cá nhân" />
        </jsp:include>
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        <main class="account-page account-page--public account-page--profile">
    </c:otherwise>
</c:choose>

            <div class="account-shell account-shell--profile">
                <header class="account-hero">
                    <p class="account-hero__eyebrow">Tài khoản làm việc</p>
                    <p class="account-hero__sub">Cập nhật thông tin liên hệ và giấy tờ để dùng khi điều hành kỳ thi.</p>
                </header>

                <c:if test="${not empty message}">
                    <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}" role="status">
                        <span class="material-symbols-outlined" aria-hidden="true">
                            ${messageType eq 'success' ? 'check_circle' : 'error'}
                        </span>
                        <span><c:out value="${message}" /></span>
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty p}">
                        <section class="account-panel">
                            <p class="account-empty">Chưa có hồ sơ cá nhân gắn với tài khoản. Liên hệ quản trị viên để được hỗ trợ.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <c:set var="displayName" value="${not empty p.fullName ? p.fullName : u.username}" />
                        <c:set var="nameParts" value="${fn:split(fn:trim(displayName), ' ')}" />
                        <c:set var="partCount" value="${fn:length(nameParts)}" />
                        <c:choose>
                            <c:when test="${partCount ge 2}">
                                <c:set var="pA" value="${nameParts[partCount - 2]}" />
                                <c:set var="pB" value="${nameParts[partCount - 1]}" />
                                <c:set var="initials" value="${fn:toUpperCase(fn:substring(pA, 0, 1))}${fn:toUpperCase(fn:substring(pB, 0, 1))}" />
                            </c:when>
                            <c:when test="${partCount eq 1 and fn:length(nameParts[0]) gt 0}">
                                <c:set var="initials" value="${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="initials" value="?" />
                            </c:otherwise>
                        </c:choose>

                        <section class="account-identity" aria-label="Tóm tắt hồ sơ">
                            <div class="account-identity__avatar" aria-hidden="true">${initials}</div>
                            <div class="account-identity__body">
                                <h2 class="account-identity__name"><c:out value="${displayName}" /></h2>
                                <p class="account-identity__meta">
                                    <span class="account-identity__chip">
                                        <span class="material-symbols-outlined" aria-hidden="true">person</span>
                                        <c:out value="${u.username}" />
                                    </span>
                                    <c:if test="${not empty u.email}">
                                        <span class="account-identity__chip">
                                            <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                            <c:out value="${u.email}" />
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty p.phoneNumber}">
                                        <span class="account-identity__chip">
                                            <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                            <c:out value="${p.phoneNumber}" />
                                        </span>
                                    </c:if>
                                </p>
                            </div>
                            <c:if test="${not empty accountChangePasswordPath}">
                                <a href="${ctx}${accountChangePasswordPath}" class="account-btn account-btn--ghost account-identity__action">
                                    <span class="material-symbols-outlined" aria-hidden="true">key</span>
                                    Đổi mật khẩu
                                </a>
                            </c:if>
                        </section>

                        <form method="post" action="${ctx}${accountProfilePath}" class="account-stack" novalidate>
                            <section class="account-panel" aria-label="Thông tin tài khoản">
                                <div class="account-panel__head">
                                    <span class="account-panel__icon material-symbols-outlined" aria-hidden="true">manage_accounts</span>
                                    <div>
                                        <h2 class="account-panel__title">Tài khoản</h2>
                                        <p class="account-panel__desc">Tên đăng nhập và email dùng để đăng nhập hệ thống.</p>
                                    </div>
                                </div>
                                <div class="account-grid">
                                    <div class="account-field">
                                        <label class="account-field__label" for="username">Tên đăng nhập</label>
                                        <input class="account-input" type="text" id="username" name="username"
                                               required minlength="3" maxlength="50"
                                               pattern="[A-Za-z0-9._-]+"
                                               value="${fn:escapeXml(u.username)}">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="email">Email</label>
                                        <input class="account-input" type="email" id="email" name="email"
                                               required value="${fn:escapeXml(u.email)}">
                                    </div>
                                </div>
                            </section>

                            <section class="account-panel" aria-label="Thông tin cá nhân">
                                <div class="account-panel__head">
                                    <span class="account-panel__icon material-symbols-outlined" aria-hidden="true">badge</span>
                                    <div>
                                        <h2 class="account-panel__title">Thông tin cá nhân</h2>
                                        <p class="account-panel__desc">Họ tên, ngày sinh và giấy tờ tùy thân.</p>
                                    </div>
                                </div>
                                <div class="account-grid">
                                    <div class="account-field">
                                        <label class="account-field__label" for="fullName">Họ và tên</label>
                                        <input class="account-input" type="text" id="fullName" name="fullName" required
                                               value="${fn:escapeXml(p.fullName)}">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="dateOfBirth">Ngày sinh</label>
                                        <input class="account-input" type="date" id="dateOfBirth" name="dateOfBirth"
                                               value="<fmt:formatDate value='${p.dateOfBirth}' pattern='yyyy-MM-dd' />">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="sex">Giới tính</label>
                                        <select class="account-select" id="sex" name="sex">
                                            <option value="1" <c:if test="${p.sex}">selected="selected"</c:if>>Nam</option>
                                            <option value="0" <c:if test="${not p.sex}">selected="selected"</c:if>>Nữ</option>
                                        </select>
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="phoneNumber">Số điện thoại</label>
                                        <input class="account-input" type="tel" id="phoneNumber" name="phoneNumber"
                                               required pattern="0[0-9]{9,10}"
                                               value="${fn:escapeXml(p.phoneNumber)}">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="governmentIdNumber">Số căn cước</label>
                                        <input class="account-input" type="text" id="governmentIdNumber"
                                               name="governmentIdNumber" required pattern="\d{12}" maxlength="12"
                                               inputmode="numeric"
                                               value="${fn:escapeXml(p.governmentIdNumber)}">
                                    </div>
                                    <div class="account-field account-grid__full">
                                        <label class="account-field__label" for="address">Địa chỉ</label>
                                        <input class="account-input" type="text" id="address" name="address"
                                               value="${fn:escapeXml(p.address)}">
                                    </div>
                                </div>
                                <div class="account-actions">
                                    <button type="submit" class="account-btn account-btn--primary">
                                        <span class="material-symbols-outlined" aria-hidden="true">save</span>
                                        Lưu thay đổi
                                    </button>
                                </div>
                            </section>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>

<c:choose>
    <c:when test="${useAdminShell}">
                </div>
            </main>
        </div>
    </body>
    </html>
</c:when>
    <c:when test="${useExamstaffShell}">
        </div>
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />
    </c:when>
    <c:when test="${useExaminerShell}">
                    </div>
                </main>
            </div>
        </body>
        </html>
    </c:when>
    <c:when test="${usePoliceShell}">
                    </div></main><jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false"/></jsp:include></div></body></html>
    </c:when>
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>
