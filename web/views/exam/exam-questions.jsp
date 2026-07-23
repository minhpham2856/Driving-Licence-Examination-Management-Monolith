<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bài thi lý thuyết | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam/exam-questions.css" rel="stylesheet">
</head>
<body class="exam-paper-body">
<main class="exam-paper-shell">
    <header class="exam-paper-header">
        <div>
            <p class="exam-paper-eyebrow">Bài thi lý thuyết</p>
            <h1>SBD <c:out value="${candidateExam.candidateNumber}"/></h1>
        </div>
        <div class="exam-paper-timer">
            <span class="material-symbols-outlined">timer</span>
            <strong id="remainingTime" data-seconds="${durationSeconds}"></strong>
        </div>
    </header>

    <form method="post" action="${ctx}/exam/submit" id="examForm" class="exam-paper-form">
        <c:forEach var="q" items="${questions}" varStatus="status">
            <section class="exam-question-card">
                <div class="exam-question-card__head">
                    <span>Câu ${status.count}</span>
                    <c:if test="${q.critical}">
                        <em>Câu điểm liệt</em>
                    </c:if>
                </div>
                <c:if test="${not empty q.imageUrl}">
                    <img class="exam-question-card__image" src="${q.imageUrl}" alt="Hình câu hỏi ${status.count}">
                </c:if>
                <div class="exam-choice-grid">
                    <label class="exam-choice">
                        <input type="radio" name="ans_${q.questionId}" value="A">
                        <span>A</span>
                    </label>
                    <label class="exam-choice">
                        <input type="radio" name="ans_${q.questionId}" value="B">
                        <span>B</span>
                    </label>
                    <label class="exam-choice">
                        <input type="radio" name="ans_${q.questionId}" value="C">
                        <span>C</span>
                    </label>
                    <label class="exam-choice">
                        <input type="radio" name="ans_${q.questionId}" value="D">
                        <span>D</span>
                    </label>
                </div>
            </section>
        </c:forEach>
        <div class="exam-submit-bar">
            <button type="submit" class="exam-submit-button">
                <span class="material-symbols-outlined">send</span>
                Nộp bài
            </button>
        </div>
    </form>
</main>
<script>
(function () {
    var timer = document.getElementById('remainingTime');
    var form = document.getElementById('examForm');
    var seconds = Number(timer.dataset.seconds || 0);

    function render() {
        var minutes = Math.floor(seconds / 60);
        var rest = String(seconds % 60).padStart(2, '0');
        timer.textContent = minutes + ':' + rest;
    }

    render();
    var handle = window.setInterval(function () {
        seconds -= 1;
        if (seconds <= 0) {
            window.clearInterval(handle);
            timer.textContent = '0:00';
            form.submit();
            return;
        }
        render();
    }, 1000);
}());
</script>
</body>
</html>
