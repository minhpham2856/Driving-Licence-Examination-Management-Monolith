<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<header class="examiner-header">
    <div class="examiner-header__title examiner-header__crumb-row">
        <c:choose>
            <c:when test="${empty headerBreadcrumbItems}">
                <span class="examiner-header__crumb-primary">Sát hạch viên</span>
            </c:when>
            <c:when test="${fn:length(headerBreadcrumbItems) == 1}">
                <a href="${headerBreadcrumbItems[0].href}" class="examiner-header__crumb-primary">${headerBreadcrumbItems[0].label}</a>
            </c:when>
            <c:otherwise>
                <c:forEach items="${headerBreadcrumbItems}" var="crumb" varStatus="st">
                    <c:if test="${not st.first}">
                        <span class="examiner-header__crumb-sep">&gt;</span>
                    </c:if>
                    <c:choose>
                        <c:when test="${crumb.primary}">
                            <a href="${crumb.href}" class="examiner-header__crumb-primary">${crumb.label}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${crumb.href}" class="examiner-header__crumb-child">${crumb.label}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="examiner-header__meta">
        <c:choose>
            <c:when test="${examinerHasActiveSession}">
                <span class="examiner-header__meta-label">Phần thi</span>
                <span class="examiner-tag examiner-tag--done">${examSectionName}</span>
            </c:when>
            <c:otherwise>
                <span class="examiner-tag examiner-tag--pending">Chưa có ca</span>
                <span class="examiner-header__meta-hint" title="${examinerSessionMessage}">${examinerSessionMessage}</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<c:if test="${not empty headerBreadcrumb}">
<script>document.title = '<c:out value="${headerBreadcrumb}" /> - SÁT HẠCH';</script>
</c:if>
