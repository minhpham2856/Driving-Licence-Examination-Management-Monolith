<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />
<c:set var="useExaminerShell" value="${requestScope.accountShell eq 'examiner'}" />
<c:set var="useAdminShell" value="${requestScope.accountShell eq 'admin'}" />
<c:set var="headerTitle" value="Đổi mật khẩu" scope="request" />
<c:set var="accountCssVer" value="20260714d" />

<c:choose>
    <c:when test="${useAdminShell}">
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
        <title>Đổi mật khẩu - Lái Vui</title>
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
                <div class="account-page account-page--portal account-page--password">
</c:when>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="doi-mat-khau" />
            <jsp:param name="pageTitle" value="Đổi mật khẩu" />
            <jsp:param name="noCache" value="true" />
            <jsp:param name="mainClass" value="examstaff-main--scroll" />
            <jsp:param name="resolveQueue" value="false" />
            <jsp:param name="pageCss" value="account.css" />
        </jsp:include>
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        <div class="account-page account-page--portal account-page--password">
    </c:when>
    <c:when test="${useExaminerShell}">
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
            <title>Đổi mật khẩu - SÁT HẠCH</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
            <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
            <jsp:include page="/views/examiner/components/examiner-styles.jsp" />
            <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        </head>
        <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">
            <jsp:include page="/views/layout/sidebar-examiner.jsp">
                <jsp:param name="activeSidebar" value="doi-mat-khau" />
            </jsp:include>
            <div class="examiner-shell">
                <jsp:include page="/views/layout/header-examiner.jsp" />
                <main class="examiner-main examiner-main--scroll">
                    <div class="account-page account-page--portal account-page--password">
    </c:when>
    <c:otherwise>
        <jsp:include page="/views/layout/header.jsp">
            <jsp:param name="title" value="Lái Vui - Đổi mật khẩu" />
        </jsp:include>
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        <main class="account-page account-page--public account-page--password">
    </c:otherwise>
</c:choose>

            <div class="account-shell account-shell--narrow">
                <header class="account-hero">
                    <p class="account-hero__eyebrow">Bảo mật tài khoản</p>
                    <p class="account-hero__sub">Nhập mật khẩu hiện tại và đặt mật khẩu mới để bảo vệ tài khoản làm việc.</p>
                </header>

                <c:if test="${not empty message}">
                    <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}" role="status">
                        <span class="material-symbols-outlined" aria-hidden="true">
                            ${messageType eq 'success' ? 'check_circle' : 'error'}
                        </span>
                        <span><c:out value="${message}" /></span>
                    </div>
                </c:if>

                <section class="account-panel account-panel--secure" aria-label="Form đổi mật khẩu">
                    <div class="account-panel__head">
                        <span class="account-panel__icon material-symbols-outlined" aria-hidden="true">lock</span>
                        <div>
                            <h2 class="account-panel__title">Thông tin mật khẩu</h2>
                            <p class="account-panel__desc">Mật khẩu mới tối thiểu 6 ký tự, khác mật khẩu hiện tại.</p>
                        </div>
                    </div>
                    <form method="post" action="${ctx}${accountChangePasswordPath}" class="account-stack" autocomplete="off">
                        <div class="account-field">
                            <label class="account-field__label" for="currentPassword">Mật khẩu hiện tại</label>
                            <input class="account-input" type="password" id="currentPassword" name="currentPassword"
                                   required autocomplete="current-password">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="newPassword">Mật khẩu mới</label>
                            <input class="account-input" type="password" id="newPassword" name="newPassword"
                                   required minlength="6" autocomplete="new-password">
                            <span class="account-field__hint">Tối thiểu 6 ký tự</span>
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="confirmPassword">Xác nhận mật khẩu mới</label>
                            <input class="account-input" type="password" id="confirmPassword" name="confirmPassword"
                                   required minlength="6" autocomplete="new-password">
                        </div>
                        <div class="account-actions">
                            <c:if test="${not empty accountProfilePath}">
                                <a href="${ctx}${accountProfilePath}" class="account-btn account-btn--ghost">
                                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                                    Về hồ sơ
                                </a>
                            </c:if>
                            <button type="submit" class="account-btn account-btn--primary">
                                <span class="material-symbols-outlined" aria-hidden="true">key</span>
                                Cập nhật mật khẩu
                            </button>
                        </div>
                    </form>
                </section>
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
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>
