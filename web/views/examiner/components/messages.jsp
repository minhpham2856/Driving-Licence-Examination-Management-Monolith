<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--flash bars from controller--%>
<div class="flash-wrap">
    
    <%--case 1: success--%>
    <c:if test="${not empty flashSuccess}">
        <p class="flash-bar green">
            <c:out value="${flashSuccess}"/>
        </p>
    </c:if>
        
    <%--case 2: error--%>
    <c:if test="${not empty flashError}">
        <p class="flash-bar red">
            <c:out value="${flashError}"/>
        </p>
    </c:if>
</div>
