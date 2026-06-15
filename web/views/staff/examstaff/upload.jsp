<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tải Danh Sách Thí Sinh - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="tai-ds" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tải danh sách thi</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhập danh sách & Sinh số báo danh</h1>
                <p class="page-subtitle">Tải lên danh sách học viên từ file Excel/CSV được trích xuất từ hệ thống ngoài PC08 và tự động cấp Số Báo Danh (SBD).</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="${pageContext.request.contextPath}/views/staff/examstaff/upload?action=downloadTemplate" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #0052cc; border-color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tải CSV Mẫu (.csv)
                </a>
                <a href="${pageContext.request.contextPath}/views/staff/examstaff/upload?action=downloadTestFile" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #10b981; border-color: #10b981; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tải File Test Mẫu (.csv)
                </a>
            </div>
        </header>

        <!-- Exception 1.0.E1 Alert: Invalid file or corrupted structure -->
        <c:if test="${not empty sessionScope.uploadError}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated shake">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 600; color: #b91c1c;">
                    Invalid file format or structure does not match PC08 standards. Chi tiết: ${sessionScope.uploadError}
                </span>
            </div>
            <% session.removeAttribute("uploadError"); %>
        </c:if>

        <!-- Layout for Dropzone and Guidelines -->
        <div class="report-grid" style="grid-template-columns: 1.2fr 1fr; gap: 1.5rem;">
            
            <!-- Left Pane: File Upload Form -->
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: center; gap: 1rem;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem;">Tải tệp dữ liệu lên</h2>
                </div>
                
                <!-- Normal Flow: upload file → parse → preview -->
                <form id="uploadForm" action="upload" method="POST" enctype="multipart/form-data"
                      style="display: flex; flex-direction: column; gap: 1.25rem; width: 100%;">
                    
                    <!-- Target exam selector (đồng bộ với phân bổ / phân bổ giám khảo) -->
                    <div style="display: flex; flex-direction: column; gap: 6px; text-align: left;">
                        <label for="examSessionId" style="font-size: 0.82rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Chọn kỳ thi (hạng / ngày):</label>
                        <select id="examSessionId" name="examSessionId" style="height: 42px; padding: 0 10px; border-radius: 8px; border: 1.5px solid #cbd5e1; font-weight: 600; color: #1e293b; outline: none; width: 100%; background: #ffffff; cursor: pointer;">
                            <c:forEach var="exam" items="${requestScope.examOptions}">
                                <option value="${exam.id}" ${requestScope.selectedImportExamId eq exam.examId ? 'selected' : ''}>
                                    Kỳ thi hạng ${exam.licenseCode} — <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/> (${exam.status})
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty requestScope.importExamLicense}">
                            <span style="font-size: 0.75rem; color: #1d4ed8; font-weight: 600;">
                                Hạng bằng kỳ thi: <strong>${requestScope.importExamLicense}</strong> — CSV phải khớp hạng này (B/B1/B2 được coi cùng nhóm).
                            </span>
                        </c:if>
                    </div>

                    <div class="upload-dropzone-container">
                        <div class="dropzone-icon" style="margin-bottom: 1rem;">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <span id="dropzoneLabel" style="font-size: 0.95rem; font-weight: 700; color: #0f172a; display: block; margin-bottom: 0.25rem;">
                            Kéo thả tệp CSV danh sách PC08 vào đây hoặc click để chọn tệp...
                        </span>
                        <span style="font-size: 0.78rem; color: #64748b; display: block; margin-bottom: 1rem;">Chấp nhận file định dạng .csv hoặc .txt (Tối đa 15MB)</span>
                        
                        <!-- Chọn file → tự động submit POST để parse và hiện preview -->
                        <input type="file" id="fileInput" name="fileInput" class="upload-file-input" accept=".csv,.txt">
                    </div>

                    <div style="display: flex; align-items: center; gap: 8px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 10px 14px;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #3b82f6; flex-shrink: 0;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span style="font-size: 0.78rem; font-weight: 600; color: #1d4ed8;">
                            Chọn file → Hệ thống phân tích và hiện bảng xem trước → Xác nhận mới lưu vào CSDL.
                        </span>
                    </div>
                </form>
            </div>
            
            <!-- Right Pane: Formatting rules & guide -->
            <div class="report-pane rule-card">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0.75rem;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 6px; color: #0f172a;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Quy cách dữ liệu danh sách thi (PC08)
                    </h2>
                </div>
                
                <p style="font-size: 0.82rem; color: #475569; margin-bottom: 1rem; line-height: 1.5;">Tệp CSV UTF-8, phân tách dấu phẩy, <strong>đủ 7 cột bắt buộc</strong>. Hạng GPLX (cột 5) phải khớp hạng kỳ thi đã chọn.</p>
                
                <div style="display: flex; flex-direction: column;">
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 1</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">SBD cũ / ID đăng ký:</strong> Mã từ PC08 (Bắt buộc, không được trống).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 2</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Họ và tên:</strong> Họ tên đầy đủ (Bắt buộc).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 3</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Ngày sinh:</strong> DD/MM/YYYY (Bắt buộc, đúng định dạng).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 4</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Số định danh / CCCD:</strong> Mã 12 chữ số định danh duy nhất (Bắt buộc).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 5</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Hạng GPLX:</strong> Phải khớp hạng kỳ thi (A1↔A1, B/B1/B2↔B,...).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 6</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Số điện thoại:</strong> Số điện thoại thí sinh (Bắt buộc).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 7</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Email:</strong> Hòm thư điện tử thí sinh (Bắt buộc).
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Preview table ( Normal Flow 5) -->
        <c:if test="${param.preview eq 'true' and not empty sessionScope.previewCandidates}">
            <div class="preview-table-card animated fadeIn" style="margin-top: 1.5rem;">
                <form action="upload" method="GET" style="margin: 0;">
                    <input type="hidden" name="action" value="save">

                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; margin-bottom: 1.25rem;">
                        <div>
                            <h2 style="font-size: 1.05rem; font-weight: 700; color: #10b981; display: inline-flex; align-items: center; gap: 8px; margin: 0;">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Bảng xem trước (${fn:length(sessionScope.previewCandidates)} thí sinh)
                            </h2>
                            <p style="font-size: 0.8rem; color: #64748b; margin-top: 4px; margin-bottom: 0;">
                                Kỳ thi: hạng <strong>${sessionScope.selectedImportExamLicense}</strong>.
                                Chỉ lưu dòng khớp hạng kỳ thi và đủ 7 trường.
                            </p>
                        </div>
                        <div style="display: flex; gap: 10px;">
                            <a href="upload" class="btn-reset" style="height: 38px; padding: 0 1rem; font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; color: #475569;">Hủy bỏ</a>
                            <c:choose>
                                <c:when test="${sessionScope.hasInvalidRows eq true}">
                                    <button type="button" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #cbd5e1; border-color: #cbd5e1; color: #64748b; cursor: not-allowed; display: inline-flex; align-items: center; justify-content: center;" disabled>
                                        ⚠️ Khóa (File có lỗi dữ liệu)
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <button type="submit" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #10b981; border-color: #10b981; color: #ffffff; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;">
                                        Xác nhận &amp; Lưu danh sách
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <c:if test="${sessionScope.hasInvalidRows eq true}">
                        <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; font-size: 0.82rem; font-weight: 600; color: #dc2626; display: flex; gap: 8px; align-items: center;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>⚠️ Có dòng không đủ 7 trường hoặc hạng GPLX không khớp kỳ thi. Sửa file CSV rồi tải lại — nút Lưu bị khóa.</span>
                        </div>
                    </c:if>

                    <div class="table-responsive" style="max-height: 420px; overflow-y: auto;">
                        <table class="audit-table" style="font-size: 0.88rem; width: 100%;">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 130px; text-align: left;">SBD (Tự sinh)</th>
                                    <th scope="col" style="text-align: left;">Họ và tên</th>
                                    <th scope="col" style="width: 100px; text-align: center;">Ngày sinh</th>
                                    <th scope="col" style="width: 135px; text-align: center;">Số CCCD</th>
                                    <th scope="col" style="width: 80px; text-align: center;">Hạng</th>
                                    <th scope="col" style="width: 110px; text-align: center;">Số điện thoại</th>
                                    <th scope="col" style="width: 140px; text-align: left;">Email</th>
                                    <th scope="col" style="text-align: center; width: 160px;">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${sessionScope.previewCandidates}">
                                    <c:set var="rowStyle" value="" />
                                    <c:choose>
                                        <c:when test="${c.invalid}">
                                            <c:set var="rowStyle" value="background-color: #fef2f2; border-left: 3px solid #ef4444;" />
                                        </c:when>
                                        <c:when test="${c.duplicate}">
                                            <c:set var="rowStyle" value="background-color: #fffbeb; border-left: 3px solid #f59e0b;" />
                                        </c:when>
                                    </c:choose>
                                    <tr style="${rowStyle}">
                                        <td style="font-weight: 800; color: #0052cc; font-family: monospace;">${c.sbd}</td>
                                        <td style="font-weight: 700; color: #0f172a;">
                                            <c:choose>
                                                <c:when test="${empty c.fullName}"><span style="color: #ef4444; font-style: italic;">[Thiếu]</span></c:when>
                                                <c:otherwise>${c.fullName}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; color: #475569;">
                                            <fmt:formatDate value="${c.dateOfBirth}" pattern="dd/MM/yyyy" />
                                        </td>
                                        <td style="text-align: center; font-family: monospace; color: #475569;">
                                            <c:choose>
                                                <c:when test="${empty c.govIdNo}"><span style="color: #ef4444; font-style: italic;">[Thiếu]</span></c:when>
                                                <c:otherwise>${c.govIdNo}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="role-badge ${c.licenseCode eq 'A1' ? 'role-badge--coi' : 'role-badge--admin'}" style="font-size: 0.72rem; padding: 2px 6px;">Hạng ${c.licenseCode}</span>
                                        </td>
                                        <td style="text-align: center; color: #475569; font-family: monospace;">
                                            <c:choose>
                                                <c:when test="${empty c.phoneNo}"><span style="color: #94a3b8; font-style: italic;">[Trống]</span></c:when>
                                                <c:otherwise>${c.phoneNo}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: left; color: #475569; font-family: monospace;">
                                            <c:choose>
                                                <c:when test="${empty c.email}"><span style="color: #94a3b8; font-style: italic;">[Trống]</span></c:when>
                                                <c:otherwise>${c.email}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${c.invalid}">
                                                    <span class="action-badge action-badge--danger" style="font-weight: 700; text-align: left; display: inline-block; max-width: 150px; line-height: 1.3;">KHÔNG HỢP LỆ: ${c.validationMessage}</span>
                                                </c:when>
                                                <c:when test="${c.duplicate}">
                                                    <span class="action-badge action-badge--warning" style="font-weight: 700; margin-right: 4px;">TRÙNG KỲ THI</span>
                                                    <select name="dupAction_${c.govIdNo}" style="font-size: 0.72rem; border-radius: 6px; padding: 2px 6px; height: 26px; border: 1.5px solid #f59e0b; background: #fff; font-weight: 700; color: #b45309; outline: none; cursor: pointer;">
                                                        <option value="overwrite">Ghi đè</option>
                                                        <option value="skip">Bỏ qua</option>
                                                    </select>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--success" style="font-weight: 700;">KHỚP KỲ THI</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </form>
            </div>
        </c:if>

        <!-- Success notification -->
        <c:if test="${param.importSuccess eq 'true'}">
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 12px; padding: 1.25rem; display: flex; gap: 12px; align-items: center; margin-top: 2rem; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.08);" class="animated slideInUp">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <div>
                    <h4 style="margin: 0; font-size: 0.95rem; font-weight: 800; color: #065f46;">Lưu danh sách chính thức thành công!</h4>
                    <p style="margin: 4px 0 0; font-size: 0.82rem; color: #047857;">Hệ thống đã lưu thành công **${sessionScope.importedCount}** học viên từ tệp CSV trích xuất PC08, kích hoạt trạng thái có mặt ở phòng chờ và tự động ghi nhật ký Audit Log kiểm toán.</p>
                </div>
            </div>
            <% 
                session.removeAttribute("importedCount");
                session.removeAttribute("uploadedFileName");
                session.removeAttribute("selectedImportSessionId");
            %>
        </c:if>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${pageContext.request.contextPath}/assets/js/upload.js"></script>
</body>
</html>
