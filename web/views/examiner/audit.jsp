<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="nhat-ky" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--dashboard">

        <%-- Toolbar --%>
        <section class="examiner-toolbar examiner-toolbar--tools">
            <div class="examiner-toolbar__group">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">print</span>
                    In nhật ký
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">filter_alt</span>
                    Lọc
                </a>
            </div>
            <div class="examiner-toolbar__group">
                <div class="examiner-search examiner-search--lg">
                    <span class="examiner-search__icon material-symbols-outlined">search</span>
                    <input type="text" class="examiner-search__input" placeholder="Lọc nhật ký...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">search</span>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/audit.jsp" class="examiner-btn examiner-btn--icon examiner-btn--white">
                    <span class="material-symbols-outlined">refresh</span>
                </a>
            </div>
        </section>

        <%-- Table Card with Pagination --%>
        <div class="audit-card">
            <div class="examiner-table-wrap">
                <table class="examiner-table audit-table">
                    <thead>
                        <tr>
                            <th class="audit-col--user">Người dùng</th>
                            <th class="audit-col--action">Thao tác</th>
                            <th class="audit-col--target">Đối tượng</th>
                            <th class="audit-col--sbd">SBD</th>
                            <th class="audit-col--info">Thông tin</th>
                            <th class="audit-col--old">Cũ</th>
                            <th class="audit-col--new">Mới</th>
                            <th class="audit-col--reason">Lý do</th>
                            <th class="audit-col--time">Thời gian</th>
                            <th class="audit-col--date">Ngày</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>admin_nv</td>
                            <td><span class="audit-badge audit-badge--update">CẬP NHẬT</span></td>
                            <td>Thí sinh</td>
                            <td class="examiner-table__mono">SBD-00123</td>
                            <td class="audit-td--ellipsis">Cập nhật điểm thi...</td>
                            <td class="audit-td--old"><s>28/30</s></td>
                            <td class="audit-td--new audit-new--green">30/30</td>
                            <td class="audit-td--reason">Phúc khảo</td>
                            <td class="examiner-table__mono-md">09:15:22</td>
                            <td class="examiner-table__mono-md">2023-10-25</td>
                        </tr>
                        <tr>
                            <td>system_auto</td>
                            <td><span class="audit-badge audit-badge--system">HỆ THỐNG</span></td>
                            <td>Phòng thi</td>
                            <td class="examiner-table__mono">-</td>
                            <td class="audit-td--ellipsis">Mở khóa ca thi s...</td>
                            <td class="audit-td--old">Khóa</td>
                            <td class="audit-td--new audit-new--blue">Mở</td>
                            <td class="audit-td--reason">Theo lịch trình</td>
                            <td class="examiner-table__mono-md">07:00:00</td>
                            <td class="examiner-table__mono-md">2023-10-25</td>
                        </tr>
                        <tr>
                            <td>giam_thi_01</td>
                            <td><span class="audit-badge audit-badge--warning">CẢNH BÁO</span></td>
                            <td>Thí sinh</td>
                            <td class="examiner-table__mono">SBD-00456</td>
                            <td class="audit-td--ellipsis">Đánh dấu vi phạm...</td>
                            <td class="audit-td--old">Bình thường</td>
                            <td class="audit-td--new audit-new--dark">Vi phạm</td>
                            <td class="audit-td--reason">Mang điện thoại</td>
                            <td class="examiner-table__mono-md">10:45:11</td>
                            <td class="examiner-table__mono-md">2023-10-24</td>
                        </tr>
                        <tr>
                            <td>admin_nv</td>
                            <td><span class="audit-badge audit-badge--delete">XÓA</span></td>
                            <td></td>
                            <td class="examiner-table__mono">SBD-00789</td>
                            <td class="audit-td--ellipsis">Xóa hồ sơ trùng ...</td>
                            <td class="audit-td--old">Tồn tại</td>
                            <td class="audit-td--new audit-new--red">Đã xóa</td>
                            <td class="audit-td--reason">Trùng CMND</td>
                            <td class="examiner-table__mono-md">14:20:05</td>
                            <td class="examiner-table__mono-md">2023-10-24</td>
                        </tr>
                        <tr>
                            <td>admin_nv</td>
                            <td><span class="audit-badge audit-badge--update">CẬP NHẬT</span></td>
                            <td>Thí sinh</td>
                            <td class="examiner-table__mono">SBD-00124</td>
                            <td class="audit-td--ellipsis">Sửa lỗi sai tên đệm</td>
                            <td class="audit-td--old">Nguyễn Văn A</td>
                            <td class="audit-td--new audit-new--green">Nguyễn Văn B</td>
                            <td class="audit-td--reason">Yêu cầu từ Cục</td>
                            <td class="examiner-table__mono-md">08:10:00</td>
                            <td class="examiner-table__mono-md">2023-10-23</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <%-- Pagination --%>
            <div class="audit-pagination">
                <nav class="audit-page-nav">
                    <button class="audit-page-btn audit-page-btn--nav" disabled>
                        <span class="material-symbols-outlined">chevron_left</span>
                    </button>
                    <button class="audit-page-btn audit-page-btn--active">1</button>
                    <button class="audit-page-btn">2</button>
                    <button class="audit-page-btn">3</button>
                    <span class="audit-page-dots">...</span>
                    <button class="audit-page-btn">10</button>
                    <button class="audit-page-btn audit-page-btn--nav">
                        <span class="material-symbols-outlined">chevron_right</span>
                    </button>
                </nav>
            </div>
        </div>

    </main>
</div>

</body>
</html>
