<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <c:if test="${param.noCache eq 'true'}">
        <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
        <meta http-equiv="Pragma" content="no-cache">
        <meta http-equiv="Expires" content="0">
    </c:if>
    <title><c:out value="${empty param.pageTitle ? 'Ban Sát Hạch' : param.pageTitle}" /></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <jsp:include page="/views/staff/examstaff/includes/examstaff-styles.jsp">
        <jsp:param name="pageCss" value="${param.pageCss}" />
    </jsp:include>
</head>
<body class="has-side-nav-bar examstaff-portal"<c:if test="${not empty param.dataAllocSession}"> data-alloc-session="<c:out value='${param.dataAllocSession}' />"</c:if><c:if test="${not empty param.dataAuditBase}"> data-audit-base="<c:out value='${ctx}${param.dataAuditBase}' />"</c:if><c:if test="${not empty param.dataAuditExportBase}"> data-audit-export-base="<c:out value='${ctx}${param.dataAuditExportBase}' />"</c:if><c:if test="${not empty param.bodyAttrs}"> <c:out value="${param.bodyAttrs}" escapeXml="false" /></c:if>>
<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="${param.activeSidebar}" />
    <jsp:param name="sessionId" value="${param.sessionId}" />
</jsp:include>
<c:if test="${param.resolveQueue ne 'false'}">
    <jsp:include page="/views/staff/examstaff/includes/resolve-candidate-queue.jsp" />
</c:if>
<div class="examstaff-shell">
    <jsp:include page="/views/layout/header-examstaff.jsp">
        <jsp:param name="pageTitle" value="${param.pageTitle}" />
        <jsp:param name="sectionTitle" value="${param.sectionTitle}" />
        <jsp:param name="sectionUrl" value="${param.sectionUrl}" />
        <jsp:param name="sessionId" value="${param.sessionId}" />
    </jsp:include>
    <main class="examstaff-main${not empty param.mainClass ? ' '.concat(param.mainClass) : ''}">
