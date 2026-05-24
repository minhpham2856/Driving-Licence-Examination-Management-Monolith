<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

            <c:set var="pageTitle" value="${param.title}" />
            <c:if test="${empty pageTitle}">
                <c:set var="pageTitle" value="Lái Vui" />
            </c:if>

            <c:set var="ctx" value="${pageContext.request.contextPath}" />
            <c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

            <c:set var="activeNav" value="${param.activeNav}" />
            <c:if test="${empty activeNav}">
                <c:choose>
                    <c:when
                        test="${fn:contains(pageContext.request.requestURI, 'home.jsp') or fn:contains(pageContext.request.requestURI, 'about') or pageContext.request.requestURI eq '/' or fn:endsWith(pageContext.request.requestURI, '/')}">
                        <c:set var="activeNav" value="gioi-thieu" />
                    </c:when>
                    <c:when test="${fn:contains(pageContext.request.requestURI, 'process')}">
                        <c:set var="activeNav" value="quy-trinh" />
                    </c:when>
                    <c:when
                        test="${fn:contains(pageContext.request.requestURI, 'license-grades') or fn:contains(pageContext.request.requestURI, 'license-categories')}">
                        <c:set var="activeNav" value="hang-bang" />
                    </c:when>
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
                <link rel="stylesheet" href="${ctx}/assets/css/style.css">
                <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
            </head>
            <c:set var="bodyClass" value="${param.bodyClass}" />
            <c:if test="${empty bodyClass}">
                <c:set var="bodyClass" value="public-site" />
            </c:if>

            <body class="${bodyClass}">
                <header class="top-nav-bar" role="banner" data-node-id="1:1221">
                    <div class="top-nav-bar__container" data-node-id="1:1222">
                        <a href="home.jsp" class="top-nav-bar__logo" aria-label="Lái Vui - Trang chủ"
                            data-node-id="40:18">
                            <img src="${logoUrl}" alt="Lái Vui" width="54" height="54" class="top-nav-bar__logo-img">
                        </a>

                        <nav class="top-nav-bar__nav" aria-label="Điều hướng chính" data-node-id="1:1225">
                            <a href="home.jsp" class="top-nav-bar__link${activeNav eq 'gioi-thieu' ? ' is-active' : ''}"
                                data-node-id="1:1226" <c:if test="${activeNav eq 'gioi-thieu'}">aria-current="page"
                                </c:if>>
                                Giới thiệu
                            </a>
                            <a href="process.jsp"
                                class="top-nav-bar__link${activeNav eq 'quy-trinh' ? ' is-active' : ''}"
                                data-node-id="1:1228" <c:if test="${activeNav eq 'quy-trinh'}">aria-current="page"
                                </c:if>>
                                Quy trình
                            </a>
                            <a href="license-categories.jsp"
                                class="top-nav-bar__link${activeNav eq 'hang-bang' ? ' is-active' : ''}"
                                data-node-id="1:1230" <c:if test="${activeNav eq 'hang-bang'}">aria-current="page"
                                </c:if>>
                                Hạng bằng
                            </a>
                        </nav>

                        <div class="top-nav-bar__actions" data-node-id="1:1232">
                            <a href="login.jsp" class="top-nav-bar__btn-login" data-node-id="1:1233">
                                Đăng nhập
                            </a>
                            <a href="register.jsp" class="top-nav-bar__btn-register" data-node-id="1:1235">
                                Đăng ký
                            </a>
                        </div>
                    </div>
                </header>
