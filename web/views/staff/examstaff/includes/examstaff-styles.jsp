<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssEx" value="${ctx}/assets/css/examiner" />
<c:set var="cssEs" value="${ctx}/assets/css/staff" />

<link rel="stylesheet" href="${ctx}/assets/css/layout.css">
<link rel="stylesheet" href="${ctx}/assets/css/style.css">
<link rel="stylesheet" href="${cssEx}/base.css">
<link rel="stylesheet" href="${cssEx}/sidebar.css">
<link rel="stylesheet" href="${cssEx}/header.css">
<link rel="stylesheet" href="${cssEx}/toolbar.css">
<link rel="stylesheet" href="${cssEx}/buttons.css">
<link rel="stylesheet" href="${cssEx}/card.css">
<link rel="stylesheet" href="${cssEx}/table.css">
<link rel="stylesheet" href="${cssEx}/icons.css">
<link rel="stylesheet" href="${cssEs}/examstaff.css">

<c:if test="${not empty param.pageCss}">
    <c:forTokens items="${param.pageCss}" delims="," var="cssFile">
        <c:if test="${not empty fn:trim(cssFile)}">
            <link rel="stylesheet" href="${cssEx}/${fn:trim(cssFile)}">
        </c:if>
    </c:forTokens>
</c:if>
