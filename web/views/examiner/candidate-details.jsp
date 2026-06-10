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
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
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
                    <span class="material-symbols-outlined">print</span>
                    In thông tin chi tiết
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">list</span>
                    In danh sách
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">description</span>
                    In kết quả
                </a>
            </div>
            <div class="examiner-toolbar__group">
                <div class="examiner-search">
                    <span class="examiner-search__icon material-symbols-outlined">search</span>
                    <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">search</span>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <span class="material-symbols-outlined">refresh</span>
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
