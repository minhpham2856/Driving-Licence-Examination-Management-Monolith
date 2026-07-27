<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="variant" value="${param.variant}" />
<tr>
    <th class="table-center" style="width: 56px;">STT</th>
    <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
        <jsp:param name="sortColumn" value="sbd" />
        <jsp:param name="label" value="SBD" />
    </jsp:include>
    <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
        <jsp:param name="sortColumn" value="name" />
        <jsp:param name="label" value="Họ tên" />
    </jsp:include>
    <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
        <jsp:param name="sortColumn" value="clazz" />
        <jsp:param name="label" value="Hạng" />
    </jsp:include>
    <c:if test="${variant eq 'waiting'}">
        <th>Trạng thái</th>
    </c:if>
    <c:if test="${variant eq 'theory'}">
        <th>Hồ sơ</th>
        <th>Phòng thi</th>
        <th>Điểm LT</th>
    </c:if>
    <c:if test="${variant eq 'practical'}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
            <jsp:param name="sortColumn" value="theoryScore" />
            <jsp:param name="label" value="Lý thuyết" />
        </jsp:include>
        <th>Sân thi</th>
        <th>Điểm TH</th>
    </c:if>
    <c:if test="${variant eq 'results-pass' or variant eq 'results-fail'}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
            <jsp:param name="sortColumn" value="theoryScore" />
            <jsp:param name="label" value="Lý thuyết" />
        </jsp:include>
        <jsp:include page="/views/staff/examstaff/includes/examstaff-sort-th.jsp">
            <jsp:param name="sortColumn" value="practicalScore" />
            <jsp:param name="label" value="Thực hành" />
        </jsp:include>
        <c:if test="${variant eq 'results-fail'}">
            <th>Lý do</th>
        </c:if>
    </c:if>
</tr>
