<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="About Us" />
</jsp:include>
<main class="main-content" style="margin-left: 0; width: 100%;">
    <header class="top-header" style="max-width: 1200px; margin: 0 auto; width: 100%;">
        <div class="header-title" style="display:flex; align-items:center; gap: 1rem;">
            <div class="logo-icon" style="width:36px; height:36px; font-size:1rem;">G</div>
            <h1 style="font-size:1.5rem;">GPLX Portal</h1>
        </div>
        <div style="display:flex; gap: 1.5rem;">
            <a href="home.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Home</a>
            <a href="about.jsp" class="btn btn-secondary active" style="padding: 0.5rem 1rem;">About</a>
            <a href="licenseTypes.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Licenses</a>
            <a href="process.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Process</a>
            <a href="login.jsp" class="btn btn-primary" style="padding: 0.5rem 1rem;">Sign In</a>
        </div>
    </header>

    <div style="max-width: 800px; margin: 4rem auto; padding: 0 1rem; text-align: center;">
        <h2 style="font-size: 2.5rem; margin-bottom: 1.5rem;">About Driving Exam Portal</h2>
        <p style="font-size: 1.1rem; margin-bottom: 2rem;">This platform implements full digitizations for candidates, examiners, and administrative staff to optimize Vietnam's driving license exam operations.</p>
        <div class="glass-card" style="text-align: left; margin-bottom: 2rem;">
            <h3 style="color: var(--primary); margin-bottom: 1rem;">System Highlights</h3>
            <ul style="color: var(--text-secondary); line-height: 2; padding-left: 1.5rem;">
                <li>Interactive candidate selfie and Face ID bio-verification flow.</li>
                <li>Live waiting-room queues and automatic computer room seat allocations.</li>
                <li>Immediate candidate exam score cards and secure operational audit logging.</li>
            </ul>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
