<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nộp hồ sơ sát hạch - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="upload-documents" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/views/registrant/dashboard.jsp">Trang chủ</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Nộp hồ sơ</span>
    </nav>

    <header class="page-header">
        <div class="page-title-wrap">
            <h1 class="page-title">Nộp hồ sơ sát hạch</h1>
            <p class="page-subtitle">Tải đầy đủ giấy tờ và gửi Ban quản lý thẩm định trước khi đăng ký lịch thi.</p>
        </div>
        <span class="action-badge action-badge--warning">${empty dossier.status ? 'Draft' : dossier.status}</span>
    </header>

    <c:if test="${not empty dossierError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b">${dossierError}</div>
    </c:if>
    <c:if test="${not empty sessionScope.dossierSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857">${sessionScope.dossierSuccess}</div>
        <c:remove var="dossierSuccess" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${dossier.status eq 'Approved'}">
            <section class="report-pane" style="padding:2rem;text-align:center">
                <h2 style="color:#047857">Hồ sơ đã được xác minh</h2>
                <p>Bạn có thể chuyển sang bước đăng ký lịch sát hạch.</p>
                <a class="btn-filter" href="${ctx}/views/registrant/register-exam.jsp">Đăng ký lịch thi</a>
            </section>
        </c:when>
        <c:otherwise>
            <form action="${ctx}/registrant/dossier" method="post" enctype="multipart/form-data">
                <section class="p-form-card" style="margin-bottom:1.5rem">
                    <div class="p-form-header"><h2 class="p-form-title">Thông tin đăng ký</h2></div>
                    <div class="p-form-body">
                        <div class="p-form-grid">
                            <div class="p-input-group">
                                <label class="p-input-label" for="applicantType">Phân loại học viên</label>
                                <select class="p-input-field" id="applicantType" name="applicantType" required>
                                    <option value="free">Thí sinh tự do</option>
                                    <option value="student">Học viên chính khóa</option>
                                </select>
                            </div>
                            <div class="p-input-group">
                                <label class="p-input-label" for="licenceClass">Hạng GPLX</label>
                                <select class="p-input-field" id="licenceClass" name="licenceClass" required>
                                    <option value="A1">A1</option>
                                    <option value="A2">A2 / A</option>
                                    <option value="B1">B1</option>
                                    <option value="B2">B2 / B</option>
                                    <option value="C">C</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </section>

                <div class="report-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem">
                    <c:set var="portrait" value="${dossier.documents['PORTRAIT']}" />
                    <c:set var="idFront" value="${dossier.documents['ID_FRONT']}" />
                    <c:set var="idBack" value="${dossier.documents['ID_BACK']}" />
                    <c:set var="health" value="${dossier.documents['HEALTH_CERTIFICATE']}" />
                    <section class="p-form-card">
                        <div class="p-form-header"><h3 class="p-form-title">Ảnh chân dung</h3></div>
                        <div class="p-form-body">
                            <c:if test="${not empty portrait}"><a href="${ctx}${portrait.documentUrl}" target="_blank">Xem tệp đã tải</a></c:if>
                            <input class="p-input-field" type="file" name="portrait" accept=".jpg,.jpeg,.png">
                        </div>
                    </section>
                    <section class="p-form-card">
                        <div class="p-form-header"><h3 class="p-form-title">CCCD mặt trước</h3></div>
                        <div class="p-form-body">
                            <c:if test="${not empty idFront}"><a href="${ctx}${idFront.documentUrl}" target="_blank">Xem tệp đã tải</a></c:if>
                            <input class="p-input-field" type="file" name="idFront" accept=".jpg,.jpeg,.png,.pdf">
                        </div>
                    </section>
                    <section class="p-form-card">
                        <div class="p-form-header"><h3 class="p-form-title">CCCD mặt sau</h3></div>
                        <div class="p-form-body">
                            <c:if test="${not empty idBack}"><a href="${ctx}${idBack.documentUrl}" target="_blank">Xem tệp đã tải</a></c:if>
                            <input class="p-input-field" type="file" name="idBack" accept=".jpg,.jpeg,.png,.pdf">
                        </div>
                    </section>
                    <section class="p-form-card">
                        <div class="p-form-header"><h3 class="p-form-title">Giấy khám sức khỏe</h3></div>
                        <div class="p-form-body">
                            <c:if test="${not empty health}"><a href="${ctx}${health.documentUrl}" target="_blank">Xem tệp đã tải</a></c:if>
                            <input class="p-input-field" type="file" name="healthCertificate" accept=".jpg,.jpeg,.png,.pdf">
                        </div>
                    </section>
                </div>

                <div class="upload-action-bar" style="margin-top:1.5rem">
                    <div class="upload-action-bar__info">
                        <strong>${dossier.documentCount}/4 tài liệu đã có</strong>
                        <span>Mỗi tệp tối đa 5 MB, định dạng JPG, PNG hoặc PDF.</span>
                    </div>
                    <div class="upload-action-bar__buttons">
                        <button class="btn-export" type="submit" name="action" value="save">Lưu bản nháp</button>
                        <button class="btn-filter" type="submit" name="action" value="submit">Gửi duyệt hồ sơ</button>
                    </div>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/layout/footer.jsp" />
</div>
</body>
</html>
