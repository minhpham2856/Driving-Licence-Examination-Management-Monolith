<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${examinerSectionTheory}">
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="fullName" /><jsp:param name="label" value="Tên" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="sbd" /><jsp:param name="label" value="SBD" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="dob" /><jsp:param name="label" value="Ngày sinh" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="address" /><jsp:param name="label" value="Địa chỉ" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId" /><jsp:param name="label" value="Số CC" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="correct" /><jsp:param name="label" value="Đúng" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="wrong" /><jsp:param name="label" value="Sai" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="unanswered" /><jsp:param name="label" value="Không TL" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="result" /><jsp:param name="label" value="Kết quả" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="status" /><jsp:param name="label" value="Tình trạng" /></jsp:include>
    </c:when>
    <c:otherwise>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="fullName" /><jsp:param name="label" value="Tên" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="sbd" /><jsp:param name="label" value="SBD" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="dob" /><jsp:param name="label" value="Ngày sinh" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="address" /><jsp:param name="label" value="Địa chỉ" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId" /><jsp:param name="label" value="Số CC" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="examScore" /><jsp:param name="label" value="Điểm thi" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="result" /><jsp:param name="label" value="Kết quả" /></jsp:include>
        <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="status" /><jsp:param name="label" value="Tình trạng" /></jsp:include>
    </c:otherwise>
</c:choose>
