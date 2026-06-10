<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gọi thí sinh - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar examiner-portal">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="goi-thi-sinh" />
</jsp:include>

<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />

    <main class="examiner-main examiner-main--dashboard">
        <section class="examiner-toolbar examiner-toolbar--tools">
            <div class="examiner-toolbar__group">
                <a href="#" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">campaign</span>
                    Gọi thí sinh
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">print</span>
                    In đề thi
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">description</span>
                    In kết quả thi
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
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-call.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <span class="material-symbols-outlined">refresh</span>
                </a>
            </div>
        </section>

        <section class="examiner-card examiner-card--dashboard-table">
            <div class="examiner-card__head">
                <h3 class="examiner-card__title">Danh sách thí sinh chờ sát hạch lý thuyết</h3>
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
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Nguyễn Văn</span>
                                    <span>An</span>
                                </span>
                            </td>
                            <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">1001</td>
                            <td class="examiner-table__mono-md">12/05/1995</td>
                            <td class="examiner-table__mono-md examiner-text-ink">001095001234</td>
                            <td class="examiner-table__mono-md">25/10/2023</td>
                            <td class="examiner-table__ellipsis">123 Lê Lợi, Quận 1, TP.HCM</td>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td>
                                <div class="examiner-actions">
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Chi tiết</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Sửa TT</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-link-action">Sửa KQ</a>
                                </div>
                            </td>
                        </tr>
                        <tr class="examiner-table__row--alt">
                            <td class="examiner-table__center examiner-table__mono">02</td>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td class="examiner-table__name">Trần Thị Bình</td>
                            <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">1002</td>
                            <td class="examiner-table__mono-md">08/11/1998</td>
                            <td class="examiner-table__mono-md examiner-text-ink">079198005678</td>
                            <td class="examiner-table__mono-md">25/10/2023</td>
                            <td class="examiner-table__ellipsis">456 Nguyễn Huệ, Quận 1, TP.HCM</td>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td>
                                <div class="examiner-actions">
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Chi tiết</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Sửa TT</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-link-action">Sửa KQ</a>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="examiner-table__center examiner-table__mono">03</td>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check" checked></td>
                            <td class="examiner-table__name">
                                <span class="examiner-table__name-lines">
                                    <span>Lê Hoàng</span>
                                    <span>Cường</span>
                                </span>
                            </td>
                            <td class="examiner-table__center examiner-table__mono-md examiner-text-ink">1003</td>
                            <td class="examiner-table__mono-md">22/03/2000</td>
                            <td class="examiner-table__mono-md examiner-text-ink">001200009012</td>
                            <td class="examiner-table__mono-md">25/10/2023</td>
                            <td class="examiner-table__ellipsis">789 Trần Hưng Đạo, Quận 5, TP.HCM</td>
                            <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                            <td>
                                <div class="examiner-actions">
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Chi tiết</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="examiner-link-action">Sửa TT</a>
                                    <span class="examiner-actions__sep">|</span>
                                    <a href="${pageContext.request.contextPath}/views/examiner/result-details-edit.jsp" class="examiner-link-action">Sửa KQ</a>
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
