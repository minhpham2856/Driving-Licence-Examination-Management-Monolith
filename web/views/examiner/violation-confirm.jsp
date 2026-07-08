<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Xác nhận đình chỉ" />
<c:set var="backUrl" value="${ctx}/views/examiner/violations" />
<c:set var="sbdParam" value="${not empty candidate.sbd ? candidate.sbd : param.sbd}" />
<c:set var="pageUrl" value="${ctx}/views/examiner/violation-confirm?sbd=${sbdParam}" />
<c:set var="exportExcelUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportXmlUrl" value="${ctx}/examiner/export/violations/xml" />
<c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx" />

<!--page-->
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
            <jsp:param name="pageCss" value="score-entry.css,print.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="violations" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll">

                <!--action message-->
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="score-entry-toolbar" />
                    <jsp:param name="leftClass" value="score-entry-toolbar__left" />
                    <jsp:param name="rightClass" value="score-entry-toolbar__right" />
                    <jsp:param name="btnBack" value="left" />
                    <jsp:param name="btnPrintViolation" value="right" />
                </jsp:include>

                <!--violation form-->
                <div class="score-entry-grid" id="violationPrintArea">
                    <div class="score-entry-col score-entry-col--main">
                        <section class="score-entry-card">
                            <div class="score-entry-card__head">
                                <h2>Thí sinh vi phạm</h2>
                            </div>
                            <div class="score-entry-table-wrap">
                                <table class="score-entry-table">
                                    <thead>
                                        <tr>
                                            <th>SBD</th>
                                            <th>Họ và tên</th>
                                            <th>Ngày sinh</th>
                                            <th>Số căn cước</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td>${candidate.sbd}</td>
                                            <td>${candidate.fullName}</td>
                                            <td>${candidate.dob}</td>
                                            <td>${candidate.governmentId}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>

                    <!--violation choosing-->
                    <aside class="score-entry-col score-entry-col--penalties">
                        <form action="${ctx}/views/examiner/violation-confirm" method="post"
                              enctype="multipart/form-data"
                              class="score-entry-card score-entry-card--penalties">
                            <input type="hidden" name="sbd" value="${sbdParam}">
                            <input type="hidden" name="returnTo" value="${param.returnTo}">
                            <div class="score-entry-card__head"><h2>Ghi nhận vi phạm</h2></div>

                            <!--violation reason-->
                            <div class="violation-form-field">
                                <label for="reasonCode" class="violation-form-field__label">Lý do vi phạm</label>
                                <select id="reasonCode" name="reasonCode" class="violation-form-field__select" required>
                                    <option value="">Chọn lý do...</option>
                                    <c:forEach var="reason" items="${violationReasons}">
                                        <option value="${reason.code}">${reason.label}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="violation-form-field">
                                <label for="reasonDetail" class="violation-form-field__label">Chi tiết vi phạm</label>
                                <textarea id="reasonDetail" name="reasonDetail" class="violation-form-field__textarea"
                                          placeholder="Mô tả chi tiết vi phạm..."></textarea>
                            </div>

                            <div class="violation-form-field">
                                <label for="evidenceFile" class="violation-form-field__label">Ảnh minh chứng</label>
                                <input type="file" id="evidenceFile" name="evidenceFile"
                                       class="violation-form-field__file"
                                       accept="image/jpeg,image/png,image/webp">
                            </div>

                            <div class="violation-form-actions">
                                <button type="submit" class="examiner-btn examiner-btn--primary">
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
