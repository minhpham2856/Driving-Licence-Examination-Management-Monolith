<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<div id="procedure-desk" class="procedure-desk-section"
     data-ctx="${pageContext.request.contextPath}"
     <c:if test="${not empty requestScope.openDossierPrint}">data-open-dossier-print="${requestScope.openDossierPrint}"</c:if>>
    <header class="procedure-desk-header">
        <h2 class="procedure-desk-title">Bàn làm thủ tục</h2>
        <p class="procedure-desk-subtitle">Quy trình 3 bước: Xác minh hồ sơ &rarr; Chụp ảnh xác minh danh tính &rarr; Thu lệ phí. Import CSV không cần ảnh; ảnh lưu DB để bộ phận khác in hồ sơ sau khi thi xong.</p>
    </header>

        <c:if test="${requestScope.examMutationsLocked}">
            <div style="background-color: #fef2f2; border: 1px solid #ef4444; border-radius: 8px; padding: 10px 12px; margin-bottom: 1rem; font-size: 0.85rem; font-weight: 600; color: #b91c1c;">
                Kỳ thi đã kết thúc — chỉ xem hồ sơ, không sửa / xóa / thu phí lại.
            </div>
        </c:if>

        <c:if test="${not empty requestScope.profile}">
            <div class="procedure-active-bar">
                <div class="procedure-active-bar__meta">
                    <span class="procedure-active-bar__sbd">SBD: ${profile.sbd}</span>
                    <span class="procedure-active-bar__label">Đang lập hồ sơ cho: <strong style="color: #0f172a;">${profile.name}</strong> (Hạng ${profile.clazz})</span>
                </div>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty requestScope.profile}">

                <c:set var="currentSbd" value="${profile.sbd}" />
                <c:set var="cName" value="${profile.name}" />
                <c:set var="cDob">
                    <fmt:formatDate value="${profile.dob}" pattern="dd/MM/yyyy"/>
                </c:set>
                <c:set var="cDobIso">
                    <fmt:formatDate value="${profile.dob}" pattern="dd-MM-yyyy"/>
                </c:set>
                <c:set var="cCccd" value="${profile.cccd}" />
                <c:set var="cClass" value="${profile.clazz}" />

                <c:set var="currentStep" value="${not empty requestScope.step ? requestScope.step : param.step}" />
                <c:if test="${empty currentStep}">
                    <c:set var="currentStep" value="1" />
                </c:if>

                <div class="procedure-steps-bar">
                    <a href="procedure?sbd=${currentSbd}&amp;step=1#procedure-desk"
                       class="procedure-step-item procedure-step-item--link ${currentStep eq '1' ? 'procedure-step-item--active' : ''} ${currentStep eq '2' or currentStep eq '3' ? 'procedure-step-item--done' : ''}">
                        <div class="step-number-badge">1</div>
                        <span>Xác minh &amp; Sửa lỗi</span>
                    </a>

                    <div class="procedure-step-divider"></div>

                    <a href="procedure?sbd=${currentSbd}&amp;step=2#procedure-desk"
                       class="procedure-step-item procedure-step-item--link ${currentStep eq '2' ? 'procedure-step-item--active' : ''} ${currentStep eq '3' ? 'procedure-step-item--done' : ''}">
                        <div class="step-number-badge">2</div>
                        <span>Chụp ảnh chân dung</span>
                    </a>

                    <div class="procedure-step-divider"></div>

                    <a href="procedure?sbd=${currentSbd}&amp;step=3#procedure-desk"
                       class="procedure-step-item procedure-step-item--link ${currentStep eq '3' ? 'procedure-step-item--active' : ''}">
                        <div class="step-number-badge">3</div>
                        <span>Lệ phí &amp; QR chuyển khoản</span>
                    </a>
                </div>

                <div class="report-grid" style="grid-template-columns: 1.5fr 1fr; gap: 1.5rem; margin-bottom: 2.5rem;">

                    <div class="report-pane">

                        <c:if test="${currentStep eq '1'}">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 1: Tra cứu, đối chiếu và sửa đổi hồ sơ học viên</h3>
                            </div>

                            <c:if test="${requestScope.profileUpdatedAlert eq 'true'}">
                                <div style="background-color: #fffbeb; border: 1px solid #f59e0b; border-radius: 8px; padding: 10px; margin-bottom: 1rem; font-size: 0.8rem; color: #b45309; display: flex; gap: 8px; align-items: center;">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #f59e0b; flex-shrink: 0;">
                                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    </svg>
                                    <span>
                                        <strong>Đã ghi nhận thay đổi kiểm toán:</strong> Sửa đổi thông tin nhân thân của học viên thành công! Dữ liệu đã được cập nhật vào phiên làm việc và ghi nhận lịch sử thay đổi.
                                    </span>
                                </div>
                            </c:if>

                            <form action="procedure" method="GET" id="procedureForm" style="display: flex; flex-direction: column; gap: 1.25rem;">
                                <input type="hidden" name="sbd" value="${currentSbd}">
                                <input type="hidden" name="step" value="2">
                                <input type="hidden" name="action" id="formAction" value="">

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Họ và tên thí sinh:</label>
                                        <input type="text" name="fullName" class="input-field" value="${cName}" style="font-weight: 700;">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số báo danh (SBD):</label>
                                        <input type="text" class="input-field" value="${currentSbd}" readonly style="background-color: #f1f5f9; font-weight: 800; color: #0052cc; font-family: monospace;">
                                    </div>
                                </div>

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Ngày tháng năm sinh:</label>
                                        <input type="date" name="dateOfBirth" class="input-field" value="${cDobIso}">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số định danh / căn cước:</label>
                                        <input type="text" name="govIdNo" class="input-field" value="${cCccd}" style="font-family: monospace;">
                                    </div>
                                </div>

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                                    <div class="input-group">
                                        <label class="input-label">Email:</label>
                                        <input type="text" name="email" class="input-field" value="${profile.email}">
                                    </div>
                                    <div class="input-group">
                                        <label class="input-label">Số điện thoại:</label>
                                        <input type="text" name="phoneNo" class="input-field" value="${profile.phoneNo}">
                                    </div>
                                </div>

                                <div class="input-group">
                                    <label class="input-label">Hạng bằng sát hạch:</label>
                                    <input type="text" class="input-field" value="Hạng ${cClass}" readonly style="background-color: #f1f5f9; font-weight: 700; color: #334155;">
                                </div>

                                <c:if test="${not requestScope.examMutationsLocked}">
                                <button type="submit" id="submitBtn" class="btn-filter" style="height: 42px; border-radius: 8px; justify-content: center; font-weight: 700; margin-top: 1rem; transition: all 0.3s; background: linear-gradient(135deg, #0052cc, #003d9b); border-color: #003d9b;">
                                    Xác nhận & Sang Bước 2 (Chụp ảnh) &rarr;
                                </button>
                                </c:if>
                            </form>
                        </c:if>

                        <c:if test="${currentStep eq '2'}">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 2: Chụp ảnh chân dung xác minh danh tính</h3>
                                <p style="margin: 6px 0 0; font-size: 0.8rem; color: #64748b;">Thí sinh import từ CSV không có ảnh - chụp tại bàn thủ tục khi đến làm hồ sơ. Ảnh lưu vào hệ thống để bộ phận khác in hồ sơ sau khi thi xong.</p>
                            </div>

                            <c:if test="${not empty requestScope.photoStaleMsg}">
                                <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                    ${requestScope.photoStaleMsg}
                                </div>
                            </c:if>

                            <c:if test="${not empty requestScope.photoRequiredMsg}">
                                <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                    ${requestScope.photoRequiredMsg}
                                </div>
                            </c:if>

                            <c:choose>
                                <c:when test="${requestScope.hasValidPhoto}">
                                    <div class="camera-live-frame camera-live-frame--captured">
                                        <div class="camera-live-reticle"></div>
                                        <c:choose>
                                            <c:when test="${not empty profile.photoUrl}">
                                                <img class="camera-captured-img"
                                                     src="${pageContext.request.contextPath}/examstaff/candidate-photo?sbd=${profile.sbd}&amp;t=${profile.id}"
                                                     alt="Ảnh chân dung ${cName}">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="photo-avatar-placeholder">${fn:substring(cName, 0, 1)}</div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div style="position: absolute; bottom: 12px; background: rgba(16, 185, 129, 0.9); color: #ffffff; padding: 4px 10px; border-radius: 6px; font-size: 0.72rem; font-weight: bold; z-index: 4;">
                                            ẢNH CHỤP ĐÃ LƯU VÀO HỒ SƠ
                                        </div>
                                    </div>
                                    <div style="display: flex; gap: 10px; margin-top: 1.25rem;">
                                        <c:if test="${not requestScope.examMutationsLocked}">
                                        <a href="procedure?sbd=${currentSbd}&amp;step=2&amp;action=recapture#procedure-desk" class="btn-reset" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1;">Chụp lại ảnh</a>
                                        </c:if>
                                        <a href="procedure?sbd=${currentSbd}&amp;step=3#procedure-desk" class="btn-filter" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #10b981; border-color: #10b981;">Sang Bước 3</a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div id="cameraError" class="camera-error-box is-hidden"></div>
                                    <div id="cameraFrame" class="camera-live-frame camera-live-frame--active">
                                        <video id="cameraVideo" class="camera-live-video" autoplay playsinline muted></video>
                                        <canvas id="captureCanvas" class="capture-canvas-hidden"></canvas>
                                        <div class="camera-live-reticle"></div>
                                        <span id="cameraStatus" class="camera-status-badge">Đang khởi động camera...</span>
                                        <span style="z-index: 3; font-weight: 700; font-size: 0.85rem; color: rgba(255, 255, 255, 0.9); text-transform: uppercase; position: absolute; bottom: 64px; text-shadow: 0 1px 4px rgba(0,0,0,0.6);">
                                            Căn chỉnh mặt vào khung hình
                                        </span>
                                        <button type="button" id="captureBtn" class="btn-filter" disabled
                                                data-label-capture="Chụp ảnh chân dung"
                                                data-label-saving="Đang lưu ảnh..."
                                                style="position: absolute; bottom: 15px; height: 38px; border-radius: 6px; padding: 0 1.25rem; font-size: 0.82rem; z-index: 4; display: inline-flex; align-items: center; gap: 6px; cursor: pointer;">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                                            </svg>
                                            Chụp ảnh chân dung
                                        </button>
                                    </div>
                                    <p style="margin-top: 0.75rem; font-size: 0.75rem; color: #64748b;">
                                        Trình duyệt sẽ yêu cầu quyền truy cập camera. Nếu bị từ chối, hãy bật quyền camera trong cài đặt trình duyệt rồi tải lại trang.
                                    </p>
                                </c:otherwise>
                            </c:choose>
                        </c:if>

                        <c:if test="${currentStep eq '3'}">
                            <div class="procedure-step-header-row">
                                <div>
                                    <h3 class="procedure-step-title">Bước 3: Lệ phí sát hạch &amp; Thanh toán QR Code ngân hàng</h3>
                                </div>
                            </div>

                            <c:if test="${not requestScope.hasValidPhoto}">
                                <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                    Chưa có ảnh chân dung hợp lệ. Hoàn tất Bước 2 trước khi thu lệ phí.
                                </div>
                            </c:if>

                            <c:if test="${not empty requestScope.paymentErrorMsg}">
                                <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                    ${requestScope.paymentErrorMsg}
                                </div>
                            </c:if>

                            <c:if test="${requestScope.paymentJustCompleted}">
                                <div class="procedure-payment-success">
                                    <strong>Đã thu lệ phí thành công.</strong>
                                    Chọn <strong>In hồ sơ</strong> để in phiếu xác nhận, hoặc <strong>Chuyển thí sinh tiếp theo</strong> nếu không cần in.
                                </div>
                            </c:if>

                            <c:if test="${profile.paymentCompleted and not requestScope.paymentJustCompleted}">
                                <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 8px; padding: 10px 12px; margin-bottom: 1rem; font-size: 0.82rem; color: #047857;">
                                    Thí sinh này đã có bản ghi thanh toán trong hệ thống. Bấm <strong>Chuyển học viên tiếp theo</strong> để tiếp tục hàng đợi.
                                </div>
                            </c:if>

                            <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 1rem; align-items: start;">
                                <div>
                                    <p style="margin: 0 0 0.75rem; font-size: 0.78rem; color: #64748b;">
                                        Bảng giá theo quy định - Hạng <strong style="color: #0f172a;">${cClass}</strong>
                                    </p>
                                    <table class="report-table" style="font-size: 0.85rem; width: 100%;">
                                        <thead>
                                            <tr>
                                                <th scope="col">Khoản lệ phí thi</th>
                                                <th scope="col" style="text-align: right; width: 120px;">Thành tiền (đ)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty requestScope.feeLines}">
                                                    <c:forEach var="feeLine" items="${requestScope.feeLines}">
                                                        <tr>
                                                            <td>${feeLine.feeName}</td>
                                                            <td style="text-align: right; font-weight: 600;">
                                                                <fmt:formatNumber value="${feeLine.amount}" pattern="#,##0"/>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                    <tr style="border-top: 2px solid #cbd5e1; background-color: #f8fafc;">
                                                        <td style="font-weight: 800; color: #0f172a;">TỔNG CỘNG LỆ PHÍ:</td>
                                                        <td style="text-align: right; font-weight: 800; color: #0052cc; font-size: 0.95rem;">
                                                            <fmt:formatNumber value="${requestScope.feeTotal}" pattern="#,##0"/> đ
                                                        </td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr>
                                                        <td colspan="2" style="text-align: center; color: #94a3b8; padding: 1rem;">
                                                            Không tìm thấy bảng giá áp dụng cho hạng này. Kiểm tra bảng <code>Fee</code> trong cơ sở dữ liệu.
                                                        </td>
                                                    </tr>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>

                                    <div style="display: flex; flex-direction: column; gap: 12px; margin-top: 1.5rem;">
                                        <c:choose>
                                            <c:when test="${profile.paymentCompleted}">
                                                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                                                    <a href="candidate-dossier?sbd=${currentSbd}&amp;print=true" target="_blank" rel="noopener"
                                                       class="procedure-btn procedure-btn--print"
                                                       title="Mở trang in hồ sơ thí sinh">
                                                        In hồ sơ
                                                    </a>
                                                    <a href="procedure?action=nextCandidate&amp;sbd=${currentSbd}"
                                                       class="procedure-btn procedure-btn--next">
                                                        Chuyển thí sinh tiếp theo &rarr;
                                                    </a>
                                                </div>
                                            </c:when>
                                            <c:when test="${requestScope.hasValidPhoto and not empty requestScope.feeLines and not requestScope.examMutationsLocked}">
                                                <form action="procedure" method="POST" style="margin: 0; display: flex; flex-direction: column; gap: 12px;">
                                                    <input type="hidden" name="action" value="confirmPayment">
                                                    <input type="hidden" name="sbd" value="${currentSbd}">
                                                    <input type="hidden" name="step" value="3">
                                                    <label class="procedure-print-option">
                                                        <input type="checkbox" name="printAfterPayment" value="true" checked>
                                                        Mở in hồ sơ ngay sau khi đóng tiền
                                                    </label>
                                                    <button type="submit" class="procedure-btn procedure-btn--pay" style="width: 100%;">
                                                        Đóng Tiền Mặt
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:when test="${requestScope.examMutationsLocked}">
                                                <span class="procedure-btn procedure-btn--disabled" style="width: 100%;">Kỳ đã kết thúc — không thu phí lại</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="procedure-btn procedure-btn--disabled" style="width: 100%; margin-bottom: 8px;">
                                                    Chưa cấu hình bảng lệ phí cho hạng ${cClass}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <div class="qr-card sepay-pay-card" id="sePayQrCard"
                                     data-sbd="${currentSbd}"
                                     data-ctx="${pageContext.request.contextPath}"
                                     data-configured="${sePayConfigured}"
                                     data-awaiting="${not empty sessionScope.sePayAwaitingSbd and sessionScope.sePayAwaitingSbd eq currentSbd}">
                                    <c:choose>
                                        <c:when test="${profile.paymentCompleted}">
                                            <div class="sepay-pay-card__paid">
                                                <span class="sepay-pay-card__paid-badge">Đã thanh toán</span>
                                                <span class="sepay-pay-card__paid-hint">SePay / Tiền mặt</span>
                                            </div>
                                        </c:when>
                                        <c:when test="${requestScope.hasValidPhoto and not empty requestScope.feeLines and not requestScope.examMutationsLocked}">
                                            <c:if test="${not empty requestScope.feeTotal and requestScope.feeTotal > 0}">
                                                <div class="sepay-pay-card__amount">
                                                    <span class="sepay-pay-card__amount-label">Tổng lệ phí</span>
                                                    <span class="sepay-pay-card__amount-value">
                                                        <fmt:formatNumber value="${requestScope.feeTotal}" pattern="#,##0"/> đ
                                                    </span>
                                                </div>
                                            </c:if>
                                            <div class="sepay-pay-card__actions">
                                                <button type="button" id="btnSePayCheckout"
                                                        class="procedure-btn procedure-btn--pay sepay-pay-card__btn-primary"
                                                        ${sePayConfigured ? '' : 'disabled'}
                                                        title="${sePayConfigured ? 'Mở cổng SePay (QR)' : 'Chưa cấu hình SePay (.env)'}">
                                                    Thu qua SePay (QR)
                                                </button>
                                                <button type="button" id="btnSePayCheck"
                                                        class="sepay-pay-card__btn-check"
                                                        ${sePayConfigured ? '' : 'disabled'}>
                                                    Kiểm tra đã thanh toán
                                                </button>
                                            </div>
                                            <p id="sePayStatusMsg" class="sepay-pay-card__status" role="status">
                                                <c:if test="${not sePayConfigured}">SePay chưa cấu hình — dùng tiền mặt</c:if>
                                            </p>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="sepay-pay-card__waiting">
                                                <span>Chờ đủ điều kiện thu phí</span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>

                    </div>

                    <div class="report-pane" style="height: fit-content;">
                        <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.5rem; margin-bottom: 0.75rem;">
                            <h3 style="font-size: 0.95rem; font-weight: 700; color: #0f172a; margin: 0;">Sơ đồ tóm tắt học viên</h3>
                        </div>

                        <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 1rem 0;">
                            <c:choose>
                                <c:when test="${requestScope.hasValidPhoto}">
                                    <img src="${pageContext.request.contextPath}/examstaff/candidate-photo?sbd=${profile.sbd}&amp;t=${profile.id}"
                                         alt="Ảnh ${cName}"
                                         style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 2px solid #e2e8f0;">
                                </c:when>
                                <c:otherwise>
                                    <div class="photo-avatar-placeholder" style="width: 80px; height: 80px; font-size: 2rem;">
                                        ${fn:substring(cName, 0, 1)}
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <h4 style="margin: 0; font-size: 1rem; font-weight: 800; color: #0f172a;">${cName}</h4>
                            <span style="font-family: monospace; font-weight: 800; color: #0052cc; font-size: 0.9rem;">SBD: ${currentSbd}</span>

                            <div style="width: 100%; border-top: 1px solid #f1f5f9; margin-top: 8px; padding-top: 8px; display: flex; flex-direction: column; gap: 6px; font-size: 0.8rem;">
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">Hạng sát hạch:</span>
                                    <span style="font-weight: 700; color: #0f172a;">Hạng ${cClass}</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">Căn cước:</span>
                                    <span style="font-weight: 600; color: #0f172a; font-family: monospace;">${cCccd}</span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: #64748b;">Ngày sinh:</span>
                                    <span style="font-weight: 600; color: #0f172a;">${cDob}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </c:when>
            <c:otherwise>
                <div class="report-pane" style="text-align: center; padding: 4rem 1rem; color: #64748b;">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1rem; display: block; opacity: 0.35; color: #64748b;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span style="font-weight: 700; font-size: 1rem; color: #334155; display: block; margin-bottom: 0.5rem;">Bàn làm thủ tục trống</span>
                    Chưa có học viên nào được chọn làm thủ tục.
                    <p style="font-size: 0.82rem; color: #94a3b8; max-width: 420px; margin: 0.5rem auto 1.5rem;">Chọn thí sinh từ danh sách bên dưới, hoặc bấm <strong>Tiến hành lập hồ sơ</strong> / <strong>Hồ sơ</strong> ở hàng đợi phía trên.</p>

                    <div style="max-width: 520px; margin: 1.5rem auto 0; padding: 1.5rem; background: rgba(255, 255, 255, 0.9); border: 1.5px solid #e2e8f0; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05); backdrop-filter: blur(8px);">
                        <form action="procedure" method="GET" style="display: flex; flex-direction: column; gap: 12px; text-align: left; margin: 0;">
                            <label for="emptySbdInput" style="font-size: 0.85rem; font-weight: 700; color: #334155; display: flex; align-items: center; gap: 8px;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #0052cc;">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="9" cy="7" r="4"></circle>
                                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                </svg>
                                CHỌN THÍ SINH ĐÃ ĐƯỢC GỌI VÀO PHÒNG LÀM THỦ TỤC:
                            </label>
                            <div style="position: relative; display: flex; width: 100%;">
                                <select id="emptySbdInput" name="sbd" data-auto-submit class="procedure-empty-sbd-select">
                                    <option value="">-- Click để chọn học viên đã được gọi --</option>
                                    <c:forEach var="c" items="${activeCallQueue}">
                                        <option value="${c.sbd}">
                                                ${c.sbd} - ${c.name} (Hạng ${c.clazz})
                                            </option>
                                    </c:forEach>
                                </select>
                                <div style="position: absolute; right: 15px; top: 50%; transform: translateY(-50%); pointer-events: none; color: #0052cc; display: flex; align-items: center;">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </div>
                            </div>
                        </form>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>
</div>
