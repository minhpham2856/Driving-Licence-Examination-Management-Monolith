<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bài thi lý thuyết</title>
    <link href="${pageContext.request.contextPath}/assets/css/exam/exam-questions.css" rel="stylesheet">
</head>
<body>
<main>
    <header><h1>Bài thi lý thuyết - SBD ${candidateExam.candidateNumber}</h1>
        <strong id="remainingTime" data-seconds="${durationSeconds}"></strong></header>
    <form method="post" action="${pageContext.request.contextPath}/exam/submit" id="examForm">
        <c:forEach var="q" items="${questions}" varStatus="status">
            <fieldset>
                <legend>Câu ${status.count}</legend>
                <c:if test="${not empty q.imageUrl}"><img src="${q.imageUrl}" alt="Hình câu hỏi ${status.count}"></c:if>
                <c:forEach var="choice" items="${['A','B','C','D']}">
                    <label><input type="radio" name="ans_${q.questionId}" value="${choice}"> ${choice}</label>
                </c:forEach>
            </fieldset>
        </c:forEach>
        <button type="submit">Nộp bài</button>
    </form>
</main>
<script>
(function () {
    var timer = document.getElementById('remainingTime');
    var seconds = Number(timer.dataset.seconds || 0);
    var handle = window.setInterval(function () {
        timer.textContent = Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0');
        if (seconds-- <= 0) {
            window.clearInterval(handle);
            document.getElementById('examForm').submit();
        }
    }, 1000);
}());
</script>
</body>
</html>
