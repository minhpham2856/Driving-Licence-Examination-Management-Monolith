<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="pg" value="${empty allocationPageSlice ? examStaffPageSlice : allocationPageSlice}" />

<c:set var="listPath" value="${empty allocationListPath ? examStaffListPath : allocationListPath}" />

<c:if test="${not empty pg and pg.totalItems > 0}">

<c:set var="rangeStart" value="${pg.rowOffset + 1}" />
<c:set var="rangeEnd" value="${pg.rowOffset + fn:length(pg.items)}" />

<div class="allocation-pagination examstaff-pagination" aria-label="Phân trang">

    <p class="examstaff-pagination__summary">
        Hiển thị ${rangeStart}–${rangeEnd} / ${pg.totalItems} thao tác
        <c:if test="${pg.totalPages > 1}"> · Trang ${pg.page}/${pg.totalPages}</c:if>
    </p>

    <c:if test="${pg.totalPages > 1}">

    <div class="allocation-pagination__actions">

        <c:if test="${pg.page gt 1}">

            <c:url var="prevUrl" value="${listPath}">

                <c:param name="page" value="${pg.page - 1}" />

                <c:if test="${not empty allocationSearchQuery}"><c:param name="q" value="${allocationSearchQuery}" /></c:if>

                <c:if test="${not empty param.sessionId}"><c:param name="sessionId" value="${param.sessionId}" /></c:if>
                <c:if test="${empty param.sessionId and not empty requestScope.selectedSessionId}">
                    <c:param name="sessionId" value="${requestScope.selectedSessionId}" />
                </c:if>

                <c:if test="${not empty sortBy and sortBy ne 'sbd'}"><c:param name="sort" value="${sortBy}" /></c:if>
                <c:if test="${not empty sortDir and sortDir ne 'asc'}"><c:param name="dir" value="${sortDir}" /></c:if>
                <c:if test="${not empty allocationAreaFilter}">
                    <c:param name="areaFilter" value="${allocationAreaFilter eq -1 ? 'none' : allocationAreaFilter}" />
                </c:if>

                <c:if test="${not empty param.filterDate}"><c:param name="filterDate" value="${param.filterDate}" /></c:if>

            </c:url>

            <a href="${prevUrl}" class="allocation-pagination__btn examstaff-pagination__btn">

                <span class="material-symbols-outlined" aria-hidden="true">chevron_left</span> Trước

            </a>

        </c:if>

        <c:if test="${pg.totalPages gt 0 and pg.page lt pg.totalPages}">

            <c:url var="nextUrl" value="${listPath}">

                <c:param name="page" value="${pg.page + 1}" />

                <c:if test="${not empty allocationSearchQuery}"><c:param name="q" value="${allocationSearchQuery}" /></c:if>

                <c:if test="${not empty param.sessionId}"><c:param name="sessionId" value="${param.sessionId}" /></c:if>
                <c:if test="${empty param.sessionId and not empty requestScope.selectedSessionId}">
                    <c:param name="sessionId" value="${requestScope.selectedSessionId}" />
                </c:if>

                <c:if test="${not empty sortBy and sortBy ne 'sbd'}"><c:param name="sort" value="${sortBy}" /></c:if>
                <c:if test="${not empty sortDir and sortDir ne 'asc'}"><c:param name="dir" value="${sortDir}" /></c:if>
                <c:if test="${not empty allocationAreaFilter}">
                    <c:param name="areaFilter" value="${allocationAreaFilter eq -1 ? 'none' : allocationAreaFilter}" />
                </c:if>

                <c:if test="${not empty param.filterDate}"><c:param name="filterDate" value="${param.filterDate}" /></c:if>

            </c:url>

            <a href="${nextUrl}" class="allocation-pagination__btn examstaff-pagination__btn">

                Sau <span class="material-symbols-outlined" aria-hidden="true">chevron_right</span>

            </a>

        </c:if>

    </div>

    </c:if>

</div>

</c:if>
