<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Dòng tóm tắt kỳ thi: hạng — mã — ngày — giờ — trạng thái (dùng chung dashboard / phân bổ giám khảo). --%>
<p class="dashboard-sessions-panel__desc">
    Kỳ thi hạng <strong>${currentSession.licenseCode}</strong>
    <c:if test="${not empty currentSession.examCode}"> — <strong>${currentSession.examCode}</strong></c:if>
    — <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/>.
    <c:choose>
        <c:when test="${not empty currentSession.scheduledStartAt}">
            Giờ bắt đầu <fmt:formatDate value="${currentSession.scheduledStartAt}" pattern="HH:mm"/>.
            <c:if test="${not empty currentSession.scheduledEndAt}">
                — kết thúc <fmt:formatDate value="${currentSession.scheduledEndAt}" pattern="HH:mm"/>.
            </c:if>
        </c:when>
        <c:when test="${not empty currentSession.shiftStartTime}">
            Khung giờ
            <fmt:formatDate value="${currentSession.shiftStartTime}" pattern="HH:mm"/>
            <c:if test="${not empty currentSession.shiftEndTime}">
                –<fmt:formatDate value="${currentSession.shiftEndTime}" pattern="HH:mm"/>.
            </c:if>
        </c:when>
    </c:choose>
    Trạng thái: <strong>${currentSession.status}</strong>.
    <c:if test="${param.showAllocatorLink eq 'true'}">
        <strong>${assignedExaminerUniqueCount}/${totalActiveExaminerCount}</strong> giám khảo đã phân công
        — <a href="examiner-allocation?sessionId=${sessionScope.selectedSessionId}">Phân bổ giám khảo</a>.
    </c:if>
</p>
