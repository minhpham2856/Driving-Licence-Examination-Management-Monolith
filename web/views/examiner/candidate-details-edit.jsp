<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin chi tiết - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="sua-thong-tin" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--scroll">
        <section class="examiner-toolbar">
            <div class="exr-toolbar-left">
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details.jsp" class="exr-back">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" fill="currentColor"/></svg>
                    QUAY LẠI
                </a>
                <h2 class="examiner-toolbar__title">Thông tin chi tiết</h2>
            </div>
            <div class="examiner-toolbar__actions">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="15" height="14" viewBox="0 0 24 24" fill="none"><path d="M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3M16 19H8v-5h8v5M19 12c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1M18 3H6v4h12V3z" fill="currentColor"/></svg>
                    In thông tin chi tiết
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-paper.jsp" class="examiner-btn examiner-btn--white">
                    <svg width="17" height="12" viewBox="0 0 24 24" fill="none"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" fill="currentColor"/></svg>
                    Xem đề thi
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-btn examiner-btn--primary">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" fill="currentColor"/></svg>
                    Sửa kết quả
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/></svg>
                </a>
            </div>
        </section>

        <section class="examiner-bento">
            <div class="examiner-bento__profile">
                <div class="examiner-profile__photo">
                    <svg width="72" height="90" viewBox="0 0 24 24" fill="none"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" fill="#9aa1b1"/></svg>
                </div>
                <p class="examiner-profile__name">Nguyễn Văn A</p>
            </div>

            <div class="examiner-bento__detail">
                <div class="examiner-detail-section">
                    <svg class="examiner-detail-section__icon" width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" fill="currentColor"/></svg>
                    <span>THÔNG TIN CÁ NHÂN</span>
                </div>

                <div class="examiner-fields">
                    <div class="examiner-field">
                        <p class="examiner-field__label">Họ và Tên</p>
                        <p class="examiner-field__value">Nguyễn Văn A</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Số báo danh (SBD)</p>
                        <p class="examiner-field__value examiner-field__value--mono">12045</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Số căn cước công dân</p>
                        <p class="examiner-field__value examiner-field__value--mono">079012345678</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Ngày sinh</p>
                        <p class="examiner-field__value examiner-field__value--mono">15/08/1995</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Giới tính</p>
                        <p class="examiner-field__value">Nam</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Địa chỉ</p>
                        <p class="examiner-field__value examiner-field__value--ellipsis">123 Nguyễn Văn Linh, P. Tân Phong, Q.7, TP.HCM</p>
                    </div>

                    <div class="examiner-detail-section examiner-detail-section--full">
                        <svg class="examiner-detail-section__icon" width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M3 13h2v-2H3v2m0 4h2v-2H3v2m0-8h2V7H3v2m4 4h14v-2H7v2m0 4h14v-2H7v2M7 7v2h14V7H7z" fill="currentColor"/></svg>
                        <span>CHI TIẾT KỲ THI</span>
                    </div>

                    <div class="examiner-field">
                        <p class="examiner-field__label">Hạng GPLX</p>
                        <p class="examiner-field__value examiner-field__value--bold">B</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Ngày thi</p>
                        <p class="examiner-field__value examiner-field__value--mono">24/10/2023</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Điểm thi Lý Thuyết (LT)</p>
                        <p class="examiner-field__value examiner-field__value--mono examiner-field__value--green">25/25</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Điểm thi Thực Hành (TH)</p>
                        <p class="examiner-field__value examiner-field__value--mono">x</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Điểm thi Sa Hình (SH)</p>
                        <p class="examiner-field__value examiner-field__value--mono">90/100</p>
                    </div>
                    <div class="examiner-field">
                        <p class="examiner-field__label">Điểm thi Đường Trường (ĐT)</p>
                        <p class="examiner-field__value examiner-field__value--mono">80/100</p>
                    </div>
                    <div class="examiner-field examiner-field--full">
                        <p class="examiner-field__label">Lý do sát hạch</p>
                        <p class="examiner-field__value">Cấp mới</p>
                    </div>
                </div>
            </div>
        </section>
    </main>
</div>

</body>
</html>
