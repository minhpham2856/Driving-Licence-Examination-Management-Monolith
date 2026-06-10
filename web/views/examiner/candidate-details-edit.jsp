<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/layout/examiner-seed-data.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Thông tin chi tiết" />
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details.jsp" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-details-edit.jsp?sbd=${candidate.sbd}" />
<c:set var="paperUrl" value="${ctx}/views/examiner/candidate-paper.jsp?sbd=${candidate.sbd}" />
<c:set var="resultUrl" value="${ctx}/views/examiner/result-details-edit.jsp?sbd=${candidate.sbd}" />

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
            <jsp:param name="activeSidebar" value="sua-thong-tin" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <!--toolbar-->
                <section class="examiner-toolbar">
                    <div class="exr-toolbar-left">
                        <a href="${backUrl}" class="exr-back">
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
                        <a href="${paperUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">visibility</span>
                            Xem đề thi
                        </a>
                        <a href="${resultUrl}" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">edit</span>
                            Sửa kết quả
                        </a>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <!--detail-->
                <section class="examiner-bento">
                    <div class="examiner-bento__profile">
                        <div class="examiner-profile__photo">
                            <span class="examiner-profile__photo-icon material-symbols-outlined">person</span>
                        </div>
                        <p class="examiner-profile__name">${candidate.fullName}</p>
                    </div>

                    <div class="examiner-bento__detail">
                        <div class="examiner-detail-section">
                            <span class="examiner-detail-section__icon material-symbols-outlined">person</span>
                            <span>THÔNG TIN CÁ NHÂN</span>
                        </div>

                        <div class="examiner-fields">
                            <div class="examiner-field">
                                <p class="examiner-field__label">Họ và Tên</p>
                                <p class="examiner-field__value">${candidate.fullName}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Số báo danh (SBD)</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.sbd}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Số căn cước công dân</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.governmentId}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Ngày sinh</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.dob}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Giới tính</p>
                                <p class="examiner-field__value">${candidate.sex}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Địa chỉ</p>
                                <p class="examiner-field__value examiner-field__value--ellipsis">${candidate.address}</p>
                            </div>

                            <div class="examiner-detail-section examiner-detail-section--full">
                                <span class="examiner-detail-section__icon material-symbols-outlined">list_alt</span>
                                <span>CHI TIẾT KỲ THI</span>
                            </div>

                            <div class="examiner-field">
                                <p class="examiner-field__label">Hạng GPLX</p>
                                <p class="examiner-field__value examiner-field__value--bold">${candidate.licenceClass}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Ngày thi</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.examDate}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Điểm thi Lý Thuyết (LT)</p>
                                <p class="examiner-field__value examiner-field__value--mono examiner-field__value--green">${candidate.scoreTheory}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Điểm thi Thực Hành (TH)</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scorePractical}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Điểm thi Sa Hình (SH)</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scoreRoadLayout}</p>
                            </div>
                            <div class="examiner-field">
                                <p class="examiner-field__label">Điểm thi Đường Trường (ĐT)</p>
                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scoreOnRoad}</p>
                            </div>
                            <div class="examiner-field examiner-field--full">
                                <p class="examiner-field__label">Lý do sát hạch</p>
                                <p class="examiner-field__value">${candidate.reasonForTaking}</p>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>
