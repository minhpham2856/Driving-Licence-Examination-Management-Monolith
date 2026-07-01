<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty sortBy and sortBy ne 'sbd'}">
    <input type="hidden" name="sort" value="${sortBy}">
</c:if>
<c:if test="${not empty sortDir and sortDir ne 'asc'}">
    <input type="hidden" name="dir" value="${sortDir}">
</c:if>
<c:if test="${not empty allocationAreaFilter}">
    <input type="hidden" name="areaFilter" value="${allocationAreaFilter eq -1 ? 'none' : allocationAreaFilter}">
</c:if>
