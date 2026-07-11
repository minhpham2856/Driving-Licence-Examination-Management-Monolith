<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bàn Làm Thủ Tục - Ban Sát Hạch</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="goi-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <jsp:include page="/views/layout/header-examstaff.jsp">
            <jsp:param name="sectionTitle" value="Bàn làm thủ tục" />
            <jsp:param name="pageTitle" value="Lập hồ sơ thí sinh tại bàn thủ tục" />
        </jsp:include>

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/staff/examstaff/dashboard">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Bàn làm thủ tục</span>
        </nav>

        <div id="procedure-desk" class="procedure-desk-section" style="margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #e2e8f0; scroll-margin-top: 1rem;">
            <header style="margin-bottom: 1.25rem;">
                <h2 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0 0 4px;">Bàn làm thủ tục</h2>
                <p style="font-size: 0.82rem; color: #64748b; margin: 0;">Quy trình 3 bước: Xác minh hồ sơ &rarr; Chụp ảnh &rarr; Thu lệ phí.</p>
            </header>

            <!-- Active Candidate Status Bar -->
            <c:if test="${not empty requestScope.profile}">
                <div style="background-color: rgba(0, 82, 204, 0.05); border: 1px solid rgba(0, 82, 204, 0.15); border-radius: 12px; padding: 10px 16px; margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center; backdrop-filter: blur(10px);">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="background-color: #0052cc; color: #ffffff; font-family: monospace; font-weight: 800; font-size: 0.78rem; padding: 2px 8px; border-radius: 6px;">SBD: ${profile.candidateNumber}</span>
                        <span style="font-size: 0.88rem; font-weight: 700; color: #1e293b;">Đang lập hồ sơ cho: <strong style="color: #0f172a;">${profile.fullName}</strong> (Hạng ${not empty requestScope.currentExam ? requestScope.currentExam.licenseCode : ''})</span>
                    </div>

                    <form action="procedure" method="GET" style="display: flex; align-items: center; gap: 6px; margin: 0;">
                        <span style="font-size: 0.72rem; font-weight: 600; color: #64748b;">Chuyển học viên:</span>
                        <select name="sbd" data-auto-submit class="procedure-switch-form__select">
                            <option value="">-- Chọn --</option>
                            <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                <c:if test="${not (c.validCapturedPhoto and c.paymentCompleted)}">
                                    <option value="${c.candidateNumber}" ${profile.candidateNumber eq c.candidateNumber ? 'selected' : ''}>${c.candidateNumber} - ${c.fullName}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </form>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty requestScope.profile}">

                    <c:set var="currentSbd" value="${profile.candidateNumber}" />
                    <c:set var="cName" value="${profile.fullName}" />
                    <c:set var="cDob" value="${profile.dob}" />
                    <c:set var="cCccd" value="${profile.govIdNo}" />
                    <c:set var="cClass" value="${not empty requestScope.currentExam ? requestScope.currentExam.licenseCode : ''}" />

                    <c:set var="currentStep" value="${not empty param.step ? param.step : requestScope.step}" />
                    <c:if test="${empty currentStep}">
                        <c:set var="currentStep" value="1" />
                    </c:if>

                    <!-- Step progress indicator -->
                    <div class="procedure-steps-bar">
                        <div class="procedure-step-item ${currentStep eq '1' ? 'procedure-step-item--active' : (currentStep > 1 ? 'procedure-step-item--done' : '')}">
                            <div class="step-number-badge">1</div>
                            <span>Xác minh & Sửa lỗi</span>
                        </div>
                        <div style="flex: 1; height: 1px; background-color: #e2e8f0; margin: 0 1rem;"></div>
                        <div class="procedure-step-item ${currentStep eq '2' ? 'procedure-step-item--active' : (currentStep > 2 ? 'procedure-step-item--done' : '')}">
                            <div class="step-number-badge">2</div>
                            <span>Chụp ảnh chân dung</span>
                        </div>
                        <div style="flex: 1; height: 1px; background-color: #e2e8f0; margin: 0 1rem;"></div>
                        <div class="procedure-step-item ${currentStep eq '3' ? 'procedure-step-item--active' : (currentStep > 3 ? 'procedure-step-item--done' : '')}">
                            <div class="step-number-badge">3</div>
                            <span>Lệ phí & QR chuyển khoản</span>
                        </div>
                    </div>

                    <div class="report-grid" style="grid-template-columns: 1.5fr 1fr; gap: 1.5rem; margin-bottom: 2.5rem;">

                        <!-- Left Column: Step Content Panels -->
                        <div class="report-pane">

                            <!-- STEP 1: Verify & Edit Info -->
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
                                        <span><strong>Đã cập nhật:</strong> Sửa đổi thông tin nhân thân của học viên thành công!</span>
                                    </div>
                                </c:if>

                                <c:if test="${not empty requestScope.profileUpdateError}">
                                    <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                        ${requestScope.profileUpdateError}
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
                                            <input type="text" name="dateOfBirth" class="input-field" value="<fmt:formatDate value='${cDob}' pattern='dd/MM/yyyy' />">
                                        </div>
                                        <div class="input-group">
                                            <label class="input-label">Số định danh CCCD:</label>
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

                                    <button type="submit" id="submitBtn" class="btn-filter" style="height: 42px; border-radius: 8px; justify-content: center; font-weight: 700; margin-top: 1rem; transition: all 0.3s; background: linear-gradient(135deg, #0052cc, #003d9b); border-color: #003d9b;">
                                        Xác nhận & Sang Bước 2 (Chụp ảnh) &rarr;
                                    </button>
                                </form>
                            </c:if>

                            <!-- STEP 2: Live Camera Capture -->
                            <c:if test="${currentStep eq '2'}">
                                <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                    <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 2: Chụp ảnh chân dung từ camera thực tế</h3>
                                    <p style="margin: 6px 0 0; font-size: 0.8rem; color: #64748b;">Thí sinh import từ CSV không có ảnh — bắt buộc chụp tại đây trước khi thu phí và in hồ sơ kết quả.</p>
                                </div>

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
                                                <c:when test="${not empty profile.candidate.photoImageUrl}">
                                                    <img class="camera-captured-img"
                                                         src="${pageContext.request.contextPath}/${profile.candidate.photoImageUrl}?t=${profile.candidateNumber}"
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
                                            <a href="procedure?sbd=${currentSbd}&step=2&amp;action=recapture#procedure-desk" class="btn-reset" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1;">Chụp lại ảnh</a>
                                            <a href="procedure?sbd=${currentSbd}&step=3#procedure-desk" class="btn-filter" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #10b981; border-color: #10b981;">Xác nhận & Chuyển sang Bước 3</a>
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

                            <!-- STEP 3: Lệ phí & Thanh toán QR -->
                            <c:if test="${currentStep eq '3'}">
                                <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                                    <h3 style="font-size: 1.05rem; font-weight: 700; color: #0f172a; margin: 0;">Bước 3: Lệ phí sát hạch & Thanh toán QR Code ngân hàng</h3>
                                </div>

                                <c:if test="${not requestScope.hasValidPhoto}">
                                    <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                        Chưa có ảnh chân dung hợp lệ. Vui lòng quay lại <a href="procedure?sbd=${currentSbd}&amp;step=2#procedure-desk">Bước 2</a> để chụp ảnh trước khi thu lệ phí.
                                    </div>
                                </c:if>

                                <c:if test="${not empty requestScope.paymentErrorMsg}">
                                    <div class="camera-error-box" style="display:block; margin-bottom:1rem;">
                                        ${requestScope.paymentErrorMsg}
                                    </div>
                                </c:if>

                                <c:if test="${profile.paymentCompleted}">
                                    <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 8px; padding: 10px 12px; margin-bottom: 1rem; font-size: 0.82rem; color: #047857;">
                                        Thí sinh này đã có bản ghi thanh toán trong hệ thống. Bấm <strong>Chuyển học viên tiếp theo</strong> để tiếp tục hàng đợi.
                                    </div>
                                </c:if>

                                <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 1rem; align-items: start;">
                                    <div>
                                        <table class="report-table" style="font-size: 0.85rem; width: 100%;">
                                            <thead>
                                                <tr>
                                                    <th scope="col">Khoản lệ phí thi</th>
                                                    <th scope="col" style="text-align: right; width: 100px;">Thành tiền</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td>Lệ phí sát hạch lý thuyết</td>
                                                    <td style="text-align: right; font-weight: 600;">80,000 đ</td>
                                                </tr>
                                                <tr>
                                                    <td>Lệ phí sát hạch mô phỏng</td>
                                                    <td style="text-align: right; font-weight: 600;">100,000 đ</td>
                                                </tr>
                                                <tr>
                                                    <td>Lệ phí cấp phôi bằng nhựa PET</td>
                                                    <td style="text-align: right; font-weight: 600;">20,000 đ</td>
                                                </tr>
                                                <tr style="border-top: 2px solid #cbd5e1; background-color: #f8fafc;">
                                                    <td style="font-weight: 800; color: #0f172a;">TỔNG CỘNG LỆ PHÍ:</td>
                                                    <td style="text-align: right; font-weight: 800; color: #0052cc; font-size: 0.95rem;">200,000 đ</td>
                                                </tr>
                                            </tbody>
                                        </table>

                                        <div style="display: flex; gap: 10px; margin-top: 1.5rem;">
                                            <c:choose>
                                                <c:when test="${profile.paymentCompleted}">
                                                    <a href="procedure?action=nextCandidate&amp;sbd=${currentSbd}" class="btn-filter" style="height: 42px; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #0052cc; border-color: #0052cc;">
                                                        Chuyển học viên tiếp theo &rarr;
                                                    </a>
                                                </c:when>
                                                <c:when test="${requestScope.hasValidPhoto}">
                                                    <form action="procedure" method="POST" style="flex: 1; margin: 0;">
                                                        <input type="hidden" name="action" value="confirmPayment">
                                                        <input type="hidden" name="sbd" value="${currentSbd}">
                                                        <input type="hidden" name="step" value="3">
                                                        <button type="submit" class="btn-filter" style="width: 100%; height: 42px; border-radius: 8px; background-color: #10b981; border-color: #10b981; cursor: pointer;">
                                                            Đóng Tiền Mặt
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="btn-filter" style="height: 42px; border-radius: 8px; display: inline-flex; align-items: center; justify-content: center; flex: 1; background-color: #94a3b8; border-color: #94a3b8; cursor: not-allowed; opacity: 0.7;">
                                                        Cần chụp ảnh trước khi thu phí
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <div class="qr-card">
                                        <div style="border: 1px solid #cbd5e1; border-radius: 8px; padding: 6px; background-color: #ffffff; display: flex; align-items: center; justify-content: center; width: 110px; height: 110px;">
                                            <div style="width: 100%; height: 100%; display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; border: 2px solid #000000; padding: 4px; box-sizing: border-box; background-color: #ffffff;">
                                                <div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div>
                                                <div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div><div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div>
                                                <div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div><div style="background-color: #000000;"></div>
                                                <div style="background-color: #000000;"></div><div style="background-color: #000000;"></div><div style="background-color: #ffffff;"></div><div style="background-color: #000000;"></div>
                                            </div>
                                        </div>
                                        <span style="font-size: 0.7rem; font-weight: 800; color: #475569; text-transform: uppercase;">VIETQR Chuyển Khoản</span>
                                        <span style="font-size: 0.65rem; color: #64748b; text-align: center;">Tự động xác nhận khi nhận tiền</span>
                                    </div>
                                </div>
                            </c:if>

                        </div>

                        <!-- Right Column: Brief profile summary card -->
                        <div class="report-pane" style="height: fit-content;">
                            <div style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.5rem; margin-bottom: 0.75rem;">
                                <h3 style="font-size: 0.95rem; font-weight: 700; color: #0f172a; margin: 0;">Sơ đồ tóm tắt học viên</h3>
                            </div>

                            <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 1rem 0;">
                                <c:choose>
                                    <c:when test="${requestScope.hasValidPhoto}">
                                        <img src="${pageContext.request.contextPath}/${profile.candidate.photoImageUrl}?t=${profile.candidateNumber}"
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
                                        <span style="color: #64748b;">CCCD:</span>
                                        <span style="font-weight: 600; color: #0f172a; font-family: monospace;">${cCccd}</span>
                                    </div>
                                    <div style="display: flex; justify-content: space-between;">
                                        <span style="color: #64748b;">Ngày sinh:</span>
                                        <span style="font-weight: 600; color: #0f172a;"><fmt:formatDate value="${cDob}" pattern="dd/MM/yyyy" /></span>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Empty desk state -->
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
                                        <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                            <c:if test="${not (c.validCapturedPhoto and c.paymentCompleted)}">
                                                <option value="${c.candidateNumber}">
                                                    ${c.candidateNumber} - ${c.fullName} (Hạng ${not empty requestScope.currentExam ? requestScope.currentExam.licenseCode : ''})
                                                </option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <jsp:include page="/views/layout/footer.jsp">
            <jsp:param name="standalone" value="false" />
        </jsp:include>
    </main>
