<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Xác nhận vắng thi" />
<c:set var="sbd" value="${not empty param.sbd ? param.sbd : candidate.sbd}" />
<c:set var="name" value="${not empty param.name ? param.name : candidate.fullName}" />
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-call" />

<!--page-->
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
        <jsp:include page="/views/examiner/components/examiner-styles.jsp">
            <jsp:param name="pageCss" value="result-edit.css,confirmation.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="candidate-call" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll examiner-main--confirmation">
                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="examiner-toolbar" />
                    <jsp:param name="leftClass" value="exr-toolbar-left" />
                    <jsp:param name="backClass" value="exr-back" />
                    <jsp:param name="btnBack" value="left" />
                </jsp:include>

                <!--confirmation-->
                <section class="exr-grid">
                    <div class="exr-col-left">
                        <div class="exr-card exr-card--accent">
                            <div class="exr-section-title">
                                <span class="material-symbols-outlined">person</span>
                                <span>THÔNG TIN THÍ SINH</span>
                            </div>
                            <div class="exr-id-grid">
                                <div class="exr-field">
                                    <p class="exr-field__label">HỌ VÀ TÊN</p>
                                    <p class="exr-field__value">
                                        <c:choose>
                                            <c:when test="${not empty name}">${name}</c:when>
                                            <c:otherwise><span class="exr-field__value--sm">-</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="exr-field">
                                    <p class="exr-field__label">SỐ BÁO DANH</p>
                                    <c:choose>
                                        <c:when test="${not empty sbd}">
                                            <span class="exr-chip">${sbd}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="exr-field__value exr-field__value--sm">-</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <div class="exr-warning">
                            <span class="exr-warning__icon material-symbols-outlined">warning</span>
                            <div class="exr-warning__body">
                                <p class="exr-warning__title">CẢNH BÁO</p>
                                <p class="exr-warning__text">
                                    Thí sinh sẽ được đánh dấu <strong>vắng thi</strong> và không thể tham gia ca thi hiện tại.
                                    Thao tác này được ghi nhận vào nhật ký hệ thống.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="exr-col-right">
                        <div class="exr-card">
                            <div class="exr-section-title">
                                <span class="material-symbols-outlined">event_busy</span>
                                <span>XÁC NHẬN VẮNG THI</span>
                            </div>

                            <div class="examiner-absence-actions">
                                <form action="${backUrl}" method="get">
                                    <input type="hidden" name="absenceConfirmed" value="1">
                                    <input type="hidden" name="sbd" value="${sbd}">
                                    <button type="submit" class="examiner-btn-absence">
                                        <span class="material-symbols-outlined">event_busy</span>
                                        XÁC NHẬN VẮNG
                                    </button>
                                </form>
                                <a href="${backUrl}" class="examiner-btn examiner-btn--white examiner-absence-cancel">Hủy bỏ</a>
                                <p class="examiner-absence-note">
                                    Nhấn <strong>Xác nhận vắng</strong> đồng nghĩa bạn chịu trách nhiệm về quyết định này.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>
