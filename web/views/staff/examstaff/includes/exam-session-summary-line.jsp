<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Dòng tóm tắt kỳ thi: hạng — mã — ngày — giờ — trạng thái (dùng chung dashboard / phân bổ giám khảo). --%>
<p class="dashboard-sessions-panel__desc">
    Kỳ thi hạng <strong>${currentExam.licenseCode}</strong>
    <c:if test="${not empty currentExam.examCode}"> — <strong>${currentExam.examCode}</strong></c:if>
    — <fmt:formatDate value="${currentExam.examDate}" pattern="dd/MM/yyyy"/>.
    <c:choose>
        <c:when test="${not empty currentExam.scheduledStartAt}">
            Giờ bắt đầu <fmt:formatDate value="${currentExam.scheduledStartAt}" pattern="HH:mm"/>.
            <c:if test="${not empty currentExam.scheduledEndAt}">
                — kết thúc <fmt:formatDate value="${currentExam.scheduledEndAt}" pattern="HH:mm"/>.
            </c:if>
        </c:when>
        <c:when test="${not empty currentExam.shiftStartTime}">
            Khung giờ
            <fmt:formatDate value="${currentExam.shiftStartTime}" pattern="HH:mm"/>
            <c:if test="${not empty currentExam.shiftEndTime}">
                –<fmt:formatDate value="${currentExam.shiftEndTime}" pattern="HH:mm"/>.
            </c:if>
        </c:when>
    </c:choose>
    Trạng thái: <strong>${currentExam.status}</strong>.
    <c:if test="${param.showAllocatorLink eq 'true'}">
        <strong>${assignedExaminerUniqueCount}/${totalActiveExaminerCount}</strong> giám khảo đã phân công
        — <a href="examiner-allocation?examId=${sessionScope.selectedExamId}">Phân bổ giám khảo</a>.
    </c:if>
</p>
