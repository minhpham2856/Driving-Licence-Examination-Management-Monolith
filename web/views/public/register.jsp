<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="Sign Up" />
</jsp:include>
<main style="display:grid; grid-template-columns: 450px 1fr; width: 100vw; min-height: 100vh;">
    <!-- Form Side -->
    <div style="background: var(--bg-surface); padding: 3rem; display:flex; flex-direction:column; justify-content:center; border-right:1px solid var(--border-color); overflow-y:auto;">
        <div style="margin-bottom: 2rem; display:flex; align-items:center; gap: 0.75rem;">
            <div class="logo-icon">G</div>
            <div class="logo-text" style="font-size: 1.5rem;">GPLX</div>
        </div>
        
        <h2 style="font-size: 1.75rem; font-weight:700; margin-bottom: 0.5rem;">Create Account</h2>
        <p style="margin-bottom: 1.5rem;">Get registered for your Vietnam driving exam.</p>
        
        <form style="display:flex; flex-direction:column; gap:1.25rem;" onsubmit="event.preventDefault(); window.location.href='login.jsp';">
            <div class="form-group">
                <label class="form-label">Full Name</label>
                <input type="text" class="form-control" placeholder="Enter Full Name" required>
            </div>
            
            <div class="form-group">
                <label class="form-label">Email Address</label>
                <input type="email" class="form-control" placeholder="name@example.com" required>
            </div>
            
            <div class="form-group">
                <label class="form-label">Mobile Number</label>
                <input type="text" class="form-control" placeholder="+84 ..." required>
            </div>
            
            <div class="form-group">
                <label class="form-label">Select License Class</label>
                <select class="form-control" style="background: #0f172a;">
                    <option>Class A1 (Motorcycle under 175cc)</option>
                    <option>Class A2 (Motorcycle over 175cc)</option>
                    <option>Class B1 (Automobile Automatic)</option>
                    <option>Class B2 (Automobile Manual)</option>
                </select>
            </div>
            
            <div class="form-group">
                <label class="form-label">Password</label>
                <input type="password" class="form-control" placeholder="â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢" required>
            </div>
            
            <button type="submit" class="btn btn-primary" style="margin-top: 0.5rem; width:100%;">Create Account</button>
        </form>
        
        <p style="margin-top: 1.5rem; text-align:center; font-size:0.9rem;">
            Already have an account? <a href="login.jsp" style="color:var(--primary); text-decoration:none; font-weight:bold;">Sign In</a>
        </p>
    </div>
    
    <!-- Image Panel Side -->
    <div style="background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%); display:flex; align-items:center; justify-content:center; padding: 4rem;">
        <div class="glass-card pulse-glow" style="max-width: 500px; text-align:center;">
            <h2 style="font-size:2rem; line-height:1.3; margin-bottom:1rem;">Fast-Track Registration</h2>
            <p>Once registered, you can log in, edit your applicant profile, upload document scans (Medical certificates, passport size photos), register for open exam slots, and track review status in real time.</p>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
