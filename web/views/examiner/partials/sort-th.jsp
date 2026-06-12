<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="col" value="${param.sortColumn}" />
<c:set var="label" value="${param.label}" />
<c:set var="nextDir" value="${sortBy == col and sortDir == 'asc' ? 'desc' : 'asc'}" />
<c:url var="sortUrl" value="${pageUrl}">
    <c:param name="sort" value="${col}" />
    <c:param name="dir" value="${nextDir}" />
    <c:if test="${not empty searchQuery}">
        <c:param name="q" value="${searchQuery}" />
    </c:if>
</c:url>

<c:set var="thClass" value="" />
<c:if test="${param.center == 'true'}"><c:set var="thClass" value="${thClass} examiner-table__center" /></c:if>

    <th<c:if test="${not empty thClass}"> class="${fn:trim(thClass)}"</c:if>>
    <a href="${sortUrl}" class="examiner-sort-link
       <c:if test="${sortBy == col}"> examiner-sort-link--active
           <c:if test="${sortDir == 'desc'}"> examiner-sort-link--desc</c:if>
       </c:if>">${label}
        <span class="material-symbols-outlined examiner-sort-link__icon" aria-hidden="true">arrow_upward</span>
    </a>
</th>
