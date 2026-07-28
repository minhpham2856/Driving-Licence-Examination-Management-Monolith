<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--resolve active filter (default: all)--%>
<c:set var="activeFilter" value="${empty param.filter ? 'all' : param.filter}" />

<%--theory paper answer table--%>
<div class="paper-table-wrap">
    <table class="paper-table" id="paperTable">
        <%--headers--%>
        <thead>
            <tr>
                <jsp:include page="/views/examiner/components/sort-th.jsp">
                    <jsp:param name="sortColumn" value="questionNo" />
                    <jsp:param name="label" value="Câu hỏi" />
                    <jsp:param name="thClass" value="paper-th paper-th-no" />
                </jsp:include>
                <th class="paper-th paper-th-content">Nội dung</th>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="correctAnswer" />
                        <jsp:param name="label" value="Đáp án" />
                        <jsp:param name="thClass" value="paper-th paper-th-answer" />
                        <jsp:param name="center" value="true" />
                    </jsp:include>
                    <jsp:include page="/views/examiner/components/sort-th.jsp">
                        <jsp:param name="sortColumn" value="studentAnswer" />
                        <jsp:param name="label" value="Thí sinh trả lời" />
                        <jsp:param name="thClass" value="paper-th paper-th-student" />
                        <jsp:param name="center" value="true" />
                    </jsp:include>
            </tr>
        </thead>

        <%--body--%>
        <tbody>
            <c:choose>

                <%--case 1: empty--%>
                <c:when test="${empty paperAnswers}">
                    <tr>
                        <td colspan="4" class="table-empty">Chưa có dữ liệu.</td>
                    </tr>
                </c:when>

                <%--case 2: has answers--%>
                <c:otherwise>
                    <c:set var="visibleCount" value="0" />
                    <c:forEach items="${paperAnswers}" var="q" varStatus="st">

                        <%--filter: match tab to answerStatus (unanswered tab matches skipped status)--%>
                        <c:set var="matchesFilter"
                               value="${activeFilter == 'all'
                                        or activeFilter == q.answerStatus
                                        or (activeFilter == 'unanswered' and q.answerStatus == 'skipped')}" />
                        <c:if test="${matchesFilter}">
                            <c:set var="visibleCount" value="${visibleCount + 1}" />
                            <tr class="paper-tr paper-tr-${q.answerStatus}<c:if test="${st.index % 2 == 1}"> paper-tr-alt</c:if>"
                                data-answer-status="${q.answerStatus}">
                                <td class="paper-td paper-td-no">
                                    ${st.count} (${q.questionNo}<c:if test="${q.critical}"> - ĐL</c:if>)
                                </td>
                                <td class="paper-td paper-td-content">
                                    <c:if test="${not empty q.imageUrl}">
                                        <img src="<c:out value='${q.imageUrl}'/>"
                                             class="paper-img"
                                             alt="Hình câu hỏi số ${q.questionNo}"/>
                                    </c:if>
                                </td>
                                <td class="paper-td paper-td-answer">${q.correctAnswer}</td>
                                <td class="paper-td paper-td-student">
                                    <c:choose>
                                        <%--case 1: unanswered--%>
                                        <c:when test="${q.unanswered}">
                                            <span class="paper-ans paper-ans-skipped">${q.studentAnswer}</span>
                                        </c:when>
                                        <%--case 2: answered--%>
                                        <c:otherwise>
                                            <span class="paper-ans paper-ans-${q.correct ? 'correct' : 'wrong'}">
                                                ${q.studentAnswer}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>

                    <%--empty after filter--%>
                    <c:if test="${visibleCount == 0}">
                        <tr>
                            <td colspan="4" class="table-empty">Không có câu hỏi nào thuộc bộ lọc này.</td>
                        </tr>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>
