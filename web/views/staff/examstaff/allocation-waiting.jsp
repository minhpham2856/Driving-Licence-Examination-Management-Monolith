<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Phòng chờ chính" />
    <jsp:param name="breadcrumbLabel" value="Phòng chờ" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="examId" value="${param.examId}" />
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pg" value="${allocationPageSlice}" />
<c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />
<c:set var="stageTotal" value="${empty pg ? 0 : pg.totalItems}" />

<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />

<div class="allocation-stage-panel allocation-stage-panel--waiting">
    <div class="allocation-stage-panel__head">
        <h4 class="allocation-stage-panel__title">Phòng chờ chính</h4>
        <span class="allocation-stage-panel__count">${stageTotal} thí sinh</span>
    </div>
    <div class="examiner-table-wrap">
        <table class="examiner-table allocation-stage-table allocation-table--fill">
            <thead>
                <jsp:include page="/views/staff/examstaff/includes/allocation-table-head.jsp">
                    <jsp:param name="variant" value="waiting" />
                </jsp:include>
            </thead>
            <tbody>
                <c:forEach var="c" items="${allocationStageList}" varStatus="st">
                    <tr>
                        <td class="examiner-table__center">${rowStart + st.count}</td>
                        <td><strong>${c.sbd}</strong></td>
                        <td>${c.name}</td>
                        <td>${c.clazz}</td>
                        <td><span class="allocation-stage-status allocation-stage-status--waiting">Chờ thủ tục</span></td>
                    </tr>
                </c:forEach>
                <c:if test="${stageTotal eq 0}">
                    <tr><td colspan="5" class="allocation-stage-table__empty">Không có thí sinh trong phòng chờ.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/views/staff/examstaff/includes/allocation-pagination.jsp" />
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />
