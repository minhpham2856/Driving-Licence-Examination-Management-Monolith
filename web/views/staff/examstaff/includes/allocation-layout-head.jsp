<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="pageTitle" value="${empty param.pageTitle ? 'Phân bổ thí sinh' : param.pageTitle}" />

<c:set var="breadcrumbLabel" value="${empty param.breadcrumbLabel ? 'Phân bổ' : param.breadcrumbLabel}" />

<c:set var="layoutSessionId" value="${param.sessionId}" />
<c:if test="${empty layoutSessionId and not empty requestScope.examStaffLoadedSessionId}">
    <c:set var="layoutSessionId" value="${requestScope.examStaffLoadedSessionId}" />
</c:if>
<c:if test="${empty layoutSessionId and not empty sessionScope.selectedSessionId}">
    <c:set var="layoutSessionId" value="${sessionScope.selectedSessionId}" />
</c:if>

<c:set var="allocSessionMarker" value="${requestScope.examStaffLoadedSessionId}" />
<c:if test="${empty allocSessionMarker}">
    <c:set var="allocSessionMarker" value="${layoutSessionId}" />
</c:if>

<c:set var="layoutListPath" value="${not empty requestScope.allocationListPath ? requestScope.allocationListPath : pageContext.request.servletPath}" />

<c:url var="allocOverviewUrl" value="/views/staff/examstaff/allocation">
    <c:if test="${not empty layoutSessionId}"><c:param name="sessionId" value="${layoutSessionId}" /></c:if>
</c:url>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">

    <jsp:param name="activeSidebar" value="phan-bo" />

    <jsp:param name="pageTitle" value="${breadcrumbLabel}" />

    <jsp:param name="sectionTitle" value="Phân bổ thí sinh" />

    <jsp:param name="sectionUrl" value="${allocOverviewUrl}" />

    <jsp:param name="noCache" value="true" />

    <jsp:param name="mainClass" value="examstaff-main--scroll" />

    <jsp:param name="sessionId" value="${layoutSessionId}" />

    <jsp:param name="dataAllocSession" value="${allocSessionMarker}" />

</jsp:include>

<header class="page-header page-header--toolbar">
    <c:if test="${param.showSearch ne 'true'}">
        <p class="examiner-page-desc"><c:out value="${pageTitle}" /></p>
    </c:if>
    <c:if test="${param.showSearch eq 'true'}">
    <div class="page-actions allocation-page-actions">
        <form method="get" action="${ctx}${layoutListPath}" class="allocation-search-form" id="allocationSearchForm">
                <c:if test="${not empty layoutSessionId}"><input type="hidden" name="sessionId" value="${layoutSessionId}"></c:if>
            <div class="allocation-search-row">
                <div class="es-search-box">
                    <input type="text" name="q" id="candidateSearch" class="es-search-box__input"
                           value="${allocationSearchQuery}" placeholder="Tìm SBD, họ tên, CCCD…">
                </div>
                <button type="submit" class="allocation-search-btn" aria-label="Tìm kiếm">
                    <span class="material-symbols-outlined" aria-hidden="true">search</span>
                </button>
            </div>
        </form>
    </div>
    </c:if>
</header>

<jsp:include page="/views/staff/examstaff/includes/allocation-alerts.jsp" />
