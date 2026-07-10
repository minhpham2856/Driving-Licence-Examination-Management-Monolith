<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Câu hỏi thi | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@600&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam-questions.css" rel="stylesheet">
    <style>
        html, body { height:auto; min-height:100%; overflow-y:auto; }
        .qz-wrap { max-width:1200px; margin:1.5rem auto; padding:1.5rem 1rem 0; display:grid; grid-template-columns:1fr 320px; gap:1.25rem; font-family:'Inter',sans-serif; align-items:start; }
        .qz-stage { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:1.5rem; overflow:visible; }
        .qz-qtitle { font-size:1.05rem; font-weight:800; color:#0f172a; margin:0 0 1rem; }
        .qz-crit { display:inline-block; margin-left:8px; font-size:0.72rem; font-weight:700; color:#b91c1c; background:rgba(239,68,68,0.1); padding:2px 8px; border-radius:9999px; }
        .qz-img { width:100%; max-height:360px; object-fit:contain; border-radius:12px; background:#f8fafc; margin-bottom:1.25rem; }
        .qz-opts { display:flex; flex-direction:column; gap:10px; }
        .qz-opt { display:flex; align-items:center; gap:12px; padding:0.75rem 1rem; border:1.5px solid #e2e8f0; border-radius:10px; cursor:pointer; font-weight:600; color:#334155; transition:border-color .15s; }
        .qz-opt:hover { border-color:#0052cc; }
        .qz-opt input { width:18px; height:18px; accent-color:#0052cc; }
        .qz-nav-btns { display:flex; justify-content:space-between; margin-top:1.25rem; }
        .qz-btn { padding:0.6rem 1.4rem; border-radius:10px; border:none; font-weight:700; cursor:pointer; }
        .qz-btn--ghost { background:#f1f5f9; color:#0f172a; }
        .qz-btn--primary { background:#0052cc; color:#fff; }
        .qz-side { background:#fff; border:1px solid #e2e8f0; border-radius:16px; padding:1.25rem; position:sticky; top:1rem; align-self:start; }
        .qz-timer { font-size:1.7rem; font-weight:800; text-align:center; color:#0052cc; font-variant-numeric:tabular-nums; margin-bottom:0.35rem; font-family:'JetBrains Mono',monospace; line-height:1.1; overflow:hidden; }
        .qz-timer.qz-danger { color:#dc2626; }
        .qz-meta { text-align:center; font-size:0.78rem; color:#64748b; margin-bottom:1rem; }
        .qz-grid { display:grid; grid-template-columns:repeat(5,1fr); gap:8px; margin-bottom:1.25rem; }
        .qz-cell { aspect-ratio:1; border:1.5px solid #e2e8f0; border-radius:8px; background:#fff; font-weight:700; color:#64748b; cursor:pointer; font-size:0.85rem; }
        .qz-cell.answered { background:#0052cc; border-color:#0052cc; color:#fff; }
        .qz-cell.current { outline:2px solid #f59e0b; outline-offset:1px; }
        .qz-submit { width:100%; padding:0.85rem; border:none; border-radius:12px; background:#059669; color:#fff; font-weight:800; font-size:1rem; cursor:pointer; }
        @media (max-width: 900px){ .qz-wrap{ grid-template-columns:1fr; } .qz-side{ position:static; } }
    </style>
</head>
<body>
    <jsp:include page="/views/layout/header-exam-time.jsp">
        <jsp:param name="timeLeft" value="00:00:00" />
    </jsp:include>

    <form id="examForm" action="${ctx}/exam/submit" method="post">
        <div class="qz-wrap">
            <div class="qz-stage">
                <c:forEach var="q" items="${questions}" varStatus="st">
                    <div class="qz-slide" data-index="${st.index}" style="${st.first ? '' : 'display:none;'}">
                        <h2 class="qz-qtitle">
                            Câu ${q.questionNumber} / ${totalQuestions}
                            <c:if test="${q.critical}"><span class="qz-crit">Câu điểm liệt</span></c:if>
                        </h2>
                        <c:choose>
                            <c:when test="${not empty q.imageUrl and (fn:startsWith(q.imageUrl,'http') or fn:startsWith(q.imageUrl,'/'))}">
                                <img class="qz-img" src="${q.imageUrl}" alt="Câu ${q.questionNumber}">
                            </c:when>
                            <c:when test="${not empty q.imageUrl}">
                                <img class="qz-img" src="${ctx}/${q.imageUrl}" alt="Câu ${q.questionNumber}">
                            </c:when>
                            <c:otherwise>
                                <img class="qz-img" src="${ctx}/assets/imgs/exam-question-traffic.png" alt="Câu ${q.questionNumber}">
                            </c:otherwise>
                        </c:choose>
                        <div class="qz-opts">
                            <c:forEach var="opt" begin="1" end="4">
                                <label class="qz-opt">
                                    <input type="radio" name="ans_${q.questionId}" value="${opt}"
                                           onchange="markAnswered(${st.index})">
                                    <span>Đáp án ${opt}</span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>

                <div class="qz-nav-btns">
                    <button type="button" class="qz-btn qz-btn--ghost" onclick="prevQ()">← Câu trước</button>
                    <button type="button" class="qz-btn qz-btn--primary" onclick="nextQ()">Câu sau →</button>
                </div>
            </div>

            <aside class="qz-side">
                <div id="qzTimer" class="qz-timer">--:--</div>
                <div class="qz-meta">Cần đúng tối thiểu ${passThreshold}/${totalQuestions} câu &amp; không sai câu điểm liệt</div>
                <div class="qz-grid">
                    <c:forEach var="q" items="${questions}" varStatus="st">
                        <button type="button" class="qz-cell${st.first ? ' current' : ''}" id="cell-${st.index}"
                                onclick="goQ(${st.index})">${q.questionNumber}</button>
                    </c:forEach>
                </div>
                <button type="button" class="qz-submit" onclick="confirmSubmit()">NỘP BÀI</button>
            </aside>
        </div>
    </form>


    <script>
        const total = ${totalQuestions};
        let current = 0;
        let remaining = ${durationSeconds};
        let submitted = false;

        function show(i){
            document.querySelectorAll('.qz-slide').forEach(s => s.style.display = (+s.dataset.index===i)?'block':'none');
            document.querySelectorAll('.qz-cell').forEach(c => c.classList.remove('current'));
            const cell = document.getElementById('cell-'+i);
            if(cell) cell.classList.add('current');
            current = i;
        }
        function goQ(i){ show(i); }
        function nextQ(){ if(current < total-1) show(current+1); }
        function prevQ(){ if(current > 0) show(current-1); }
        function markAnswered(i){ document.getElementById('cell-'+i).classList.add('answered'); }

        function doSubmit(){
            if(submitted) return;
            submitted = true;
            document.getElementById('examForm').submit();
        }
        function confirmSubmit(){
            const answered = document.querySelectorAll('.qz-cell.answered').length;
            if(confirm('Bạn đã trả lời ' + answered + '/' + total + ' câu.\nBạn chắc chắn muốn nộp bài?')) doSubmit();
        }

        function tick(){
            const t = document.getElementById('qzTimer');
            if(remaining <= 0){ t.textContent = '00:00'; doSubmit(); return; }
            const m = Math.floor(remaining/60), s = remaining%60;
            t.textContent = String(m).padStart(2,'0') + ':' + String(s).padStart(2,'0');
            if(remaining <= 60) t.classList.add('qz-danger');
            remaining--;
        }
        tick();
        setInterval(tick, 1000);
    </script>
</body>
</html>