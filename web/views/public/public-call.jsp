<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    if (request.getAttribute("isCallingActive") == null) {
        String q = request.getQueryString();
        response.sendRedirect(request.getContextPath() + "/views/public/public-call" + (q != null ? "?" + q : ""));
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MÀN HÌNH GỌI THÍ SINH - PHÒNG CHỜ CHÍNH</title>
    <meta http-equiv="refresh" content="3">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">

    <style>
        body.public-call-mode {
            background: radial-gradient(circle at center, #0f172a, #020617);
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            padding: 2rem;
            box-sizing: border-box;
            gap: 1.5rem;
        }

        .tv-top-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            width: 100%;
        }

        .tv-session-badge {
            font-size: 0.85rem;
            font-weight: 800;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .tv-grid {
            display: grid;
            grid-template-columns: 1.4fr 1fr;
            gap: 1.5rem;
            flex: 1;
            align-items: stretch;
        }

        .tv-panel {
            background: rgba(30, 41, 59, 0.45);
            border-radius: 24px;
            backdrop-filter: blur(16px);
            padding: 2.5rem 2rem;
            text-align: center;
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 420px;
        }

        .tv-panel--current {
            border: 2px solid rgba(59, 130, 246, 0.45);
            box-shadow: 0 20px 50px -10px rgba(0, 0, 0, 0.5), inset 0 1px 1px rgba(255, 255, 255, 0.08);
        }

        .tv-panel--next {
            border: 2px solid rgba(16, 185, 129, 0.35);
            box-shadow: 0 12px 30px -8px rgba(0, 0, 0, 0.35);
        }

        .tv-panel--idle {
            border: 2px solid rgba(148, 163, 184, 0.25);
        }

        .panel-label {
            font-size: 1rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.12em;
            margin-bottom: 1.25rem;
        }

        .panel-label--current { color: #60a5fa; }
        .panel-label--next { color: #34d399; }
        .panel-label--idle { color: #94a3b8; }

        .sbd-current {
            font-size: 5.5rem;
            font-weight: 900;
            font-family: monospace;
            letter-spacing: 0.05em;
            background: linear-gradient(135deg, #60a5fa, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin: 0.5rem 0;
            line-height: 1;
        }

        .sbd-next {
            font-size: 3.5rem;
            font-weight: 900;
            font-family: monospace;
            color: #34d399;
            margin: 0.5rem 0;
            line-height: 1;
        }

        .name-current {
            font-size: 2.2rem;
            font-weight: 800;
            color: #ffffff;
            margin: 0.5rem 0;
        }

        .name-next {
            font-size: 1.6rem;
            font-weight: 800;
            color: #e2e8f0;
            margin: 0.5rem 0;
        }

        .class-tag {
            font-size: 1rem;
            font-weight: 700;
            color: #94a3b8;
            text-transform: uppercase;
            margin-top: 0.25rem;
        }

        .call-pulsar {
            position: absolute;
            top: 18px;
            right: 18px;
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 0.72rem;
            font-weight: 800;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .call-pulsar--live {
            background-color: rgba(239, 68, 68, 0.15);
            color: #ef4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .call-pulsar--ready {
            background-color: rgba(16, 185, 129, 0.15);
            color: #10b981;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }

        .pulsar-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            animation: pulse-dot 1.2s infinite;
        }

        .pulsar-dot--red { background-color: #ef4444; }
        .pulsar-dot--green { background-color: #10b981; }

        @keyframes pulse-dot {
            0% { transform: scale(0.8); opacity: 0.5; }
            50% { transform: scale(1.3); opacity: 1; }
            100% { transform: scale(0.8); opacity: 0.5; }
        }

        .call-soundwaves {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            height: 50px;
            margin-top: 1.5rem;
        }

        .wave-bar {
            width: 5px;
            height: 10px;
            background: linear-gradient(to top, #60a5fa, #10b981);
            border-radius: 99px;
            animation: bounce-wave 1.2s infinite alternate;
        }
        .wave-bar:nth-child(2) { height: 30px; animation-delay: 0.1s; }
        .wave-bar:nth-child(3) { height: 50px; animation-delay: 0.2s; }
        .wave-bar:nth-child(4) { height: 25px; animation-delay: 0.3s; }
        .wave-bar:nth-child(5) { height: 15px; animation-delay: 0.4s; }
        .wave-bar:nth-child(6) { height: 40px; animation-delay: 0.2s; }
        .wave-bar:nth-child(7) { height: 55px; animation-delay: 0.15s; }
        .wave-bar:nth-child(8) { height: 20px; animation-delay: 0.45s; }

        @keyframes bounce-wave {
            0% { transform: scaleY(0.2); }
            100% { transform: scaleY(1.2); }
        }

        .instruction-box {
            background-color: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(51, 65, 85, 0.5);
            border-radius: 16px;
            padding: 1.25rem 1.5rem;
            font-size: 1rem;
            line-height: 1.6;
            color: #94a3b8;
            text-align: center;
            max-width: 1100px;
            margin: 0 auto;
            width: 100%;
        }

        .tv-footer {
            text-align: center;
            color: #475569;
            font-size: 0.78rem;
            font-weight: 600;
        }

        @media (max-width: 900px) {
            .tv-grid { grid-template-columns: 1fr; }
            .sbd-current { font-size: 4rem; }
            .sbd-next { font-size: 2.5rem; }
        }
    </style>
</head>
<body class="public-call-mode">

    <div class="tv-top-bar">
        <span class="tv-session-badge">
            Hội trường phòng chờ chính
            <c:if test="${not empty currentSession}">
                &mdash; Ca thi
                <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy" />
            </c:if>
        </span>
        <span style="font-size: 0.75rem; color: #475569; font-weight: 700;">Tự động cập nhật 3 giây</span>
    </div>

    <div class="tv-grid">
        <!-- Panel trái: thí sinh đang được gọi -->
        <div class="tv-panel ${requestScope.isCallingActive ? 'tv-panel--current' : 'tv-panel--idle'}">
            <c:if test="${requestScope.isCallingActive}">
                <div class="call-pulsar call-pulsar--live">
                    <span class="pulsar-dot pulsar-dot--red"></span>
                    <span>ĐANG PHÁT LOA</span>
                </div>
            </c:if>

            <div class="panel-label panel-label--current">
                ${requestScope.isCallingActive ? 'Thí sinh đang được gọi' : 'Chưa có lượt gọi'}
            </div>

            <c:choose>
                <c:when test="${requestScope.isCallingActive}">
                    <div class="sbd-current">${callingCandidate.sbd}</div>
                    <div class="name-current">${callingCandidate.name}</div>
                    <div class="class-tag">Hạng ${callingCandidate.clazz}</div>
                    <div class="call-soundwaves">
                        <div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>
                        <div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div>
                        <div class="wave-bar"></div><div class="wave-bar"></div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="sbd-current" style="background: linear-gradient(135deg, #94a3b8, #64748b); -webkit-background-clip: text; font-size: 3rem;">CHỜ GỌI</div>
                    <div class="name-current" style="color: #94a3b8; font-size: 1.5rem;">Vui lòng theo dõi bảng bên phải</div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Panel phải: thí sinh chuẩn bị tiếp theo -->
        <div class="tv-panel tv-panel--next">
            <div class="call-pulsar call-pulsar--ready">
                <span class="pulsar-dot pulsar-dot--green"></span>
                <span>CHUẨN BỊ TIẾP THEO</span>
            </div>

            <div class="panel-label panel-label--next">Thí sinh chuẩn bị vào bàn</div>

            <c:choose>
                <c:when test="${not empty requestScope.nextCandidate}">
                    <div class="sbd-next">${nextCandidate.sbd}</div>
                    <div class="name-next">${nextCandidate.name}</div>
                    <div class="class-tag" style="color: #6ee7b7;">Hạng ${nextCandidate.clazz}</div>
                    <p style="margin-top: 1.25rem; font-size: 0.9rem; color: #64748b; max-width: 320px; line-height: 1.5;">
                        Giữ sẵn CCCD, chuẩn bị di chuyển vào bàn thủ tục ngay sau khi người trước hoàn tất.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="sbd-next" style="color: #64748b; font-size: 2rem;">--</div>
                    <div class="name-next" style="color: #64748b; font-size: 1.1rem;">
                        <c:choose>
                            <c:when test="${requestScope.shiftEnded}">Ca thi đã kết thúc</c:when>
                            <c:otherwise>Không còn thí sinh chờ trong hàng đợi</c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="instruction-box">
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
        Bảng truyền thông sát hạch lái xe Lái Vui &copy; 2026 &mdash; Đồng bộ từ bàn điều hành loa gọi thi
    </div>

</body>
</html>
