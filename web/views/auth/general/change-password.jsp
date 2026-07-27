<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context / shell flags--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />
<c:set var="useExaminerShell" value="${requestScope.accountShell eq 'examiner'}" />
<c:set var="useAdminShell" value="${requestScope.accountShell eq 'admin'}" />
<c:set var="useManagingShell" value="${requestScope.accountShell eq 'managingstaff'}" />
<c:set var="usePoliceShell" value="${requestScope.accountShell eq 'police'}" />
<c:set var="headerTitle" value="Đổi mật khẩu" scope="request" />
<c:set var="accountCssVer" value="20260714d" />

<%--shell open--%>
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
    <c:when test="${useManagingShell}">
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
            <title>Đổi mật khẩu - Ban quản lý</title>
            <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
            <link rel="stylesheet" href="${ctx}/assets/css/style.css">
            <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
            <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}">
        </head>
        <body class="has-side-nav-bar">
            <jsp:include page="/views/layout/sidebar-managingstaff.jsp">
                <jsp:param name="activeSidebar" value="change-password" />
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
    <c:when test="${usePoliceShell}">
        <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Đổi mật khẩu - CSGT</title><link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css"><link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css?v=${accountCssVer}"></head><body class="has-side-nav-bar"><jsp:include page="/views/layout/sidebar-policestaff.jsp"><jsp:param name="activeSidebar" value="change-password"/></jsp:include><div class="dashboard-shell"><main class="main-content"><div class="account-page account-page--portal account-page--password">
    </c:when>
    <%--case 3: public shell--%>
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
    </body>
    </html>
</c:when>
    <c:when test="${useManagingShell}">
                    </div>
                </main>
                <jsp:include page="/views/layout/footer.jsp">
                    <jsp:param name="standalone" value="false" />
                </jsp:include>
            </div>
        </body>
        </html>
    </c:when>
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
    <%--case 3: public shell--%>
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>
