<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Chi tiết thí sinh" />
<c:set var="backUrl" value="${param.from == 'action' ? ctx.concat('/examiner/action') : ctx.concat('/examiner/candidates')}" scope="request" />
<c:if test="${not empty candidate}">
    <c:set var="pageUrl" value="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}" scope="request" />
    <c:set var="paperUrl" value="${ctx}/examiner/candidate-paper?sbd=${candidate.candidateNumber}" scope="request" />
    <c:set var="resultUrl" value="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}" scope="request" />
</c:if>

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
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">

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

                <!--candidate detail-->
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
                                    <c:choose>
                                        <c:when test="${not empty candidate.photoImageUrl}">
                                            <img src="${candidate.photoImageUrl}" alt="Ảnh thí sinh" class="examiner-profile__photo-img">
                                        </c:when>
                                        <c:otherwise>
                                            <span class="examiner-profile__photo-icon material-symbols-outlined">person</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <p class="examiner-profile__name">${candidate.fullName}</p>
                                <p class="examiner-profile__sbd">SBD: ${candidate.candidateNumber}</p>
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
                                        <p class="examiner-field__value examiner-field__value--mono examiner-field__value--bold">${candidate.candidateNumber}</p>
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
                                        <p class="examiner-field__value">${candidate.sexLabel}</p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Số điện thoại</p>
                                        <p class="examiner-field__value examiner-field__value--mono">${empty candidate.phoneNo ? '' : candidate.phoneNo}</p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Email</p>
                                        <p class="examiner-field__value">${empty candidate.email ? '' : candidate.email}</p>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Hạng thi</p>
                                        <p class="examiner-field__value">${empty candidate.licenceClass ? '' : candidate.licenceClass}</p>
                                    </div>

                                    <div class="examiner-field examiner-field--full">
                                        <p class="examiner-field__label">Địa chỉ</p>
                                        <p class="examiner-field__value">${empty candidate.address ? '' : candidate.address}</p>
                                    </div>

                                    <c:if test="${not empty candidate.reasonForTaking}">
                                        <div class="examiner-field examiner-field--full">
                                            <p class="examiner-field__label">Lý do thi</p>
                                            <p class="examiner-field__value">${candidate.reasonForTaking}</p>
                                        </div>
                                    </c:if>

                                    <div class="examiner-form-actions">
                                        <a href="${backUrl}" class="examiner-btn examiner-btn--white">Quay lại</a>
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
