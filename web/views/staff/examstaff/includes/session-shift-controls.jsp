<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="shiftStatus" value="${param.status}" />
<c:set var="shiftCanStart" value="${shiftStatus eq 'Chưa diễn ra' or shiftStatus eq 'Mở' or shiftStatus eq 'Scheduled' or shiftStatus eq 'Open'}" />
<c:set var="shiftInProgress" value="${shiftStatus eq 'Đang diễn ra' or shiftStatus eq 'InProgress'}" />
<div class="session-shift-chip__actions">
    <form action="session-control" method="POST" class="session-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Bắt đầu ca ${param.sessionName}?');">
        <input type="hidden" name="action" value="startSession">
        <input type="hidden" name="sessionId" value="${param.sessionId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-filter session-shift-chip__btn" <c:if test="${not shiftCanStart}">disabled</c:if>>Bắt đầu</button>
    </form>
    <form action="session-control" method="POST" class="session-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Kết thúc ca ${param.sessionName}?');">
        <input type="hidden" name="action" value="endSession">
        <input type="hidden" name="sessionId" value="${param.sessionId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-export session-shift-chip__btn session-shift-chip__btn--end" <c:if test="${not shiftInProgress}">disabled</c:if>>Kết thúc</button>
    </form>
</div>
