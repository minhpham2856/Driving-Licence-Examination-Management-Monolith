<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="candidateName" value="${not empty param.candidateName ? param.candidateName : 'NGUYỄN MINH HOÀNG'}" />
<c:set var="sbdCode" value="${not empty param.sbd ? param.sbd : '12'}" />
<c:set var="dob" value="${not empty param.dob ? param.dob : '15 / 08 / 1995'}" />
<c:set var="cccd" value="${not empty param.cccd ? param.cccd : '038095XXXXXX'}" />
<c:set var="licenseClass" value="${not empty param.licenseClass ? param.licenseClass : 'A1'}" />
<c:set var="examLocation" value="${not empty param.examLocation ? param.examLocation : 'TT Sát hạch Lái Vui, TP.HCM'}" />
<c:set var="timeLeft" value="${not empty param.timeLeft ? param.timeLeft : '08:22:38'}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Xác nhận thông tin cá nhân | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@500&family=Roboto:wght@800&display=swap" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/exam-candidate-info.css" rel="stylesheet">
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
                                    <img src="${pageContext.request.contextPath}/assets/imgs/exam-candidate-photo.png" alt="${candidateName}">
                                </div>
                                <img class="profile-verified" src="${pageContext.request.contextPath}/assets/imgs/exam-candidate-verified.svg" alt="" aria-hidden="true" data-node-id="113:734">
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
                            <img src="${pageContext.request.contextPath}/assets/imgs/exam-rules-icon.svg" alt="" aria-hidden="true">
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
                                    <p class="rule-text">Bạn có tổng cộng 20 phút để hoàn thành bộ đề thi gồm 35 câu hỏi. Hệ thống sẽ tự động nộp bài khi hết giờ.</p>
                                </div>
                            </article>

                            <article class="rule-item" data-node-id="113:793">
                                <span class="rule-number">04</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Quy Định Chụp Ảnh</h3>
                                    <p class="rule-text">Camera sẽ chụp ảnh thí sinh để đảm bảo không xảy ra vi phạm.</p>
                                </div>
                            </article>

                            <article class="rule-item" data-node-id="113:801">
                                <span class="rule-number">05</span>
                                <div class="rule-copy">
                                    <h3 class="rule-title">Điều Kiện Đạt</h3>
                                    <p class="rule-text">Thí sinh cần đạt tối thiểu 32/35 câu (đối với hạng B2) và không sai câu điểm liệt.</p>
                                </div>
                            </article>
                        </div>
                    </div>

                    <form class="rules-action" action="${pageContext.request.contextPath}/exam/face" method="post" data-node-id="113:809">
                        <label class="confirm-label" data-node-id="113:810">
                            <input class="confirm-checkbox" type="checkbox" name="confirmed" value="true" data-node-id="113:812">
                            <span>Tôi đã đọc kỹ, hiểu rõ các nội quy phòng thi và xác nhận thông tin cá nhân của mình là chính xác.</span>
                        </label>

                        <button type="submit" class="continue-button" data-node-id="113:815">
                            <span>Tiếp Tục</span>
                            <img src="${pageContext.request.contextPath}/assets/imgs/exam-continue-arrow.svg" alt="" aria-hidden="true">
                        </button>
                    </form>
                </section>
            </div>
        </main>

        <jsp:include page="/views/layout/footer-exam.jsp">
            <jsp:param name="noticeTitle" value="" />
            <jsp:param name="noticeText" value="" />
</body>
</html>
