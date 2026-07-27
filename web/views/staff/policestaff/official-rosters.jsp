<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Danh sách thi chính thức</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .roster-tabs{display:flex;gap:.65rem;margin:1rem 0}.roster-tabs a{flex:1;text-align:center;text-decoration:none;padding:.75rem;border:1px solid #cbd5e1;border-radius:10px;color:#334155;font-weight:700}.roster-tabs a.is-active{background:#075bd8;color:#fff;border-color:#075bd8}
        .roster-grid{display:grid;grid-template-columns:285px minmax(0,1fr);gap:1rem;align-items:start}.card{background:#fff;border:1px solid #dbe3ef;border-radius:14px;padding:1.2rem}
        .submission-list{display:grid;gap:.65rem}.submission-item{display:block;padding:.9rem;border:1px solid #dbe3ef;border-radius:10px;color:#0f172a;text-decoration:none}.submission-item.is-active{background:#eff6ff;border-color:#60a5fa;box-shadow:inset 4px 0 #0b5ed7}.submission-item small{display:block;margin-top:.35rem;color:#64748b}
        .summary{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:.65rem;margin:1rem 0}.summary div{padding:.9rem;background:#f8fafc;border-radius:10px;color:#475569}.summary strong{display:block;font-size:1.3rem;color:#0f172a}
        .table{width:100%;border-collapse:collapse}.table th,.table td{padding:.8rem;border-bottom:1px solid #e2e8f0;text-align:left}.table th{background:#f8fafc;font-size:.78rem;color:#475569}.table th:nth-child(2),.table td:nth-child(2){min-width:145px}.table th:nth-child(6),.table td:nth-child(6){min-width:190px}
        .view-dossier-link{display:inline-block;margin-top:.35rem;color:#075bd8;font-size:.78rem;font-weight:700;text-decoration:none}.view-dossier-link:hover{text-decoration:underline}
        .alert,.scope-note{padding:.9rem;border-radius:10px;margin-bottom:1rem}.ok{background:#ecfdf5;color:#047857}.err{background:#fef2f2;color:#b91c1c}.scope-note{background:#eff6ff;color:#1e40af}
        .publish{display:flex;justify-content:space-between;gap:1rem;align-items:center;margin-top:1rem;padding-top:1rem;border-top:1px solid #e2e8f0}.badge{display:inline-flex;padding:.25rem .55rem;border-radius:999px;font-size:.78rem;font-weight:700}.pending{background:#fef3c7;color:#92400e}.done{background:#dcfce7;color:#166534}
        .publish button:disabled{opacity:.5;cursor:not-allowed;box-shadow:none}
        .year-filter{display:grid;gap:.4rem;margin-top:1rem;padding-top:1rem;border-top:1px solid #e2e8f0}.year-filter label{font-size:.78rem;color:#475569}.year-filter select{width:100%;min-height:36px;padding:.4rem .55rem;font-size:.85rem}
        .pager{display:flex;justify-content:center;align-items:center;gap:.45rem;padding-top:1rem}.pager a,.pager span{padding:.42rem .68rem;border:1px solid #cbd5e1;border-radius:8px;text-decoration:none}.pager .is-current{background:#075bd8;color:#fff;border-color:#075bd8}
        @media(max-width:1050px){.roster-grid{grid-template-columns:1fr}.summary{grid-template-columns:repeat(2,minmax(0,1fr))}.publish{align-items:flex-start;flex-direction:column}}
    </style>
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-policestaff.jsp">
    <jsp:param name="activeSidebar" value="rosters"/>
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <p class="breadcrumb">Cổng CSGT / Danh sách thi chính thức</p>
    <h1 class="page-title">Danh sách thi chính thức</h1>
    <p class="page-subtitle">Chỉ tổng hợp các thí sinh đã có tài khoản, hồ sơ và đăng ký trên hệ thống Lái Vui.</p>

    <c:if test="${not empty rosterSuccess}"><div class="alert ok"><c:out value="${rosterSuccess}"/></div></c:if>
    <c:if test="${not empty rosterError}"><div class="alert err"><c:out value="${rosterError}"/></div></c:if>

    <nav class="roster-tabs" aria-label="Lọc danh sách chính thức">
        <a class="${activeStatus eq 'pending' ? 'is-active' : ''}" href="${ctx}/police/official-rosters?status=pending">Chờ ban hành (${pendingRosterCount})</a>
        <a class="${activeStatus eq 'completed' ? 'is-active' : ''}" href="${ctx}/police/official-rosters?status=completed">Đã ban hành (${completedRosterCount})</a>
    </nav>
    <div class="roster-grid">
        <aside class="card">
            <h2 style="margin-top:0">${activeStatus eq 'completed' ? 'Danh sách đã ban hành' : 'Danh sách chờ ban hành'}</h2>
            <c:if test="${activeStatus eq 'completed'}">
                <form class="year-filter" method="get" action="${ctx}/police/official-rosters"
                      style="margin-top:0;margin-bottom:1rem;padding-top:0;padding-bottom:1rem;border-top:0;border-bottom:1px solid #e2e8f0">
                    <input type="hidden" name="status" value="completed">
                    <label for="year"><strong>Lọc theo năm</strong></label>
                    <select class="input-field" id="year" name="year" onchange="this.form.submit()">
                        <option value="">Tất cả các năm</option>
                        <c:forEach var="reportYear" items="${completedYears}">
                            <option value="${reportYear}" ${selectedYear eq reportYear ? 'selected' : ''}>Năm ${reportYear}</option>
                        </c:forEach>
                    </select>
                </form>
            </c:if>
            <div class="submission-list">
                <c:forEach var="row" items="${submissions}">
                    <a class="submission-item ${not empty selected and selected.examDateId eq row.examDateId ? 'is-active' : ''}"
                       href="${ctx}/police/official-rosters?status=${activeStatus}&amp;year=${selectedYear}&amp;page=${page}&amp;dateId=${row.examDateId}">
                        <strong><fmt:formatDate value="${row.examDate}" pattern="dd/MM/yyyy"/> · Hạng ${row.licenceClass}</strong>
                        <small>${row.approvedCandidates} đã duyệt · ${row.pendingCandidates} chờ xử lý</small>
                    </a>
                </c:forEach>
                <c:if test="${empty submissions}"><p style="color:#64748b">Không có danh sách trong nhóm này.</p></c:if>
            </div>
            <c:if test="${totalPages gt 1}">
                <nav class="pager" aria-label="Phân trang danh sách">
                    <c:if test="${page gt 1}"><a href="${ctx}/police/official-rosters?status=${activeStatus}&amp;year=${selectedYear}&amp;page=${page - 1}">Trước</a></c:if>
                    <span class="is-current">${page}/${totalPages}</span>
                    <c:if test="${page lt totalPages}"><a href="${ctx}/police/official-rosters?status=${activeStatus}&amp;year=${selectedYear}&amp;page=${page + 1}">Sau</a></c:if>
                </nav>
            </c:if>
        </aside>

        <section class="card">
            <c:choose>
                <c:when test="${not empty selected}">
                    <div style="display:flex;justify-content:space-between;gap:1rem;align-items:start">
                        <div>
                            <h2 style="margin:0">Ngày <fmt:formatDate value="${selected.examDate}" pattern="dd/MM/yyyy"/> · Hạng ${selected.licenceClass}</h2>
                            <p style="color:#64748b">Danh sách #${selected.examDateId}</p>
                        </div>
                        <span class="badge ${selected.completed ? 'done' : 'pending'}">${selected.completed ? 'Đã ban hành' : 'Chờ ban hành'}</span>
                    </div>

                    <div class="summary">
                        <div><strong>${selected.totalCandidates}</strong>Hồ sơ gửi lên</div>
                        <div><strong>${selected.approvedCandidates}</strong>Đủ điều kiện</div>
                        <div><strong>${selected.rejectedCandidates}</strong>Bị từ chối</div>
                        <div><strong>${selected.pendingCandidates}</strong>Chờ thẩm định</div>
                    </div>

                    <div class="scope-note"><strong>Phạm vi danh sách:</strong> chỉ gồm hồ sơ trên hệ thống đã được CSGT duyệt. Không thêm thí sinh ngoài hệ thống và không cần import lại Excel.</div>

                    <div style="overflow:auto">
                        <table class="table">
                            <thead><tr><th>SBD</th><th>Họ tên</th><th>Ngày sinh</th><th>CCCD</th><th>Hạng</th><th>Nội dung thi</th><th>Liên hệ</th></tr></thead>
                            <tbody>
                            <c:forEach var="row" items="${officialCandidates}">
                                <tr>
                                    <td>${empty row.candidateNumber ? 'Chờ cấp' : row.candidateNumber}</td>
                                    <td>
                                        <strong><c:out value="${row.fullName}"/></strong>
                                        <c:if test="${not empty row.registrationDateId}">
                                            <br><a class="view-dossier-link"
                                               href="${ctx}/police/submissions?dateId=${selected.examDateId}&amp;candidate=${row.registrationDateId}">Xem hồ sơ</a>
                                        </c:if>
                                    </td>
                                    <td><fmt:formatDate value="${row.dateOfBirth}" pattern="dd/MM/yyyy"/></td>
                                    <td><c:out value="${row.governmentIdNumber}"/></td>
                                    <td>${row.licenceClass}</td>
                                    <td><strong><c:out value="${row.examParticipationLabel}"/></strong></td>
                                    <td><c:out value="${row.phoneNumber}"/><br><small><c:out value="${row.email}"/></small></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty officialCandidates}">
                                <tr><td colspan="7" style="text-align:center;color:#64748b;padding:2rem">Chưa có hồ sơ nào được CSGT duyệt.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${candidatePages gt 1}">
                        <nav class="pager" aria-label="Phân trang thí sinh">
                            <c:if test="${candidatePage gt 1}"><a href="${ctx}/police/official-rosters?status=${activeStatus}&amp;year=${selectedYear}&amp;page=${page}&amp;dateId=${selected.examDateId}&amp;candidatePage=${candidatePage - 1}">Trước</a></c:if>
                            <span class="is-current">${candidatePage}/${candidatePages}</span>
                            <c:if test="${candidatePage lt candidatePages}"><a href="${ctx}/police/official-rosters?status=${activeStatus}&amp;year=${selectedYear}&amp;page=${page}&amp;dateId=${selected.examDateId}&amp;candidatePage=${candidatePage + 1}">Sau</a></c:if>
                        </nav>
                    </c:if>

                    <div class="publish">
                        <div>
                            <strong>${selected.completed ? 'Danh sách đã được gửi về trung tâm.' : 'Ban hành danh sách'}</strong><br>
                            <span style="color:#64748b">${selected.completed ? 'Trung tâm có thể tạo phiên thi chính thức từ dữ liệu này.' : 'Hệ thống cấp số báo danh và gửi email cho trung tâm cùng thí sinh.'}</span>
                        </div>
                        <c:choose>
                            <c:when test="${selected.completed}">
                                <a class="btn-filter" href="${ctx}/police/official-rosters?status=completed&amp;year=${selectedYear}&amp;dateId=${selected.examDateId}&amp;export=csv" style="text-decoration:none">Tải CSV đối soát</a>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/police/official-rosters"
                                      onsubmit="return confirm('Ban hành danh sách chính thức? Sau bước này không thể sửa.');">
                                    <input type="hidden" name="action" value="complete">
                                    <input type="hidden" name="dateId" value="${selected.examDateId}">
                                    <button class="btn-filter" type="submit"
                                            ${selected.pendingCandidates gt 0 or selected.approvedCandidates eq 0 ? 'disabled' : ''}>Ban hành danh sách</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:when>
                <c:otherwise><p style="color:#64748b">Chưa có danh sách để hiển thị.</p></c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false"/></jsp:include>
</div>
</body>
</html>
