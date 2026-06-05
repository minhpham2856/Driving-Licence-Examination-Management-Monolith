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
                    <svg width="15" height="14" viewBox="0 0 24 24" fill="none">
                        <path d="M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3M16 19H8v-5h8v5M19 12c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1M18 3H6v4h12V3z" fill="currentColor"/>
                    </svg>
                    In nhật ký
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="14" height="10" viewBox="0 0 24 24" fill="none">
                        <path d="M4.25 5.61C6.27 8.2 10 12 10 12v6c0 1.1.9 2 2 2s2-.9 2-2v-6s3.72-3.8 5.74-6.39A1 1 0 0 0 18.95 4H5.04a1 1 0 0 0-.79 1.61z" fill="currentColor"/>
                    </svg>
                    Lọc
                </a>
            </div>
            <div class="examiner-toolbar__group">
                <div class="examiner-search examiner-search--lg">
                    <svg class="examiner-search__icon" viewBox="0 0 18 18" fill="none">
                        <path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    <input type="text" class="examiner-search__input" placeholder="Lọc nhật ký...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <svg width="16" height="16" viewBox="0 0 18 18" fill="none">
                        <path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/audit.jsp" class="examiner-btn examiner-btn--icon examiner-btn--white">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/>
                    </svg>
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
                        <svg width="8" height="13" viewBox="0 0 8 14" fill="none">
                            <path d="M7 1L1 7L7 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                    <button class="audit-page-btn audit-page-btn--active">1</button>
                    <button class="audit-page-btn">2</button>
                    <button class="audit-page-btn">3</button>
                    <span class="audit-page-dots">...</span>
                    <button class="audit-page-btn">10</button>
                    <button class="audit-page-btn audit-page-btn--nav">
                        <svg width="8" height="13" viewBox="0 0 8 14" fill="none">
                            <path d="M1 1L7 7L1 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                </nav>
            </div>
        </div>

    </main>
</div>

</body>
</html>
