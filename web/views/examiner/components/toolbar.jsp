<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="wrapperClass" value="${not empty param.wrapperClass ? param.wrapperClass : 'examiner-toolbar examiner-toolbar--tools'}" />
<c:set var="leftClass" value="${not empty param.leftClass ? param.leftClass : 'examiner-toolbar__group'}" />
<c:set var="rightClass" value="${not empty param.rightClass ? param.rightClass : 'examiner-toolbar__group examiner-toolbar__search-form'}" />

<section class="${wrapperClass}">

    <!--left-->
    <c:if test="${param.showBack == 'true' or param.showPrintGroup == 'true' or param.showVehicleAction == 'true' or param.showSave == 'true' or param.showConfirm == 'true' or param.showUndo == 'true' or param.showResultDetailsPrintGroup == 'true' or param.showPaperFilterGroup == 'true' or param.showResultEditPrintGroup == 'true' or param.showAuditPrintGroup == 'true' or param.showCandidateCallLeft == 'true'}">
        <div class="${leftClass}">

            <c:if test="${param.showBack == 'true'}">
                <a href="${requestScope.backUrl}" class="${param.backClass != null ? param.backClass : 'examiner-btn examiner-btn--white'}">
                    <span class="material-symbols-outlined">arrow_back</span>Quay lại
                </a>
            </c:if>

            <c:if test="${param.showPrintGroup == 'true'}">
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">print</span>In thông tin chi tiết
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">list</span>In danh sách
                </a>
                <a href="#" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">description</span>In kết quả
                </a>
            </c:if>


            <c:if test="${param.showResultDetailsPrintGroup == 'true'}">
                <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                    <span class="material-symbols-outlined">print</span>In thông tin chi tiết
                </button>
                <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất Excel
                </a>
                <a href="${requestScope.exportResultsXmlUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất XML
                </a>
                <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                    <span class="material-symbols-outlined">list</span>In danh sách
                </button>
                <a href="${requestScope.exportCandidatesUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất DS Excel
                </a>
                <a href="${requestScope.exportCandidatesXmlUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất DS XML
                </a>
                <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                    <span class="material-symbols-outlined">description</span>In kết quả
                </button>
            </c:if>

            <c:if test="${param.showResultEditPrintGroup == 'true'}">
                <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                    <span class="material-symbols-outlined">print</span>In thông tin chi tiết
                </button>
                <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất Excel
                </a>
                <a href="${requestScope.exportResultsXmlUrl}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất XML
                </a>
                <c:if test="${requestScope.examinerSectionTheory}">
                    <a href="${requestScope.paperUrl}" class="examiner-btn examiner-btn--white">
                        <span class="material-symbols-outlined">visibility</span>Xem đề thi
                    </a>
                </c:if>
            </c:if>

            <c:if test="${param.showAuditPrintGroup == 'true'}">
                <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                    <span class="material-symbols-outlined">print</span>In nhật ký
                </button>
                <a href="${requestScope.ctx}/examiner/export/audit?q=${requestScope.searchQuery}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất Excel
                </a>
                <a href="${requestScope.ctx}/examiner/export/audit/xml?q=${requestScope.searchQuery}" class="examiner-btn examiner-btn--white">
                    <span class="material-symbols-outlined">download</span>Xuất XML
                </a>
            </c:if>

            <c:if test="${param.showCandidateCallLeft == 'true'}">
                <button type="submit" form="callSelectedForm" class="examiner-btn examiner-btn--primary">
                    <span class="material-symbols-outlined">group_add</span>Gọi đã chọn
                </button>
            </c:if>

            <c:if test="${param.showVehicleAction == 'true'}">
                <c:if test="${not empty requestScope.candidate and not empty requestScope.sessionVehicles}">
                    <form method="get" action="${requestScope.pageUrl}" class="score-entry-vehicle-form">
                        <input type="hidden" name="action" value="changeVehicle">
                        <input type="hidden" name="sbd" value="${requestScope.candidate.sbd}">
                        <select name="deviceId" class="score-entry-select" aria-label="Chọn xe" required>
                            <option value="">Chọn xe...</option>
                            <c:forEach var="vehicle" items="${requestScope.sessionVehicles}">
                                <option value="${vehicle.id}"${requestScope.candidateVehicleId eq vehicle.id ? ' selected' : ''}>
                                    ${vehicle.name}
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="examiner-btn examiner-btn--success">Thay xe</button>
                    </form>
                </c:if>
                <c:if test="${empty requestScope.candidate or empty requestScope.sessionVehicles}">
                    <select class="score-entry-select" aria-label="Chọn xe" disabled>
                        <option value="">Chọn xe...</option>
                    </select>
                    <button type="button" class="examiner-btn examiner-btn--success" disabled>Thay xe</button>
                </c:if>

                <c:choose>
                    <c:when test="${not empty requestScope.candidate}">
                        <a href="${requestScope.pageUrl}?action=deferAbsent&amp;sbd=${requestScope.candidate.sbd}" class="examiner-btn examiner-btn--danger">Vắng</a>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="examiner-btn examiner-btn--danger" disabled>Vắng</button>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <c:if test="${param.showSave == 'true'}">
                <button type="submit" form="${param.saveFormId}" class="examiner-btn examiner-btn--primary">Lưu thay đổi</button>
            </c:if>

        </div>
    </c:if>


    <c:if test="${param.showSearch == 'true' or param.showRefresh == 'true' or param.showPrintFinal == 'true' or param.showViolationPrintGroup == 'true' or param.showViewPaper == 'true' or param.showExportDocx == 'true' or param.showCandidateDetailsActions == 'true' or param.showPaperFilterGroup == 'true'}">
        <c:choose>

            <c:when test="${param.showSearch == 'true'}">
                <form action="${requestScope.pageUrl}" method="get" class="${rightClass}">
                    <c:if test="${not empty requestScope.sortBy}"><input type="hidden" name="sort" value="${requestScope.sortBy}"></c:if>
                    <c:if test="${not empty requestScope.sortDir}"><input type="hidden" name="dir" value="${requestScope.sortDir}"></c:if>
                    <div class="examiner-search ${param.searchWide == 'true' ? 'examiner-search--wide' : ''}">
                        <input type="text" name="q" class="examiner-search__input" value="${requestScope.searchQuery}"
                               placeholder="${not empty param.searchPlaceholder ? param.searchPlaceholder : 'Tìm kiếm...'}">
                    </div>
                    <button type="submit" class="examiner-btn examiner-btn--primary">
                        <span class="material-symbols-outlined">search</span>Tìm kiếm
                    </button>
                    <c:if test="${param.showRefresh == 'true'}">
                        <a href="${requestScope.pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </c:if>
                </form>
            </c:when>
            <c:otherwise>
                <div class="${rightClass}">
                    <c:if test="${param.showViolationPrintGroup == 'true'}">
                        <a href="${requestScope.exportXmlUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>Xuất Docx
                        </a>
                        <button type="button" class="examiner-btn examiner-btn--white" onclick="window.print();">
                            <span class="material-symbols-outlined">print</span>In biên bản
                        </button>
                    </c:if>
                    <c:if test="${param.showPrintFinal == 'true'}">
                        <a href="${requestScope.ctx}/views/examiner/print-documents" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>In kết quả thi
                        </a>
                        <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>Xuất Docx
                        </a>
                    </c:if>
                    <c:if test="${param.showViewPaper == 'true'}">
                        <c:if test="${requestScope.examinerSectionTheory}">
                            <a href="${requestScope.paperUrl}" class="examiner-btn examiner-btn--white">
                                <span class="material-symbols-outlined">visibility</span>Xem đề thi
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${param.showExportDocx == 'true'}">
                        <a href="${requestScope.exportResultsUrl}" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">download</span>Xuất Docx
                        </a>
                    </c:if>
                    <c:if test="${param.showCandidateDetailsActions == 'true'}">
                        <c:if test="${not empty requestScope.candidate}">
                            <c:if test="${requestScope.examinerSectionTheory}">
                                <a href="${requestScope.paperUrl}" class="examiner-btn examiner-btn--white">
                                    <span class="material-symbols-outlined">visibility</span>Xem đề thi
                                </a>
                            </c:if>
                            <c:if test="${not requestScope.examinerSectionTheory}">
                                <a href="${requestScope.resultUrl}" class="examiner-btn examiner-btn--white">
                                    <span class="material-symbols-outlined">fact_check</span>Sửa kết quả
                                </a>
                            </c:if>
                        </c:if>
                    </c:if>

                    <c:if test="${param.showPaperFilterGroup == 'true'}">
                        <c:set var="currentFilter" value="${empty param.filter ? 'all' : param.filter}" />
                        <c:set var="sortParams" value="${not empty param.sort ? '&sort='.concat(param.sort) : ''}${not empty param.dir ? '&dir='.concat(param.dir) : ''}" />
                        <div class="paper-filter-tabs">
                            <a href="${requestScope.pageUrl}&filter=all${sortParams}" class="paper-filter-tab paper-filter-tab--all ${currentFilter == 'all' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">apps</span>Tất cả (${empty requestScope.paperSummary.totalCount ? 0 : requestScope.paperSummary.totalCount})
                            </a>
                            <a href="${requestScope.pageUrl}&filter=correct${sortParams}" class="paper-filter-tab paper-filter-tab--correct ${currentFilter == 'correct' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">check</span>Câu đúng (${empty requestScope.paperSummary.correctCount ? 0 : requestScope.paperSummary.correctCount})
                            </a>
                            <a href="${requestScope.pageUrl}&filter=wrong${sortParams}" class="paper-filter-tab paper-filter-tab--wrong ${currentFilter == 'wrong' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">close</span>Câu sai (${empty requestScope.paperSummary.wrongCount ? 0 : requestScope.paperSummary.wrongCount})
                            </a>
                            <a href="${requestScope.pageUrl}&filter=unanswered${sortParams}" class="paper-filter-tab paper-filter-tab--skipped ${currentFilter == 'unanswered' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">remove</span>Bỏ (${empty requestScope.paperSummary.unansweredCount ? 0 : requestScope.paperSummary.unansweredCount})
                            </a>
                        </div>
                    </c:if>
                    <c:if test="${param.showRefresh == 'true'}">
                        <a href="${requestScope.pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

</section>
