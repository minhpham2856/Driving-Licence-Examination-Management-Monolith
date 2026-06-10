<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề thi - Lái Vui</title>
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

    <main class="examiner-main examiner-main--scroll">

        <%-- Toolbar --%>
        <section class="examiner-toolbar">
            <div class="exr-toolbar-left">
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-details-edit.jsp" class="exr-back">
                    <span class="material-symbols-outlined">arrow_back</span>
                    QUAY LẠI
                </a>
                <h2 class="examiner-toolbar__title">Đề thi</h2>
            </div>
            <div class="examiner-toolbar__actions">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">print</span>
                    In kết quả
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">filter_alt</span>
                    Lọc
                </a>
                <div class="paper-filter-tabs">
                    <span class="paper-filter-tab paper-filter-tab--correct">
                        <span class="material-symbols-outlined">check</span>
                        Câu đúng (26)
                    </span>
                    <span class="paper-filter-tab paper-filter-tab--wrong">
                        <span class="material-symbols-outlined">close</span>
                        Câu sai (9)
                    </span>
                </div>
                <a href="${pageContext.request.contextPath}/views/examiner/candidate-paper.jsp" class="examiner-btn examiner-btn--white examiner-btn--icon">
                    <span class="material-symbols-outlined">refresh</span>
                </a>
            </div>
        </section>

        <%-- Data Table --%>
        <div class="paper-table-wrap">
            <table class="paper-table">
                <thead>
                    <tr>
                        <th class="paper-th paper-th--no">Câu<br/>hỏi</th>
                        <th class="paper-th paper-th--content">Nội dung</th>
                        <th class="paper-th paper-th--answer">Đáp án</th>
                        <th class="paper-th paper-th--student">Thí sinh trả<br/>lời</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">01</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/001_pb4uxc.png" alt="Q-001" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">02</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/002_xfqch7.png" alt="Q-002" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">03</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127983/003_f2kpqz.png" alt="Q-003" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">04</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/004_ype2gx.png" alt="Q-004" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">05</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/005_pnn5lk.png" alt="Q-005" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">06</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/006_s70rei.png" alt="Q-006" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">D</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">07</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127983/007_whxzz0.png" alt="Q-007" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">08</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/008_mxsrqj.png" alt="Q-008" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">09</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/009_abuu5g.png" alt="Q-009" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">D</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">10</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/010_uyxezy.png" alt="Q-010" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">11</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/011_ixho2u.png" alt="Q-011" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">12</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/012_q8eac6.png" alt="Q-012" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">D</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">13</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127985/013_m9ukph.png" alt="Q-013" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">14</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127986/014_gr7xtc.png" alt="Q-014" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">15</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/015_yd6vvp.png" alt="Q-015" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">16</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/016_fhnvpg.png" alt="Q-016" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">17</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/017_ed6f9x.png" alt="Q-017" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">B</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">18</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/018_pnkk0a.png" alt="Q-018" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">B</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">19</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/019_vi5sbd.png" alt="Q-019" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">20</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/020_qowgmo.png" alt="Q-020" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">21</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/021_wu5ldu.png" alt="Q-021" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">B</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">22</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/022_cqaxks.png" alt="Q-022" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">23</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127989/023_faqfex.png" alt="Q-023" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">24</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127989/024_r2of7f.png" alt="Q-024" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">25</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127990/025_u8hekl.png" alt="Q-025" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">B</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">26</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127990/026_hjqqi6.png" alt="Q-026" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">D</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">27</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/027_uoiwz5.png" alt="Q-027" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">C</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">28</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/028_plm6ha.png" alt="Q-028" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">C</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">29</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/029_du9wza.png" alt="Q-029" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">B</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">30</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/030_ham25h.png" alt="Q-030" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">31</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/031_m6czby.png" alt="Q-031" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">B</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">32</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/032_t9kpuy.png" alt="Q-032" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">D</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">D</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">33</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/033_nmi4s9.png" alt="Q-033" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">A</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">A</span></td>
                    </tr>
                    <tr class="paper-tr paper-tr--alt">
                        <td class="paper-td paper-td--no">34</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127993/034_guyjgw.png" alt="Q-034" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">B</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--correct">B</span></td>
                    </tr>
                    <tr class="paper-tr">
                        <td class="paper-td paper-td--no">35</td>
                        <td class="paper-td paper-td--content"><img src="https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/035_zgtght.png" alt="Q-035" class="paper-img"/></td>
                        <td class="paper-td paper-td--answer">C</td>
                        <td class="paper-td paper-td--student"><span class="paper-ans paper-ans--wrong">A</span></td>
                    </tr>
                </tbody>
            </table>
        </div>

    </main>
</div>

</body>
</html>
