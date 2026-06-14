<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="headerTitle" value="Nhập điểm" />

<c:set var="pageUrl" value="${ctx}/views/examiner/score-entry" />

<c:set var="callUrl" value="${ctx}/views/examiner/candidate-call" />

<c:set var="confirmUrl" value="${ctx}/views/examiner/confirmation" />

<c:set var="exportResultsUrl" value="${ctx}/examiner/export/results" />

<c:set var="exportResultsXmlUrl" value="${ctx}/examiner/export/results/xml" />

<c:set var="baseScore" value="100" />



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
        <jsp:include page="/views/examiner/partials/examiner-styles.jsp">
            <jsp:param name="pageCss" value="score-entry.css" />
        </jsp:include>

    </head>

    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">



        <jsp:include page="/views/layout/sidebar-examiner.jsp">

            <jsp:param name="activeSidebar" value="nhap-diem" />

        </jsp:include>



        <div class="examiner-shell">

            <jsp:include page="/views/layout/header-examiner.jsp" />



            <main class="examiner-main examiner-main--scroll">

                <jsp:include page="/views/examiner/partials/examiner-messages.jsp" />

                <section class="score-entry-toolbar">

                    <div class="score-entry-toolbar__left">

                        <select class="score-entry-select" aria-label="Chọn xe">

                            <option value="">Chọn xe...</option>

                            <option value="xe-01">Xe số 01</option>

                            <option value="xe-02">Xe số 02</option>

                            <option value="xe-03">Xe tự động 01</option>

                        </select>

                        <button type="button" class="examiner-btn examiner-btn--success">Thay xe</button>

                        <c:choose>

                            <c:when test="${not empty candidate}">

                                <a href="${pageUrl}?action=deferAbsent&amp;sbd=${candidate.sbd}" class="examiner-btn examiner-btn--danger">Vắng</a>

                            </c:when>

                            <c:otherwise>

                                <button type="button" class="examiner-btn examiner-btn--danger" disabled>Vắng</button>

                            </c:otherwise>

                        </c:choose>

                    </div>

                    <div class="score-entry-toolbar__right">

                        <a href="${ctx}/views/examiner/print-documents" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>
                            In kết quả thi
                        </a>

                        <a href="${exportResultsUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất Excel
                        </a>

                        <a href="${exportResultsXmlUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất XML
                        </a>

                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">

                            <span class="material-symbols-outlined">refresh</span>

                        </a>

                    </div>

                </section>

                <div class="score-entry-grid">

                    <div class="score-entry-col score-entry-col--main">

                        <section class="score-entry-card">

                            <div class="score-entry-card__head">

                                <div class="score-entry-card__title">

                                    <span class="material-symbols-outlined">groups</span>

                                    <h2>Danh sách thí sinh</h2>

                                </div>

                                <span class="score-entry-badge score-entry-badge--pending">Tổng: ${scoreQueueTotal} thí sinh</span>

                            </div>

                            <div class="score-entry-table-wrap">

                                <table class="score-entry-table score-entry-table--queue">

                                    <thead>

                                        <tr>

                                            <th>SBD</th>

                                            <th>HỌ VÀ TÊN</th>

                                            <th>CĂN CƯỚC</th>

                                            <th>GỌI</th>

                                        </tr>

                                    </thead>

                                    <tbody>

                                        <c:choose>

                                            <c:when test="${empty scoreQueue}">

                                                <tr>

                                                    <td colspan="4" class="score-entry-table__empty">Chưa có thí sinh trong hàng đợi nhập điểm.</td>

                                                </tr>

                                            </c:when>

                                            <c:otherwise>

                                                <c:forEach var="q" items="${scoreQueue}">

                                                    <tr class="score-entry-queue-row${q.active ? ' score-entry-queue-row--active' : ''}${q.called ? ' score-entry-queue-row--called' : ''}">

                                                        <td class="score-entry-table__sbd">

                                                            <a href="${pageUrl}?sbd=${q.sbd}" class="score-entry-queue-link">${q.sbd}</a>

                                                        </td>

                                                        <td>

                                                            <a href="${pageUrl}?sbd=${q.sbd}" class="score-entry-queue-link">${q.fullName}</a>

                                                        </td>

                                                        <td class="score-entry-table__mono">${q.governmentId}</td>

                                                        <td>

                                                            <a href="${pageUrl}?action=call&amp;sbd=${q.sbd}" class="examiner-btn examiner-btn--primary examiner-btn--sm">Gọi</a>

                                                        </td>

                                                    </tr>

                                                </c:forEach>

                                            </c:otherwise>

                                        </c:choose>

                                    </tbody>

                                </table>

                            </div>

                        </section>



                        <section class="score-entry-timer-card">

                            <c:if test="${not empty candidate}">

                                <p class="score-entry-selected-label">Đang nhập điểm: <strong>${candidate.sbd}</strong> — ${candidate.fullName}</p>

                            </c:if>

                            <div class="score-entry-timer">

                                <p class="score-entry-timer__label">THỜI GIAN THI</p>

                                <p class="score-entry-timer__value" id="examTimer">20:00:00</p>

                            </div>

                            <button type="button" class="examiner-btn examiner-btn--success" id="timerStartBtn" style="min-width:200px;height:60px;font-size:14px;">

                                <span class="material-symbols-outlined">play_arrow</span>

                                Bắt đầu

                            </button>

                        </section>



                        <section class="score-entry-score-card">

                            <h3 class="score-entry-score-card__title">Điểm số hiện tại</h3>

                            <div class="score-entry-score-display">

                                <span class="score-entry-score-value" id="currentScore">${baseScore}</span>

                                <span class="score-entry-score-max" id="scoreMaxLabel">/ ${baseScore}</span>

                            </div>

                            <p class="score-entry-score-hint">Điểm tự động trừ khi chọn lỗi bên phải.</p>

                        </section>

                    </div>



                    <aside class="score-entry-col score-entry-col--penalties">

                        <section class="score-entry-card score-entry-card--penalties">

                            <div class="score-entry-card__head">

                                <div class="score-entry-card__title">

                                    <span class="material-symbols-outlined">warning</span>

                                    <h2>Bảng Lỗi Trừ Điểm</h2>

                                </div>

                                <button type="button" class="score-entry-help" title="Hướng dẫn">

                                    <span class="material-symbols-outlined">help</span>

                                </button>

                            </div>

                            <div class="score-entry-penalty-wrap">

                                <table class="score-entry-penalty-table">

                                    <thead>

                                        <tr>

                                            <th>CHI TIẾT LỖI</th>

                                            <th>TRỪ</th>

                                            <th>CHỌN</th>

                                        </tr>

                                    </thead>

                                    <tbody>

                                        <c:choose>

                                            <c:when test="${empty scoreDeductions}">

                                                <tr>

                                                    <td colspan="3" class="score-entry-table__empty">Chưa có dữ liệu lỗi trừ điểm.</td>

                                                </tr>

                                            </c:when>

                                            <c:otherwise>

                                                <c:forEach var="deduction" items="${scoreDeductions}">

                                                    <tr class="${deduction.critical ? 'score-entry-penalty-row--critical' : ''}">

                                                        <td>

                                                            <span class="score-entry-penalty-reason">${deduction.reason}</span>

                                                            <c:if test="${deduction.critical}">

                                                                <span class="score-entry-penalty-tag score-entry-penalty-tag--direct">LOẠI</span>

                                                            </c:if>

                                                        </td>

                                                        <td class="score-entry-penalty-points">

                                                            <c:choose>

                                                                <c:when test="${deduction.critical}">LOẠI</c:when>

                                                                <c:otherwise>-${deduction.points}</c:otherwise>

                                                            </c:choose>

                                                        </td>

                                                        <td class="score-entry-penalty-check">

                                                            <input type="checkbox"

                                                                   class="score-entry-check score-penalty-check"

                                                                   data-points="${deduction.critical ? 0 : deduction.points}"

                                                                   data-critical="${deduction.critical}">

                                                        </td>

                                                    </tr>

                                                </c:forEach>

                                            </c:otherwise>

                                        </c:choose>

                                    </tbody>

                                </table>

                            </div>

                            <div class="score-entry-penalty-footer">

                                <button type="button" class="score-entry-clear" id="clearPenalties">Xóa tất cả chọn</button>

                            </div>

                        </section>

                    </aside>

                </div>

            </main>

        </div>



        <script>

            (function () {

                var baseScore = ${baseScore};

                var timerEl = document.getElementById('examTimer');

                var startBtn = document.getElementById('timerStartBtn');

                var clearBtn = document.getElementById('clearPenalties');

                var scoreEl = document.getElementById('currentScore');

                var scoreMaxLabel = document.getElementById('scoreMaxLabel');

                var penaltyChecks = document.querySelectorAll('.score-penalty-check');

                var remaining = 20 * 60;

                var intervalId = null;



                function formatTime(seconds) {

                    var h = Math.floor(seconds / 3600);

                    var m = Math.floor((seconds % 3600) / 60);

                    var s = seconds % 60;

                    return String(h).padStart(2, '0') + ':' +

                            String(m).padStart(2, '0') + ':' +

                            String(s).padStart(2, '0');

                }



                function recalcScore() {

                    if (!scoreEl) {

                        return;

                    }

                    var disqualified = false;

                    var totalDeduct = 0;

                    penaltyChecks.forEach(function (cb) {

                        if (!cb.checked) {

                            return;

                        }

                        if (cb.dataset.critical === 'true') {

                            disqualified = true;

                        } else {

                            totalDeduct += parseFloat(cb.dataset.points || '0');

                        }

                    });

                    scoreEl.classList.remove('score-entry-score-value--fail');

                    if (disqualified) {

                        scoreEl.textContent = 'LOẠI';

                        scoreEl.classList.add('score-entry-score-value--fail');

                        if (scoreMaxLabel) {

                            scoreMaxLabel.style.visibility = 'hidden';

                        }

                        return;

                    }

                    if (scoreMaxLabel) {

                        scoreMaxLabel.style.visibility = 'visible';

                    }

                    var score = Math.max(0, baseScore - totalDeduct);

                    scoreEl.textContent = Number.isInteger(score) ? String(score) : score.toFixed(1);

                }



                penaltyChecks.forEach(function (cb) {

                    cb.addEventListener('change', recalcScore);

                });



                if (timerEl) {

                    timerEl.textContent = formatTime(remaining);

                }



                if (startBtn) {

                    startBtn.addEventListener('click', function () {

                        if (intervalId) {

                            clearInterval(intervalId);

                            intervalId = null;

                            startBtn.innerHTML = '<span class="material-symbols-outlined">play_arrow</span>Bắt đầu';

                            return;

                        }

                        intervalId = setInterval(function () {

                            if (remaining <= 0) {

                                clearInterval(intervalId);

                                intervalId = null;

                                return;

                            }

                            remaining -= 1;

                            timerEl.textContent = formatTime(remaining);

                        }, 1000);

                        startBtn.innerHTML = '<span class="material-symbols-outlined">pause</span>Tạm dừng';

                    });

                }



                if (clearBtn) {

                    clearBtn.addEventListener('click', function () {

                        penaltyChecks.forEach(function (cb) {

                            cb.checked = false;

                        });

                        recalcScore();

                    });

                }



                recalcScore();

            })();

        </script>

    </body>

</html>


