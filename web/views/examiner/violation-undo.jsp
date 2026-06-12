<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Hoàn tác đình chỉ" />
<c:set var="backUrl" value="${ctx}/views/examiner/violations" />

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
                </section>

                <jsp:include page="/views/examiner/partials/examiner-messages.jsp" />

                <div class="score-entry-grid">
                    <div class="score-entry-col score-entry-col--main">
                        <section class="score-entry-card">
                            <div class="score-entry-card__head">
                                <div class="score-entry-card__title">
                                    <span class="material-symbols-outlined">person</span>
                                    <h2>Thí sinh bị đình chỉ</h2>
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
                                            <th>Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td class="score-entry-table__sbd">${candidate.sbd}</td>
                                            <td>${candidate.fullName}</td>
                                            <td class="score-entry-table__mono">${candidate.dob}</td>
                                            <td class="score-entry-table__mono">${candidate.governmentId}</td>
                                            <td><span class="examiner-tag examiner-tag--suspended">${candidate.statusLabel}</span></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>

                    <aside class="score-entry-col score-entry-col--penalties">
                        <form action="${ctx}/views/examiner/violation-undo" method="post"
                              class="score-entry-card score-entry-card--penalties">
                            <input type="hidden" name="sbd" value="${candidate.sbd}">
                            <div class="score-entry-card__head">
                                <div class="score-entry-card__title">
                                    <span class="material-symbols-outlined">undo</span>
                                    <h2>Hoàn tác đình chỉ</h2>
                                </div>
                            </div>

                            <div class="violation-form-field">
                                <label for="reasonCode" class="violation-form-field__label">Lý do hoàn tác</label>
                                <select id="reasonCode" name="reasonCode" class="violation-form-field__select" required>
                                    <option value="">— Chọn lý do —</option>
                                    <c:forEach var="reason" items="${violationReasons}">
                                        <option value="${reason.code}">${reason.label}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="violation-form-field">
                                <label for="reasonDetail" class="violation-form-field__label">Chi tiết</label>
                                <textarea id="reasonDetail" name="reasonDetail" class="violation-form-field__textarea"
                                          placeholder="Mô tả lý do hoàn tác đình chỉ..."></textarea>
                            </div>

                            <div class="violation-form-actions">
                                <button type="submit" class="examiner-btn examiner-btn--primary">
                                    <span class="material-symbols-outlined">undo</span>
                                    Hoàn tác đình chỉ
                                </button>
                            </div>
                        </form>
                    </aside>
                </div>
            </main>
        </div>

    </body>
</html>
