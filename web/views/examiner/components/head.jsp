<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssImports" value="${ctx}/assets/css/examiner" />

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>SÁT HẠCH</title>

<%-- google fonts --%>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">

<%--examiner css imports--%>
<link rel="stylesheet" href="${cssImports}/shell.css">
<link rel="stylesheet" href="${cssImports}/util.css">
<link rel="stylesheet" href="${cssImports}/base.css">
<link rel="stylesheet" href="${cssImports}/sidebar.css">
<link rel="stylesheet" href="${cssImports}/header.css">
<link rel="stylesheet" href="${cssImports}/toolbar.css">
<link rel="stylesheet" href="${cssImports}/buttons.css">
<link rel="stylesheet" href="${cssImports}/card.css">
<link rel="stylesheet" href="${cssImports}/table.css">
<link rel="stylesheet" href="${cssImports}/icons.css">

<%--optional css imports--%>
<c:if test="${not empty param.pageCss}">
    <c:forTokens items="${param.pageCss}" delims="," var="cssFile">
        <c:if test="${not empty fn:trim(cssFile)}">
            <link rel="stylesheet" href="${cssImports}/${fn:trim(cssFile)}">
        </c:if>
    </c:forTokens>
</c:if>
