<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>



<c:set var="hasSession" value="${not empty requestScope.sessionId and requestScope.sessionId gt 0}" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:choose>

    <c:when test="${hasSession}">

        <c:set var="backUrl" value="${ctx}/views/staff/examstaff/candidatecall?sessionId=${sessionId}" />

    </c:when>

    <c:otherwise>

        <c:set var="backUrl" value="${ctx}/views/staff/examstaff/candidatecall" />

    </c:otherwise>

</c:choose>



<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>MÀN HÌNH GỌI THÍ SINH - PHÒNG CHỜ CHÍNH</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">

    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/public-call.css">

</head>

<body class="public-call-mode${not hasSession ? ' public-call-mode--no-session' : ''}"

      data-call-ctx="${ctx}"

      data-call-session-id="${hasSession ? sessionId : 0}"

      data-msg-no-session-label="Chưa chọn ca thi"

      data-msg-no-session-queue="Chưa có ca thi — không hiển thị danh sách chờ"

      data-msg-no-session-sync="Chưa kết nối ca thi"

      data-msg-queue-empty="Không còn thí sinh chờ gọi"

      data-msg-shift-ended="Kỳ thi đã đóng"

      data-msg-exam-paused="Kỳ thi tạm dừng — chờ tiếp tục"

      data-msg-class-prefix="Hạng "

      data-msg-call-prefix="Mời thí sinh số báo danh "

      data-msg-call-suffix=", nhanh chóng đến bàn thủ tục chính với căn cước công dân."

      data-msg-prepare-prefix="Thí sinh số báo danh "

      data-msg-prepare-suffix=", "

      data-msg-prepare-tail="xin chuẩn bị, sắp đến lượt làm thủ tục tại bàn."

      data-msg-queue-prepare-badge="Chuẩn bị"

      data-msg-session-prefix="Phòng chờ chính — Ca thi "

      data-msg-sync-connecting="Đang kết nối..."

      data-msg-sync-ready="Đồng bộ &amp; loa sẵn sàng"

      data-msg-sync-needs-audio="Đồng bộ — cần bật loa một lần"

      data-msg-sync-no-speech="Trình duyệt không hỗ trợ đọc loa"

      data-msg-sync-speech-error="Lỗi phát loa — kiểm tra âm lượng TV"

      data-msg-sync-offline="Mất kết nối — thử lại..."

      data-msg-audio-unlock="Đã bật loa">



    <div id="audioGate" class="audio-gate">

        <div class="audio-gate__card">

            <div class="audio-gate__title">Bật loa màn hình TV</div>

            <p class="audio-gate__desc">Bấm một lần để cho phép phát loa khi có thí sinh được gọi.</p>

            <button type="button" id="btnEnableAudio" class="audio-gate__btn">Bật loa</button>

        </div>

    </div>



    <header class="tv-top-bar">

        <a href="${backUrl}" class="tv-back-btn" aria-label="Quay lại" title="Quay lại gọi thủ tục">

            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" aria-hidden="true">

                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" fill="currentColor"/>

            </svg>

        </a>

        <span class="tv-session-badge" id="sessionBadge">

            <c:choose>

                <c:when test="${hasSession and not empty currentSession}">

                    Phòng chờ chính — Ca thi <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />

                </c:when>

                <c:when test="${hasSession}">Phòng chờ chính</c:when>

                <c:otherwise>Chưa chọn ca thi</c:otherwise>

            </c:choose>

        </span>

        <span id="syncStatus" class="tv-status-pill">Đang kết nối...</span>

    </header>



    <main class="tv-layout-main">

        <section class="tv-queue-panel" id="queuePanel" aria-label="Danh sách chờ gọi">

            <h1 class="tv-queue-panel__title">Danh sách chờ gọi thủ tục</h1>

            <ul class="tv-queue-list" id="queueList">

                <c:choose>

                    <c:when test="${not hasSession}">

                        <li class="tv-queue-empty">Chưa có ca thi — không hiển thị danh sách chờ</li>

                    </c:when>

                    <c:when test="${empty requestScope.waitingQueue}">

                        <li class="tv-queue-empty">Không còn thí sinh chờ gọi</li>

                    </c:when>

                    <c:otherwise>

                        <c:forEach var="wc" items="${requestScope.waitingQueue}" varStatus="st">

                            <li class="tv-queue-item${st.index eq 0 ? ' tv-queue-item--head' : ''}${requestScope.isCallingActive and callingCandidate.sbd eq wc.sbd ? ' tv-queue-item--calling' : ''}">

                                <span class="tv-queue-item__sbd">${wc.sbd}</span>

                                <span class="tv-queue-item__sep" aria-hidden="true">—</span>

                                <span class="tv-queue-item__name">${wc.name}</span>

                            </li>

                        </c:forEach>

                    </c:otherwise>

                </c:choose>

            </ul>

        </section>

    </main>



    <script src="${ctx}/assets/js/public-call.js?v=11" charset="UTF-8"></script>

</body>

</html>


