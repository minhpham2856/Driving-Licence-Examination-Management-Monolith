<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="candidateName" value="${not empty cand.fullName ? cand.fullName : 'THÍ SINH'}" />

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
    <title>Xác nhận thông tin | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@600&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #eef2f7; color: #0f172a; }
        .info-page { max-width: 1100px; margin: 1.5rem auto; padding: 6rem 1rem 5rem; }
        .info-layout { display: grid; grid-template-columns: 360px 1fr; gap: 1.25rem; align-items: start; }
        .card { background: #fff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 1.5rem; }
        .profile-head { text-align: center; padding-bottom: 1.25rem; border-bottom: 1px solid #eef2f7; margin-bottom: 1.25rem; }
        .photo-wrap { position: relative; width: 140px; height: 140px; margin: 0 auto 1rem; }
        .photo { width: 140px; height: 140px; border-radius: 50%; overflow: hidden; border: 4px solid #eef4ff; background: #f8fafc; }
        .photo img { width: 100%; height: 100%; object-fit: cover; }
        .verified { position: absolute; right: 6px; bottom: 6px; width: 34px; height: 34px; }
        .profile-name { margin: .25rem 0; font-size: 1.3rem; font-weight: 800; }
        .profile-sbd { margin: 0; color: #64748b; font-weight: 600; font-family: 'JetBrains Mono', monospace; }
        .info-row { display: flex; justify-content: space-between; align-items: center; padding: .7rem 0; border-bottom: 1px dashed #eef2f7; }
        .info-row:last-child { border-bottom: none; }
        .info-label { margin: 0; font-size: .72rem; font-weight: 700; letter-spacing: .5px; color: #94a3b8; }
        .info-value { margin: 0; font-weight: 700; text-align: right; }
        .info-value--mono { font-family: 'JetBrains Mono', monospace; }
        .license-badge { padding: 4px 14px; border-radius: 9999px; background: #eef4ff; color: #0052cc; font-weight: 800; }
        .rules-title { display: flex; align-items: center; gap: 10px; margin: 0 0 1.25rem; font-size: 1.2rem; font-weight: 800; }
        .rules-title img { width: 26px; height: 26px; }
        .rule-item { display: flex; gap: 14px; padding: .85rem 0; border-bottom: 1px solid #f1f5f9; }
        .rule-number { flex-shrink: 0; width: 36px; height: 36px; border-radius: 10px; background: #eef4ff; color: #0052cc; font-weight: 800; display: flex; align-items: center; justify-content: center; }
        .rule-title { margin: 0 0 4px; font-size: .95rem; font-weight: 700; }
        .rule-text { margin: 0; font-size: .85rem; color: #64748b; line-height: 1.5; }
        .confirm-label { display: flex; gap: 12px; align-items: flex-start; margin: 1.25rem 0; font-size: .88rem; color: #334155; cursor: pointer; }
        .confirm-checkbox { width: 20px; height: 20px; margin-top: 2px; accent-color: #0052cc; flex-shrink: 0; }
        .continue-button { width: 100%; height: 54px; display: inline-flex; align-items: center; justify-content: center; gap: 10px; border: none; border-radius: 14px; background: #0052cc; color: #fff; font-size: 1rem; font-weight: 800; cursor: pointer; }
        .continue-button:hover { background: #0041a3; }
        .continue-button img { width: 18px; height: 18px; }
        .err-text { margin: 0 0 10px; color: #b91c1c; font-weight: 600; font-size: .88rem; }
        @media (max-width: 860px){ .info-layout { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
    <jsp:include page="/views/layout/header-exam-time.jsp">
        <jsp:param name="timeLeft" value="08:22:38" />
    </jsp:include>

    <main class="info-page">
        <div class="info-layout">
            <section class="card">
                <div class="profile-head">
                    <div class="photo-wrap">
                        <div class="photo"><img src="${photoUrl}" alt="${candidateName}"></div>
                        <img class="verified" src="${ctx}/assets/imgs/exam-candidate-verified.svg" alt="" aria-hidden="true">
                    </div>
                    <h1 class="profile-name">${candidateName}</h1>
                    <p class="profile-sbd">SBD: ${cand.candidateNumber}</p>
                </div>
                <div>
                    <div class="info-row"><p class="info-label">NGÀY SINH</p><p class="info-value info-value--mono">${cand.dobDisplay}</p></div>
                    <div class="info-row"><p class="info-label">SỐ CCCD</p><p class="info-value info-value--mono">${cand.citizenId}</p></div>
                    <div class="info-row"><p class="info-label">HẠNG GPLX</p><span class="license-badge">${cand.licenseClass}</span></div>
                    <div class="info-row"><p class="info-label">ĐỊA ĐIỂM</p><p class="info-value">${not empty cand.examLocation ? cand.examLocation : 'Trung tâm sát hạch'}</p></div>
                </div>
            </section>

            <section class="card">
                <h2 class="rules-title">
                    <img src="${ctx}/assets/imgs/exam-rules-icon.svg" alt="" aria-hidden="true">
                    <span>Nội Quy &amp; Quy Chế Thi</span>
                </h2>
                <div>
                    <div class="rule-item"><span class="rule-number">01</span><div><h3 class="rule-title">Tính Trung Thực</h3><p class="rule-text">Thí sinh không được sử dụng tài liệu, thiết bị điện tử hoặc bất kỳ hình thức gian lận nào trong quá trình làm bài.</p></div></div>
                    <div class="rule-item"><span class="rule-number">02</span><div><h3 class="rule-title">Thời Gian Làm Bài</h3><p class="rule-text">Thời gian và số câu hỏi xác định theo hạng GPLX. Hệ thống tự động nộp bài khi hết giờ.</p></div></div>
                    <div class="rule-item"><span class="rule-number">03</span><div><h3 class="rule-title">Quy Định Chụp Ảnh</h3><p class="rule-text">Camera sẽ chụp ảnh thí sinh để đảm bảo không xảy ra vi phạm.</p></div></div>
                    <div class="rule-item"><span class="rule-number">04</span><div><h3 class="rule-title">Điều Kiện Đạt</h3><p class="rule-text">Đạt đủ số câu tối thiểu theo hạng <strong>và không sai câu điểm liệt</strong>. Sai 1 câu điểm liệt là trượt.</p></div></div>
                </div>

                <c:if test="${param.err eq '1'}"><p class="err-text">Bạn cần tích xác nhận đã đọc nội quy trước khi tiếp tục.</p></c:if>
                <c:if test="${not empty error}"><p class="err-text">${error}</p></c:if>

                <form action="${ctx}/exam/face" method="post">
                    <label class="confirm-label">
                        <input class="confirm-checkbox" type="checkbox" name="confirmed" value="true">
                        <span>Tôi đã đọc kỹ, hiểu rõ nội quy phòng thi và xác nhận thông tin cá nhân của mình là chính xác.</span>
                    </label>
                    <button type="submit" class="continue-button">
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
