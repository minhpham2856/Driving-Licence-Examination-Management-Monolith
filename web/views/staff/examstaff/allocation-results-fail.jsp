<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="failLayoutSessionId" value="${param.sessionId}" />
<c:if test="${empty failLayoutSessionId and not empty requestScope.examStaffLoadedSessionId}">
    <c:set var="failLayoutSessionId" value="${requestScope.examStaffLoadedSessionId}" />
</c:if>
<c:if test="${empty failLayoutSessionId and not empty sessionScope.selectedSessionId}">
    <c:set var="failLayoutSessionId" value="${sessionScope.selectedSessionId}" />
</c:if>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Kết quả — Trượt / vắng" />
    <jsp:param name="breadcrumbLabel" value="Trượt" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="sessionId" value="${failLayoutSessionId}" />
</jsp:include>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pg" value="${allocationPageSlice}" />
<c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />
<c:set var="sq" value="" />
<c:set var="navSessionId" value="${not empty param.sessionId ? param.sessionId : requestScope.examStaffLoadedSessionId}" />
<c:if test="${not empty navSessionId}">
    <c:set var="sq" value="?sessionId=${navSessionId}" />
</c:if>
<nav class="allocation-result-subnav">
    <a href="${ctx}/views/staff/examstaff/allocation-results-pass${sq}" class="allocation-result-subnav__tab">Đỗ</a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-fail${sq}" class="allocation-result-subnav__tab is-active">Trượt / vắng</a>
</nav>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-rules-box.jsp">
    <jsp:param name="variant" value="results" />
</jsp:include>
<div class="allocation-results-panel allocation-results-panel--fail">
    <div class="allocation-results-panel__head">
        <h4 class="allocation-results-panel__title">Tổng hợp — Trượt / vắng</h4>
        <span class="allocation-results-panel__count">${pg.totalItems} thí sinh</span>
    </div>
    <div class="examiner-table-wrap">
        <table class="examiner-table allocation-results-table allocation-table--fill">
            <thead>
                <jsp:include page="/views/staff/examstaff/includes/allocation-table-head.jsp">
                    <jsp:param name="variant" value="results-fail" />
                </jsp:include>
            </thead>
            <tbody>
                <c:forEach var="c" items="${allocationStageList}" varStatus="st">
                    <tr>
                        <td class="examiner-table__center">${rowStart + st.count}</td>
                        <td><strong>${c.sbd}</strong></td>
                        <td>${c.name}</td>
                        <td>${c.clazz}</td>
                        <td>
                            <c:choose>
                                <c:when test="${c.absent}">—</c:when>
                                <c:otherwise><span class="allocation-score allocation-score--${c.theoryPassed eq 'passed' ? 'pass' : 'fail'}">${c.theoryScore}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.absent}">—</c:when>
                                <c:when test="${c.practicalScore != null}"><span class="allocation-score allocation-score--${c.practicalPassed eq 'passed' ? 'pass' : 'fail'}">${c.practicalScore}</span></c:when>
                                <c:otherwise><span class="allocation-na">Chưa thi</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${allocationNoRoadTestIds.contains(c.id)}"><span class="allocation-na">N/A</span></c:when>
                                <c:when test="${c.absent or c.practicalPassed ne 'passed'}">—</c:when>
                                <c:otherwise><span class="allocation-score allocation-score--${c.roadTestPassed eq 'passed' ? 'pass' : 'fail'}">${c.roadTestScore}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.absent}"><span class="allocation-result-reason allocation-result-reason--absent">Vắng thi</span></c:when>
                                <c:otherwise><span class="allocation-result-reason allocation-result-reason--fail">Trượt</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${pg.totalItems eq 0}"><tr><td colspan="8" class="allocation-results-table__empty">Chưa có thí sinh trượt hoặc vắng.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
