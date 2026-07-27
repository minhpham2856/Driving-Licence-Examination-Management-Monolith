<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context variable--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--candidate list for actions page (theory)--%>
<section class="card action-card">
    <div class="card-head">
        <h2>Danh sách thí sinh</h2>
    </div>

    <div class="table-wrap">
        <table class="table">
            <%--headers--%>
            <thead>
                <tr>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="sbd"/>
                        <jsp:param name="label" value="SBD"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="fullName"/>
                        <jsp:param name="label" value="Họ tên"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="status"/>
                        <jsp:param name="label" value="Trạng thái"/>
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="examScore"/>
                        <jsp:param name="label" value="Điểm"/>
                    </jsp:include>
                    <th>Điểm danh</th>
                    <th>Đình chỉ</th>
                    <th>In</th>
                    <th>Hoàn thành</th>
                </tr>
            </thead>

            <%--body--%>
            <tbody>
                <c:choose>

                    <%--case 1: empty list--%>
                    <c:when test="${empty candidates}">
                        <tr>
                            <td colspan="8" class="table-empty">
                                ${searchActive ? 'Không tìm thấy thí sinh phù hợp.' : 'Chưa có thí sinh trong kỳ thi/phần thi này.'}
                            </td>
                        </tr>
                    </c:when>

                    <%--case 2: has candidates--%>
                    <c:otherwise>
                        <c:forEach items="${candidates}" var="candidate" varStatus="row">
                            <tr class="${row.index % 2 == 1 ? 'table-row-alt' : ''}">

                                <%--sbd--%>
                                <td class="table-mono-md">
                                    <a class="table-link"
                                       href="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}&amp;from=action">
                                        ${candidate.candidateNumber}
                                    </a>
                                </td>

                                <%--name--%>
                                <td class="table-name">
                                    <a class="table-link"
                                       href="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}&amp;from=action">
                                        ${candidate.fullName}
                                    </a>
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

                                <%--score--%>
                                <td class="table-mono-md">
                                    ${empty candidate.examScore ? '' : candidate.examScore}
                                </td>

                                <%--attendance--%>
                                <td>
                                    <%--case 1: mark present--%>
                                    <c:if test="${candidate.sectionRequired and candidate.markPresentEligible}">
                                        <form method="post" action="${pageUrl}">
                                            <input type="hidden" name="action" value="markPresent">
                                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                            <button type="submit"
                                                    class="btn amber compact">
                                                Điểm danh
                                            </button>
                                        </form>
                                    </c:if>

                                    <%--case 2: undo present--%>
                                    <c:if test="${candidate.sectionRequired and candidate.undoPresentEligible}">
                                        <form method="post" action="${pageUrl}">
                                            <input type="hidden" name="action" value="undoPresent">
                                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                            <button type="submit"
                                                    class="btn white compact">
                                                Hủy điểm danh
                                            </button>
                                        </form>
                                    </c:if>
                                </td>

                                <%--violation--%>
                                <td>
                                    <%--case 1: view violation--%>
                                    <c:if test="${candidate.sectionRequired and candidate.suspended}">
                                        <a href="${ctx}/examiner/violations?sbd=${candidate.candidateNumber}&amp;mode=view&amp;from=action"
                                           class="btn white compact">
                                            Xem vi phạm
                                        </a>
                                    </c:if>

                                    <%--case 2: suspend--%>
                                    <c:if test="${candidate.sectionRequired and not candidate.suspended and candidate.violationEligible}">
                                        <a href="${ctx}/examiner/violations?sbd=${candidate.candidateNumber}&amp;mode=create&amp;from=action"
                                           class="btn red compact">
                                            Đình chỉ
                                        </a>
                                    </c:if>
                                </td>

                                <%--print--%>
                                <td>
                                    <c:if test="${candidate.sectionRequired and candidate.awaitingSignature}">
                                        <form method="post" action="${pageUrl}" target="_blank">
                                            <input type="hidden" name="action" value="printResult">
                                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                            <button type="submit"
                                                    class="btn white compact">
                                                In
                                            </button>
                                        </form>
                                    </c:if>
                                </td>

                                <%--complete--%>
                                <td>
                                    <c:if test="${candidate.sectionRequired and candidate.completeEligible}">
                                        <form method="post" action="${pageUrl}">
                                            <input type="hidden" name="action" value="completeSection">
                                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                            <button type="submit"
                                                    class="btn green compact">
                                                Hoàn thành
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</section>
