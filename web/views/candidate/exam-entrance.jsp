<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhập SBD | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #eef2f7; color: #0f172a; }
        .entrance-shell { min-height: calc(100vh - 160px); display: flex; align-items: center; justify-content: center; padding: 6rem 1rem 5rem; }
        .sbd-card { width: 100%; max-width: 440px; background: #fff; border: 1px solid #e2e8f0; border-radius: 20px; padding: 2rem; box-shadow: 0 10px 30px rgba(15,23,42,.06); }
        .sbd-label { display: block; font-size: .8rem; font-weight: 700; letter-spacing: .5px; color: #0052cc; margin-bottom: .6rem; }
        .sbd-input-wrap { position: relative; margin-bottom: 1.25rem; }
        .sbd-input { width: 100%; height: 60px; padding: 0 48px 0 16px; font-size: 1.6rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; letter-spacing: 1px; border: 2px solid #e2e8f0; border-radius: 12px; background: #f8fafc; color: #0f172a; }
        .sbd-input:focus { outline: none; border-color: #0052cc; background: #fff; }
        .sbd-input-action { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 6px; }
        .keypad { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 1.25rem; }
        .key { height: 60px; border: 1.5px solid #e2e8f0; border-radius: 12px; background: #f8fafc; font-size: 1.35rem; font-weight: 700; color: #0f172a; cursor: pointer; transition: all .12s; }
        .key:hover { border-color: #0052cc; background: #eef4ff; }
        .key:active { transform: scale(.97); }
        .key--danger { color: #dc2626; font-size: 1rem; }
        .key--primary { background: #0052cc; border-color: #0052cc; color: #fff; }
        .key--primary:hover { background: #0041a3; }
        .check-button { width: 100%; height: 56px; display: inline-flex; align-items: center; justify-content: center; gap: 10px; border: none; border-radius: 14px; background: #059669; color: #fff; font-size: 1rem; font-weight: 800; letter-spacing: .5px; cursor: pointer; }
        .check-button:hover { background: #047a55; }
        .err-box { margin: 0 0 12px; padding: 10px 14px; border-radius: 10px; text-align: center; background: rgba(239,68,68,.08); border: 1px solid rgba(239,68,68,.25); color: #b91c1c; font-weight: 600; font-size: .9rem; }
    </style>
</head>
<body>
    <jsp:include page="/views/layout/header-exam-time.jsp">
        <jsp:param name="timeLeft" value="08:22:38" />
    </jsp:include>

    <main class="entrance-shell">
        <form class="sbd-card" action="${ctx}/exam/entrance" method="post">
            <c:if test="${not empty error}">
                <p class="err-box">${error}</p>
            </c:if>

            <label class="sbd-label" for="sbdInput">SỐ BÁO DANH (SBD)</label>
            <div class="sbd-input-wrap">
                <input id="sbdInput" name="sbd" class="sbd-input" type="text" autocomplete="off"
                       maxlength="20" value="${not empty param.sbd ? param.sbd : ''}" placeholder="SBD-...">
                <button type="button" class="sbd-input-action" aria-label="Xóa" onclick="backspaceSbd()">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 6h7l7 6-7 6H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"></path>
                        <line x1="14" y1="9" x2="18" y2="13"></line><line x1="18" y1="9" x2="14" y2="13"></line>
                    </svg>
                </button>
            </div>

            <div class="keypad">
                <button type="button" class="key" onclick="pressKey('1')">1</button>
                <button type="button" class="key" onclick="pressKey('2')">2</button>
                <button type="button" class="key" onclick="pressKey('3')">3</button>
                <button type="button" class="key" onclick="pressKey('4')">4</button>
                <button type="button" class="key" onclick="pressKey('5')">5</button>
                <button type="button" class="key" onclick="pressKey('6')">6</button>
                <button type="button" class="key" onclick="pressKey('7')">7</button>
                <button type="button" class="key" onclick="pressKey('8')">8</button>
                <button type="button" class="key" onclick="pressKey('9')">9</button>
                <button type="button" class="key key--danger" onclick="backspaceSbd()">XÓA</button>
                <button type="button" class="key" onclick="pressKey('0')">0</button>
                <button type="submit" class="key key--primary">OK</button>
            </div>

            <button type="submit" class="check-button">
                <span>KIỂM TRA THÔNG TIN</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline>
                </svg>
            </button>
        </form>
    </main>

    <jsp:include page="/views/layout/footer-exam.jsp">
        <jsp:param name="noticeTitle" value="Lưu ý:" />
        <jsp:param name="noticeText" value="Nhập đúng số báo danh (VD: SBD-202611) rồi bấm OK. Có thể dùng bàn phím số." />
    </jsp:include>

    <script>
        function pressKey(d){ var i=document.getElementById('sbdInput'); i.value+=d; i.focus(); }
        function backspaceSbd(){ var i=document.getElementById('sbdInput'); i.value=i.value.slice(0,-1); i.focus(); }
    </script>
</body>
</html>
