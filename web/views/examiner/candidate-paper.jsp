<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Đề thi" />
<c:if test="${not empty candidate}">
    <c:set var="headerTitle" value="Đề thi — ${candidate.fullName}" />
</c:if>
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details-edit?sbd=${candidate.sbd}" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-paper?sbd=${candidate.sbd}" />
<c:set var="exportResultsUrl" value="${ctx}/examiner/export/results" />
<c:set var="exportResultsXmlUrl" value="${ctx}/examiner/export/results/xml" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <jsp:include page="/views/examiner/partials/examiner-styles.jsp">
            <jsp:param name="pageCss" value="paper.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="sua-thong-tin" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <!--toolbar-->
                <section class="examiner-toolbar">
                    <div class="exr-toolbar-left">
                        <a href="${backUrl}" class="exr-back">
                            <span class="material-symbols-outlined">arrow_back</span>
                            QUAY LẠI
                        </a>
                    </div>
                    <div class="examiner-toolbar__actions">
                        <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                            <span class="material-symbols-outlined">print</span>
                            In kết quả
                        </button>
                        <a href="${exportResultsUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất Excel
                        </a>
                        <a href="${exportResultsXmlUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất XML
                        </a>
                        <div class="paper-filter-tabs" role="tablist" aria-label="Lọc câu hỏi">
                            <button type="button" class="paper-filter-tab paper-filter-tab--all is-active" data-filter="all">
                                Tất cả (${empty paperSummary.totalQuestions ? 0 : paperSummary.totalQuestions})
                            </button>
                            <button type="button" class="paper-filter-tab paper-filter-tab--correct" data-filter="correct">
                                <span class="material-symbols-outlined">check</span>
                                Câu đúng (${empty paperSummary.correctCount ? 0 : paperSummary.correctCount})
                            </button>
                            <button type="button" class="paper-filter-tab paper-filter-tab--wrong" data-filter="wrong">
                                <span class="material-symbols-outlined">close</span>
                                Câu sai (${empty paperSummary.wrongCount ? 0 : paperSummary.wrongCount})
                            </button>
                            <button type="button" class="paper-filter-tab paper-filter-tab--skipped" data-filter="unanswered">
                                <span class="material-symbols-outlined">remove</span>
                                Bỏ (${empty paperSummary.unansweredCount ? 0 : paperSummary.unansweredCount})
                            </button>
                        </div>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <!--paper table-->
                <div class="paper-table-wrap">
                    <table class="paper-table" id="paperTable">
                        <thead>
                            <tr>
                                <th class="paper-th paper-th--no">Câu<br/>hỏi</th>
                                <th class="paper-th paper-th--content">Nội dung</th>
                                <th class="paper-th paper-th--answer">Đáp án</th>
                                <th class="paper-th paper-th--student">Thí sinh trả<br/>lời</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty paperAnswers}">
                                    <tr><td colspan="4" class="examiner-table__empty">Chưa có dữ liệu đề thi.</td></tr>
                                </c:when>
                                <c:otherwise>
                            <c:forEach items="${paperAnswers}" var="q" varStatus="st">
                                <tr class="paper-tr paper-tr--${q.answerStatus}<c:if test="${st.index % 2 == 1}"> paper-tr--alt</c:if>" data-answer-status="${q.answerStatus}">
                                    <td class="paper-td paper-td--no">${q.questionNo}</td>
                                    <td class="paper-td paper-td--content"><img src="${q.imageUrl}" alt="Q-${q.questionNo}" class="paper-img"/></td>
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
            </main>
        </div>

        <script>
            (function () {
                var tabs = document.querySelectorAll('.paper-filter-tab[data-filter]');
                var rows = document.querySelectorAll('#paperTable tbody tr[data-answer-status]');
                tabs.forEach(function (tab) {
                    tab.addEventListener('click', function () {
                        var filter = tab.getAttribute('data-filter');
                        tabs.forEach(function (item) { item.classList.remove('is-active'); });
                        tab.classList.add('is-active');
                        rows.forEach(function (row) {
                            var status = row.getAttribute('data-answer-status');
                            row.hidden = filter !== 'all' && status !== filter;
                        });
                    });
                });
            })();
        </script>
    </body>
</html>
