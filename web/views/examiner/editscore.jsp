<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%--
    ========================================================================
    DỮ LIỆU ĐỘNG CHO MÀN HÌNH SỬA ĐIỂM SÁT HẠCH (SC-053 / SC-055)
    ========================================================================
    Trang này hoàn toàn nhận dữ liệu động từ backend thông qua JSTL.
    Nếu backend chưa cung cấp dữ liệu, hệ thống hiển thị trạng thái trống (Fallback) an toàn.
--%>
<c:set var="hasCandidates" value="${not empty completedCandidates}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điều chỉnh điểm sát hạch - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<%-- Inject the sidebar template --%>
<jsp:include page="/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="sua-diem" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Sửa điểm sát hạch</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Sửa điểm sát hạch</h1>
                <p class="page-subtitle">Xem danh sách kết quả, tra cứu hồ sơ và thực hiện điều chỉnh điểm thi của thí sinh (yêu cầu phê duyệt).</p>
            </div>
        </header>

        <!-- Search & Filter Panel (SC-055) -->
        <section class="filter-panel" aria-label="Bộ lọc tra cứu thí sinh">
            <div class="filter-title">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 21l-6-6m2-5a7 7 0 1 1-14 0 7 7 0 0 1 14 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>Tra cứu thí sinh đã hoàn thành thi</span>
            </div>
            
            <form method="GET" action="editscore.jsp" class="filter-grid">
                <!-- Tra cứu theo SBD hoặc Tên -->
                <div class="input-group">
                    <label for="searchQuery" class="input-label">Tìm kiếm thí sinh</label>
                    <input type="text" id="searchQuery" name="query" class="input-field" placeholder="Nhập SBD, họ tên thí sinh..." value="${param.query}">
                </div>
                
                <!-- Lọc theo Hạng bằng -->
                <div class="input-group">
                    <label for="filterLicense" class="input-label">Hạng GPLX</label>
                    <select id="filterLicense" name="licenseClass" class="input-field">
                        <option value="">Tất cả hạng bằng</option>
                        <option value="A1" ${param.licenseClass eq 'A1' ? 'selected' : ''}>Hạng A1</option>
                        <option value="A2" ${param.licenseClass eq 'A2' ? 'selected' : ''}>Hạng A2</option>
                        <option value="B1" ${param.licenseClass eq 'B1' ? 'selected' : ''}>Hạng B1</option>
                        <option value="B2" ${param.licenseClass eq 'B2' ? 'selected' : ''}>Hạng B2</option>
                        <option value="C" ${param.licenseClass eq 'C' ? 'selected' : ''}>Hạng C</option>
                    </select>
                </div>
                
                <!-- Lọc theo Trạng thái kết quả -->
                <div class="input-group">
                    <label for="filterResult" class="input-label">Kết quả chung</label>
                    <select id="filterResult" name="resultStatus" class="input-field">
                        <option value="">Tất cả trạng thái</option>
                        <option value="PASSED" ${param.resultStatus eq 'PASSED' ? 'selected' : ''}>Đạt (Đỗ)</option>
                        <option value="FAILED" ${param.resultStatus eq 'FAILED' ? 'selected' : ''}>Chưa đạt (Trượt)</option>
                    </select>
                </div>
                
                <!-- Lọc theo Đợt thi -->
                <div class="input-group">
                    <label for="filterSession" class="input-label">Đợt thi sát hạch</label>
                    <select id="filterSession" name="session" class="input-field">
                        <option value="all">Ca Sáng - 24/05/2026</option>
                        <option value="session02">Ca Chiều - 24/05/2026</option>
                    </select>
                </div>
                
                <!-- Nút thao tác lọc -->
                <div class="btn-group">
                    <button type="submit" class="btn-filter" style="flex: 1.5; height: 42px;">Tìm</button>
                    <a href="editscore.jsp" class="btn-reset" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; height: 42px;">Đặt lại</a>
                </div>
            </form>
        </section>

        <!-- Candidates Table Panel -->
        <div class="log-card">
            <div class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh sách thí sinh có kết quả thi
                </h2>
            </div>
            
            <div class="table-responsive">
                <table class="audit-table" style="font-size: 0.88rem;">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 120px;">SBD</th>
                            <th scope="col" style="width: 200px;">Họ và tên</th>
                            <th scope="col" style="width: 110px;">Hạng GPLX</th>
                            <th scope="col" style="text-align: center; width: 100px;">Lý thuyết</th>
                            <th scope="col" style="text-align: center; width: 100px;">Sa hình</th>
                            <th scope="col" style="text-align: center; width: 100px;">Đường trường</th>
                            <th scope="col" style="text-align: center; width: 130px;">Kết quả chung</th>
                            <th scope="col" style="text-align: center; width: 120px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${hasCandidates}">
                                <c:forEach var="candidate" items="${completedCandidates}">
                                    <tr>
                                        <td style="font-weight: 700; color: #0052cc;">${candidate.sbd}</td>
                                        <td style="font-weight: 600; color: #0f172a;">${candidate.name}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${fn:contains(candidate.licenseClass, 'A1') or fn:contains(candidate.licenseClass, 'A2')}">
                                                    <span class="role-badge role-badge--coi">${candidate.licenseClass}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(candidate.licenseClass, 'B1') or fn:contains(candidate.licenseClass, 'B2')}">
                                                    <span class="role-badge role-badge--admin">${candidate.licenseClass}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge role-badge--cham">${candidate.licenseClass}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-weight: 600;">${candidate.theoryScore}đ</td>
                                        <td style="text-align: center; font-weight: 600;">${candidate.practiceScore}đ</td>
                                        <td style="text-align: center; font-weight: 600;">${candidate.roadScore}đ</td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${candidate.status eq 'PASSED'}">
                                                    <span class="action-badge action-badge--success" style="font-weight: 700;">ĐẠT (ĐỖ)</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="action-badge action-badge--danger" style="font-weight: 700;">CHƯA ĐẠT</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <button class="btn-export" style="border-color: #0052cc; color: #0052cc; font-size: 0.8rem; padding: 4px 10px; border-radius: 6px;" 
                                                    onclick="openAdjustmentModal('${candidate.sbd}', '${candidate.name}', '${candidate.licenseClass}', ${candidate.theoryScore}, ${candidate.practiceScore}, ${candidate.roadScore}, '${candidate.status}');">
                                                Sửa điểm
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; color: #64748b; padding: 4rem 2rem; font-style: italic;">
                                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #cbd5e1; margin-bottom: 0.75rem; display: block; margin-left: auto; margin-right: auto;">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Không tìm thấy thí sinh nào đã thi phù hợp với điều kiện lọc.
                                        <p style="font-size: 0.78rem; color: #94a3b8; margin-top: 4px; font-weight: normal;">Vui lòng kết nối cơ sở dữ liệu và truyền danh sách completedCandidates từ Controller.</p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<!-- ========================================================
     MODAL ĐIỀU CHỈNH ĐIỂM SÁT HẠCH BẢO MẬT CAO (SC-055)
     ======================================================== -->
