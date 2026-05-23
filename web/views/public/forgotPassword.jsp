<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("noSidebar", true); %>
<jsp:include page="/WEB-INF/views/layout/header.jsp">
    <jsp:param name="title" value="Forgot Password" />
</jsp:include>
<main style="display:flex; align-items:center; justify-content:center; width: 100vw; min-height: 100vh; background: var(--bg-main);">
    <div class="glass-card" style="width: 450px; display:flex; flex-direction:column; gap: 1.5rem;">
        <div style="display:flex; align-items:center; gap: 0.75rem; justify-content:center; margin-bottom: 1rem;">
            <div class="logo-icon">G</div>
            <div class="logo-text" style="font-size: 1.5rem;">GPLX</div>
        </div>
        <h2 style="text-align:center; font-size:1.75rem;">Reset Password</h2>
        <p style="text-align:center;">Enter your email below and we will send a password reset code verification simulator.</p>
        <form style="display:flex; flex-direction:column; gap: 1.5rem;" onsubmit="alert('A mock verification email has been simulated!'); window.location.href='login.jsp';">
            <div class="form-group">
                <label class="form-label">Email Address</label>
                <input type="email" class="form-control" placeholder="name@example.com" required>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;">Send Reset Code</button>
            <a href="login.jsp" class="btn btn-secondary" style="width:100%; text-align:center; display:block; text-decoration:none;">Back to Sign In</a>
        </form>
    </div>
</main>
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
