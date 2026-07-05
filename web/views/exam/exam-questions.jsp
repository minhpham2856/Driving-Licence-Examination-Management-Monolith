<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:set var="timeLeft" value="${not empty param.timeLeft ? param.timeLeft : '19:59'}" />
<c:set var="currentQuestion" value="${not empty param.currentQuestion ? param.currentQuestion : 1}" />
<c:set var="totalQuestions" value="${not empty param.totalQuestions ? param.totalQuestions : 35}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Câu hỏi thi | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@700;800&display=swap" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/exam-questions.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="/views/layout/header-exam-time.jsp">
            <jsp:param name="timeLeft" value="${timeLeft}" />
        </jsp:include>

        <main class="questions-page" data-node-id="115:835" data-name="Html → Body">
            <section class="questions-layout" data-node-id="115:854" data-name="Main Content Layout">
                <div class="question-stage" data-node-id="115:855" data-name="Section - Left Panel: Question Content Area (approx 75%)">
                    <div class="question-stage__media-wrap" data-node-id="115:856" data-name="Container">
                        <div class="question-stage__media" data-node-id="115:861" data-name="Media/Illustration">
                            <img
                                class="question-stage__image"
                                src="${pageContext.request.contextPath}/assets/imgs/exam-question-traffic.png"
                                alt="Tình huống giao thông"
                                data-node-id="115:862"
                                >
                        </div>
                    </div>
                </div>

                <aside class="question-sidebar" data-node-id="115:889" data-name="Aside - Right Panel: Question Navigator Sidebar (approx 25%)">
                    <c:if test="${not empty faceMatchRate}">
                        <p class="face-match-result">Kết quả quét mặt: ${faceMatchRate}% khớp hồ sơ</p>
                    </c:if>
                    <form class="question-sidebar-form" action="${pageContext.request.contextPath}/exam/submit" method="post">
                        <div class="question-grid-area" data-node-id="115:898" data-name="Question Grid Area">
                            <table class="question-table">
                                <thead>
                                    <tr>
                                        <th>Câu</th>
                                        <th>1</th>
                                        <th>2</th>
                                        <th>3</th>
                                        <th>4</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="q" begin="1" end="${totalQuestions}">
                                        <c:set var="answered" value="${q lt currentQuestion}" />
                                        <c:set var="active" value="${q eq currentQuestion}" />
                                        <tr class="${active ? 'active-row' : ''}">
                                            <td>
                                                <a href="${pageContext.request.contextPath}/exam/questions?currentQuestion=${q}" class="question-link">
                                                    Câu ${q}
                                                </a>
                                            </td>
                                            <td>
                                                <input type="checkbox" name="q_${q}" value="1" id="q${q}_1">
                                                <label for="q${q}_1">1</label>
                                            </td>
                                            <td>
                                                <input type="checkbox" name="q_${q}" value="2" id="q${q}_2">
                                                <label for="q${q}_2">2</label>
                                            </td>
                                            <td>
                                                <input type="checkbox" name="q_${q}" value="3" id="q${q}_3">
                                                <label for="q${q}_3">3</label>
                                            </td>
                                            <td>
                                                <input type="checkbox" name="q_${q}" value="4" id="q${q}_4">
                                                <label for="q${q}_4">4</label>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <div class="submit-panel" data-node-id="115:1565" data-name="CTA Footer">
                            <button type="submit" formaction="${pageContext.request.contextPath}/exam/save" formmethod="post" class="submit-panel__button submit-panel__button--secondary">LƯU BÀI</button>
                            <button type="submit" class="submit-panel__button" data-node-id="115:1566">NỘP BÀI</button>
                        </div>
                    </form>
                </aside>
            </section>
        </main>

        <div class="questions-progress" data-node-id="116:54" aria-hidden="true">
            <div class="questions-progress__track">
                <div class="questions-progress__filled"></div>
            </div>
        </div>

        <jsp:include page="/views/layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="Lưu ý:" />
            <jsp:param name="noticeText" value="Phần thi kết thúc khi thí sinh chọn “NỘP BÀI” hoặc khi hết thời gian làm bài" />
</body>
</html>
