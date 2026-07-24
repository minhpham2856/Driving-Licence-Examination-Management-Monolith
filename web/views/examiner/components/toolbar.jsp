<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="wrapperClass" value="${not empty param.wrapperClass ? param.wrapperClass : 'examiner-toolbar examiner-toolbar--tools'}" />
<c:set var="leftClass" value="${not empty param.leftClass ? param.leftClass : 'examiner-toolbar__group'}" />
<c:set var="rightClass" value="${not empty param.rightClass ? param.rightClass : 'examiner-toolbar__group examiner-toolbar__search-form'}" />

<section class="${wrapperClass}">

    <!--left-->
    <div class="${leftClass}">

        <c:if test="${param.btnBack eq 'left'}">
            <a href="${requestScope.backUrl}" class="${not empty param.backClass ? param.backClass : 'examiner-btn examiner-btn--white'}">
                <span class="material-symbols-outlined">arrow_back</span>Quay lại
            </a>
        </c:if>

        <c:if test="${param.btnPrintInfo eq 'left'}">
            <a href="${ctx}/examiner/print?type=candidates" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In thông tin chi tiết
            </a>
        </c:if>

        <c:if test="${param.btnPrintList eq 'left'}">
            <a href="${ctx}/examiner/print?type=candidates" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">list</span>In danh sách
            </a>
        </c:if>

        <c:if test="${param.btnPrintResult eq 'left'}">
            <a href="${ctx}/examiner/print?type=result" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">description</span>In kết quả
            </a>
        </c:if>

        <c:if test="${param.btnExportExcel eq 'left'}">
            <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất Excel
            </a>
        </c:if>


        <c:if test="${param.btnExportCandidatesExcel eq 'left'}">
            <a href="${requestScope.exportCandidatesUrl}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất DS Excel
            </a>
        </c:if>


        <c:if test="${param.btnPrintAudit eq 'left'}">
            <a href="${ctx}/examiner/print?type=audit&amp;q=${requestScope.searchQuery}" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In nhật ký
            </a>
            <a href="${pageContext.request.contextPath}/examiner/export/audit?q=${requestScope.searchQuery}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất Excel
            </a>
        </c:if>

        <c:if test="${param.btnVehicle eq 'left'}">
            <c:if test="${not empty requestScope.candidate and not empty requestScope.examVehicles}">
                <form method="post" action="${requestScope.pageUrl}" class="score-entry-vehicle-form">
                    <input type="hidden" name="action" value="changeVehicle">
                    <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                    <select name="deviceId" class="score-entry-select" aria-label="Chọn xe" required>
                        <option value="">Chọn xe...</option>
                        <c:forEach var="vehicle" items="${requestScope.examVehicles}">
                            <option value="${vehicle.id}"${requestScope.candidateVehicleId eq vehicle.id ? ' selected' : ''}>${vehicle.name}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="examiner-btn examiner-btn--success">Thay xe</button>
                </form>
            </c:if>
            <c:if test="${empty requestScope.candidate or empty requestScope.examVehicles}">
                <select class="score-entry-select" aria-label="Chọn xe" disabled><option value="">Chọn xe...</option></select>
                <button type="button" class="examiner-btn examiner-btn--success" disabled>Thay xe</button>
            </c:if>
        </c:if>

        <c:if test="${param.btnViolation eq 'left'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.suspended}">
                    <form method="post" action="${ctx}/examiner/violations" style="display:inline">
                        <input type="hidden" name="action" value="undoSuspend">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">undo</span>Gỡ đình chỉ</button>
                    </form>
                </c:when>
                <c:when test="${not empty requestScope.candidate}">
                    <form method="post" action="${ctx}/examiner/violations" style="display:inline">
                        <input type="hidden" name="action" value="suspend">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--danger">
                            <span class="material-symbols-outlined">gavel</span>Đình chỉ</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <button type="button" class="examiner-btn examiner-btn--danger" disabled><span class="material-symbols-outlined">gavel</span>Đình chỉ</button>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnPrintSignature eq 'left'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and (requestScope.candidate.status == 'awaiting' or requestScope.candidate.status == 'done')}">
                    <form method="post" action="${requestScope.pageUrl}" target="examinerPrintTab" style="display:inline"
                          onsubmit="window.open('', 'examinerPrintTab'); setTimeout(function () { window.location.reload(); }, 800);">
                        <input type="hidden" name="action" value="printResult">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--orange">
                            <span class="material-symbols-outlined">print</span>In biên bản</button>
                    </form>
                    </c:when>
                    <c:otherwise>
                    <span class="examiner-btn examiner-btn--orange examiner-btn--disabled"><span class="material-symbols-outlined">print</span>In biên bản</span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnComplete eq 'left'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.completeEligible}">
                    <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                        <input type="hidden" name="action" value="completeSectionScore">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">check_circle</span>Hoàn tất</button>
                    </form>
                    </c:when>
                    <c:when test="${not empty requestScope.candidate and requestScope.candidate.status == 'awaiting'}">
                    <span class="examiner-btn examiner-btn--primary examiner-btn--disabled"><span class="material-symbols-outlined">check_circle</span>Hoàn tất</span>
                </c:when>
                <c:otherwise>
                    <span class="examiner-btn examiner-btn--primary examiner-btn--disabled"><span class="material-symbols-outlined">check_circle</span>Hoàn tất</span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnStart eq 'left'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.markPresentEligible}">
                    <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                        <input type="hidden" name="action" value="markPresent">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--success">
                            <span class="material-symbols-outlined">play_arrow</span>Bắt đầu
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <span class="examiner-btn examiner-btn--success examiner-btn--disabled">
                        <span class="material-symbols-outlined">play_arrow</span>Bắt đầu
                    </span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnSave eq 'left'}">
            <button type="submit" form="${param.saveFormId}" class="examiner-btn examiner-btn--primary">Lưu thay đổi</button>
        </c:if>

        <c:if test="${param.btnViewPaper eq 'left'}">
            <c:if test="${requestScope.examinerSectionTheory}">
                <c:choose>
                    <c:when test="${not empty requestScope.candidate and (requestScope.candidate.status == 'awaiting' or requestScope.candidate.status == 'done')}">
                        <a href="${requestScope.paperUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">visibility</span>Xem đề thi
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span class="examiner-btn examiner-btn--white examiner-btn--disabled">
                            <span class="material-symbols-outlined">visibility</span>Xem đề thi
                        </span>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:if>

        <c:if test="${param.btnEditResult eq 'left'}">
            <c:if test="${not requestScope.examinerSectionTheory}">
                <a href="${requestScope.resultUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">fact_check</span>Sửa kết quả
                </a>
            </c:if>
        </c:if>

        <c:if test="${param.btnPrintDocs eq 'left'}">
            <a href="${pageContext.request.contextPath}/examiner/print-documents" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In kết quả thi
            </a>
        </c:if>

        <c:if test="${param.btnPrintViolation eq 'left'}">
            <a href="${ctx}/examiner/print?type=violations" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In danh sách vi phạm
            </a>
        </c:if>

        <c:if test="${param.btnPaperFilter eq 'left'}">
            <c:set var="_curF" value="${empty param.filter ? 'all' : param.filter}" />
            <c:set var="_sortP" value="${not empty param.sort ? '&amp;sort='.concat(param.sort) : ''}${not empty param.dir ? '&amp;dir='.concat(param.dir) : ''}" />
            <div class="paper-filter-tabs">
                <a href="${requestScope.pageUrl}&amp;filter=all${_sortP}" class="paper-filter-tab paper-filter-tab--all ${_curF == 'all' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">apps</span>Tất cả (${empty requestScope.paperSummary.totalCount ? 0 : requestScope.paperSummary.totalCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=correct${_sortP}" class="paper-filter-tab paper-filter-tab--correct ${_curF == 'correct' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">check</span>Câu đúng (${empty requestScope.paperSummary.correctCount ? 0 : requestScope.paperSummary.correctCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=wrong${_sortP}" class="paper-filter-tab paper-filter-tab--wrong ${_curF == 'wrong' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">close</span>Câu sai (${empty requestScope.paperSummary.wrongCount ? 0 : requestScope.paperSummary.wrongCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=unanswered${_sortP}" class="paper-filter-tab paper-filter-tab--skipped ${_curF == 'unanswered' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">remove</span>Bỏ (${empty requestScope.paperSummary.unansweredCount ? 0 : requestScope.paperSummary.unansweredCount})</a>
            </div>
        </c:if>

        <c:if test="${param.btnRefresh eq 'left'}">
            <a href="${requestScope.pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">
                <span class="material-symbols-outlined">refresh</span>
            </a>
        </c:if>

    </div>


    <!--right-->
    <div class="${rightClass}">

        <c:if test="${param.btnSearch eq 'right'}">
            <form action="${requestScope.pageUrl}" method="get" style="display:contents">
                <c:if test="${not empty requestScope.sortBy}"><input type="hidden" name="sort" value="${requestScope.sortBy}"></c:if>
                <c:if test="${not empty requestScope.sortDir}"><input type="hidden" name="dir" value="${requestScope.sortDir}"></c:if>
                <div class="examiner-search ${param.searchWide == 'true' ? 'examiner-search--wide' : ''}">
                    <input type="text" name="q" class="examiner-search__input" value="${requestScope.searchQuery}"
                           placeholder="${not empty param.searchPlaceholder ? param.searchPlaceholder : 'Tìm kiếm...'}">
                </div>
                <button type="submit" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">search</span>Tìm kiếm</button>
            </form>
        </c:if>

        <c:if test="${param.btnBack eq 'right'}">
            <a href="${requestScope.backUrl}" class="${not empty param.backClass ? param.backClass : 'examiner-btn examiner-btn--white'}">
                <span class="material-symbols-outlined">arrow_back</span>Quay lại
            </a>
        </c:if>

        <c:if test="${param.btnPrintInfo eq 'right'}">
            <a href="${ctx}/examiner/print?type=candidates" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In thông tin chi tiết
            </a>
        </c:if>

        <c:if test="${param.btnPrintList eq 'right'}">
            <a href="${ctx}/examiner/print?type=candidates" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">list</span>In danh sách
            </a>
        </c:if>

        <c:if test="${param.btnPrintResult eq 'right'}">
            <a href="${ctx}/examiner/print?type=result" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">description</span>In kết quả
            </a>
        </c:if>

        <c:if test="${param.btnExportExcel eq 'right'}">
            <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất Excel
            </a>
        </c:if>


        <c:if test="${param.btnExportCandidatesExcel eq 'right'}">
            <a href="${requestScope.exportCandidatesUrl}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất DS Excel
            </a>
        </c:if>


        <c:if test="${param.btnPrintAudit eq 'right'}">
            <a href="${ctx}/examiner/print?type=audit&amp;q=${requestScope.searchQuery}" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In nhật ký
            </a>
            <a href="${pageContext.request.contextPath}/examiner/export/audit?q=${requestScope.searchQuery}" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">download</span>Xuất Excel
            </a>
        </c:if>

        <c:if test="${param.btnVehicle eq 'right'}">
            <c:if test="${not empty requestScope.candidate and not empty requestScope.examVehicles}">
                <form method="post" action="${requestScope.pageUrl}" class="score-entry-vehicle-form">
                    <input type="hidden" name="action" value="changeVehicle">
                    <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                    <select name="deviceId" class="score-entry-select" aria-label="Chọn xe" required>
                        <option value="">Chọn xe...</option>
                        <c:forEach var="vehicle" items="${requestScope.examVehicles}">
                            <option value="${vehicle.id}"${requestScope.candidateVehicleId eq vehicle.id ? ' selected' : ''}>${vehicle.name}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="examiner-btn examiner-btn--success">Thay xe</button>
                </form>
            </c:if>
            <c:if test="${empty requestScope.candidate or empty requestScope.examVehicles}">
                <select class="score-entry-select" aria-label="Chọn xe" disabled><option value="">Chọn xe...</option></select>
                <button type="button" class="examiner-btn examiner-btn--success" disabled>Thay xe</button>
            </c:if>
        </c:if>

        <c:if test="${param.btnViolation eq 'right'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.suspended}">
                    <form method="post" action="${ctx}/examiner/violations" style="display:inline">
                        <input type="hidden" name="action" value="undoSuspend">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">undo</span>Gỡ đình chỉ</button>
                    </form>
                </c:when>
                <c:when test="${not empty requestScope.candidate}">
                    <form method="post" action="${ctx}/examiner/violations" style="display:inline">
                        <input type="hidden" name="action" value="suspend">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--danger">
                            <span class="material-symbols-outlined">gavel</span>Đình chỉ</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <button type="button" class="examiner-btn examiner-btn--danger" disabled><span class="material-symbols-outlined">gavel</span>Đình chỉ</button>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnPrintSignature eq 'right'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and (requestScope.candidate.status == 'awaiting' or requestScope.candidate.status == 'done')}">
                    <form method="post" action="${requestScope.pageUrl}" target="examinerPrintTab" style="display:inline"
                          onsubmit="window.open('', 'examinerPrintTab'); setTimeout(function () { window.location.reload(); }, 800);">
                        <input type="hidden" name="action" value="printResult">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--orange">
                            <span class="material-symbols-outlined">print</span>In biên bản</button>
                    </form>
                    </c:when>
                    <c:otherwise>
                    <span class="examiner-btn examiner-btn--orange examiner-btn--disabled"><span class="material-symbols-outlined">print</span>In biên bản</span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnComplete eq 'right'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.completeEligible}">
                    <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                        <input type="hidden" name="action" value="completeSectionScore">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">check_circle</span>Hoàn tất</button>
                    </form>
                    </c:when>
                    <c:when test="${not empty requestScope.candidate and requestScope.candidate.status == 'awaiting'}">
                    <span class="examiner-btn examiner-btn--primary examiner-btn--disabled"><span class="material-symbols-outlined">check_circle</span>Hoàn tất</span>
                </c:when>
                <c:otherwise>
                    <span class="examiner-btn examiner-btn--primary examiner-btn--disabled"><span class="material-symbols-outlined">check_circle</span>Hoàn tất</span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnStart eq 'right'}">
            <c:choose>
                <c:when test="${not empty requestScope.candidate and requestScope.candidate.markPresentEligible}">
                    <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                        <input type="hidden" name="action" value="markPresent">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.candidateNumber}">
                        <button type="submit" class="examiner-btn examiner-btn--success">
                            <span class="material-symbols-outlined">play_arrow</span>Bắt đầu
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <span class="examiner-btn examiner-btn--success examiner-btn--disabled">
                        <span class="material-symbols-outlined">play_arrow</span>Bắt đầu
                    </span>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${param.btnSave eq 'right'}">
            <button type="submit" form="${param.saveFormId}" class="examiner-btn examiner-btn--primary">Lưu thay đổi</button>
        </c:if>

        <c:if test="${param.btnViewPaper eq 'right'}">
            <c:if test="${requestScope.examinerSectionTheory}">
                <a href="${requestScope.paperUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">visibility</span>Xem đề thi</a>
                </c:if>
            </c:if>

        <c:if test="${param.btnEditResult eq 'right'}">
            <c:if test="${not requestScope.examinerSectionTheory}">
                <a href="${requestScope.resultUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">fact_check</span>Sửa kết quả
                </a>
            </c:if>
        </c:if>

        <c:if test="${param.btnPrintDocs eq 'right'}">
            <a href="${pageContext.request.contextPath}/examiner/print-documents" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In kết quả thi
            </a>
        </c:if>

        <c:if test="${param.btnPrintViolation eq 'right'}">
            <a href="${ctx}/examiner/print?type=violations" target="_blank" rel="noopener" class="examiner-btn examiner-btn--white">
                <span class="material-symbols-outlined">print</span>In danh sách vi phạm
            </a>
        </c:if>

        <c:if test="${param.btnPaperFilter eq 'right'}">
            <c:set var="_curF" value="${empty param.filter ? 'all' : param.filter}" />
            <c:set var="_sortP" value="${not empty param.sort ? '&amp;sort='.concat(param.sort) : ''}${not empty param.dir ? '&amp;dir='.concat(param.dir) : ''}" />
            <div class="paper-filter-tabs">
                <a href="${requestScope.pageUrl}&amp;filter=all${_sortP}" class="paper-filter-tab paper-filter-tab--all ${_curF == 'all' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">apps</span>Tất cả (${empty requestScope.paperSummary.totalCount ? 0 : requestScope.paperSummary.totalCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=correct${_sortP}" class="paper-filter-tab paper-filter-tab--correct ${_curF == 'correct' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">check</span>Câu đúng (${empty requestScope.paperSummary.correctCount ? 0 : requestScope.paperSummary.correctCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=wrong${_sortP}" class="paper-filter-tab paper-filter-tab--wrong ${_curF == 'wrong' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">close</span>Câu sai (${empty requestScope.paperSummary.wrongCount ? 0 : requestScope.paperSummary.wrongCount})</a>
                <a href="${requestScope.pageUrl}&amp;filter=unanswered${_sortP}" class="paper-filter-tab paper-filter-tab--skipped ${_curF == 'unanswered' ? 'is-active' : ''}">
                    <span class="material-symbols-outlined">remove</span>Bỏ (${empty requestScope.paperSummary.unansweredCount ? 0 : requestScope.paperSummary.unansweredCount})</a>
            </div>
        </c:if>

        <c:if test="${param.btnRefresh eq 'right'}">
            <a href="${requestScope.pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">
                <span class="material-symbols-outlined">refresh</span>
            </a>
        </c:if>

    </div>

</section>
