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
                    <svg class="examiner-search__icon" viewBox="0 0 18 18" fill="none"><path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/><path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                    <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên, Căn cước...">
                </div>
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M7.5 13.5C10.8137 13.5 13.5 10.8137 13.5 7.5C13.5 4.18629 10.8137 1.5 7.5 1.5C4.18629 1.5 1.5 4.18629 1.5 7.5C1.5 10.8137 4.18629 13.5 7.5 13.5Z" stroke="currentColor" stroke-width="1.5"/><path d="M12 12L16.5 16.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                    Tìm kiếm
                </a>
                <a href="${pageContext.request.contextPath}/views/examiner/dashboard.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.65 2.35C12.2 0.9 10.2 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/></svg>
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
