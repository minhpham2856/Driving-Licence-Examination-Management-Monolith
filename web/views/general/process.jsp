<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Quy trình đăng ký & thi" />
    <jsp:param name="activeNav" value="quy-trinh" />
</jsp:include>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/landing/process.css">

<main class="public-main process-page">
    <%--hero--%>
    <section class="process-hero">
        <div class="hero-glow hero-glow--left" aria-hidden="true"></div>
        <div class="hero-glow hero-glow--right" aria-hidden="true"></div>
        <div class="process-container hero-content">
            <h1 class="hero-title">Từ đăng ký đến nhận bằng</h1>
            <p class="hero-subtitle">
                Lộ trình đăng ký thi sát hạch GPLX tại hệ thống Lái Vui - năm bước rõ ràng, theo dõi online.
            </p>
            <a href="#lien-he" class="hero-contact-link">Liên hệ trung tâm ↓</a>
        </div>
    </section>

    <%--timeline steps--%>
    <section class="process-section timeline-section" id="quy-trinh">
        <div class="process-container">
            <div class="timeline-wrapper">
                <div class="timeline-connector" aria-hidden="true"></div>

                <div class="timeline-steps">
                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <span class="material-symbols-outlined step-icon" aria-hidden="true">person_add</span>
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 01</span>
                            <h3 class="step-title">Đăng ký tài khoản</h3>
                            <p class="step-desc">Tạo tài khoản trên hệ thống bằng email / căn cước.</p>
                        </div>
                    </div>

                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <span class="material-symbols-outlined step-icon" aria-hidden="true">upload_file</span>
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 02</span>
                            <h3 class="step-title">Hoàn thiện hồ sơ</h3>
                            <p class="step-desc">Bổ sung thông tin cá nhân và giấy tờ theo hướng dẫn.</p>
                        </div>
                    </div>

                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <span class="material-symbols-outlined step-icon" aria-hidden="true">badge</span>
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 03</span>
                            <h3 class="step-title">Đăng ký hạng thi</h3>
                            <p class="step-desc">Chọn hạng GPLX (A1, A, B1) và nộp lệ phí dự thi.</p>
                        </div>
                    </div>

                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <span class="material-symbols-outlined step-icon" aria-hidden="true">directions_car</span>
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 04</span>
                            <h3 class="step-title">Tham dự sát hạch</h3>
                            <p class="step-desc">Thi lý thuyết trên máy và thực hành theo lịch gọi.</p>
                        </div>
                    </div>

                    <div class="timeline-step">
                        <div class="step-icon-container">
                            <span class="material-symbols-outlined step-icon"
                                  aria-hidden="true">workspace_premium</span>
                        </div>
                        <div class="step-content">
                            <span class="step-number">BƯỚC 05</span>
                            <h3 class="step-title">Nhận kết quả</h3>
                            <p class="step-desc">Tra cứu kết quả online và nhận giấy phép khi đạt.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <%--why choose us--%>
    <section class="process-section bento-section">
        <div class="process-container">
            <h2 class="bento-heading">Tại sao nên chọn chúng tôi?</h2>

            <div class="bento-grid">
                <div class="bento-card bento-card--left">
                    <div class="bento-card-bg"
                         style="background-image: url('${pageContext.request.contextPath}/assets/imgs/training_car.png')">
                    </div>
                    <div class="bento-card-overlay">
                        <div class="bento-card-content">
                            <h3 class="bento-card-title">Hệ thống thi hiện đại</h3>
                            <p class="bento-card-desc">
                                Gọi thí sinh, chấm điểm và cập nhật kết quả realtime.
                            </p>
                        </div>
                    </div>
                </div>

                <div class="bento-card bento-card--right">
                    <div class="right-card-header">
                        <h3 class="bento-card-stat">Minh bạch 100%</h3>
                    </div>
                    <div class="right-card-body">
                        <p class="bento-card-desc">
                            Theo dõi trạng thái hồ sơ và lịch thi rõ ràng trên tài khoản của bạn.
                        </p>
                    </div>
                    <div class="right-card-footer">
                        <a href="${pageContext.request.contextPath}/register"
                           class="btn-bento-action">Đăng ký ngay</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <%--contact strip--%>
    <section class="process-section contact-section" id="lien-he">
        <div class="process-container">
            <div class="contact-strip">
                <div class="contact-strip__intro">
                    <p class="contact-strip__eyebrow">Liên hệ trung tâm</p>
                    <h2 class="contact-strip__title">Cần hỗ trợ đăng ký hoặc lịch thi?</h2>
                    <p class="contact-strip__desc">
                        Gọi hoặc gửi email - đội ngũ tư vấn phản hồi trong giờ hành chính.
                    </p>
                </div>
                <div class="contact-strip__channels">
                    <div class="contact-channel contact-channel--static">
                        <span class="contact-channel__label">Điện thoại</span>
                        <span class="contact-channel__value">(028) 3820 1234</span>
                    </div>
                    <div class="contact-channel contact-channel--static">
                        <span class="contact-channel__label">Email</span>
                        <span class="contact-channel__value">trungtamlaivui@gmail.com</span>
                    </div>
                    <div class="contact-channel contact-channel--static">
                        <span class="contact-channel__label">Giờ làm việc</span>
                        <span class="contact-channel__value">T2–T7 · 7:30–17:00</span>
                    </div>
                </div>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/views/layout/footer.jsp" />
