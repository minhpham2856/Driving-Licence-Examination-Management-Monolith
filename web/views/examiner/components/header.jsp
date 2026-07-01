<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<header class="examiner-header">
    <div class="examiner-header__title examiner-header__crumb-row">
        <span class="examiner-header__crumb-primary">
            <c:out value="${not empty headerTitle ? headerTitle : 'Sát hạch viên'}" />
        </span>
    </div>
    <div class="examiner-header__meta">
        <c:choose>
            <c:when test="${examinerHasActiveExam}">
                <span class="examiner-header__meta-label">Phần thi</span>
                <span class="examiner-tag examiner-tag--done">${examSectionName}</span>
            </c:when>
            <c:otherwise>
                <span class="examiner-tag examiner-tag--pending">Chưa có ca</span>
                <span class="examiner-header__meta-hint" title="${examinerExamMessage}">${examinerExamMessage}</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<c:if test="${not empty headerTitle}">
<script>document.title = '<c:out value="${headerTitle}" /> - SÁT HẠCH';</script>
</c:if>
