<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="Examination Process" />
</jsp:include>
<main class="main-content" style="margin-left: 0; width: 100%;">
    <header class="top-header" style="max-width: 1200px; margin: 0 auto; width: 100%;">
        <div class="header-title" style="display:flex; align-items:center; gap: 1rem;">
            <div class="logo-icon" style="width:36px; height:36px; font-size:1rem;">G</div>
            <h1 style="font-size:1.5rem;">GPLX Portal</h1>
        </div>
        <div style="display:flex; gap: 1.5rem;">
            <a href="home.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Home</a>
            <a href="about.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">About</a>
            <a href="licenseTypes.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Licenses</a>
            <a href="process.jsp" class="btn btn-secondary active" style="padding: 0.5rem 1rem;">Process</a>
            <a href="login.jsp" class="btn btn-primary" style="padding: 0.5rem 1rem;">Sign In</a>
        </div>
    </header>

    <div style="max-width: 800px; margin: 4rem auto; padding: 0 1rem;">
        <h2 style="text-align: center; margin-bottom: 3rem; font-size: 2.5rem;">Licensing Steps Overview</h2>
        
        <div style="display:flex; flex-direction:column; gap: 2rem;">
            <div class="glass-card" style="display:flex; gap: 2rem; align-items:center;">
                <div class="logo-icon" style="width:60px; height:60px; font-size:1.5rem; flex-shrink:0;">1</div>
                <div>
                    <h3>Create Account & Profile Info</h3>
                    <p style="margin-top: 0.5rem;">Sign up using your personal email, phone, and upload passport photo and ID cards scan copies.</p>
                </div>
            </div>
            
            <div class="glass-card" style="display:flex; gap: 2rem; align-items:center;">
                <div class="logo-icon" style="width:60px; height:60px; font-size:1.5rem; flex-shrink:0;">2</div>
                <div>
                    <h3>Medical Verification & Fee Payment</h3>
                    <p style="margin-top: 0.5rem;">Submit dynamic medical health check certificates from licensed local clinics and pay registration charges via fee scan cards.</p>
                </div>
            </div>
            
            <div class="glass-card" style="display:flex; gap: 2rem; align-items:center;">
                <div class="logo-icon" style="width:60px; height:60px; font-size:1.5rem; flex-shrink:0;">3</div>
                <div>
                    <h3>Theoretical computer test & Practical yard tests</h3>
                    <p style="margin-top: 0.5rem;">Check SBD at Waiting room, confirm identity details, complete 30 minutes computer theory questions, and undergo yard tests supervised by examiners.</p>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
