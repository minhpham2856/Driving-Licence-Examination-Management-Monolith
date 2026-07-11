<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="examiner-header">
    <div class="examiner-header__title examiner-header__crumb-row">
        <c:url var="dashboardUrl" value="/views/staff/examstaff/dashboard">
            <c:if test="${not empty param.examId}"><c:param name="examId" value="${param.examId}" /></c:if>
            <c:if test="${empty param.examId and not empty requestScope.selectedExamId}">
                <c:param name="examId" value="${requestScope.selectedExamId}" />
            </c:if>
        </c:url>
        <a href="${dashboardUrl}" class="examiner-header__crumb-primary">Ban Sát Hạch</a>
        <c:if test="${not empty param.sectionTitle}">
            <span class="examiner-header__crumb-sep">&gt;</span>
            <c:choose>
                <c:when test="${not empty param.sectionUrl}">
                    <a href="${param.sectionUrl}" class="examiner-header__crumb-child"><c:out value="${param.sectionTitle}" /></a>
                </c:when>
                <c:otherwise>
                    <span class="examiner-header__crumb-child"><c:out value="${param.sectionTitle}" /></span>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${not empty param.pageTitle}">
            <span class="examiner-header__crumb-sep">&gt;</span>
            <span class="examiner-header__crumb-child"><c:out value="${param.pageTitle}" /></span>
        </c:if>
    </div>
    <div class="examiner-header__meta">
        <c:choose>
            <c:when test="${not empty requestScope.currentExam}">
                <span class="examiner-header__meta-label">Kỳ thi</span>
                <span class="examiner-tag examiner-tag--done">
                    Hạng <c:out value="${requestScope.currentExam.licenseCode}" default="—" />
                    <c:if test="${not empty requestScope.currentExam.examDate}">
                        — <fmt:formatDate value="${requestScope.currentExam.examDate}" pattern="dd/MM/yyyy" />
                    </c:if>
                </span>
            </c:when>
            <c:otherwise>
                <span class="examiner-tag examiner-tag--pending">Chưa chọn kỳ thi</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<c:if test="${not empty param.pageTitle}">
<script>document.title = '<c:out value="${param.pageTitle}" /> - Ban Sát Hạch';</script>
</c:if>
