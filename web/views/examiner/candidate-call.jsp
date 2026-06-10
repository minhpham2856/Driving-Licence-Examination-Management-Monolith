<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Gọi thí sinh" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-call.jsp" />
<c:set var="confirmUrl" value="${ctx}/views/examiner/confirmation.jsp" />
<c:set var="detailUrl" value="${ctx}/views/examiner/candidate-details-edit.jsp" />
<c:set var="resultUrl" value="${ctx}/views/examiner/result-details-edit.jsp" />

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
            <jsp:param name="activeSidebar" value="goi-thi-sinh" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">
                <!--toolbar-->
                <section class="examiner-toolbar examiner-toolbar--tools">
                    <!--tb.left-->
                    <div class="examiner-toolbar__group">
                        <a href="#" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">campaign</span>Gọi thí sinh
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>In đề thi
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">description</span>In kết quả thi
                        </a>
                    </div>

                    <!--tb.right-->
                    <div class="examiner-toolbar__group">
                        <div class="examiner-search">
                            <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, tên...">
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
                    <div class="examiner-card__head">
                        <h3 class="examiner-card__title">Danh sách thí sinh</h3>
                        <span class="examiner-card__badge">Tổng: 45 thí sinh</span>
                    </div>
                    <div class="examiner-table-wrap">
                        <table class="examiner-table examiner-table--dark examiner-table--call">
                            <colgroup>
                                <col style="width:50px">
                                <col style="width:64px">
                                <col style="width:120px">
                                <col style="width:96px">
                                <col style="width:128px">
                                <col style="width:160px">
                                <col style="width:128px">
                                <col style="width:200px">
                                <col style="width:64px">
                                <col style="width:190px">
                            </colgroup>
                            <thead>
                                <tr>
                                    <th class="examiner-table__center">STT</th>
                                    <th class="examiner-table__center">Chọn</th>
                                    <th>Tên</th>
                                    <th class="examiner-table__center">SBD</th>
                                    <th>Ngày sinh</th>
                                    <th>Số căn cước</th>
                                    <th>Ngày thi</th>
                                    <th>Địa chỉ</th>
                                    <th class="examiner-table__center">Vắng</th>
                                    <th class="examiner-table__center">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td class="examiner-table__center examiner-table__mono">01</td>
                                    <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                                    <td class="examiner-table__name">Nguyễn Văn An</td>
                                    <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">123</td>
                                    <td class="examiner-table__mono-md">12/05/1995</td>
                                    <td class="examiner-table__mono-md examiner-text-ink">001095001234</td>
                                    <td class="examiner-table__mono-md">25/10/2023</td>
                                    <td class="examiner-table__ellipsis">123 Lê Lợi, Quận 1, TP.HCM</td>
                                    <td class="examiner-table__center">
                                        <form action="${confirmUrl}" method="get">
                                            <input type="hidden" name="sbd" value="123">
                                            <input type="hidden" name="name" value="Nguyễn Văn An">
                                            <input type="submit" class="examiner-link-action" value="Vắng">
                                        </form>
                                    </td>
                                    <td>
                                        <div class="examiner-actions">
                                            <a href="${detailUrl}" class="examiner-link-action">Chi tiết</a>|
                                            <a href="${detailUrl}" class="examiner-link-action">Sửa TT</a>|
                                            <a href="${resultUrl}" class="examiner-link-action">Sửa KQ</a>
                                        </div>
                                    </td>
                                </tr>
                                <tr class="examiner-table__row--alt">
                                    <td class="examiner-table__center examiner-table__mono">02</td>
                                    <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                                    <td class="examiner-table__name">Trần Thị Bình</td>
                                    <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">124</td>
                                    <td class="examiner-table__mono-md">08/11/1998</td>
                                    <td class="examiner-table__mono-md examiner-text-ink">079198005678</td>
                                    <td class="examiner-table__mono-md">25/10/2023</td>
                                    <td class="examiner-table__ellipsis">456 Nguyễn Huệ, Quận 1, TP.HCM</td>
                                    <td class="examiner-table__center">
                                        <form action="${confirmUrl}" method="get">
                                            <input type="hidden" name="sbd" value="124">
                                            <input type="hidden" name="name" value="Trần Thị Bình">
                                            <input type="submit" class="examiner-link-action" value="Vắng">
                                        </form>
                                    </td>
                                    <td>
                                        <div class="examiner-actions">
                                            <a href="${detailUrl}" class="examiner-link-action">Chi tiết</a>|
                                            <a href="${detailUrl}" class="examiner-link-action">Sửa TT</a>|
                                            <a href="${resultUrl}" class="examiner-link-action">Sửa KQ</a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="examiner-table__center examiner-table__mono">03</td>
                                    <td class="examiner-table__center"><input type="checkbox" class="examiner-check" checked></td>
                                    <td class="examiner-table__name">Lê Hoàng Cường</td>
                                    <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">1003</td>
                                    <td class="examiner-table__mono-md">22/03/2000</td>
                                    <td class="examiner-table__mono-md examiner-text-ink">001200009012</td>
                                    <td class="examiner-table__mono-md">25/10/2023</td>
                                    <td class="examiner-table__ellipsis">789 Trần Hưng Đạo, Quận 5, TP.HCM</td>
                                    <td class="examiner-table__center">
                                        <form action="${confirmUrl}" method="get">
                                            <input type="hidden" name="sbd" value="1003">
                                            <input type="hidden" name="name" value="Lê Hoàng Cường">
                                            <input type="submit" class="examiner-link-action" value="Vắng">
                                        </form>
                                    </td>
                                    <td>
                                        <div class="examiner-actions">
                                            <a href="${detailUrl}" class="examiner-link-action">Chi tiết</a>|
                                            <a href="${detailUrl}" class="examiner-link-action">Sửa TT</a>|
                                            <a href="${resultUrl}" class="examiner-link-action">Sửa KQ</a>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>
