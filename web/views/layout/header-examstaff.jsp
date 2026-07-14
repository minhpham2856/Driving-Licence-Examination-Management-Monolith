<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="examiner-header examiner-header--examstaff">
    <div class="examiner-header__title examiner-header__crumb-row">
        <c:url var="dashboardUrl" value="/views/staff/examstaff/dashboard">
            <c:if test="${not empty param.examId}"><c:param name="examId" value="${param.examId}" /></c:if>
            <c:if test="${empty param.examId and not empty requestScope.selectedExamId}">
                <c:param name="examId" value="${requestScope.selectedExamId}" />
            </c:if>
        </c:url>
        <c:choose>
            <c:when test="${not empty param.sectionTitle}">
                <c:choose>
                    <c:when test="${not empty param.sectionUrl}">
                        <a href="${param.sectionUrl}" class="examiner-header__crumb-primary"><c:out value="${param.sectionTitle}" /></a>
                    </c:when>
                    <c:otherwise>
                        <span class="examiner-header__crumb-primary examiner-header__crumb-current"><c:out value="${param.sectionTitle}" /></span>
                    </c:otherwise>
                </c:choose>
                <c:if test="${not empty param.pageTitle and param.pageTitle ne param.sectionTitle}">
                    <span class="examiner-header__crumb-sep" aria-hidden="true">&rsaquo;</span>
                    <span class="examiner-header__crumb-child examiner-header__crumb-current"><c:out value="${param.pageTitle}" /></span>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${not empty param.pageTitle}">
                        <span class="examiner-header__crumb-primary examiner-header__crumb-current"><c:out value="${param.pageTitle}" /></span>
                    </c:when>
                    <c:otherwise>
                        <a href="${dashboardUrl}" class="examiner-header__crumb-primary examiner-header__crumb-current">Tổng quan</a>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="examiner-header__meta examiner-header__exam">
        <c:choose>
            <c:when test="${not empty requestScope.currentExam}">
                <span class="examiner-header__exam-license">
                    Hạng <c:out value="${requestScope.currentExam.licenseCode}" default="—" />
                </span>
                <c:if test="${not empty requestScope.currentExam.examDate}">
                    <span class="examiner-header__exam-date">
                        <fmt:formatDate value="${requestScope.currentExam.examDate}" pattern="dd/MM/yyyy" />
                    </span>
                </c:if>
            </c:when>
            <c:otherwise>
                <span class="examiner-tag examiner-tag--pending">Chưa chọn kỳ thi</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<c:if test="${not empty param.pageTitle}">
<script>document.title = '<c:out value="${param.pageTitle}" />';</script>
</c:if>
