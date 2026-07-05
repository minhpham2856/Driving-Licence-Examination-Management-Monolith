<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Xuất dữ liệu" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" />
<c:set var="exportResultsUrl" value="${ctx}/examiner/export/results" />
<c:set var="exportMinutesUrl" value="${ctx}/examiner/export/minutes" />
<c:set var="exportViolationsUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportAuditUrl" value="${ctx}/examiner/export/audit" />
<c:set var="exportCandidatesXmlUrl" value="${ctx}/examiner/export/candidates/xml" />
<c:set var="exportResultsXmlUrl" value="${ctx}/examiner/export/results/xml" />
<c:set var="exportMinutesXmlUrl" value="${ctx}/examiner/export/minutes/xml" />
<c:set var="exportViolationsXmlUrl" value="${ctx}/examiner/export/violations/xml" />
<c:set var="exportAuditXmlUrl" value="${ctx}/examiner/export/audit/xml" />
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
            <jsp:param name="pageCss" value="export.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="export" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll">
                
                <!--export list-->
                <div class="export-card">
                    <jsp:include page="/views/examiner/components/export-row.jsp" />

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
                            <a href="${exportCandidatesUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="${exportCandidatesXmlUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="${exportDocxUrl}?type=candidates" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
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
                            <a href="${exportResultsUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="${exportResultsXmlUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="${exportDocxUrl}?type=results" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

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
                            <a href="${exportMinutesUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="${exportMinutesXmlUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="${exportDocxUrl}?type=minutes" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--red">
                                <span class="material-symbols-outlined">warning</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Biên bản vi phạm</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${exportViolationsUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="${exportViolationsXmlUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="${exportDocxUrl}?type=violations" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>

                    <div class="export-row export-row--last">
                        <div class="export-row__left">
                            <div class="export-row__icon export-row__icon--blue">
                                <span class="material-symbols-outlined">list_alt</span>
                            </div>
                            <div class="export-row__info">
                                <p class="export-row__title">Nhật ký</p>
                            </div>
                        </div>
                        <div class="export-row__actions">
                            <a href="${exportAuditUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">excel</span>
                            </a>
                            <a href="${exportAuditXmlUrl}" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">XML</span>
                            </a>
                            <a href="${exportDocxUrl}?type=audit" class="export-btn">
                                <span class="material-symbols-outlined">download</span>
                                <span class="export-btn__text">docx</span>
                            </a>
                        </div>
                    </div>
                </div>
            </main>
        </div>

    </body>
</html>
