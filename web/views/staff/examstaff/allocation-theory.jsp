<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Phòng thi lý thuyết" />
    <jsp:param name="breadcrumbLabel" value="Lý thuyết" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="sessionId" value="${param.sessionId}" />
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pg" value="${allocationPageSlice}" />
<c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />
<c:set var="extra" value="${allocationExtraQuery}" />

<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />

<jsp:include page="/views/staff/examstaff/includes/allocation-rules-box.jsp">
    <jsp:param name="variant" value="theory" />
</jsp:include>

<div class="allocation-stage-panel allocation-stage-panel--theory">
    <div class="allocation-stage-panel__head">
        <div class="allocation-stage-panel__title-wrap">
            <h4 class="allocation-stage-panel__title">Phòng thi lý thuyết</h4>
        </div>
        <span class="allocation-stage-panel__count">${pg.totalItems} thí sinh</span>
    </div>
    <div class="examiner-table-wrap">
        <table class="examiner-table allocation-stage-table allocation-table--fill">
            <thead>
                <jsp:include page="/views/staff/examstaff/includes/allocation-table-head.jsp">
                    <jsp:param name="variant" value="theory" />
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
                            <span class="badge-pill-status badge-pill-status--success">Ảnh</span>
                            <span class="badge-pill-status badge-pill-status--success">Lệ phí</span>
                        </td>
                        <td>
                            <c:if test="${not empty activeTheoryRooms}">
                                <form action="${ctx}${allocationListPath}" method="get" class="allocation-inline-form allocation-inline-form--room-change">
                                    <input type="hidden" name="action" value="allocateRoom">
                                    <input type="hidden" name="id" value="${c.id}">
                                    <c:if test="${not empty allocationSearchQuery}"><input type="hidden" name="q" value="${allocationSearchQuery}"></c:if>
                                    <c:if test="${pg.page gt 1}"><input type="hidden" name="page" value="${pg.page}"></c:if>
                                    <c:if test="${not empty layoutSessionId}"><input type="hidden" name="sessionId" value="${layoutSessionId}"></c:if>
                                    <c:if test="${empty layoutSessionId and allocationActiveSessionId gt 0}"><input type="hidden" name="sessionId" value="${allocationActiveSessionId}"></c:if>
                                    <jsp:include page="/views/staff/examstaff/includes/allocation-sort-hidden.jsp" />
                                    <select name="areaId" data-auto-submit class="allocation-area-select allocation-area-select--table" title="Đổi phòng">
                                        <c:if test="${empty c.allocatedAreaId}">
                                            <option value="" disabled selected>—</option>
                                        </c:if>
                                        <c:forEach var="room" items="${activeTheoryRooms}">
                                            <c:set var="roomLabel" value="${fn:replace(room.areaName, 'Phòng thi lý thuyết ', '')}" />
                                            <option value="${room.id}" ${c.allocatedAreaId eq room.id ? 'selected' : ''}>${roomLabel}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                            </c:if>
                            <c:if test="${empty activeTheoryRooms}">
                                <span class="allocation-room-pending">—</span>
                            </c:if>
                        </td>
                        <td>
                            <c:set var="lic" value="${c.clazz}" />
                            <c:set var="demo" value="45" />
                            <c:if test="${lic eq 'A' or lic eq 'A1'}"><c:set var="demo" value="36" /></c:if>
                            <c:if test="${lic eq 'C' or lic eq 'C1'}"><c:set var="demo" value="50" /></c:if>
                            <c:if test="${lic eq 'D' or lic eq 'D1' or lic eq 'D2' or lic eq 'E'}"><c:set var="demo" value="56" /></c:if>
                            <c:set var="scoreSessionId" value="${not empty layoutSessionId ? layoutSessionId : allocationActiveSessionId}" />
                            <c:set var="scoreSessionQuery" value="${scoreSessionId gt 0 ? '&amp;sessionId='.concat(scoreSessionId) : ''}" />
                            <a href="${ctx}${allocationListPath}?action=submitTheoryScore&amp;id=${c.id}&amp;score=${demo}${scoreSessionQuery}${extra}" class="allocation-table-action allocation-table-action--theory">Chấm LT (Auto)</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${pg.totalItems eq 0}">
                    <tr><td colspan="7" class="allocation-stage-table__empty">Không có thí sinh chờ thi lý thuyết.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
