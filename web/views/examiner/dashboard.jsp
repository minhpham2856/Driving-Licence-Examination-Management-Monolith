<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--dashboard">
        <section class="examiner-toolbar examiner-toolbar--tools">
            <div class="examiner-toolbar__group">
                <h2 class="examiner-toolbar__title">Bảng điều khiển</h2>
            </div>
            <div class="examiner-toolbar__group">
                <div class="examiner-search examiner-search--wide">
                    <span class="examiner-search__icon material-symbols-outlined">search</span>
                    <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên, Căn cước...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">search</span>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/dashboard.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <span class="material-symbols-outlined">refresh</span>
                </a>
            </div>
        </section>

        <section class="examiner-card examiner-card--dashboard-table">
            <div class="examiner-table-wrap">
                <table class="examiner-table examiner-table--dashboard">
                    <colgroup>
                        <col style="width:13.87%">
                        <col style="width:8.25%">
                        <col style="width:9.28%">
                        <col style="width:10.31%">
                        <col style="width:11.34%">
                        <col style="width:13.87%">
                        <col style="width:6.19%">
                        <col style="width:6.19%">
                        <col style="width:8.25%">
                        <col style="width:12.45%">
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Tên</th>
                            <th>SBD</th>
                            <th>Ngày sinh</th>
                            <th>Địa chỉ</th>
                            <th>Tình trạng</th>
                            <th>Số căn cước</th>
                            <th class="examiner-table__center">Đúng</th>
                            <th class="examiner-table__center">Sai</th>
                            <th class="examiner-table__center">Không TL</th>
                            <th>Kết quả</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Nguyễn Văn</span>
                                    <span>An</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>001</span>
                                </span>
                            </td>
                            <td>12/05/1995</td>
                            <td>Hà Nội</td>
                            <td><span class="examiner-tag examiner-tag--done">Đã thi</span></td>
                            <td class="examiner-table__mono">001095123456</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">35</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-table__mono-md">0</td>
                            <td><span class="examiner-tag examiner-tag--pass">ĐẠT</span></td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Trần Thị</span>
                                    <span>Bình</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>002</span>
                                </span>
                            </td>
                            <td>20/10/1998</td>
                            <td>Hải Phòng</td>
                            <td><span class="examiner-tag examiner-tag--testing">Đang thi</span></td>
                            <td class="examiner-table__mono">031098654321</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">12</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">3</td>
                            <td class="examiner-table__center examiner-table__mono-md">20</td>
                            <td><span class="examiner-tag examiner-tag--none">---</span></td>
                        </tr>
                        <tr>
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Nguyễn Văn</span>
                                    <span>An</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>001</span>
                                </span>
                            </td>
                            <td>12/05/1995</td>
                            <td>Hà Nội</td>
                            <td><span class="examiner-tag examiner-tag--done">Đã thi</span></td>
                            <td class="examiner-table__mono">001095123456</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">35</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-table__mono-md">0</td>
                            <td><span class="examiner-tag examiner-tag--pass">ĐẠT</span></td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Phạm Thị</span>
                                    <span>Dung</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>004</span>
                                </span>
                            </td>
                            <td>15/08/1992</td>
                            <td>Hồ Chí Minh</td>
                            <td><span class="examiner-tag examiner-tag--pending">Chưa thi</span></td>
                            <td class="examiner-table__mono">079192887766</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-table__mono-md">35</td>
                            <td><span class="examiner-tag examiner-tag--none">---</span></td>
                        </tr>
                        <tr>
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Lê Văn</span>
                                    <span>Cường</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>003</span>
                                </span>
                            </td>
                            <td>05/02/2000</td>
                            <td>Đà Nẵng</td>
                            <td><span class="examiner-tag examiner-tag--done-fail">Đã thi</span></td>
                            <td class="examiner-table__mono">049196112233</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">28</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">7</td>
                            <td class="examiner-table__center examiner-table__mono-md">0</td>
                            <td><span class="examiner-tag examiner-tag--fail">TRƯỢT</span></td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Phạm Thị</span>
                                    <span>Dung</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>004</span>
                                </span>
                            </td>
                            <td>15/08/1992</td>
                            <td>Hồ Chí Minh</td>
                            <td><span class="examiner-tag examiner-tag--pending">Chưa thi</span></td>
                            <td class="examiner-table__mono">079192887766</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-table__mono-md">35</td>
                            <td><span class="examiner-tag examiner-tag--none">---</span></td>
                        </tr>
                        <tr>
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Lê Văn</span>
                                    <span>Cường</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>003</span>
                                </span>
                            </td>
                            <td>05/02/2000</td>
                            <td>Đà Nẵng</td>
                            <td><span class="examiner-tag examiner-tag--done-fail">Đã thi</span></td>
                            <td class="examiner-table__mono">049196112233</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">28</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">7</td>
                            <td class="examiner-table__center examiner-table__mono-md">0</td>
                            <td><span class="examiner-tag examiner-tag--fail">TRƯỢT</span></td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Phạm Thị</span>
                                    <span>Dung</span>
                                </span>
                            </td>
                            <td class="examiner-table__mono">
                                <span class="examiner-table__sbd-lines">
                                    <span>SBD-</span>
                                    <span>004</span>
                                </span>
                            </td>
                            <td>15/08/1992</td>
                            <td>Hồ Chí Minh</td>
                            <td><span class="examiner-tag examiner-tag--pending">Chưa thi</span></td>
                            <td class="examiner-table__mono">079192887766</td>
                            <td class="examiner-table__center examiner-text-green examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-text-red examiner-table__mono-md">0</td>
                            <td class="examiner-table__center examiner-table__mono-md">35</td>
                            <td><span class="examiner-tag examiner-tag--none">---</span></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="examiner-summary examiner-summary--dashboard">
            <div class="examiner-summary__grid">
                <div class="examiner-summary__course">
                    <p class="examiner-summary__label">Khoá thi</p>
                    <p class="examiner-summary__value">K23-B2-04</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--total">
                    <p class="examiner-summary__label">Tổng số</p>
                    <p class="examiner-summary__value examiner-summary__value--sm">120</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--done">
                    <p class="examiner-summary__label">Đã thi</p>
                    <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--blue">85</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--testing">
                    <p class="examiner-summary__label">Đang thi</p>
                    <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--amber">15</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--pending">
                    <p class="examiner-summary__label">Chưa thi</p>
                    <p class="examiner-summary__value examiner-summary__value--sm">20</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--pass">
                    <p class="examiner-summary__label">Thi đạt</p>
                    <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--green">18</p>
                </div>
                <div class="examiner-summary__stat examiner-summary__stat--fail">
                    <p class="examiner-summary__label">Thi trượt</p>
                    <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--red">68</p>
                </div>
            </div>
        </section>
    </main>
</div>

</body>
</html>
