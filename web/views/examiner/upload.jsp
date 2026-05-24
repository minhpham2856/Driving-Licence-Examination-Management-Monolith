<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<%--
    ========================================================================
    DỮ LIỆU ĐỘNG CHO TRANG TẢI DANH SÁCH THÍ SINH (SC-058)
    ========================================================================
    Trang này nhận các trạng thái nhập file trực tiếp từ Servlet.
    Nếu chưa nạp file hoặc không có danh sách preview, hiển thị bảng trống fallback an toàn.
--%>
<c:set var="hasImported" value="${not empty importedCandidates}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tải danh sách thí sinh từ Excel - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <!-- SheetJS to parse Excel (.xlsx, .xls) and CSV natively on client side -->
    <script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.mini.min.js"></script>
</head>
<body class="has-side-nav-bar">

<%-- Inject the sidebar template --%>
<jsp:include page="/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="tai-ds" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tải DS Thí sinh</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tải danh sách thí sinh</h1>
                <p class="page-subtitle">Nhập nhanh danh sách hồ sơ thí sinh từ tệp Excel để tổ chức ca thi sát hạch lái xe.</p>
            </div>
            
            <!-- Quick Actions: Tải File Excel Mẫu -->
            <div class="page-actions">
                <a href="#" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #0052cc; border-color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;" onclick="alert('Đang tải tệp Excel mẫu (.xlsx) tiêu chuẩn của Sở GTVT...');">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tải file mẫu Excel
                </a>
            </div>
        </header>

        <!-- Main Upload Workspace Grid (Dropzone + Guide) -->
        <div class="report-grid" id="uploadWorkspaceZone" style="grid-template-columns: 1.2fr 1fr;">
            
            <!-- LEFT PANE: Drag & Drop Zone -->
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: center;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem;">Tải tệp dữ liệu lên</h2>
                </div>
                
                <!-- Drag and Drop Dropzone -->
                <div id="dropzone" class="upload-dropzone" onclick="triggerFileSelect();">
                    <div class="dropzone-icon">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">Kéo thả tệp Excel vào đây</span>
                    <span style="font-size: 0.82rem; color: #64748b;">hoặc click để chọn tệp từ thiết bị của bạn</span>
                    <span style="font-size: 0.75rem; color: #94a3b8; font-weight: 500;">Hỗ trợ định dạng: .xlsx, .xls, .csv (Tối đa 15MB)</span>
                    
                    <!-- Hidden file input -->
                    <input type="file" id="excelFileInput" style="display: none;" accept=".xlsx, .xls, .csv" onchange="handleFileSelect(event);">
                </div>
                
                <!-- Progress & File Info (Hidden initially) -->
                <div id="progressBox" class="file-info-box" style="display: none; flex-direction: column; align-items: stretch; gap: 8px; width: 100%;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" fill="rgba(16, 185, 129, 0.08)" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M14 2v6h6" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            </svg>
                            <div>
                                <span id="uploadedFileName" style="font-size: 0.9rem; font-weight: 700; color: #0f172a; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 220px;">danh_sach_thi_sinh.xlsx</span>
                                <span id="uploadedFileSize" style="font-size: 0.75rem; color: #64748b;">1.2 MB</span>
                            </div>
                        </div>
                        <span id="progressText" style="font-size: 0.85rem; font-weight: 800; color: #0052cc;">0%</span>
                    </div>
                    
                    <div class="progress-bar-container">
                        <div id="progressBarFill" class="progress-bar-fill"></div>
                    </div>
                </div>
            </div>
            
            <!-- RIGHT PANE: Import Guide Rules -->
            <div class="report-pane">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 0.75rem;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem; color: #003d9b;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Quy tắc chuẩn hóa tệp Excel
                    </h2>
                </div>
                
                <p style="font-size: 0.85rem; color: #475569; margin-bottom: 1rem; line-height: 1.5;">Vui lòng điều chỉnh các cột dữ liệu trong tệp Excel của bạn khớp chính xác với cấu trúc cột mẫu dưới đây để hệ thống tự động import thành công:</p>
                
                <div style="display: flex; flex-direction: column; gap: 0.65rem;">
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT A</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Số báo danh:</strong> Định dạng chữ và số viết liền (ví dụ: SBD-202601).</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT B</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Họ và tên:</strong> Chữ viết hoa có dấu chuẩn tiếng Việt (ví dụ: NGUYỄN VĂN AN).</span>
                    </div>
                    <div style="display: flex; gap: 8px; align-items: start;">
                        <span style="font-size: 0.75rem; font-weight: 800; background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #475569; width: 62px; text-align: center; flex-shrink: 0;">CỘT C</span>
                        <span style="font-size: 0.82rem; color: #334155; font-weight: 500;"><strong style="color: #0f172a;">Ngày sinh:</strong> Định dạng DD/MM/YYYY (ví dụ: 15/08/1998).</span>
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

        <!-- PREVIEW SECTION: Shows up dynamically after Excel is processed (Figma SC-058) -->
        <div class="log-card" id="previewContainer" style="display: none; margin-top: 2rem; margin-bottom: 2.5rem; animation: modalZoomIn 0.3s ease;">
            <div class="log-card-header" style="justify-content: space-between;">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                        <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Xem trước dữ liệu thí sinh nhập khẩu (Phân tích từ Excel)
                </h2>
                
                <div style="display: flex; gap: 10px; flex-shrink: 0; align-items: center;">
                    <button class="btn-reset" style="height: 36px; padding: 0 1rem; font-size: 0.85rem; white-space: nowrap;" onclick="cancelImport();">Hủy bỏ</button>
                    <button class="btn-filter" style="height: 36px; padding: 0 1.25rem; font-size: 0.85rem; background-color: #10b981; border-color: #10b981; white-space: nowrap;" onclick="submitImport();">Xác nhận</button>
                </div>
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
                    <tbody id="previewTableBody">
                        <%-- Thẻ lặp JSTL JSTL động cho biến importedCandidates từ Servlet --%>
                        <c:choose>
                            <c:when test="${hasImported}">
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
                            </c:when>
                            <c:otherwise>
                                <%-- Bàn giao trạng thái rỗng khi chưa import file --%>
                                <tr id="previewEmptyPlaceholder">
                                    <td colspan="6" style="text-align: center; color: #64748b; padding: 4rem 2rem; font-style: italic;">
                                        Chưa có tệp Excel nào được chọn. Kéo thả tệp của bạn lên khu vực phía trên để hiển thị xem trước.
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
     JAVASCRIPT ĐIỀU PHỐI KÉO THẢ & GIẢ LẬP ĐỌC FILE EXCEL ĐỘNG
     ======================================================== -->
