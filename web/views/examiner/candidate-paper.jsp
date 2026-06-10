<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/layout/examiner-seed-data.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Đề thi" />
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details-edit.jsp?sbd=${candidate.sbd}" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-paper.jsp?sbd=${candidate.sbd}" />

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
        <link rel="stylesheet" href="${cssStyle}">
        <link rel="stylesheet" href="${cssLayout}">
    </head>
    <body class="has-side-nav-bar examiner-portal">

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
                        <h2 class="examiner-toolbar__title">Đề thi — ${candidate.fullName}</h2>
                    </div>
                    <div class="examiner-toolbar__actions">
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>
                            In kết quả
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">filter_alt</span>
                            Lọc
                        </a>
                        <div class="paper-filter-tabs">
                            <span class="paper-filter-tab paper-filter-tab--correct">
                                <span class="material-symbols-outlined">check</span>
                                Câu đúng (${paperSummary.correctCount})
                            </span>
                            <span class="paper-filter-tab paper-filter-tab--wrong">
                                <span class="material-symbols-outlined">close</span>
                                Câu sai (${paperSummary.wrongCount})
                            </span>
                        </div>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <!--paper table-->
                <div class="paper-table-wrap">
                    <table class="paper-table">
                        <thead>
                            <tr>
                                <th class="paper-th paper-th--no">Câu<br/>hỏi</th>
                                <th class="paper-th paper-th--content">Nội dung</th>
                                <th class="paper-th paper-th--answer">Đáp án</th>
                                <th class="paper-th paper-th--student">Thí sinh trả<br/>lời</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${paperAnswers}" var="q" varStatus="st">
                                <tr class="paper-tr<c:if test="${st.index % 2 == 1}"> paper-tr--alt</c:if>">
                                    <td class="paper-td paper-td--no">${q.questionNo}</td>
                                    <td class="paper-td paper-td--content"><img src="${q.imageUrl}" alt="Q-${q.questionNo}" class="paper-img"/></td>
                                    <td class="paper-td paper-td--answer">${q.correctAnswer}</td>
                                    <td class="paper-td paper-td--student">
                                        <span class="paper-ans paper-ans--${q.correct ? 'correct' : 'wrong'}">${q.studentAnswer}</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>

    </body>
</html>
