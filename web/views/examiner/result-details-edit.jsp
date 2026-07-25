<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="backUrl" value="${ctx}/examiner/action" scope="request" />
<c:set var="pageUrl" value="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}" scope="request" />
<c:set var="currentScore" value="${requestScope.currentScore}" />
<c:set var="selectedReason" value="${formReason}" />

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

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />
            <main class="examiner-main examiner-main--scroll">
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="examiner-toolbar" />
                    <jsp:param name="leftClass" value="exr-toolbar-left" />
                    <jsp:param name="rightClass" value="examiner-toolbar__actions" />
                    <jsp:param name="backClass" value="exr-back" />
                    <jsp:param name="btnBack" value="left" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <c:choose>
                    <c:when test="${empty candidate}">
                        <section class="exr-card">
                            <p>Không tìm thấy thí sinh để sửa kết quả.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
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
                                                            <span class="exr-score-number" id="oldScoreDisplay">
                                                                <fmt:formatNumber value="${currentScore}" pattern="#"/>
                                                            </span>
                                                            <span class="exr-score-max">/ 100</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <div class="exr-score-panel">
                                                <p class="exr-score-panel__label">ĐIỂM MỚI</p>
                                                <div class="exr-score-panel__value">
                                                    <span class="exr-score-number" id="previewNewScore">
                                                        <fmt:formatNumber value="${empty currentScore ? 100 : currentScore}" pattern="#"/>
                                                    </span>
                                                    <span class="exr-score-max">/ 100</span>
                                                </div>
                                                <p class="exr-confirm-note">Điểm mới cập nhật khi +/- lỗi bên dưới.</p>
                                            </div>
                                        </div>
                                    </section>

                                    <jsp:include page="/views/examiner/components/faults.jsp">
                                        <jsp:param name="deferredAdjust" value="true" />
                                    </jsp:include>
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
                    </c:otherwise>
                </c:choose>
            </main>
        </div>

        <script>
            (function () {
                var form = document.getElementById('scoreEditForm');
                var preview = document.getElementById('previewNewScore');
                if (!form || !preview) {
                    return;
                }
                var counts = new Map();

                function ensureHiddenInput(id) {
                    var input = form.querySelector('input[name="deduction_' + id + '"]');
                    if (!input) {
                        input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = 'deduction_' + id;
                        form.appendChild(input);
                    }
                    return input;
                }

                function render() {
                    var score = 100;
                    var failed = false;
                    document.querySelectorAll('tr[data-deduction-id]').forEach(function (row) {
                        var id = Number(row.dataset.deductionId);
                        if (!counts.has(id)) {
                            counts.set(id, Number(row.dataset.baseCount || 0));
                        }
                        var count = counts.get(id);
                        var label = row.querySelector('.js-deduction-count');
                        if (label) {
                            label.textContent = count || '';
                        }
                        var timeLabel = row.querySelector('.js-deduction-time');
                        if (timeLabel && count === 0) {
                            timeLabel.textContent = '';
                        }
                        if (row.dataset.critical === 'true' && count > 0) {
                            failed = true;
                        }
                        score -= Number(row.dataset.points || 0) * count;
                        ensureHiddenInput(id).value = count;
                    });
                    preview.textContent = failed ? '0' : String(Math.max(0, score));
                }

                document.querySelectorAll('.js-deduction-adjust').forEach(function (button) {
                    button.addEventListener('click', function () {
                        var id = Number(button.dataset.deductionId);
                        var previous = counts.get(id) || 0;
                        var next = Math.max(0, previous + Number(button.dataset.delta));
                        counts.set(id, next);
                        var row = button.closest('tr[data-deduction-id]');
                        var timeLabel = row ? row.querySelector('.js-deduction-time') : null;
                        if (timeLabel) {
                            if (next > 0 && previous === 0) {
                                timeLabel.textContent = new Date().toLocaleTimeString('vi-VN', {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                    second: '2-digit',
                                    hour12: false
                                });
                            } else if (next === 0) {
                                timeLabel.textContent = '';
                            }
                        }
                        render();
                    });
                });

                render();
            })();
        </script>
    </body>
</html>
