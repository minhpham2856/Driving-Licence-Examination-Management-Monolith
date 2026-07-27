<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--search form, keeps sort/dir--%>
<form action="${pageUrl}" method="get" style="display:contents">
    <c:if test="${not empty sortBy}">
        <input type="hidden" name="sort" value="${sortBy}">
    </c:if>
    <c:if test="${not empty sortDir}">
        <input type="hidden" name="dir" value="${sortDir}">
    </c:if>
    <div class="search ${param.wide == 'true' ? 'search-wide' : ''}">
        <input type="text"
               name="q"
               class="search-input"
               value="${searchQuery}"
               placeholder="${not empty param.placeholder ? param.placeholder : 'Tìm kiếm...'}">
    </div>
    <button type="submit" class="btn blue">
        <span class="material-symbols-outlined">search</span>Tìm kiếm
    </button>
</form>
