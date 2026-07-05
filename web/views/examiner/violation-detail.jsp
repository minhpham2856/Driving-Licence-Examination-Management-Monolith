<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Chi tiết vi phạm / đình chỉ" />
<c:set var="backUrl" value="${ctx}/views/examiner/violations" />
<c:choose>
    <c:when test="${not empty candidate}">
        <c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx?type=violations&amp;sbd=${candidate.sbd}" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx?type=violations" scope="request" />
    </c:otherwise>
</c:choose>

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

                <div class="score-entry-grid" id="violationPrintArea">
                    <div class="score-entry-col score-entry-col--main">
                        <section class="score-entry-card">
                            <div class="score-entry-card__head">
                                <h2>Thông tin thí sinh</h2>
                            </div>
                            <c:choose>
                                <c:when test="${empty candidate}">
                                    <p class="examiner-table__empty">Không tìm thấy thí sinh.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="score-entry-table-wrap">
                                        <table class="score-entry-table">
                                            <thead>
                                                <tr>
                                                    <th>SBD</th>
                                                    <th>Họ và tên</th>
                                                    <th>Ngày sinh</th>
                                                    <th>Số căn cước</th>
                                                    <th>Trạng thái</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td>${candidate.sbd}</td>
                                                    <td>${candidate.fullName}</td>
                                                    <td>${candidate.dob}</td>
                                                    <td>${candidate.governmentId}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${candidate.suspended}">
                                                                <span class="examiner-tag examiner-tag--suspended">${candidate.statusLabel}</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="examiner-tag examiner-tag--pending">${candidate.statusLabel}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </div>

                    <aside class="score-entry-col score-entry-col--penalties">
                        <section class="score-entry-card score-entry-card--penalties">
                            <div class="score-entry-card__head"><h2>Vi phạm / đình chỉ</h2></div>
                            <c:choose>
                                <c:when test="${empty candidate}">
                                    <p class="examiner-table__empty">Không có dữ liệu.</p>
                                </c:when>
                                <c:when test="${candidate.suspended}">
                                    <p>Thí sinh đang bị <strong>đình chỉ</strong> thi. Giám khảo chỉ được xem thông tin, không thể ghi nhận hoặc hoàn tác vi phạm tại đây.</p>
                                </c:when>
                                <c:otherwise>
                                    <p>Thí sinh <strong>chưa bị đình chỉ</strong>. Giám khảo không có quyền ghi nhận vi phạm tại cổng giám khảo.</p>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </aside>
                </div>
            </main>
        </div>
    </body>
</html>
