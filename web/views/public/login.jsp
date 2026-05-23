<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="Sign In" />
</jsp:include>
<main style="display:grid; grid-template-columns: 450px 1fr; width: 100vw; min-height: 100vh;">
    <!-- Form Side -->
    <div style="background: var(--bg-surface); padding: 4rem 3rem; display:flex; flex-direction:column; justify-content:center; border-right:1px solid var(--border-color);">
        <div style="margin-bottom: 3rem; display:flex; align-items:center; gap: 0.75rem;">
            <div class="logo-icon">G</div>
            <div class="logo-text" style="font-size: 1.5rem;">GPLX</div>
        </div>
        
        <h2 style="font-size: 2rem; font-weight:700; margin-bottom: 0.5rem;">Welcome back!</h2>
        <p style="margin-bottom: 2rem;">Sign in to your driving licensing account.</p>
        
        <form style="display:flex; flex-direction:column; gap:1.5rem;" onsubmit="event.preventDefault(); window.location.href='<%= request.getContextPath() %>/WEB-INF/views/registrant/dashboard.jsp';">
            <div class="form-group">
                <label class="form-label">Email or SBD</label>
                <input type="text" class="form-control" placeholder="Enter registration email" required>
            </div>
            
            <div class="form-group">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <label class="form-label">Password</label>
                    <a href="forgotPassword.jsp" style="font-size: 0.8rem; color: var(--primary); text-decoration:none;">Forgot?</a>
                </div>
                <input type="password" class="form-control" placeholder="â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢" required>
            </div>
            
            <button type="submit" class="btn btn-primary" style="margin-top: 1rem; width:100%;">Sign In</button>
        </form>
        
        <p style="margin-top: 2rem; text-align:center; font-size:0.9rem;">
            Don't have an account? <a href="register.jsp" style="color:var(--primary); text-decoration:none; font-weight:bold;">Sign Up</a>
        </p>
    </div>
    
    <!-- Image Panel Side -->
    <div style="background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%); display:flex; align-items:center; justify-content:center; padding: 4rem;">
        <div class="glass-card pulse-glow" style="max-width: 500px; text-align:center;">
            <span class="badge badge-info" style="margin-bottom: 1.5rem;">SECURE & ENCRYPTED</span>
            <h2 style="font-size:2rem; line-height:1.3; margin-bottom:1rem;">Automated Examination & Driving Licensing</h2>
            <p>Viet Nam's premier integrated driving license exam monolith. Dynamic live scoreboard panels, real-time desk ticket announcements, and secure administrative controls.</p>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
