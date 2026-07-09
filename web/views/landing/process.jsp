<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Quy trình thi" />
    <jsp:param name="activeNav" value="quy-trinh" />
</jsp:include>

<!-- Link custom stylesheet for this page -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/process.css">

<main class="public-main process-page">
    <!-- Hero Section -->
    <section class="process-hero">
        <div class="hero-glow hero-glow--left" aria-hidden="true"></div>
        <div class="hero-glow hero-glow--right" aria-hidden="true"></div>
        <div class="process-container hero-content">
            <h1 class="hero-title">Lộ trình chinh phục GPLX</h1>
            <p class="hero-subtitle">Hệ thống đào tạo hiện đại giúp bạn nắm vững kiến thức và kỹ năng lái xe an toàn chỉ trong 5 bước tinh gọn.</p>
        </div>
    </section>

    <!-- Timeline Section -->
    <section class="process-section timeline-section">
        <div class="process-container">
            <div class="timeline-wrapper">
                <!-- Line connecting steps on desktop -->
                <div class="timeline-connector" aria-hidden="true"></div>
                
                <div class="timeline-steps">
                    <!-- Step 1 -->
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <img src="${pageContext.request.contextPath}/assets/imgs/step1_icon.png" alt="Nộp hồ sơ & Tư vấn" class="step-icon">
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 01</span>
                            <h3 class="step-title">Nộp hồ sơ & Tư vấn</h3>
                            <p class="step-desc">Hoàn thiện thủ tục nhanh chóng, nhận lộ trình học tập cá nhân hóa phù hợp với thời gian của bạn.</p>
                        </div>
                    </div>
                    
                    <!-- Step 2 -->
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <img src="${pageContext.request.contextPath}/assets/imgs/step2_icon.png" alt="Học lý thuyết & Thực hành" class="step-icon">
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 02</span>
                            <h3 class="step-title">Học lý thuyết & Thực hành</h3>
                            <p class="step-desc">Lớp học lý thuyết sinh động kết hợp cùng giờ thực hành lái xe trên cabin mô phỏng và đường trường thực tế.</p>
                        </div>
                    </div>
                    
                    <!-- Step 3 -->
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <img src="${pageContext.request.contextPath}/assets/imgs/step3_icon.png" alt="Thi chứng chỉ tốt nghiệp" class="step-icon">
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 03</span>
                            <h3 class="step-title">Thi chứng chỉ tốt nghiệp</h3>
                            <p class="step-desc">Kỳ kiểm tra nội bộ đánh giá năng lực toàn diện trước khi bước vào kỳ thi sát hạch chính thức của Sở GTVT.</p>
                        </div>
                    </div>
                    
                    <!-- Step 4 -->
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <img src="${pageContext.request.contextPath}/assets/imgs/step4_icon.png" alt="Thi sát hạch" class="step-icon">
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 04</span>
                            <h3 class="step-title">Thi sát hạch</h3>
                            <p class="step-desc">Chinh phục 3 phần thi quan trọng: Lý thuyết trên máy tính, Thực hành và Đường trường.</p>
                        </div>
                    </div>
                    
                    <!-- Step 5 -->
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <img src="${pageContext.request.contextPath}/assets/imgs/step5_icon.png" alt="Nhận bằng GPLX" class="step-icon">
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 05</span>
                            <h3 class="step-title">Nhận bằng GPLX</h3>
                            <p class="step-desc">Chúc mừng! Bạn đã sở hữu bằng lái xe và sẵn sàng cho những hành trình an toàn cùng Lái Vui.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Bento Spotlight Section -->
    <section class="process-section bento-section">
        <div class="process-container">
            <h2 class="bento-heading">Tại sao nên chọn chúng tôi?</h2>
            
            <div class="bento-grid">
                <!-- Left Card: System training cars -->
                <div class="bento-card bento-card--left">
                    <div class="bento-card-bg" style="background-image: url('${pageContext.request.contextPath}/assets/imgs/training_car.png')"></div>
                    <div class="bento-card-overlay">
                        <div class="bento-card-content">
                            <h3 class="bento-card-title">Hệ thống xe đời mới</h3>
                            <p class="bento-card-desc">Toàn bộ xe thực hành được trang bị thiết bị cảm biến hiện đại và máy lạnh 100%.</p>
                        </div>
                    </div>
                </div>
                
                <!-- Right Card: High commitment -->
                <div class="bento-card bento-card--right">
                    <!-- Premium subtle background decoration icon (SVG shield) -->
                    <svg class="bento-card-decor-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="rgba(255, 255, 255, 0.05)" stroke-width="1.2" fill="rgba(255, 255, 255, 0.02)" />
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="rgba(255, 255, 255, 0.12)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M9 11l2 2 4-4" stroke="rgba(255, 255, 255, 0.35)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    
                    <div class="right-card-header">
                        <h3 class="bento-card-stat">Cam kết đầu ra 99%</h3>
                    </div>
                    <div class="right-card-body">
                        <p class="bento-card-desc">Chúng tôi không chỉ dạy bạn cách thi đỗ, chúng tôi dạy bạn cách trở thành một lái xe an toàn và văn minh.</p>
                    </div>
                    <div class="right-card-footer">
                        <a href="#" class="btn-bento-action">Xem chi tiết cam kết</a>
                    </div>
                </div>
            </div>
        </div>
    </section>
</main>
