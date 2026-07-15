<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${requestScope.reportReady ne true}">
    <c:redirect url="/manager/reports" />
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo thống kê - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .report-control-panel {
            background:#fff;border:1px solid #dbe4f0;border-radius:16px;padding:1.25rem;
            box-shadow:0 8px 24px rgba(15,23,42,.05);margin-bottom:1rem
        }
        .period-tabs {display:flex;flex-wrap:wrap;gap:.55rem;margin-bottom:1rem}
        .period-tab {
            display:inline-flex;align-items:center;justify-content:center;padding:.65rem 1rem;
            border:1px solid #cbd5e1;border-radius:10px;color:#475569;background:#fff;
            text-decoration:none;font-weight:700;font-size:.88rem
        }
        .period-tab.is-active {color:#fff;background:#0052cc;border-color:#0052cc;box-shadow:0 6px 14px rgba(0,82,204,.2)}
        .report-filter-grid {display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:.85rem;align-items:end}
        .report-filter-note {font-size:.75rem;color:#64748b;margin-top:.3rem}
        .report-chart-grid {display:grid;grid-template-columns:minmax(0,2fr) minmax(260px,.85fr);gap:1rem;margin-bottom:1rem}
        .chart-card {background:#fff;border:1px solid #dbe4f0;border-radius:16px;padding:1.3rem;box-shadow:0 8px 24px rgba(15,23,42,.05)}
        .chart-heading {display:flex;justify-content:space-between;align-items:flex-start;gap:1rem;margin-bottom:1.1rem}
        .chart-heading h2 {font-size:1rem;color:#0f172a;margin:0 0 .25rem}
        .chart-heading p {font-size:.8rem;color:#64748b;margin:0}
        .chart-list {display:grid;gap:1rem;max-height:440px;overflow:auto;padding-right:.25rem}
        .chart-row__header {display:flex;justify-content:space-between;gap:1rem;margin-bottom:.4rem;font-size:.8rem}
        .chart-row__title {font-weight:700;color:#334155;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
        .chart-row__total {color:#64748b;white-space:nowrap}
        .stacked-bar {height:22px;display:flex;background:#f1f5f9;border-radius:7px;overflow:hidden}
        .stacked-bar span {display:block;height:100%;min-width:0;transition:width .2s ease}
        .bar-pass {background:#10b981}.bar-fail {background:#ef4444}
        .chart-row__values {display:flex;flex-wrap:wrap;gap:.75rem;margin-top:.35rem;color:#64748b;font-size:.72rem}
        .legend {display:flex;flex-wrap:wrap;gap:.85rem;margin-top:1rem;font-size:.75rem;color:#475569}
        .legend span {display:inline-flex;align-items:center;gap:.35rem}
        .legend i {width:9px;height:9px;border-radius:2px;display:inline-block}
        .donut-wrap {display:grid;place-items:center;min-height:190px}
        .donut {
            width:168px;height:168px;border-radius:50%;display:grid;place-items:center;
            position:relative;box-shadow:inset 0 0 0 1px rgba(15,23,42,.04)
        }
        .donut::after {content:"";position:absolute;width:112px;height:112px;border-radius:50%;background:#fff;box-shadow:0 0 0 1px #e2e8f0}
        .donut__value {position:relative;z-index:1;text-align:center}
        .donut__value strong {display:block;font-size:1.7rem;color:#0f172a}
        .donut__value span {font-size:.72rem;color:#64748b}
        .report-metrics {display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:1rem;margin-bottom:1rem}
        .report-metric {background:#fff;border:1px solid #dbe4f0;border-radius:14px;padding:1rem 1.15rem}
        .report-metric__label {display:block;color:#64748b;font-size:.78rem;font-weight:600;margin-bottom:.35rem}
        .report-metric__value {font-size:1.55rem;font-weight:800;color:#0f172a}
        .report-metric__detail {display:block;font-size:.72rem;color:#94a3b8;margin-top:.25rem}
        .empty-chart {padding:3rem 1rem;text-align:center;color:#64748b;background:#f8fafc;border-radius:12px}
        @media (max-width:1050px) {
            .report-filter-grid {grid-template-columns:repeat(2,minmax(0,1fr))}
            .report-chart-grid {grid-template-columns:1fr}
            .report-metrics {grid-template-columns:repeat(2,minmax(0,1fr))}
        }
        @media (max-width:640px) {
            .report-filter-grid,.report-metrics {grid-template-columns:1fr}
            .period-tab {flex:1}
        }
    </style>
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="bao-cao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs">
            <a href="${ctx}/manager/dashboard">Dashboard</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Báo cáo thống kê</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Báo cáo thống kê sát hạch</h1>
                <p class="page-subtitle">Theo dõi kết quả của các hạng A1, A và B1 theo kỳ thi, tháng hoặc năm.</p>
            </div>
            <span class="action-badge action-badge--info" style="font-weight:700">${periodGroupLabel}</span>
        </header>

        <%-- Bộ lọc và biểu đồ được đặt đầu trang để quản lý nhìn thấy ngay. --%>
        <section class="report-control-panel" aria-label="Chọn phạm vi báo cáo">
            <nav class="period-tabs" aria-label="Kiểu phân kỳ">
                <a class="period-tab ${periodGroup eq 'exam' ? 'is-active' : ''}"
                   href="${ctx}/manager/reports?periodGroup=exam&amp;licenceClass=${selectedLicence}">Theo kỳ thi</a>
                <a class="period-tab ${periodGroup eq 'month' ? 'is-active' : ''}"
                   href="${ctx}/manager/reports?periodGroup=month&amp;year=${selectedYear}&amp;licenceClass=${selectedLicence}">Theo tháng</a>
                <a class="period-tab ${periodGroup eq 'year' ? 'is-active' : ''}"
                   href="${ctx}/manager/reports?periodGroup=year&amp;licenceClass=${selectedLicence}">Theo năm</a>
            </nav>

            <form action="${ctx}/manager/reports" method="get">
                <input type="hidden" name="periodGroup" value="${periodGroup}">
                <div class="report-filter-grid">
                    <div class="input-group" id="examFilterGroup">
                        <label for="examId" class="input-label">Kỳ thi sát hạch</label>
                        <select id="examId" name="examId" class="input-field">
                            <option value="0">Tất cả kỳ thi</option>
                            <c:forEach var="exam" items="${examOptions}">
                                <option value="${exam.examId}" ${selectedExamId eq exam.examId ? 'selected' : ''}>
                                    <c:out value="${exam.examCode}" /> · Hạng <c:out value="${exam.licenceClass}" /> ·
                                    <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" />
                                </option>
                            </c:forEach>
                        </select>
                        <div class="report-filter-note">Áp dụng khi xem theo kỳ thi.</div>
                    </div>

                    <div class="input-group" id="yearFilterGroup">
                        <label for="year" class="input-label">Năm báo cáo</label>
                        <select id="year" name="year" class="input-field">
                            <c:forEach var="reportYear" items="${availableYears}">
                                <option value="${reportYear}" ${selectedYear eq reportYear ? 'selected' : ''}>${reportYear}</option>
                            </c:forEach>
                        </select>
                        <div class="report-filter-note">Áp dụng khi xem theo tháng.</div>
                    </div>

                    <div class="input-group">
                        <label for="licenceClass" class="input-label">Hạng GPLX</label>
                        <select id="licenceClass" name="licenceClass" class="input-field">
                            <option value="">A1, A và B1</option>
                            <option value="A1" ${selectedLicence eq 'A1' ? 'selected' : ''}>Hạng A1</option>
                            <option value="A" ${selectedLicence eq 'A' ? 'selected' : ''}>Hạng A</option>
                            <option value="B1" ${selectedLicence eq 'B1' ? 'selected' : ''}>Hạng B1</option>
                        </select>
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn-filter">Áp dụng</button>
                        <a href="${ctx}/manager/reports" class="btn-reset">Đặt lại</a>
                    </div>
                </div>
            </form>
        </section>

        <section class="report-chart-grid" aria-label="Biểu đồ kết quả sát hạch">
            <article class="chart-card">
                <div class="chart-heading">
                    <div>
                        <h2>Biểu đồ phân bổ kết quả · ${periodGroupLabel}</h2>
                        <p>Mỗi thanh thể hiện tỷ trọng đạt và trượt; thí sinh vắng được tính là trượt.</p>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not empty reportData}">
                        <div class="chart-list">
                            <c:forEach var="row" items="${reportData}">
                                <div class="chart-row">
                                    <div class="chart-row__header">
                                        <span class="chart-row__title"><c:out value="${row.periodLabel}" /> · Hạng ${row.licenceClass}</span>
                                        <span class="chart-row__total">${row.totalCount} thí sinh</span>
                                    </div>
                                    <div class="stacked-bar" role="img"
                                         aria-label="Đạt ${row.passCount}, trượt ${row.failCount}">
                                        <span class="bar-pass" style="width:${row.passShare}%"></span>
                                        <span class="bar-fail" style="width:${row.failShare}%"></span>
                                    </div>
                                    <div class="chart-row__values">
                                        <span>Đạt: <strong>${row.passCount}</strong></span>
                                        <span>Trượt: <strong>${row.failCount}</strong></span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <div class="legend">
                            <span><i class="bar-pass"></i>Đạt</span>
                            <span><i class="bar-fail"></i>Trượt</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-chart">Không có dữ liệu trong phạm vi đã chọn.</div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="chart-card">
                <div class="chart-heading">
                    <div>
                        <h2>Tỷ lệ đạt chung</h2>
                        <p>Tính trên các thí sinh đã có kết quả đạt hoặc trượt.</p>
                    </div>
                </div>
                <div class="donut-wrap">
                    <div class="donut" style="background:conic-gradient(#10b981 0 ${passRateAll}%,#e2e8f0 ${passRateAll}% 100%)">
                        <div class="donut__value">
                            <strong>${passRateAll}%</strong>
                            <span>${totalPassed}/${totalPassed + totalFailed} đã đánh giá</span>
                        </div>
                    </div>
                </div>
                <div class="legend" style="justify-content:center">
                    <span><i class="bar-pass"></i>${totalPassed} đạt</span>
                    <span><i class="bar-fail"></i>${totalFailed} trượt</span>
                </div>
            </article>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>
</body>
</html>
