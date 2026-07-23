<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Vi phạm" />

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
            <jsp:param name="pageCss" value="score-entry.css,candidate-detail.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="details" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />
            <main class="examiner-main examiner-main--scroll">
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <c:choose>
                    <c:when test="${param.mode eq 'create' and not empty candidate}">
                        <section class="examiner-card">
                            <div class="examiner-card__head">
                                <div class="score-entry-card__title">
                                    <span class="material-symbols-outlined">gavel</span>
                                    <h2>Đình chỉ thí sinh</h2>
                                </div>
                            </div>

                            <div class="score-entry-selected-card">
                                <div class="score-entry-selected-card__main">
                                    <span class="score-entry-selected-card__eyebrow">Thí sinh bị đình chỉ</span>
                                    <h2>${candidate.fullName}</h2>
                                    <p>SBD: <strong>${candidate.candidateNumber}</strong></p>
                                </div>
                                <div class="score-entry-selected-card__meta">
                                    <span>${candidate.statusLabel}</span>
                                    <span>Hạng: ${empty candidate.licenceClass ? '-' : candidate.licenceClass}</span>
                                </div>
                            </div>

                            <form method="post" action="${ctx}/examiner/violations" enctype="multipart/form-data">
                                <input type="hidden" name="action" value="createViolation">
                                <input type="hidden" name="sbd" value="${candidate.candidateNumber}">

                                <div class="violation-form-field">
                                    <label class="violation-form-field__label" for="reasonCode">Lý do đình chỉ</label>
                                    <select id="reasonCode" name="reasonCode" class="violation-form-field__select" required>
                                        <option value="">-- Chọn lý do --</option>
                                        <c:forEach var="reason" items="${violationReasons}">
                                            <option value="${reason.code}">${reason.label}</option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="violation-form-field">
                                    <label class="violation-form-field__label" for="reasonDetail">Chi tiết vi phạm</label>
                                    <textarea id="reasonDetail" name="reasonDetail" class="violation-form-field__textarea"
                                              rows="5" maxlength="2000"
                                              placeholder="Nhập mô tả chi tiết vi phạm, bắt buộc nếu chọn lý do Khác"></textarea>
                                </div>

                                <div class="violation-form-field">
                                    <label class="violation-form-field__label" for="evidenceFile">Ảnh minh chứng</label>
                                    <input id="evidenceFile" type="file" name="evidenceFile"
                                           class="violation-form-field__file"
                                           accept="image/jpeg,image/png,image/webp" required>
                                    <p class="violation-form-field__hint">Chỉ nhận JPEG, PNG hoặc WebP; tối đa 5 MB.</p>
                                </div>

                                <div class="violation-form-actions">
                                    <button type="submit" class="examiner-btn examiner-btn--danger">
                                        <span class="material-symbols-outlined">gavel</span>
                                        Xác nhận đình chỉ
                                    </button>
                                    <a href="${ctx}/examiner/action" class="examiner-btn examiner-btn--white">
                                        <span class="material-symbols-outlined">close</span>
                                        Hủy
                                    </a>
                                </div>
                            </form>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <jsp:include page="/views/examiner/components/candidate-list.jsp">
                            <jsp:param name="title" value="Danh sách vi phạm"/>
                            <jsp:param name="actionViewViolation" value="true"/>
                            <jsp:param name="showStatus" value="true"/>
                        </jsp:include>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </body>
</html>
