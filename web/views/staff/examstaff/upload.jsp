<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="tai-ds" />
    <jsp:param name="pageTitle" value="Nhập danh sách" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
</jsp:include>

        <header class="page-header page-header--toolbar page-header--actions-only">
            <div class="page-actions">
                <a href="${pageContext.request.contextPath}/views/staff/examstaff/upload?action=downloadTemplate" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined" aria-hidden="true">download</span>Tải CSV mẫu DSTS
                </a>
            </div>
        </header>

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

        <div class="report-grid" style="grid-template-columns: 1.2fr 1fr; gap: 1.5rem;">

            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: center; gap: 1rem;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem;">Tải tệp dữ liệu lên</h2>
                </div>

                <form id="uploadForm" action="upload" method="POST" enctype="multipart/form-data"
                      style="display: flex; flex-direction: column; gap: 1.25rem; width: 100%;">

                    <c:if test="${not empty requestScope.currentSession}">
                        <input type="hidden" name="examSessionId" value="${requestScope.currentSession.id}" />
                        <div style="display: flex; flex-direction: column; gap: 4px; text-align: left; padding: 10px 14px; background: #f0fdf4; border: 1px solid #86efac; border-radius: 8px;">
                            <span style="font-size: 0.75rem; color: #166534; font-weight: 700;">
                                Đang nhập vào kỳ:
                                <strong>
                                    <c:choose>
                                        <c:when test="${not empty requestScope.currentSession.examCode}">
                                            ${requestScope.currentSession.examCode}
                                        </c:when>
                                        <c:otherwise>
                                            Hạng ${requestScope.currentSession.licenseCode}
                                            <c:if test="${not empty requestScope.currentSession.examDate}">
                                                — <fmt:formatDate value="${requestScope.currentSession.examDate}" pattern="dd/MM/yyyy" />
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </strong>
                            </span>
                            <span style="font-size: 0.72rem; color: #15803d;">
                                Thí sinh được ghi vào các phần thi theo Nội dung SH (L / H / Đ).
                                Chọn đúng kỳ ở sidebar trước khi tải file.
                            </span>
                        </div>
                    </c:if>

                    <c:if test="${not empty requestScope.importExamLicense}">
                        <div style="display: flex; flex-direction: column; gap: 4px; text-align: left; padding: 10px 14px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px;">
                            <span style="font-size: 0.75rem; color: #1d4ed8; font-weight: 600;">
                                Hạng bằng kỳ thi: <strong>${requestScope.importExamLicense}</strong> — cột Hạng GPLX phải khớp đúng hạng kỳ thi (A1, A hoặc B1).
                            </span>
                        </div>
                    </c:if>

                    <div class="upload-dropzone-container">
                        <div class="dropzone-icon" style="margin-bottom: 1rem;">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <span id="dropzoneLabel" data-analyzing-prefix="Đang phân tích: "
                              style="font-size: 0.95rem; font-weight: 700; color: #0f172a; display: block; margin-bottom: 0.25rem;">
                            Kéo thả tệp danh sách DSTS / PC08 vào đây hoặc click để chọn tệp...
                        </span>
                        <span style="font-size: 0.78rem; color: #64748b; display: block; margin-bottom: 1rem;">Chấp nhận .xlsx, .xls, .csv, .txt (tối đa 15MB) — format DSTS 10 cột</span>

                        <input type="file" id="fileInput" name="fileInput" class="upload-file-input" accept=".xlsx,.xls,.csv,.txt">
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

            <div class="report-pane rule-card">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0.75rem;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 6px; color: #0f172a;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Quy cách dữ liệu danh sách thi
                    </h2>
                </div>

                <p style="font-size: 0.82rem; color: #475569; margin-bottom: 1rem; line-height: 1.5;">Định dạng hỗ trợ: <strong>.xlsx, .xls, .csv, .txt</strong>. File gồm <strong>10 cột DSTS</strong>; SBD lấy từ file, cột <strong>Nội dung SH</strong> xác định phần thi và lần thi.</p>

                <div style="display: flex; flex-direction: column;">
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 1</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Số báo danh:</strong> SBD từ file (không tự sinh).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 2</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Họ và tên</strong>
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 3</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Số căn cước:</strong> CCCD 12 số, duy nhất.
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 4</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Ngày sinh:</strong> DD/MM/YYYY.
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 5</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Giới tính:</strong> Nam / Nữ.
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 6</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Nơi cư trú:</strong> Địa chỉ thí sinh.
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 7</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Hạng GPLX:</strong> Khớp kỳ thi — chỉ A1, A, B1 (A2→A, B/B2→B1 trong file vẫn chấp nhận).
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 8</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Nội dung SH:</strong> VD: <em>SH lần đầu L+H</em>, <em>SH lại L</em>, <em>Sát hạch H</em>.
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 9</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Số điện thoại</strong>
                        </div>
                    </div>
                    <div class="rule-item">
                        <span class="rule-column-tag">CỘT 10</span>
                        <div style="font-size: 0.8rem; color: #334155;">
                            <strong style="color: #0f172a;">Email</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>

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
                                Chỉ lưu dòng hợp lệ (đủ trường, khớp hạng, nội dung SH L/H/Đ khớp ca của kỳ) — dòng lỗi sẽ bỏ qua.
                            </p>
                        </div>
                        <div style="display: flex; gap: 10px;">
                            <a href="upload" class="btn-reset" style="height: 38px; padding: 0 1rem; font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; color: #475569;">Hủy bỏ</a>
                            <c:choose>
                                <c:when test="${empty sessionScope.validImportCount or sessionScope.validImportCount le 0}">
                                    <button type="button" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #cbd5e1; border-color: #cbd5e1; color: #64748b; cursor: not-allowed; display: inline-flex; align-items: center; justify-content: center;" disabled>
                                        Không có dòng hợp lệ để lưu
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <button type="submit" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #10b981; border-color: #10b981; color: #ffffff; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;">
                                        Lưu ${sessionScope.validImportCount} thí sinh hợp lệ
                                        <c:if test="${sessionScope.hasInvalidRows eq true}"> (bỏ qua dòng lỗi)</c:if>
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <c:if test="${sessionScope.hasInvalidRows eq true}">
                        <div style="background-color: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; font-size: 0.82rem; font-weight: 600; color: #b45309; display: flex; gap: 8px; align-items: center;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #f59e0b;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>Có dòng lỗi (thiếu trường, hạng không khớp, hoặc nội dung SH không khớp ca kỳ…). Các dòng hợp lệ vẫn lưu được — dòng lỗi sẽ bỏ qua khi xác nhận.</span>
                        </div>
                    </c:if>

                    <div class="table-responsive" style="max-height: 420px; overflow-y: auto;">
                        <table class="audit-table upload-preview-table" style="font-size: 0.88rem; width: 100%;">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 70px; text-align: left;">SBD</th>
                                    <th scope="col" style="text-align: left;">Họ và tên</th>
                                    <th scope="col" style="width: 100px; text-align: center;">Ngày sinh</th>
                                    <th scope="col" style="width: 130px; text-align: center;">CCCD</th>
                                    <th scope="col" style="width: 70px; text-align: center;">Hạng</th>
                                    <th scope="col" style="min-width: 140px; text-align: left;">Nội dung SH</th>
                                    <th scope="col" style="width: 100px; text-align: center;">SĐT</th>
                                    <th scope="col" style="width: 130px; text-align: left;">Email</th>
                                    <th scope="col" class="upload-preview-status-cell">Trạng thái</th>
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
                                            <span class="role-badge ${c.licenseCode eq 'A1' ? 'role-badge--coi' : 'role-badge--admin'}" style="font-size: 0.72rem; padding: 2px 6px;">${c.licenseCode}</span>
                                        </td>
                                        <td style="font-size: 0.78rem; color: #475569; max-width: 180px;">
                                            <c:out value="${c.reasonForTaking}" />
                                            <c:if test="${c.takeNo gt 1}">
                                                <span style="display:block; font-size:0.7rem; color:#64748b;">Lần thi: ${c.takeNo}</span>
                                            </c:if>
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
                                        <td class="upload-preview-status-cell">
                                            <c:choose>
                                                <c:when test="${c.invalid}">
                                                    <span class="upload-preview-status upload-preview-status--invalid">
                                                        <strong>Không hợp lệ:</strong> ${c.validationMessage}
                                                    </span>
                                                </c:when>
                                                <c:when test="${c.duplicate}">
                                                    <div class="upload-preview-status--duplicate">
                                                        <span class="action-badge action-badge--warning">Trùng kỳ thi</span>
                                                        <select name="dupAction_${c.govIdNo}" style="font-size: 0.72rem; border-radius: 6px; padding: 2px 6px; height: 26px; border: 1.5px solid #f59e0b; background: #fff; font-weight: 700; color: #b45309; outline: none; cursor: pointer; max-width: 100%;">
                                                            <option value="overwrite">Ghi đè</option>
                                                            <option value="skip">Bỏ qua</option>
                                                        </select>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="upload-preview-status upload-preview-status--ok">Khớp kỳ thi</span>
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

        <c:if test="${param.importSuccess eq 'true'}">
            <c:choose>
                <c:when test="${sessionScope.importedCount eq 0}">
                    <div style="background-color: #fffbeb; border: 1px solid #f59e0b; border-radius: 12px; padding: 1.25rem; display: flex; gap: 12px; align-items: center; margin-top: 2rem;">
                        <div>
                            <h4 style="margin: 0; font-size: 0.95rem; font-weight: 800; color: #92400e;">Không lưu được thí sinh nào</h4>
                            <p style="margin: 4px 0 0; font-size: 0.82rem; color: #b45309;">
                                Đã xử lý xong nhưng <strong>0</strong> thí sinh được ghi vào cơ sở dữ liệu
                                <c:if test="${not empty sessionScope.importSkippedCount and sessionScope.importSkippedCount gt 0}">
                                    — bỏ qua / lỗi <strong>${sessionScope.importSkippedCount}</strong> dòng.
                                </c:if>
                                Kiểm tra log server (lỗi SQL), ca thi đã chọn, và dòng trùng CCCD trong ca.
                            </p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 12px; padding: 1.25rem; display: flex; gap: 12px; align-items: center; margin-top: 2rem; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.08);" class="animated slideInUp">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <div>
                    <h4 style="margin: 0; font-size: 0.95rem; font-weight: 800; color: #065f46;">Lưu danh sách chính thức thành công!</h4>
                    <p style="margin: 4px 0 0; font-size: 0.82rem; color: #047857;">Hệ thống đã lưu thành công <strong>${sessionScope.importedCount}</strong> thí sinh.
                        <c:if test="${not empty sessionScope.importSkippedCount and sessionScope.importSkippedCount gt 0}">
                            (Bỏ qua ${sessionScope.importSkippedCount} dòng)
                        </c:if>
                    </p>
                    <c:if test="${not empty sessionScope.importSkipSummary}">
                        <p style="margin: 6px 0 0; font-size: 0.78rem; color: #b45309;">
                            Chi tiết bỏ qua: ${sessionScope.importSkipSummary}
                        </p>
                    </c:if>
                </div>
            </div>
                </c:otherwise>
            </c:choose>
            <%
                session.removeAttribute("importedCount");
                session.removeAttribute("importSkippedCount");
                session.removeAttribute("importSkipSummary");
                session.removeAttribute("uploadedFileName");
                session.removeAttribute("selectedImportSessionId");
            %>
        </c:if>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp">
    <jsp:param name="extraScript" value="/assets/js/upload.js" />
</jsp:include>
