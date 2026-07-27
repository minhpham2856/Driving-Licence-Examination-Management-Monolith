<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý kỳ thi - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .schedule-grid {
            display: grid;
            grid-template-columns: minmax(320px, 0.9fr) minmax(520px, 1.5fr);
            gap: 1.25rem;
            align-items: start;
        }

        .schedule-card {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
            padding: 1.25rem;
        }

        .schedule-card__title {
            margin: 0 0 .35rem;
            font-size: 1.05rem;
            color: #0f172a;
        }

        .schedule-card__hint {
            margin: 0 0 1rem;
            color: #64748b;
            font-size: .86rem;
            line-height: 1.5;
        }

        .schedule-form {
            display: grid;
            gap: .95rem;
        }

        .schedule-form__row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: .8rem;
        }

        .schedule-table-wrap {
            overflow-x: auto;
        }

        .schedule-table {
            width: 100%;
            border-collapse: collapse;
            font-size: .86rem;
        }

        .schedule-table th,
        .schedule-table td {
            padding: .75rem .65rem;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
            vertical-align: top;
        }

        .schedule-table th {
            color: #475569;
            background: #f8fafc;
            font-size: .75rem;
            text-transform: uppercase;
            letter-spacing: .04em;
        }

        .status-pill {
            display: inline-flex;
            padding: .25rem .55rem;
            border-radius: 999px;
            font-weight: 700;
            font-size: .74rem;
            background: #e0f2fe;
            color: #0369a1;
        }

        .status-pill--Open { background: #dcfce7; color: #166534; }
        .status-pill--Closed { background: #f1f5f9; color: #475569; }
        .status-pill--Cancelled { background: #fee2e2; color: #991b1b; }

        .status-form {
            display: flex;
            gap: .4rem;
            align-items: center;
        }

        .status-form select {
            min-width: 105px;
            height: 34px;
            padding: 0 .45rem;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            background: #fff;
        }
        .schedule-tabs{display:grid;grid-template-columns:repeat(4,1fr);gap:.65rem;margin:1.25rem 0}
        .schedule-tabs a{text-align:center;text-decoration:none;padding:.75rem;border:1px solid #cbd5e1;border-radius:10px;background:#fff;color:#475569;font-weight:700}
        .schedule-tabs a.is-active{background:#0052cc;color:#fff;border-color:#0052cc}
        .year-filter{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:0 0 1rem;padding:.8rem;background:#f8fafc;border-radius:10px}
        .schedule-table tbody tr{transition:background-color .18s ease}
        .schedule-table tbody tr.schedule-row-selected td{background:#eff6ff;border-bottom-color:#bfdbfe}
        .schedule-table tbody tr.schedule-row-selected td:first-child{box-shadow:inset 4px 0 #0b5ed7}
        .schedule-table tbody tr.schedule-row-selected strong{color:#0755b5}

        @media (max-width: 1100px) {
            .schedule-grid,
            .schedule-form__row {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="phien-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs">
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Quản lý kỳ thi</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản Lý Kỳ Thi</h1>
                <p class="page-subtitle">
                    Managing Staff tạo phiên thi từ danh sách chính thức đã nhận và quản lý trạng thái các phiên.
                </p>
            </div>
        </header>

        <c:if test="${not empty scheduleSuccess}">
            <div class="p-alert-banner" style="border-color:#10b981;color:#047857"><c:out value="${scheduleSuccess}" /></div>
        </c:if>
        <c:if test="${not empty scheduleError}">
            <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b"><c:out value="${scheduleError}" /></div>
        </c:if>

        <div class="schedule-grid" style="margin-top:1.25rem">
            <section class="schedule-card">
                <h2 class="schedule-card__title">${empty editingSession ? 'Tạo phiên thi mới' : 'Sửa phiên thi'}</h2>
                <p class="schedule-card__hint">
                    Ngày thi và hạng GPLX được lấy từ danh sách CSGT đã ban hành, không nhập hoặc thay đổi lại.
                </p>
                <form class="schedule-form" action="${ctx}/manager/exam-schedules" method="post">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="sessionId" value="${editingSession.id}">
                    <c:if test="${empty editingSession}">
                        <div class="input-group">
                            <label class="input-label">Danh sách CSGT đã ban hành</label>
                            <select class="input-field" id="officialRosterSelect" name="sourceExamDateId" required>
                                <option value="">Chọn danh sách chính thức</option>
                                <c:forEach var="source" items="${policeCompletedDates}">
                                    <fmt:formatDate var="sourceDate" value="${source.examDate}" pattern="dd/MM/yyyy"/>
                                    <option value="${source.id}"
                                            data-date="${sourceDate}"
                                            data-licence="${source.licenceClass}">
                                        ${sourceDate} · Hạng ${source.licenceClass} · ${source.officialCandidateCount} thí sinh chính thức
                                    </option>
                                </c:forEach>
                            </select>
                            <small style="color:#64748b">Hệ thống tự lấy ngày, hạng và toàn bộ thí sinh từ danh sách này.</small>
                        </div>
                        <div id="officialRosterSummary" class="schedule-form__row" style="display:none">
                            <div class="input-group">
                                <label class="input-label">Ngày thi chính thức</label>
                                <input id="officialExamDate" class="input-field" type="text" readonly>
                            </div>
                            <div class="input-group">
                                <label class="input-label">Hạng GPLX</label>
                                <input id="officialLicenceClass" class="input-field" type="text" readonly>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${not empty editingSession}">
                        <fmt:formatDate var="editingExamDate" value="${editingSession.examDate}" pattern="dd/MM/yyyy" />
                        <div class="schedule-form__row">
                            <div class="input-group">
                                <label class="input-label">Ngày thi chính thức</label>
                                <input class="input-field" type="text" readonly value="${editingExamDate}">
                            </div>
                            <div class="input-group">
                                <label class="input-label">Hạng GPLX</label>
                                <input class="input-field" type="text" readonly value="Hạng ${editingSession.licenseCode}">
                            </div>
                        </div>
                        <small style="color:#64748b">Ngày thi và hạng GPLX đã được CSGT ban hành nên không thể sửa.</small>
                    </c:if>
                    <div class="input-group"><label class="input-label">Trung tâm</label>
                        <input class="input-field" name="centreName" required minlength="3" value="<c:out value='${editingSession.centreName}' />"></div>
                    <fmt:formatDate var="editingStartTime" value="${editingSession.shiftStartTime}" pattern="HH:mm" />
                    <div class="input-group"><label class="input-label">Giờ bắt đầu</label>
                        <input class="input-field" type="time" name="startTime" required value="${editingStartTime}"></div>
                    <div class="btn-group"><button class="btn-filter" type="submit">${empty editingSession ? 'Tạo phiên thi' : 'Lưu thay đổi'}</button>
                        <c:if test="${not empty editingSession}"><a class="btn-reset" href="${ctx}/manager/exam-schedules">Hủy sửa</a></c:if></div>
                </form>
            </section>

            <section class="schedule-card">
                <h2 class="schedule-card__title">Danh sách phiên thi</h2>
                <p class="schedule-card__hint">
                    Hiển thị ${totalSessions} phiên trong nhóm đang chọn; dữ liệu được phân trang tại database.
                </p>
                <nav class="schedule-tabs" aria-label="Trạng thái phiên thi">
                    <a class="${activeTab eq 'upcoming' ? 'is-active' : ''}" href="${ctx}/manager/exam-schedules?tab=upcoming">Chưa thi (${upcomingCount})</a>
                    <a class="${activeTab eq 'ongoing' ? 'is-active' : ''}" href="${ctx}/manager/exam-schedules?tab=ongoing">Đang thi (${ongoingCount})</a>
                    <a class="${activeTab eq 'completed' ? 'is-active' : ''}" href="${ctx}/manager/exam-schedules?tab=completed">Đã thi (${completedCount})</a>
                    <a class="${activeTab eq 'cancelled' ? 'is-active' : ''}" href="${ctx}/manager/exam-schedules?tab=cancelled">Đã hủy (${cancelledCount})</a>
                </nav>
                <c:if test="${activeTab eq 'completed' or activeTab eq 'cancelled'}">
                    <form class="year-filter" method="get" action="${ctx}/manager/exam-schedules">
                        <input type="hidden" name="tab" value="${activeTab}">
                        <label for="scheduleYear"><strong>Lọc năm:</strong></label>
                        <select id="scheduleYear" name="year" class="input-field" style="width:150px;height:38px" onchange="this.form.submit()">
                            <option value="">Tất cả các năm</option>
                            <c:forEach var="y" items="${availableYears}">
                                <option value="${y}" ${selectedYears.contains(y) ? 'selected' : ''}>${y}</option>
                            </c:forEach>
                        </select>
                    </form>
                </c:if>

                <div class="schedule-table-wrap">
                    <table class="schedule-table">
                        <thead>
                        <tr>
                            <th>Phiên thi</th>
                            <th>Hạng</th>
                            <th>Thời gian</th>
                            <th>SL</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${sessions}">
                            <tr class="${not empty viewSession and viewSession.id eq s.id ? 'schedule-row-selected' : ''}">
                                <td>
                                    <strong><c:out value="${s.sessionName}" /></strong><br>
                                    <small style="color:#64748b"><c:out value="${s.examTypeName}" /></small>
                                </td>
                                <td><strong>Hạng <c:out value="${s.licenseCode}" /></strong></td>
                                <td>
                                    <fmt:formatDate value="${s.examDate}" pattern="dd/MM/yyyy" /><br><small><fmt:formatDate value="${s.shiftStartTime}" pattern="HH:mm" /></small>
                                </td>
                                <td>${s.registeredCount}</td>
                                <td>
                                    <span class="status-pill status-pill--${s.status}">
                                        <c:out value="${s.status}" />
                                    </span>
                                </td>
                                <td>
                                    <div style="display:flex;gap:.4rem;white-space:nowrap">
                                        <c:url var="candidateListUrl" value="/manager/exam-schedules">
                                            <c:param name="view" value="${s.id}" />
                                            <c:param name="tab" value="${activeTab}" />
                                            <c:param name="page" value="${currentPage}" />
                                            <c:forEach var="y" items="${selectedYears}">
                                                <c:param name="year" value="${y}" />
                                            </c:forEach>
                                        </c:url>
                                        <a class="${not empty viewSession and viewSession.id eq s.id ? 'btn-filter' : 'btn-export'}"
                                           href="${candidateListUrl}#candidate-list"
                                           aria-current="${not empty viewSession and viewSession.id eq s.id ? 'true' : 'false'}"
                                           style="padding:.4rem .55rem;text-decoration:none">${not empty viewSession and viewSession.id eq s.id ? 'Đang xem' : 'Danh sách'}</a>
                                        <c:if test="${s.editable}"><a class="btn-filter" href="${ctx}/manager/exam-schedules?edit=${s.id}" style="padding:.4rem .55rem;text-decoration:none">Sửa</a></c:if>
                                    </div>
                                    <c:if test="${s.editable}"><form action="${ctx}/manager/exam-schedules" method="post" style="margin-top:.45rem" onsubmit="return confirm('Xác nhận hủy phiên thi? Danh sách thí sinh vẫn được giữ lại.');">
                                        <input type="hidden" name="action" value="cancel"><input type="hidden" name="sessionId" value="${s.id}">
                                        <input name="reason" required minlength="5" placeholder="Lý do hủy" style="width:125px;height:30px"><button type="submit" class="btn-reset" style="height:30px;width:auto;padding:0 .45rem">Hủy phiên</button>
                                    </form></c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty sessions}">
                            <tr>
                                <td colspan="6" style="text-align:center;color:#64748b;padding:2rem">
                                    Chưa có phiên thi nào.
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
                <c:if test="${totalPages gt 1}"><nav class="pagination-nav" style="margin-top:1rem"><c:forEach var="p" begin="1" end="${totalPages}"><c:url var="pageUrl" value="/manager/exam-schedules"><c:param name="tab" value="${activeTab}"/><c:param name="page" value="${p}"/><c:forEach var="y" items="${selectedYears}"><c:param name="year" value="${y}"/></c:forEach></c:url><a class="page-btn ${p eq currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a></c:forEach></nav></c:if>
            </section>
        </div>

        <c:if test="${not empty viewSession}">
            <section id="candidate-list" class="schedule-card" style="margin-top:1.25rem;scroll-margin-top:1rem">
                <h2 class="schedule-card__title">Danh sách thí sinh · <c:out value="${viewSession.sessionName}" /></h2>
                <p class="schedule-card__hint">${viewSession.registeredCount} thí sinh · ${viewSession.status}</p>
                <div class="schedule-table-wrap"><table class="schedule-table"><thead><tr>
                    <th>SBD</th><th>Họ và tên</th><th>CCCD</th><th>Ngày sinh</th><th>Nội dung thi</th><th>Điện thoại</th><th>Email</th>
                </tr></thead><tbody>
                    <c:forEach var="c" items="${sessionCandidates}"><tr>
                        <td>${c.sbd}</td><td><strong><c:out value="${c.fullName}" /></strong></td><td><c:out value="${c.govIdNo}" /></td>
                        <td><fmt:formatDate value="${c.dateOfBirth}" pattern="dd/MM/yyyy" /></td><td><strong><c:out value="${c.examParticipationLabel}" /></strong></td><td><c:out value="${c.phoneNo}" /></td><td><c:out value="${c.email}" /></td>
                    </tr></c:forEach>
                    <c:if test="${empty sessionCandidates}"><tr><td colspan="7" style="text-align:center;padding:2rem;color:#64748b">Phiên thi chưa có thí sinh.</td></tr></c:if>
                </tbody></table></div>
            </section>
        </c:if>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script>
    (() => {
        const select = document.getElementById('officialRosterSelect');
        if (!select) return;

        const summary = document.getElementById('officialRosterSummary');
        const date = document.getElementById('officialExamDate');
        const licence = document.getElementById('officialLicenceClass');
        const syncOfficialRoster = () => {
            const option = select.options[select.selectedIndex];
            const selected = option && option.value;
            summary.style.display = selected ? 'grid' : 'none';
            date.value = selected ? option.dataset.date : '';
            licence.value = selected ? 'Hạng ' + option.dataset.licence : '';
        };

        select.addEventListener('change', syncOfficialRoster);
        syncOfficialRoster();
    })();
</script>
</body>
</html>
