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
        .report-filter-grid {display:grid;grid-template-columns:minmax(220px,1fr) minmax(220px,1fr) auto;gap:.85rem;align-items:end}
        .report-filter-panel {margin-top:1rem;margin-bottom:1rem}
        .report-filter-note {font-size:.75rem;color:#64748b;margin-top:.3rem}
        .report-chart-grid {display:none}
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
        .trend-card {margin-bottom:1rem}
        .trend-canvas-wrap {position:relative;width:100%;height:320px}
        #passTrendChart {width:100%;height:100%;display:block}
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
                <p class="page-subtitle">Theo dõi kết quả của các hạng A1, A và B1 theo tháng hoặc năm.</p>
            </div>
            <span class="action-badge action-badge--info" style="font-weight:700">${periodGroupLabel}</span>
        </header>

        <%-- Bộ lọc và biểu đồ được đặt đầu trang để quản lý nhìn thấy ngay. --%>
        <section class="report-control-panel" aria-label="Chọn phạm vi báo cáo">
            <nav class="period-tabs" aria-label="Kiểu phân kỳ">
                <a class="period-tab ${periodGroup eq 'month' ? 'is-active' : ''}"
                   href="${ctx}/manager/reports?periodGroup=month&amp;year=${selectedYear}&amp;licenceClass=${selectedLicence}">Theo tháng</a>
                <a class="period-tab ${periodGroup eq 'year' ? 'is-active' : ''}"
                   href="${ctx}/manager/reports?periodGroup=year&amp;licenceClass=${selectedLicence}">Theo năm</a>
            </nav>

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

        <section class="chart-card trend-card" aria-label="Biểu đồ đường xu hướng tỷ lệ đạt">
            <div class="chart-heading">
                <div>
                    <h2>Biểu đồ tỷ lệ đạt ${periodGroup eq 'month' ? 'theo tháng' : 'theo năm'}</h2>
                    <p>${periodGroup eq 'month' ? 'Mỗi điểm cộng toàn bộ kỳ thi cùng hạng trong tháng; tháng chưa có dữ liệu được để trống.' : 'Mỗi điểm cộng toàn bộ kỳ thi cùng hạng trong cả năm.'}</p>
                </div>
            </div>
            <c:choose>
                <c:when test="${not empty trendData}">
                    <div id="trendData" data-year="${selectedYear}" data-group="${periodGroup}" hidden>
                        <c:forEach var="row" items="${trendData}">
                            <span data-period="${row.periodLabel}" data-licence="${row.licenceClass}" data-rate="${row.passRate}"></span>
                        </c:forEach>
                    </div>
                    <div class="trend-canvas-wrap"><canvas id="passTrendChart" role="img" aria-label="Xu hướng tỷ lệ đạt"></canvas></div>
                    <div class="legend" id="trendLegend"></div>
                </c:when>
                <c:otherwise><div class="empty-chart">Không có dữ liệu để vẽ đường xu hướng.</div></c:otherwise>
            </c:choose>
        </section>

        <section class="report-control-panel report-filter-panel" aria-label="Bộ lọc báo cáo">
            <form action="${ctx}/manager/reports" method="get">
                <input type="hidden" name="periodGroup" value="${periodGroup}">
                <div class="report-filter-grid">
                    <c:if test="${periodGroup eq 'month'}">
                        <div class="input-group" id="yearFilterGroup">
                            <label for="year" class="input-label">Năm báo cáo</label>
                            <select id="year" name="year" class="input-field">
                                <c:forEach var="reportYear" items="${availableYears}">
                                    <option value="${reportYear}" ${selectedYear eq reportYear ? 'selected' : ''}>${reportYear}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>

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
                        <a href="${ctx}/manager/reports?periodGroup=${periodGroup}" class="btn-reset">Đặt lại</a>
                    </div>
                </div>
            </form>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>
<script>
(() => {
    const canvas = document.getElementById('passTrendChart');
    const source = document.getElementById('trendData');
    if (!canvas || !source) return;
    const year = Number(source.dataset.year || new Date().getFullYear());
    const group = source.dataset.group || 'month';
    const points = [...source.querySelectorAll('[data-rate]')].map(el => {
        const period = el.dataset.period || '';
        const monthMatch = period.match(/(\d{2})\/(\d{4})/);
        const yearMatch = period.match(/(\d{4})/);
        const key = group === 'year'
            ? (yearMatch ? yearMatch[1] : period)
            : (monthMatch ? monthMatch[1] + '/' + monthMatch[2] : period);
        return {period, key,
            licence: el.dataset.licence || '', rate: Number(el.dataset.rate || 0)};
    });
    if (!points.length) return;
    const periods = group === 'year'
        ? [...new Set(points.map(p => p.key))]
            .sort((a,b) => Number(a) - Number(b))
            .map(value => ({key:value,label:'Năm ' + value}))
        : Array.from({length:12},(_,i)=>({
            key:String(i+1).padStart(2,'0') + '/' + year,
            label:'Tháng ' + (i+1)
        }));
    const licences = [...new Set(points.map(p => p.licence))];
    const colors = {A1:'#0052cc',A:'#f59e0b',B1:'#10b981'};
    const legend = document.getElementById('trendLegend');
    if (legend) legend.innerHTML = licences.map(l => '<span><i style="background:'
        + (colors[l] || '#7c3aed') + '"></i>Hạng ' + l + '</span>').join('');
    const draw = () => {
        const box = canvas.parentElement.getBoundingClientRect();
        const ratio = window.devicePixelRatio || 1;
        canvas.width = Math.max(320, box.width) * ratio;
        canvas.height = Math.max(260, box.height) * ratio;
        const ctx = canvas.getContext('2d');
        ctx.scale(ratio, ratio);
        const w = canvas.width / ratio, h = canvas.height / ratio;
        const pad = {left:52,right:24,top:22,bottom:72};
        const cw=w-pad.left-pad.right,ch=h-pad.top-pad.bottom;
        ctx.clearRect(0,0,w,h);ctx.font='12px Arial';ctx.fillStyle='#64748b';ctx.strokeStyle='#e2e8f0';ctx.lineWidth=1;
        for(let value=0;value<=100;value+=20){const y=pad.top+ch-(value/100)*ch;ctx.beginPath();ctx.moveTo(pad.left,y);ctx.lineTo(w-pad.right,y);ctx.stroke();ctx.fillText(value+'%',8,y+4);}
        const x=i=>periods.length===1?pad.left+cw/2:pad.left+(i/(periods.length-1))*cw;
        const y=v=>pad.top+ch-(Math.max(0,Math.min(100,v))/100)*ch;
        licences.forEach(licence=>{
            const series=periods.map(period=>points.find(p=>p.key===period.key&&p.licence===licence));
            ctx.strokeStyle=colors[licence]||'#7c3aed';ctx.lineWidth=3;ctx.lineJoin='round';ctx.beginPath();let started=false;
            series.forEach((p,i)=>{if(!p)return;if(started)ctx.lineTo(x(i),y(p.rate));else{ctx.moveTo(x(i),y(p.rate));started=true;}});ctx.stroke();
            series.forEach((p,i)=>{if(!p)return;const px=x(i),py=y(p.rate);ctx.fillStyle='#fff';ctx.strokeStyle=colors[licence]||'#7c3aed';ctx.lineWidth=3;ctx.beginPath();ctx.arc(px,py,5,0,Math.PI*2);ctx.fill();ctx.stroke();ctx.fillStyle='#0f172a';ctx.font='bold 11px Arial';ctx.textAlign='center';ctx.fillText(p.rate.toFixed(1)+'%',px,py-11);});
        });
        periods.forEach((period,i)=>{const px=x(i);ctx.font='11px Arial';ctx.fillStyle='#475569';ctx.textAlign='center';ctx.fillText(period.label,px,h-22);});
    };
    draw();let timer;window.addEventListener('resize',()=>{clearTimeout(timer);timer=setTimeout(draw,120);});
})();
</script>
</body>
</html>