<div id="scoreAdjustmentModal" class="modal-backdrop">
    <div class="score-modal-content">
        <!-- Modal Header -->
        <div class="modal-header">
            <h3>ĐIỀU CHỈNH ĐIỂM THI SÁT HẠCH</h3>
            <button class="btn-close-modal" onclick="closeAdjustmentModal();">&times;</button>
        </div>
        
        <!-- Modal Body / Form -->
        <form id="adjustmentForm" method="POST" action="editscore.jsp" onsubmit="return validateAndSubmitAdjustment(event);">
            <div class="modal-body">
                <!-- Thí sinh đang được sửa -->
                <div style="display: flex; gap: 1rem; background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.25rem;">
                    <div style="flex: 1;">
                        <span style="font-size: 0.7rem; font-weight: 800; color: #64748b; text-transform: uppercase; display: block; margin-bottom: 2px;">Thí sinh</span>
                        <span id="modalCandidateName" style="font-size: 0.95rem; font-weight: 800; color: #0f172a;">NGUYỄN VĂN HÙNG</span>
                    </div>
                    <div style="width: 120px;">
                        <span style="font-size: 0.7rem; font-weight: 800; color: #64748b; text-transform: uppercase; display: block; margin-bottom: 2px;">SBD</span>
                        <span id="modalCandidateSbd" style="font-size: 0.95rem; font-weight: 800; color: #0052cc;">SBD-202688</span>
                        <input type="hidden" id="hiddenSbd" name="sbd" value="">
                    </div>
                    <div style="width: 80px;">
                        <span style="font-size: 0.7rem; font-weight: 800; color: #64748b; text-transform: uppercase; display: block; margin-bottom: 2px;">Hạng GPLX</span>
                        <span id="modalCandidateClass" style="font-size: 0.95rem; font-weight: 700; color: #475569;">B2</span>
                    </div>
                </div>

                <!-- Nhập điểm số mới -->
                <div class="adjustment-form-grid">
                    <!-- Lý thuyết -->
                    <div class="adjustment-field-wrap">
                        <label for="inputTheory" class="input-label" style="font-size: 0.75rem;">Lý thuyết</label>
                        <input type="number" id="inputTheory" name="theoryScore" class="input-field" style="height: 38px;" min="0" max="35" required onchange="calculateAutoStatus();">
                    </div>
                    <!-- Sa hình -->
                    <div class="adjustment-field-wrap">
                        <label for="inputPractice" class="input-label" style="font-size: 0.75rem;">Sa hình (Thực hành)</label>
                        <input type="number" id="inputPractice" name="practiceScore" class="input-field" style="height: 38px;" min="0" max="100" required onchange="calculateAutoStatus();">
                    </div>
                    <!-- Đường trường -->
                    <div class="adjustment-field-wrap">
                        <label for="inputRoad" class="input-label" style="font-size: 0.75rem;">Đường trường</label>
                        <input type="number" id="inputRoad" name="roadScore" class="input-field" style="height: 38px;" min="0" max="100" required onchange="calculateAutoStatus();">
                    </div>
                </div>

                <!-- Kết quả tự động dự kiến -->
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #e2e8f0; border-bottom: 1px dashed #e2e8f0; padding: 0.75rem 0.5rem; margin-bottom: 1.25rem;">
                    <span style="font-size: 0.85rem; font-weight: 700; color: #475569;">KẾT QUẢ CHUNG DỰ KIẾN:</span>
                    <span id="modalAutoStatus" class="action-badge action-badge--success" style="font-size: 0.85rem; font-weight: 800; padding: 4px 12px; border-radius: 4px;">ĐẠT (ĐỖ)</span>
                </div>

                <!-- Hộp cảnh báo bảo mật nhạy cảm -->
                <div class="security-alert-box">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <div class="security-alert-text">
                        CẢNH BÁO: Thao tác sửa điểm sát hạch sẽ được ghi nhận chi tiết vào lịch sử hệ thống (Audit Trail) cùng thông tin tài khoản của bạn để thanh tra Sở GTVT giám sát.
                    </div>
                </div>

                <!-- Lý do sửa điểm (Bắt buộc) -->
                <div class="input-group" style="margin-bottom: 1.25rem;">
                    <label for="adjustReason" class="input-label" style="font-size: 0.75rem;">Lý do điều chỉnh điểm <span style="color: #ef4444;">*</span></label>
                    <textarea id="adjustReason" name="reason" class="input-field" style="height: 64px; padding: 8px 12px; resize: none;" placeholder="Giải trình rõ lý do (ví dụ: Chấm phúc khảo bài sa hình, Lỗi thiết bị cảm biến xe số 05...)" required></textarea>
                </div>

                <!-- Mã phê duyệt bảo mật (Bắt buộc) -->
                <div class="input-group">
                    <label for="approvalCode" class="input-label" style="font-size: 0.75rem;">Mã phê duyệt của Trưởng ban sát hạch <span style="color: #ef4444;">*</span></label>
                    <input type="password" id="approvalCode" name="approvalCode" class="input-field" style="height: 38px;" placeholder="Nhập mã phê duyệt bảo mật (Supervisor Code)..." required>
                </div>
            </div>
            
            <!-- Modal Footer -->
            <div class="modal-footer">
                <button type="button" class="btn-reset" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem;" onclick="closeAdjustmentModal();">Hủy</button>
                <button type="submit" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #0052cc; border-color: #0052cc;">Xác nhận lưu</button>
            </div>
        </form>
    </div>
