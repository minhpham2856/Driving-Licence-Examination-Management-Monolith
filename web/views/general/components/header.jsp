<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--page title--%>
<c:set var="pageTitle" value="${param.title}" />
<%--case 1: empty title fallback--%>
<c:if test="${empty pageTitle}">
    <c:set var="pageTitle" value="Lái Vui" />
</c:if>

<%--context / logo--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

<%--active nav from param or uri--%>
<c:set var="activeNav" value="${param.activeNav}" />
<%--case 1: derive from uri when empty--%>
<c:if test="${empty activeNav}">
    <c:choose>
        <%--case 1: home--%>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'home')
                        or pageContext.request.requestURI eq '/'
                        or fn:endsWith(pageContext.request.requestURI, '/')}">
            <c:set var="activeNav" value="gioi-thieu" />
        </c:when>
        <%--case 2: process--%>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'process')}">
            <c:set var="activeNav" value="quy-trinh" />
        </c:when>
        <%--case 3: license grades / categories--%>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'license-grades')
                        or fn:contains(pageContext.request.requestURI, 'license-categories')}">
            <c:set var="activeNav" value="hang-bang" />
        </c:when>
        <%--case 4: none--%>
        <c:otherwise>
            <c:set var="activeNav" value="" />
        </c:otherwise>
    </c:choose>
</c:if>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle}</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap"
              rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap"
              rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    </head>

    <%--body class--%>
    <c:set var="bodyClass" value="${param.bodyClass}" />
    <%--case 1: default public-site--%>
    <c:if test="${empty bodyClass}">
        <c:set var="bodyClass" value="public-site" />
    </c:if>

    <body class="${bodyClass}">
        <header class="top-nav-bar" role="banner">
            <div class="top-nav-bar__container">
                <%--logo--%>
                <a href="${ctx}/home" class="top-nav-bar__logo">
                    <img src="${logoUrl}"
                         alt="Lái Vui"
                         width="54"
                         height="54"
                         class="top-nav-bar__logo-img">
                </a>

                <nav class="top-nav-bar__nav">
                    <a href="${ctx}/home"
                       class="top-nav-bar__link${activeNav eq 'gioi-thieu' ? ' is-active' : ''}">Giới thiệu</a>
                    <a href="${ctx}/process"
                       class="top-nav-bar__link${activeNav eq 'quy-trinh' ? ' is-active' : ''}">Quy trình</a>
                    <a href="${ctx}/license-categories"
                       class="top-nav-bar__link${activeNav eq 'hang-bang' ? ' is-active' : ''}">Hạng bằng</a>
                </nav>

                <div class="top-nav-bar__actions" data-node-id="1:1232">
                    <a href="${ctx}/login" class="top-nav-bar__btn-login">Đăng nhập</a>
                    <a href="${ctx}/register" class="top-nav-bar__btn-register">Đăng ký</a>
                </div>
            </div>
        </header>
