<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/devices" scope="request" />

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="devices.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <%--sidebar--%>
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="devices" />
        </jsp:include>

        <%--shell--%>
        <div class="shell">

            <%--header--%>
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Quản lý thiết bị" />
            </jsp:include>

            <%--main content--%>
            <main class="main scroll">

                <%--action message--%>
                <jsp:include page="/views/examiner/components/messages.jsp" />

                <%--toolbar--%>
                <section class="toolbar toolbar-tools">
                    <div class="toolbar-group"></div>
                    <div class="toolbar-group search-form">
                        <jsp:include page="/views/examiner/components/search-form.jsp">
                            <jsp:param name="wide" value="true" />
                            <jsp:param name="placeholder" value="Tìm theo tên, loại, trạng thái..." />
                        </jsp:include>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <%--device list--%>
                <div class="status-key">
                    <span class="status-key-item">
                        <span class="status-key-color-dot free"></span>
                        Sẵn sàng
                    </span>
                    <span class="status-key-item">
                        <span class="status-key-color-dot unused"></span>
                        Bảo trì
                    </span>
                </div>
                <section class="card table-fill">
                    <div class="card-head">
                        <h2 class="card-title">
                            ${examinerSectionTheory ? 'Máy thi lý thuyết' : 'Xe thi thực hành'}
                        </h2>
                        <span class="badge">
                            Tổng: ${fn:length(devices)} ${examinerSectionTheory ? 'máy' : 'xe'}
                        </span>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <%--case 1: no devices--%>
                            <c:when test="${empty devices}">
                                <p class="table-empty">Không có thiết bị trong khu vực thi.</p>
                            </c:when>

                            <%--case 2: device table--%>
                            <c:otherwise>
                                <div class="table-wrap">
                                    <table class="table">
                                        <thead>
                                            <tr>
                                                <th>Tên</th>
                                                <th>Khu vực</th>
                                                <th>Trạng thái</th>
                                                <th>Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="device" items="${devices}" varStatus="row">
                                                <tr class="${row.index % 2 == 1 ? 'table-row-alt' : ''}">
                                                    <td class="table-mono-md">${device.name}</td>
                                                    <td>${empty device.area ? '—' : device.area}</td>
                                                    <td>
                                                        <span class="device-badge ${device.statusClass}">
                                                            ${device.statusLabel}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <form method="post" action="${pageUrl}">
                                                            <input type="hidden"
                                                                   name="action"
                                                                   value="${device.status eq 'Bảo trì' ? 'operational' : 'maintenance'}">
                                                            <input type="hidden" name="deviceId" value="${device.id}">
                                                            <c:if test="${not empty searchQuery}">
                                                                <input type="hidden" name="q" value="${searchQuery}">
                                                            </c:if>
                                                            <button type="submit" class="link-action">
                                                                ${device.status eq 'Bảo trì' ? 'Sử dụng' : 'Bảo trì'}
                                                            </button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </main>
        </div>
    </body>
</html>
