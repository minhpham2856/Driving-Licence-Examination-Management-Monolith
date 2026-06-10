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
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
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
                    <span class="material-symbols-outlined">arrow_back</span>
                    QUAY LẠI
                </a>
                <h2 class="examiner-toolbar__title">Thông tin chi tiết</h2>
            </div>
            <div class="examiner-toolbar__actions">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">print</span>
                    In thông tin chi tiết
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-paper.jsp" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">visibility</span>
                    Xem đề thi
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">edit</span>
                    Sửa kết quả
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <span class="material-symbols-outlined">refresh</span>
                </a>
            </div>
        </section>

        <section class="examiner-bento">
            <div class="examiner-bento__profile">
                <div class="examiner-profile__photo">
                    <span class="examiner-profile__photo-icon material-symbols-outlined">person</span>
                </div>
                <p class="examiner-profile__name">Nguyễn Văn A</p>
            </div>

            <div class="examiner-bento__detail">
                <div class="examiner-detail-section">
                    <span class="examiner-detail-section__icon material-symbols-outlined">person</span>
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
                        <span class="examiner-detail-section__icon material-symbols-outlined">list_alt</span>
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
