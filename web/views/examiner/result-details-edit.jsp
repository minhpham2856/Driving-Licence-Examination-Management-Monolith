<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="backUrl" value="${ctx}/examiner/result-details" scope="request" />
<c:set var="pageUrl" value="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}" scope="request" />
<c:set var="paperUrl" value="${ctx}/examiner/candidate-paper?sbd=${candidate.candidateNumber}" scope="request" />
<c:set var="exportResultsUrl" value="${ctx}/examiner/export/result" scope="request" />
<c:set var="currentScore" value="${requestScope.currentScore}" />
<c:set var="selectedReason" value="${formReason}" />
<c:set var="displayNewScore" value="${empty formNewScore ? (scoreDisqualified ? '0' : (empty currentScore ? '0' : currentScore)) : formNewScore}" />

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
            <jsp:param name="pageCss" value="result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="result-details" />
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
                    <jsp:param name="btnPrintInfo" value="left" />
                    <jsp:param name="btnExportExcel" value="left" />
                    <jsp:param name="btnViewPaper" value="left" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <form id="scoreEditForm" action="${ctx}/examiner/result-details-edit" method="post">
                    <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                    <div class="exr-grid exr-card--mt">
                        <div class="exr-col-left">
                            <section class="exr-card">
                                <div class="exr-section-title">
                                    <span class="material-symbols-outlined">badge</span>
                                    <span>THÔNG TIN THÍ SINH</span>
                                </div>
                                <p class="exr-candidate-line">
                                    ${candidate.fullName} - ${candidate.governmentId} - SBD: ${candidate.candidateNumber}
                                </p>
                            </section>

                            <section class="exr-card">
                                <div class="exr-section-title">
                                    <span class="material-symbols-outlined">calculate</span>
                                    <span>ĐIỀU CHỈNH ĐIỂM</span>
                                </div>
                                <div class="exr-score-compare">
                                    <div class="exr-score-panel">
                                        <p class="exr-score-panel__label">ĐIỂM HIỆN TẠI</p>
                                        <div class="exr-score-panel__value">
                                            <c:choose>
                                                <c:when test="${scoreDisqualified}">
                                                    <span class="exr-score-number exr-score-number--fail">TRƯỢT</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="exr-score-number">
                                                        <fmt:formatNumber value="${currentScore}" pattern="#"/>
                                                    </span>
                                                    <span class="exr-score-max">/ 100</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="exr-score-panel">
                                        <label class="exr-score-panel__label" for="newScore">
                                            ĐIỂM MỚI <span class="exr-req">*</span>
                                        </label>
                                        <div class="exr-new-score-row">
                                            <input id="newScore" name="newScore" class="exr-input exr-input--mono exr-new-score-input"
                                                   type="number" min="0" max="100" step="5"
                                                   value="${displayNewScore}" required>
                                            <button id="setFailBtn" type="button" class="examiner-btn examiner-btn--secondary exr-fail-btn">
                                                Trượt
                                            </button>
                                        </div>
                                        <p id="newScoreError" class="exr-error-text"></p>
                                    </div>
                                </div>
                            </section>
                        </div>

                        <aside class="exr-col-right">
                            <section class="exr-card">
                                <div class="exr-section-title">
                                    <span class="material-symbols-outlined">verified_user</span>
                                    <span>XÁC NHẬN CHỈNH SỬA</span>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="reason">CHỌN LÝ DO <span class="exr-req">*</span></label>
                                    <select id="reason" name="reasonCode" class="exr-select" required>
                                        <option value="">-- Lựa chọn lý do quy định --</option>
                                        <option value="cham-sai" ${selectedReason eq 'cham-sai' ? 'selected' : ''}>Chấm sai</option>
                                        <option value="nhap-nham" ${selectedReason eq 'nhap-nham' ? 'selected' : ''}>Nhập nhầm điểm</option>
                                        <option value="khieu-nai" ${selectedReason eq 'khieu-nai' ? 'selected' : ''}>Thí sinh khiếu nại</option>
                                        <option value="khac" ${selectedReason eq 'khac' ? 'selected' : ''}>Lý do khác</option>
                                    </select>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="reasonDetail">LÝ DO CHI TIẾT (tùy chọn)</label>
                                    <textarea id="reasonDetail" name="reasonDetail" class="exr-textarea"
                                              placeholder="Nhập mô tả chi tiết nguyên nhân dẫn đến việc thay đổi điểm số...">${formReasonDetail}</textarea>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="pwd">MẬT KHẨU XÁC THỰC BẢO MẬT <span class="exr-req">*</span></label>
                                    <input type="password" id="pwd" name="confirmPassword" class="exr-input"
                                           placeholder="Nhập mật khẩu của bạn" required autocomplete="current-password">
                                </div>
                                <div class="exr-confirm-wrap">
                                    <button type="submit" class="examiner-btn examiner-btn--primary exr-confirm-btn--full">
                                        <span class="material-symbols-outlined">task_alt</span>
                                        XÁC NHẬN THAY ĐỔI ĐIỂM
                                    </button>
                                    <p class="exr-confirm-note">Mọi thay đổi điểm sẽ được ghi nhận vào lịch sử kiểm tra.</p>
                                </div>
                            </section>
                        </aside>
                    </div>
                </form>
            </main>
        </div>

        <script>
            (function () {
                var form = document.getElementById('scoreEditForm');
                var scoreInput = document.getElementById('newScore');
                var scoreError = document.getElementById('newScoreError');
                var failButton = document.getElementById('setFailBtn');
                if (!form || !scoreInput || !scoreError) {
                    return;
                }

                function showError(message) {
                    scoreError.textContent = message || '';
                    scoreInput.classList.toggle('exr-input--error', !!message);
                }

                function validateScoreInput() {
                    var value = (scoreInput.value || '').trim();
                    if (value === '') {
                        showError('Vui lòng nhập điểm mới.');
                        return false;
                    }
                    var parsed = Number(value);
                    if (!Number.isInteger(parsed)) {
                        showError('Điểm mới phải là số nguyên.');
                        return false;
                    }
                    if (parsed < 0 || parsed > 100) {
                        showError('Điểm mới phải nằm trong khoảng từ 0 đến 100.');
                        return false;
                    }
                    if (parsed % 5 !== 0) {
                        showError('Điểm mới phải chia hết cho 5.');
                        return false;
                    }
                    showError('');
                    return true;
                }

                if (failButton) {
                    failButton.addEventListener('click', function () {
                        scoreInput.value = '0';
                        validateScoreInput();
                        scoreInput.focus();
                    });
                }

                scoreInput.addEventListener('input', validateScoreInput);
                form.addEventListener('submit', function (event) {
                    if (!validateScoreInput()) {
                        event.preventDefault();
                    }
                });
            })();
        </script>
    </body>
</html>
