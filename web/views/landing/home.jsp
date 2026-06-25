<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <jsp:include page="/views/layout/header.jsp">
        <jsp:param name="title" value="Lái Vui - Trang chủ" />
        <jsp:param name="activeNav" value="gioi-thieu" />
    </jsp:include>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing.css">

    <main class="public-main" style="padding: 0;">
        <!-- Section 1: Hero / History & Vision -->
        <section class="landing-section">
            <div class="landing-container">
                <div class="hero-history-grid">
                    <div class="hero-history-image-wrap">
                        <img class="hero-history-img" src="${pageContext.request.contextPath}/assets/imgs/history.png"
                            alt="Lái Vui History">
                    </div>
                    <div class="hero-history-content">
                        <span class="about-badge">Trung tâm Lái Vui</span>
                        <h2 class="hero-history-title">Sát hạch GPLX nhanh chóng, tiện lợi</h2>
                        <p class="hero-history-desc">
                            Thành lập từ năm 2010, Lái Vui không chỉ là trung tâm đào tạo và sát hạch lái xe.
                            Chúng tôi là những người tiên phong trong việc kết hợp công nghệ hiện đại
                            và tâm huyết giảng dạy để mỗi học viên không chỉ lấy được bằng lái mà còn
                            thực sự làm chủ tay lái với sự tự tin và trách nhiệm.
                        </p>
                        <div class="vision-mission-row">
                            <div class="vision-mission-card vision-card">
                                <h3>Đào tạo</h3>
                                <p>Trang bị kiến thức và kỹ năng lái xe an toàn cho mọi người.</p>
                            </div>
                            <div class="vision-mission-card mission-card">
                                <h3>Sát hạch</h3>
                                <p>Quy trình hiện đại, nhanh chóng, uy tín.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Section 2: Facilities Showcase -->
        <section class="landing-section facilities-section">
            <div class="landing-container">
                <div class="facilities-header">
                    <h2 class="facilities-title">Cơ sở vật chất hiện đại</h2>
                    <p class="facilities-desc">Chúng tôi đầu tư không ngừng vào hạ tầng và trang thiết bị để mang lại
                        trải nghiệm tốt nhất cho bạn.</p>
                </div>

                <div class="bento-grid">
                    <!-- Large Left Item: Sân tập lái -->
                    <div class="bento-left">
                        <div class="bento-bg"
                            style="background-image: url('${pageContext.request.contextPath}/assets/imgs/facilities_track.png');">
                        </div>
                        <div class="bento-overlay">
                            <h3 class="bento-card-title">Sân tập tiêu chuẩn</h3>
                            <p class="bento-card-desc">Diện tích 20.000m² với đầy đủ 11 bài thi sa hình, mô phỏng thực
                                tế các tình huống giao thông phức tạp nhất.</p>
                        </div>
                    </div>

                    <!-- Twin Right Items column -->
                    <div class="bento-right">
                        <!-- Top Item: Cabin 3D simulator -->
                        <div class="bento-card-small">
                            <div class="bento-bg"
                                style="background-image: url('${pageContext.request.contextPath}/assets/imgs/facilities_classroom.png');">
                            </div>
                            <div class="bento-overlay bento-overlay-small">
                                <h3 class="bento-card-title">Hệ thống lái cabin</h3>
                                <p class="bento-card-desc">Học viên được làm quen với cabin ảo trước khi thực hành trên
                                    xe thật.</p>
                            </div>
                        </div>

                        <!-- Bottom Item: Car fleet -->
                        <div class="bento-card-small">
                            <div class="bento-bg"
                                style="background-image: url('${pageContext.request.contextPath}/assets/imgs/facilities_cars.png');">
                            </div>
                            <div class="bento-overlay bento-overlay-small">
                                <h3 class="bento-card-title">Dàn xe tiêu chuẩn</h3>
                                <p class="bento-card-desc">Sử dụng các dòng xe phổ biến như Toyota Vios, Hyundai Accent
                                    được bảo dưỡng định kỳ.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Section 3: Instructor Team -->
        <section class="landing-section">
            <div class="landing-container">
                <div class="instructors-header">
                    <div class="instructors-title-wrap">
                        <h2 class="instructors-title">Đội ngũ giảng viên tận tâm</h2>
                    </div>
                    <a href="#" class="btn-view-all">
                        Xem tất cả giảng viên
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M6 12L10 8L6 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </a>
                </div>

                <div class="instructor-grid">
                    <!-- Instructor Card 1 -->
                    <div class="instructor-card">
                        <div class="instructor-img-wrap"
                            style="background-image: url('${pageContext.request.contextPath}/assets/imgs/instructor_1.png');">
                            <span class="instructor-badge">Hạng B</span>
                        </div>
                        <div class="instructor-info">
                            <h4 class="instructor-name">Thầy Trần Nam</h4>
                            <p class="instructor-role">Giảng viên</p>
                            <div class="instructor-meta">
                                <span class="instructor-exp">15 năm kinh nghiệm</span>
                                <span class="instructor-icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor"
                                            stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"
                                            stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Instructor Card 2 -->
                    <div class="instructor-card">
                        <div class="instructor-img-wrap"
                            style="background-image: url('${pageContext.request.contextPath}/assets/imgs/instructor_2.png');">
                            <span class="instructor-badge">Hạng A</span>
                        </div>
                        <div class="instructor-info">
                            <h4 class="instructor-name">Cô Lê Minh</h4>
                            <p class="instructor-role">Giảng viên</p>
                            <div class="instructor-meta">
                                <span class="instructor-exp">8 năm kinh nghiệm</span>
                                <span class="instructor-icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor"
                                            stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"
                                            stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Instructor Card 3 -->
                    <div class="instructor-card">
                        <div class="instructor-img-wrap"
                            style="background-image: url('${pageContext.request.contextPath}/assets/imgs/instructor_3.png');">
                            <span class="instructor-badge">Hạng C</span>
                        </div>
                        <div class="instructor-info">
                            <h4 class="instructor-name">Thầy Phạm Hùng</h4>
                            <p class="instructor-role">Giảng viên</p>
                            <div class="instructor-meta">
                                <span class="instructor-exp">12 năm kinh nghiệm</span>
                                <span class="instructor-icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor"
                                            stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"
                                            stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Instructor Card 4 -->
                    <div class="instructor-card">
                        <div class="instructor-img-wrap"
                            style="background-image: url('${pageContext.request.contextPath}/assets/imgs/instructor_4.png');">
                            <span class="instructor-badge">Hạng B1/B</span>
                        </div>
                        <div class="instructor-info">
                            <h4 class="instructor-name">Cô Nguyễn Anh</h4>
                            <p class="instructor-role">Giảng viên</p>
                            <div class="instructor-meta">
                                <span class="instructor-exp">10 năm kinh nghiệm</span>
                                <span class="instructor-icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor"
                                            stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"
                                            stroke-linecap="round" stroke-linejoin="round" />
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Section 4: CTA Section -->
        <section class="landing-section">
            <div class="landing-container">
                <div class="cta-container">
                    <div class="cta-ambient-1"></div>
                    <div class="cta-ambient-2"></div>
                    <h2 class="cta-title">Sẵn sàng bắt đầu hành trình của bạn?</h2>
                    <p class="cta-desc">Gia nhập cộng đồng hơn 50.000 học viên đã tốt nghiệp tại Lái Vui và nhận chứng
                        chỉ lái xe an toàn ngay hôm nay.</p>
                    <div class="cta-buttons">
                        <a href="register.jsp" class="btn-cta-primary">Đăng ký ngay</a>
                        <a href="#" class="btn-cta-secondary">Liên hệ tư vấn</a>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <jsp:include page="/views/layout/footer.jsp" />
