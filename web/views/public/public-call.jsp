<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${empty requestScope.sessionId}">
    <c:redirect url="/views/public/public-call">
        <c:param name="sessionId" value="${param.sessionId}" />
    </c:redirect>
</c:if>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MÀN HÌNH GỌI THÍ SINH - PHÒNG CHỜ CHÍNH</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/public-call.css">
</head>
<body class="public-call-mode"
      data-call-ctx="${pageContext.request.contextPath}"
      data-call-session-id="${sessionId}">

    <div id="audioGate" class="audio-gate">
        <div class="audio-gate__card">
            <div class="audio-gate__title">Bật loa màn hình TV</div>
            <p class="audio-gate__desc">
                Trình duyệt yêu cầu bấm một lần để cho phép phát loa tự động khi có thí sinh được gọi.
            </p>
            <button type="button" id="btnEnableAudio" class="audio-gate__btn">Bật loa</button>
        </div>
    </div>

    <div class="tv-top-bar">
        <span class="tv-session-badge" id="sessionBadge">
            Hội trường phòng chờ chính
            <c:if test="${not empty currentSession}">
                &mdash; Ca thi
                <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />
            </c:if>
        </span>
        <span id="syncStatus" class="tv-status-pill">Đang kết nối...</span>
    </div>

    <div class="tv-grid">
        <div id="panelCurrent" class="tv-panel ${requestScope.isCallingActive ? 'tv-panel--current' : 'tv-panel--idle'}">
            <div id="currentPulsar" class="call-pulsar call-pulsar--live" style="${requestScope.isCallingActive ? '' : 'display:none;'}">
                <span class="pulsar-dot pulsar-dot--red"></span>
                <span>ĐANG PHÁT LOA</span>
            </div>

            <div id="currentLabel" class="panel-label panel-label--current">
                ${requestScope.isCallingActive ? 'Thí sinh đang được gọi' : 'Chưa có lượt gọi'}
            </div>

            <div id="currentBody">
                <c:choose>
                    <c:when test="${requestScope.isCallingActive}">
                        <div class="sbd-current" id="currentSbd">${callingCandidate.sbd}</div>
                        <div class="name-current" id="currentName">${callingCandidate.name}</div>
                        <div class="class-tag" id="currentClass">Hạng ${callingCandidate.clazz}</div>
                        <div class="call-soundwaves" id="currentWaves">
                            <div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>
                            <div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>
                            <div class="wave-bar"></div><div class="wave-bar"></div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="sbd-current" id="currentSbd" style="background: linear-gradient(135deg, #94a3b8, #64748b); -webkit-background-clip: text; font-size: 3rem;">CHỜ GỌI</div>
                        <div class="name-current" id="currentName" style="color: #94a3b8; font-size: 1.5rem;">Vui lòng theo dõi bảng bên phải</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="tv-panel tv-panel--next">
            <div class="call-pulsar call-pulsar--ready">
                <span class="pulsar-dot pulsar-dot--green"></span>
                <span>CHUẨN BỊ TIẾP THEO</span>
            </div>

            <div class="panel-label panel-label--next">Thí sinh chuẩn bị vào bàn</div>

            <div id="nextBody">
                <c:choose>
                    <c:when test="${not empty requestScope.nextCandidate}">
                        <div class="sbd-next" id="nextSbd">${nextCandidate.sbd}</div>
                        <div class="name-next" id="nextName">${nextCandidate.name}</div>
                        <div class="class-tag" id="nextClass" style="color: #6ee7b7;">Hạng ${nextCandidate.clazz}</div>
                        <p id="nextHint" style="margin-top: 1.25rem; font-size: 0.9rem; color: #64748b; max-width: 320px; line-height: 1.5;">
                            Giữ sẵn CCCD, chuẩn bị di chuyển vào bàn thủ tục ngay sau khi người trước hoàn tất.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="sbd-next" id="nextSbd" style="color: #64748b; font-size: 2rem;">--</div>
                        <div class="name-next" id="nextName" style="color: #64748b; font-size: 1.1rem;">
                            <c:choose>
                                <c:when test="${requestScope.shiftEnded}">Ca thi đã kết thúc</c:when>
                                <c:otherwise>Không còn thí sinh chờ trong hàng đợi</c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <div class="instruction-box" id="instructionBox">
        <c:choose>
            <c:when test="${requestScope.isCallingActive}">
                Mời thí sinh <strong style="color: #f8fafc;">${callingCandidate.sbd} &mdash; ${callingCandidate.name}</strong>
                nhanh chóng đến <strong style="color: #60a5fa;">Bàn thủ tục chính</strong> với CCCD để đối chiếu hồ sơ, chụp ảnh chân dung và đóng lệ phí thi.
            </c:when>
            <c:otherwise>
                Thí sinh vui lòng chuẩn bị sẵn <strong style="color: #f8fafc;">thẻ CCCD</strong>, tập trung trật tự tại phòng chờ và theo dõi bảng gọi thi.
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tv-footer">
        Bảng truyền thông sát hạch lái xe Lái Vui &copy; 2026 &mdash; Loa Web Speech (trình duyệt)
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/public-call.js?v=2"></script>

</body>
</html>
