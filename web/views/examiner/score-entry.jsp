<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Nhập điểm" />
<c:set var="pageUrl" value="${ctx}/examiner/score-entry" scope="request" />
<c:set var="actionUrl" value="${ctx}/examiner/action" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Sát hạch</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <jsp:include page="/views/examiner/components/examiner-styles.jsp">
            <jsp:param name="pageCss" value="score-entry.css,devices.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />
            <main class="examiner-main examiner-main--score-entry">
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <div class="score-entry-page-head">
                    <a href="${actionUrl}" class="examiner-btn examiner-btn--white">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Quay lại thao tác
                    </a>
                </div>

                <c:choose>
                    <c:when test="${empty candidate}">
                        <section class="examiner-card">
                            <div class="examiner-card__head">
                                <h2>Chưa chọn thí sinh</h2>
                            </div>
                            <p class="score-entry-selected-label">Vui lòng quay lại trang Thao tác và chọn một thí sinh để nhập điểm.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <div id="scoreEntryWorkspace"
                             data-draft-key="exam-score:${activeExamId}:LAYOUT:${examAreaId}:${candidate.candidateNumber}">
                            <form method="post" action="${pageUrl}" id="practicalScoreForm" class="score-entry-hidden-form">
                                <input type="hidden" name="action" value="savePracticalScore">
                                <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                <input type="hidden" name="submissionToken" value="${scoreSubmissionToken}">
                                <input type="hidden" name="elapsedSeconds" id="elapsedSeconds" value="0">
                            </form>

                            <div class="score-entry-grid">
                                <div class="score-entry-col score-entry-col--main">
                                    <section class="score-entry-card score-entry-candidate-card">
                                        <div class="score-entry-card__head">
                                            <div class="score-entry-card__title">
                                                <span class="material-symbols-outlined">badge</span>
                                                <h2>Thí sinh</h2>
                                            </div>
                                        </div>
                                        <div class="score-entry-candidate-card__body">
                                            <table class="score-entry-candidate-table">
                                                <thead>
                                                <tr>
                                                    <th>SBD</th>
                                                    <th>Tên</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td>${candidate.candidateNumber}</td>
                                                    <td>${candidate.fullName}</td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </section>

                                    <c:set var="timerMinutes" value="${empty defaultTimerMinutes ? 20 : defaultTimerMinutes}" />
                                    <section class="score-entry-timer-card">
                                        <div class="score-entry-timer">
                                            <p class="score-entry-timer__label">THỜI GIAN THI</p>
                                            <p class="score-entry-timer__value" id="examTimer">00:00:00</p>
                                            <div class="score-entry-timer__setup">
                                                <label class="score-entry-timer__input-label" for="timerMinutesInput">Phút</label>
                                                <input type="number" id="timerMinutesInput" class="score-entry-timer__input"
                                                       min="1" max="120" step="1" value="${timerMinutes}"
                                                       data-default-minutes="${timerMinutes}">
                                                <div class="score-entry-timer__presets" role="group" aria-label="Thời gian theo hạng">
                                                    <button type="button" class="score-entry-timer__preset" data-minutes="10" title="A1, A">10p</button>
                                                    <button type="button" class="score-entry-timer__preset" data-minutes="18" title="B1, B">18p</button>
                                                    <button type="button" class="score-entry-timer__preset" data-minutes="15" title="D1, D2">15p</button>
                                                    <button type="button" class="score-entry-timer__preset" data-minutes="20" title="C1, C, D">20p</button>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="score-entry-timer__actions">
                                            <button type="button" class="examiner-btn examiner-btn--success score-entry-timer__btn" id="timerStartBtn">
                                                <span class="material-symbols-outlined">timer</span>
                                                Bắt đầu / Tạm dừng
                                            </button>
                                            <button type="button" class="examiner-btn examiner-btn--white score-entry-timer__btn" id="timerResetBtn">
                                                <span class="material-symbols-outlined">restart_alt</span>
                                                Đặt lại
                                            </button>
                                        </div>
                                    </section>

                                    <section class="score-entry-score-card">
                                        <h3 class="score-entry-score-card__title">Điểm tạm tính</h3>
                                        <div class="score-entry-score-card__body">
                                            <div class="score-entry-score-display">
                                                <span class="score-entry-score-value" id="currentScore">
                                                    <c:choose>
                                                        <c:when test="${scoreDisqualified}">0</c:when>
                                                        <c:otherwise><fmt:formatNumber value="${empty currentScore ? 100 : currentScore}" pattern="#"/></c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="score-entry-score-max" id="scoreMaxLabel">/ 100</span>
                                            </div>
                                            <button type="submit" form="practicalScoreForm" class="examiner-btn examiner-btn--primary score-entry-save-button">
                                                <span class="material-symbols-outlined">save</span>
                                                Nhập điểm
                                            </button>
                                        </div>
                                    </section>
                                </div>

                                <aside class="score-entry-col score-entry-col--side">
                                    <section class="score-entry-card score-entry-action-panel">
                                        <div class="score-entry-card__head">
                                            <div class="score-entry-card__title">
                                                <span class="material-symbols-outlined">rule_settings</span>
                                                <h2>Thao tác hồ sơ</h2>
                                            </div>
                                        </div>
                                        <div class="score-entry-form-body">
                                            <label class="score-entry-form-label" for="deviceId">Chọn xe thi</label>
                                            <select id="deviceId" name="deviceId" form="practicalScoreForm" class="score-entry-select" required>
                                                <option value="">-- Chọn xe --</option>
                                                <c:forEach var="vehicle" items="${examVehicles}">
                                                    <option value="${vehicle.id}" ${candidateVehicleId == vehicle.id ? 'selected' : ''}>${vehicle.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="score-entry-flow-actions score-entry-flow-actions--stacked">
                                            <a href="${ctx}/examiner/violations?sbd=${candidate.candidateNumber}&amp;mode=create&amp;from=score-entry"
                                               class="examiner-btn examiner-btn--danger">
                                                <span class="material-symbols-outlined">gavel</span>
                                                Đình chỉ
                                            </a>
                                            <c:choose>
                                                <c:when test="${candidate.awaitingSignature}">
                                                    <form method="post" action="${pageUrl}" target="examinerPrintTab"
                                                          onsubmit="window.open('', 'examinerPrintTab'); setTimeout(function () { window.location.reload(); }, 800);">
                                                        <input type="hidden" name="action" value="printResult">
                                                        <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                                        <button type="submit" class="examiner-btn examiner-btn--white">
                                                            <span class="material-symbols-outlined">print</span>
                                                            In biên bản
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="examiner-btn examiner-btn--disabled">In biên bản</span>
                                                </c:otherwise>
                                            </c:choose>

                                            <c:choose>
                                                <c:when test="${candidate.completeEligible}">
                                                    <form method="post" action="${pageUrl}">
                                                        <input type="hidden" name="action" value="completeSection">
                                                        <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                                        <button type="submit" class="examiner-btn examiner-btn--success">
                                                            <span class="material-symbols-outlined">done_all</span>
                                                            Hoàn tất
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="examiner-btn examiner-btn--disabled">Hoàn tất</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </section>

                                    <jsp:include page="/views/examiner/components/faults.jsp">
                                        <jsp:param name="deferredAdjust" value="true" />
                                    </jsp:include>
                                </aside>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
        <script src="${ctx}/assets/js/examiner-action.js"></script>
    </body>
</html>