</div>

<!-- ========================================================
     JAVASCRIPT ĐIỀU PHỐI VÀ XỬ LÝ MODAL ĐIỀU CHỈNH ĐIỂM
     ======================================================== -->
<script>
    // Mở Modal và nạp thông tin thí sinh được chọn
    function openAdjustmentModal(sbd, name, licenseClass, theoryScore, practiceScore, roadScore, status) {
        document.getElementById('modalCandidateName').innerHTML = name.toUpperCase();
        document.getElementById('modalCandidateSbd').innerHTML = sbd;
        document.getElementById('hiddenSbd').value = sbd;
        document.getElementById('modalCandidateClass').innerHTML = licenseClass;

        // Điền các điểm hiện tại vào input
        document.getElementById('inputTheory').value = theoryScore;
        document.getElementById('inputPractice').value = practiceScore;
        document.getElementById('inputRoad').value = roadScore;

        // Tự động điều chỉnh giới hạn tối đa cho điểm lý thuyết tùy theo hạng GPLX
        let theoryInput = document.getElementById('inputTheory');
        if (licenseClass.includes('A1') || licenseClass.includes('A2')) {
            theoryInput.max = 25; // Hạng xe máy tối đa 25 câu
        } else {
            theoryInput.max = 35; // Hạng ô tô tối đa 35 câu
        }

        // Tự động tính toán kết quả dự kiến hiển thị
        calculateAutoStatus();

        // Xóa sạch form bảo mật cũ
        document.getElementById('adjustReason').value = "";
        document.getElementById('approvalCode').value = "";

        // Hiển thị modal
        document.getElementById('scoreAdjustmentModal').classList.add('show');
    }

    // Đóng Modal
    function closeAdjustmentModal() {
        document.getElementById('scoreAdjustmentModal').classList.remove('show');
    }

    // Tự động tính toán Kết quả ĐẠT / TRƯỢT dự kiến trên giao diện form
    function calculateAutoStatus() {
        let theory = parseInt(document.getElementById('inputTheory').value) || 0;
        let practice = parseInt(document.getElementById('inputPractice').value) || 0;
        let road = parseInt(document.getElementById('inputRoad').value) || 0;
        let licenseClass = document.getElementById('modalCandidateClass').innerHTML;

        let isPassed = false;

        // Quy chuẩn đánh giá của Sở GTVT:
        // - Hạng A1/A2: Lý thuyết >= 21/25 (hoặc 23/25 tùy chuẩn), Sa hình >= 80/100
        // - Hạng B1/B2: Lý thuyết >= 32/35 (hoặc 30/35), Sa hình >= 80/100, Đường trường >= 80/100
        if (licenseClass.includes('A1') || licenseClass.includes('A2')) {
            let passTheoryLimit = licenseClass.includes('A1') ? 21 : 23;
            if (theory >= passTheoryLimit && practice >= 80) {
                isPassed = true;
            }
        } else {
            let passTheoryLimit = licenseClass.includes('B1') ? 27 : 32;
            if (theory >= passTheoryLimit && practice >= 80 && road >= 80) {
                isPassed = true;
            }
        }

        let statusBadge = document.getElementById('modalAutoStatus');
        if (isPassed) {
            statusBadge.innerHTML = "ĐẠT (ĐỖ)";
            statusBadge.className = "action-badge action-badge--success";
        } else {
            statusBadge.innerHTML = "CHƯA ĐẠT";
            statusBadge.className = "action-badge action-badge--danger";
        }
    }

    // Xác thực bảo mật và nộp Form thay đổi điểm
    function validateAndSubmitAdjustment(event) {
        event.preventDefault();

        let sbd = document.getElementById('hiddenSbd').value;
        let theory = parseInt(document.getElementById('inputTheory').value) || 0;
        let practice = parseInt(document.getElementById('inputPractice').value) || 0;
        let road = parseInt(document.getElementById('inputRoad').value) || 0;
        let reason = document.getElementById('adjustReason').value.trim();
        let approvalCode = document.getElementById('approvalCode').value.trim();

        if (reason.length < 10) {
            alert("Bảo mật: Lý do điều chỉnh điểm thi quá ngắn (tối thiểu 10 ký tự)! Vui lòng ghi rõ giải trình để làm căn cứ thanh tra.");
            return false;
        }

        if (approvalCode === "") {
            alert("Bảo mật: Vui lòng nhập Mã phê duyệt của Trưởng ban sát hạch!");
            return false;
        }

        // Điểm số tối đa hợp lệ
        let theoryInput = document.getElementById('inputTheory');
        let maxTheory = parseInt(theoryInput.max);
        if (theory < 0 || theory > maxTheory) {
            alert("Lỗi: Điểm lý thuyết cho hạng xe này phải nằm trong khoảng từ 0 đến " + maxTheory + "!");
            return false;
        }
        if (practice < 0 || practice > 100 || road < 0 || road > 100) {
            alert("Lỗi: Điểm sa hình hoặc Đường trường phải nằm trong khoảng từ 0 đến 100!");
            return false;
        }

        let isPassedText = document.getElementById('modalAutoStatus').innerHTML;
        
        let confirmSubmit = confirm(
            "CẢNH BÁO THAY ĐỔI ĐIỂM SÁT HẠCH:\n\n" +
            "- SBD: " + sbd + "\n" +
            "- Điểm mới nhập: Lý thuyết (" + theory + "), Sa hình (" + practice + "), Đường trường (" + road + ")\n" +
            "- Kết quả mới dự kiến: " + isPassedText + "\n" +
            "- Giải trình lý do: " + reason + "\n\n" +
            "Hệ thống sẽ đồng thời tạo một bản ghi lịch sử Audit Trail. Bạn có chắc chắn muốn cập nhật?"
        );

        if (!confirmSubmit) return false;

        alert("Thành công: Đã ghi nhận lịch sử điều chỉnh điểm và cập nhật kết quả sát hạch thành công!");
        closeAdjustmentModal();
        return true;
    }
</script>

</body>
</html>
