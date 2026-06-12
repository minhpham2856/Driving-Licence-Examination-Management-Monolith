<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa thông tin" />
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-details-edit?sbd=${candidate.sbd}" />
<c:set var="paperUrl" value="${ctx}/views/examiner/candidate-paper?sbd=${candidate.sbd}" />
<c:set var="resultUrl" value="${ctx}/views/examiner/result-details-edit?sbd=${candidate.sbd}" />

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
            <jsp:param name="pageCss" value="candidate-detail.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="sua-thong-tin" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <section class="examiner-toolbar">
                    <div class="exr-toolbar-left">
                        <a href="${backUrl}" class="exr-back">
                            <span class="material-symbols-outlined">arrow_back</span>
                            QUAY LẠI
                        </a>
                    </div>
                    <div class="examiner-toolbar__actions">
                        <c:if test="${not empty candidate}">
                            <a href="${paperUrl}" class="examiner-btn examiner-btn--white">
                                <span class="material-symbols-outlined">visibility</span>
                                Xem đề thi
                            </a>
                            <a href="${resultUrl}" class="examiner-btn examiner-btn--white">
                                <span class="material-symbols-outlined">fact_check</span>
                                Sửa kết quả
                            </a>
                        </c:if>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <c:if test="${not empty profileError}">
                    <div class="examiner-alert examiner-alert--error">${profileError}</div>
                </c:if>
                <c:if test="${param.saved eq '1'}">
                    <div class="examiner-alert examiner-alert--success">Đã lưu thông tin thí sinh.</div>
                </c:if>

                <c:choose>
                    <c:when test="${empty candidate}">
                        <section class="examiner-card">
                            <p class="examiner-table__empty">Không tìm thấy thí sinh. Quay lại danh sách và chọn SBD hợp lệ.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <form action="${ctx}/views/examiner/candidate-details-edit" method="post" class="examiner-bento examiner-bento--form">
                            <input type="hidden" name="sbd" value="${candidate.sbd}">

                            <div class="examiner-bento__profile">
                                <div class="examiner-profile__photo">
                                    <span class="examiner-profile__photo-icon material-symbols-outlined">person</span>
                                </div>
                                <p class="examiner-profile__name">${candidate.fullName}</p>
                                <p class="examiner-profile__sbd">SBD: ${candidate.sbd}</p>
                            </div>

                            <div class="examiner-bento__detail">
                                <div class="examiner-detail-section">
                                    <span class="examiner-detail-section__icon material-symbols-outlined">person</span>
                                    <span>THÔNG TIN CÁ NHÂN</span>
                                </div>

                                <div class="examiner-fields examiner-fields--form">
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="fullName">Họ và Tên</label>
                                        <input type="text" id="fullName" name="fullName" class="exr-input" required
                                               value="${candidate.fullName != '—' ? candidate.fullName : ''}">
                                    </div>
                                    <div class="examiner-field">
                                        <p class="examiner-field__label">SBD</p>
                                        <p class="examiner-field__value examiner-field__value--mono examiner-field__value--bold">${candidate.sbd}</p>
                                    </div>
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="govIdNo">Số căn cước</label>
                                        <input type="text" id="govIdNo" name="govIdNo" class="exr-input exr-input--mono" required
                                               value="${candidate.governmentId != '—' ? candidate.governmentId : ''}">
                                    </div>
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="dateOfBirth">Ngày sinh</label>
                                        <input type="date" id="dateOfBirth" name="dateOfBirth" class="exr-input exr-input--mono" required
                                               value="${candidate.dobRaw}">
                                    </div>
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="sex">Giới tính</label>
                                        <select id="sex" name="sex" class="exr-select">
                                            <option value="0" ${candidate.genderValue eq '0' ? 'selected' : ''}>Nam</option>
                                            <option value="1" ${candidate.genderValue eq '1' ? 'selected' : ''}>Nữ</option>
                                        </select>
                                    </div>
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="phoneNo">Số điện thoại</label>
                                        <input type="text" id="phoneNo" name="phoneNo" class="exr-input exr-input--mono"
                                               value="${candidate.phoneNo}">
                                    </div>
                                    <div class="examiner-field">
                                        <label class="examiner-field__label" for="email">Email</label>
                                        <input type="email" id="email" name="email" class="exr-input"
                                               value="${candidate.email}">
                                    </div>
                                    <div class="examiner-field examiner-field--full">
                                        <label class="examiner-field__label" for="address">Địa chỉ</label>
                                        <input type="text" id="address" name="address" class="exr-input"
                                               value="${candidate.address != '—' ? candidate.address : ''}">
                                    </div>

                                    <div class="examiner-detail-section examiner-detail-section--full">
                                        <span class="examiner-detail-section__icon material-symbols-outlined">list_alt</span>
                                        <span>CHI TIẾT KỲ THI (chỉ đọc)</span>
                                    </div>

                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Hạng GPLX</p>
                                        <p class="examiner-field__value examiner-field__value--bold">${candidate.licenceClass}</p>
                                    </div>
                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Ngày thi</p>
                                        <p class="examiner-field__value examiner-field__value--mono">${candidate.examDate}</p>
                                    </div>
                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Tình trạng</p>
                                        <p class="examiner-field__value">${candidate.statusLabel}</p>
                                    </div>
                                    <div class="examiner-field">
                                        <p class="examiner-field__label">Kết quả</p>
                                        <p class="examiner-field__value examiner-field__value--bold">${candidate.resultLabel}</p>
                                    </div>
                                    <c:choose>
                                        <c:when test="${examinerSectionTheory}">
                                            <div class="examiner-field">
                                                <p class="examiner-field__label">Đúng / Sai / Không TL</p>
                                                <p class="examiner-field__value examiner-field__value--mono">${candidate.correct} / ${candidate.wrong} / ${candidate.unanswered}</p>
                                            </div>
                                            <div class="examiner-field">
                                                <p class="examiner-field__label">Điểm lý thuyết</p>
                                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scoreTheory}</p>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="examiner-field">
                                                <p class="examiner-field__label">Điểm phần thi</p>
                                                <p class="examiner-field__value examiner-field__value--mono">${candidate.examScore}</p>
                                            </div>
                                            <div class="examiner-field">
                                                <p class="examiner-field__label">Điểm thực hành</p>
                                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scorePractical}</p>
                                            </div>
                                            <div class="examiner-field">
                                                <p class="examiner-field__label">Điểm đường trường</p>
                                                <p class="examiner-field__value examiner-field__value--mono">${candidate.scoreOnRoad}</p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="examiner-field examiner-field--full">
                                        <label class="examiner-field__label" for="reasonForTaking">Lý do sát hạch</label>
                                        <textarea id="reasonForTaking" name="reasonForTaking" class="exr-textarea">${candidate.reasonForTaking != '—' ? candidate.reasonForTaking : ''}</textarea>
                                    </div>
                                </div>

                                <div class="examiner-form-actions">
                                    <button type="submit" class="examiner-btn examiner-btn--primary">
                                        <span class="material-symbols-outlined">save</span>
                                        Lưu thông tin
                                    </button>
                                    <a href="${backUrl}" class="examiner-btn examiner-btn--white">Hủy</a>
                                </div>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>

    </body>
</html>
