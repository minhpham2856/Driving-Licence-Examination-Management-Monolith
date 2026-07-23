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

                <!--edit layout: reason form + fault list (faults must not nest inside reason form)-->
                <div class="score-entry-grid">
                    <div class="score-entry-col score-entry-col--main">

                        <!-- candidate info  -->
                        <jsp:include page="/views/examiner/components/candidate-list.jsp">
                            <jsp:param name="cardClass" value="examiner-card examiner-card--dashboard-table exr-card--mt" />
                            <jsp:param name="title" value="Thông tin thí sinh" />
                            <jsp:param name="itemsAttr" value="singleCandidateList" />
                            <jsp:param name="showAddress" value="false" />
                            <jsp:param name="showTheoryScores" value="false" />
                            <jsp:param name="showPracticalScore" value="false" />
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
                        <form action="${ctx}/examiner/result-details-edit" method="post">
                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                            <input type="hidden" name="pendingAdjustments" id="pendingAdjustmentsInput" value="">
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
                                        <select id="reason" name="reasonCode" class="exr-select" required>
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
                                        <input type="password" id="pwd" name="confirmPassword" class="exr-input" placeholder="Nhập mật khẩu của bạn" required autocomplete="current-password">
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
                        </form>
                    </div>

                    <aside class="score-entry-col score-entry-col--penalties">
                        <jsp:include page="/views/examiner/components/faults.jsp">
                            <jsp:param name="deferredAdjust" value="true" />
                        </jsp:include>
                        <div class="exr-warning exr-warning--mt">
                            <span class="exr-warning__icon material-symbols-outlined">warning</span>
                            <div class="exr-warning__body">
                                <p class="exr-warning__title">CẢNH BÁO</p>
                                <p class="exr-warning__text">Mọi thao tác đều được lưu lại trong hệ thống.</p>
                            </div>
                        </div>
                    </aside>
                </div>
            </main>
        </div>

        <script>
            (function () {
                var hiddenInput = document.getElementById('pendingAdjustmentsInput');
                if (!hiddenInput) {
                    return;
                }
                var adjustButtons = document.querySelectorAll('.js-deduction-adjust');
                if (!adjustButtons.length) {
                    return;
                }

                var scoreEl = document.getElementById('currentScore');
                var scoreMaxEl = document.getElementById('scoreMaxLabel');
                var scoreCard = scoreEl ? scoreEl.parentElement : null;
                var baseScore = 0;
                if (scoreEl) {
                    var rawScore = (scoreEl.textContent || '').replace(/[^\d.-]/g, '');
                    baseScore = parseFloat(rawScore);
                    if (isNaN(baseScore)) {
                        baseScore = 0;
                    }
                }

                var rows = {};
                document.querySelectorAll('tr[data-deduction-id]').forEach(function (row) {
                    var id = parseInt(row.getAttribute('data-deduction-id'), 10);
                    var baseCount = parseInt(row.getAttribute('data-base-count'), 10);
                    var points = parseFloat(row.getAttribute('data-points'));
                    var critical = row.getAttribute('data-critical') === 'true';
                    if (isNaN(id)) {
                        return;
                    }
                    rows[id] = {
                        baseCount: isNaN(baseCount) ? 0 : baseCount,
                        currentCount: isNaN(baseCount) ? 0 : baseCount,
                        points: isNaN(points) ? 0 : points,
                        critical: critical
                    };
                });

                function updateCountDisplay(id) {
                    var cell = document.querySelector('.js-deduction-count[data-deduction-id="' + id + '"]');
                    if (!cell || !rows[id]) {
                        return;
                    }
                    var value = rows[id].currentCount;
                    cell.textContent = value > 0 ? String(value) : '';
                }

                function serializePending() {
                    var tokens = [];
                    Object.keys(rows).forEach(function (key) {
                        var id = parseInt(key, 10);
                        var delta = rows[id].currentCount - rows[id].baseCount;
                        if (delta !== 0) {
                            tokens.push(id + ':' + delta);
                        }
                    });
                    hiddenInput.value = tokens.join(',');
                }

                function refreshScorePreview() {
                    if (!scoreEl) {
                        serializePending();
                        return;
                    }
                    var hasCritical = false;
                    var deltaScore = 0;
                    Object.keys(rows).forEach(function (key) {
                        var id = parseInt(key, 10);
                        var row = rows[id];
                        var delta = row.currentCount - row.baseCount;
                        if (row.critical && row.currentCount > 0) {
                            hasCritical = true;
                        }
                        if (!row.critical && delta !== 0) {
                            deltaScore += row.points * delta;
                        }
                    });
                    var preview = Math.round(baseScore - deltaScore);
                    if (preview < 0) {
                        preview = 0;
                    }
                    if (hasCritical) {
                        scoreEl.textContent = 'TRƯỢT';
                        scoreEl.classList.add('score-entry-score-value--fail');
                        if (scoreMaxEl) {
                            scoreMaxEl.style.display = 'none';
                        }
                    } else {
                        scoreEl.textContent = String(preview);
                        scoreEl.classList.remove('score-entry-score-value--fail');
                        if (scoreMaxEl) {
                            scoreMaxEl.style.display = '';
                        }
                    }
                    if (scoreCard) {
                        scoreCard.setAttribute('data-preview-updated', '1');
                    }
                    serializePending();
                }

                adjustButtons.forEach(function (btn) {
                    btn.addEventListener('click', function () {
                        var id = parseInt(btn.getAttribute('data-deduction-id'), 10);
                        var delta = parseInt(btn.getAttribute('data-delta'), 10);
                        if (!rows[id] || isNaN(delta)) {
                            return;
                        }
                        var next = rows[id].currentCount + delta;
                        if (next < 0) {
                            next = 0;
                        }
                        rows[id].currentCount = next;
                        updateCountDisplay(id);
                        refreshScorePreview();
                    });
                });

                refreshScorePreview();
            })();
        </script>
    </body>
</html>
