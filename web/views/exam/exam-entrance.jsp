

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nhập SBD | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@800&display=swap"
            rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/exam-entrance.css" rel="stylesheet">

    </head>

    <body>
        <jsp:include page="/views/layout/header-exam-time.jsp">
            <jsp:param name="timeLeft" value="${not empty timeLeft ? timeLeft : '08:22:38'}" />
        </jsp:include>

        <main class="entrance-shell">
            <section class="entrance-panel" aria-label="Nhập số báo danh">
                <form class="sbd-card" action="${pageContext.request.contextPath}/exam/entrance" method="post">
                    <label class="sbd-label" for="sbdInput">SỐ BÁO DANH (SBD)</label>

                    <div class="sbd-input-wrap">
                        <input id="sbdInput" name="sbd" class="sbd-input" type="text" inputmode="numeric"
                               autocomplete="off" maxlength="6" value="${not empty param.sbd ? param.sbd : ''}"
                               placeholder="">
                        <button type="button" class="sbd-input-action" aria-label="Xóa ký tự cuối">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M3 6h7l7 6-7 6H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"></path>
                            <line x1="14" y1="9" x2="18" y2="13"></line>
                            <line x1="18" y1="9" x2="14" y2="13"></line>
                            </svg>
                        </button>
                    </div>

                    <div class="keypad" aria-label="Bàn phím số">
                        <button type="button" class="key">1</button>
                        <button type="button" class="key">2</button>
                        <button type="button" class="key">3</button>
                        <button type="button" class="key">4</button>
                        <button type="button" class="key">5</button>
                        <button type="button" class="key">6</button>
                        <button type="button" class="key">7</button>
                        <button type="button" class="key">8</button>
                        <button type="button" class="key">9</button>
                        <button type="button" class="key key--danger">XÓA</button>
                        <button type="button" class="key">0</button>
                        <button type="submit" class="key key--primary">OK</button>
                    </div>

                    <button type="submit" class="check-button" aria-label="Kiểm tra thông tin">
                        <span>KIỂM TRA THÔNG TIN</span>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                        </svg>
                    </button>
                </form>
            </section>
        </main>

        <jsp:include page="/views/layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="Lưu ý:" />
            <jsp:param name="noticeText"
                       value="Sử dụng bàn phím trên màn hình hoặc bàn phím số bên phải của máy tính" />
        </jsp:include>
    </body>

</html>
