<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="In văn bản" />

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
        <jsp:include page="/views/examiner/partials/examiner-styles.jsp">
            <jsp:param name="pageCss" value="export.css,print.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="in-van-ban" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <p class="export-header__desc">
                    Lựa chọn biên bản và phiếu in phục vụ công tác tổ chức sát hạch tại ca thi hiện tại.
                </p>

                <div class="export-card">
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
                            <a href="#" class="print-btn" onclick="window.print(); return false;">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">group</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Danh sách thí sinh</p>
                                <p class="export-row__desc">Danh sách tổng hợp thí sinh tham gia ca thi.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="#" class="print-btn" onclick="window.print(); return false;">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
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
                            <a href="#" class="print-btn" onclick="window.print(); return false;">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">assignment_turned_in</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Phiếu điểm thực hành</p>
                                <p class="export-row__desc">Phiếu ghi nhận điểm và lỗi trừ điểm thi sa hình.</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${ctx}/views/examiner/score-entry" class="print-btn">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row export-row--last">
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
                            <a href="#" class="print-btn" onclick="window.print(); return false;">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>
