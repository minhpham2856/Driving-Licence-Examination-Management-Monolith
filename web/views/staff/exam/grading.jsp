<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="hasCandidate" value="${not empty currentCandidate}" />
<c:set var="hasSteps" value="${not empty examSteps}" />
<c:set var="isStarted" value="${examStarted}" />
<c:set var="liveScore" value="${empty currentScore ? 100 : currentScore}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Chấm điểm sát hạch sa hình - Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    </head>
    <body class="has-side-nav-bar">

        <jsp:include page="/views/layout/sidebar-examstaff.jsp">
            <jsp:param name="activeSidebar" value="cham-diem" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content">

                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current">Quản lý thi</span>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Chấm điểm sát hạch</span>
                </nav>

                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Chấm điểm sát hạch</h1>
                        <p class="page-subtitle">Bảng điều hành giám sát và trừ điểm thực hành sa hình thời gian thực của Giám thị.</p>
                    </div>

                    <div class="page-actions" style="display: flex; gap: 10px;">
                        <c:choose>
                            <c:when test="${not isStarted}">
                                <a href="${pageContext.request.contextPath}/examiner/grading?action=start&sbd=${currentCandidate.sbd}" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #10b981; border-color: #10b981; <c:if test='${not hasSteps}'>pointer-events: none; opacity: 0.5;</c:if>">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 3l14 9-14 9V3z" fill="currentColor"/>
                                        </svg>
                                        Bắt đầu chấm
                                    </a>
                            </c:when>
                            <c:otherwise>
                                <button class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; background-color: #10b981; border-color: #10b981; opacity: 0.5; cursor: not-allowed;" disabled>
                                    Đang chạy...
                                </button>
                            </c:otherwise>
                        </c:choose>

                        <a href="${pageContext.request.contextPath}/examiner/grading?action=submit&sbd=${currentCandidate.sbd}" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #0052cc; border-color: #0052cc; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2"/>
                                <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" stroke-width="2"/>
                                </svg>
                                Khóa & Nộp điểm
                            </a>

                            <a href="${pageContext.request.contextPath}/examiner/grading?action=terminate&sbd=${currentCandidate.sbd}" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ef4444; border-color: #ef4444; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                                </svg>
                                Đình chỉ thi
                            </a>
                        </div>
                    </header>

                    <div class="grading-grid">
                        <div class="grading-pane">
                            <div class="grading-pane__header" style="margin-bottom: 1.25rem;">
                                <h2 class="grading-pane__title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                    <path d="M9 11l3 3L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    Bảng chấm điểm bài thi sa hình
                                </h2>
                            </div>

                        <c:choose>
                            <c:when test="${hasSteps}">
                                <div class="step-controller-bar" style="display: flex; justify-content: space-between; align-items: center; background-color: rgba(0, 82, 204, 0.04); border: 1px solid rgba(0, 82, 204, 0.1); padding: 0.75rem 1rem; border-radius: 10px; margin-bottom: 1.5rem;">
                                    <a href="${pageContext.request.contextPath}/examiner/grading?action=prev&sbd=${currentCandidate.sbd}" class="btn-deduct" style="color: #0052cc; border-color: #0052cc; margin: 0; height: 36px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted or currentActiveStepId eq 1}'>pointer-events: none; opacity: 0.5;</c:if>">
                                            &larr; Bài trước
                                        </a>
                                        <div style="text-align: center;">
                                            <span style="font-size: 0.72rem; font-weight: 700; color: #64748b; text-transform: uppercase; display: block; letter-spacing: 0.05em; margin-bottom: 2px;">Bài thi hiện tại</span>
                                            <span style="font-size: 0.92rem; font-weight: 800; color: #003d9b;">
                                            <c:choose>
                                                <c:when test="${isStarted}">BÀI ${currentActiveStepId}: ${activeStepName}</c:when>
                                                <c:otherwise>CHƯA BẮT ĐẦU</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/examiner/grading?action=next&sbd=${currentCandidate.sbd}" class="btn-filter" style="margin: 0; height: 36px; padding: 0 1rem; font-size: 0.85rem; background-color: #0052cc; border-color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">
                                            Bài tiếp theo &rarr;
                                        </a>
                                    </div>

                                    <div class="scoring-list">
                                    <c:forEach var="item" items="${examSteps}">
                                        <c:set var="isActive" value="${isStarted and item.stepId eq currentActiveStepId}" />
                                        <c:set var="stepPenalty" value="${stepPenaltiesMap[item.stepId]}" />

                                        <div class="step-card ${isActive ? 'active' : ''} ${isStarted and item.stepId < currentActiveStepId ? 'completed' : ''}">
                                            <div class="step-info">
                                                <div class="step-header-row">
                                                    <span class="step-num">BÀI ${item.stepId}</span>
                                                    <span class="step-title">${item.stepName}</span>
                                                </div>
                                                <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">
                                                    <span class="step-desc-muted">Lỗi chạm vạch/tắt máy: -${item.maxPoints}đ</span>
                                                    <c:if test="${not empty stepPenalty and stepPenalty > 0}">
                                                        <span class="step-points-deducted">-${stepPenalty}đ</span>
                                                    </c:if>
                                                </div>
                                            </div>

                                            <div class="step-actions">
                                                <a href="${pageContext.request.contextPath}/examiner/grading?action=deduct&stepId=${item.stepId}&points=5&sbd=${currentCandidate.sbd}" class="btn-deduct" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">-5đ</a>
                                                <a href="${pageContext.request.contextPath}/examiner/grading?action=deduct&stepId=${item.stepId}&points=10&sbd=${currentCandidate.sbd}" class="btn-deduct" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">-10đ</a>
                                                <a href="${pageContext.request.contextPath}/examiner/grading?action=disqualify&stepId=${item.stepId}&sbd=${currentCandidate.sbd}" class="btn-disqualify" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">LOẠI</a>
                                                <a href="${pageContext.request.contextPath}/examiner/grading?action=reset&stepId=${item.stepId}&sbd=${currentCandidate.sbd}" class="btn-deduct" style="color: #64748b; border-color: #cbd5e1; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; <c:if test='${not isStarted}'>pointer-events: none; opacity: 0.5;</c:if>">
                                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                                        <path d="M3 3v5h5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                                        </svg>
                                                    </a>
                                                </div>
                                            </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="text-align: center; color: #64748b; padding: 4rem 2rem; font-style: italic;">
                                    <img src="${pageContext.request.contextPath}/assets/imgs/empty-grading.svg" alt="Chờ chấm điểm" style="width: 48px; height: 48px; margin-bottom: 1rem;">
                                    <p style="font-size: 0.95rem; font-weight: 500;">Chưa có danh sách bài thi sa hình.</p>
                                    <p style="font-size: 0.8rem; color: #94a3b8; margin-top: 4px;">Vui lòng kết nối cơ sở dữ liệu và truyền danh sách bài thi từ Controller.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="grading-sidebar-cards">
                        <div class="grading-pane" style="display: flex; flex-direction: column; align-items: center;">
                            <div class="grading-pane__header" style="width: 100%; justify-content: center; margin-bottom: 1rem;">
                                <h2 class="grading-pane__title" style="color: #0052cc;">KẾT QUẢ ĐANG THI</h2>
                            </div>

                            <div class="live-score-display ${liveScore >= 80 and not isDisqualified ? 'passed' : 'failed'}" style="width: 100%;">
                                <span class="live-score-display__value">${isDisqualified ? 0 : liveScore}</span>
                                <span class="live-score-display__badge">
                                    <c:choose>
                                        <c:when test="${isDisqualified}">BỊ LOẠI</c:when>
                                        <c:when test="${liveScore >= 80}">ĐANG ĐẠT</c:when>
                                        <c:otherwise>CHƯA ĐẠT</c:otherwise>
                                    </c:choose>
                                </span>

                                <div class="live-timer-container">
                                    <span class="live-timer-label">Thời gian thi sa hình:</span>
                                    <span class="live-timer-value">${empty examTimer ? '20:00' : examTimer}</span>
                                </div>
                            </div>
                        </div>

                        <div class="grading-pane">
                            <div class="grading-pane__header" style="margin-bottom: 1.25rem;">
                                <h2 class="grading-pane__title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                                    </svg>
                                    Thông tin thí sinh thi
                                </h2>
                            </div>

                            <div class="call-grid" style="grid-template-columns: 110px 1fr; gap: 1.25rem;">
                                <div class="candidate-photo-frame" style="width: 110px; height: 140px; border-radius: 6px;">
                                    <c:choose>
                                        <c:when test="${hasCandidate and not empty currentCandidate.photoUrl}">
                                            <img src="${pageContext.request.contextPath}${currentCandidate.photoUrl}" alt="${currentCandidate.name}">
                                        </c:when>
                                        <c:otherwise>
                                            <div style="display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background-color: #cbd5e1; color: #64748b;">
                                                <img src="${pageContext.request.contextPath}/assets/imgs/avatar-placeholder.svg" alt="Ảnh chân dung trống" style="width: 36px; height: 36px; opacity: 0.4;">
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="candidate-details-list" style="grid-template-columns: 1fr; gap: 0.75rem;">
                                    <div class="candidate-detail-item">
                                        <span class="candidate-detail-label">Số báo danh</span>
                                        <span class="candidate-detail-value" style="color: #0052cc; font-size: 1.05rem;">
                                            ${hasCandidate ? currentCandidate.sbd : 'N/A'}
                                        </span>
                                    </div>
                                    <div class="candidate-detail-item">
                                        <span class="candidate-detail-label">Họ và tên</span>
                                        <span class="candidate-detail-value">
                                            ${hasCandidate ? currentCandidate.name : 'Chưa có thí sinh'}
                                        </span>
                                    </div>
                                    <div class="candidate-detail-item">
                                        <span class="candidate-detail-label">Hạng xe / Số xe thi</span>
                                        <span class="candidate-detail-value" style="font-size: 0.85rem;">
                                            ${hasCandidate ? currentCandidate.licenseClass : 'Chưa rõ'} | ${hasCandidate ? currentCandidate.carNumber : 'Chưa xếp xe'}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="grading-pane">
                            <div class="grading-pane__header" style="margin-bottom: 1rem;">
                                <h2 class="grading-pane__title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                    <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                                    </svg>
                                    Camera giám sát sa hình (Live)
                                </h2>
                            </div>

                            <div class="camera-feed" style="aspect-ratio: 16 / 10; border-radius: 8px;">
                                <div class="camera-feed__overlay"></div>
                                <div class="camera-feed__reticle" style="width: 140px; height: 140px; border-color: rgba(99, 102, 241, 0.4);"></div>
                                <div class="camera-feed__scan-line" style="background: linear-gradient(to bottom, transparent, #6366f1, transparent); box-shadow: 0 0 8px #6366f1;"></div>
                                <div class="camera-feed__live-tag" style="background: rgba(0, 82, 204, 0.85);">
                                    Xe số ${hasCandidate ? currentCandidate.carNumber : '00'} - CAM 01
                                </div>
                                <span style="font-size: 0.8rem; font-weight: 600; color: #94a3b8; z-index: 10; text-transform: uppercase; letter-spacing: 0.05em; font-family: monospace; text-shadow: 0 2px 4px rgba(0,0,0,0.8);">SA HÌNH: ĐANG GIÁM SÁT</span>
                            </div>
                        </div>
                    </div>
                </div>

            </main>

        </div>

    </body>
</html>
