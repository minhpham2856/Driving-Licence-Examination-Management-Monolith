<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="backUrl" value="${ctx}/views/examiner/result-details" scope="request" />
<c:set var="pageUrl" value="${ctx}/views/examiner/result-details-edit?sbd=${candidate.sbd}" scope="request" />
<c:set var="paperUrl" value="${ctx}/views/examiner/candidate-paper?sbd=${candidate.sbd}" scope="request" />
<c:set var="exportResultsUrl" value="${ctx}/examiner/export/results" scope="request" />
<c:set var="exportResultsXmlUrl" value="${ctx}/examiner/export/results/xml" scope="request" />
<c:set var="currentScore" value="${candidate.theoryCorrectScore}" />
<c:set var="maxScore" value="${empty theoryMaxScore ? 35 : theoryMaxScore}" />
<c:set var="inputScore" value="${not empty formNewScore ? formNewScore : (not empty currentScore ? currentScore : '')}" />
<c:set var="selectedReason" value="${formReason}" />

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
            <jsp:param name="pageCss" value="result-edit.css,score-entry.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="sua-ket-qua" />
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
                    <jsp:param name="showBack" value="true" />
                    <jsp:param name="showResultEditPrintGroup" value="true" />
                    <jsp:param name="showRefresh" value="true" />
                </jsp:include>

                <!--edit form-->
                <form action="${ctx}/views/examiner/result-details-edit" method="post">
                    <input type="hidden" name="sbd" value="${candidate.sbd}">
                    <div class="score-entry-grid">
                        <div class="score-entry-col score-entry-col--main">

                            <!-- candidate info  -->
                            <jsp:include page="/views/examiner/components/candidate-list.jsp">
                                <jsp:param name="cardClass" value="examiner-card examiner-card--dashboard-table exr-card--mt" />
                                <jsp:param name="title" value="Thông tin thí sinh" />
                                <jsp:param name="itemsAttr" value="singleCandidateList" />
                                <jsp:param name="showAddress" value="false" />
                                <jsp:param name="showTheoryScores" value="false" />
                                <jsp:param name="showExamScore" value="false" />
                                <jsp:param name="showResult" value="true" />
                                <jsp:param name="showStatus" value="true" />
                            </jsp:include>

                            <!-- Score -->
                            <section class="score-entry-score-card">
                                <h3 class="score-entry-score-card__title">Điểm hiện tại</h3>
                                <div class="score-entry-score-display">
                                    <c:choose>
                                        <c:when test="${scoreDisqualified}">
                                            <span class="score-entry-score-value score-entry-score-value--fail">TRƯỢT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-entry-score-value" id="currentScore">
                                                <fmt:formatNumber value="${currentScore}" pattern="#"/>
                                            </span>
                                            <span class="score-entry-score-max" id="scoreMaxLabel">/ 100</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </section>

                            <!-- Reason Form -->
                            <section class="score-entry-card exr-card--mt">
                                <div class="score-entry-card__head">
                                    <div class="score-entry-card__title">
                                        <span class="material-symbols-outlined">notes</span>
                                        <h2>Lý do điều chỉnh</h2>
                                    </div>
                                </div>
                                <div class="exr-card__body">
                                    <div class="exr-control">
                                        <label class="exr-input-label" for="reason">CHỌN LÝ DO <span class="exr-req">*</span></label>
                                        <select id="reason" name="reason" class="exr-select" required>
                                            <option value="">-- Lựa chọn lý do quy định --</option>
                                            <option value="cham-sai" ${selectedReason eq 'cham-sai' ? 'selected' : ''}>Chấm sai</option>
                                            <option value="khieu-nai" ${selectedReason eq 'khieu-nai' ? 'selected' : ''}>Thí sinh khiếu nại</option>
                                            <option value="khac" ${selectedReason eq 'khac' ? 'selected' : ''}>Lý do khác</option>
                                        </select>
                                    </div>
                                    <div class="exr-control">
                                        <label class="exr-input-label" for="reasonDetail">LÝ DO CHI TIẾT (tùy chọn)</label>
                                        <textarea id="reasonDetail" name="reasonDetail" class="exr-textarea" placeholder="Nhập mô tả chi tiết nguyên nhân dẫn đến việc thay đổi điểm số...">${formReasonDetail}</textarea>
                                    </div>

                                    <div class="exr-control">
                                        <label class="exr-input-label" for="pwd">MẬT KHẨU XÁC THỰC BẢO MẬT <span class="exr-req">*</span></label>
                                        <input type="password" id="pwd" name="password" class="exr-input" placeholder="Nhập mật khẩu của bạn" required autocomplete="current-password">
                                    </div>
                                    <div class="exr-confirm-wrap">
                                        <button type="submit" class="examiner-btn examiner-btn--primary score-entry-finalize-btn exr-confirm-btn--full">
                                            <span class="material-symbols-outlined">verified_user</span>
                                            XÁC NHẬN THAY ĐỔI ĐIỂM
                                        </button>
                                        <p class="exr-confirm-note exr-confirm-note--mt">Bắt buộc chọn lý do và nhập mật khẩu trước khi lưu.</p>
                                    </div>
                                </div>
                            </section>
                        </div>

                        <aside class="score-entry-col score-entry-col--penalties">
                            <!-- Fault List -->
                            <jsp:include page="/views/examiner/components/faults.jsp" />


                            <!--warning-->
                            <div class="exr-warning exr-warning--mt">
                                <span class="exr-warning__icon material-symbols-outlined">warning</span>
                                <div class="exr-warning__body">
                                    <p class="exr-warning__title">CẢNH BÁO</p>
                                    <p class="exr-warning__text">Mọi thao tác đều được lưu lại trong hệ thống.</p>
                                </div>
                            </div>
                        </aside>
                    </div>
                </form>
            </main>
        </div>

    </body>
</html>
