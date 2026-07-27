<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--context / page urls--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/score-entry" scope="request" />
<c:set var="actionUrl" value="${ctx}/examiner/action" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="score-entry.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>

        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Nhập điểm" />
            </jsp:include>
            <main class="main score">
                <jsp:include page="/views/examiner/components/messages.jsp" />

                <div class="page-head">
                    <a href="${actionUrl}" class="btn white">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Quay lại thao tác
                    </a>
                </div>

                <c:choose>
                    <%--case 1: no candidate selected--%>
                    <c:when test="${empty candidate}">
                        <section class="card">
                            <div class="card-head">
                                <h2>Chưa chọn thí sinh</h2>
                            </div>
                            <p class="selected-label">
                                Vui lòng quay lại trang Thao tác và chọn một thí sinh để nhập điểm.
                            </p>
                        </section>
                    </c:when>

                    <%--case 2: score entry workspace--%>
                    <c:otherwise>
                        <div id="scoreEntryWorkspace"
                             data-draft-key="exam-score:${activeExamId}:LAYOUT:${examAreaId}:${candidate.candidateNumber}">
                            <form method="post"
                                  action="${pageUrl}"
                                  id="practicalScoreForm"
                                  class="hidden-form">
                                <input type="hidden" name="action" value="savePracticalScore">
                                <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                <input type="hidden" name="submissionToken" value="${scoreSubmissionToken}">
                                <input type="hidden" name="elapsedSeconds" id="elapsedSeconds" value="0">
                            </form>

                            <div class="grid">
                                <div class="col col-main">
                                    <section class="cand-meta">
                                        <div><span class="cand-meta-k">SBD</span>${candidate.candidateNumber}</div>
                                        <div><span class="cand-meta-k">Họ tên</span>${candidate.fullName}</div>
                                        <div><span class="cand-meta-k">CCCD</span>${candidate.governmentId}</div>
                                    </section>

                                    <%--default timer minutes--%>
                                    <c:set var="timerMinutes"
                                           value="${empty defaultTimerMinutes ? 20 : defaultTimerMinutes}" />
                                    <section class="timer-card">
                                        <div class="timer">
                                            <p class="timer-label">THỜI GIAN THI</p>
                                            <p class="timer-value" id="examTimer">00:00:00</p>
                                            <div class="timer-setup">
                                                <label class="timer-input-label" for="timerMinutesInput">
                                                    Phút
                                                </label>
                                                <input type="number"
                                                       id="timerMinutesInput"
                                                       class="timer-input"
                                                       min="1"
                                                       max="120"
                                                       step="1"
                                                       value="${timerMinutes}"
                                                       data-default-minutes="${timerMinutes}">
                                                <div class="timer-presets"
                                                     role="group"
                                                     aria-label="Thời gian theo hạng">
                                                    <button type="button"
                                                            class="timer-preset"
                                                            data-minutes="10"
                                                            title="A1, A">10p</button>
                                                    <button type="button"
                                                            class="timer-preset"
                                                            data-minutes="18"
                                                            title="B1, B">18p</button>
                                                    <button type="button"
                                                            class="timer-preset"
                                                            data-minutes="15"
                                                            title="D1, D2">15p</button>
                                                    <button type="button"
                                                            class="timer-preset"
                                                            data-minutes="20"
                                                            title="C1, C, D">20p</button>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="timer-actions">
                                            <button type="button"
                                                    class="btn green timer-btn"
                                                    id="timerStartBtn">
                                                <span class="material-symbols-outlined">timer</span>
                                                Bắt đầu / Tạm dừng
                                            </button>
                                            <button type="button"
                                                    class="btn white timer-btn"
                                                    id="timerResetBtn">
                                                <span class="material-symbols-outlined">restart_alt</span>
                                                Đặt lại
                                            </button>
                                        </div>
                                    </section>

                                    <section class="score-card">
                                        <h3 class="score-card-title">Điểm tạm tính</h3>
                                        <div class="score-card-body">
                                            <div class="score-display">
                                                <span class="score-value" id="currentScore">
                                                    <c:choose>
                                                        <%--case 1: disqualified--%>
                                                        <c:when test="${scoreDisqualified}">0</c:when>

                                                        <%--case 2: live score--%>
                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${empty currentScore ? 100 : currentScore}"
                                                                              pattern="#"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="score-max" id="scoreMaxLabel">/ 100</span>
                                            </div>
                                            <button type="submit"
                                                    form="practicalScoreForm"
                                                    class="btn blue save-btn">
                                                <span class="material-symbols-outlined">save</span>
                                                Lưu điểm
                                            </button>
                                        </div>
                                    </section>
                                </div>

                                <aside class="col col-side">
                                    <section class="card action-panel">
                                        <div class="card-head">
                                            <div class="card-title">
                                                <span class="material-symbols-outlined">rule_settings</span>
                                                <h2>Thao tác hồ sơ</h2>
                                            </div>
                                        </div>
                                        <div class="form-body">
                                            <label class="form-label" for="deviceId">Chọn xe thi</label>
                                            <select id="deviceId"
                                                    name="deviceId"
                                                    form="practicalScoreForm"
                                                    class="select"
                                                    required>
                                                <option value="">-- Chọn xe --</option>
                                                <c:forEach var="vehicle" items="${examVehicles}">
                                                    <option value="${vehicle.id}"
                                                            ${candidateVehicleId == vehicle.id ? 'selected' : ''}>
                                                        ${vehicle.name}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="flow-actions stacked">
                                            <a href="${ctx}/examiner/violations?sbd=${candidate.candidateNumber}&amp;mode=create&amp;from=score-entry"
                                               class="btn red">
                                                <span class="material-symbols-outlined">gavel</span>
                                                Đình chỉ
                                            </a>
                                            <c:if test="${candidate.awaitingSignature}">
                                                <form method="post"
                                                      action="${pageUrl}"
                                                      target="examinerPrintTab"
                                                      onsubmit="window.open('', 'examinerPrintTab'); setTimeout(function () { window.location.reload(); }, 800);">
                                                    <input type="hidden" name="action" value="printResult">
                                                    <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                                    <button type="submit" class="btn white">
                                                        <span class="material-symbols-outlined">print</span>
                                                        In biên bản
                                                    </button>
                                                </form>
                                            </c:if>

                                            <c:if test="${candidate.completeEligible}">
                                                <form method="post" action="${pageUrl}">
                                                    <input type="hidden" name="action" value="completeSection">
                                                    <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                                    <button type="submit" class="btn green">
                                                        <span class="material-symbols-outlined">done_all</span>
                                                        Hoàn thành
                                                    </button>
                                                </form>
                                            </c:if>
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
