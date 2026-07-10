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
            <jsp:param name="timeLeft" value="00:00:00" />
        </jsp:include>

        <main class="exam-results-main">
            <div class="results-wrapper">
                <!-- Candidate Info Section -->
                <section class="info-card" data-name="Thông tin thí sinh card">
                    <h2 class="card-title">Thông tin thí sinh</h2>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="info-label">SBD:</span>
                            <span class="info-value">${candidate.sbd}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Hạng:</span>
                            <span class="info-value">${candidate.licenseClass}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Họ &amp; Tên:</span>
                            <span class="info-value">${candidate.fullName}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Địa chỉ:</span>
                            <span class="info-value">${empty candidate.address ? '—' : candidate.address}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số CC:</span>
                            <span class="info-value">${candidate.citizenId}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Ngày sinh:</span>
                            <span class="info-value">${candidate.dob}</span>
                        </div>
                    </div>
                </section>

                <!-- Exam Results Section -->
                <section class="info-card" data-name="Kết quả thi card">
                    <div class="results-header-row">
                        <h2 class="card-title" style="margin-bottom: 0;">Kết quả thi</h2>
                        <c:choose>
                            <c:when test="${result.passed}">
                                <div class="status-badge status-badge--pass">ĐẠT</div>
                            </c:when>
                            <c:otherwise>
                                <div class="status-badge status-badge--fail">KHÔNG ĐẠT</div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${result.criticalFailed}">
                        <p style="margin:0.75rem 0 0; padding:0.6rem 0.9rem; border-radius:8px;
                                  background:rgba(239,68,68,0.08); border:1px solid rgba(239,68,68,0.25);
                                  color:#b91c1c; font-weight:600; font-size:0.85rem;">
                            Không đạt do sai câu điểm liệt (dù tổng số câu đúng có thể đã đủ).
                        </p>
                    </c:if>

                    <div class="info-grid" style="margin-top: 1rem;">
                        <div class="info-item">
                            <span class="info-label">Điểm:</span>
                            <span class="info-value">${result.score}/${result.totalQuestions}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu trả lời đúng:</span>
                            <span class="info-value">${result.correctCount}/${result.totalQuestions}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Giờ làm bài:</span>
                            <span class="info-value">${result.startTime}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu trả lời sai:</span>
                            <span class="info-value">${result.incorrectCount}/${result.totalQuestions}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Giờ nộp bài:</span>
                            <span class="info-value">${result.endTime}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Số câu không trả lời:</span>
                            <span class="info-value">${result.unansweredCount}/${result.totalQuestions}</span>
                        </div>
                    </div>
                </section>

                <!-- Actions and Notices -->
                <div class="action-container">
                    <a href="${ctx}/exam/entrance" class="exit-btn">Thoát</a>
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
