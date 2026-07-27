<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
    Footer
    Figma node: 117:62

    JSTL-only component. No JavaScript required.

    Optional parameters:
    - noticeTitle: Title for the notice card.
    - noticeText: Body text for the notice card.
--%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="noticeTitle" value="${not empty param.noticeTitle ? param.noticeTitle : 'Lưu ý:'}" />
<c:set var="noticeText" value="${not empty param.noticeText ? param.noticeText : 'Sử dụng bàn phím trên màn hình hoặc bàn phím số bên phải của máy tính'}" />
<c:set var="noticeIconUrl" value="${ctx}/assets/imgs/footer-notice-icon.svg" />
<c:set var="csgtImageUrl" value="${ctx}/assets/imgs/csgt-footer.png" />

<style>
    .exam-footer {
        position: fixed;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 1000;
        box-sizing: border-box;
        height: 64px;
        padding: 0 32px;
        background: #f2f3ff;
        border-top: 1px solid #c3c6d6;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 32px;
        font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
    }

    .exam-footer__notice-card {
        box-sizing: border-box;
        width: 473px;
        flex: 0 0 473px;
        background: #ffffff;
        border: 1px solid rgba(195, 198, 214, 0.5);
        border-radius: 8px;
    }

    .exam-footer__notice-inner {
        display: flex;
        align-items: center;
        gap: 8px;
        box-sizing: border-box;
        width: 100%;
        padding: 8px 12px;
    }

    .exam-footer__notice-icon {
        width: 14px;
        height: 18px;
        flex: 0 0 14px;
        display: block;
    }

    .exam-footer__notice-copy {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 6px;
        min-width: 0;
        padding-right: 0;
        font-size: 11px;
        line-height: 1.2;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .exam-footer__notice-title,
    .exam-footer__notice-text {
        margin: 0;
        line-height: 1.2;
    }

    .exam-footer__notice-title {
        color: #131b2e;
        font-weight: 700;
    }

    .exam-footer__notice-text {
        color: #434654;
        font-weight: 400;
    }

    .exam-footer__agency {
        flex: 1 1 auto;
        min-width: 280px;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 12px;
    }

    .exam-footer__agency-text {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: center;
        min-width: 0;
        text-align: right;
        text-transform: uppercase;
        white-space: nowrap;
        letter-spacing: 1px;
    }

    .exam-footer__agency-kicker,
    .exam-footer__agency-name {
        margin: 0;
        line-height: 1.2;
    }

    .exam-footer__agency-kicker {
        color: #404040;
        font-size: 10px;
        font-weight: 500;
    }

    .exam-footer__agency-name {
        color: #db0c00;
        font-family: "Roboto", "Inter", Arial, sans-serif;
        font-size: 13px;
        font-weight: 800;
    }

    .exam-footer__divider-wrap {
        width: 20px;
        height: 24px;
        padding: 0 8px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        flex: 0 0 20px;
    }

    .exam-footer__divider {
        width: 100%;
        min-width: 1px;
        height: 24px;
        border-radius: 50px;
        background: #595959;
    }

    .exam-footer__agency-image {
        width: 32px;
        height: 40px;
        flex: 0 0 32px;
        display: block;
        object-fit: cover;
        pointer-events: none;
    }

    @media (max-width: 900px) {
        .exam-footer {
            height: 64px;
            padding: 0 16px;
            align-items: center;
            flex-direction: row;
            gap: 16px;
        }

        .exam-footer__notice-card {
            width: auto;
            flex: 1 1 auto;
        }

        .exam-footer__notice-copy {
            white-space: nowrap;
        }

        .exam-footer__agency {
            width: auto;
            min-width: 0;
            justify-content: flex-end;
        }
    }

    @media (max-width: 600px) {
        .exam-footer__notice-card {
            display: none;
        }
    }
</style>

<footer class="exam-footer" role="contentinfo" data-node-id="117:62" data-name="Footer">
    <section class="exam-footer__notice-card" aria-label="${noticeTitle}" data-node-id="117:63" data-name="Background+Border">
        <div class="exam-footer__notice-inner">
            <img class="exam-footer__notice-icon" src="${noticeIconUrl}" alt="" aria-hidden="true" data-node-id="111:98">
            <div class="exam-footer__notice-copy" data-node-id="111:99">
                <p class="exam-footer__notice-title" data-node-id="111:100">${noticeTitle}</p>
                <p class="exam-footer__notice-text" data-node-id="111:101">${noticeText}</p>
            </div>
        </div>
    </section>

    <section class="exam-footer__agency" aria-label="Bộ Công An - Cục Cảnh Sát Giao Thông" data-node-id="117:66" data-name="Instruction Header">
        <div class="exam-footer__agency-text" data-node-id="117:67" data-name="Group">
            <p class="exam-footer__agency-kicker" data-node-id="117:68">Bộ công an</p>
            <p class="exam-footer__agency-name" data-node-id="117:69">Cục Cảnh Sát Giao Thông</p>
        </div>
        <div class="exam-footer__divider-wrap" data-node-id="117:70" data-name="Margin">
            <span class="exam-footer__divider" data-node-id="117:71" data-name="Vertical Divider"></span>
        </div>
        <img class="exam-footer__agency-image" src="${csgtImageUrl}" alt="Cục Cảnh Sát Giao Thông" data-node-id="117:72" data-name="csgt 1">
    </section>
</footer>
