<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<header class="header">
    <div class="header-title crumb-row">
        <span class="crumb-primary">
            <c:out value="${not empty param.title ? param.title : 'Sát hạch viên'}" />
        </span>
    </div>
    <div class="header-meta">
        <c:choose>
            <c:when test="${examinerHasActiveExam}">
                <span class="header-meta-label">Phần thi</span>
                <span class="tag tag-done">${examSectionName}</span>
            </c:when>
            <c:otherwise>
                <span class="tag pending">Chưa có ca</span>
                <span class="header-meta-hint" title="${examinerExamMessage}">${examinerExamMessage}</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<c:if test="${not empty param.title}">
<script>document.title = '<c:out value="${param.title}" /> - SÁT HẠCH';</script>
</c:if>
