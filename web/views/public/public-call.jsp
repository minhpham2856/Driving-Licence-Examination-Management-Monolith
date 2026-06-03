<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MÀN HÌNH GỌI THÍ SINH - PHÒNG CHỜ CHÍNH</title>
    
    <!-- SC-081: 100% JS-Free auto-refresh every 3s to sync with caller console -->
    <meta http-equiv="refresh" content="3">
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        body.public-call-mode {
            background: radial-gradient(circle at center, #0f172a, #020617);
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 3rem 2rem;
            box-sizing: border-box;
        }
        
        .main-call-display {
            background: rgba(30, 41, 59, 0.4);
            border: 2px solid rgba(59, 130, 246, 0.3);
            border-radius: 24px;
            backdrop-filter: blur(16px);
            padding: 4rem 2rem;
            text-align: center;
            max-width: 900px;
            width: 100%;
            margin: 0 auto;
            box-shadow: 0 20px 50px -10px rgba(0, 0, 0, 0.5), inset 0 1px 1px rgba(255, 255, 255, 0.1);
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }
        
        .call-pulsar {
            position: absolute;
            top: 20px;
            right: 20px;
            background-color: rgba(239, 68, 68, 0.15);
            color: #ef4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 800;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        
        .pulsar-dot {
            width: 6px;
            height: 6px;
            background-color: #ef4444;
            border-radius: 50%;
            animation: pulse-red 1.2s infinite;
        }
        @keyframes pulse-red {
            0% { transform: scale(0.8); opacity: 0.5; }
            50% { transform: scale(1.3); opacity: 1; }
            100% { transform: scale(0.8); opacity: 0.5; }
        }
        
        .called-sbd-giant {
            font-size: 6rem;
            font-weight: 900;
            font-family: monospace;
            letter-spacing: 0.05em;
            background: linear-gradient(135deg, #60a5fa, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin: 1.5rem 0;
            text-shadow: 0 0 40px rgba(59, 130, 246, 0.1);
        }
        
        .called-name-giant {
            font-size: 2.5rem;
            font-weight: 800;
            color: #ffffff;
            margin: 0.5rem 0;
        }
        
        .call-instruction-box {
            background-color: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(51, 65, 85, 0.5);
            border-radius: 16px;
            padding: 1.5rem;
            max-width: 600px;
            width: 100%;
            margin: 2.5rem auto 0;
            font-size: 1.15rem;
            line-height: 1.6;
            color: #94a3b8;
        }
        
        /* Soundwave animations */
        .call-soundwaves {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            height: 60px;
            margin-top: 2rem;
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
    </style>
</head>
<body class="public-call-mode">

    <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
        <a href="${pageContext.request.contextPath}/views/staff/examstaff/candidatecall" class="header-nav-btn" style="background-color: rgba(255,255,255,0.03); border-color: rgba(255,255,255,0.08); color: #94a3b8; text-decoration: none; padding: 8px 16px; border-radius: 8px; font-size: 0.8rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
            &larr; Vào điều hành loa
        </a>
        
        <span style="font-size: 0.85rem; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 0.1em;">Hội trường phòng chờ chính</span>
    </div>

    <!-- JSTL dynamic check of called candidate -->
    <c:choose>
        <c:when test="${not empty sessionScope.callingSbd}">
            <c:forEach var="candidate" items="${sessionScope.candidateQueue}">
                <c:if test="${candidate.sbd eq sessionScope.callingSbd}">
                    <c:set var="cCallSbd" value="${candidate.sbd}" />
                    <c:set var="cCallName" value="${candidate.name}" />
                    <c:set var="cCallClass" value="Hạng ${candidate['class']}" />
                </c:if>
            </c:forEach>
            <c:set var="isCallingActive" value="true" />
        </c:when>
        <c:otherwise>
            <c:set var="cCallSbd" value="CHỜ GỌI THI" />
            <c:set var="cCallName" value="Xin Vui Lòng Đợi..." />
            <c:set var="cCallClass" value="--" />
            <c:set var="isCallingActive" value="false" />
        </c:otherwise>
    </c:choose>

    <!-- Main display screen -->
    <div class="main-call-display" style="${isCallingActive ? 'border-color: rgba(59, 130, 246, 0.4);' : 'border-color: rgba(148, 163, 184, 0.2);'}">
        <div class="call-pulsar" style="${isCallingActive ? '' : 'background-color: rgba(16, 185, 129, 0.15); color: #10b981; border-color: rgba(16, 185, 129, 0.3);'}">
            <span class="pulsar-dot" style="${isCallingActive ? '' : 'background-color: #10b981; animation-name: pulse-green;'}"></span>
            <span>${isCallingActive ? 'PHÁT LOA TTS HỘI TRƯỜNG' : 'HỆ THỐNG ĐANG SẴN SÀNG'}</span>
        </div>
        
        <div style="background-color: ${isCallingActive ? 'rgba(59, 130, 246, 0.1)' : 'rgba(148, 163, 184, 0.05)'}; border-radius: 50%; width: 80px; height: 80px; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: ${isCallingActive ? '#60a5fa' : '#94a3b8'};">
                <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
        </div>
        
        <div style="margin-top: 1.5rem; font-size: 1.25rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.1em;">
            ${isCallingActive ? 'MỜI THÍ SINH CÓ SỐ BÁO DANH:' : 'DANH SÁCH GỌI THI TIẾP THEO'}
        </div>
        
        <div class="called-sbd-giant" style="${isCallingActive ? '' : 'background: linear-gradient(135deg, #94a3b8, #64748b); -webkit-background-clip: text;'}">${cCallSbd}</div>
        
        <div class="called-name-giant" style="${isCallingActive ? '' : 'color: #94a3b8;'}">${cCallName}</div>
        
        <div style="font-size: 1.15rem; font-weight: 700; color: #34d399; margin-top: 0.5rem; text-transform: uppercase;">
            SÁT HẠCH: ${cCallClass}
        </div>
        
        <!-- Animated Voice Soundwave simulation -->
        <div class="call-soundwaves">
            <c:choose>
                <c:when test="${isCallingActive}">
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                    <div class="wave-bar"></div>
                </c:when>
                <c:otherwise>
                    <!-- Small static dots when idle -->
                    <div style="width: 6px; height: 6px; background-color: #475569; border-radius: 50%;"></div>
                    <div style="width: 6px; height: 6px; background-color: #475569; border-radius: 50%; margin: 0 4px;"></div>
                    <div style="width: 6px; height: 6px; background-color: #475569; border-radius: 50%;"></div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="call-instruction-box">
            <c:choose>
                <c:when test="${isCallingActive}">
                    Vui lòng cầm sẵn **thẻ căn cước công dân (CCCD)**, xếp hàng có trật tự và tiến thẳng về **Bàn thủ tục chính** để cán bộ làm hồ sơ đối chiếu và chụp ảnh FaceID.
                </c:when>
                <c:otherwise>
                    Thí sinh vui lòng chuẩn bị sẵn **thẻ căn cước công dân (CCCD)**, tập trung trật tự tại phòng chờ chính và theo dõi bảng điểm live hoặc bảng thông báo gọi thi.
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Footer info -->
    <div style="text-align: center; color: #475569; font-size: 0.8rem; font-weight: 600; width: 100%;">
        Bảng truyền thông sát hạch lái xe Lái Vui &copy; 2026. Tất cả các ca thi được giám sát tự động.
    </div>

</body>
</html>
