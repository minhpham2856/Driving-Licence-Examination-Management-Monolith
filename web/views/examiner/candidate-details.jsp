<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa thông tin - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="sua-thong-tin" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--dashboard">
        <section class="examiner-toolbar examiner-toolbar--tools">
            <div class="examiner-toolbar__group">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="15" height="14" viewBox="0 0 24 24" fill="none"><path d="M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3M16 19H8v-5h8v5M19 12c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1M18 3H6v4h12V3z" fill="currentColor"/></svg>
                    In thông tin chi tiết
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M3 13h2v-2H3v2m0 4h2v-2H3v2m0-8h2V7H3v2m4 4h14v-2H7v2m0 4h14v-2H7v2M7 7v2h14V7H7z" fill="currentColor"/></svg>
                    In danh sách
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <svg width="14" height="15" viewBox="0 0 24 24" fill="none"><path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6M16 18H8v-2h8v2m0-4H8v-2h8v2m-3-5V3.5L18.5 9H13z" fill="currentColor"/></svg>
                    In kết quả
                </a>
            </div>
            <div class="examiner-toolbar__group">
                <div class="examiner-search">
                    <svg class="examiner-search__icon" viewBox="0 0 18 18" fill="none"><path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/><path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                    <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/><path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/></svg>
                </a>
            </div>
        </section>

        <section class="examiner-card examiner-card--dashboard-table">
            <div class="examiner-table-wrap">
                <table class="examiner-table examiner-table--detail">
                    <colgroup>
                        <col style="width:48px">
                        <col style="width:143px">
                        <col style="width:86px">
                        <col style="width:122px">
                        <col style="width:139px">
                        <col style="width:122px">
                        <col>
                        <col style="width:100px">
                    </colgroup>
                    <thead>
                        <tr>
                            <th class="examiner-table__center"><input type="checkbox" class="examiner-check"></th>
                            <th>Tên</th>
                            <th>SBD</th>
                            <th>Ngày sinh</th>
                            <th>Số căn cước</th>
                            <th>Ngày sinh</th>
                            <th>Địa chỉ</th>
                            <th class="examiner-table__center">Chi tiết</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td class="examiner-table__name">Nguyễn Văn An</td>
                            <td class="examiner-table__mono-md">B2-001</td>
                            <td class="examiner-table__mono-md">15/05/1990</td>
                            <td class="examiner-table__mono-md">001090123456</td>
                            <td class="examiner-table__mono-md">15/05/1990</td>
                            <td class="examiner-table__ellipsis">123 Lê Lợi, Quận 1, TP.HCM</td>
                            <td class="examiner-table__center"><a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Xem</a></td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td class="examiner-table__name">Trần Thị Bích</td>
                            <td class="examiner-table__mono-md">B2-002</td>
                            <td class="examiner-table__mono-md">22/08/1995</td>
                            <td class="examiner-table__mono-md">002095654321</td>
                            <td class="examiner-table__mono-md">22/08/1995</td>
                            <td class="examiner-table__ellipsis">45 Nguyễn Huệ, Quận 1, TP.HCM</td>
                            <td class="examiner-table__center"><a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Xem</a></td>
                        </tr>
                        <tr>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td class="examiner-table__name">Lê Văn Cường</td>
                            <td class="examiner-table__mono-md">C-015</td>
                            <td class="examiner-table__mono-md">10/11/1988</td>
                            <td class="examiner-table__mono-md">079088112233</td>
                            <td class="examiner-table__mono-md">10/11/1988</td>
                            <td class="examiner-table__ellipsis">89 Võ Văn Tần, Quận 3, TP.HCM</td>
                            <td class="examiner-table__center"><a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Xem</a></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>

</body>
</html>
