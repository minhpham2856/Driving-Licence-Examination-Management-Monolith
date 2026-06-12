<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Xác nhận đình chỉ" />
<c:set var="backUrl" value="${ctx}/views/examiner/violations" />
<c:set var="sbdParam" value="${not empty candidate.sbd ? candidate.sbd : param.sbd}" />
<c:set var="pageUrl" value="${ctx}/views/examiner/violation-confirm?sbd=${sbdParam}" />
<c:set var="exportExcelUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportXmlUrl" value="${ctx}/examiner/export/violations/xml" />

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
            <jsp:param name="pageCss" value="score-entry.css,print.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="vi-pham" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <section class="score-entry-toolbar">
                    <div class="score-entry-toolbar__left">
                        <a href="${backUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">arrow_back</span>
                            Quay lại
                        </a>
                    </div>
                    <div class="score-entry-toolbar__right">
                        <a href="${exportExcelUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất Excel
                        </a>
                        <a href="${exportXmlUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>
                            Xuất XML
                        </a>
                        <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                            <span class="material-symbols-outlined">print</span>
                            In biên bản
                        </button>
                    </div>
                </section>

                <jsp:include page="/views/examiner/partials/examiner-messages.jsp" />

                <div class="score-entry-grid" id="violationPrintArea">
                    <div class="score-entry-col score-entry-col--main">
                        <section class="score-entry-card">
                            <div class="score-entry-card__head">
                                <div class="score-entry-card__title">
                                    <span class="material-symbols-outlined">person</span>
                                    <h2>Thí sinh vi phạm</h2>
                                </div>
                            </div>
                            <div class="score-entry-table-wrap">
                                <table class="score-entry-table">
                                    <thead>
                                        <tr>
                                            <th>SBD</th>
                                            <th>Họ và tên</th>
                                            <th>Ngày sinh</th>
                                            <th>CCCD/CMND</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td class="score-entry-table__sbd">${candidate.sbd}</td>
                                            <td>${candidate.fullName}</td>
                                            <td class="score-entry-table__mono">${candidate.dob}</td>
                                            <td class="score-entry-table__mono">${candidate.governmentId}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        <c:if test="${not empty candidateViolations}">
                            <section class="score-entry-card">
                                <div class="score-entry-card__head">
                                    <div class="score-entry-card__title">
                                        <span class="material-symbols-outlined">history</span>
                                        <h2>Vi phạm đã ghi nhận</h2>
                                    </div>
                                </div>
                                <div class="score-entry-table-wrap">
                                    <table class="score-entry-table">
                                        <thead>
                                            <tr>
                                                <th>Lý do</th>
                                                <th>Phần thi</th>
                                                <th>Điểm trừ</th>
                                                <th>Điểm hiện tại</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="v" items="${candidateViolations}">
                                                <tr>
                                                    <td>${v.violationReason}</td>
                                                    <td>${v.sectionName}</td>
                                                    <td>${v.deductionPoints}</td>
                                                    <td>${v.currentScore}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        </c:if>
                    </div>

                    <aside class="score-entry-col score-entry-col--penalties">
                        <form action="${ctx}/views/examiner/violation-confirm" method="post"
                              enctype="multipart/form-data"
                              class="score-entry-card score-entry-card--penalties">
                            <input type="hidden" name="sbd" value="${sbdParam}">
                            <div class="score-entry-card__head">
                                <div class="score-entry-card__title">
                                    <span class="material-symbols-outlined">warning</span>
                                    <h2>Ghi nhận vi phạm</h2>
                                </div>
                            </div>

                            <div class="violation-form-field">
                                <label for="reasonCode" class="violation-form-field__label">Lý do vi phạm</label>
                                <select id="reasonCode" name="reasonCode" class="violation-form-field__select" required>
                                    <option value="">— Chọn lý do —</option>
                                    <c:forEach var="reason" items="${violationReasons}">
                                        <option value="${reason.code}">${reason.label}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="violation-form-field">
                                <label for="reasonDetail" class="violation-form-field__label">Chi tiết vi phạm</label>
                                <textarea id="reasonDetail" name="reasonDetail" class="violation-form-field__textarea"
                                          placeholder="Mô tả chi tiết vi phạm quy chế thi..."></textarea>
                            </div>

                            <div class="violation-form-field">
                                <label for="evidenceFile" class="violation-form-field__label">Ảnh minh chứng</label>
                                <input type="file" id="evidenceFile" name="evidenceFile"
                                       class="violation-form-field__file"
                                       accept="image/jpeg,image/png,image/webp">
                                <p class="violation-form-field__hint">JPG, PNG hoặc WEBP, tối đa 5MB.</p>
                            </div>

                            <c:if test="${not examinerSectionTheory}">
                                <div class="score-entry-penalty-wrap">
                                    <table class="score-entry-penalty-table">
                                        <thead>
                                            <tr>
                                                <th>Chi tiết lỗi</th>
                                                <th>Trừ</th>
                                                <th>Chọn</th>
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
                                                                <input type="checkbox" class="score-entry-check" name="deductionId" value="${deduction.id}">
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>

                            <div class="violation-form-actions">
                                <button type="submit" class="examiner-btn examiner-btn--primary">
                                    <span class="material-symbols-outlined">gavel</span>
                                    Xác nhận đình chỉ
                                </button>
                            </div>
                        </form>
                    </aside>
                </div>
            </main>
        </div>

    </body>
</html>
