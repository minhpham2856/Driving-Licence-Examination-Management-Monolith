<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--candidate list for dashboard--%>
<section class="card table-fill">
    <div class="card-head">
        <h3 class="card-title">Danh sách thí sinh</h3>
    </div>

    <div class="table-wrap">
        <table class="table">
            <%--headers--%>
            <thead>
                <tr>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="fullName"/>
                        <jsp:param name="label" value="Tên"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="sbd"/>
                        <jsp:param name="label" value="SBD"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="dob"/>
                        <jsp:param name="label" value="Ngày sinh"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="governmentId"/>
                        <jsp:param name="label" value="Số CC"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="examScore"/>
                        <jsp:param name="label" value="Điểm thi"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="result"/>
                        <jsp:param name="label" value="Kết quả"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="status"/>
                        <jsp:param name="label" value="Tình trạng"/>
                    </jsp:include>
                </tr>
            </thead>

            <%--body--%>
            <tbody>
                <c:choose>

                    <%--case 1: empty list--%>
                    <c:when test="${empty candidates}">
                        <tr>
                            <td colspan="7" class="table-empty">
                                ${searchActive ? 'Không tìm thấy thí sinh phù hợp.' : 'Chưa có dữ liệu.'}
                            </td>
                        </tr>
                    </c:when>

                    <%--case 2: has candidates--%>
                    <c:otherwise>
                        <c:forEach items="${candidates}" var="candidate" varStatus="row">
                            <tr class="${row.index % 2 == 1 ? 'table-row-alt' : ''}">
                                <td class="table-name">${candidate.fullName}</td>
                                <td class="table-mono-md">${candidate.candidateNumber}</td>
                                <td class="table-mono-md">${empty candidate.dob ? '' : candidate.dob}</td>
                                <td class="table-mono-md">
                                    ${empty candidate.governmentId ? '' : candidate.governmentId}
                                </td>
                                <td class="table-mono-md">
                                    ${empty candidate.examScore ? '' : candidate.examScore}
                                </td>

                                <%--result tag--%>
                                <td>
                                    <%--case 1: passed--%>
                                    <c:if test="${candidate.passed}">
                                        <span class="tag tag-pass">${candidate.resultLabel}</span>
                                    </c:if>
                                    <%--case 2: failed / has label--%>
                                    <c:if test="${not candidate.passed and not empty candidate.resultLabel}">
                                        <span class="tag tag-fail">${candidate.resultLabel}</span>
                                    </c:if>
                                </td>

                                <%--status--%>
                                <td>
                                    <%--map not-required -> pending css--%>
                                    <c:set var="statusClass"
                                           value="${candidate.status == 'not-required' ? 'pending' : candidate.status}" />
                                    <span class="tag tag-${empty statusClass ? 'pending' : statusClass}">
                                        ${candidate.statusLabel}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</section>
