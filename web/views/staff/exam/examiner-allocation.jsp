<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Phân bổ sát hạch viên - Ban Sát Hạch</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    </head>
    <body class="has-side-nav-bar">

        <jsp:include page="/views/layout/sidebar-examstaff.jsp">
            <jsp:param name="activeSidebar" value="phan-bo-giam-khao" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content">
                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current">Ban Sát Hạch</span>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Phân bổ sát hạch viên</span>
                </nav>

                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Phân bổ sát hạch viên theo phòng thi</h1>
                        <p class="page-subtitle">Schema <strong>DLEM_DB_2</strong>: phòng từ <strong>Session_ExamArea</strong>, thiết bị <strong>ExamDevice</strong>, sát hạch viên <strong>Session_Examiner</strong> + phòng trong <strong>Audit</strong>.</p>
                    </div>
                    <div class="page-actions">
                        <form method="get" action="${pageContext.request.contextPath}/views/staff/exam/examiner-allocation" class="examiner-session-form">
                            <label for="sessionId" class="examiner-session-form__label">Ca thi:</label>
                            <select name="sessionId" id="sessionId" class="examiner-session-form__select">
                                <c:forEach var="s" items="${allSessions}">
                                    <option value="${s.id}" ${s.id eq currentSession.id ? 'selected' : ''}>
                                        ${s.sessionName} - <fmt:formatDate value="${s.examDate}" pattern="dd/MM/yyyy"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>
                </header>

                <c:if test="${not empty alertMsg}">
                    <div class="examiner-alert examiner-alert--success">${alertMsg}</div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="examiner-alert examiner-alert--error">${errorMsg}</div>
                </c:if>

                <c:if test="${not empty currentSession}">
                    <div class="examiner-panel-card examiner-panel-card--spaced" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <div>
                            <strong>Trạng thái ca:</strong>
                            <c:choose>
                                <c:when test="${currentSession.status eq 'Đang diễn ra'}">
                                    <span class="role-badge role-badge--admin" style="margin-left: 6px;">Đang diễn ra - sát hạch viên có thể đăng nhập</span>
                                </c:when>
                                <c:when test="${currentSession.status eq 'Hoàn tất'}">
                                    <span class="role-badge" style="margin-left: 6px;">Đã kết thúc</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="role-badge role-badge--coi" style="margin-left: 6px;">Chưa bắt đầu - phân công xong, bấm Bắt đầu ca ở Tổng quan</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${currentSession.status ne 'Đang diễn ra' and currentSession.status ne 'Hoàn tất'}">
                            <form action="session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Bắt đầu ca thi sau khi đã phân đủ sát hạch viên?');">
                                <input type="hidden" name="action" value="startSession">
                                <input type="hidden" name="sessionId" value="${currentSession.id}">
                                <input type="hidden" name="redirect" value="examiner-allocation">
                                <button type="submit" class="btn-filter" style="height: 36px; padding: 0 1rem; border-radius: 8px; font-weight: 700;">Bắt đầu ca thi</button>
                            </form>
                        </c:if>
                        <c:if test="${currentSession.status eq 'Đang diễn ra'}">
                            <form action="session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Kết thúc ca thi?');">
                                <input type="hidden" name="action" value="endSession">
                                <input type="hidden" name="sessionId" value="${currentSession.id}">
                                <input type="hidden" name="redirect" value="examiner-allocation">
                                <button type="submit" class="btn-export" style="height: 36px; padding: 0 1rem; border-radius: 8px; font-weight: 700; color: #b91c1c; border-color: #fecaca;">Kết thúc ca thi</button>
                            </form>
                        </c:if>
                    </div>
                    <div class="examiner-panel-card examiner-panel-card--spaced">
                        <h3>Ca thi trong ngày <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/></h3>
                        <c:forEach var="ds" items="${daySessions}">
                            <span class="session-pill">
                                ${ds.sessionName}
                                (<fmt:formatDate value="${ds.shiftStartTime}" pattern="HH:mm"/>–<fmt:formatDate value="${ds.shiftEndTime}" pattern="HH:mm"/>)
                                - <c:choose><c:when test="${ds.examTypeName eq 'Theory'}">Lý thuyết</c:when><c:when test="${ds.examTypeName eq 'Practical'}">Thực hành</c:when><c:when test="${ds.examTypeName eq 'OnRoad'}">Đường trường</c:when><c:otherwise>${ds.examTypeName}</c:otherwise></c:choose>
                                    </span>
                        </c:forEach>
                    </div>

                    <div class="examiner-grid">
                        <div class="examiner-panel-card">
                            <h3>sát hạch viên khả dụng (${availableExaminers.size()})</h3>
                            <c:choose>
                                <c:when test="${empty availableExaminers}">
                                    <p class="es-text-muted-sm">Không còn sát hạch viên trống trong ngày này.</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ex" items="${availableExaminers}">
                                        <span class="examiner-chip chip-available">${ex.profile.fullName} (@${ex.username})</span>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="examiner-panel-card">
                            <h3>sát hạch viên đã phân công (${busyExaminers.size()})</h3>
                            <c:choose>
                                <c:when test="${empty busyExaminers}">
                                    <p class="es-text-muted-sm">Chưa phân công sát hạch viên nào.</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ex" items="${busyExaminers}">
                                        <span class="examiner-chip chip-busy">${ex.profile.fullName} (@${ex.username})</span>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="examiner-panel-card examiner-panel-card--section">
                        <h3>Phân công mới</h3>
                        <form class="examiner-assign-form" method="get" action="${pageContext.request.contextPath}/views/staff/exam/examiner-allocation">
                            <input type="hidden" name="sessionId" value="${currentSession.id}">
                            <input type="hidden" name="action" value="assign">
                            <div>
                                <label for="targetSessionId">Ca thi</label>
                                <select name="targetSessionId" id="targetSessionId" required>
                                    <c:forEach var="ds" items="${daySessions}">
                                        <option value="${ds.id}">${ds.sessionName} (<c:choose><c:when test="${ds.examTypeName eq 'Theory'}">Lý thuyết</c:when><c:when test="${ds.examTypeName eq 'Practical'}">Thực hành</c:when><c:when test="${ds.examTypeName eq 'OnRoad'}">Đường trường</c:when><c:otherwise>${ds.examTypeName}</c:otherwise></c:choose>)</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label for="areaId">Phòng thi (Session_ExamArea)</label>
                                <select name="areaId" id="areaId" required>
                                    <c:forEach var="ds" items="${daySessions}">
                                        <c:forEach var="ar" items="${areasBySession[ds.id]}">
                                            <option value="${ar.id}" data-session="${ds.id}" data-type="${ar.areaType}">
                                                ${ar.areaName} (${ar.areaType})
                                            </option>
                                        </c:forEach>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label for="examinerUserId">sát hạch viên</label>
                                <select name="examinerUserId" id="examinerUserId" required>
                                    <c:forEach var="ex" items="${allExaminers}">
                                        <option value="${ex.id}">${ex.profile.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label>&nbsp;</label>
                                <button type="submit">Phân công</button>
                            </div>
                        </form>
                    </div>

                    <div class="examiner-panel-card examiner-panel-card--section">
                        <h3>Phân công ca đang chọn: ${currentSession.sessionName}</h3>
                        <table class="examiner-data-table">
                            <thead>
                                <tr>
                                    <th>Phòng thi</th>
                                    <th>Loại thi</th>
                                    <th>sát hạch viên</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty sessionAssignments}">
                                        <tr><td colspan="4" class="es-text-muted-sm">Chưa có phân công cho ca này.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="a" items="${sessionAssignments}">
                                            <tr>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty a.areaName}">${a.areaName}<div class="area-type-tag">${a.areaType}</div></c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${a.examTypeName}</td>
                                                <td>${a.examinerName} <span class="es-text-muted-sm">(@${a.examinerUsername})</span></td>
                                                <td>
                                                    <c:if test="${a.areaId > 0}">
                                                        <a class="btn-examiner-remove"
                                                           href="${pageContext.request.contextPath}/views/staff/exam/examiner-allocation?sessionId=${currentSession.id}&action=remove&slotKey=${a.slotKey}"
                                                           data-confirm-remove="true"
                                                           data-confirm-msg="Gỡ phân công sát hạch viên này?">Gỡ</a>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <div class="examiner-panel-card">
                        <h3>Tổng hợp phân công cả ngày</h3>
                        <table class="examiner-data-table">
                            <thead>
                                <tr>
                                    <th>Ca thi</th>
                                    <th>Phòng</th>
                                    <th>Loại thi</th>
                                    <th>sát hạch viên</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty dayAssignments}">
                                        <tr><td colspan="4" class="es-text-muted-sm">Chưa có phân công trong ngày.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="a" items="${dayAssignments}">
                                            <tr>
                                                <td>${a.sessionName}</td>
                                                <td>${empty a.areaName ? '-' : a.areaName}</td>
                                                <td>${a.examTypeName}</td>
                                                <td>${a.examinerName}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </main>
        </div>

        <script src="${pageContext.request.contextPath}/assets/js/examiner-allocation.js" charset="UTF-8"></script>
    </body>
</html>
