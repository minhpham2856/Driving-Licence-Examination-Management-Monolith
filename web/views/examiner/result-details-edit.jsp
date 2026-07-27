<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--context / back / page urls--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="backUrl" value="${ctx}/examiner/action" scope="request" />
<c:set var="pageUrl"
       value="${ctx}/examiner/result-details-edit?sbd=${candidate.candidateNumber}"
       scope="request" />

<%--score / form reason--%>
<c:set var="currentScore" value="${requestScope.currentScore}" />
<c:set var="selectedReason" value="${formReason}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="result-edit.css,score-entry.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>

        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Sửa kết quả" />
            </jsp:include>
            <main class="main scroll">
                <jsp:include page="/views/examiner/components/messages.jsp" />

                <section class="toolbar">
                    <div class="toolbar-left">
                        <a href="${backUrl}" class="back">
                            <span class="material-symbols-outlined">arrow_back</span>Quay lại
                        </a>
                    </div>
                    <div class="toolbar-actions">
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <c:choose>
                    <%--case 1: missing candidate--%>
                    <c:when test="${empty candidate}">
                        <section class="card">
                            <p>Không tìm thấy thí sinh để sửa kết quả.</p>
                        </section>
                    </c:when>

                    <%--case 2: edit form--%>
                    <c:otherwise>
                        <form id="scoreEditForm"
                              action="${ctx}/examiner/result-details-edit"
                              method="post">
                            <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                            <div class="edit-grid card-mt">
                                <div class="col-left">
                                    <section class="card">
                                        <div class="section-title">
                                            <span class="material-symbols-outlined">badge</span>
                                            <span>THÔNG TIN THÍ SINH</span>
                                        </div>
                                        <p class="candidate-line">
                                            ${candidate.fullName} - ${candidate.governmentId} - SBD: ${candidate.candidateNumber}
                                        </p>
                                    </section>

                                    <section class="card">
                                        <div class="section-title">
                                            <span class="material-symbols-outlined">calculate</span>
                                            <span>ĐIỀU CHỈNH ĐIỂM</span>
                                        </div>
                                        <div class="score-compare">
                                            <div class="score-panel">
                                                <p class="score-panel-label">ĐIỂM HIỆN TẠI</p>
                                                <div class="score-panel-value">
                                                    <c:choose>
                                                        <%--case 1: disqualified--%>
                                                        <c:when test="${scoreDisqualified}">
                                                            <span class="score-number score-number-fail">TRƯỢT</span>
                                                        </c:when>

                                                        <%--case 2: numeric score--%>
                                                        <c:otherwise>
                                                            <span class="score-number" id="oldScoreDisplay">
                                                                <fmt:formatNumber value="${currentScore}" pattern="#"/>
                                                            </span>
                                                            <span class="score-max">/ 100</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <div class="score-panel">
                                                <p class="score-panel-label">ĐIỂM MỚI</p>
                                                <div class="score-panel-value">
                                                    <span class="score-number" id="previewNewScore">
                                                        <fmt:formatNumber value="${empty currentScore ? 100 : currentScore}"
                                                                          pattern="#"/>
                                                    </span>
                                                    <span class="score-max">/ 100</span>
                                                </div>
                                                <p class="confirm-note">Điểm mới cập nhật khi +/- lỗi bên dưới.</p>
                                            </div>
                                        </div>
                                    </section>

                                    <jsp:include page="/views/examiner/components/faults.jsp">
                                        <jsp:param name="deferredAdjust" value="true" />
                                    </jsp:include>
                                </div>

                                <aside class="col-right">
                                    <section class="card">
                                        <div class="section-title">
                                            <span class="material-symbols-outlined">verified_user</span>
                                            <span>XÁC NHẬN CHỈNH SỬA</span>
                                        </div>
                                        <div class="control">
                                            <label class="input-label" for="reason">
                                                CHỌN LÝ DO <span class="req">*</span>
                                            </label>
                                            <select id="reason" name="reasonCode" class="select" required>
                                                <option value="">-- Lựa chọn lý do quy định --</option>
                                                <option value="cham-sai"
                                                        ${selectedReason eq 'cham-sai' ? 'selected' : ''}>
                                                    Chấm sai
                                                </option>
                                                <option value="nhap-nham"
                                                        ${selectedReason eq 'nhap-nham' ? 'selected' : ''}>
                                                    Nhập nhầm điểm
                                                </option>
                                                <option value="khieu-nai"
                                                        ${selectedReason eq 'khieu-nai' ? 'selected' : ''}>
                                                    Thí sinh khiếu nại
                                                </option>
                                                <option value="khac"
                                                        ${selectedReason eq 'khac' ? 'selected' : ''}>
                                                    Lý do khác
                                                </option>
                                            </select>
                                        </div>
                                        <div class="control">
                                            <label class="input-label" for="reasonDetail">
                                                LÝ DO CHI TIẾT (tùy chọn)
                                            </label>
                                            <textarea id="reasonDetail"
                                                      name="reasonDetail"
                                                      class="textarea"
                                                      placeholder="Nhập mô tả chi tiết nguyên nhân dẫn đến việc thay đổi điểm số...">${formReasonDetail}</textarea>
                                        </div>
                                        <div class="control">
                                            <label class="input-label" for="pwd">
                                                MẬT KHẨU XÁC THỰC BẢO MẬT <span class="req">*</span>
                                            </label>
                                            <input type="password"
                                                   id="pwd"
                                                   name="confirmPassword"
                                                   class="input"
                                                   placeholder="Nhập mật khẩu của bạn"
                                                   required
                                                   autocomplete="current-password">
                                        </div>
                                        <div class="confirm-wrap">
                                            <button type="submit"
                                                    class="btn blue confirm-btn-full">
                                                <span class="material-symbols-outlined">task_alt</span>
                                                XÁC NHẬN THAY ĐỔI ĐIỂM
                                            </button>
                                            <p class="confirm-note">
                                                Mọi thay đổi điểm sẽ được ghi nhận vào lịch sử kiểm tra.
                                            </p>
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
                        render();
                    });
                });

                render();
            })();
        </script>
    </body>
</html>
