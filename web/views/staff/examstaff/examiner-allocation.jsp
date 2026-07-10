<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân bổ Giám khảo - Ban Sát Hạch</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="phan-bo-giam-khao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Phân bổ giám khảo</span>
        </nav>

        <jsp:include page="/views/layout/header-examstaff.jsp">
            <jsp:param name="pageTitle" value="Phân bổ giám khảo" />
            <jsp:param name="sectionTitle" value="Ban Sát Hạch" />
        </jsp:include>

        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Phân bổ giám khảo theo phòng thi</h1>
                <p class="page-subtitle">Phòng từ <strong>Session_ExamArea</strong>, thiết bị <strong>ExamDevice</strong>, giám khảo <strong>Session_Examiner</strong> + phòng trong <strong>Audit</strong>.</p>
            </div>
            <div class="page-actions">
                <form method="get" action="${ctx}/staff/examstaff/examiner-allocation" class="examiner-session-form">
                    <label for="sessionId" class="examiner-session-form__label">Ca thi:</label>
                    <select name="sessionId" id="sessionId" class="examiner-session-form__select">
                        <c:forEach var="s" items="${allSessions}">
                            <option value="${s.id}" ${s.id eq currentSession.id ? 'selected' : ''}>
                                ${s.sessionLabel} — <fmt:formatDate value="${s.examDate}" pattern="dd/MM/yyyy"/>
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
            <div class="report-pane" style="margin-top: 1.25rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                    <div>
                        <strong>Trạng thái ca:</strong>
                        <c:choose>
                            <c:when test="${currentSession.status eq 'InProgress'}">
                                <span class="role-badge role-badge--admin" style="margin-left: 6px;">Đang diễn ra — giám khảo có thể đăng nhập</span>
                            </c:when>
                            <c:when test="${currentSession.status eq 'Completed'}">
                                <span class="role-badge" style="margin-left: 6px;">Đã kết thúc</span>
                            </c:when>
                            <c:otherwise>
                                <span class="role-badge role-badge--coi" style="margin-left: 6px;">Chưa bắt đầu — phân công xong, bấm Bắt đầu ca ở Tổng quan</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${currentSession.status ne 'InProgress' and currentSession.status ne 'Completed'}">
                        <form action="${ctx}/staff/examstaff/session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Bắt đầu ca thi sau khi đã phân đủ giám khảo?');">
                            <input type="hidden" name="action" value="startSession">
                            <input type="hidden" name="sessionId" value="${currentSession.id}">
                            <input type="hidden" name="redirect" value="examiner-allocation">
                            <button type="submit" class="btn-filter" style="height: 36px; padding: 0 1rem; border-radius: 8px; font-weight: 700;">Bắt đầu ca thi</button>
                        </form>
                    </c:if>
                    <c:if test="${currentSession.status eq 'InProgress'}">
                        <form action="${ctx}/staff/examstaff/session-control" method="POST" style="margin: 0;" onsubmit="return confirm('Kết thúc ca thi?');">
                            <input type="hidden" name="action" value="endSession">
                            <input type="hidden" name="sessionId" value="${currentSession.id}">
                            <input type="hidden" name="redirect" value="examiner-allocation">
                            <button type="submit" class="btn-export" style="height: 36px; padding: 0 1rem; border-radius: 8px; font-weight: 700; color: #b91c1c; border-color: #fecaca;">Kết thúc ca thi</button>
                        </form>
                    </c:if>
                </div>
            </div>

            <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                <h3 style="font-size: 1rem; font-weight: 800; color: #0f172a; margin: 0 0 1rem;">Ca thi trong ngày <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/></h3>
                <c:forEach var="ds" items="${daySessions}">
                    <span class="role-badge role-badge--coi" style="margin: 0 6px 6px 0; display: inline-block; font-size: 0.78rem; padding: 4px 10px; border-radius: 6px;">
                        ${ds.sessionLabel}
                        (<fmt:formatDate value="${ds.shiftStartTime}" pattern="HH:mm"/>–<fmt:formatDate value="${ds.shiftEndTime}" pattern="HH:mm"/>)
                        — ${ds.examTypeName}
                    </span>
                </c:forEach>
            </div>

            <div style="display: flex; gap: 1.5rem; flex-wrap: wrap; margin-top: 1.5rem;">
                <div class="report-pane" style="flex: 1; min-width: 280px; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                    <h3 style="font-size: 1rem; font-weight: 800; color: #16a34a; margin: 0 0 1rem;">Giám khảo khả dụng (${availableExaminers.size()})</h3>
                    <c:choose>
                        <c:when test="${empty availableExaminers}">
                            <p style="color: #94a3b8; font-size: 0.85rem;">Không còn giám khảo trống trong ngày này.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="ex" items="${availableExaminers}">
                                <span class="role-badge role-badge--coi" style="margin: 0 6px 6px 0; display: inline-block; font-size: 0.78rem; padding: 4px 10px; border-radius: 6px;">
                                    <c:choose>
                                        <c:when test="${not empty ex.profile.fullName}">${ex.profile.fullName}</c:when>
                                        <c:otherwise>${ex.username}</c:otherwise>
                                    </c:choose>
                                    (@${ex.username})
                                </span>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="report-pane" style="flex: 1; min-width: 280px; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                    <h3 style="font-size: 1rem; font-weight: 800; color: #b91c1c; margin: 0 0 1rem;">Giám khảo đã phân công (${busyExaminers.size()})</h3>
                    <c:choose>
                        <c:when test="${empty busyExaminers}">
                            <p style="color: #94a3b8; font-size: 0.85rem;">Chưa phân công giám khảo nào.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="ex" items="${busyExaminers}">
                                <span class="role-badge role-badge--admin" style="margin: 0 6px 6px 0; display: inline-block; font-size: 0.78rem; padding: 4px 10px; border-radius: 6px;">
                                    <c:choose>
                                        <c:when test="${not empty ex.profile.fullName}">${ex.profile.fullName}</c:when>
                                        <c:otherwise>${ex.username}</c:otherwise>
                                    </c:choose>
                                    (@${ex.username})
                                </span>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem;">Phân công mới</h3>
                <form class="examiner-assign-form" method="get" action="${ctx}/staff/examstaff/examiner-allocation" style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
                    <input type="hidden" name="sessionId" value="${currentSession.id}">
                    <input type="hidden" name="action" value="assign">
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label for="targetSessionId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Ca thi</label>
                        <select name="targetSessionId" id="targetSessionId" required class="es-session-selector__select es-session-selector__select--wide">
                            <c:forEach var="ds" items="${daySessions}">
                                <option value="${ds.id}">${ds.sessionLabel} (${ds.examTypeName})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label for="areaId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Phòng thi (Session_ExamArea)</label>
                        <select name="areaId" id="areaId" required class="es-session-selector__select es-session-selector__select--wide">
                            <c:forEach var="ds" items="${daySessions}">
                                <c:forEach var="ar" items="${areasBySession[ds.id]}">
                                    <option value="${ar.examAreaId}" data-session="${ds.id}" data-type="${ar.areaType}">
                                        ${ar.areaName} (${ar.areaType})
                                    </option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label for="examinerUserId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Giám khảo</label>
                        <select name="examinerUserId" id="examinerUserId" required class="es-session-selector__select es-session-selector__select--wide">
                            <c:forEach var="ex" items="${allExaminers}">
                                <option value="${ex.userId}">
                                    <c:choose>
                                        <c:when test="${not empty ex.profile.fullName}">${ex.profile.fullName}</c:when>
                                        <c:otherwise>${ex.username}</c:otherwise>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">&nbsp;</label>
                        <button type="submit" class="btn-export" style="height: 38px; padding: 0 1rem; border-radius: 8px; font-weight: 700;">Phân công</button>
                    </div>
                </form>
            </div>

            <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem;">Phân công ca đang chọn: ${currentSession.sessionLabel}</h3>
                <div class="table-responsive">
                    <table class="audit-table" style="font-size: 0.88rem;">
                        <thead>
                            <tr>
                                <th scope="col">Phòng thi</th>
                                <th scope="col">Loại thi</th>
                                <th scope="col">Giám khảo</th>
                                <th scope="col"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty sessionAssignments}">
                                    <tr><td colspan="4" style="text-align: center; padding: 2rem 1rem; color: #64748b;">Chưa có phân công cho ca này.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="a" items="${sessionAssignments}">
                                        <tr>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty a.areaName}">${a.areaName}<div class="area-type-tag" style="font-size: 0.72rem; color: #64748b;">${a.areaType}</div></c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${a.examTypeName}</td>
                                            <td>${a.examinerName} <span style="font-size: 0.78rem; color: #64748b;">(@${a.examinerUsername})</span></td>
                                            <td>
                                                <c:if test="${a.areaId > 0}">
                                                    <a class="btn-examiner-remove"
                                                       href="${ctx}/staff/examstaff/examiner-allocation?sessionId=${currentSession.id}&action=remove&slotKey=${a.examSessionId}:${a.areaId}:${a.examinerUserId}"
                                                       data-confirm-remove="true"
                                                       data-confirm-msg="Gỡ phân công giám khảo này?"
                                                       style="padding: 4px 10px; font-size: 0.78rem; border-radius: 6px; text-decoration: none; border: 1px solid rgba(185,28,28,0.25); color: #b91c1c; font-weight: 700;">
                                                        Gỡ
                                                    </a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
                <h3 style="font-size: 1rem; font-weight: 800; color: #2563eb; margin: 0 0 1rem;">Tổng hợp phân công cả ngày</h3>
                <div class="table-responsive">
                    <table class="audit-table" style="font-size: 0.88rem;">
                        <thead>
                            <tr>
                                <th scope="col">Ca thi</th>
                                <th scope="col">Phòng</th>
                                <th scope="col">Loại thi</th>
                                <th scope="col">Giám khảo</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty dayAssignments}">
                                    <tr><td colspan="4" style="text-align: center; padding: 2rem 1rem; color: #64748b;">Chưa có phân công trong ngày.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="a" items="${dayAssignments}">
                                        <tr>
                                            <td>${a.sessionLabel}</td>
                                            <td>${empty a.areaName ? '—' : a.areaName}</td>
                                            <td>${a.examTypeName}</td>
                                            <td>${a.examinerName}</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${ctx}/assets/js/examiner-allocation.js" charset="UTF-8"></script>
</body>
</html>
