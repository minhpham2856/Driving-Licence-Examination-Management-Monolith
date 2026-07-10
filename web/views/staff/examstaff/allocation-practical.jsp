<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Thực hành / Sa hình" />
    <jsp:param name="breadcrumbLabel" value="Thực hành" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="sessionId" value="${param.sessionId}" />
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
                        <td><a href="${ctx}${allocationListPath}?action=submitPracticalScore&amp;id=${c.id}&amp;score=90${extra}" class="allocation-table-action allocation-table-action--practical">Chấm TH (Auto)</a></td>
                    </tr>
                </c:forEach>
                <c:if test="${pg.totalItems eq 0}"><tr><td colspan="6" class="allocation-stage-table__empty">Không có thí sinh.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>
<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
