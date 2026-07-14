<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa thông tin" />
<c:set var="backUrl" value="${ctx}/examiner/candidate-details" scope="request" />
<c:set var="pageUrl" value="${ctx}/examiner/candidate-details-edit?sbd=${candidate.sbd}" scope="request" />
<c:set var="paperUrl" value="${ctx}/examiner/candidate-paper?sbd=${candidate.sbd}" scope="request" />
<c:set var="resultUrl" value="${ctx}/examiner/result-details-edit?sbd=${candidate.sbd}" scope="request" />

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
                    <jsp:param name="btnEditResult" value="left" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--candidate form-->
                <c:choose>
                    <c:when test="${empty candidate}">
                        <section class="examiner-card">
                            <p class="examiner-table__empty">Không tìm thấy thí sinh.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <form action="${ctx}/examiner/candidate-details-edit" method="post" 
                              class="examiner-bento examiner-bento--form">
                            <input type="hidden" name="sbd" value="${candidate.sbd}">

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

                                <!--header-->
                                <div class="examiner-detail-section">
                                    <span class="examiner-detail-section__icon material-symbols-outlined">person</span>
                                    <span>THÔNG TIN CÁ NHÂN</span>
                                </div>

                                <!--info-->
                                <div class="examiner-fields examiner-fields--form">
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="fullName">Họ và Tên</label>
                                        <input type="text" id="fullName" name="fullName" class="exr-input" required
                                               value="${candidate.fullName != '' ? candidate.fullName : ''}">
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">SBD</p>
                                        <p class="examiner-field__value examiner-field__value--mono examiner-field__value--bold">
                                            ${candidate.sbd}
                                        </p>
                                    </div>

                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="govIdNo">Số căn cước</label>
                                        <input type="text" id="govIdNo" name="govIdNo" class="exr-input exr-input--mono" required
                                               value="${candidate.governmentId != '' ? candidate.governmentId : ''}">
                                    </div>

                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="dateOfBirth">Ngày sinh</label>
                                        <input type="date" id="dateOfBirth" name="dateOfBirth" class="exr-input exr-input--mono" required
                                               value="${candidate.dobRaw}">
                                    </div>

                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="sex">Giới tính</label>
                                        <select id="sex" name="sex" class="exr-select">
                                            <option value="0" ${candidate.sexValue eq '0' ? 'selected' : ''}>Nam</option>
                                            <option value="1" ${candidate.sexValue eq '1' ? 'selected' : ''}>Nữ</option>
                                        </select>
                                    </div>

                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="phoneNo">Số điện thoại</label>
                                        <input type="text" id="phoneNo" name="phoneNo" class="exr-input exr-input--mono"
                                               value="${candidate.phoneNo}">
                                    </div>

                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="email">Email</label>
                                        <input type="email" id="email" name="email" class="exr-input"
                                               value="${candidate.email}">
                                    </div>

                                    <div class="examiner-field examiner-field--full">
                                        <label class="examiner-field__label" for="address">Địa chỉ</label>
                                        <input type="text" id="address" name="address" class="exr-input"
                                               value="${candidate.address != '' ? candidate.address : ''}">
                                    </div>

                                    <div class="examiner-form-actions">
                                        <button type="submit" class="examiner-btn examiner-btn--primary">
                                            <span class="material-symbols-outlined">save</span>Lưu thông tin
                                        </button>
                                        <a href="${backUrl}" class="examiner-btn examiner-btn--white">Hủy</a>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </body>
</html>
