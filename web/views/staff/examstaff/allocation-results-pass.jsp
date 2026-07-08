<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="passLayoutSessionId" value="${param.sessionId}" />
<c:if test="${empty passLayoutSessionId and not empty requestScope.examStaffLoadedSessionId}">
    <c:set var="passLayoutSessionId" value="${requestScope.examStaffLoadedSessionId}" />
</c:if>
<c:if test="${empty passLayoutSessionId and not empty sessionScope.selectedSessionId}">
    <c:set var="passLayoutSessionId" value="${sessionScope.selectedSessionId}" />
</c:if>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Kết quả — Đỗ" />
    <jsp:param name="breadcrumbLabel" value="Đỗ" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="sessionId" value="${passLayoutSessionId}" />
</jsp:include>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pg" value="${allocationPageSlice}" />
<c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />
<c:set var="passTotal" value="${empty pg ? 0 : pg.totalItems}" />
<c:set var="sq" value="" />
<c:set var="navSessionId" value="${not empty param.sessionId ? param.sessionId : requestScope.examStaffLoadedSessionId}" />
<c:if test="${not empty navSessionId}">
    <c:set var="sq" value="?sessionId=${navSessionId}" />
</c:if>
<nav class="allocation-result-subnav">
    <a href="${ctx}/views/staff/examstaff/allocation-results-pass${sq}" class="allocation-result-subnav__tab is-active">Đỗ</a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-fail${sq}" class="allocation-result-subnav__tab">Trượt / vắng</a>
</nav>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-rules-box.jsp">
    <jsp:param name="variant" value="results" />
</jsp:include>
<div class="allocation-results-panel allocation-results-panel--pass">
    <div class="allocation-results-panel__head">
        <h4 class="allocation-results-panel__title">Tổng hợp — Đỗ</h4>
        <span class="allocation-results-panel__count">${passTotal} thí sinh</span>
    </div>
    <div class="examiner-table-wrap">
        <table class="examiner-table allocation-results-table allocation-table--fill">
            <thead>
                <jsp:include page="/views/staff/examstaff/includes/allocation-table-head.jsp">
                    <jsp:param name="variant" value="results-pass" />
                </jsp:include>
            </thead>
            <tbody>
                <c:forEach var="c" items="${allocationStageList}" varStatus="st">
                    <tr>
                        <td class="examiner-table__center">${rowStart + st.count}</td>
                        <td><strong>${c.sbd}</strong></td>
                        <td>${c.name}</td>
                        <td>${c.clazz}</td>
                        <td><span class="allocation-score allocation-score--pass">${c.theoryScore}</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${c.skipsPractical}"><span class="allocation-na">Bảo lưu</span></c:when>
                                <c:when test="${c.practicalScore != null}"><span class="allocation-score allocation-score--pass">${c.practicalScore}</span></c:when>
                                <c:otherwise><span class="allocation-na">—</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.skipsRoad and c.requiresRoadTest}"><span class="allocation-na">Bảo lưu</span></c:when>
                                <c:when test="${allocationNoRoadTestIds.contains(c.id)}"><span class="allocation-na">Không áp dụng</span></c:when>
                                <c:otherwise><span class="allocation-score allocation-score--pass">${c.roadTestScore}</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${passTotal eq 0}"><tr><td colspan="7" class="allocation-results-table__empty">Chưa có thí sinh đỗ.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
