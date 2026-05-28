<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="hasCandidates" value="${not empty completedCandidates}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điều chỉnh điểm sát hạch - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="sua-diem" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Sửa điểm sát hạch</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Sửa điểm sát hạch</h1>
                <p class="page-subtitle">Xem danh sách kết quả, tra cứu hồ sơ và thực hiện điều chỉnh điểm thi của thí sinh.</p>
            </div>
        </header>

        <c:if test="${not empty param.editSbd}">
            <section class="filter-panel" aria-label="Form điều chỉnh điểm" style="border: 1px solid #0052cc; background-color: rgba(0, 82, 204, 0.01);">
                <div class="filter-title" style="color: #0052cc; border-bottom: 1px solid rgba(0, 82, 204, 0.1); padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span>Form điều chỉnh điểm thi: ${param.editName} (${param.editSbd})</span>
                </div>
                
                <form method="POST" action="${pageContext.request.contextPath}/examiner/edit-score" style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <input type="hidden" name="sbd" value="${param.editSbd}">
                    
                    <div class="filter-grid" style="grid-template-columns: 1fr 1fr 1fr 1.5fr;">
                        <div class="input-group">
                            <label class="input-label">Hạng GPLX</label>
                            <input type="text" class="input-field" value="${param.editClass}" readonly style="background-color: #f1f5f9;">
                        </div>
                        <div class="input-group">
                            <label for="inputTheory" class="input-label">Điểm Lý thuyết</label>
                            <input type="number" id="inputTheory" name="theoryScore" class="input-field" min="0" max="${fn:contains(param.editClass, 'A1') or fn:contains(param.editClass, 'A2') ? 25 : 35}" value="${param.editTheory}" required>
                        </div>
                        <div class="input-group">
                            <label for="inputPractice" class="input-label">Điểm Sa hình</label>
                            <input type="number" id="inputPractice" name="practiceScore" class="input-field" min="0" max="100" value="${param.editPractice}" required>
                        </div>
                        <div class="input-group">
                            <label for="inputRoad" class="input-label">Điểm Đường trường</label>
                            <input type="number" id="inputRoad" name="roadScore" class="input-field" min="0" max="100" value="${param.editRoad}" required>
                        </div>
                    </div>

                    <div style="display: flex; gap: 1.25rem; flex-wrap: wrap;">
                        <div class="input-group" style="flex: 2; min-width: 300px;">
                            <label for="adjustReason" class="input-label">Lý do điều chỉnh điểm <span style="color: #ef4444;">*</span></label>
                            <textarea id="adjustReason" name="reason" class="input-field" style="height: 42px; padding: 8px 12px; resize: none;" placeholder="Giải trình rõ lý do (Tối thiểu 10 ký tự)..." required minlength="10"></textarea>
                        </div>
                        <div class="input-group" style="flex: 1; min-width: 200px;">
                            <label for="approvalCode" class="input-label">Mã phê duyệt Trưởng ban <span style="color: #ef4444;">*</span></label>
                            <input type="password" id="approvalCode" name="approvalCode" class="input-field" placeholder="Nhập mã bảo mật..." required>
                        </div>
                    </div>

                    <div class="security-alert-box" style="margin: 0;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <div class="security-alert-text" style="color: #ea580c;">
                            CẢNH BÁO: Thao tác sửa điểm sẽ lưu vào nhật ký kiểm toán hệ thống (Audit Trail) phục vụ công tác thanh tra.
                        </div>
                    </div>

                    <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 0.5rem;">
                        <a href="editscore.jsp" class="btn-reset" style="height: 42px; padding: 0 1.5rem; text-decoration: none; display: inline-flex; align-items: center; justify-content: center;">Hủy bỏ</a>
                        <button type="submit" class="btn-filter" style="height: 42px; padding: 0 2rem; background-color: #0052cc; border-color: #0052cc;">Xác nhận lưu</button>
                    </div>
                </form>
            </section>
        </c:if>

        <section class="filter-panel" aria-label="Bộ lọc tra cứu thí sinh">
            <div class="filter-title">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 21l-6-6m2-5a7 7 0 1 1-14 0 7 7 0 0 1 14 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>Tra cứu thí sinh đã hoàn thành thi</span>
            </div>
            
            <form method="GET" action="editscore.jsp" class="filter-grid">
                <div class="input-group">
                    <label for="searchQuery" class="input-label">Tìm kiếm thí sinh</label>
                    <input type="text" id="searchQuery" name="query" class="input-field" placeholder="Nhập SBD, họ tên thí sinh..." value="${param.query}">
                </div>
                
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
                
                <div class="input-group">
                    <label for="filterResult" class="input-label">Kết quả chung</label>
                    <select id="filterResult" name="resultStatus" class="input-field">
                        <option value="">Tất cả trạng thái</option>
                        <option value="PASSED" ${param.resultStatus eq 'PASSED' ? 'selected' : ''}>Đạt (Đỗ)</option>
                        <option value="FAILED" ${param.resultStatus eq 'FAILED' ? 'selected' : ''}>Chưa đạt (Trượt)</option>
                    </select>
                </div>
                
                <div class="input-group">
                    <label for="filterSession" class="input-label">Đợt thi sát hạch</label>
                    <select id="filterSession" name="session" class="input-field">
                        <option value="all">Ca Sáng - 24/05/2026</option>
                        <option value="session02">Ca Chiều - 24/05/2026</option>
                    </select>
                </div>
                
                <div class="btn-group">
                    <button type="submit" class="btn-filter" style="flex: 1.5; height: 42px;">Tìm</button>
                    <a href="editscore.jsp" class="btn-reset" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; height: 42px;">Đặt lại</a>
                </div>
            </form>
        </section>

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
                                            <a href="editscore.jsp?editSbd=${candidate.sbd}&editName=${candidate.name}&editClass=${candidate.licenseClass}&editTheory=${candidate.theoryScore}&editPractice=${candidate.practiceScore}&editRoad=${candidate.roadScore}&query=${param.query}&licenseClass=${param.licenseClass}&resultStatus=${param.resultStatus}" 
                                               class="btn-export" style="border-color: #0052cc; color: #0052cc; font-size: 0.8rem; padding: 4px 10px; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; height: 26px;">
                                                Sửa điểm
                                            </a>
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

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
