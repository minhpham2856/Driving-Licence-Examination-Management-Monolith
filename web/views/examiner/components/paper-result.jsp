<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="paper-table-wrap">
    <table class="paper-table" id="paperTable">
        <thead>
            <tr>
                <jsp:include page="/views/examiner/components/sort-th.jsp">
                    <jsp:param name="sortColumn" value="questionNo" />
                    <jsp:param name="label" value="Câu hỏi" />
                    <jsp:param name="thClass" value="paper-th paper-th--no" />
                </jsp:include>
                <th class="paper-th paper-th--content">Nội dung</th>
                <jsp:include page="/views/examiner/components/sort-th.jsp">
                    <jsp:param name="sortColumn" value="correctAnswer" />
                    <jsp:param name="label" value="Đáp án" />
                    <jsp:param name="thClass" value="paper-th paper-th--answer" />
                    <jsp:param name="center" value="true" />
                </jsp:include>
                <jsp:include page="/views/examiner/components/sort-th.jsp">
                    <jsp:param name="sortColumn" value="studentAnswer" />
                    <jsp:param name="label" value="Thí sinh trả lời" />
                    <jsp:param name="thClass" value="paper-th paper-th--student" />
                    <jsp:param name="center" value="true" />
                </jsp:include>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty paperAnswers}">
                    <tr><td colspan="4" class="examiner-table__empty">Chưa có dữ liệu.</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${paperAnswers}" var="q" varStatus="st">
                        <tr class="paper-tr paper-tr--${q.answerStatus}<c:if test="${st.index % 2 == 1}"> paper-tr--alt</c:if>" data-answer-status="${q.answerStatus}">
                            <td class="paper-td paper-td--no">${q.questionNo}</td>
                            <td class="paper-td paper-td--content">
                                <img src="${q.imageUrl}" class="paper-img"/>
                            </td>
                            <td class="paper-td paper-td--answer">${q.correctAnswer}</td>
                            <td class="paper-td paper-td--student">
                                <c:choose>
                                    <c:when test="${q.unanswered}">
                                        <span class="paper-ans paper-ans--skipped">${q.studentAnswer}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="paper-ans paper-ans--${q.correct ? 'correct' : 'wrong'}">${q.studentAnswer}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>
