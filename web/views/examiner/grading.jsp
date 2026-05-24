<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%--
    ========================================================================
    MÀN HÌNH CHẤM ĐIỂM SÁT HẠCH SỐ THỜI GIAN THỰC (SC-053)
    ========================================================================
    Trang này hoàn toàn nhận dữ liệu động từ backend thông qua JSTL.
    Nếu backend chưa cung cấp dữ liệu, hệ thống hiển thị trạng thái trống (Fallback) an toàn.
--%>
<c:set var="hasCandidate" value="${not empty currentCandidate}" />
<c:set var="hasSteps" value="${not empty examSteps}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chấm điểm sát hạch sa hình - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<%-- Inject the sidebar template --%>
<jsp:include page="/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="cham-diem" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Chấm điểm sát hạch</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Chấm điểm sát hạch</h1>
                <p class="page-subtitle">Bảng điều hành giám sát và trừ điểm thực hành sa hình thời gian thực của Giám thị.</p>
            </div>
            
            <!-- Quick Actions on Header -->
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button id="btnStartExam" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; background-color: #10b981; border-color: #10b981;" onclick="startTimer();" <c:if test="${not hasSteps}">disabled style="opacity: 0.5; cursor: not-allowed;"</c:if>>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                        <path d="M5 3l14 9-14 9V3z" fill="currentColor"/>
                    </svg>
                    Bắt đầu chấm
                </button>
                <button id="btnSubmitScore" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; background-color: #0052cc; border-color: #0052cc;" onclick="submitFinalScore();" disabled>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                        <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Khóa & Nộp điểm
                </button>
                <button class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; background-color: #ef4444; border-color: #ef4444;" onclick="disqualifyCandidate();">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 5px;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Đình chỉ thi
                </button>
            </div>
        </header>

        <!-- Main Workspace Grid -->
        <div class="grading-grid">
            
            <!-- LEFT COLUMN: scoring console with steps -->
            <div class="grading-pane">
                <div class="grading-pane__header" style="margin-bottom: 1.25rem;">
                    <h2 class="grading-pane__title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <path d="M9 11l3 3L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Bảng chấm điểm bài thi sa hình
                    </h2>
                </div>
                
                <c:choose>
                    <c:when test="${hasSteps}">
                        <!-- Bộ điều khiển chuyển bài thi tuần tự (SC-053) -->
                        <div class="step-controller-bar" style="display: flex; justify-content: space-between; align-items: center; background-color: rgba(0, 82, 204, 0.04); border: 1px solid rgba(0, 82, 204, 0.1); padding: 0.75rem 1rem; border-radius: 10px; margin-bottom: 1.5rem;">
                            <button id="btnPrevStep" class="btn-deduct" style="color: #0052cc; border-color: #0052cc; margin: 0; height: 36px;" onclick="prevStep();" disabled>
                                &larr; Bài trước
                            </button>
                            <div style="text-align: center;">
                                <span style="font-size: 0.72rem; font-weight: 700; color: #64748b; text-transform: uppercase; display: block; letter-spacing: 0.05em; margin-bottom: 2px;">Bài thi hiện tại</span>
                                <span id="currentStepNameDisplay" style="font-size: 0.92rem; font-weight: 800; color: #003d9b;">CHƯA BẮT ĐẦU</span>
                            </div>
                            <button id="btnNextStep" class="btn-filter" style="margin: 0; height: 36px; padding: 0 1rem; font-size: 0.85rem; background-color: #0052cc; border-color: #0052cc; opacity: 0.5; cursor: not-allowed;" onclick="nextStep();" disabled>
                                Bài tiếp theo &rarr;
                            </button>
                        </div>
                        
                        <div class="scoring-list">
                            <c:forEach var="item" items="${examSteps}">
                                <!-- Step Card (Bài thi sa hình) -->
                                <div class="step-card" id="stepCard_${item.stepId}" onclick="selectStep(${item.stepId});" style="cursor: pointer;">
                                    <div class="step-info">
                                        <div class="step-header-row">
                                            <span class="step-num">BÀI ${item.stepId}</span>
                                            <span class="step-title">${item.stepName}</span>
                                        </div>
                                        <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">
                                            <span class="step-desc-muted">Lỗi chạm vạch/tắt máy: -${item.maxPoints}đ</span>
                                            <span class="step-points-deducted none" id="penaltyDisplay_${item.stepId}">
                                                -0đ
                                            </span>
                                        </div>
                                    </div>
                                    
                                    <div class="step-actions" onclick="event.stopPropagation();">
                                        <!-- Trừ 5đ nhanh -->
                                        <button class="btn-deduct" onclick="deductPoints(${item.stepId}, 5);" title="Trừ 5 điểm lỗi thông thường (chạm vạch, tắt máy)">
                                            -5đ
                                        </button>
                                        <!-- Trừ 10đ nhanh -->
                                        <button class="btn-deduct" onclick="deductPoints(${item.stepId}, 10);" title="Trừ 10 điểm lỗi nặng (đi sai làn bánh xe, quá giờ bài thi)">
                                            -10đ
                                        </button>
                                        <!-- Lỗi loại trực tiếp -->
                                        <button class="btn-disqualify" onclick="disqualifyStep(${item.stepId});" title="Đánh lỗi loại trực tiếp bài thi (gây tai nạn, đi sai hình)">
                                            LOẠI
                                        </button>
                                        <!-- Hoàn tác điểm trừ bài thi này -->
                                        <button class="btn-deduct" style="color: #64748b; border-color: #cbd5e1;" onclick="resetStep(${item.stepId});" title="Hoàn tác điểm trừ của bài thi này">
                                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                                <path d="M3 3v5h5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </button>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="text-align: center; color: #64748b; padding: 4rem 2rem; font-style: italic;">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #cbd5e1; margin-bottom: 1rem;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            <p style="font-size: 0.95rem; font-weight: 500;">Chưa có danh sách bài thi sa hình.</p>
                            <p style="font-size: 0.8rem; color: #94a3b8; margin-top: 4px;">Vui lòng kết nối cơ sở dữ liệu và truyền danh sách bài thi từ Controller.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            
            <!-- RIGHT COLUMN: candidate live info, score display, camera feed -->
            <div class="grading-sidebar-cards">
                
                <!-- Panel 1: Live Score & Timer Displays -->
                <div class="grading-pane" style="display: flex; flex-direction: column; align-items: center;">
                    <div class="grading-pane__header" style="width: 100%; justify-content: center; margin-bottom: 1rem;">
                        <h2 class="grading-pane__title" style="color: #0052cc;">KẾT QUẢ ĐANG THI</h2>
                    </div>
                    
                    <!-- Live Score displaying green (passed) or red (failed) dynamically -->
                    <div id="scoreBox" class="live-score-display passed" style="width: 100%;">
                        <span id="scoreVal" class="live-score-display__value">100</span>
                        <span id="scoreBadge" class="live-score-display__badge">ĐANG ĐẠT</span>
                        
                        <!-- Live Timer -->
                        <div class="live-timer-container">
                            <span class="live-timer-label">Thời gian thi sa hình:</span>
                            <span id="examTimer" class="live-timer-value">20:00</span>
                        </div>
                    </div>
                </div>
                
                <!-- Panel 2: Current Candidate Profile -->
                <div class="grading-pane">
                    <div class="grading-pane__header" style="margin-bottom: 1.25rem;">
                        <h2 class="grading-pane__title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Thông tin thí sinh thi
                        </h2>
                    </div>
                    
                    <div class="call-grid" style="grid-template-columns: 110px 1fr; gap: 1.25rem;">
                        <div class="candidate-photo-frame" style="width: 110px; height: 140px; border-radius: 6px;">
                            <c:choose>
                                <c:when test="${hasCandidate and not empty currentCandidate.photoUrl}">
                                    <img src="${pageContext.request.contextPath}${currentCandidate.photoUrl}" alt="${currentCandidate.name}">
                                </c:when>
                                <c:otherwise>
                                    <div style="display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background-color: #cbd5e1; color: #64748b;">
                                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="candidate-details-list" style="grid-template-columns: 1fr; gap: 0.75rem;">
                            <div class="candidate-detail-item">
                                <span class="candidate-detail-label">Số báo danh</span>
                                <span class="candidate-detail-value" style="color: #0052cc; font-size: 1.05rem;">
                                    ${hasCandidate ? currentCandidate.sbd : 'N/A'}
                                </span>
                            </div>
                            <div class="candidate-detail-item">
                                <span class="candidate-detail-label">Họ và tên</span>
                                <span class="candidate-detail-value">
                                    ${hasCandidate ? currentCandidate.name : 'Chưa có thí sinh'}
                                </span>
                            </div>
                            <div class="candidate-detail-item">
                                <span class="candidate-detail-label">Hạng xe / Số xe thi</span>
                                <span class="candidate-detail-value" style="font-size: 0.85rem;">
                                    ${hasCandidate ? currentCandidate.licenseClass : 'Chưa rõ'} | ${hasCandidate ? currentCandidate.carNumber : 'Chưa xếp xe'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Panel 3: Live Vehicle Camera / Sa Hình Surveillance -->
                <div class="grading-pane">
                    <div class="grading-pane__header" style="margin-bottom: 1rem;">
                        <h2 class="grading-pane__title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Camera giám sát sa hình (Live)
                        </h2>
                    </div>
                    
                    <div class="camera-feed" style="aspect-ratio: 16 / 10; border-radius: 8px;">
                        <div class="camera-feed__overlay"></div>
                        <div class="camera-feed__reticle" style="width: 140px; height: 140px; border-color: rgba(99, 102, 241, 0.4);"></div>
                        <div class="camera-feed__scan-line" style="background: linear-gradient(to bottom, transparent, #6366f1, transparent); box-shadow: 0 0 8px #6366f1;"></div>
                        <div class="camera-feed__live-tag" style="background: rgba(0, 82, 204, 0.85);">
                            <span style="display:inline-block; width: 6px; height: 6px; border-radius: 50%; background: #ffffff; animation: blinkTag 1.2s infinite;"></span>
                            XE SỐ ${hasCandidate ? currentCandidate.carNumber : '00'} - CAM 01
                        </div>
                        <span style="font-size: 0.8rem; font-weight: 600; color: #94a3b8; z-index: 10; text-transform: uppercase; letter-spacing: 0.05em; font-family: monospace; text-shadow: 0 2px 4px rgba(0,0,0,0.8);">SA HÌNH: ĐANG GIÁM SÁT</span>
                    </div>
                </div>
                
            </div>
        </div>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<!-- ========================================================
     JAVASCRIPT GIẢ LẬP ĐIỀU HÀNH & CHẤM ĐIỂM SÁT HẠCH DỘNG
     ======================================================== -->
<script>
    // Khởi tạo các thông số chấm điểm
    let currentScore = 100;
    let isDisqualified = false;
    let stepPenalties = {}; // Lưu trữ điểm trừ cho từng bài thi sa hình {stepId: penalty}
    
    // Khởi tạo các thông số chuyển bài thi tuần tự (SC-053)
    let currentActiveStep = null;
    let stepIds = []; // Mảng chứa ID các bài thi thực tế trên giao diện
    
    // Khởi tạo thông số đếm ngược thời gian
    let examTimeSeconds = 1200; // 20 phút = 1200 giây
    let timerInterval = null;
    let examStarted = false;

    // Đọc động các ID bài thi thực tế có trên giao diện để khởi tạo mảng
    document.addEventListener("DOMContentLoaded", function() {
        let stepCards = document.querySelectorAll('.step-card');
        stepCards.forEach(card => {
            let id = parseInt(card.id.replace('stepCard_', ''));
            stepIds.push(id);
            stepPenalties[id] = 0;
        });
        
        // Sắp xếp tăng dần
        stepIds.sort((a, b) => a - b);
        if (stepIds.length > 0) {
            currentActiveStep = stepIds[0];
        }
    });

    // Hàm Bắt đầu thi (Chạy đồng hồ đếm ngược)
    function startTimer() {
        if (examStarted || stepIds.length === 0) return;
        examStarted = true;
        
        // Cập nhật nút bấm
        document.getElementById('btnStartExam').disabled = true;
        document.getElementById('btnStartExam').style.opacity = '0.6';
        document.getElementById('btnStartExam').style.cursor = 'not-allowed';
        document.getElementById('btnSubmitScore').disabled = false;
        
        // Kích hoạt các nút bấm chuyển bài trên thanh điều khiển
        document.getElementById('btnNextStep').disabled = false;
        document.getElementById('btnNextStep').style.opacity = '1';
        document.getElementById('btnNextStep').style.cursor = 'pointer';
        
        // Làm nổi bật bài thi đầu tiên
        let firstCard = document.getElementById('stepCard_' + currentActiveStep);
        if (firstCard) firstCard.classList.add('active');
        
        updateStepControllerUI();
        
        timerInterval = setInterval(function() {
            if (examTimeSeconds <= 0) {
                clearInterval(timerInterval);
                document.getElementById('examTimer').innerHTML = "HẾT GIỜ";
                document.getElementById('examTimer').classList.add('warning');
                alert("Hết giờ làm bài thi sa hình! Hệ thống tự động khóa điểm.");
                lockExam();
                return;
            }
            
            examTimeSeconds--;
            
            // Định dạng thời gian MM:SS
            let minutes = Math.floor(examTimeSeconds / 60);
            let seconds = examTimeSeconds % 60;
            let timeString = (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
            
            let timerEl = document.getElementById('examTimer');
            timerEl.innerHTML = timeString;
            
            // Cảnh báo khi thời gian dưới 3 phút (180 giây)
            if (examTimeSeconds <= 180) {
                timerEl.classList.add('warning');
            }
        }, 1000);
    }

    // Cập nhật giao diện thanh chuyển bài thi (Next/Prev)
    function updateStepControllerUI() {
        if (!examStarted || stepIds.length === 0) return;

        // Tìm tên bài thi hiện tại để hiển thị lên màn hình điều khiển
        let stepName = "";
        let cardEl = document.getElementById('stepCard_' + currentActiveStep);
        if (cardEl) {
            let titleEl = cardEl.querySelector('.step-title');
            if (titleEl) {
                stepName = "BÀI " + currentActiveStep + ": " + titleEl.innerText.toUpperCase();
            }
        }
        document.getElementById('currentStepNameDisplay').innerHTML = stepName;

        // Cập nhật trạng thái nút Bài trước
        let btnPrev = document.getElementById('btnPrevStep');
        if (currentActiveStep === stepIds[0]) {
            btnPrev.disabled = true;
            btnPrev.style.opacity = '0.5';
            btnPrev.style.cursor = 'not-allowed';
        } else {
            btnPrev.disabled = false;
            btnPrev.style.opacity = '1';
            btnPrev.style.cursor = 'pointer';
        }

        // Cập nhật trạng thái nút Bài tiếp theo
        let btnNext = document.getElementById('btnNextStep');
        if (currentActiveStep === stepIds[stepIds.length - 1]) {
            btnNext.innerHTML = "Hoàn thành thi";
            btnNext.style.backgroundColor = '#10b981';
            btnNext.style.borderColor = '#10b981';
        } else {
            btnNext.innerHTML = "Bài tiếp theo &rarr;";
            btnNext.style.backgroundColor = '#0052cc';
            btnNext.style.borderColor = '#0052cc';
        }
    }

    // Chuyển sang bài tiếp theo (Next Bài)
    function nextStep() {
        if (!examStarted || isDisqualified || stepIds.length === 0) return;

        let currentIndex = stepIds.indexOf(currentActiveStep);
        if (currentIndex === stepIds.length - 1) {
            // Đang ở bài cuối, bấm Next sẽ kích hoạt nộp điểm thi chính thức
            submitFinalScore();
            return;
        }

        // Đánh dấu bài thi cũ là đã hoàn thành
        let prevCard = document.getElementById('stepCard_' + currentActiveStep);
        if (prevCard) {
            prevCard.classList.remove('active');
            prevCard.classList.add('completed');
        }

        // Tăng ID bài thi active lên
        currentActiveStep = stepIds[currentIndex + 1];

        // Làm nổi bật bài thi tiếp theo
        let nextCard = document.getElementById('stepCard_' + currentActiveStep);
        if (nextCard) {
            nextCard.classList.remove('completed');
            nextCard.classList.add('active');
            // Tự động cuộn màn hình đến bài thi đang active
            nextCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }

        updateStepControllerUI();
    }

    // Quay lại bài thi trước (Prev Bài)
    function prevStep() {
        if (!examStarted || isDisqualified || stepIds.length === 0) return;

        let currentIndex = stepIds.indexOf(currentActiveStep);
        if (currentIndex === 0) return; // Bài đầu tiên

        // Bỏ active bài thi hiện tại
        let currentCard = document.getElementById('stepCard_' + currentActiveStep);
        if (currentCard) {
            currentCard.classList.remove('active');
        }

        // Giảm ID bài thi active xuống
        currentActiveStep = stepIds[currentIndex - 1];

        // Làm nổi bật và khôi phục bài trước
        let prevCard = document.getElementById('stepCard_' + currentActiveStep);
        if (prevCard) {
            prevCard.classList.remove('completed');
            prevCard.classList.add('active');
            // Tự động cuộn màn hình đến bài thi
            prevCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }

        updateStepControllerUI();
    }

    // Chọn bài thi trực tiếp bằng cách click vào card
    function selectStep(stepId) {
        if (!examStarted || isDisqualified || stepIds.length === 0) return;

        // Gỡ class active của bài thi cũ và đánh dấu đã xong (nếu bài đó nhỏ hơn bài mới chọn)
        let oldCard = document.getElementById('stepCard_' + currentActiveStep);
        if (oldCard) {
            oldCard.classList.remove('active');
            if (currentActiveStep < stepId) {
                oldCard.classList.add('completed');
            }
        }

        // Kích hoạt bài thi mới
        currentActiveStep = stepId;
        let newCard = document.getElementById('stepCard_' + currentActiveStep);
        if (newCard) {
            newCard.classList.remove('completed');
            newCard.classList.add('active');
        }

        updateStepControllerUI();
    }

    // Hàm trừ điểm cho một bài thi sa hình cụ thể
    function deductPoints(stepId, points) {
        if (!examStarted) {
            alert("Vui lòng bấm nút 'Bắt đầu chấm' trước khi thực hiện chấm điểm!");
            return;
        }
        if (isDisqualified) return;

        // Tự động kích hoạt bài thi đang chấm
        selectStep(stepId);

        // Cộng dồn điểm bị trừ của bài thi đó
        stepPenalties[stepId] += points;
        
        // Hiển thị điểm trừ trên card bài thi
        let displayEl = document.getElementById('penaltyDisplay_' + stepId);
        if (displayEl) {
            displayEl.innerHTML = "-" + stepPenalties[stepId] + "đ";
            displayEl.classList.remove('none');
        }

        // Tính toán lại tổng điểm và cập nhật giao diện
        recalculateTotalScore();
    }

    // Hàm đánh lỗi LOẠI TRỰC TIẾP cho một bài thi cụ thể
    function disqualifyStep(stepId) {
        if (!examStarted) {
            alert("Vui lòng bấm nút 'Bắt đầu chấm' trước khi thực hiện chấm điểm!");
            return;
        }
        if (isDisqualified) return;

        selectStep(stepId);

        let confirmDisq = confirm("Bạn có chắc chắn muốn đánh lỗi LOẠI TRỰC TIẾP cho Bài thi " + stepId + "? Thí sinh sẽ bị trượt ngay lập tức.");
        if (!confirmDisq) return;

        isDisqualified = true;
        
        // Cập nhật giao diện bài thi bị loại
        let displayEl = document.getElementById('penaltyDisplay_' + stepId);
        if (displayEl) {
            displayEl.innerHTML = "BỊ LOẠI";
            displayEl.classList.remove('none');
            displayEl.style.backgroundColor = 'rgba(239, 68, 68, 0.15)';
            displayEl.style.color = '#ef4444';
        }

        // Cập nhật Live Scoreboard
        updateScoreboardDisplay(0, "TRƯỢT (LOẠI)");
    }

    // Hàm Hoàn tác (Reset) điểm trừ của một bài thi cụ thể về 0
    function resetStep(stepId) {
        if (!examStarted) return;
        
        selectStep(stepId);

        // Reset điểm trừ bài thi này về 0
        stepPenalties[stepId] = 0;
        
        // Khôi phục hiển thị badge điểm trừ
        let displayEl = document.getElementById('penaltyDisplay_' + stepId);
        if (displayEl) {
            displayEl.innerHTML = "-0đ";
            displayEl.classList.add('none');
            displayEl.style.backgroundColor = '';
            displayEl.style.color = '';
        }

        // Nếu thí sinh đang bị loại bởi bài thi này, cần khôi phục lại trạng thái bình thường
        if (isDisqualified) {
            isDisqualified = false;
        }

        recalculateTotalScore();
    }

    // Hàm làm sạch class active trên toàn bộ các bài thi
    function clearAllActiveSteps() {
        let stepCards = document.querySelectorAll('.step-card');
        stepCards.forEach(card => {
            card.classList.remove('active');
        });
    }

    // Hàm tính toán lại tổng điểm dựa trên mảng stepPenalties
    function recalculateTotalScore() {
        if (isDisqualified) return;

        let totalDeductions = 0;
        stepIds.forEach(id => {
            totalDeductions += stepPenalties[id];
        });

        currentScore = 100 - totalDeductions;
        if (currentScore < 0) currentScore = 0;

        // Cập nhật Live Scoreboard
        let badgeLabel = currentScore >= 80 ? "ĐANG ĐẠT" : "CHƯA ĐẠT";
        updateScoreboardDisplay(currentScore, badgeLabel);
    }

    // Hàm cập nhật hiển thị bảng điểm trực tiếp (Live Scoreboard)
    function updateScoreboardDisplay(score, badgeText) {
        let scoreValEl = document.getElementById('scoreVal');
        let scoreBadgeEl = document.getElementById('scoreBadge');
        let scoreBoxEl = document.getElementById('scoreBox');

        scoreValEl.innerHTML = score;
        scoreBadgeEl.innerHTML = badgeText;

        if (score >= 80 && !isDisqualified) {
            scoreBoxEl.classList.remove('failed');
            scoreBoxEl.classList.add('passed');
        } else {
            scoreBoxEl.classList.remove('passed');
            scoreBoxEl.classList.add('failed');
        }
    }

    // Hàm Đình chỉ thi nhanh từ Header (Hủy kết quả trực tiếp)
    function disqualifyCandidate() {
        if (!examStarted) {
            alert("Ca thi chưa bắt đầu!");
            return;
        }
        let confirmAction = confirm("ĐÌNH CHỈ THI THÍ SINH: Bạn có chắc chắn muốn hủy kết quả và đình chỉ thi thí sinh này ngay lập tức?");
        if (!confirmAction) return;

        isDisqualified = true;
        clearInterval(timerInterval);
        
        // Chuyển toàn bộ các bài thi về trạng thái hoàn thành/khóa
        stepIds.forEach(id => {
            let displayEl = document.getElementById('penaltyDisplay_' + id);
            if (displayEl) {
                displayEl.innerHTML = "ĐÌNH CHỈ";
                displayEl.classList.remove('none');
            }
        });

        updateScoreboardDisplay(0, "TRƯỢT (ĐÌNH CHỈ)");
        alert("Đã đình chỉ thi thí sinh thành công. Kết quả thi đã được ghi nhận: TRƯỢT.");
        lockExam();
    }

    // Khóa mọi tính năng sau khi thi xong hoặc hết giờ
    function lockExam() {
        clearInterval(timerInterval);
        document.getElementById('btnSubmitScore').disabled = true;
        document.getElementById('btnSubmitScore').style.opacity = '0.6';
        
        // Khóa các nút chuyển bài
        document.getElementById('btnPrevStep').disabled = true;
        document.getElementById('btnNextStep').disabled = true;
        document.getElementById('btnPrevStep').style.opacity = '0.5';
        document.getElementById('btnNextStep').style.opacity = '0.5';
        document.getElementById('btnPrevStep').style.cursor = 'not-allowed';
        document.getElementById('btnNextStep').style.cursor = 'not-allowed';
        
        // Khóa tất cả nút bấm chấm điểm
        let buttons = document.querySelectorAll('.step-actions button');
        buttons.forEach(btn => {
            btn.disabled = true;
            btn.style.opacity = '0.5';
            btn.style.cursor = 'not-allowed';
        });
        
        clearAllActiveSteps();
    }

    // Khóa và nộp điểm chính thức
    function submitFinalScore() {
        if (!examStarted) return;
        
        let resultMsg = currentScore >= 80 && !isDisqualified ? "ĐẠT (ĐỖ)" : "CHƯA ĐẠT (TRƯỢT)";
        let confirmSubmit = confirm("XÁC NHẬN NỘP BÀI THI SA HÌNH:\n\n- Điểm số chính thức: " + (isDisqualified ? 0 : currentScore) + " điểm\n- Kết quả sát hạch: " + resultMsg + "\n\nBạn có chắc chắn muốn khóa và gửi kết quả này lên hệ thống? (Thao tác này không thể hoàn tác)");
        
        if (!confirmSubmit) return;
        
        lockExam();
        alert("Đã nộp điểm thi thành công! Kết quả đã được cập nhật vào hồ sơ thí sinh và đồng bộ lên Sở GTVT.");
    }
</script>

</body>
</html>
