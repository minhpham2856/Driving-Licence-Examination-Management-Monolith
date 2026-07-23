<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Vi phạm" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vi phạm</title>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet">
    <jsp:include page="/views/examiner/components/examiner-styles.jsp">
        <jsp:param name="pageCss" value="score-entry.css" />
    </jsp:include>
</head>
<body class="has-side-nav-bar examiner-portal">
<jsp:include page="/views/layout/sidebar-examiner.jsp"><jsp:param name="activeSidebar" value="violations"/></jsp:include>
<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp"/>
    <main class="examiner-main">
        <jsp:include page="/views/examiner/components/examiner-messages.jsp"/>
        <c:choose>
            <c:when test="${param.mode eq 'create' and not empty candidate}">
                <section class="examiner-card">
                    <div class="examiner-card__head"><h2>Đình chỉ thí sinh</h2></div>
                    <p><strong>SBD:</strong> ${candidate.candidateNumber}</p>
                    <p><strong>Họ tên:</strong> ${candidate.fullName}</p>
                    <p><strong>Trạng thái:</strong> ${candidate.statusLabel}</p>
                    <form method="post" action="${ctx}/examiner/violations" enctype="multipart/form-data">
                        <input type="hidden" name="action" value="createViolation">
                        <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                        <label for="reasonCode">Lý do đình chỉ</label>
                        <select id="reasonCode" name="reasonCode" required>
                            <option value="">-- Chọn lý do --</option>
                            <c:forEach var="reason" items="${violationReasons}">
                                <option value="${reason.code}">${reason.label}</option>
                            </c:forEach>
                        </select>
                        <label for="reasonDetail">Chi tiết vi phạm</label>
                        <textarea id="reasonDetail" name="reasonDetail" rows="5" maxlength="2000"></textarea>
                        <label for="evidenceFile">Ảnh minh chứng</label>
                        <input id="evidenceFile" type="file" name="evidenceFile"
                               accept="image/jpeg,image/png,image/webp" required>
                        <p>JPEG, PNG hoặc WebP; tối đa 5 MB.</p>
                        <button class="examiner-btn examiner-btn--danger">Xác nhận đình chỉ</button>
                        <a href="${ctx}/examiner/action" class="examiner-btn examiner-btn--white">Hủy</a>
                    </form>
                </section>
            </c:when>
            <c:otherwise>
                <jsp:include page="/views/examiner/components/candidate-list.jsp">
                    <jsp:param name="title" value="Danh sách vi phạm"/>
                    <jsp:param name="actionViewViolation" value="true"/>
                    <jsp:param name="showStatus" value="true"/>
                </jsp:include>
            </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>
