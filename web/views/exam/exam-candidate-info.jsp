<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="candidateName" value="${not empty cand.fullName ? cand.fullName : 'THÍ SINH'}" />
<c:set var="sbdCode" value="${cand.candidateNumber}" />
<c:set var="dob" value="${cand.dobDisplay}" />
<c:set var="cccd" value="${cand.citizenId}" />
<c:set var="licenseClass" value="${cand.licenseClass}" />
<c:set var="examLocation" value="${not empty cand.examLocation ? cand.examLocation : 'Trung tâm sát hạch'}" />
<c:set var="timeLeft" value="08:22:38" />

<%-- Ảnh chân dung: dùng PhotoImageUrl nếu có (đường dẫn tuyệt đối hoặc tương đối), fallback ảnh mẫu. --%>
<c:choose>
    <c:when test="${not empty cand.photoUrl and (fn:startsWith(cand.photoUrl,'http') or fn:startsWith(cand.photoUrl,'/'))}">
        <c:set var="photoUrl" value="${cand.photoUrl}" />
    </c:when>
    <c:when test="${not empty cand.photoUrl}">
        <c:set var="photoUrl" value="${ctx}/${cand.photoUrl}" />
    </c:when>
    <c:otherwise>
        <c:set var="photoUrl" value="${ctx}/assets/imgs/exam-candidate-photo.png" />
    </c:otherwise>
</c:choose>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Xác nhận thông tin cá nhân | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@800&display=swap" rel="stylesheet">
        <link href="${ctx}/assets/css/exam-candidate-info.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="/views/layout/header-exam-time.jsp">
            <jsp:param name="timeLeft" value="${timeLeft}" />
        </jsp:include>

        <main class="candidate-info-page" data-node-id="113:705" data-name="Xác nhận thông tin cá nhân - Candidate (Light Mode)">
            <div class="candidate-info-layout" data-node-id="113:722" data-name="Container">
                <section class="profile-column" aria-label="Thông tin cá nhân thí sinh" data-node-id="113:723">
                    <article class="profile-card" data-node-id="113:724">
                        <div class="profile-head" data-node-id="113:725">
                            <div class="profile-photo-wrap" data-node-id="113:730">
                                <span class="profile-photo-glow" aria-hidden="true" data-node-id="113:732"></span>
                                <div class="profile-photo" data-node-id="113:733">
                                    <img src="${photoUrl}" alt="${candidateName}">
                                </div>
                                <img class="profile-verified" src="${ctx}/assets/imgs/exam-candidate-verified.svg" alt="" aria-hidden="true" data-node-id="113:734">
                            </div>
                            <h1 class="profile-name" data-node-id="113:726">${candidateName}</h1>
                            <p class="profile-sbd" data-node-id="113:729">SBD: ${sbdCode}</p>
                        </div>

                        <div class="profile-details" data-node-id="113:736">
                            <div class="info-row" data-node-id="113:737">
                                <p class="info-label">NGÀY SINH</p>
                                <p class="info-value info-value--mono">${dob}</p>
                            </div>
                            <div class="info-row" data-node-id="113:742">
                                <p class="info-label">SỐ CCCD</p>
                                <p class="info-value info-value--mono">${cccd}</p>
                            </div>
                            <div class="info-row" data-node-id="113:747">
                                <p class="info-label">HẠNG GPLX</p>
                                <span class="license-badge" data-node-id="113:750">${licenseClass}</span>
                            </div>
                            <div class="info-row" data-node-id="113:752">
                                <p class="info-label">ĐỊA ĐIỂM</p>
                                <p class="info-value">${examLocation}</p>
                            </div>
                        </div>
                    </article>
                </section>

                <section class="rules-card" aria-label="Nội quy và quy chế thi" data-node-id="113:761">
                    <div class="rules-header" data-node-id="113:762">
                        <h2 class="rules-title" data-node-id="113:763">
                            <img src="${ctx}/assets/imgs/exam-rules-icon.svg" alt="" aria-hidden="true">
                            <span>Nội Quy &amp; Quy Chế Thi</span>
                        </h2>
                    </div>

                    <div class="rules-list-wrap" data-node-id="113:767">
                        <div class="rules-list" data-node-id="113:768">
                            <article class="rule-item" data-node-id="113:769">
                                <span class="rule-number">01</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Tính Trung Thực</h3>
                                    <p class="rule-text">Thí sinh không được sử dụng tài liệu, thiết bị điện tử hoặc bất kỳ hình thức gian lận nào trong suốt quá trình làm bài.</p>
                                </div>
                            </article>

                            <article class="rule-item" data-node-id="113:777">
                                <span class="rule-number">02</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Thời Gian Làm Bài</h3>
                                    <p class="rule-text">Thời gian và số câu hỏi được xác định theo hạng GPLX. Hệ thống sẽ tự động nộp bài khi hết giờ.</p>
                                </div>
                            </article>

                            <article class="rule-item" data-node-id="113:793">
                                <span class="rule-number">03</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Quy Định Chụp Ảnh</h3>
                                    <p class="rule-text">Camera sẽ chụp ảnh thí sinh để đảm bảo không xảy ra vi phạm.</p>
                                </div>
                            </article>

                            <article class="rule-item" data-node-id="113:801">
                                <span class="rule-number">04</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Điều Kiện Đạt</h3>
                                    <p class="rule-text">Thí sinh phải đạt đủ số câu tối thiểu theo hạng <strong>và không được sai câu điểm liệt</strong>. Sai 1 câu điểm liệt là trượt.</p>
                                </div>
                            </article>
                        </div>
                    </div>

                    <c:if test="${param.err eq '1'}">
                        <p style="margin:0 0 10px; color:#b91c1c; font-weight:600; font-size:0.88rem;">
                            Bạn cần tích xác nhận đã đọc nội quy trước khi tiếp tục.
                        </p>
                    </c:if>
                    <c:if test="${not empty error}">
                        <p style="margin:0 0 10px; color:#b91c1c; font-weight:600; font-size:0.88rem;">${error}</p>
                    </c:if>

                    <form class="rules-action" action="${ctx}/exam/face" method="post" data-node-id="113:809">
                        <label class="confirm-label" data-node-id="113:810">
                            <input class="confirm-checkbox" type="checkbox" name="confirmed" value="true" data-node-id="113:812">
                            <span>Tôi đã đọc kỹ, hiểu rõ các nội quy phòng thi và xác nhận thông tin cá nhân của mình là chính xác.</span>
                        </label>

                        <button type="submit" class="continue-button" data-node-id="113:815">
                            <span>Tiếp Tục</span>
                            <img src="${ctx}/assets/imgs/exam-continue-arrow.svg" alt="" aria-hidden="true">
                        </button>
                    </form>
                </section>
            </div>
        </main>

        <jsp:include page="/views/layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="" />
            <jsp:param name="noticeText" value="" />
        </jsp:include>
    </body>
</html>
