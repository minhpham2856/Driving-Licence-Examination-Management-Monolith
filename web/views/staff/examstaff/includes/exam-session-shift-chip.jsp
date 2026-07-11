<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="dashboard-sessions-panel__chips">
    <div class="session-shift-chip">
        <span class="session-shift-chip__meta">
            <strong>${currentExam.sessionName}</strong>
            <c:if test="${not empty currentExam.examTypeName}">
                <span class="es-text-muted-sm"> — ${currentExam.examTypeName}</span>
            </c:if>
        </span>
        <jsp:include page="/views/staff/examstaff/includes/session-shift-controls.jsp">
            <jsp:param name="examId" value="${currentExam.id}" />
            <jsp:param name="sessionName" value="${currentExam.sessionName}" />
            <jsp:param name="status" value="${currentExam.status}" />
            <jsp:param name="redirect" value="${param.redirect}" />
        </jsp:include>
    </div>
</div>
