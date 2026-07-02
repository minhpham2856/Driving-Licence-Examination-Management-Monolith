<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Quản lý thiết bị" />
<c:set var="pageUrl" value="${ctx}/views/examiner/devices" />

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
            <jsp:param name="pageCss" value="devices.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="devices" />
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
                    <jsp:param name="btnSearch" value="right" />
                    <jsp:param name="searchWide" value="true" />
                    <jsp:param name="searchPlaceholder" value="Tìm theo tên, loại, trạng thái..." />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--device list-->
                <div class="device-grid-legend">
                    <span class="device-grid-legend__item">
                        <span class="device-grid-legend__swatch device-grid-legend__swatch--available"></span>
                        Sẵn sàng
                    </span>
                    <span class="device-grid-legend__item">
                        <span class="device-grid-legend__swatch device-grid-legend__swatch--maintenance"></span>
                        Bảo trì
                    </span>
                </div>
                <jsp:include page="/views/examiner/components/device-grid.jsp">
                    <jsp:param name="cardClass" value="examiner-card examiner-card--dashboard-table" />
                    <jsp:param name="title" value="${empty devicesTitle ? 'Máy thi' : devicesTitle}" />
                    <jsp:param name="badgeText" value="Tổng: ${fn:length(devices)} ${empty devicesUnit ? 'máy' : devicesUnit}" />
                    <jsp:param name="pageUrl" value="${pageUrl}" />
                </jsp:include>
            </main>
        </div>
    </body>
</html>
