<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="Driving License System - Home" />
</jsp:include>
<main class="main-content" style="margin-left: 0; width: 100%;">
    <header class="top-header" style="max-width: 1200px; margin: 0 auto; width: 100%;">
        <div class="header-title" style="display:flex; align-items:center; gap: 1rem;">
            <div class="logo-icon" style="width:36px; height:36px; font-size:1rem;">G</div>
            <h1 style="font-size:1.5rem;">GPLX Portal</h1>
        </div>
        <div style="display:flex; gap: 1.5rem;">
            <a href="home.jsp" class="btn btn-secondary active" style="padding: 0.5rem 1rem;">Home</a>
            <a href="about.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">About</a>
            <a href="licenseTypes.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Licenses</a>
            <a href="process.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Process</a>
            <a href="login.jsp" class="btn btn-primary" style="padding: 0.5rem 1rem;">Sign In</a>
        </div>
    </header>

    <div class="hero-section">
        <span class="hero-badge">OFFICIAL GPXL LICENCE PLATFORM</span>
        <h1 class="hero-title">Your Journey to Safe Driving Starts Here</h1>
        <p class="hero-desc">Register, upload your medical documents, track your profile verification, and simulate theory exams in Vietnam's driving license examination system.</p>
        <div style="display:flex; gap: 1rem;">
            <a href="register.jsp" class="btn btn-primary" style="padding: 1rem 2rem; font-size: 1.1rem;">Register Account</a>
            <a href="licenseTypes.jsp" class="btn btn-secondary" style="padding: 1rem 2rem; font-size: 1.1rem;">View License Classes</a>
        </div>
    </div>

    <div style="max-width: 1200px; margin: 3rem auto; display:grid; grid-template-columns: repeat(3, 1fr); gap: 2rem; padding: 0 1rem;">
        <div class="glass-card">
            <h3>1. Quick Application</h3>
            <p style="margin-top: 1rem;">Submit your details, ID card, and health certifications through our premium paperless portal.</p>
        </div>
        <div class="glass-card">
            <h3>2. Dynamic Processing</h3>
            <p style="margin-top: 1rem;">Real-time document audits and fee payment checks by our operational staff team.</p>
        </div>
        <div class="glass-card">
            <h3>3. State-of-the-Art Exam</h3>
            <p style="margin-top: 1rem;">Take your active theory tests inside automated computer rooms with Live displays.</p>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
