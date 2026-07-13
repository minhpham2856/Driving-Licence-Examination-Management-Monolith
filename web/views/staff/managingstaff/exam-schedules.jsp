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
    <title>Quản lý phiên thi - Lái Vui</title>
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
            <span class="breadcrumbs__current">Quản lý phiên thi</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản Lý Phiên Thi</h1>
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
                <h2 class="schedule-card__title">Tạo phiên thi mới</h2>
                <p class="schedule-card__hint">
                    Tạo phiên thi từ danh sách chính thức và đối soát từng thí sinh với hồ sơ đã được Managing Staff duyệt.
                </p>
                <ul class="schedule-card__hint" style="padding-left:1.1rem;line-height:1.75">
                    <li>Chỉ nhận hạng A1, A và B1.</li>
                    <li>Kiểm tra CCCD, hạng GPLX, SBD và dữ liệu trùng.</li>
                    <li>Chỉ lưu khi toàn bộ danh sách hợp lệ.</li>
                </ul>
                <a class="btn-filter" href="${ctx}/manager/exam-schedules/create"
                   style="height:42px;justify-content:center;text-decoration:none;display:flex">
                    Tạo phiên thi và nhập danh sách
                </a>
            </section>

            <section class="schedule-card">
                <h2 class="schedule-card__title">Danh sách phiên thi</h2>
                <p class="schedule-card__hint">
                    Phiên mới được tạo ở trạng thái `Scheduled`; ExamStaff tiếp nhận và vận hành phiên theo danh sách đã xếp.
                </p>

                <div class="schedule-table-wrap">
                    <table class="schedule-table">
                        <thead>
                        <tr>
                            <th>Phiên thi</th>
                            <th>Hạng</th>
                            <th>Thời gian</th>
                            <th>Khu vực</th>
                            <th>SL</th>
                            <th>Trạng thái</th>
                            <th>Cập nhật</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${sessions}">
                            <tr>
                                <td>
                                    <strong><c:out value="${s.sessionName}" /></strong><br>
                                    <small style="color:#64748b"><c:out value="${s.examTypeName}" /></small>
                                </td>
                                <td><strong>Hạng <c:out value="${s.licenseCode}" /></strong></td>
                                <td>
                                    <fmt:formatDate value="${s.examDate}" pattern="dd/MM/yyyy" /><br>
                                    <small style="color:#64748b">
                                        <fmt:formatDate value="${s.shiftStartTime}" pattern="HH:mm" />
                                        -
                                        <fmt:formatDate value="${s.shiftEndTime}" pattern="HH:mm" />
                                    </small>
                                </td>
                                <td><c:out value="${empty s.areaName ? 'Chưa gán khu vực' : s.areaName}" /></td>
                                <td>${s.registeredCount}/${s.maxCandidates}</td>
                                <td>
                                    <span class="status-pill status-pill--${s.status}">
                                        <c:out value="${s.status}" />
                                    </span>
                                </td>
                                <td>
                                    <form class="status-form" action="${ctx}/manager/exam-schedules" method="post">
                                        <input type="hidden" name="action" value="status">
                                        <input type="hidden" name="sessionId" value="${s.id}">
                                        <select name="status">
                                            <option value="Scheduled" ${s.status eq 'Scheduled' ? 'selected' : ''}>Scheduled</option>
                                            <option value="Open" ${s.status eq 'Open' ? 'selected' : ''}>Open</option>
                                            <option value="Closed" ${s.status eq 'Closed' ? 'selected' : ''}>Closed</option>
                                            <option value="Cancelled" ${s.status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                        </select>
                                        <button class="btn-reset" type="submit" style="height:34px;width:auto;padding:0 .7rem">Lưu</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty sessions}">
                            <tr>
                                <td colspan="7" style="text-align:center;color:#64748b;padding:2rem">
                                    Chưa có phiên thi nào.
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
