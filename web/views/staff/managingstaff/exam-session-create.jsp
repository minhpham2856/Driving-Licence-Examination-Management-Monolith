<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="draft" value="${sessionScope.examSessionImportDraft}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import danh sách thí sinh - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .create-session-layout {
            display: grid;
            grid-template-columns: minmax(560px, 1.55fr) minmax(310px, .8fr);
            gap: 1.25rem;
            align-items: start;
        }
        .create-card {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 1.25rem;
            box-shadow: 0 8px 24px rgba(15, 23, 42, .05);
        }
        .create-card__title {
            margin: 0 0 .35rem;
            color: #0f172a;
            font-size: 1.05rem;
        }
        .create-card__hint {
            margin: 0 0 1rem;
            color: #64748b;
            font-size: .82rem;
            line-height: 1.55;
        }
        .session-form {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: .9rem 1rem;
        }
        .session-form .full-width { grid-column: 1 / -1; }
        .field-label {
            display: block;
            margin-bottom: .4rem;
            font-size: .78rem;
            font-weight: 800;
            color: #475569;
            text-transform: uppercase;
            letter-spacing: .025em;
        }
        .required { color: #dc2626; }
        .field-control {
            width: 100%;
            min-height: 42px;
            border: 1.5px solid #cbd5e1;
            border-radius: 8px;
            padding: 0 .75rem;
            background: #fff;
            color: #0f172a;
            font: inherit;
            font-size: .88rem;
            box-sizing: border-box;
        }
        .field-control:focus {
            border-color: #2563eb;
            outline: 3px solid rgba(37, 99, 235, .12);
        }
        .upload-box {
            border: 2px dashed #93c5fd;
            border-radius: 12px;
            padding: 1.15rem;
            background: #f8fbff;
        }
        .upload-box input { margin-top: .7rem; width: 100%; }
        .form-actions {
            display: flex;
            justify-content: flex-end;
            gap: .65rem;
            align-items: center;
        }
        .guide-list {
            margin: 0;
            padding-left: 1.15rem;
            color: #475569;
            font-size: .82rem;
            line-height: 1.65;
        }
        .guide-list strong { color: #0f172a; }
        .flow-step {
            display: flex;
            gap: .75rem;
            padding: .7rem 0;
            border-bottom: 1px solid #eef2f7;
            font-size: .82rem;
            color: #475569;
        }
        .flow-step:last-child { border-bottom: 0; }
        .flow-step__number {
            width: 24px;
            height: 24px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex: 0 0 24px;
            border-radius: 50%;
            background: #dbeafe;
            color: #1d4ed8;
            font-weight: 800;
        }
        .preview-summary {
            display: grid;
            grid-template-columns: repeat(5, minmax(130px, 1fr));
            gap: .75rem;
            margin: 1rem 0;
        }
        .summary-item {
            border: 1px solid #dbeafe;
            border-radius: 10px;
            padding: .75rem;
            background: #f8fbff;
        }
        .summary-label {
            display: block;
            color: #64748b;
            font-size: .7rem;
            text-transform: uppercase;
            font-weight: 800;
            margin-bottom: .25rem;
        }
        .summary-value { color: #0f172a; font-size: .86rem; font-weight: 750; }
        .alert-box {
            border-radius: 10px;
            padding: .8rem 1rem;
            margin-bottom: 1rem;
            font-size: .84rem;
            font-weight: 650;
        }
        .alert-box--error { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
        .alert-box--success { background: #ecfdf5; border: 1px solid #a7f3d0; color: #065f46; }
        .status-tag {
            display: inline-flex;
            border-radius: 999px;
            padding: .28rem .55rem;
            font-size: .7rem;
            font-weight: 800;
            line-height: 1.25;
        }
        .status-tag--valid { background: #dcfce7; color: #166534; }
        .status-tag--invalid { background: #fee2e2; color: #991b1b; border-radius: 8px; }
        .preview-table-wrap { overflow: auto; max-height: 520px; }
        .preview-table { width: 100%; border-collapse: collapse; font-size: .82rem; }
        .preview-table th, .preview-table td {
            padding: .7rem .6rem;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
            vertical-align: middle;
        }
        .preview-table th {
            position: sticky;
            top: 0;
            z-index: 1;
            background: #f8fafc;
            color: #475569;
            font-size: .7rem;
            text-transform: uppercase;
            letter-spacing: .025em;
        }
        .row-invalid { background: #fff7f7; }
        @media (max-width: 1100px) {
            .create-session-layout { grid-template-columns: 1fr; }
            .preview-summary { grid-template-columns: repeat(2, 1fr); }
        }
        @media (max-width: 680px) {
            .session-form { grid-template-columns: 1fr; }
            .session-form .full-width { grid-column: auto; }
            .preview-summary { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="import-thi-sinh" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${ctx}/manager/exam-schedules">Quản lý phiên thi</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Import danh sách</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Import danh sách thí sinh</h1>
                <p class="page-subtitle">
                    Chọn phiên thi đã tạo và đối soát danh sách Bộ Công an trả về với các hồ sơ đã được duyệt.
                </p>
            </div>
            <div class="page-actions" style="display:flex;gap:.6rem;flex-wrap:wrap">
                <a href="${ctx}/manager/exam-schedules/create?action=downloadTemplate" class="btn-export" style="text-decoration:none">
                    Tải CSV mẫu
                </a>
                <a href="${ctx}/manager/exam-schedules/create?action=downloadTestFile" class="btn-export" style="text-decoration:none;color:#047857;border-color:#10b981">
                    Tải CSV test A1
                </a>
            </div>
        </header>

        <c:if test="${not empty uploadError}">
            <div class="alert-box alert-box--error"><c:out value="${uploadError}" /></div>
        </c:if>
        <c:if test="${not empty uploadSuccess}">
            <div class="alert-box alert-box--success"><c:out value="${uploadSuccess}" /></div>
        </c:if>

        <c:if test="${param.importSuccess eq 'true'}">
            <section class="create-card" style="margin-bottom:1.25rem;border-color:#86efac">
                <h2 class="create-card__title" style="color:#047857">Đã import danh sách thành công</h2>
                <p class="create-card__hint" style="margin-bottom:.8rem">
                    Phiên <strong><c:out value="${sessionScope.createdSessionName}" /></strong>
                    (Session #${sessionScope.createdSessionId}) đã nhận
                    <strong>${sessionScope.importedCount} thí sinh</strong>. Các thí sinh đang ở trạng thái
                    <strong>Pending - có trong danh sách chính thức, chưa điểm danh</strong>.
                </p>
                <a class="btn-filter" href="${ctx}/manager/exam-schedules"
                   style="text-decoration:none;display:inline-flex">Xem danh sách phiên thi</a>
            </section>
        </c:if>

        <div class="create-session-layout" id="importFormLayout">
            <section class="create-card">
                <h2 class="create-card__title">1. Chọn phiên thi và tệp danh sách</h2>
                <p class="create-card__hint">
                    Phiên thi phải được tạo trước trong màn hình Quản lý phiên thi. Import chỉ bổ sung danh sách thí sinh vào phiên đã chọn.
                </p>

                <form id="uploadForm" class="session-form"
                      action="${ctx}/manager/exam-schedules/create" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="preview">

                    <div class="full-width">
                        <label class="field-label" for="sessionId">Phiên thi chưa diễn ra <span class="required">*</span></label>
                        <select class="field-control" id="sessionId" name="sessionId" required>
                            <option value="">Chọn phiên thi</option>
                            <c:forEach var="s" items="${importSessions}"><c:if test="${s.editable}">
                                <option value="${s.id}" ${draft.examId eq s.id ? 'selected' : ''}><c:out value="${s.sessionName}" /> · Hạng ${s.licenseCode} · <fmt:formatDate value="${s.examDate}" pattern="dd/MM/yyyy" /></option>
                            </c:if></c:forEach>
                        </select>
                    </div>

                    <div class="full-width upload-box upload-dropzone-container">
                        <strong id="dropzoneLabel" style="display:block;color:#1e3a8a;font-size:.9rem">
                            Chọn tệp CSV danh sách chính thức
                        </strong>
                        <span style="display:block;color:#64748b;font-size:.78rem;margin-top:.3rem">
                            Tệp UTF-8, đúng 7 cột, tối đa 15 MB. SBD trong tệp sẽ được giữ làm SBD chính thức.
                        </span>
                        <input id="fileInput" name="fileInput" type="file" accept=".csv,.txt" required>
                    </div>

                </form>
            </section>

            <aside style="display:grid;gap:1rem">
                <section class="create-card">
                    <h2 class="create-card__title">Quy cách CSV</h2>
                    <ol class="guide-list">
                        <li><strong>Số báo danh</strong> - bắt buộc, không trùng.</li>
                        <li><strong>Họ và tên</strong> - bắt buộc.</li>
                        <li><strong>Ngày sinh</strong> - DD/MM/YYYY.</li>
                        <li><strong>CCCD</strong> - đúng 12 chữ số.</li>
                        <li><strong>Hạng GPLX</strong> - A1, A hoặc B1; hệ thống gán hạng này vào hồ sơ khi import.</li>
                        <li><strong>Số điện thoại</strong> - bắt buộc, gồm 10 chữ số và bắt đầu bằng 0.</li>
                        <li><strong>Email</strong> - bắt buộc, đúng định dạng email.</li>
                    </ol>
                    <p class="create-card__hint" style="margin-top:.85rem;margin-bottom:0">
                        Hệ thống không tạo tài khoản mới từ CSV. Dòng không khớp hồ sơ đã duyệt sẽ được báo lỗi để xử lý lại.
                        Khi chỉnh bằng Excel, hãy đặt cột SBD, CCCD và số điện thoại ở định dạng <strong>Text</strong> để giữ số 0 đầu.
                    </p>
                </section>
            </aside>
        </div>

        <c:if test="${param.preview eq 'true' and not empty sessionScope.previewCandidates}">
            <section class="create-card" id="importPreview" style="margin-bottom:1.25rem">
                <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:1rem;flex-wrap:wrap">
                    <div>
                        <h2 class="create-card__title">2. Xem trước và xác nhận import</h2>
                        <p class="create-card__hint" style="margin-bottom:0">
                            Chỉ có thể xác nhận khi toàn bộ CCCD thuộc hồ sơ đã duyệt và dữ liệu không bị trùng.
                        </p>
                    </div>
                    <div style="display:flex;gap:.6rem">
                        <a href="${ctx}/manager/exam-schedules/create?action=cancel" class="btn-reset"
                           style="text-decoration:none;display:inline-flex;align-items:center">Hủy bản xem trước</a>
                        <form action="${ctx}/manager/exam-schedules/create" method="post" style="margin:0">
                            <input type="hidden" name="action" value="confirm">
                            <button type="submit" class="btn-filter"
                                    ${sessionScope.hasInvalidRows ? 'disabled' : ''}
                                    style="height:38px;${sessionScope.hasInvalidRows ? 'background:#cbd5e1;border-color:#cbd5e1;cursor:not-allowed' : 'background:#059669;border-color:#059669'}">
                                Xác nhận import ${sessionScope.validCandidateCount} thí sinh
                            </button>
                        </form>
                    </div>
                </div>

                <div class="preview-summary">
                    <div class="summary-item"><span class="summary-label">Tên phiên</span><span class="summary-value"><c:out value="${draft.sessionName}" /></span></div>
                    <div class="summary-item"><span class="summary-label">Hạng / phần thi</span><span class="summary-value">${draft.licenceClass} - <c:out value="${draft.sectionName}" /></span></div>
                    <div class="summary-item"><span class="summary-label">Ngày giờ</span><span class="summary-value">${draft.examDateValue}, ${draft.startTimeValue}-${draft.endTimeValue}</span></div>
                    <div class="summary-item"><span class="summary-label">Đối soát</span><span class="summary-value">${sessionScope.validCandidateCount}/${fn:length(sessionScope.previewCandidates)} hợp lệ</span></div>
                </div>

                <c:if test="${sessionScope.hasInvalidRows}">
                    <div class="alert-box alert-box--error">
                        Có thí sinh chưa đối soát được với hồ sơ đã duyệt. Hãy sửa tệp nguồn rồi chọn lại tệp.
                    </div>
                </c:if>

                <div class="preview-table-wrap">
                    <table class="preview-table">
                        <thead>
                        <tr>
                            <th>SBD chính thức</th>
                            <th>Họ và tên</th>
                            <th>Ngày sinh</th>
                            <th>CCCD</th>
                            <th>Hạng</th>
                            <th>Liên hệ</th>
                            <th>Kết quả đối soát</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="candidate" items="${sessionScope.previewCandidates}">
                            <tr class="${candidate.invalid ? 'row-invalid' : ''}">
                                <td style="font-family:monospace;font-weight:800;color:#1d4ed8"><c:out value="${candidate.sbd}" /></td>
                                <td style="font-weight:700"><c:out value="${candidate.fullName}" /></td>
                                <td><fmt:formatDate value="${candidate.dateOfBirth}" pattern="dd/MM/yyyy" /></td>
                                <td style="font-family:monospace"><c:out value="${candidate.govIdNo}" /></td>
                                <td><strong><c:out value="${candidate.licenseCode}" /></strong></td>
                                <td>
                                    <c:out value="${empty candidate.phoneNo ? '-' : candidate.phoneNo}" /><br>
                                    <small style="color:#64748b"><c:out value="${empty candidate.email ? '-' : candidate.email}" /></small>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${candidate.invalid}">
                                            <span class="status-tag status-tag--invalid"><c:out value="${candidate.validationMessage}" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-tag status-tag--valid">Khớp hồ sơ Approved</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:if>
    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${ctx}/assets/js/upload.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const preview = document.getElementById('importPreview');
        const formLayout = document.getElementById('importFormLayout');
        if (preview && formLayout && formLayout.parentNode) {
            formLayout.parentNode.insertBefore(preview, formLayout);
        }
    });
</script>
</body>
</html>
