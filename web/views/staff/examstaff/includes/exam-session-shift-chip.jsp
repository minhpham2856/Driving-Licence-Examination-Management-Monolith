<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="dashboard-sessions-panel__chips">
    <div class="session-shift-chip">
        <span class="session-shift-chip__meta">
            <strong>${currentSession.sessionName}</strong>
            <c:if test="${not empty currentSession.examTypeName}">
                <span class="es-text-muted-sm"> — ${currentSession.examTypeName}</span>
            </c:if>
        </span>
        <jsp:include page="/views/staff/examstaff/includes/session-shift-controls.jsp">
            <jsp:param name="sessionId" value="${currentSession.id}" />
            <jsp:param name="sessionName" value="${currentSession.sessionName}" />
            <jsp:param name="status" value="${currentSession.status}" />
            <jsp:param name="redirect" value="${param.redirect}" />
        </jsp:include>
    </div>
</div>
