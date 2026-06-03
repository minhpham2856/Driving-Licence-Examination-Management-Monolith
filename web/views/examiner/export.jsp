<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xuất file - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="xuat-file" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--scroll">

        <%-- Header Section --%>
        <div class="export-header">
            <h2 class="export-header__title">Xuất dữ liệu hệ thống</h2>
            <p class="export-header__desc">
                Lựa chọn định dạng phù hợp để tải xuống các báo cáo, biên bản và nhật ký hệ thống<br>
                phục vụ cho công tác lưu trữ và kiểm tra.
            </p>
        </div>

        <%-- Content Card --%>
        <div class="export-card">

            <%-- Row 1: Danh sách thí sinh --%>
            <div class="export-row">
                <div class="export-row__left">
                    <div class="export-row__icon export-row__icon--blue">
                        <%-- People / group icon --%>
                        <svg width="22" height="15" viewBox="0 0 24 24" fill="none">
                            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" fill="#3a56d4"/>
                        </svg>
                    </div>
                    <div class="export-row__info">
                        <p class="export-row__title">Danh sách thí sinh</p>
                        <p class="export-row__desc">Danh sách tổng hợp toàn bộ thí sinh tham gia đợt sát hạch.</p>
                    </div>
                </div>
                <div class="export-row__actions">
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">excel</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">XML</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">docx</span>
                    </a>
                </div>
            </div>

            <%-- Row 2: Kết quả thi --%>
            <div class="export-row">
                <div class="export-row__left">
                    <div class="export-row__icon export-row__icon--blue">
                        <%-- Clipboard / checklist icon --%>
                        <svg width="18" height="20" viewBox="0 0 24 24" fill="none">
                            <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" fill="#3a56d4"/>
                        </svg>
                    </div>
                    <div class="export-row__info">
                        <p class="export-row__title">Kết quả thi</p>
                        <p class="export-row__desc">Bảng điểm chi tiết từng phần thi của các thí sinh.</p>
                    </div>
                </div>
                <div class="export-row__actions">
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">excel</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">XML</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">docx</span>
                    </a>
                </div>
            </div>

            <%-- Row 3: Biên bản thi --%>
            <div class="export-row">
                <div class="export-row__left">
                    <div class="export-row__icon export-row__icon--blue">
                        <%-- Document icon --%>
                        <svg width="18" height="20" viewBox="0 0 24 24" fill="none">
                            <path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z" fill="#3a56d4"/>
                        </svg>
                    </div>
                    <div class="export-row__info">
                        <p class="export-row__title">Biên bản thi</p>
                        <p class="export-row__desc">Biên bản chính thức xác nhận quá trình tổ chức sát hạch.</p>
                    </div>
                </div>
                <div class="export-row__actions">
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">excel</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">XML</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">docx</span>
                    </a>
                </div>
            </div>

            <%-- Row 4: Biên bản vi phạm --%>
            <div class="export-row">
                <div class="export-row__left">
                    <div class="export-row__icon export-row__icon--red">
                        <%-- Warning / alert triangle icon --%>
                        <svg width="20" height="18" viewBox="0 0 24 24" fill="none">
                            <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" fill="#ba1a1a"/>
                        </svg>
                    </div>
                    <div class="export-row__info">
                        <p class="export-row__title">Biên bản vi phạm</p>
                        <p class="export-row__desc">Ghi nhận các trường hợp thí sinh vi phạm quy chế phòng thi.</p>
                    </div>
                </div>
                <div class="export-row__actions">
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">excel</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">XML</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">docx</span>
                    </a>
                </div>
            </div>

            <%-- Row 5: Nhật ký --%>
            <div class="export-row export-row--last">
                <div class="export-row__left">
                    <div class="export-row__icon export-row__icon--gray">
                        <%-- Log / list icon --%>
                        <svg width="20" height="14" viewBox="0 0 24 24" fill="none">
                            <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z" fill="#5f6368"/>
                        </svg>
                    </div>
                    <div class="export-row__info">
                        <p class="export-row__title">Nhật ký</p>
                        <p class="export-row__desc">Log hoạt động của hệ thống, tác động của giám thị và máy trạm.</p>
                    </div>
                </div>
                <div class="export-row__actions">
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">excel</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">XML</span>
                    </a>
                    <a href="#" class="export-btn">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>
                        <span class="export-btn__text">docx</span>
                    </a>
                </div>
            </div>

        </div>

    </main>
</div>

</body>
</html>
