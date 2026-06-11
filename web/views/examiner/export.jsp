<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Xuất file" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${cssStyle}">
        <link rel="stylesheet" href="${cssLayout}">
    </head>
    <body class="has-side-nav-bar examiner-portal">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="xuat-file" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <!--page intro-->
                <div class="export-header">
                    <h2 class="export-header__title">Xuất dữ liệu hệ thống</h2>
                    <p class="export-header__desc">
                        Lựa chọn định dạng phù hợp để tải xuống các báo cáo, biên bản và nhật ký hệ thống<br>
                        phục vụ cho công tác lưu trữ và kiểm tra.
                    </p>
                </div>

                <!--export list-->
                <div class="export-card">
                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">group</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Danh sách thí sinh</p>
                                <p class="export-row__desc">Danh sách tổng hợp toàn bộ thí sinh tham gia đợt sát hạch.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${exportCandidatesUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">assignment</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Kết quả thi</p>
                                <p class="export-row__desc">Bảng điểm chi tiết từng phần thi của các thí sinh.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">description</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Biên bản thi</p>
                                <p class="export-row__desc">Biên bản chính thức xác nhận quá trình tổ chức sát hạch.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--red">
                                <span class="material-symbols-outlined">warning</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Biên bản vi phạm</p>
                                <p class="export-row__desc">Ghi nhận các trường hợp thí sinh vi phạm quy chế phòng thi.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row export-row--last">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--gray">
                                <span class="material-symbols-outlined">list_alt</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Nhật ký</p>
                                <p class="export-row__desc">Log hoạt động của hệ thống, tác động của giám thị và máy trạm.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="#" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>
                </div>
            </main>
        </div>

    </body>
</html>
