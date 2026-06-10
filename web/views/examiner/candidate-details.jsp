<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/layout/examiner-seed-data.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Sửa thông tin" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-details.jsp" />
<c:set var="detailUrl" value="${ctx}/views/examiner/candidate-details-edit.jsp" />

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
            <jsp:param name="activeSidebar" value="sua-thong-tin" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">
                <!--toolbar-->
                <section class="examiner-toolbar examiner-toolbar--tools">
                    <!--tb.left-->
                    <div class="examiner-toolbar__group">
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>In thông tin chi tiết
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">list</span>In danh sách
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">description</span>In kết quả
                        </a>
                    </div>

                    <!--tb.right-->
                    <div class="examiner-toolbar__group">
                        <div class="examiner-search">
                            <input type="text" class="examiner-search__input" placeholder="Tìm kiếm SBD, Tên...">
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
                    </div>
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
                                    <th>Ngày thi</th>
                                    <th>Địa chỉ</th>
                                    <th class="examiner-table__center">Chi tiết</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${candidates}" var="c" varStatus="st">
                                    <tr<c:if test="${st.index % 2 == 1}"> class="examiner-table__row--alt"</c:if>>
                                        <td class="examiner-table__center"><input type="checkbox" class="examiner-check"></td>
                                        <td class="examiner-table__name">${c.fullName}</td>
                                        <td class="examiner-table__mono-md">${c.sbd}</td>
                                        <td class="examiner-table__mono-md">${c.dob}</td>
                                        <td class="examiner-table__mono-md">${c.governmentId}</td>
                                        <td class="examiner-table__mono-md">${c.examDate}</td>
                                        <td class="examiner-table__ellipsis">${c.address}</td>
                                        <td class="examiner-table__center"><a href="${detailUrl}?sbd=${c.sbd}" class="examiner-link-action">Xem</a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>
