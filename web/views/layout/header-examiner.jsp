<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="headerTitle" value="${param.title}" />
<c:if test="${empty headerTitle}">
    <c:set var="headerTitle" value="Quản lý thi lý thuyết" />
</c:if>

<header class="examiner-header">
    <h1 class="examiner-header__title">${headerTitle}</h1>
</header>
