<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="pageTitle" value="${empty param.pageTitle ? 'Phân bổ thí sinh' : param.pageTitle}" />

<c:set var="breadcrumbLabel" value="${empty param.breadcrumbLabel ? 'Phân bổ' : param.breadcrumbLabel}" />

<c:set var="layoutExamId" value="${param.examId}" />
<c:if test="${empty layoutExamId and not empty requestScope.examStaffLoadedExamId}">
    <c:set var="layoutExamId" value="${requestScope.examStaffLoadedExamId}" />
</c:if>
<c:if test="${empty layoutExamId and not empty sessionScope.selectedExamId}">
    <c:set var="layoutExamId" value="${sessionScope.selectedExamId}" />
</c:if>

<c:set var="allocExamMarker" value="${requestScope.examStaffLoadedExamId}" />
<c:if test="${empty allocExamMarker}">
    <c:set var="allocExamMarker" value="${layoutExamId}" />
</c:if>

<c:set var="layoutListPath" value="${not empty requestScope.allocationListPath ? requestScope.allocationListPath : pageContext.request.servletPath}" />

<c:url var="allocOverviewUrl" value="/examstaff/allocation">
    <c:if test="${not empty layoutExamId}"><c:param name="examId" value="${layoutExamId}" /></c:if>
</c:url>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">

    <jsp:param name="activeSidebar" value="phan-bo" />

    <jsp:param name="pageTitle" value="${breadcrumbLabel}" />

    <jsp:param name="sectionTitle" value="Phân bổ thí sinh" />

    <jsp:param name="sectionUrl" value="${allocOverviewUrl}" />

    <jsp:param name="noCache" value="true" />

    <jsp:param name="mainClass" value="examstaff-main--scroll" />

    <jsp:param name="examId" value="${layoutExamId}" />

    <jsp:param name="dataAllocExam" value="${allocExamMarker}" />

</jsp:include>

<header class="page-header page-header--toolbar">
    <c:if test="${param.showSearch ne 'true'}">
        <p class="examiner-page-desc"><c:out value="${pageTitle}" /></p>
    </c:if>
    <div class="page-actions allocation-page-actions${param.showSearch ne 'true' ? ' allocation-page-actions--end' : ''}">
        <c:if test="${param.showSearch eq 'true'}">
        <form method="get" action="${ctx}${layoutListPath}" class="allocation-search-form" id="allocationSearchForm">
                <c:if test="${not empty layoutExamId}"><input type="hidden" name="examId" value="${layoutExamId}"></c:if>
                <c:if test="${not empty sortBy and sortBy ne 'sbd'}"><input type="hidden" name="sort" value="${sortBy}"></c:if>
                <c:if test="${not empty sortDir and sortDir ne 'asc'}"><input type="hidden" name="dir" value="${sortDir}"></c:if>
            <div class="allocation-search-row">
                <c:if test="${param.showRoomFilter eq 'theory' or param.showRoomFilter eq 'practical'}">
                    <label class="allocation-room-filter" for="allocationAreaFilter">
                        <span class="allocation-room-filter__label">
                            <c:choose>
                                <c:when test="${param.showRoomFilter eq 'practical'}">Sân</c:when>
                                <c:otherwise>Phòng</c:otherwise>
                            </c:choose>
                        </span>
                        <select name="areaFilter" id="allocationAreaFilter" class="allocation-room-filter__select"
                                onchange="this.form.submit()"
                                title="Lọc theo ${param.showRoomFilter eq 'practical' ? 'sân thi' : 'phòng thi'}">
                            <option value="0" ${empty allocationAreaFilter ? 'selected' : ''}>Tất cả</option>
                            <c:choose>
                                <c:when test="${param.showRoomFilter eq 'practical'}">
                                    <c:forEach var="yard" items="${activePracticalAreas}">
                                        <c:set var="yardLabel" value="${fn:replace(yard.areaName, 'Sân thi ', '')}" />
                                        <option value="${yard.id}" ${allocationAreaFilter eq yard.id ? 'selected' : ''}>${yardLabel}</option>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="room" items="${activeTheoryRooms}">
                                        <c:set var="roomLabel" value="${fn:replace(room.areaName, 'Phòng thi ', '')}" />
                                        <option value="${room.id}" ${allocationAreaFilter eq room.id ? 'selected' : ''}>${roomLabel}</option>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </label>
                </c:if>
                <div class="es-search-box">
                    <input type="text" name="q" id="candidateSearch" class="es-search-box__input"
                           value="${allocationSearchQuery}" placeholder="Tìm SBD, họ tên, căn cước…">
                </div>
                <button type="submit" class="allocation-search-btn" aria-label="Tìm kiếm">
                    <span class="material-symbols-outlined" aria-hidden="true">search</span>
                </button>
            </div>
        </form>
        </c:if>
        <c:if test="${allocationStage eq 'overview'
                      or allocationStage eq 'theory'
                      or allocationStage eq 'practical'}">
            <form method="post" action="${ctx}${layoutListPath}" class="allocation-auto-form">
                <input type="hidden" name="action" value="autoAllocate">
                <c:if test="${not empty layoutExamId}">
                    <input type="hidden" name="examId" value="${layoutExamId}">
                </c:if>
                <button type="submit" class="allocation-search-btn allocation-auto-btn"
                        title="Tự động phân thí sinh vào phòng/sân đã có sát hạch viên">
                    <span>
                        <c:choose>
                            <c:when test="${allocationStage eq 'theory'}">Tự động phân phòng</c:when>
                            <c:when test="${allocationStage eq 'practical'}">Tự động phân sân</c:when>
                            <c:otherwise>Tự động phân phòng/sân</c:otherwise>
                        </c:choose>
                    </span>
                </button>
            </form>
        </c:if>
        <c:set var="allocationRefreshQuery" value="${allocationExtraQuery}" />
        <c:if test="${fn:startsWith(allocationRefreshQuery, '&')}">
            <c:set var="allocationRefreshQuery"
                   value="?${fn:substring(allocationRefreshQuery, 1, fn:length(allocationRefreshQuery))}" />
        </c:if>
        <c:choose>
            <c:when test="${empty allocationRefreshQuery}">
                <c:set var="allocationRefreshQuery" value="?refresh=1" />
            </c:when>
            <c:otherwise>
                <c:set var="allocationRefreshQuery" value="${allocationRefreshQuery}&amp;refresh=1" />
            </c:otherwise>
        </c:choose>
        <a href="${ctx}${layoutListPath}${allocationRefreshQuery}"
           class="allocation-search-btn allocation-refresh-btn"
           title="Tải lại dữ liệu" aria-label="Tải lại dữ liệu">
            <span class="material-symbols-outlined" aria-hidden="true">refresh</span>
        </a>
    </div>
</header>

<jsp:include page="/views/staff/examstaff/includes/allocation-alerts.jsp" />
