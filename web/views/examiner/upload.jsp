<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tải danh sách thí sinh từ Excel - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="tai-ds" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tải DS Thí sinh</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tải danh sách thí sinh</h1>
                <p class="page-subtitle">Nhập danh sách hồ sơ thí sinh từ tệp Excel để tổ chức ca thi sát hạch lái xe.</p>
            </div>
            
            <div class="page-actions">
                <a href="${pageContext.request.contextPath}/assets/templates/danh_sach_mau.xlsx" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #0052cc; border-color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tải file mẫu Excel
                </a>
            </div>
        </header>

        <div class="report-grid" id="uploadWorkspaceZone" style="grid-template-columns: 1.2fr 1fr;">
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: center;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem;">Tải tệp dữ liệu lên</h2>
                </div>
                
                <form action="${pageContext.request.contextPath}/examiner/upload" method="POST" enctype="multipart/form-data" style="display: flex; flex-direction: column; align-items: center; gap: 1.5rem; width: 100%;">
                    <div class="upload-dropzone" style="cursor: pointer; width: 100%; box-sizing: border-box;">
                        <div class="dropzone-icon">
                            <img src="${pageContext.request.contextPath}/assets/imgs/cloud-upload.svg" alt="Tải lên đám mây" style="width: 48px; height: 48px;">
                        </div>
                        <span style="font-size: 1rem; font-weight: 700; color: #0f172a; display: block; margin-bottom: 0.5rem;">Chọn tệp danh sách thí sinh</span>
                        <span style="font-size: 0.82rem; color: #64748b; display: block; margin-bottom: 1rem;">Hỗ trợ định dạng: .xlsx, .xls, .csv (Tối đa 15MB)</span>
                        <input type="file" name="file" accept=".xlsx, .xls, .csv" required style="font-size: 0.85rem; color: #475569;">
                    </div>
                    <button type="submit" class="btn-filter" style="height: 42px; padding: 0 2rem; font-size: 0.9rem; border-radius: 8px; width: 100%; justify-content: center;">Tải lên và phân tích</button>
                </form>
            </div>
            
            <div class="report-pane">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0.75rem;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem; color: #003d9b; display: inline-flex; align-items: center; gap: 6px;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Quy tắc chuẩn hóa tệp Excel
                    </h2>
                </div>
                
                <p style="font-size: 0.85rem; color: #475569; margin-bottom: 1rem; line-height: 1.5;">Vui lòng điều chỉnh các cột dữ liệu trong tệp Excel của bạn khớp chính xác với cấu trúc cột mẫu:</p>
                
                <div style="display: flex; flex-direction: column; gap: 0.65rem;">
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT A</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Số báo danh:</strong> Định dạng chữ và số viết liền.</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT B</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Họ và tên:</strong> Chữ viết hoa có dấu tiếng Việt.</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT C</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Ngày sinh:</strong> Định dạng DD/MM/YYYY.</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT D</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Số CCCD/CMND:</strong> Chuỗi 12 chữ số hợp lệ.</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT E</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Hạng GPLX:</strong> Chỉ nhận giá trị: A1, A2, B1, B2, C.</span>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${not empty importedCandidates}">
            <div class="log-card" style="margin-top: 2rem; margin-bottom: 2.5rem;">
                <div class="log-card-header" style="justify-content: space-between;">
                    <h2 class="log-card-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                            <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Xem trước dữ liệu thí sinh nhập khẩu (Phân tích từ Excel)
                    </h2>
                    
                    <form action="${pageContext.request.contextPath}/examiner/confirm-import" method="POST" style="margin: 0;">
                        <div style="display: flex; gap: 10px; flex-shrink: 0; align-items: center;">
                            <a href="${pageContext.request.contextPath}/views/examiner/upload.jsp" class="btn-reset" style="height: 36px; padding: 0 1rem; font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; justify-content: center;">Hủy bỏ</a>
                            <button type="submit" class="btn-filter" style="height: 36px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #10b981; border-color: #10b981; white-space: nowrap;">Xác nhận</button>
                        </div>
                    </form>
                </div>
                
                <div class="table-responsive">
                    <table class="audit-table" style="font-size: 0.88rem;">
                        <thead>
                            <tr>
                                <th scope="col" style="width: 120px;">SBD</th>
                                <th scope="col" style="width: 200px;">Họ và tên</th>
                                <th scope="col" style="width: 120px; text-align: center;">Ngày sinh</th>
                                <th scope="col" style="width: 160px; text-align: center;">Số CCCD</th>
                                <th scope="col" style="width: 110px;">Hạng GPLX</th>
                                <th scope="col" style="text-align: center; width: 120px;">Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="candidate" items="${importedCandidates}">
                                <tr>
                                    <td style="font-weight: 700; color: #0052cc;">${candidate.sbd}</td>
                                    <td style="font-weight: 600; color: #0f172a;">${candidate.name}</td>
                                    <td style="text-align: center; font-weight: 500;">${candidate.dob}</td>
                                    <td style="text-align: center; font-family: monospace;">${candidate.cccd}</td>
                                    <td><span class="role-badge role-badge--admin">${candidate.licenseClass}</span></td>
                                    <td style="text-align: center;">
                                        <span class="action-badge action-badge--success" style="font-weight: 700;">HỢP LỆ</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
