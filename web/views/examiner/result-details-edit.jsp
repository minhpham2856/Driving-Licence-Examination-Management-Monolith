<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/layout/examiner-seed-data.jsp" />

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cssStyle" value="${ctx}/assets/css/style.css" />
<c:set var="cssLayout" value="${ctx}/assets/css/layout.css" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="backUrl" value="${ctx}/views/examiner/result-details.jsp" />
<c:set var="pageUrl" value="${ctx}/views/examiner/result-details-edit.jsp?sbd=${candidate.sbd}" />
<c:set var="currentScore" value="${candidate.correct}" />
<c:set var="maxScore" value="35" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${cssStyle}">
        <link rel="stylesheet" href="${cssLayout}">
    </head>
    <body class="has-side-nav-bar examiner-portal">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="sua-ket-qua" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <!--toolbar-->
                <section class="examiner-toolbar">
                    <div class="exr-toolbar-left">
                        <a href="${backUrl}" class="exr-back">
                            <span class="material-symbols-outlined">arrow_back</span>
                            QUAY LẠI
                        </a>
                        <h2 class="examiner-toolbar__title">Sửa kết quả</h2>
                    </div>
                    <div class="examiner-toolbar__actions">
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>
                            In thông tin chi tiết
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">visibility</span>
                            Xem đề thi
                        </a>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <!--edit form-->
                <section class="exr-grid">
                    <div class="exr-col-left">
                        <div class="exr-card exr-card--accent">
                            <div class="exr-section-title">
                                <span class="material-symbols-outlined">person</span>
                                <span>THÔNG TIN THÍ SINH</span>
                            </div>
                            <div class="exr-id-grid">
                                <div class="exr-field">
                                    <p class="exr-field__label">HỌ VÀ TÊN</p>
                                    <p class="exr-field__value">${candidate.fullName}</p>
                                </div>
                                <div class="exr-field">
                                    <p class="exr-field__label">CCCD / CMND</p>
                                    <span class="exr-chip">${candidate.governmentId}</span>
                                </div>
                                <div class="exr-field">
                                    <p class="exr-field__label">SỐ BÁO DANH</p>
                                    <span class="exr-chip">${candidate.sbd}</span>
                                </div>
                                <div class="exr-field">
                                    <p class="exr-field__label">NGÀY THI</p>
                                    <p class="exr-field__value exr-field__value--sm">${candidate.examDate}</p>
                                </div>
                                <div class="exr-field">
                                    <p class="exr-field__label">HẠNG GPLX</p>
                                    <p class="exr-field__value exr-field__value--bold">Hạng ${candidate.licenceClass}</p>
                                </div>
                            </div>
                        </div>

                        <div class="exr-pair">
                            <div class="exr-card exr-card--mod">
                                <div class="exr-section-title">
                                    <span class="material-symbols-outlined">edit</span>
                                    <span>ĐIỀU CHỈNH ĐIỂM</span>
                                </div>
                                <div class="exr-score-box">
                                    <p class="exr-field__label">ĐIỂM HIỆN TẠI</p>
                                    <div class="exr-score-row">
                                        <span class="exr-score-current">${currentScore}</span>
                                        <span class="exr-score-total">/${maxScore}</span>
                                        <span class="exr-badge-fail"><c:choose><c:when test="${candidate.passed}">ĐẠT</c:when><c:otherwise>KHÔNG ĐẠT</c:otherwise></c:choose></span>
                                    </div>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="newScore">ĐIỂM MỚI</label>
                                    <div class="exr-input-suffix">
                                        <input type="text" id="newScore" class="exr-input exr-input--mono" placeholder="Nhập điểm số" value="${currentScore}">
                                        <span class="exr-input-suffix__text">/${maxScore}</span>
                                    </div>
                                </div>
                            </div>

                            <div class="exr-card">
                                <div class="exr-section-title">
                                    <span class="material-symbols-outlined">notes</span>
                                    <span>LÝ DO ĐIỀU CHỈNH</span>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="reason">CHỌN LÝ DO</label>
                                    <select id="reason" class="exr-select">
                                        <option value="">-- Lựa chọn lý do quy định --</option>
                                        <option value="cham-sai">Chấm sai</option>
                                        <option value="nhap-nham">Nhập nhầm điểm</option>
                                        <option value="khieu-nai">Thí sinh khiếu nại</option>
                                        <option value="khac">Lý do khác</option>
                                    </select>
                                </div>
                                <div class="exr-control">
                                    <label class="exr-input-label" for="reasonDetail">LÝ DO CHI TIẾT</label>
                                    <textarea id="reasonDetail" class="exr-textarea" placeholder="Nhập mô tả chi tiết nguyên nhân dẫn đến việc thay đổi điểm số..."></textarea>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="exr-col-right">
                        <div class="exr-warning">
                            <span class="exr-warning__icon material-symbols-outlined">warning</span>
                            <div class="exr-warning__body">
                                <p class="exr-warning__title">CẢNH BÁO HỆ THỐNG</p>
                                <p class="exr-warning__text">Mọi thao tác thay đổi điểm số đều được lưu trữ vĩnh viễn vào nhật ký hệ thống.</p>
                            </div>
                        </div>

                        <div class="exr-card">
                            <div class="exr-section-title">
                                <span class="material-symbols-outlined">lock</span>
                                <span>XÁC THỰC BẢO MẬT</span>
                            </div>
                            <div class="exr-control">
                                <label class="exr-input-label" for="pwd">MẬT KHẨU</label>
                                <input type="password" id="pwd" class="exr-input" placeholder="Nhập mật khẩu của bạn">
                            </div>
                            <div class="exr-control">
                                <label class="exr-input-label">MÃ XÁC NHẬN</label>
                                <div class="exr-captcha">
                                    <span class="exr-captcha__img">8H3K9A</span>
                                    <a href="#" class="exr-captcha__refresh">
                                        <span class="material-symbols-outlined">refresh</span>
                                    </a>
                                </div>
                                <input type="text" class="exr-input exr-input--mono exr-input--captcha" placeholder="NHẬP MÃ XÁC NHẬN">
                            </div>
                            <div class="exr-confirm-wrap">
                                <button type="button" class="exr-btn-confirm">
                                    <span class="material-symbols-outlined">verified_user</span>
                                    XÁC NHẬN THAY ĐỔI ĐIỂM
                                </button>
                                <p class="exr-confirm-note">Nhấn xác nhận đồng nghĩa với việc bạn chịu trách nhiệm hoàn toàn về thay đổi này.</p>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>
