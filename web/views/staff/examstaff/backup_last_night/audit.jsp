<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký Cá Nhân - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        .staff-profile-card {
            background: linear-gradient(135deg, #0052cc 0%, #003d9b 100%);
            color: #ffffff;
            border-radius: 16px;
            padding: 1.5rem;
            box-shadow: 0 10px 25px -5px rgba(0, 82, 204, 0.15);
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 1.5rem;
        }
        
        .profile-info-group {
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        
        .profile-avatar-circle {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background-color: rgba(255, 255, 255, 0.2);
            color: #ffffff;
            font-size: 1.5rem;
            font-weight: 800;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 2px solid rgba(255, 255, 255, 0.4);
        }
        
        .profile-meta-text {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="nhat-ky" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Nhật ký cá nhân</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Nhật ký hoạt động cá nhân</h1>
                <p class="page-subtitle">Xem lại lịch sử thao tác nghiệp vụ, đối chiếu hồ sơ học viên do chính bạn thực hiện trong ngày trực.</p>
            </div>
        </header>

        <!-- Personal Staff Profile Header -->
        <div class="staff-profile-card">
            <div class="profile-info-group">
                <div class="profile-avatar-circle">MA</div>
                <div class="profile-meta-text">
                    <span style="font-size: 1.15rem; font-weight: 800;">Nguyễn Minh Anh</span>
                    <span style="font-size: 0.82rem; opacity: 0.85; font-family: monospace;">Tài khoản: @anhnm | Mã cán bộ: CBSH-0089</span>
                </div>
            </div>
            
            <div style="text-align: right; font-size: 0.82rem; opacity: 0.9;">
                <span style="display: block; font-weight: 700; text-transform: uppercase;">Ca trực hôm nay</span>
                <span style="font-size: 1rem; font-weight: 800;">Ca Sáng (24/05/2026)</span>
            </div>
        </div>

        <!-- KPI Metrics Row (Simulated Personal Progress Statistics) -->
        <section class="metrics-row" aria-label="Số liệu hoạt động cá nhân">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">6</span>
                    <span class="stat-label">Tổng thao tác cá nhân</span>
                    <span class="stat-trend stat-trend--up">Trong ca trực hôm nay</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber" style="background-color: rgba(126, 34, 206, 0.06); color: #7e22ce;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                        <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #7e22ce;">3</span>
                    <span class="stat-label">Học viên đã làm thủ tục</span>
                    <span class="stat-trend stat-trend--up">Xác minh, chụp ảnh, lệ phí</span>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">600,000 đ</span>
                    <span class="stat-label">Lệ phí đã xác nhận thu</span>
                    <span class="stat-trend stat-trend--up">QR chuyển khoản / Tiền mặt</span>
                </div>
            </div>
        </section>

        <!-- Audit Table Card -->
        <section class="log-card" style="margin-top: 1.5rem; margin-bottom: 2.5rem;">
            <header class="log-card-header" style="justify-content: space-between;">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v8M3 10V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v4" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M7 8h10M7 14h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Bảng kiểm toán hoạt động cá nhân (Hôm nay)
                </h2>
                
                <div class="log-card-actions">
                    <button class="btn-export" style="height: 36px; padding: 0 12px; font-size: 0.8rem; border-radius: 6px;">In nhật ký cá nhân</button>
                </div>
            </header>
            
            <div class="table-responsive">
                <table class="audit-table" style="font-size: 0.88rem;">
                    <thead>
                        <tr>
                            <th scope="col" style="width: 80px;" class="col-id">STT</th>
                            <th scope="col" style="width: 140px;">Thời gian</th>
                            <th scope="col" style="width: 150px;">Nghiệp vụ</th>
                            <th scope="col">Chi tiết thao tác ghi nhận kiểm toán</th>
                            <th scope="col" style="width: 140px; text-align: center;">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="col-id">1</td>
                            <td class="col-time">15:10:04</td>
                            <td><span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 6px;">Gọi thí sinh</span></td>
                            <td class="details-cell">
                                Đã phát loa gọi đợt **10 thí sinh** (SBD từ A1-0024 trở đi) xếp hàng tiến vào bàn thủ tục chính.
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                        <tr>
                            <td class="col-id">2</td>
                            <td class="col-time">15:05:22</td>
                            <td><span class="role-badge role-badge--admin" style="font-size: 0.72rem; padding: 2px 6px;">Lập hồ sơ</span></td>
                            <td class="details-cell">
                                Đã tra cứu, đối chiếu CCCD và sửa đổi Hạng GPLX cũ học viên **Nguyễn Anh Tuấn** (SBD: **A1-0024**).
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                        <tr>
                            <td class="col-id">3</td>
                            <td class="col-time">14:58:11</td>
                            <td><span class="role-badge role-badge--cham" style="font-size: 0.72rem; padding: 2px 6px;">Chụp ảnh</span></td>
                            <td class="details-cell">
                                Kích hoạt Live Camera thành công và lưu ảnh chân dung học viên **Trần Thị Mai** (SBD: **B2-0145**).
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                        <tr>
                            <td class="col-id">4</td>
                            <td class="col-time">14:52:45</td>
                            <td><span class="role-badge role-badge--other" style="font-size: 0.72rem; padding: 2px 6px; background-color: #fef3c7; color: #b45309;">Thu lệ phí</span></td>
                            <td class="details-cell">
                                Xác nhận thanh toán qua QR Code **200,000đ** cho học viên **Vũ Huy Hoàng** (SBD: **B2-0112**).
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                        <tr>
                            <td class="col-id">5</td>
                            <td class="col-time">14:30:12</td>
                            <td><span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 6px;">Nhập Excel</span></td>
                            <td class="details-cell">
                                Tải lên tệp **danh_sach_thi_sinh_24_05.xlsx**, tự động chuẩn hóa và sinh SBD cho **120 học viên**.
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                        <tr>
                            <td class="col-id">6</td>
                            <td class="col-time">14:00:00</td>
                            <td><span class="role-badge" style="font-size: 0.72rem; padding: 2px 6px; background-color: #f1f5f9; color: #475569;">Đăng nhập</span></td>
                            <td class="details-cell">
                                Đăng nhập thành công phân hệ Ban Sát Hạch từ IP máy trạm **192.168.1.15**.
                            </td>
                            <td style="text-align: center;"><span class="action-badge action-badge--success" style="font-weight: 700;">Ghi nhận log</span></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>
