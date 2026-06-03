<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Get live candidates state from session
    java.util.List<java.util.Map<String, String>> q = (java.util.List<java.util.Map<String, String>>) session.getAttribute("candidateQueue");
    List<Map<String, Object>> rows = new ArrayList<>();
    
    int total = 120;
    int present = 0;
    int paid = 0;
    int absent = 0;
    
    if (q != null) {
        for (java.util.Map<String, String> c : q) {
            Map<String, Object> r = new HashMap<>();
            r.put("sbd", c.get("sbd"));
            r.put("name", c.get("name"));
            r.put("class", c.get("class"));
            
            // Simulating theory and practical scores dynamically based on captures and session state
            if ("A1-0024".equals(c.get("sbd"))) {
                r.put("theory", "25/25");
                r.put("practical", "--");
                present++;
            } else if ("B2-0145".equals(c.get("sbd"))) {
                r.put("theory", "34/35");
                r.put("practical", (c.get("photoUrl") != null && !c.get("photoUrl").trim().isEmpty()) ? "95/100" : "--");
                if (c.get("photoUrl") != null && !c.get("photoUrl").trim().isEmpty()) {
                    present++;
                }
            } else if ("B2-0112".equals(c.get("sbd"))) {
                r.put("theory", "35/35");
                r.put("practical", "32/100");
                present++;
                absent++;
            } else {
                r.put("theory", "--");
                r.put("practical", "--");
            }
            
            if ("true".equals(c.get("paymentCompleted"))) {
                paid++;
            }
            
            rows.add(r);
        }
    }
    
    request.setAttribute("scoreboardList", rows);
    
    Map<String, Integer> stats = new HashMap<>();
    stats.put("total", total);
    stats.put("present", 8 + present);
    stats.put("paid", 48 + paid);
    stats.put("absent", 12 + absent);
    request.setAttribute("stats", stats);
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <!-- SC-080: 100% JS-Free Automatic refresh every 5 seconds -->
    <meta http-equiv="refresh" content="5">
    
    <title>BẢNG ĐIỂM LIVE - PHÒNG CHỜ CHÍNH</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        body.live-monitor-mode {
            background: radial-gradient(circle at top left, #0f172a, #020617);
            color: #f8fafc;
            min-height: 100vh;
            padding: 2rem;
            box-sizing: border-box;
        }
        
        .live-monitor-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid rgba(51, 65, 85, 0.5);
            padding-bottom: 1.25rem;
            margin-bottom: 2rem;
        }
        
        .live-brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .live-brand-title {
            font-size: 1.5rem;
            font-weight: 800;
            letter-spacing: 0.05em;
            background: linear-gradient(135deg, #3b82f6, #10b981);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        
        .live-timer-badge {
            background-color: rgba(30, 41, 59, 0.8);
            border: 1px solid #334155;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 8px;
            color: #94a3b8;
        }
        .live-indicator-dot {
            width: 8px;
            height: 8px;
            background-color: #10b981;
            border-radius: 50%;
            animation: pulse-green 1.5s infinite;
        }
        @keyframes pulse-green {
            0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
            70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
            100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }
        
        .live-grid-table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0 10px;
            margin-top: 1rem;
        }
        
        .live-grid-table th {
            text-transform: uppercase;
            font-size: 0.78rem;
            font-weight: 800;
            color: #64748b;
            letter-spacing: 0.08em;
            padding: 10px 16px;
            border: none;
        }
        
        .live-grid-row {
            background-color: rgba(30, 41, 59, 0.4);
            border: 1px solid rgba(51, 65, 85, 0.4);
            backdrop-filter: blur(8px);
            transition: all 0.2s ease;
        }
        .live-grid-row:hover {
            background-color: rgba(30, 41, 59, 0.6);
            transform: scale(1.002);
        }
        
        .live-grid-cell {
            padding: 1rem 1.25rem;
            font-size: 0.95rem;
            border-top: 1px solid rgba(51, 65, 85, 0.3);
            border-bottom: 1px solid rgba(51, 65, 85, 0.3);
            vertical-align: middle;
        }
        .live-grid-cell:first-child {
            border-left: 1px solid rgba(51, 65, 85, 0.3);
            border-top-left-radius: 12px;
            border-bottom-left-radius: 12px;
        }
        .live-grid-cell:last-child {
            border-right: 1px solid rgba(51, 65, 85, 0.3);
            border-top-right-radius: 12px;
            border-bottom-right-radius: 12px;
        }
        
        .live-score-number {
            font-family: monospace;
            font-weight: 800;
            font-size: 1.15rem;
        }
        
        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 0.75rem;
            font-weight: 800;
            padding: 4px 10px;
            border-radius: 6px;
            text-transform: uppercase;
        }
        .status-badge--testing { background-color: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); }
        .status-badge--passed { background-color: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }
        .status-badge--failed { background-color: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }
        .status-badge--waiting { background-color: rgba(148, 163, 184, 0.1); color: #94a3b8; border: 1px solid rgba(148, 163, 184, 0.2); }
        
        .header-nav-btn {
            background-color: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: #94a3b8;
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: all 0.2s;
        }
        .header-nav-btn:hover {
            background-color: rgba(255, 255, 255, 0.1);
            color: #ffffff;
        }
    </style>
</head>
<body class="live-monitor-mode">

    <!-- Header info board -->
    <header class="live-monitor-header">
        <div class="live-brand">
            <div style="background: linear-gradient(135deg, #0052cc, #003d9b); width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 10px rgba(0,82,204,0.3);">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ffffff;">
                    <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M9 17v-5M15 17v-3M12 17v-8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
            </div>
            <div>
                <h1 class="live-brand-title" style="margin: 0; font-size: 1.25rem;">HỆ THỐNG SÁT HẠCH LÁI XE</h1>
                <span style="font-size: 0.72rem; color: #64748b; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;">Bảng Điểm Live Phòng Chờ Chính</span>
            </div>
        </div>
        
        <div style="display: flex; gap: 10px; align-items: center;">
            <a href="${pageContext.request.contextPath}/views/staff/examstaff/dashboard.jsp" class="header-nav-btn">
                &larr; Vào dashboard
            </a>
            
            <div class="live-timer-badge">
                <span class="live-indicator-dot"></span>
                <span>TỰ ĐỘNG CẬP NHẬT (5S)</span>
            </div>
        </div>
    </header>

    <!-- Scoreboard Metrics Row -->
    <section class="metrics-row" style="margin-bottom: 2rem; display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; content-visibility: auto;">
        <div class="stat-card" style="background-color: rgba(30, 41, 59, 0.3); border-color: rgba(51, 65, 85, 0.4); color: #ffffff;">
            <div class="stat-info">
                <span class="stat-number" style="color: #ffffff; font-size: 1.75rem;">${not empty stats ? stats.total : 120}</span>
                <span class="stat-label" style="color: #94a3b8;">Tổng thí sinh ca thi</span>
            </div>
        </div>
        <div class="stat-card" style="background-color: rgba(30, 41, 59, 0.3); border-color: rgba(51, 65, 85, 0.4); color: #ffffff;">
            <div class="stat-info">
                <span class="stat-number" style="color: #60a5fa; font-size: 1.75rem;">${not empty stats ? stats.present : 8}</span>
                <span class="stat-label" style="color: #94a3b8;">Đã trình diện thi</span>
            </div>
        </div>
        <div class="stat-card" style="background-color: rgba(30, 41, 59, 0.3); border-color: rgba(51, 65, 85, 0.4); color: #ffffff;">
            <div class="stat-info">
                <span class="stat-number" style="color: #34d399; font-size: 1.75rem;">${not empty stats ? stats.paid : 48}</span>
                <span class="stat-label" style="color: #94a3b8;">Đã đóng lệ phí</span>
            </div>
        </div>
        <div class="stat-card" style="background-color: rgba(30, 41, 59, 0.3); border-color: rgba(51, 65, 85, 0.4); color: #ffffff;">
            <div class="stat-info">
                <span class="stat-number" style="color: #f87171; font-size: 1.75rem;">${not empty stats ? stats.absent : 12}</span>
                <span class="stat-label" style="color: #94a3b8;">Học viên vắng mặt</span>
            </div>
        </div>
    </section>

    <!-- Scoreboard Table -->
    <div style="background-color: rgba(15, 23, 42, 0.6); border: 1px solid rgba(51, 65, 85, 0.5); border-radius: 16px; padding: 1.5rem; box-shadow: 0 10px 30px rgba(0,0,0,0.2);">
        
        <table class="live-grid-table">
            <thead>
                <tr>
                    <th scope="col" style="text-align: left; width: 120px;">SBD</th>
                    <th scope="col" style="text-align: left;">Họ và Tên</th>
                    <th scope="col" style="width: 80px; text-align: center;">Hạng</th>
                    <th scope="col" style="text-align: center; width: 130px;">Điểm Lý Thuyết</th>
                    <th scope="col" style="text-align: center; width: 130px;">Điểm Mô Phỏng</th>
                    <th scope="col" style="text-align: center; width: 130px;">Điểm Thực Hành</th>
                    <th scope="col" style="text-align: center; width: 160px;">Trạng Thái Thi</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="row" items="${scoreboardList}">
                    <tr class="live-grid-row">
                        <td class="live-grid-cell" style="font-weight: 800; color: #60a5fa; font-family: monospace;">${row.sbd}</td>
                        <td class="live-grid-cell" style="font-weight: 700;">${row.name}</td>
                        <td class="live-grid-cell" style="text-align: center;"><span class="role-badge role-badge--coi" style="font-size: 0.7rem; padding: 2px 6px;">${row['class']}</span></td>
                        <td class="live-grid-cell" style="text-align: center;"><span class="live-score-number" style="color: #34d399;">${row.theory}</span></td>
                        <td class="live-grid-cell" style="text-align: center;"><span class="live-score-number" style="color: #94a3b8;">--</span></td>
                        <td class="live-grid-cell" style="text-align: center;"><span class="live-score-number" style="color: #34d399;">${row.practical}</span></td>
                        <td class="live-grid-cell" style="text-align: center;">
                            <span class="status-badge ${row.practical eq '--' ? 'status-badge--testing' : (row.practical < 80 ? 'status-badge--failed' : 'status-badge--passed')}">
                                ${row.practical eq '--' ? 'Đang thi thực hành' : (row.practical < 80 ? 'TRƯỢT THỰC HÀNH' : 'ĐẠT SÁT HẠCH')}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        
    </div>

</body>
</html>
