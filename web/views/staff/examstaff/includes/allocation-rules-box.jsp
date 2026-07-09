<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="variant" value="${empty param.variant ? 'theory' : param.variant}" />

<c:choose>
    <c:when test="${variant eq 'practical'}">
        <c:set var="boxClass" value="examstaff-rules-box--practical" />
        <c:set var="boxIcon" value="precision_manufacturing" />
        <c:set var="boxLabel" value="Quy định điểm đạt thi thực hành / sa hình" />
        <c:set var="boxTitle" value="Quy định điểm đạt — Thực hành / Sa hình" />
    </c:when>
    <c:when test="${variant eq 'results'}">
        <c:set var="boxClass" value="examstaff-rules-box--results" />
        <c:set var="boxIcon" value="fact_check" />
        <c:set var="boxLabel" value="Tổng hợp quy định điểm đạt các phần thi" />
        <c:set var="boxTitle" value="Quy định điểm đạt — Tổng hợp kết quả" />
    </c:when>
    <c:otherwise>
        <c:set var="boxClass" value="examstaff-rules-box--theory" />
        <c:set var="boxIcon" value="menu_book" />
        <c:set var="boxLabel" value="Quy định điểm đạt thi lý thuyết" />
        <c:set var="boxTitle" value="Quy định điểm đạt — Thi lý thuyết" />
    </c:otherwise>
</c:choose>

<aside class="examstaff-rules-box ${boxClass}" aria-label="${boxLabel}">
    <div class="examstaff-rules-box__head">
        <span class="material-symbols-outlined examstaff-rules-box__head-icon" aria-hidden="true">${boxIcon}</span>
        <p class="examstaff-rules-box__title">${boxTitle}</p>
    </div>

    <div class="examstaff-rules-doc">
        <c:choose>
            <c:when test="${variant eq 'practical'}">
                <p class="examstaff-rules-doc__lead">Thí sinh được coi là đạt phần thi thực hành / sa hình khi đáp ứng các điều kiện sau:</p>
                <ol class="examstaff-rules-doc__list">
                    <li>Đạt tối thiểu <strong>80 điểm trên tổng 100 điểm</strong>.</li>
                    <li>Không mắc <strong>lỗi liệt</strong> trong quá trình thi.</li>
                    <li>Chỉ được vào thi khi đã <strong>đỗ lý thuyết</strong>, hoặc thuộc trường hợp <strong>thi lại sa hình</strong> (hạng A / A1).</li>
                </ol>
            </c:when>

            <c:when test="${variant eq 'results'}">
                <p class="examstaff-rules-doc__lead">Kết quả đỗ / trượt được xác định theo ngưỡng đạt của từng phần thi như sau:</p>
                <div class="examstaff-rules-doc__cards">
                    <div class="examstaff-rules-doc__card">
                        <p class="examstaff-rules-doc__card-title">1. Thi lý thuyết</p>
                        <ul class="examstaff-rules-doc__list examstaff-rules-doc__list--bullet">
                            <li>Hạng <strong>A / A1</strong>: ≥ <strong>36 / 40</strong></li>
                            <li>Hạng <strong>B1</strong>: ≥ <strong>45 / 50</strong></li>
                        </ul>
                    </div>
                    <div class="examstaff-rules-doc__card">
                        <p class="examstaff-rules-doc__card-title">2. Thực hành / Sa hình</p>
                        <p class="examstaff-rules-doc__card-text">Đạt tối thiểu <strong>80 điểm</strong>, không mắc lỗi liệt.</p>
                    </div>
                </div>
                <p class="examstaff-rules-doc__note">Lưu ý: Lý thuyết không sai câu điểm liệt. Bảo lưu phần thi theo hồ sơ đăng ký.</p>
            </c:when>

            <c:otherwise>
                <p class="examstaff-rules-doc__lead">Thí sinh được coi là đạt phần thi lý thuyết khi đáp ứng ngưỡng số câu đúng theo hạng GPLX:</p>
                <ul class="examstaff-rules-doc__list examstaff-rules-doc__list--bullet">
                    <li>Hạng <strong>A / A1</strong>: tối thiểu <strong>36 / 40</strong> câu đúng.</li>
                    <li>Hạng <strong>B1</strong>: tối thiểu <strong>45 / 50</strong> câu đúng.</li>
                </ul>
                <p class="examstaff-rules-doc__note">Lưu ý: Thí sinh không được sai câu điểm liệt. Mỗi thí sinh trong một kỳ thi chỉ được phân <strong>một ca thi</strong> và <strong>một phòng thi lý thuyết</strong>.</p>
            </c:otherwise>
        </c:choose>
    </div>
</aside>
