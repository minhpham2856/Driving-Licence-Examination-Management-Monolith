<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="shiftStatus" value="${param.status}" />
<c:set var="shiftCanStart" value="${shiftStatus eq 'Chưa diễn ra' or shiftStatus eq 'Mở' or shiftStatus eq 'Scheduled' or shiftStatus eq 'Open'}" />
<c:set var="shiftInProgress" value="${shiftStatus eq 'Đang diễn ra' or shiftStatus eq 'InProgress'}" />
<c:set var="shiftPaused" value="${shiftStatus eq 'Tạm dừng' or shiftStatus eq 'Paused'}" />
<c:set var="callShiftEnded" value="${sessionScope.shiftEnded eq 'true'}" />
<c:set var="shiftCanEnd" value="${(shiftInProgress or shiftPaused) and callShiftEnded}" />
<c:set var="examCanStartNow" value="${empty requestScope.examCanStartNow or requestScope.examCanStartNow}" />
<c:set var="startEnabled" value="${shiftCanStart and examCanStartNow}" />
<div class="exam-shift-chip__actions">
    <form action="exam-control" method="POST" class="exam-shift-chip__form"
          onsubmit="return confirm('Tạo mật khẩu OTP 6 số mới cho máy thi kỳ ${param.examName}? Mật khẩu cũ sẽ bị thay thế.');">
        <input type="hidden" name="action" value="generateExamPassword">
        <input type="hidden" name="examId" value="${param.examId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-filter exam-shift-chip__btn">Tạo mật khẩu máy thi</button>
    </form>
    <form action="exam-control" method="POST" class="exam-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Bắt đầu kỳ thi ${param.examName}? Sát hạch viên đã phân công sẽ có thể đăng nhập.');">
        <input type="hidden" name="action" value="startExam">
        <input type="hidden" name="examId" value="${param.examId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-filter exam-shift-chip__btn" <c:if test="${not startEnabled}">disabled</c:if>>Bắt đầu</button>
    </form>
    <c:if test="${shiftCanStart and not examCanStartNow and not empty requestScope.examScheduledStartLabel}">
        <span class="es-text-muted-sm exam-shift-chip__hint">Mở từ ${requestScope.examScheduledStartLabel}</span>
    </c:if>

    <c:choose>
        <c:when test="${shiftPaused}">
            <form action="exam-control" method="POST" class="exam-shift-chip__form"
                  onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Tiếp tục kỳ thi ${param.examName}? Sát hạch viên có thể đăng nhập lại.');">
                <input type="hidden" name="action" value="resumeExam">
                <input type="hidden" name="examId" value="${param.examId}">
                <input type="hidden" name="redirect" value="${param.redirect}">
                <button type="submit" class="btn-filter exam-shift-chip__btn exam-shift-chip__btn--resume">Tiếp tục kỳ thi</button>
            </form>
        </c:when>
        <c:otherwise>
            <form action="exam-control" method="POST" class="exam-shift-chip__form"
                  onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Tạm dừng kỳ thi ${param.examName}? Hàng đợi gọi số giữ nguyên; sát hạch viên sẽ không đăng nhập được khi đang tạm dừng.');">
                <input type="hidden" name="action" value="pauseExam">
                <input type="hidden" name="examId" value="${param.examId}">
                <input type="hidden" name="redirect" value="${param.redirect}">
                <button type="submit" class="btn-export exam-shift-chip__btn exam-shift-chip__btn--pause" <c:if test="${not shiftInProgress}">disabled</c:if>>Tạm dừng kỳ thi</button>
            </form>
        </c:otherwise>
    </c:choose>

    <form action="exam-control" method="POST" class="exam-shift-chip__form"
          onsubmit="if (this.querySelector('button[type=submit]').disabled) return false; return confirm('Kết thúc kỳ thi ${param.examName}? Sát hạch viên sẽ không đăng nhập được kỳ này nữa.');">
        <input type="hidden" name="action" value="endExam">
        <input type="hidden" name="examId" value="${param.examId}">
        <input type="hidden" name="redirect" value="${param.redirect}">
        <button type="submit" class="btn-export exam-shift-chip__btn exam-shift-chip__btn--end"
                title="${not callShiftEnded ? 'Phải dừng gọi số trước khi kết thúc kỳ thi' : 'Kết thúc kỳ thi'}"
                <c:if test="${not shiftCanEnd}">disabled</c:if>>Kết thúc kỳ thi</button>
    </form>
    <c:if test="${(shiftInProgress or shiftPaused) and not callShiftEnded}">
        <span class="es-text-muted-sm exam-shift-chip__hint">Phải dừng gọi số trước khi kết thúc kỳ thi.</span>
    </c:if>
</div>
