<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="printDocxUrl" value="${ctx}/examiner/print/docx" />
<c:set var="headerTitle" value="In văn bản" />

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
            <jsp:param name="pageCss" value="export.css,print.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="print-documents" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll">

                <!--action message-->
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <!--print list-->
                <div class="export-card">
                    <jsp:include page="/views/examiner/components/export-row.jsp">
                        <jsp:param name="btnClass" value="print-btn" />
                    </jsp:include>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">description</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Biên bản thi</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${printDocxUrl}?type=minutes" class="print-btn" target="_blank" rel="noopener">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">group</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Danh sách thí sinh</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${printDocxUrl}?type=candidates" class="print-btn" target="_blank" rel="noopener">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">assignment</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Kết quả thi</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${printDocxUrl}?type=results" class="print-btn" target="_blank" rel="noopener">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">assignment_turned_in</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Phiếu điểm thực hành</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${ctx}/views/examiner/score-entry" class="print-btn">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row export-row--last">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--red">
                                <span class="material-symbols-outlined">warning</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Biên bản vi phạm</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${printDocxUrl}?type=violations" class="print-btn" target="_blank" rel="noopener">
                                <span class="material-symbols-outlined">print</span>
                                <span class="print-btn__text">In</span>
                            </a>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>
