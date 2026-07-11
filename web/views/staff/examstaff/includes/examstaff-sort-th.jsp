<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="col" value="${param.sortColumn}" />
<c:set var="label" value="${param.label}" />
<c:set var="nextDir" value="${sortBy == col and sortDir == 'asc' ? 'desc' : 'asc'}" />
<c:set var="listPath" value="${empty callListPath ? allocationListPath : callListPath}" />
<c:url var="sortUrl" value="${listPath}">
    <c:param name="sort" value="${col}" />
    <c:param name="dir" value="${nextDir}" />
    <c:if test="${not empty allocationSearchQuery}"><c:param name="q" value="${allocationSearchQuery}" /></c:if>
    <c:if test="${not empty param.sessionId}"><c:param name="sessionId" value="${param.sessionId}" /></c:if>
    <c:if test="${empty param.sessionId and not empty requestScope.selectedSessionId}">
        <c:param name="sessionId" value="${requestScope.selectedSessionId}" />
    </c:if>
    <c:if test="${not empty allocationAreaFilter}">
        <c:param name="areaFilter" value="${allocationAreaFilter eq -1 ? 'none' : allocationAreaFilter}" />
    </c:if>
    <c:if test="${not empty pg and pg.page gt 1}"><c:param name="page" value="${pg.page}" /></c:if>
    <c:if test="${not empty param.view}"><c:param name="view" value="${param.view}" /></c:if>
</c:url>
<c:set var="thClass" value="${not empty param.thClass ? param.thClass : ''}" />
<c:if test="${param.center == 'true'}"><c:set var="thClass" value="${thClass} examiner-table__center" /></c:if>
<th<c:if test="${not empty fn:trim(thClass)}"> class="${fn:trim(thClass)}"</c:if>>
    <a href="${sortUrl}" class="examiner-sort-link${sortBy == col ? ' examiner-sort-link--active' : ''}${sortBy == col and sortDir == 'desc' ? ' examiner-sort-link--desc' : ''}">
        <c:out value="${label}" />
        <span class="material-symbols-outlined examiner-sort-link__icon" aria-hidden="true">arrow_upward</span>
    </a>
</th>
