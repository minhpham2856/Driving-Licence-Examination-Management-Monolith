<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Táº¡o tÃ i khoáº£n há»c viÃªn má»›i - LÃ¡i Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="tao-tai-khoan" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chá»§</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${pageContext.request.contextPath}/manager/dashboard">Dashboard quáº£n lÃ½</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Táº¡o tÃ i khoáº£n cho thÃ­ sinh ná»™p há»“ sÆ¡</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Táº¡o TÃ i Khoáº£n Cho ThÃ­ Sinh Ná»™p Há»“ SÆ¡</h1>
                <p class="page-subtitle">Nháº­p thÃ´ng tin há»“ sÆ¡ thÃ­ sinh.</p>
            </div>
            
            <div class="page-actions">
                <a href="${pageContext.request.contextPath}/manager/registrants" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Quay láº¡i danh sÃ¡ch há»c viÃªn
                </a>
            </div>
        </header>

        <div class="report-grid" style="grid-template-columns: 1.5fr 1fr; gap: 1.5rem; margin-top: 1.5rem;">
            
            <div class="report-pane" style="padding: 2rem;">
                <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.5rem; padding-bottom: 0;">
                    <h2 class="grading-pane__title" style="font-size: 1.15rem; display: flex; align-items: center; gap: 8px;">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                            <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        ThÃ´ng tin há»“ sÆ¡ Ä‘Äƒng kÃ½ tÃ i khoáº£n
                    </h2>
                </div>

                <c:if test="${not empty createUserError}">
                    <div role="alert" style="margin-bottom: 1.25rem; padding: 0.9rem 1rem; border: 1px solid #fecaca; border-radius: 8px; background: #fef2f2; color: #b91c1c; font-size: 0.9rem; font-weight: 600;">
                        <c:out value="${createUserError}" />
                    </div>
                </c:if>

                <c:if test="${not empty createUserSuccess}">
                    <div role="status" style="margin-bottom: 1.25rem; padding: 0.9rem 1rem; border: 1px solid #a7f3d0; border-radius: 8px; background: #ecfdf5; color: #047857; font-size: 0.9rem; font-weight: 600;">
                        <c:out value="${createUserSuccess}" />
                        <c:if test="${not empty createdUsername}">
                            <div style="margin-top: 0.65rem; color: #065f46; line-height: 1.6;">
                                TÃªn Ä‘Äƒng nháº­p: <strong><c:out value="${createdUsername}" /></strong><br>
                                Máº­t kháº©u: <strong><c:out value="${createdPassword}" /></strong>
                            </div>
                        </c:if>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/manager/create-user" method="POST"
                      enctype="multipart/form-data"
                      style="display: flex; flex-direction: column; gap: 1.25rem;">
                    
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <div class="input-group">
                            <label for="fullName" class="input-label">Há» vÃ  tÃªn há»c viÃªn <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="fullName" name="fullName" class="input-field" placeholder="VÃ­ dá»¥: Nguyá»…n VÄƒn A" value="${fn:escapeXml(param.fullName)}" required minlength="3" maxlength="50">
                        </div>

                        <div class="input-group">
                            <label for="cccd" class="input-label">Sá»‘ CÄƒn cÆ°á»›c cÃ´ng dÃ¢n (12 chá»¯ sá»‘) <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="cccd" name="cccd" class="input-field" placeholder="VÃ­ dá»¥: 030098001234" value="${fn:escapeXml(param.cccd)}" required pattern="[0-9]{12}" title="Vui lÃ²ng nháº­p Ä‘Ãºng 12 chá»¯ sá»‘ CCCD há»£p lá»‡">
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <div class="input-group">
                            <label for="phone" class="input-label">Sá»‘ Ä‘iá»‡n thoáº¡i liÃªn há»‡ <span style="color: #ef4444;">*</span></label>
                            <input type="tel" id="phone" name="phone" class="input-field" placeholder="VÃ­ dá»¥: 0987654321" value="${fn:escapeXml(param.phone)}" required pattern="0[0-9]{9}" title="Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i báº¯t Ä‘áº§u báº±ng sá»‘ 0 vÃ  bao gá»“m Ä‘Ãºng 10 chá»¯ sá»‘">
                        </div>

                        <div class="input-group">
                            <label for="email" class="input-label">Äá»‹a chá»‰ Email <span style="color: #ef4444;">*</span></label>
                            <input type="email" id="email" name="email" class="input-field" placeholder="VÃ­ dá»¥: hocvien@gmail.com" value="${fn:escapeXml(param.email)}" required>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                        <div class="input-group">
                            <label for="dob" class="input-label">NgÃ y sinh <span style="color: #ef4444;">*</span></label>
                            <input type="date" id="dob" name="dob" class="input-field" value="${fn:escapeXml(param.dob)}" max="<%= java.time.LocalDate.now() %>" required>
                        </div>

                        <div class="input-group">
                            <label for="gender" class="input-label">Giá»›i tÃ­nh <span style="color: #ef4444;">*</span></label>
                            <select id="gender" name="gender" class="input-field" required>
                                <option value="">Chá»n giá»›i tÃ­nh</option>
                                <option value="male" ${param.gender eq 'male' ? 'selected' : ''}>Nam</option>
                                <option value="female" ${param.gender eq 'female' ? 'selected' : ''}>Ná»¯</option>
                            </select>
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="address" class="input-label">Äá»‹a chá»‰ quÃª quÃ¡n / NÆ¡i thÆ°á»ng trÃº <span style="color: #ef4444;">*</span></label>
                        <input type="text" id="address" name="address" class="input-field" placeholder="VÃ­ dá»¥: Thanh XuÃ¢n, HÃ  Ná»™i" value="${fn:escapeXml(param.address)}" required minlength="5" maxlength="150">
                    </div>

                    <section style="border: 1px solid #bfdbfe; border-radius: 12px; padding: 1.25rem; background: #f8fbff;">
                        <div style="display: flex; justify-content: space-between; gap: 1rem; align-items: flex-start; margin-bottom: 1rem;">
                            <div>
                                <h3 style="margin: 0 0 4px; color: #0f172a; font-size: 1rem;">Há»“ sÆ¡ Managing Staff tiáº¿p nháº­n</h3>
                                <p style="margin: 0; color: #64748b; font-size: 0.82rem; line-height: 1.5;">
                                    Táº£i Ä‘á»§ giáº¥y tá» Ä‘Ã£ Ä‘á»‘i chiáº¿u táº¡i quáº§y. TÃ i khoáº£n táº¡o thÃ nh cÃ´ng sáº½ cÃ³ há»“ sÆ¡ Ä‘Æ°á»£c xÃ¡c minh.
                                </p>
                            </div>
                            <span style="white-space: nowrap; padding: 5px 9px; border-radius: 999px; background: #dbeafe; color: #1d4ed8; font-size: 0.72rem; font-weight: 700;">TIáº¾P NHáº¬N TRá»°C TIáº¾P</span>
                        </div>

                        <div class="input-group" style="margin-bottom: 1rem;">
                            <label for="licenseClass" class="input-label">Háº¡ng GPLX Ä‘Äƒng kÃ½ <span style="color: #ef4444;">*</span></label>
                            <select id="licenseClass" name="licenseClass" class="input-field" required>
                                <option value="">Chá»n háº¡ng GPLX</option>
                                <option value="A1" ${param.licenseClass eq 'A1' ? 'selected' : ''}>Háº¡ng A1</option>
                                <option value="A" ${param.licenseClass eq 'A' or param.licenseClass eq 'A2' ? 'selected' : ''}>Háº¡ng A2</option>
                                <option value="B1" ${param.licenseClass eq 'B1' ? 'selected' : ''}>Háº¡ng B1</option>
                                <option value="B" ${param.licenseClass eq 'B' or param.licenseClass eq 'B2' ? 'selected' : ''}>Háº¡ng B2</option>
                                <option value="C" ${param.licenseClass eq 'C' ? 'selected' : ''}>Háº¡ng C</option>
                            </select>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="input-group">
                                <label for="portrait" class="input-label">áº¢nh chÃ¢n dung 3x4 <span style="color: #ef4444;">*</span></label>
                                <input type="file" id="portrait" name="portrait" class="input-field" accept=".jpg,.jpeg,.png,.pdf,image/*,application/pdf" required>
                            </div>
                            <div class="input-group">
                                <label for="healthCertificate" class="input-label">Giáº¥y khÃ¡m sá»©c khá»e <span style="color: #ef4444;">*</span></label>
                                <input type="file" id="healthCertificate" name="healthCertificate" class="input-field" accept=".jpg,.jpeg,.png,.pdf,image/*,application/pdf" required>
                            </div>
                            <div class="input-group">
                                <label for="idFront" class="input-label">CCCD máº·t trÆ°á»›c <span style="color: #ef4444;">*</span></label>
                                <input type="file" id="idFront" name="idFront" class="input-field" accept=".jpg,.jpeg,.png,.pdf,image/*,application/pdf" required>
                            </div>
                            <div class="input-group">
                                <label for="idBack" class="input-label">CCCD máº·t sau <span style="color: #ef4444;">*</span></label>
                                <input type="file" id="idBack" name="idBack" class="input-field" accept=".jpg,.jpeg,.png,.pdf,image/*,application/pdf" required>
                            </div>
                            <div class="input-group">
                                <label for="graduationCertificate" class="input-label">
                                    Giấy tốt nghiệp / chứng chỉ đào tạo
                                    <span id="graduationRequiredMark" style="color: #ef4444;">*</span>
                                </label>
                                <input type="file" id="graduationCertificate" name="graduationCertificate" class="input-field" accept=".jpg,.jpeg,.png,.pdf,image/*,application/pdf">
                                <small id="graduationHint" style="display:block;margin-top:4px;color:#64748b;">
                                    Bắt buộc với hạng ô tô; không bắt buộc với A1/A2.
                                </small>
                            </div>
                        </div>
                        <p style="margin: 0.85rem 0 0; color: #64748b; font-size: 0.78rem;">
                            Cháº¥p nháº­n JPG, PNG hoáº·c PDF; tá»‘i Ä‘a 5 MB cho má»—i tá»‡p.
                        </p>
                    </section>

                    <div class="input-group" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem;">
                        <span style="font-size: 0.85rem; font-weight: 700; color: #475569; display: block; margin-bottom: 4px;">ThÃ´ng tin tÃ i khoáº£n Ä‘Äƒng nháº­p máº·c Ä‘á»‹nh:</span>
                        <span style="font-size: 0.8rem; color: #64748b; display: block; line-height: 1.4;">
                            â€¢ TÃªn Ä‘Äƒng nháº­p: <strong>Tá»± Ä‘á»™ng sinh tá»« há» tÃªn vÃ  má»™t dÃ£y sá»‘ ngáº«u nhiÃªn</strong>.<br>
                            â€¢ Máº­t kháº©u: <strong>Tá»± Ä‘á»™ng táº¡o ngáº«u nhiÃªn</strong> vÃ  gá»­i Ä‘áº¿n email há»c viÃªn.
                        </span>
                    </div>

                    <hr style="border: 0; border-top: 1px solid #f1f5f9; margin: 8px 0;">

                    <div style="display: flex; gap: 10px; justify-content: flex-end;">
                        <a href="${pageContext.request.contextPath}/manager/registrants" class="btn-reset" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; height: 42px; width: 120px; font-size: 0.9rem; font-weight: 600; color: #475569; background-color: #ffffff;">Há»§y bá»</a>
                        <button type="submit" class="btn-filter" style="height: 42px; min-width: 230px; border-radius: 8px; background-color: #0052cc; border-color: #0052cc; justify-content: center; font-weight: 700;">Táº¡o tÃ i khoáº£n &amp; hoÃ n táº¥t há»“ sÆ¡</button>
                    </div>
                </form>
            </div>

            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                
                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px; color: #003d9b;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Äiá»u kiá»‡n tuá»•i Ä‘Äƒng kÃ½ GPLX
                        </h2>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 1rem; font-size: 0.85rem; line-height: 1.45; color: #475569;">
                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">â€¢ Äá»‘i vá»›i háº¡ng A1, A2:</strong>
                            Äá»™ tuá»•i tá»‘i thiá»ƒu Ä‘Äƒng kÃ½ sÃ¡t háº¡ch lÃ  <strong>Ä‘á»§ 18 tuá»•i</strong> tÃ­nh Ä‘áº¿n ngÃ y thi.
                        </div>

                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">â€¢ Äá»‘i vá»›i háº¡ng B1, B2:</strong>
                            Äá»™ tuá»•i tá»‘i thiá»ƒu Ä‘Äƒng kÃ½ sÃ¡t háº¡ch lÃ  <strong>Ä‘á»§ 18 tuá»•i</strong> tÃ­nh Ä‘áº¿n ngÃ y thi.
                        </div>

                        <div>
                            <strong style="color: #0f172a; display: block; margin-bottom: 2px;">â€¢ Äá»‘i vá»›i háº¡ng C:</strong>
                            Äá»™ tuá»•i tá»‘i thiá»ƒu Ä‘Äƒng kÃ½ sÃ¡t háº¡ch lÃ  <strong>Ä‘á»§ 21 tuá»•i</strong> tÃ­nh Ä‘áº¿n ngÃ y thi.
                        </div>

                        <div style="background-color: rgba(239, 68, 68, 0.05); border: 1px solid rgba(239, 68, 68, 0.15); border-radius: 6px; padding: 0.75rem; color: #b91c1c; font-weight: 500;">
                            LÆ°u Ã½: Há»‡ thá»‘ng backend sáº½ tá»± Ä‘á»™ng tÃ­nh tuá»•i dá»±a trÃªn NgÃ y sinh há»c viÃªn nháº­p vÃ o Ä‘á»ƒ Ä‘á»‘i chiáº¿u vá»›i Háº¡ng GPLX Ä‘Äƒng kÃ½ trÆ°á»›c khi táº¡o tÃ i khoáº£n.
                        </div>
                    </div>
                </div>

                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                                <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Quy Ä‘á»‹nh vá» Giáº¥y tá» Ä‘Ã­nh kÃ¨m
                        </h2>
                    </div>

                    <p style="font-size: 0.85rem; color: #475569; line-height: 1.5; margin-bottom: 0.75rem;">
                        Managing Staff Ä‘á»‘i chiáº¿u báº£n giáº¥y vÃ  táº£i Ä‘á»§ cÃ¡c tÃ i liá»‡u dÆ°á»›i Ä‘Ã¢y ngay khi táº¡o tÃ i khoáº£n:
                    </p>
                    
                    <div style="display: flex; flex-direction: column; gap: 0.65rem; font-size: 0.82rem; color: #64748b;">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">âœ“</span>
                            <span>áº¢nh tháº» chÃ¢n dung 3x4 (Chá»¥p trÃªn ná»n xanh)</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">âœ“</span>
                            <span>áº¢nh chá»¥p CÄƒn cÆ°á»›c cÃ´ng dÃ¢n (Máº·t trÆ°á»›c + Máº·t sau)</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="color: #10b981; font-weight: 900;">âœ“</span>
                            <span>Giáº¥y khÃ¡m sá»©c khá»e lÃ¡i xe cÃ²n thá»i háº¡n dÆ°á»›i 6 thÃ¡ng</span>
                        </div>
                        <div style="margin-top: 0.35rem; padding: 0.75rem; border-radius: 7px; background: #ecfdf5; color: #047857; line-height: 1.45; font-weight: 600;">
                            Há»“ sÆ¡ sáº½ Ä‘Æ°á»£c Ä‘Ã¡nh dáº¥u Ä‘Ã£ xÃ¡c minh. ThÃ­ sinh khÃ´ng pháº£i táº£i láº¡i vÃ  cÃ³ thá»ƒ chuyá»ƒn sang chá»n phiÃªn thi phÃ¹ há»£p.
                        </div>
                    </div>
                </div>

            </div>

        </div>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script>
    (function () {
        const licenseSelect = document.getElementById('licenseClass');
        const graduationInput = document.getElementById('graduationCertificate');
        const requiredMark = document.getElementById('graduationRequiredMark');
        const hint = document.getElementById('graduationHint');
        function updateGraduationRequirement() {
            if (!licenseSelect || !graduationInput) return;
            const value = (licenseSelect.value || '').toUpperCase();
            const required = value !== '' && value !== 'A1' && value !== 'A';
            graduationInput.required = required;
            if (requiredMark) requiredMark.style.display = required ? 'inline' : 'none';
            if (hint) {
                hint.textContent = required
                    ? 'Bắt buộc với hồ sơ hạng ô tô.'
                    : 'Không bắt buộc với A1/A2; có thể tải lên nếu trung tâm yêu cầu.';
            }
        }
        if (licenseSelect) {
            licenseSelect.addEventListener('change', updateGraduationRequirement);
            updateGraduationRequirement();
        }
    })();
</script>

</body>
</html>

