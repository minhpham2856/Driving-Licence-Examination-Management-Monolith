<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty requestScope.examSelectMsg}">
    <div class="allocation-alert allocation-alert--info"><span>${requestScope.examSelectMsg}</span></div>
</c:if>
<c:if test="${not empty requestScope.examSelectError}">
    <div class="allocation-alert allocation-alert--error"><span>${requestScope.examSelectError}</span></div>
</c:if>
<c:if test="${not empty requestScope.errorMsg}">
    <div class="allocation-alert allocation-alert--error"><span>${requestScope.errorMsg}</span></div>
</c:if>
<c:if test="${not empty requestScope.alertMsg}">
    <div class="allocation-alert allocation-alert--info"><span>${requestScope.alertMsg}</span></div>
</c:if>
