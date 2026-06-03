<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa kết quả - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="sua-ket-qua" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--scroll">
        <section class="examiner-toolbar">
            <div class="exr-toolbar-left">
                <a href="${pageContext.request.contextPath}/views/examiner/result-details.jsp" class="exr-back">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" fill="currentColor"/></svg>
                    QUAY LẠI
                </a>
                <h2 class="examiner-toolbar__title">Sửa kết quả</h2>
            </div>
            <div class="examiner-toolbar__actions">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="15" height="14" viewBox="0 0 24 24" fill="none"><path d="M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3M16 19H8v-5h8v5M19 12c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1M18 3H6v4h12V3z" fill="currentColor"/></svg>
                    In thông tin chi tiết
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="17" height="12" viewBox="0 0 24 24" fill="none"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" fill="currentColor"/></svg>
                    Xem đề thi
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/></svg>
                </a>
            </div>
        </section>

        <section class="exr-grid">
            <div class="exr-col-left">
                <div class="exr-card exr-card--accent">
                    <div class="exr-section-title">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" fill="currentColor"/></svg>
                        <span>THÔNG TIN THÍ SINH</span>
                    </div>
                    <div class="exr-id-grid">
                        <div class="exr-field">
                            <p class="exr-field__label">HỌ VÀ TÊN</p>
                            <p class="exr-field__value">Nguyễn Văn Quyết</p>
                        </div>
                        <div class="exr-field">
                            <p class="exr-field__label">CCCD / CMND</p>
                            <span class="exr-chip">031092004581</span>
                        </div>
                        <div class="exr-field">
                            <p class="exr-field__label">SỐ BÁO DANH</p>
                            <span class="exr-chip">SBD-4829</span>
                        </div>
                        <div class="exr-field">
                            <p class="exr-field__label">NGÀY THI</p>
                            <p class="exr-field__value exr-field__value--sm">15/10/2026</p>
                        </div>
                        <div class="exr-field">
                            <p class="exr-field__label">HẠNG GPLX</p>
                            <p class="exr-field__value exr-field__value--bold">Hạng B2</p>
                        </div>
                    </div>
                </div>

                <div class="exr-pair">
                    <div class="exr-card exr-card--mod">
                        <div class="exr-section-title">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" fill="currentColor"/></svg>
                            <span>ĐIỀU CHỈNH ĐIỂM</span>
                        </div>
                        <div class="exr-score-box">
                            <p class="exr-field__label">ĐIỂM HIỆN TẠI</p>
                            <div class="exr-score-row">
                                <span class="exr-score-current">25</span>
                                <span class="exr-score-total">/35</span>
                                <span class="exr-badge-fail">KHÔNG ĐẠT</span>
                            </div>
                        </div>
                        <div class="exr-control">
                            <label class="exr-input-label" for="newScore">ĐIỂM MỚI <span class="exr-req">*</span></label>
                            <div class="exr-input-suffix">
                                <input type="text" id="newScore" class="exr-input exr-input--mono" placeholder="Nhập điểm số">
                                <span class="exr-input-suffix__text">/35</span>
                            </div>
                        </div>
                    </div>

                    <div class="exr-card">
                        <div class="exr-section-title">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M3 13h2v-2H3v2m0 4h2v-2H3v2m0-8h2V7H3v2m4 4h14v-2H7v2m0 4h14v-2H7v2M7 7v2h14V7H7z" fill="currentColor"/></svg>
                            <span>LÝ DO ĐIỀU CHỈNH</span>
                        </div>
                        <div class="exr-control">
                            <label class="exr-input-label" for="reason">CHỌN LÝ DO <span class="exr-req">*</span></label>
                            <select id="reason" class="exr-select">
                                <option value="">-- Lựa chọn lý do quy định --</option>
                                <option value="cham-sai">Chấm sai</option>
                                <option value="nhap-nham">Nhập nhầm điểm</option>
                                <option value="khieu-nai">Thí sinh khiếu nại</option>
                                <option value="khac">Lý do khác</option>
                            </select>
                        </div>
                        <div class="exr-control">
                            <label class="exr-input-label" for="reasonDetail">LÝ DO CHI TIẾT</label>
                            <textarea id="reasonDetail" class="exr-textarea" placeholder="Nhập mô tả chi tiết nguyên nhân dẫn đến việc thay đổi điểm số..."></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div class="exr-col-right">
                <div class="exr-warning">
                    <svg class="exr-warning__icon" width="22" height="21" viewBox="0 0 24 24" fill="none"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" fill="currentColor"/></svg>
                    <div class="exr-warning__body">
                        <p class="exr-warning__title">CẢNH BÁO HỆ THỐNG</p>
                        <p class="exr-warning__text">Mọi thao tác thay đổi điểm số đều được lưu trữ vĩnh viễn vào nhật ký hệ thống.</p>
                    </div>
                </div>

                <div class="exr-card">
                    <div class="exr-section-title">
                        <svg width="14" height="15" viewBox="0 0 24 24" fill="none"><path d="M12 17a2 2 0 0 0 2-2 2 2 0 0 0-2-2 2 2 0 0 0-2 2 2 2 0 0 0 2 2m6-9a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V10a2 2 0 0 1 2-2h1V6a5 5 0 0 1 5-5 5 5 0 0 1 5 5v2h1m-6-5a3 3 0 0 0-3 3v2h6V6a3 3 0 0 0-3-3z" fill="currentColor"/></svg>
                        <span>XÁC THỰC BẢO MẬT</span>
                    </div>
                    <div class="exr-control">
                        <label class="exr-input-label" for="pwd">MẬT KHẨU <span class="exr-req">*</span></label>
                        <input type="password" id="pwd" class="exr-input" placeholder="Nhập mật khẩu của bạn">
                    </div>
                    <div class="exr-control">
                        <label class="exr-input-label">MÃ XÁC NHẬN <span class="exr-req">*</span></label>
                        <div class="exr-captcha">
                            <span class="exr-captcha__img">8H3K9A</span>
                            <a href="#" class="exr-captcha__refresh">
                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/></svg>
                            </a>
                        </div>
                        <input type="text" class="exr-input exr-input--mono exr-input--captcha" placeholder="NHẬP MÃ XÁC NHẬN">
                    </div>
                    <div class="exr-confirm-wrap">
                        <button type="button" class="exr-btn-confirm">
                            <svg width="12" height="15" viewBox="0 0 24 24" fill="none"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" fill="currentColor"/></svg>
                            XÁC NHẬN THAY ĐỔI ĐIỂM
                        </button>
                        <p class="exr-confirm-note">Nhấn xác nhận đồng nghĩa với việc bạn chịu trách nhiệm hoàn toàn về thay đổi này.</p>
                    </div>
                </div>
            </div>
        </section>
    </main>
</div>

</body>
</html>