</div>

<!-- Camera config consumed by the inline capture script below -->
<div id="procedureCameraConfig"
     data-enabled="true"
     data-sbd="${not empty requestScope.profile ? requestScope.profile.candidateNumber : ''}"
     data-ctx-path="${pageContext.request.contextPath}"
     data-endpoint="/staff/examstaff/procedure"
     hidden></div>

<script charset="UTF-8">
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('select[data-auto-submit]').forEach(function (sel) {
            sel.addEventListener('change', function () {
                this.form.submit();
            });
        });
        initFormChangeChecking();
        initWebcamCapture();
    });

    function initFormChangeChecking() {
        var form = document.querySelector('#procedureForm');
        if (!form) { return; }
        var btn = document.querySelector('#submitBtn');
        if (!btn) { return; }

        var initialValues = {};
        var inputs = form.querySelectorAll('input[name]:not([type=hidden]):not([readonly])');
        inputs.forEach(function (input) {
            initialValues[input.name] = input.value;
            input.addEventListener('input', checkChanges);
            input.addEventListener('change', checkChanges);
        });

        function checkChanges() {
            var changed = false;
            inputs.forEach(function (input) {
                if (initialValues[input.name] !== input.value) { changed = true; }
            });
            var formActionInput = document.querySelector('#formAction');
            if (changed) {
                if (formActionInput) { formActionInput.value = 'saveProfile'; }
                btn.textContent = 'Lưu thay đổi & Sang Bước 2 (Chụp ảnh) →';
                btn.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
                btn.style.borderColor = '#d97706';
            } else {
                if (formActionInput) { formActionInput.value = ''; }
                btn.textContent = 'Xác nhận & Sang Bước 2 (Chụp ảnh) →';
                btn.style.background = 'linear-gradient(135deg, #0052cc, #003d9b)';
                btn.style.borderColor = '#003d9b';
            }
        }
    }

    function initWebcamCapture() {
        var config = document.getElementById('procedureCameraConfig');
        if (!config || config.dataset.enabled !== 'true') { return; }

        var ctxPath = config.dataset.ctxPath || '';
        var endpoint = config.dataset.endpoint || '/staff/examstaff/procedure';
        var sbd = config.dataset.sbd || '';
        var video = document.getElementById('cameraVideo');
        var canvas = document.getElementById('captureCanvas');
        var captureBtn = document.getElementById('captureBtn');
        var statusEl = document.getElementById('cameraStatus');
        var errorEl = document.getElementById('cameraError');
        var mediaStream = null;

        function showError(message) {
            if (!errorEl) { return; }
            errorEl.classList.remove('is-hidden');
            errorEl.textContent = message;
            if (statusEl) { statusEl.textContent = 'Lỗi'; }
        }

        function stopCamera() {
            if (mediaStream) {
                mediaStream.getTracks().forEach(function (track) { track.stop(); });
                mediaStream = null;
            }
            if (video) { video.srcObject = null; }
        }

        function buildCompressedDataUrl(srcVideo) {
            var maxWidth = 640;
            var w = srcVideo.videoWidth;
            var h = srcVideo.videoHeight;
            if (!w || !h) { return null; }
            if (w > maxWidth) { h = Math.round(h * maxWidth / w); w = maxWidth; }
            canvas.width = w;
            canvas.height = h;
            var ctx = canvas.getContext('2d');
            ctx.drawImage(srcVideo, 0, 0, w, h);
            return canvas.toDataURL('image/jpeg', 0.82);
        }

        async function startCamera() {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                showError('Trình duyệt không hỗ trợ truy cập camera.');
                return;
            }
            if (statusEl) { statusEl.textContent = 'Đang khởi động camera...'; }
            try {
                mediaStream = await navigator.mediaDevices.getUserMedia({
                    video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
                    audio: false
                });
                video.srcObject = mediaStream;
                await video.play();
                if (statusEl) { statusEl.textContent = 'LIVE'; }
                if (captureBtn) { captureBtn.disabled = false; }
            } catch (err) {
                var msg = 'Không thể mở camera.';
                if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
                    msg = 'Quyền truy cập camera bị từ chối.';
                } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
                    msg = 'Không tìm thấy camera.';
                }
                showError(msg);
            }
        }

        async function captureAndSave() {
            if (!video || !canvas || video.readyState < 2) {
                showError('Camera chưa sẵn sàng, vui lòng thử lại.');
                return;
            }
            var dataUrl = buildCompressedDataUrl(video);
            if (!dataUrl) { showError('Không lấy được khung hình.'); return; }

            captureBtn.disabled = true;
            captureBtn.textContent = 'Đang lưu ảnh...';

            var body = new URLSearchParams();
            body.append('sbd', sbd);
            body.append('action', 'saveCapturedPhoto');
            body.append('photoBase64', dataUrl);

            try {
                var resp = await fetch(ctxPath + endpoint, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                    body: body.toString(),
                    credentials: 'same-origin'
                });
                var result = await resp.json();
                if (!resp.ok || !result.success) {
                    throw new Error(result.message || ('HTTP ' + resp.status));
                }
                stopCamera();
                window.location.href = ctxPath + endpoint + '?sbd=' + encodeURIComponent(sbd) + '&step=2#procedure-desk';
            } catch (err) {
                showError('Lưu ảnh thất bại: ' + (err.message || 'lỗi không xác định'));
                captureBtn.disabled = false;
                captureBtn.textContent = 'Chụp ảnh chân dung';
            }
        }

        if (captureBtn) { captureBtn.addEventListener('click', captureAndSave); }
        window.addEventListener('pagehide', stopCamera);
        startCamera();
    }
})();
</script>
</body>
</html>
