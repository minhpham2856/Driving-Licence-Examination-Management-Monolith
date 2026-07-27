<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="backUrl"
       value="${param.from == 'action' ? ctx.concat('/examiner/action') : ctx.concat('/examiner/candidates')}"
       scope="request" />

<%--detail urls --%>
<c:if test="${not empty candidate}">
    <c:set var="pageUrl"
           value="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}"
           scope="request" />
    <c:set var="paperUrl"
           value="${ctx}/examiner/candidate-paper?sbd=${candidate.candidateNumber}"
           scope="request" />
    <c:set var="resultUrl"
           value="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}"
           scope="request" />
</c:if>

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="candidate-detail.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <%--sidebar--%>
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="candidate-details" />
        </jsp:include>

        <%--shell--%>
        <div class="shell">

            <%--header--%>
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Chi tiết thí sinh" />
            </jsp:include>

            <%--main content--%>
            <main class="main scroll">

                <%--action message--%>
                <jsp:include page="/views/examiner/components/messages.jsp" />

                <%--toolbar--%>
                <section class="toolbar">
                    <div class="toolbar-left">
                        <a href="${backUrl}" class="back">
                            <span class="material-symbols-outlined">arrow_back</span>Quay lại
                        </a>
                        <c:if test="${examinerSectionTheory}">
                            <c:choose>
                                <%--case 1: paper available--%>
                                <c:when test="${not empty candidate and (candidate.status == 'awaiting' or candidate.status == 'done')}">
                                    <a href="${paperUrl}" class="btn white">
                                        <span class="material-symbols-outlined">visibility</span>Xem đề thi
                                    </a>
                                </c:when>

                                <%--case 2: paper disabled--%>
                                <c:otherwise>
                                    <span class="btn white grey-out">
                                        <span class="material-symbols-outlined">visibility</span>Xem đề thi
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                    <div class="toolbar-actions">
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <%--candidate detail--%>
                <c:choose>
                    <%--case 1: missing candidate--%>
                    <c:when test="${empty candidate}">
                        <section class="card">
                            <p class="table-empty">Không tìm thấy thí sinh.</p>
                        </section>
                    </c:when>

                    <%--case 2: show detail--%>
                    <c:otherwise>
                        <div class="bento bento-form">

                            <%-- photo card--%>
                            <div class="bento-profile">
                                <div class="profile-photo">
                                    <c:choose>
                                        <c:when test="${not empty candidate.photoImageUrl}">
                                            <img src="${ctx}/examiner/candidate-photo?sbd=${candidate.candidateNumber}"
                                                 alt="Ảnh thí sinh"
                                                 class="profile-photo-img">
                                        </c:when>
                                    </c:choose>
                                </div>
                                <p class="profile-name">${candidate.fullName}</p>
                                <p class="profile-sbd">SBD: ${candidate.candidateNumber}</p>
                            </div>

                            <%--info card--%>
                            <div class="bento-detail">

                                <div class="detail-section">
                                    <span class="detail-section-icon material-symbols-outlined">person</span>
                                    <span>THÔNG TIN CÁ NHÂN</span>
                                </div>

                                <div class="fields fields-form">
                                    <div class="field">
                                        <p class="field-label">Họ và Tên</p>
                                        <p class="field-value">${candidate.fullName}</p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">SBD</p>
                                        <p class="field-value field-value-mono bold">
                                            ${candidate.candidateNumber}
                                        </p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Số căn cước</p>
                                        <p class="field-value field-value-mono">
                                            ${candidate.governmentId}
                                        </p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Ngày sinh</p>
                                        <p class="field-value field-value-mono">${candidate.dob}</p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Giới tính</p>
                                        <p class="field-value">${candidate.sexLabel}</p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Số điện thoại</p>
                                        <p class="field-value field-value-mono">
                                            ${empty candidate.phoneNo ? '' : candidate.phoneNo}
                                        </p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Email</p>
                                        <p class="field-value">
                                            ${empty candidate.email ? '' : candidate.email}
                                        </p>
                                    </div>

                                    <div class="field">
                                        <p class="field-label">Hạng thi</p>
                                        <p class="field-value">
                                            ${empty candidate.licenceClass ? '' : candidate.licenceClass}
                                        </p>
                                    </div>

                                    <div class="field field-full">
                                        <p class="field-label">Địa chỉ</p>
                                        <p class="field-value">
                                            ${empty candidate.address ? '' : candidate.address}
                                        </p>
                                    </div>

                                    <c:if test="${not empty candidate.reasonForTaking}">
                                        <div class="field field-full">
                                            <p class="field-label">Lý do thi</p>
                                            <p class="field-value">${candidate.reasonForTaking}</p>
                                        </div>
                                    </c:if>

                                    <div class="form-actions">
                                        <a href="${backUrl}" class="btn white">Quay lại</a>
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
