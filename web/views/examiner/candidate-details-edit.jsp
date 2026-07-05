<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Chi tiết thí sinh" />
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details" scope="request" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-details-edit?sbd=${candidate.sbd}" scope="request" />
<c:set var="paperUrl" value="${ctx}/views/examiner/candidate-paper?sbd=${candidate.sbd}" scope="request" />

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
            <jsp:param name="pageCss" value="candidate-detail.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="candidate-details" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll">

                <!--action message-->
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="examiner-toolbar" />
                    <jsp:param name="leftClass" value="exr-toolbar-left" />
                    <jsp:param name="rightClass" value="examiner-toolbar__actions" />
                    <jsp:param name="backClass" value="exr-back" />
                    <jsp:param name="btnBack" value="left" />
                    <jsp:param name="btnViewPaper" value="left" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--candidate detail (read-only)-->
                <c:choose>
                    <c:when test="${empty candidate}">
                        <section class="examiner-card">
                            <p class="examiner-table__empty">Không tìm thấy thí sinh.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <div class="examiner-bento examiner-bento--form">

                            <!--person card-->
                            <div class="examiner-bento__profile">
                                <div class="examiner-profile__photo">
                                    <span class="examiner-profile__photo-icon material-symbols-outlined">person</span>
                                </div>
                                <p class="examiner-profile__name">${candidate.fullName}</p>
                                <p class="examiner-profile__sbd">SBD: ${candidate.sbd}</p>
                            </div>

                            <!--info card-->
                            <div class="examiner-bento__detail">

                                <div class="examiner-detail-section">
                                    <span class="examiner-detail-section__icon material-symbols-outlined">person</span>
                                    <span>THÔNG TIN CÁ NHÂN</span>
                                </div>

                                <div class="examiner-fields examiner-fields--form">
                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Họ và Tên</p>
                                        <p class="examiner-field__value">${candidate.fullName}</p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">SBD</p>
                                        <p class="examiner-field__value examiner-field__value--mono examiner-field__value--bold">
                                            ${candidate.sbd}
                                        </p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Số căn cước</p>
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
                                        <p class="examiner-field__label">Số điện thoại</p>
                                        <p class="examiner-field__value examiner-field__value--mono">${empty candidate.phoneNo ? '-' : candidate.phoneNo}</p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Email</p>
                                        <p class="examiner-field__value">${empty candidate.email ? '-' : candidate.email}</p>
                                    </div>

                                    <div class="examiner-field examiner-field--full">
                                        <p class="examiner-field__label">Địa chỉ</p>
                                        <p class="examiner-field__value">${empty candidate.address ? '-' : candidate.address}</p>
                                    </div>

                                    <div class="examiner-field examiner-field--full">
                                        <p class="examiner-field__label">Tình trạng</p>
                                        <p class="examiner-field__value">${candidate.statusLabel}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </body>
</html>
