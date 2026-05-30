<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tạo tài khoản học viên mới - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-staff-exam.jsp">
    <jsp:param name="activeSidebar" value="tao-tai-khoan" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/staff/exam/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Tạo tài khoản học viên</span>
        </nav>
        
        <!-- Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tạo Tài Khoản Học Viên Mới</h1>
                <p class="page-subtitle">Nhập thông tin hồ sơ của học viên chính khóa hoặc thí sinh tự do để kích hoạt tài khoản ôn luyện.</p>
            </div>
            
            <div class="page-actions">
                <a href="${pageContext.request.contextPath}/views/staff/exam/users.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Quay lại danh sách học viên
                </a>
            </div>
        </header>

        <!-- Main Form Panel -->
        <div class="report-grid" style="grid-template-columns: 1.5fr 1fr; gap: 1.5rem; margin-top: 1.5rem;">
            
            <!-- Left Pane: Registration Form -->
            <div class="report-pane" style="padding: 2rem;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.5rem; padding-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.15rem; display: flex; align-items: center; gap: 8px;">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                            <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Thông tin hồ sơ đăng ký tài khoản
                    </h2>
                </div>

                <!-- Form using pure native HTML5 validation constraints (JS-free) -->
                <form action="${pageContext.request.contextPath}/staff/exam/create-user" method="POST" style="display: flex; flex-direction: column; gap: 1.25rem;">
                    
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <!-- Full Name -->
                        <div class="input-group">
                            <label for="fullName" class="input-label">Họ và tên học viên <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="fullName" name="fullName" class="input-field" placeholder="Ví dụ: Nguyễn Văn A" required minlength="3" maxlength="50">
                        </div>

                        <!-- Identity Card (CCCD) -->
                        <div class="input-group">
                            <label for="cccd" class="input-label">Số Căn cước công dân (12 chữ số) <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="cccd" name="cccd" class="input-field" placeholder="Ví dụ: 030098001234" required pattern="[0-9]{12}" title="Vui lòng nhập đúng 12 chữ số CCCD hợp lệ">
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <!-- Phone Number -->
                        <div class="input-group">
                            <label for="phone" class="input-label">Số điện thoại liên hệ <span style="color: #ef4444;">*</span></label>
                            <input type="tel" id="phone" name="phone" class="input-field" placeholder="Ví dụ: 0987654321" required pattern="0[0-9]{9}" title="Số điện thoại phải bắt đầu bằng số 0 và bao gồm đúng 10 chữ số">
                        </div>

                        <!-- Email -->
                        <div class="input-group">
                            <label for="email" class="input-label">Địa chỉ Email <span style="color: #ef4444;">*</span></label>
                            <input type="email" id="email" name="email" class="input-field" placeholder="Ví dụ: hocvien@gmail.com" required>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <!-- DOB -->
                        <div class="input-group">
                            <label for="dob" class="input-label">Ngày sinh <span style="color: #ef4444;">*</span></label>
                            <input type="date" id="dob" name="dob" class="input-field" required>
                        </div>

                        <!-- Gender -->
                        <div class="input-group">
                            <label for="gender" class="input-label">Giới tính <span style="color: #ef4444;">*</span></label>
                            <select id="gender" name="gender" class="input-field" required>
                                <option value="">Chọn giới tính</option>
                                <option value="Nam">Nam</option>
                                <option value="Nữ">Nữ</option>
                            </select>
                        </div>
                    </div>

                    <!-- Hometown Address -->
                    <div class="input-group">
                        <label for="address" class="input-label">Địa chỉ quê quán / Nơi thường trú <span style="color: #ef4444;">*</span></label>
                        <input type="text" id="address" name="address" class="input-field" placeholder="Ví dụ: Thanh Xuân, Hà Nội" required minlength="5" maxlength="150">
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <!-- Account Type (User Type) -->
                        <div class="input-group">
                            <label for="userType" class="input-label">Phân loại học viên <span style="color: #ef4444;">*</span></label>
                            <select id="userType" name="userType" class="input-field" required>
                                <option value="">Chọn phân loại</option>
                                <option value="student">Học viên chính khóa (Đăng ký học từ đầu)</option>
                                <option value="free">Thí sinh tự do (Chỉ nộp hồ sơ thi sát hạch)</option>
                            </select>
                        </div>

                        <!-- Registered License Class -->
                        <div class="input-group">
                            <label for="licenseClass" class="input-label">Hạng GPLX sát hạch <span style="color: #ef4444;">*</span></label>
                            <select id="licenseClass" name="licenseClass" class="input-field" required>
                                <option value="">Chọn hạng bằng GPLX</option>
                                <option value="A1">Hạng A1 (Xe máy dưới 175cc)</option>
                                <option value="A2">Hạng A2 (Xe phân khối lớn từ 175cc)</option>
                                <option value="B1">Hạng B1 (Ô tô số tự động)</option>
                                <option value="B2">Hạng B2 (Ô tô số sàn)</option>
                                <option value="C">Hạng C (Ô tô tải lớn)</option>
                            </select>
                        </div>
                    </div>

                    <!-- System Default Password Hint -->
                    <div class="input-group" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem;">
                        <span style="font-size: 0.85rem; font-weight: 700; color: #475569; display: block; margin-bottom: 4px;">Thông tin tài khoản đăng nhập mặc định:</span>
                        <span style="font-size: 0.8rem; color: #64748b; display: block; line-height: 1.4;">
                            • Tên đăng nhập: <strong>Tự động sinh ra dựa theo Email</strong> (Ví dụ: hocvien)<br>
                            • Mật khẩu mặc định: <strong>123456</strong> (Học viên được khuyên thay đổi mật khẩu trong lần đầu tiên đăng nhập).
                        </span>
                    </div>

                    <hr style="border: 0; border-top: 1px solid #f1f5f9; margin: 8px 0;">

                    <div style="display: flex; gap: 10px; justify-content: flex-end;">
                        <a href="users.jsp" class="btn-reset" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; height: 42px; width: 120px; font-size: 0.9rem; font-weight: 600; color: #475569; background-color: #ffffff;">Hủy bỏ</a>
                        <button type="submit" class="btn-filter" style="height: 42px; width: 180px; border-radius: 8px; background-color: #0052cc; border-color: #0052cc; justify-content: center; font-weight: 700;">Tạo tài khoản</button>
                    </div>
                </form>
            </div>

            <!-- Right Pane: Rules & Verification Info -->
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                
                <!-- System Constraints & Rules Card -->
                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px; color: #003d9b;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Điều kiện tuổi đăng ký GPLX
                        </h2>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 1rem; font-size: 0.85rem; line-height: 1.45; color: #475569;">
                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">• Đối với hạng A1, A2:</strong>
                            Độ tuổi tối thiểu đăng ký sát hạch là <strong>đủ 18 tuổi</strong> tính đến ngày thi.
                        </div>

                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">• Đối với hạng B1, B2:</strong>
                            Độ tuổi tối thiểu đăng ký sát hạch là <strong>đủ 18 tuổi</strong> tính đến ngày thi.
                        </div>

                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">• Đối với hạng C:</strong>
                            Độ tuổi tối thiểu đăng ký sát hạch là <strong>đủ 21 tuổi</strong> tính đến ngày thi.
                        </div>

                        <div style="background-color: rgba(239, 68, 68, 0.05); border: 1px solid rgba(239, 68, 68, 0.15); border-radius: 6px; padding: 0.75rem; color: #b91c1c; font-weight: 500;">
                            Lưu ý: Hệ thống backend sẽ tự động tính tuổi dựa trên Ngày sinh học viên nhập vào để đối chiếu với Hạng GPLX đăng ký trước khi tạo tài khoản.
                        </div>
                    </div>
                </div>

                <!-- Legal Compliance info -->
                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                                <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Quy định về Giấy tờ đính kèm
                        </h2>
                    </div>

                    <p style="font-size: 0.85rem; color: #475569; line-height: 1.5; margin-bottom: 0.75rem;">Sau khi tài khoản được tạo thành công, học viên sẽ phải đăng nhập để tải lên các giấy tờ pháp lý sau đây lên hệ thống:</p>
                    
                    <div style="display: flex; flex-direction: column; gap: 0.65rem; font-size: 0.82rem; color: #64748b;">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">✓</span>
                            <span>Ảnh thẻ chân dung 3x4 (Chụp trên nền xanh)</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">✓</span>
                            <span>Ảnh chụp Căn cước công dân (Mặt trước + Mặt sau)</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">✓</span>
                            <span>Giấy khám sức khỏe lái xe còn thời hạn dưới 6 tháng</span>
                        </div>
                    </div>
                </div>

            </div>

        </div>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