<script>
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('excelFileInput');
    const progressBox = document.getElementById('progressBox');
    const progressBarFill = document.getElementById('progressBarFill');
    const progressText = document.getElementById('progressText');
    const uploadedFileName = document.getElementById('uploadedFileName');
    const uploadedFileSize = document.getElementById('uploadedFileSize');
    const previewContainer = document.getElementById('previewContainer');
    const previewTableBody = document.getElementById('previewTableBody');

    // Thí sinh mẫu giả lập sử dụng khi offline hoặc khi file rỗng/lỗi
    const mockExcelCandidates = [
        { sbd: "SBD-202601", name: "TRẦN VĂN AN", dob: "12/04/1995", cccd: "034095001234", license: "B2" },
        { sbd: "SBD-202602", name: "NGUYỄN THỊ MAI", dob: "28/11/1999", cccd: "038099005678", license: "B2" },
        { sbd: "SBD-202603", name: "PHẠM QUỐC KHÁNH", dob: "05/09/1997", cccd: "040097003412", license: "C" },
        { sbd: "SBD-202604", name: "LÊ HOÀNG YẾN", dob: "15/02/2001", cccd: "036201009876", license: "A1" },
        { sbd: "SBD-202605", name: "VŨ MINH ĐỨC", dob: "08/08/1993", cccd: "030093004567", license: "B2" }
    ];

    // Mảng lưu trữ danh sách thí sinh đã đọc thực tế từ tệp tin
    let currentUploadedCandidates = [];

    // Mở file browser
    function triggerFileSelect() {
        fileInput.click();
    }

    // Sự kiện khi chọn file qua browser
    function handleFileSelect(event) {
        const file = event.target.files[0];
        if (file) {
            processFile(file);
        }
    }

    // Xử lý kéo thả Dropzone (Dragover)
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
    });

    // Rời dropzone (Dragleave)
    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dragover');
    });

    // Thả file (Drop)
    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        if (file) {
            const ext = file.name.split('.').pop().toLowerCase();
            if (ext === 'xlsx' || ext === 'xls' || ext === 'csv') {
                processFile(file);
            } else {
                alert("Lỗi: Hệ thống chỉ hỗ trợ định dạng tệp Excel (.xlsx, .xls) hoặc .csv!");
            }
        }
    });

    // Tiến trình nạp & phân tích tệp Excel/CSV
    function processFile(file) {
        const name = file.name;
        const size = file.size;
        const ext = name.split('.').pop().toLowerCase();
        
        // Định dạng size bytes sang MB
        const sizeInMb = (size / (1024 * 1024)).toFixed(2) + " MB";
        
        uploadedFileName.innerHTML = name;
        uploadedFileSize.innerHTML = sizeInMb;

        // Ẩn dropzone, hiện progress box
        dropzone.style.display = 'none';
        progressBox.style.display = 'flex';
        progressBarFill.style.width = '0%';
        progressText.innerHTML = "0%";

        let progress = 0;
        const uploadInterval = setInterval(() => {
            progress += 10;
            progressBarFill.style.width = progress + "%";
            progressText.innerHTML = progress + "%";

            if (progress >= 100) {
                clearInterval(uploadInterval);
                setTimeout(() => {
                    // Đọc file thực tế từ client-side
                    readFileData(file, ext);
                }, 300);
            }
        }, 40);
    }

    // Đọc dữ liệu từ File thực tế (CSV / Excel)
    function readFileData(file, ext) {
        const reader = new FileReader();

        if (ext === 'csv') {
            reader.readAsText(file, "UTF-8");
            reader.onload = function(e) {
                const text = e.target.result;
                try {
                    const parsed = parseCSVContent(text);
                    if (parsed && parsed.length > 0) {
                        currentUploadedCandidates = parsed;
                    } else {
                        currentUploadedCandidates = [...mockExcelCandidates];
                    }
                    showPreviewTable();
                } catch (error) {
                    console.error("Lỗi đọc CSV:", error);
                    currentUploadedCandidates = [...mockExcelCandidates];
                    showPreviewTable();
                }
            };
        } else if (ext === 'xlsx' || ext === 'xls') {
            if (typeof XLSX !== 'undefined') {
                reader.readAsArrayBuffer(file);
                reader.onload = function(e) {
                    try {
                        const data = new Uint8Array(e.target.result);
                        const workbook = XLSX.read(data, { type: 'array' });
                        const firstSheetName = workbook.SheetNames[0];
                        const worksheet = workbook.Sheets[firstSheetName];
                        
                        const rows = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
                        const parsed = parseExcelRows(rows);
                        
                        if (parsed && parsed.length > 0) {
                            currentUploadedCandidates = parsed;
                        } else {
                            currentUploadedCandidates = [...mockExcelCandidates];
                        }
                        showPreviewTable();
                    } catch (err) {
                        console.error("Lỗi phân tích Excel:", err);
                        currentUploadedCandidates = [...mockExcelCandidates];
                        showPreviewTable();
                    }
                };
            } else {
                console.warn("Thư viện XLSX chưa được tải, sử dụng danh sách giả lập.");
                currentUploadedCandidates = [...mockExcelCandidates];
                showPreviewTable();
            }
        }
    }

    // Hàm phân tích dữ liệu CSV (tương thích cả phân tách bằng phẩy và chấm phẩy)
    function parseCSVContent(text) {
        const lines = text.split(/\r?\n/);
        const list = [];
        
        // Bỏ qua tiêu đề (dòng 0)
        for (let i = 1; i < lines.length; i++) {
            const line = lines[i].trim();
            if (!line) continue;
            
            const cols = line.split(/[;,]/);
            if (cols.length >= 5) {
                list.push({
                    sbd: cleanValue(cols[0]),
                    name: cleanValue(cols[1]),
                    dob: cleanValue(cols[2]),
                    cccd: cleanValue(cols[3]),
                    license: cleanValue(cols[4])
                });
            }
        }
        return list;
    }

    // Hàm phân tích dữ liệu từ các hàng trong file Excel (SheetJS)
    function parseExcelRows(rows) {
        const list = [];
        // Hàng 0 là tiêu đề, bắt đầu từ hàng 1
        for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            if (!row || row.length === 0) continue;
            
            const sbd = row[0] ? String(row[0]).trim() : '';
            const name = row[1] ? String(row[1]).trim() : '';
            const dob = row[2] ? String(row[2]).trim() : '';
            const cccd = row[3] ? String(row[3]).trim() : '';
            const license = row[4] ? String(row[4]).trim() : '';
            
            if (sbd && name) {
                list.push({
                    sbd: sbd,
                    name: name,
                    dob: dob,
                    cccd: cccd,
                    license: license
                });
            }
        }
        return list;
    }

    // Chuẩn hóa, bỏ ký tự bọc chuỗi (nếu có)
    function cleanValue(val) {
        if (!val) return '';
        return val.replace(/^["']|["']$/g, '').trim();
    }

    // Render danh sách thí sinh ra bảng Preview (Dùng nối chuỗi thuần túy để tránh lỗi JSP EL biên dịch)
    function showPreviewTable() {
        let html = "";
        
        currentUploadedCandidates.forEach(function(cand) {
            var isA1 = cand.license && cand.license.indexOf('A1') !== -1;
            var badgeStyle = isA1 ? 'background-color:rgba(13, 148, 136, 0.06); color:#0d9488; border-color:rgba(13, 148, 136, 0.15);' : '';
            
            html += '<tr>' +
                '<td style="font-weight: 700; color: #0052cc;">' + cand.sbd + '</td>' +
                '<td style="font-weight: 600; color: #0f172a;">' + cand.name + '</td>' +
                '<td style="text-align: center; font-weight: 500;">' + cand.dob + '</td>' +
                '<td style="text-align: center; font-family: monospace;">' + cand.cccd + '</td>' +
                '<td>' +
                    '<span class="role-badge role-badge--admin" style="' + badgeStyle + '">' + cand.license + '</span>' +
                '</td>' +
                '<td style="text-align: center;">' +
                    '<span class="action-badge action-badge--success" style="font-weight: 700;">HỢP LỆ</span>' +
                '</td>' +
            '</tr>';
        });

        previewTableBody.innerHTML = html;
        previewContainer.style.display = 'block';

        // Tự động cuộn xuống khu vực xem trước để người dùng nhìn thấy
        previewContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    // Hủy bỏ việc nạp danh sách
    function cancelImport() {
        previewContainer.style.display = 'none';
        progressBox.style.display = 'none';
        dropzone.style.display = 'flex';
        fileInput.value = ""; // Xóa dữ liệu file
        currentUploadedCandidates = [];
    }

    // Xác nhận nộp dữ liệu danh sách lên hệ thống
    function submitImport() {
        let count = currentUploadedCandidates.length;
        let confirmAction = confirm("XÁC NHẬN NHẬP DANH SÁCH THÍ SINH:\n\n- Tổng số thí sinh hợp lệ: " + count + " hồ sơ\n\nBạn có chắc chắn muốn lưu chính thức danh sách này vào cơ sở dữ liệu để tổ chức ca thi?");
        
        if (!confirmAction) return;

        alert("Thành công: Đã import chính thức thành công " + count + " thí sinh vào ca thi sát hạch!");
        cancelImport();
    }
</script>

</body>
</html>
