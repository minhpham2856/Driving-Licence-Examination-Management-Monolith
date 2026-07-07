<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Điều hành gọi thí sinh - Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <jsp:include page="/views/staff/exam/components/staff-exam-styles.jsp" />
    </head>
    <body class="has-side-nav-bar">

        <jsp:include page="/views/staff/exam/components/sidebar.jsp">
            <jsp:param name="activeSidebar" value="ds-thi-sinh" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content">

                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current">Quản lý thi</span>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <a href="${pageContext.request.contextPath}/views/staff/exam/candidatelist.jsp">Danh sách thí sinh</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Điều hành gọi thi</span>
                </nav>

                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Điều hành gọi thí sinh</h1>
                        <p class="page-subtitle">Quản lý gọi thi, giám sát nhận diện FaceID trực tiếp, bắt đầu bài thi sát hạch cho từng thí sinh.</p>
                    </div>

                    <div class="page-actions" style="display: flex; gap: 10px; align-items: center; background: #ffffff; padding: 6px 12px; border-radius: 8px; border: 1px solid #e2e8f0;">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <label for="selectSession" style="font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase;">Ca thi:</label>
                            <select id="selectSession" class="input-field" style="height: 32px; padding: 2px 8px; font-size: 0.8rem; width: 140px; border-radius: 6px;">
                                <option value="ca01">Ca Sáng 24/05</option>
                                <option value="ca02">Ca Chiều 24/05</option>
                            </select>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px; border-left: 1px solid #cbd5e1; padding-left: 10px;">
                            <label for="selectRoom" style="font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase;">Phòng thi:</label>
                            <select id="selectRoom" class="input-field" style="height: 32px; padding: 2px 8px; font-size: 0.8rem; width: 160px; border-radius: 6px;">
                                <option value="rm01">Phòng Lý thuyết 01</option>
                                <option value="rm02">Phòng Thực hành A</option>
                                <option value="rm03">Sân Sa hình hạng B</option>
                            </select>
                        </div>
                    </div>
                </header>

                <div class="call-container">
                    <div class="call-pane">
                        <header class="call-pane__header">
                            <h2 class="call-pane__title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                                <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Thí sinh đang được gọi
                            </h2>
                            <c:if test="${not empty callingCandidate or not empty param.sbd}">
                                <span class="action-badge action-badge--warning">Chờ kiểm tra FaceID</span>
                            </c:if>
                        </header>

                        <c:choose>
                            <c:when test="${not empty callingCandidate or not empty param.sbd}">
                                <c:set var="cSbd" value="${empty callingCandidate ? param.sbd : callingCandidate.sbd}" />
                                <c:set var="cName" value="${empty callingCandidate ? (cSbd eq 'A1-0024' ? 'Nguyễn Anh Tuấn' : (cSbd eq 'B2-0145' ? 'Trần Thị Mai' : 'Thí sinh')) : callingCandidate.fullName}" />
                                <c:set var="cCCCD" value="${empty callingCandidate ? (cSbd eq 'A1-0024' ? '001204008912' : (cSbd eq 'B2-0145' ? '038201004567' : 'CCCD/Hộ chiếu')) : callingCandidate.cccd}" />
                                <c:set var="cClass" value="${empty callingCandidate ? (cSbd eq 'A1-0024' ? 'Hạng A1' : (cSbd eq 'B2-0145' ? 'Hạng B2' : 'Chưa rõ')) : callingCandidate.licenseClass}" />
                                <c:set var="cSession" value="${empty callingCandidate ? (cSbd eq 'A1-0024' ? 'Ca sáng' : (cSbd eq 'B2-0145' ? 'Ca chiều' : 'Chưa xếp khóa')) : callingCandidate.shiftLabel}" />
                                <c:set var="cAvatar" value="${empty callingCandidate ? fn:substring(cName, 0, 1) : fn:substring(callingCandidate.fullName, 0, 1)}" />

                                <div class="call-grid">
                                    <div class="candidate-photo-frame">
                                        <div class="candidate-photo-placeholder" style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #0052cc 0%, #003d9b 100%); color: #ffffff; font-size: 3rem; font-weight: 800;">
                                            ${cAvatar}
                                        </div>
                                        <div style="position: absolute; bottom: 0; left: 0; right: 0; background: rgba(0, 82, 204, 0.85); color: #ffffff; font-size: 0.65rem; text-align: center; padding: 3px 0; font-weight: bold; text-transform: uppercase;">Ảnh hồ sơ</div>
                                    </div>

                                    <div class="candidate-details-list">
                                        <div class="candidate-detail-item">
                                            <span class="candidate-detail-label">Số báo danh (SBD)</span>
                                            <span class="candidate-detail-value" style="color: #0052cc; font-size: 1.25rem; font-weight: 800;">${cSbd}</span>
                                        </div>
                                        <div class="candidate-detail-item">
                                            <span class="candidate-detail-label">Họ và tên</span>
                                            <span class="candidate-detail-value">${cName}</span>
                                        </div>
                                        <div class="candidate-detail-item">
                                            <span class="candidate-detail-label">Số CCCD / Hộ chiếu</span>
                                            <span class="candidate-detail-value" style="font-family: monospace;">${cCCCD}</span>
                                        </div>
                                        <div class="candidate-detail-item">
                                            <span class="candidate-detail-label">Hạng sát hạch</span>
                                            <span class="candidate-detail-value">
                                                <span class="role-badge role-badge--coi" style="font-size: 0.8rem; padding: 0.15rem 0.6rem;">${cClass}</span>
                                            </span>
                                        </div>
                                        <div class="candidate-detail-item" style="grid-column: span 2;">
                                            <span class="candidate-detail-label">Khóa sát hạch</span>
                                            <span class="candidate-detail-value" style="color: #475569; font-weight: 500;">${cSession}</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="calling-status-wrapper">
                                    <div class="calling-status-info">
                                        <div class="calling-indicator"></div>
                                        <span style="font-size: 0.9rem; font-weight: 700; color: #0052cc;">Hệ thống phát loa: "Mời thí sinh ${cName} vào phòng thi!"</span>
                                    </div>
                                    <span style="font-size: 0.8rem; color: #64748b; font-weight: 600;">Đã phát 1 lần</span>
                                </div>

                                <div class="control-btn-grid">
                                    <a href="candidatecall.jsp?sbd=${cSbd eq 'A1-0024' ? 'B2-0145' : 'A1-0024'}" class="btn-call-control btn-call-control--next" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; font-size: 0.88rem;">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                        Gọi tiếp theo
                                    </a>

                                    <a href="candidatecall.jsp" class="btn-call-control btn-call-control--absent" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; font-size: 0.88rem;">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Vắng mặt
                                    </a>

                                    <a href="candidatecall.jsp" class="btn-call-control btn-call-control--start" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; font-size: 0.88rem;">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 3l14 9-14 9V3z" fill="currentColor"/>
                                        </svg>
                                        Bắt đầu thi
                                    </a>

                                    <a href="candidatecall.jsp" class="btn-call-control" style="background-color: #f97316; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; font-size: 0.88rem; color: #ffffff;">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18.36 6.64L5.64 19.36M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                        Hủy kết quả
                                    </a>

                                    <a href="${pageContext.request.contextPath}/views/staff/exam/candidatelist.jsp" class="btn-call-control btn-call-control--cancel" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; font-size: 0.88rem;">
                                        Quay lại
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="text-align: center; padding: 4rem 1.5rem; color: #64748b;">
                                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1rem; display: block; opacity: 0.35; color: #64748b;">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    <span style="font-weight: 700; font-size: 1rem; color: #334155; display: block; margin-bottom: 0.5rem;">Đang chờ gọi thí sinh sát hạch</span>
                                    Chưa có thí sinh nào được chọn gọi vào phòng thi. 
                                    <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; max-width: 360px; margin: 0.5rem auto 1.5rem;">Vui lòng bấm nút dưới đây để gọi thí sinh tiếp theo, hoặc chọn trực tiếp từ danh sách thí sinh.</p>

                                    <div style="display: flex; gap: 10px; justify-content: center;">
                                        <a href="candidatecall.jsp?sbd=A1-0024" class="btn-filter" style="height: 42px; padding: 0 1.5rem; text-decoration: none; display: inline-flex; align-items: center; justify-content: center;">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                                            <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                            Gọi thí sinh tiếp theo
                                        </a>
                                        <a href="${pageContext.request.contextPath}/views/staff/exam/candidatelist.jsp" class="btn-reset" style="height: 42px; padding: 0 1.25rem; display: inline-flex; align-items: center; justify-content: center; text-decoration: none;">
                                            Danh sách thí sinh
                                        </a>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="call-pane" style="display: flex; flex-direction: column; justify-content: space-between;">
                        <header class="call-pane__header" style="margin-bottom: 1rem;">
                            <h2 class="call-pane__title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                                </svg>
                                Màn hình nhận diện FaceID
                            </h2>
                            <c:if test="${not empty callingCandidate or not empty param.sbd}">
                                <span class="action-badge" style="background-color: rgba(16, 185, 129, 0.1); color: #10b981; border-color: rgba(16, 185, 129, 0.2);">FaceID Online</span>
                            </c:if>
                        </header>

                        <div class="camera-feed">
                            <c:choose>
                                <c:when test="${not empty callingCandidate or not empty param.sbd}">
                                    <div class="camera-feed__live-tag">
                                        <span style="width: 6px; height: 6px; border-radius: 50%; background-color: #ffffff; display: inline-block;"></span>
                                        REC LIVE
                                    </div>
                                    <div class="camera-feed__reticle"></div>
                                    <div class="camera-feed__scan-line"></div>
                                    <div class="camera-feed__overlay"></div>
                                    <span style="color: rgba(16, 185, 129, 0.85); font-family: monospace; font-size: 1.25rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.1em; z-index: 1;">
                                        SCANNING FACE...
                                    </span>
                                    <div class="face-match-status">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="3"/>
                                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="3"/>
                                        </svg>
                                        ${empty callingCandidate.faceMatchRate ? '99.8%' : callingCandidate.faceMatchRate} KHỚP HỒ SƠ
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; color: #475569; padding: 2rem;">
                                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 0.75rem; display: block; opacity: 0.35;">
                                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                                        </svg>
                                        <span style="font-size: 0.85rem; font-weight: 600; display: block;">Camera FaceID offline</span>
                                        Chờ gọi thí sinh để bắt đầu nhận diện
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div style="background-color: #f8fafc; border-radius: 8px; padding: 0.85rem 1rem; border: 1px dashed #cbd5e1; margin-top: 1rem;">
                            <span style="font-size: 0.75rem; font-weight: 800; color: #1e293b; text-transform: uppercase; display: block; margin-bottom: 4px; letter-spacing: 0.02em;">Quy chế sát hạch FaceID</span>
                            <p style="font-size: 0.75rem; color: #64748b; line-height: 1.45; margin: 0;">Mỗi thí sinh phải được máy quét camera tại phòng thi nhận diện thành công trước khi giám thị phát lệnh bắt đầu bài thi. Log FaceID sẽ được đối chứng khi in biên bản kết quả thi sát hạch.</p>
                        </div>
                    </div>
                </div>

            </main>

        </div>

    </body>
</html>
