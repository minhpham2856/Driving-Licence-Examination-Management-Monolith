<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Kết quả thi - Driving Licence Examination</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@700;800&display=swap" rel="stylesheet">

        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/exam-results.css">
    </head>
    <body>

        <jsp:include page="../layout/header-exam-time.jsp">
            <jsp:param name="timeLeft" value="08:22:38" />
        </jsp:include>

        <main class="exam-results-main">
            <div class="results-wrapper">
                <!-- Candidate Info Section -->
                <section class="info-card" data-name="Thông tin thí sinh card">
                    <h2 class="card-title">Thông tin thí sinh</h2>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="info-label">SBD:</span>
                            <span class="info-value">${not empty candidate.sbd ? candidate.sbd : '123'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Hạng:</span>
                            <span class="info-value">${not empty candidate.licenseClass ? candidate.licenseClass : 'B'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Họ & Tên:</span>
                            <span class="info-value">${not empty candidate.fullName ? candidate.fullName : 'Nguyễn Văn An'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Địa chỉ:</span>
                            <span class="info-value">${not empty candidate.address ? candidate.address : 'Hà Nội'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số CC:</span>
                            <span class="info-value">${not empty candidate.citizenId ? candidate.citizenId : '0123456789098'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Ngày sinh:</span>
                            <span class="info-value">${not empty candidate.dob ? candidate.dob : '20/2/2000'}</span>
                        </div>
                    </div>
                </section>

                <!-- Exam Results Section -->
                <section class="info-card" data-name="Kết quả thi card">
                    <div class="results-header-row">
                        <h2 class="card-title" style="margin-bottom: 0;">Kết quả thi</h2>
                        <c:set var="isPassed" value="${result.status eq 'PASSED' || (not empty result.score && result.score >= 32)}" />
                        <c:choose>
                            <c:when test="${isPassed}">
                                <div class="status-badge status-badge--pass">ĐẠT</div>
                            </c:when>
                            <c:otherwise>
                                <div class="status-badge status-badge--fail">KHÔNG ĐẠT</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="info-label">Điểm:</span>
                            <span class="info-value">${not empty result.score ? result.score : '32'}/${not empty result.totalQuestions ? result.totalQuestions : '35'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu trả lời đúng:</span>
                            <span class="info-value">${not empty result.correctCount ? result.correctCount : '32'}/${not empty result.totalQuestions ? result.totalQuestions : '35'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Giờ làm bài:</span>
                            <span class="info-value">${not empty result.startTime ? result.startTime : '14h20p'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu trả lời sai:</span>
                            <span class="info-value">${not empty result.incorrectCount ? result.incorrectCount : '3'}/${not empty result.totalQuestions ? result.totalQuestions : '35'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Giờ nộp bài:</span>
                            <span class="info-value">${not empty result.endTime ? result.endTime : '14h36p'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu không trả lời:</span>
                            <span class="info-value">${not empty result.unansweredCount ? result.unansweredCount : '0'}/${not empty result.totalQuestions ? result.totalQuestions : '35'}</span>
                        </div>
                    </div>
                </section>

                <!-- Actions and Notices -->
                <div class="action-container">
                    <a href="${ctx}/candidate/dashboard" class="exit-btn">Thoát</a>
                    <p class="notice-text">Thí sinh di chuyển tới bàn ký tên</p>
                </div>
            </div>
        </main>

        <jsp:include page="../layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="Lưu ý:" />
            <jsp:param name="noticeText" value="Thí sinh di chuyển tới bàn ký tên để hoàn tất thủ tục sau khi xem kết quả." />
        </jsp:include>

    </body>
</html>
