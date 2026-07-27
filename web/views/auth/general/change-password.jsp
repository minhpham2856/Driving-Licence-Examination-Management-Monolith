<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context / shell flags--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />
<c:set var="useExaminerShell" value="${requestScope.accountShell eq 'examiner'}" />
<c:set var="useManagingShell" value="${requestScope.accountShell eq 'managingstaff'}" />
<c:set var="usePoliceShell" value="${requestScope.accountShell eq 'police'}" />
<c:set var="useAdminShell" value="${requestScope.accountShell eq 'admin'}" />
<c:set var="useStaffPortalShell"
       value="${useManagingShell or usePoliceShell or useAdminShell}" />

<%--shell open--%>
<c:choose>
    <%--case 1: examstaff shell--%>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="doi-mat-khau" />
            <jsp:param name="pageTitle" value="Đổi mật khẩu" />
            <jsp:param name="noCache" value="true" />
            <jsp:param name="mainClass" value="examstaff-main--scroll" />
            <jsp:param name="resolveQueue" value="false" />
            <jsp:param name="pageCss" value="account.css" />
        </jsp:include>
        <link rel="stylesheet"
              href="${ctx}/assets/css/examstaff/account.css?v=20260714b">
        <style>
            body.examstaff-portal .examstaff-main > .account-page--portal {
                width: 100% !important;
                max-width: none !important;
                margin: 0 !important;
                align-self: stretch !important;
            }
        </style>
        <div class="account-page account-page--portal">
    </c:when>
    <%--case 2: examiner shell--%>
    <c:when test="${useExaminerShell}">
        <!DOCTYPE html>
        <html lang="vi">
            <head>
                <jsp:include page="/views/examiner/components/head.jsp">
                    <jsp:param name="title" value="Đổi mật khẩu - SÁT HẠCH" />
                </jsp:include>
                <meta http-equiv="Cache-Control"
                      content="no-cache, no-store, must-revalidate">
                <link rel="stylesheet"
                      href="${ctx}/assets/css/examstaff/account.css?v=20260714b">
                <style>
                    body.portal .main > .account-page--portal {
                        width: 100% !important;
                        max-width: none !important;
                        margin: 0 !important;
                        align-self: stretch !important;
                    }
                </style>
            </head>
            <body class="has-side-nav-bar
                  portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">
                <jsp:include page="/views/layout/sidebar-examiner.jsp">
                    <jsp:param name="activeSidebar" value="doi-mat-khau" />
                </jsp:include>
                <div class="shell">
                    <jsp:include page="/views/layout/header-examiner.jsp">
                        <jsp:param name="title" value="Đổi mật khẩu" />
                    </jsp:include>
                    <main class="main scroll">
                        <div class="account-page account-page--portal">
    </c:when>
    <%--case 3: managing / police / admin portal shell--%>
    <c:when test="${useStaffPortalShell}">
        <!DOCTYPE html>
        <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta http-equiv="Cache-Control"
                      content="no-cache, no-store, must-revalidate">
                <title>Đổi mật khẩu - Lái Vui</title>
                <link rel="stylesheet" href="${ctx}/assets/css/style.css">
                <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
                <link rel="stylesheet"
                      href="${ctx}/assets/css/examstaff/account.css?v=20260714b">
            </head>
            <body class="has-side-nav-bar">
                <c:choose>
                    <c:when test="${useManagingShell}">
                        <jsp:include page="/views/layout/sidebar-managingstaff.jsp">
                            <jsp:param name="activeSidebar" value="change-password" />
                        </jsp:include>
                    </c:when>
                    <c:when test="${usePoliceShell}">
                        <jsp:include page="/views/layout/sidebar-policestaff.jsp">
                            <jsp:param name="activeSidebar" value="change-password" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <jsp:include page="/views/layout/sidebar-admin.jsp">
                            <jsp:param name="activeSidebar" value="change-password" />
                        </jsp:include>
                    </c:otherwise>
                </c:choose>
                <div class="dashboard-shell">
                    <main class="main-content">
                        <div class="account-page account-page--portal">
    </c:when>
    <%--case 4: public shell--%>
    <c:otherwise>
        <jsp:include page="/views/layout/header.jsp">
            <jsp:param name="title" value="Lái Vui - Đổi mật khẩu" />
        </jsp:include>
        <link rel="stylesheet"
              href="${ctx}/assets/css/examstaff/account.css?v=20260714b">
        <main class="account-page account-page--public">
    </c:otherwise>
</c:choose>

<%--shared form body--%>
<div class="account-shell">
    <%--flash message--%>
    <c:if test="${not empty message}">
        <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}"
             role="status">
            <span class="material-symbols-outlined" aria-hidden="true">
                ${messageType eq 'success' ? 'check_circle' : 'error'}
            </span>
            <span><c:out value="${message}" /></span>
        </div>
    </c:if>

    <section class="account-panel" aria-label="Form đổi mật khẩu">
        <%--<h2 class="account-panel__title">Bảo mật tài khoản</h2>--%>
        <form method="post"
              action="${ctx}${accountChangePasswordPath}"
              class="account-stack"
              autocomplete="off">
            <div class="account-field">
                <label class="account-field__label" for="currentPassword">
                    Mật khẩu hiện tại
                </label>
                <input class="account-input"
                       type="password"
                       id="currentPassword"
                       name="currentPassword"
                       required
                       autocomplete="current-password">
            </div>
            <div class="account-field">
                <label class="account-field__label" for="newPassword">
                    Mật khẩu mới
                </label>
                <input class="account-input"
                       type="password"
                       id="newPassword"
                       name="newPassword"
                       required
                       minlength="8"
                       autocomplete="new-password">
                <span class="account-field__hint">
                    Tối thiểu 8 ký tự, gồm chữ hoa, số và ký tự đặc biệt
                </span>
            </div>
            <div class="account-field">
                <label class="account-field__label" for="confirmPassword">
                    Xác nhận mật khẩu mới
                </label>
                <input class="account-input"
                       type="password"
                       id="confirmPassword"
                       name="confirmPassword"
                       required
                       minlength="8"
                       autocomplete="new-password">
            </div>
            <div class="account-actions">
                <button type="submit" class="account-btn account-btn--primary">
                    <span class="material-symbols-outlined" aria-hidden="true">key</span>
                    Cập nhật mật khẩu
                </button>
            </div>
        </form>
    </section>
</div>

<%--shell close--%>
<c:choose>
    <%--case 1: examstaff shell--%>
    <c:when test="${useExamstaffShell}">
        </div>
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />
    </c:when>
    <%--case 2: examiner shell--%>
    <c:when test="${useExaminerShell}">
                        </div>
                    </main>
                </div>
            </body>
        </html>
    </c:when>
    <%--case 3: managing / police / admin portal shell--%>
    <c:when test="${useStaffPortalShell}">
                        </div>
                    </main>
                    <jsp:include page="/views/layout/footer.jsp">
                        <jsp:param name="standalone" value="false" />
                    </jsp:include>
                </div>
            </body>
        </html>
    </c:when>
    <%--case 4: public shell--%>
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>
