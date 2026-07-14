<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Thực hành / Sa hình" />
    <jsp:param name="breadcrumbLabel" value="Thực hành" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="showRoomFilter" value="practical" />
    <jsp:param name="examId" value="${param.examId}" />
</jsp:include>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pg" value="${allocationPageSlice}" />
<c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />
<c:set var="extra" value="${allocationExtraQuery}" />
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-rules-box.jsp">
    <jsp:param name="variant" value="practical" />
</jsp:include>
<div class="allocation-stage-panel allocation-stage-panel--practical">
    <div class="allocation-stage-panel__head">
        <h4 class="allocation-stage-panel__title">Thực hành / Sa hình</h4>
        <span class="allocation-stage-panel__count">${pg.totalItems} thí sinh</span>
    </div>
    <div class="examiner-table-wrap">
        <table class="examiner-table allocation-stage-table allocation-table--fill">
            <thead>
                <jsp:include page="/views/staff/examstaff/includes/allocation-table-head.jsp">
                    <jsp:param name="variant" value="practical" />
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
                            <c:if test="${not empty activePracticalAreas}">
                                <form action="${ctx}${allocationListPath}" method="get" class="allocation-inline-form allocation-inline-form--room-change">
                                    <input type="hidden" name="action" value="allocatePracticalRoom">
                                    <input type="hidden" name="id" value="${c.id}">
                                    <c:if test="${not empty allocationSearchQuery}"><input type="hidden" name="q" value="${allocationSearchQuery}"></c:if>
                                    <c:if test="${pg.page gt 1}"><input type="hidden" name="page" value="${pg.page}"></c:if>
                                    <c:if test="${not empty layoutExamId}"><input type="hidden" name="examId" value="${layoutExamId}"></c:if>
                                    <c:if test="${empty layoutExamId and allocationActiveExamId gt 0}"><input type="hidden" name="examId" value="${allocationActiveExamId}"></c:if>
                                    <jsp:include page="/views/staff/examstaff/includes/allocation-sort-hidden.jsp" />
                                    <select name="areaId" data-auto-submit class="allocation-area-select allocation-area-select--table" title="Đổi sân thi">
                                        <c:if test="${empty c.practicalAllocatedAreaId}">
                                            <option value="" disabled selected>—</option>
                                        </c:if>
                                        <c:forEach var="yard" items="${activePracticalAreas}">
                                            <c:set var="yardLabel" value="${fn:replace(yard.areaName, 'Sân thi ', '')}" />
                                            <option value="${yard.id}" ${c.practicalAllocatedAreaId eq yard.id ? 'selected' : ''}>${yardLabel}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                            </c:if>
                            <c:if test="${empty activePracticalAreas}">
                                <span class="allocation-room-pending" title="Phân giám khảo vào sân thực hành trước">—</span>
                            </c:if>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty c.practicalScore}">
                                    <span class="allocation-score">${c.practicalScore}</span>
                                    <span class="es-text-muted-sm">
                                        <c:choose>
                                            <c:when test="${c.practicalPassed eq 'passed'}">Đạt</c:when>
                                            <c:when test="${c.practicalPassed eq 'failed'}">Không đạt</c:when>
                                            <c:otherwise>Đã chấm</c:otherwise>
                                        </c:choose>
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="es-text-muted-sm">Chờ giám khảo chấm</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${pg.totalItems eq 0}"><tr><td colspan="7" class="allocation-stage-table__empty">Không có thí sinh.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
