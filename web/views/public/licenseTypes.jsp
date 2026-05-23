<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="License Classes" />
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
            <a href="licenseTypes.jsp" class="btn btn-secondary active" style="padding: 0.5rem 1rem;">Licenses</a>
            <a href="process.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem;">Process</a>
            <a href="login.jsp" class="btn btn-primary" style="padding: 0.5rem 1rem;">Sign In</a>
        </div>
    </header>

    <div style="max-width: 1200px; margin: 4rem auto; padding: 0 1rem;">
        <h2 style="text-align: center; margin-bottom: 3rem; font-size: 2.5rem;">Available Vietnam Driving Licenses</h2>
        
        <div class="metrics-grid">
            <div class="glass-card">
                <span class="badge badge-success">Motorcycle</span>
                <h3 style="margin: 1rem 0 0.5rem 0; font-size: 1.75rem;">Class A1</h3>
                <p>Engine displacement from 50cc to under 175cc. Vietnam's most popular license.</p>
                <div style="margin-top: 1.5rem; display:flex; justify-content:space-between; font-weight:bold;">
                    <span>Fee:</span><span style="color:var(--primary);">350,000 VND</span>
                </div>
            </div>
            
            <div class="glass-card">
                <span class="badge badge-info">Motorcycle</span>
                <h3 style="margin: 1rem 0 0.5rem 0; font-size: 1.75rem;">Class A2</h3>
                <p>Heavy engine displacement from 175cc and above. Unlimited capacity bikes.</p>
                <div style="margin-top: 1.5rem; display:flex; justify-content:space-between; font-weight:bold;">
                    <span>Fee:</span><span style="color:var(--primary);">650,000 VND</span>
                </div>
            </div>
            
            <div class="glass-card">
                <span class="badge badge-warning">Automobile</span>
                <h3 style="margin: 1rem 0 0.5rem 0; font-size: 1.75rem;">Class B1</h3>
                <p>Automatic transmission cars up to 9 seats. Non-commercial license class.</p>
                <div style="margin-top: 1.5rem; display:flex; justify-content:space-between; font-weight:bold;">
                    <span>Fee:</span><span style="color:var(--primary);">1,200,000 VND</span>
                </div>
            </div>
            
            <div class="glass-card">
                <span class="badge badge-danger">Automobile</span>
                <h3 style="margin: 1rem 0 0.5rem 0; font-size: 1.75rem;">Class B2</h3>
                <p>Manual transmission cars up to 9 seats. Commercial driving allowed.</p>
                <div style="margin-top: 1.5rem; display:flex; justify-content:space-between; font-weight:bold;">
                    <span>Fee:</span><span style="color:var(--primary);">1,500,000 VND</span>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
