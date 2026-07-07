<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssEx" value="${ctx}/assets/css/exam" />

<link rel="stylesheet" href="${ctx}/assets/css/style.css">

<c:if test="${not empty param.pageCss}">
    <c:forTokens items="${param.pageCss}" delims="," var="cssFile">
        <c:if test="${not empty fn:trim(cssFile)}">
            <link rel="stylesheet" href="${cssEx}/${fn:trim(cssFile)}">
        </c:if>
    </c:forTokens>
</c:if>
