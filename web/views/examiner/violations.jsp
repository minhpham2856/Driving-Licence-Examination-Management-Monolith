<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context variable--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="violation.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>

        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="${param.mode eq 'view' ? 'Xem vi phạm' : 'Đình chỉ'}" />
            </jsp:include>
            <main class="main scroll">
                <jsp:include page="/views/examiner/components/messages.jsp" />

                <c:choose>
                    <%--case 1: create violation form--%>
                    <c:when test="${param.mode eq 'create' and not empty candidate}">
                        <div class="vio-page">
                            <a href="${ctx}/examiner/action" class="btn white vio-back">
                                <span class="material-symbols-outlined">arrow_back</span>
                                Quay lại
                            </a>

                            <h1 class="vio-title">Đình chỉ thí sinh</h1>
                            <p class="vio-meta">
                                SBD <strong>${candidate.candidateNumber}</strong>
                                · ${candidate.fullName}
                                <c:if test="${not empty candidate.licenceClass}"> · Hạng ${candidate.licenceClass}</c:if>
                            </p>

                            <form method="post"
                                  action="${ctx}/examiner/violations"
                                  enctype="multipart/form-data"
                                  class="vio-form">
                                <input type="hidden" name="action" value="createViolation">
                                <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                                <input type="hidden"
                                       name="from"
                                       value="${empty param.from ? 'action' : param.from}">

                                <label class="vio-label" for="reasonCode">Lý do đình chỉ</label>
                                <select id="reasonCode"
                                        name="reasonCode"
                                        class="vio-input"
                                        required>
                                    <option value="">-- Chọn lý do --</option>
                                    <c:forEach var="reason" items="${violationReasons}">
                                        <option value="${reason.code}">${reason.label}</option>
                                    </c:forEach>
                                </select>

                                <label class="vio-label" for="reasonDetail">Chi tiết vi phạm</label>
                                <textarea id="reasonDetail"
                                          name="reasonDetail"
                                          class="vio-input vio-textarea"
                                          rows="4"
                                          maxlength="2000"
                                          placeholder="Mô tả chi tiết (bắt buộc nếu chọn lý do Khác)"></textarea>

                                <label class="vio-label" for="evidenceFile">Ảnh minh chứng</label>
                                <input id="evidenceFile"
                                       type="file"
                                       name="evidenceFile"
                                       class="vio-input"
                                       accept="image/jpeg,image/png,image/webp"
                                       required>
                                <p class="vio-hint">JPEG, PNG hoặc WebP · tối đa 5 MB</p>

                                <label class="vio-label" for="confirmPassword">Mật khẩu xác nhận</label>
                                <input id="confirmPassword"
                                       type="password"
                                       name="confirmPassword"
                                       class="vio-input"
                                       placeholder="Nhập mật khẩu của bạn"
                                       required
                                       autocomplete="current-password">

                                <div class="vio-actions">
                                    <button type="submit" class="btn red">
                                        <span class="material-symbols-outlined">gavel</span>
                                        Xác nhận đình chỉ
                                    </button>
                                    <a href="${ctx}/examiner/action" class="btn white">Hủy</a>
                                </div>
                            </form>
                        </div>
                    </c:when>

                    <%--case 2: view violation--%>
                    <c:when test="${param.mode eq 'view' and not empty candidate}">
                        <div class="vio-page">
                            <a href="${ctx}/examiner/action" class="btn white vio-back">
                                <span class="material-symbols-outlined">arrow_back</span>
                                Quay lại
                            </a>

                            <h1 class="vio-title">Chi tiết vi phạm</h1>
                            <p class="vio-meta">
                                SBD <strong>${candidate.candidateNumber}</strong>
                                · ${candidate.fullName}
                                <c:if test="${not empty candidate.licenceClass}"> · Hạng ${candidate.licenceClass}</c:if>
                                · ${candidate.statusLabel}
                            </p>

                            <c:choose>
                                <c:when test="${empty violation}">
                                    <p class="vio-hint">Không tìm thấy biên bản vi phạm cho thí sinh này.</p>
                                </c:when>
                                <c:otherwise>
                                    <dl class="vio-dl">
                                        <dt>Lý do đình chỉ</dt>
                                        <dd>${empty violation.reason ? '—' : violation.reason}</dd>

                                        <dt>Chi tiết vi phạm</dt>
                                        <dd>${empty violation.details ? '—' : violation.details}</dd>

                                        <dt>Ảnh minh chứng</dt>
                                        <dd>
                                            <c:choose>
                                                <c:when test="${not empty violationEvidenceUrl}">
                                                    <img class="vio-img"
                                                         src="${violationEvidenceUrl}"
                                                         alt="Ảnh minh chứng">
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="vio-hint">Không có ảnh minh chứng.</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </dd>
                                    </dl>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:when>

                    <%--case 3: redirect to action--%>
                    <c:otherwise>
                        <c:redirect url="${ctx}/examiner/action" />
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </body>
</html>
