<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context variable--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--candidate list for candidates page--%>
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
                        <jsp:param name="sortColumn" value="governmentId"/>
                        <jsp:param name="label" value="Số CC"/>
                    </jsp:include>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <%--body--%>
            <tbody>
                <c:choose>

                    <%--case 1: empty list--%>
                    <c:when test="${empty candidates}">
                        <tr>
                            <td colspan="4" class="table-empty">
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
                                <td class="table-mono-md">
                                    ${empty candidate.governmentId ? '' : candidate.governmentId}
                                </td>
                                <td>
                                    <div class="actions">
                                        <a href="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}"
                                           class="btn white compact">
                                            Thông tin
                                        </a>
                                        <c:if test="${candidate.sectionRequired and candidate.practicalEntryAllowed}">
                                            <%--case 1: theory paper--%>
                                            <c:if test="${examinerSectionTheory}">
                                                <a href="${ctx}/examiner/candidate-paper?sbd=${candidate.candidateNumber}"
                                                   class="btn white compact">
                                                    Đề thi
                                                </a>
                                            </c:if>
                                            <%--case 2: edit layout result--%>
                                            <c:if test="${not examinerSectionTheory and candidate.status eq 'done'}">
                                                <a href="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}"
                                                   class="btn white compact">
                                                    Sửa KQ
                                                </a>
                                            </c:if>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</section>
