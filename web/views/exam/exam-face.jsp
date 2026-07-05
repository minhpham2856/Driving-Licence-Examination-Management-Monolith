<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Xác minh & Chụp ảnh | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@700;800&display=swap" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/exam-face.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="/views/layout/header-exam-time.jsp">
            <jsp:param name="timeLeft" value="${not empty timeLeft ? timeLeft : '08:22:38'}" />
        </jsp:include>

        <main class="face-shell">
            <section class="face-main" aria-label="Xác minh và chụp ảnh">
                <div class="face-content">
                    <div class="guide-card">
                        <h1 class="guide-title">Hướng dẫn</h1>
                        <div class="guide-list">
                            <div class="guide-item">
                                <img class="guide-icon" src="${pageContext.request.contextPath}/assets/imgs/exam-face-note.svg" alt="" aria-hidden="true">
                                <p class="guide-text">Bỏ kính hoặc khẩu trang (nếu có)</p>
                            </div>
                            <div class="guide-item">
                                <img class="guide-icon" src="${pageContext.request.contextPath}/assets/imgs/exam-face-note.svg" alt="" aria-hidden="true">
                                <p class="guide-text">Giữ đầu thẳng, nhìn trực diện vào camera</p>
                            </div>
                            <div class="guide-item">
                                <img class="guide-icon" src="${pageContext.request.contextPath}/assets/imgs/exam-face-note.svg" alt="" aria-hidden="true">
                                <p class="guide-text">Tránh có người khác trong khung hình</p>
                            </div>
                        </div>
                    </div>

                    <div class="camera-frame" aria-label="Khung camera">
                        <img src="${pageContext.request.contextPath}/assets/imgs/exam-face-camera.png" alt="Ảnh xem trước khuôn mặt thí sinh">
                        <span class="camera-overlay" aria-hidden="true"></span>
                    </div>

                    <div class="capture-wrap">
                        <form action="${pageContext.request.contextPath}/exam/face" method="post">
                            <button type="submit" class="capture-btn">
                                <img class="capture-btn-icon" src="${pageContext.request.contextPath}/assets/imgs/exam-face-capture-icon.svg" alt="" aria-hidden="true">
                                <span>QUÉT MẶT</span>
                            </button>
                        </form>
                    </div>
                </div>
            </section>
        </main>

        <jsp:include page="/views/layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="Lưu ý:" />
            <jsp:param name="noticeText" value="Thí sinh đưa mặt bên trong khung hình rồi chọn “CHỤP ẢNH” để bắt đầu thi" />
</body>
</html>
