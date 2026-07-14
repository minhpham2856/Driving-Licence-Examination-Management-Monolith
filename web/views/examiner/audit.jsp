<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Nhật ký" />
<c:set var="pageUrl" value="${ctx}/views/examiner/audit" />

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
            <jsp:param name="pageCss" value="audit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="audit" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--dashboard">

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="btnPrintAudit" value="left" />
                    <jsp:param name="btnSearch" value="right" />
                    <jsp:param name="searchWide" value="true" />
                    <jsp:param name="searchPlaceholder" value="Tìm kiếm mô tả, địa chỉ IP..." />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--audit list-->
                <div class="audit-card">
                    <div class="examiner-table-wrap">
                        <table class="examiner-table audit-table">
                            <thead>
                                <tr>
                                    <th>Người dùng</th>
                                    <th>Thao tác</th>
                                    <th>Đối tượng</th>
                                    <th>SBD</th>
                                    <th>Thông tin</th>
                                    <th>Cũ</th>
                                    <th>Mới</th>
                                    <th>Lý do</th>
                                    <th>Thời gian</th>
                                    <th>Ngày</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty auditLogs}">
                                        <tr><td colspan="10" class="examiner-table__empty">
                                            <c:choose>
                                                <c:when test="${searchActive}">Không có kết quả phù hợp.</c:when>
                                                <c:otherwise>Chưa có nhật ký.</c:otherwise>
                                            </c:choose>
                                        </td></tr>
                                    </c:when>
                                    <c:otherwise>
                                <c:forEach items="${auditLogs}" var="log">
                                    <tr>
                                        <td>${log.username}</td>
                                        <td><span class="audit-badge ${log.actionBadge}">${log.actionLabel}</span></td>
                                        <td>${log.entityName}</td>
                                        <td class="examiner-table__mono">${log.sbd}</td>
                                        <td>${log.info}</td>
                                        <td class="audit-td--old<c:if test='${log.multiline}'> audit-td--multiline</c:if>"><c:if test="${not empty log.oldValue}"><s>${log.oldValue}</s></c:if></td>
                                        <td class="audit-td--new ${log.newValueClass}<c:if test='${log.multiline}'> audit-td--multiline</c:if>">${log.newValue}</td>
                                        <td class="audit-td--reason"><c:if test="${log.reason ne '-'}">${log.reason}</c:if></td>
                                        <td class="examiner-table__mono-md">${log.time}</td>
                                        <td class="examiner-table__mono-md">${log.date}</td>
                                    </tr>
                                </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!--pagination-->
                    <div class="audit-pagination">
                        <nav class="audit-page-nav">
                            <c:url var="prevUrl" value="/views/examiner/audit">
                                <c:param name="page" value="${auditPage - 1}" />
                                <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}" /></c:if>
                            </c:url>
                            <c:url var="nextUrl" value="/views/examiner/audit">
                                <c:param name="page" value="${auditPage + 1}" />
                                <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}" /></c:if>
                            </c:url>
                            <c:choose>
                                <c:when test="${auditPage > 1}">
                                    <a href="${prevUrl}" class="audit-page-btn audit-page-btn--nav">
                                        <span class="material-symbols-outlined">chevron_left</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span class="audit-page-btn audit-page-btn--nav audit-page-btn--disabled">
                                        <span class="material-symbols-outlined">chevron_left</span>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                            <span class="audit-page-btn audit-page-btn--active">${auditPage} / ${auditTotalPages}</span>
                            <c:choose>
                                <c:when test="${auditPage < auditTotalPages}">
                                    <a href="${nextUrl}" class="audit-page-btn audit-page-btn--nav">
                                        <span class="material-symbols-outlined">chevron_right</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span class="audit-page-btn audit-page-btn--nav audit-page-btn--disabled">
                                        <span class="material-symbols-outlined">chevron_right</span>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </nav>
                    </div>
                </div>
            </main>
        </div>

    </body>
</html>
