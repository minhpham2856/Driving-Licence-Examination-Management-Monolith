<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
    Header - TopAppBar
    Figma node: 115:836

    Parameters:
    - examTitle: kept for backward compatibility, not shown in the current Figma node.
    - licenseClass: license category shown in the candidate line.
    - candidateName: candidate full name.
    - candidateCccd: candidate CCCD/ID number.
    - sbd: candidate SBD number.
    - timeLeft: countdown text shown in the right timer pill.
--%>

<c:set var="name" value="${not empty param.candidateName ? param.candidateName : 'Nguyễn Văn An'}" />
<c:set var="sbdCode" value="${not empty param.sbd ? param.sbd : '123'}" />
<c:set var="cccdCode" value="${not empty param.candidateCccd ? param.candidateCccd : '012345678909'}" />
<c:set var="licClass" value="${not empty param.licenseClass ? param.licenseClass : 'A1'}" />
<c:set var="tLeft" value="${not empty param.timeLeft ? param.timeLeft : '19:59'}" />

<style>
    .exam-running-header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1000;
        box-sizing: border-box;
        width: 100%;
        min-height: 80px;
        padding: 0 32px 1px;
        background: #ffffff;
        border-bottom: 1px solid #c3c6d6;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 24px;
        font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }

    .exam-running-header__candidate {
        display: flex;
        align-items: center;
        gap: 16px;
        min-width: 0;
        flex: 1 1 auto;
    }

    .exam-running-header__photo-shell {
        width: 48px;
        height: 48px;
        flex: 0 0 48px;
        box-sizing: border-box;
        border: 2px solid #dae2ff;
        border-radius: 9999px;
        padding: 2px;
        overflow: hidden;
        background: #ffffff;
    }

    .exam-running-header__photo {
        width: 100%;
        height: 100%;
        display: block;
        object-fit: cover;
        border-radius: 9999px;
    }

    .exam-running-header__info {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        min-width: 0;
    }

    .exam-running-header__name {
        margin: 0;
        color: #003d9b;
        font-size: 20px;
        font-weight: 800;
        line-height: 28px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .exam-running-header__meta {
        margin: 0;
        color: #434654;
        font-size: 12px;
        font-weight: 500;
        line-height: 16px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .exam-running-header__meta-label {
        color: #131b2e;
        font-weight: 500;
    }

    .exam-running-header__meta-value {
        color: #131b2e;
        font-weight: 700;
    }

    .exam-running-header__timer-card {
        flex: 0 0 auto;
        box-sizing: border-box;
        width: 192px;
        height: 47px;
        background: #ffffff;
        border: 1px solid #c3c6d6;
        border-radius: 12px;
        box-shadow: 0 4px 6px rgba(0, 82, 204, 0.08);
        padding: 9px 25px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 22px;
    }

    .exam-running-header__timer-icon {
        width: 18px;
        height: 21px;
        flex: 0 0 18px;
        display: block;
        color: #003d9b;
    }

    .exam-running-header__timer {
        margin: 0;
        color: #003d9b;
        font-family: "JetBrains Mono", "SFMono-Regular", Consolas, "Liberation Mono", monospace;
        font-size: 32px;
        font-weight: 500;
        line-height: 60px;
        letter-spacing: 4.8px;
        white-space: nowrap;
    }

    @media (max-width: 992px) {
        .exam-running-header {
            padding-left: 20px;
            padding-right: 20px;
        }

        .exam-running-header__timer-card {
            width: 176px;
            gap: 16px;
            padding-left: 18px;
            padding-right: 18px;
        }

        .exam-running-header__timer {
            font-size: 28px;
            letter-spacing: 3px;
        }
    }

    @media (max-width: 768px) {
        .exam-running-header {
            min-height: 64px;
            padding-left: 16px;
            padding-right: 16px;
            gap: 16px;
        }

        .exam-running-header__photo-shell {
            width: 40px;
            height: 40px;
            flex-basis: 40px;
        }

        .exam-running-header__name {
            font-size: 16px;
            line-height: 22px;
        }

        .exam-running-header__meta {
            display: none;
        }

        .exam-running-header__timer-card {
            width: 148px;
            height: 40px;
            gap: 12px;
            padding: 8px 14px;
        }

        .exam-running-header__timer-icon {
            width: 16px;
            height: 18px;
            flex-basis: 16px;
        }

        .exam-running-header__timer {
            font-size: 24px;
            line-height: 1;
            letter-spacing: 2px;
        }
    }
</style>

<header class="exam-running-header" role="banner" data-node-id="115:836" data-name="Header - TopAppBar">
    <div class="exam-running-header__candidate" data-node-id="115:837" data-name="Container">
        <div class="exam-running-header__photo-shell" data-node-id="115:838" data-name="Border">
            <img
                src="${pageContext.request.contextPath}/assets/imgs/header-running-photo.png"
                alt="${name}"
                class="exam-running-header__photo"
                onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/assets/imgs/avatar-placeholder.svg';"
                data-node-id="115:839"
                data-name="Candidate Photo"
            >
        </div>

        <div class="exam-running-header__info" data-node-id="115:840" data-name="Container">
            <h1 class="exam-running-header__name" title="${name}" data-node-id="115:841">${name}</h1>
            <p class="exam-running-header__meta" title="SBD: ${sbdCode} • CC: ${cccdCode} • Hạng: ${licClass}" data-node-id="115:843">
                <span class="exam-running-header__meta-label">SBD:</span>
                <span class="exam-running-header__meta-value">${sbdCode}</span>
                <span class="exam-running-header__meta-label"> • CC:</span>
                <span class="exam-running-header__meta-value"> ${cccdCode}</span>
                <span class="exam-running-header__meta-label"> • Hạng:</span>
                <span class="exam-running-header__meta-value"> ${licClass}</span>
            </p>
        </div>
    </div>

    <div class="exam-running-header__timer-card" data-node-id="115:1570" data-name="Background+Border+Shadow">
        <img
            src="${pageContext.request.contextPath}/assets/imgs/header-running-timer-icon.svg"
            alt=""
            aria-hidden="true"
            class="exam-running-header__timer-icon"
            data-node-id="115:1571"
            data-name="Container"
            onerror="this.style.display='none';"
        >
        <p class="exam-running-header__timer" data-node-id="115:1574">${tLeft}</p>
    </div>
</header>
