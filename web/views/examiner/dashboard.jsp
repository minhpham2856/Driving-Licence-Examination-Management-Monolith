<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Bảng điều khiển" />
<c:set var="pageUrl" value="${ctx}/views/examiner/dashboard.jsp" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${cssStyle}">
        <link rel="stylesheet" href="${cssLayout}">
    </head>
    <body class="has-side-nav-bar examiner-portal">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="dashboard" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">
                <!--toolbar-->
                <section class="examiner-toolbar examiner-toolbar--tools">
                    <!--tb.title-->
                    <div class="examiner-toolbar__group">
                        <h2 class="examiner-toolbar__title">Danh sách thí sinh</h2>
                    </div>

                    <!--tb.actions-->
                    <div class="examiner-toolbar__group">
                        <div class="examiner-search examiner-search--wide">
                            <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên, Căn cước...">
                        </div>
                        <a href="#" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">search</span>Tìm kiếm
                        </a>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>


                <!--candidate list-->
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
                                        <span class="examiner-table__name-lines">Nguyễn Văn An</span>
                                    </td>
                                    <td class="examiner-table__mono">
                                        <span class="examiner-table__sbd-lines">001</span>
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
                            </tbody>
                        </table>
                    </div>
                </section>

                <!--statistics-->
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
