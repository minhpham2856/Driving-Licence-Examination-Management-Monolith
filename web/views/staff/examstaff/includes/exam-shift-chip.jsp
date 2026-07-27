<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="dashboard-exam-panel__chips">
    <div class="exam-kiosk-password" aria-label="Mật khẩu máy thi kiosk">
        <span class="exam-kiosk-password__label">Mật khẩu máy thi</span>
        <c:choose>
            <c:when test="${not empty currentExam.examPassword}">
                <strong class="exam-kiosk-password__value"><c:out value="${currentExam.examPassword}" /></strong>
            </c:when>
            <c:otherwise>
                <span class="exam-kiosk-password__empty">Chưa tạo — bấm &quot;Tạo mật khẩu máy thi&quot;</span>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="exam-shift-chip">
        <span class="exam-shift-chip__meta">
            <strong>${currentExam.examName}</strong>
            <c:if test="${not empty currentExam.examTypeName}">
                <span class="es-text-muted-sm"> - ${currentExam.examTypeName}</span>
            </c:if>
        </span>
        <jsp:include page="/views/staff/examstaff/includes/exam-shift-controls.jsp">
            <jsp:param name="examId" value="${currentExam.id}" />
            <jsp:param name="examName" value="${currentExam.examName}" />
            <jsp:param name="status" value="${currentExam.status}" />
            <jsp:param name="redirect" value="${param.redirect}" />
        </jsp:include>
    </div>
</div>
