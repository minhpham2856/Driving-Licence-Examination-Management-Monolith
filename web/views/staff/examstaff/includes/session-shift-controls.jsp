<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="shiftStatus" value="${param.status}" />
<c:set var="shiftCanStart" value="${shiftStatus eq 'Chưa diễn ra' or shiftStatus eq 'Mở' or shiftStatus eq 'Scheduled' or shiftStatus eq 'Open'}" />
<c:set var="shiftInProgress" value="${shiftStatus eq 'Đang diễn ra' or shiftStatus eq 'InProgress'}" />
<c:set var="sessionCanStartNow" value="${empty requestScope.sessionCanStartNow or requestScope.sessionCanStartNow}" />
<c:set var="startEnabled" value="${shiftCanStart and sessionCanStartNow}" />
<div class="session-shift-chip__actions">
    <form action="exam-control" method="POST" class="session-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Bắt đầu kỳ thi ${param.sessionName}? Giám khảo đã phân công sẽ có thể đăng nhập.');">
        <input type="hidden" name="action" value="startExam">
        <input type="hidden" name="examId" value="${param.examId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-filter session-shift-chip__btn" <c:if test="${not startEnabled}">disabled</c:if>>Bắt đầu</button>
    </form>
    <c:if test="${shiftCanStart and not sessionCanStartNow and not empty requestScope.sessionScheduledStartLabel}">
        <span class="es-text-muted-sm session-shift-chip__hint">Mở từ ${requestScope.sessionScheduledStartLabel}</span>
    </c:if>
    <form action="exam-control" method="POST" class="session-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Kết thúc kỳ thi ${param.sessionName}? Giám khảo sẽ không đăng nhập được kỳ này nữa.');">
        <input type="hidden" name="action" value="endExam">
        <input type="hidden" name="examId" value="${param.examId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-export session-shift-chip__btn session-shift-chip__btn--end" <c:if test="${not shiftInProgress}">disabled</c:if>>Kết thúc</button>
    </form>
</div>
