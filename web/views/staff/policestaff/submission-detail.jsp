<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Thẩm định hồ sơ CSGT</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .header-actions{display:flex;justify-content:space-between;gap:1rem;align-items:flex-end}.header-actions a{text-decoration:none}
        .review-layout{display:grid;grid-template-columns:minmax(330px,37%) minmax(0,1fr);gap:1rem;align-items:start;margin-top:1rem}
        .review-card{background:#fff;border:1px solid #dbe3ef;border-radius:14px;padding:1.1rem}
        .candidate-pane{max-height:760px;overflow:auto}.candidate-pane h2{margin:0 0 .8rem}
        .review-table{width:100%;border-collapse:collapse}.review-table th,.review-table td{padding:.72rem .55rem;border-bottom:1px solid #e2e8f0;text-align:left;vertical-align:middle}.review-table th{position:sticky;top:0;background:#f8fafc;font-size:.75rem;color:#475569;z-index:1}.review-table tr.is-selected td{background:#eff6ff}.candidate-name{display:block;min-width:120px}.candidate-meta{display:block;color:#64748b;font-size:.76rem;margin-top:.25rem}.status{display:inline-flex;padding:.25rem .5rem;border-radius:999px;font-size:.75rem;font-weight:700;white-space:nowrap}.status.pending{background:#fef3c7;color:#92400e}.status.approved{background:#dcfce7;color:#166534}.status.rejected{background:#fee2e2;color:#b91c1c}
        .detail-head{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start;margin-bottom:.8rem}.detail-head h2{margin:0}.detail-head p{margin:.35rem 0 0;color:#64748b}
        .doc-tabs{display:flex;gap:.5rem;flex-wrap:wrap;margin:.8rem 0}.doc-tab{padding:.55rem .75rem;border:1px solid #cbd5e1;border-radius:9px;text-decoration:none;color:#0755b5;font-weight:700;font-size:.82rem}.doc-tab.is-active{background:#075bd8;color:#fff;border-color:#075bd8}
        .document-frame{width:100%;height:430px;border:1px solid #cbd5e1;border-radius:12px;background:#f8fafc}.empty-document{height:280px;display:grid;place-items:center;border:1px dashed #cbd5e1;border-radius:12px;background:#f8fafc;color:#64748b}
        .decision-box{margin-top:1rem;padding-top:1rem;border-top:1px solid #e2e8f0}.decision{display:grid;grid-template-columns:auto auto minmax(240px,1fr) auto;gap:.75rem;align-items:center}.decision textarea{min-height:78px;resize:vertical}.decision label{white-space:nowrap;font-weight:700}
        .alert{padding:.9rem;border-radius:10px;margin-top:1rem}.ok{background:#ecfdf5;color:#047857}.err{background:#fef2f2;color:#b91c1c}
        .pager{display:flex;justify-content:center;align-items:center;gap:.45rem;padding-top:1rem}.pager a,.pager span{padding:.42rem .68rem;border:1px solid #cbd5e1;border-radius:8px;text-decoration:none}.pager .is-current{background:#075bd8;color:#fff;border-color:#075bd8}
        @media(max-width:1050px){.review-layout{grid-template-columns:1fr}.candidate-pane{max-height:420px}.decision{grid-template-columns:1fr 1fr}.decision textarea,.decision button{grid-column:1/-1}.header-actions{align-items:flex-start;flex-direction:column}}
    </style>
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-policestaff.jsp">
    <jsp:param name="activeSidebar" value="dashboard"/>
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <div class="header-actions">
        <div>
            <p class="breadcrumb"><a href="${ctx}/police/dashboard">Tiếp nhận & thẩm định</a> / Chi tiết hồ sơ</p>
            <h1 class="page-title">Hồ sơ ngày <fmt:formatDate value="${submission.examDate}" pattern="dd/MM/yyyy"/> · Hạng ${submission.licenceClass}</h1>
            <p class="page-subtitle">${submission.approvedCandidates} đã duyệt · ${submission.rejectedCandidates} từ chối · ${submission.pendingCandidates} chờ xử lý</p>
        </div>
        <a class="btn-export" href="${ctx}/police/official-rosters?dateId=${submission.examDateId}">Mở danh sách thi chính thức</a>
    </div>

    <c:if test="${not empty policeSuccess}"><div class="alert ok"><c:out value="${policeSuccess}"/></div></c:if>
    <c:if test="${not empty policeError}"><div class="alert err"><c:out value="${policeError}"/></div></c:if>

    <c:set var="selectedCandidate" value="${null}"/>
    <c:forEach var="row" items="${candidates}">
        <c:if test="${param.candidate eq row.registrationDateId}"><c:set var="selectedCandidate" value="${row}"/></c:if>
    </c:forEach>

    <div class="review-layout">
        <section class="review-card candidate-pane">
            <h2>Danh sách hồ sơ</h2>
            <table class="review-table">
                <thead><tr><th>Mã</th><th>Thí sinh</th><th>Trạng thái</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="row" items="${candidates}">
                    <tr class="${param.candidate eq row.registrationDateId ? 'is-selected' : ''}">
                        <td>#${row.examRegistrationId}</td>
                        <td>
                            <strong class="candidate-name"><c:out value="${row.dossier.profile.fullName}"/></strong>
                            <span class="candidate-meta">CCCD: <c:out value="${row.dossier.profile.govIdNo}"/> · ${row.dossier.documentCount}/4 giấy tờ</span>
                        </td>
                        <td><span class="status ${row.approved ? 'approved' : (row.rejected ? 'rejected' : 'pending')}">${row.approved ? 'Đã duyệt' : (row.rejected ? 'Từ chối' : 'Chờ duyệt')}</span></td>
                        <td><a class="btn-export" href="${ctx}/police/submissions?dateId=${submission.examDateId}&amp;page=${page}&amp;candidate=${row.registrationDateId}">Xem</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <c:if test="${totalPages gt 1}">
                <nav class="pager" aria-label="Phân trang hồ sơ">
                    <c:if test="${page gt 1}"><a href="${ctx}/police/submissions?dateId=${submission.examDateId}&amp;page=${page - 1}">Trước</a></c:if>
                    <span class="is-current">${page}/${totalPages}</span>
                    <c:if test="${page lt totalPages}"><a href="${ctx}/police/submissions?dateId=${submission.examDateId}&amp;page=${page + 1}">Sau</a></c:if>
                </nav>
            </c:if>
        </section>

        <section class="review-card">
            <c:choose>
                <c:when test="${not empty selectedCandidate}">
                    <div class="detail-head">
                        <div>
                            <h2><c:out value="${selectedCandidate.dossier.profile.fullName}"/></h2>
                            <p>CCCD: <strong><c:out value="${selectedCandidate.dossier.profile.govIdNo}"/></strong> · Email: <c:out value="${selectedCandidate.dossier.user.email}"/></p>
                        </div>
                        <span class="status ${selectedCandidate.approved ? 'approved' : (selectedCandidate.rejected ? 'rejected' : 'pending')}">${selectedCandidate.approved ? 'Đã duyệt' : (selectedCandidate.rejected ? 'Từ chối' : 'Chờ duyệt')}</span>
                    </div>

                    <c:set var="selectedDocument" value="${null}"/>
                    <div class="doc-tabs">
                        <c:forEach var="entry" items="${selectedCandidate.dossier.documents}" varStatus="docLoop">
                            <c:if test="${empty selectedDocument and (empty param.documentId or param.documentId eq entry.value.documentId)}">
                                <c:set var="selectedDocument" value="${entry.value}"/>
                            </c:if>
                            <a class="doc-tab ${param.documentId eq entry.value.documentId or (empty param.documentId and docLoop.first) ? 'is-active' : ''}"
                               href="${ctx}/police/submissions?dateId=${submission.examDateId}&amp;page=${page}&amp;candidate=${selectedCandidate.registrationDateId}&amp;documentId=${entry.value.documentId}">
                                <c:out value="${entry.value.documentType}"/>
                            </a>
                        </c:forEach>
                    </div>

                    <c:choose>
                        <c:when test="${not empty selectedDocument}">
                            <iframe class="document-frame"
                                    title="Tài liệu ${selectedDocument.documentType}"
                                    src="${ctx}/police/document-view?id=${selectedDocument.documentId}"></iframe>
                        </c:when>
                        <c:otherwise><div class="empty-document">Hồ sơ chưa có tài liệu để xem.</div></c:otherwise>
                    </c:choose>

                    <div class="decision-box">
                        <c:if test="${selectedCandidate.pending and not submission.completed}">
                            <form class="decision" method="post" action="${ctx}/police/submissions">
                                <input type="hidden" name="dateId" value="${submission.examDateId}">
                                <input type="hidden" name="page" value="${page}">
                                <input type="hidden" name="registrationDateId" value="${selectedCandidate.registrationDateId}">
                                <label><input type="radio" name="decision" value="APPROVED" required> Duyệt hồ sơ</label>
                                <label><input type="radio" name="decision" value="REJECTED" required> Từ chối</label>
                                <textarea class="input-field" name="reason" maxlength="500" placeholder="Lý do bắt buộc khi từ chối"></textarea>
                                <button class="btn-filter" type="submit">Lưu thẩm định</button>
                            </form>
                        </c:if>
                        <c:if test="${selectedCandidate.approved}">
                            <div class="alert ok"><strong>Hồ sơ đủ điều kiện.</strong> Thí sinh sẽ được đưa vào danh sách chính thức khi CSGT ban hành.</div>
                        </c:if>
                        <c:if test="${selectedCandidate.rejected}">
                            <div class="alert err"><strong>Hồ sơ đã bị từ chối.</strong> Hồ sơ không được đưa vào danh sách chính thức.<br><strong>Lý do:</strong> <c:out value="${selectedCandidate.policeReason}"/></div>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-document">Chọn một hồ sơ bên trái để xem tài liệu và thẩm định ngay trên màn hình này.</div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false"/></jsp:include>
</div>
</body>
</html>
