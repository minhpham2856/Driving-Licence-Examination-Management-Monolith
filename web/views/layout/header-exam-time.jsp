<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
    Header - TopAppBar Navigation Shell
    Figma node: 111:16

    Parameters:
    - timeLeft: Text displayed in the center of the app bar.
--%>

<c:set var="tLeft" value="${not empty param.timeLeft ? param.timeLeft : '08:22:38'}" />

<style>
    .exam-time-header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1000;
        box-sizing: border-box;
        width: 100%;
        height: 64px;
        padding: 0 32px 1px;
        background: #ffffff;
        border-bottom: 1px solid #e2e7ff;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .exam-time-header__container {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .exam-time-header__time {
        width: 86.41px;
        margin: 0;
        color: #0052cc;
        font-family: "JetBrains Mono", "SFMono-Regular", Consolas, "Liberation Mono", monospace;
        font-size: 18px;
        font-weight: 500;
        line-height: 28px;
        letter-spacing: 0;
        text-align: left;
        word-break: break-word;
    }

    @media (max-width: 640px) {
        .exam-time-header {
            padding-left: 16px;
            padding-right: 16px;
        }
    }
</style>

<header class="exam-time-header" role="banner" data-node-id="111:16" data-name="Header - TopAppBar Navigation Shell">
    <div class="exam-time-header__container" data-node-id="111:23" data-name="Container">
        <p class="exam-time-header__time" data-node-id="111:28">${tLeft}</p>
    </div>
</header>
