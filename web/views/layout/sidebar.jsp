<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String currentPath = request.getRequestURI();
    String activeMenu = "";
    String activeRole = "public"; // default
    
    if (currentPath.contains("/registrant/")) {
        activeRole = "registrant";
    } else if (currentPath.contains("/staff/")) {
        activeRole = "staff";
    } else if (currentPath.contains("/examiner/")) {
        activeRole = "examiner";
    } else if (currentPath.contains("/admin/")) {
        activeRole = "admin";
    } else if (currentPath.contains("/candidate/")) {
        activeRole = "candidate";
    }
%>
<aside class="sidebar">
    <div class="sidebar-logo">
        <div class="logo-icon">G</div>
        <div class="logo-text">GPLX PORTAL</div>
    </div>

    <!-- Role Simulator Card (Super convenient for testing visual designs!) -->
    <div class="role-selector-card">
        <label class="form-label" style="font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.25rem; display:block;">SIMULATE USER ROLE</label>
        <select id="roleMockSelector" onchange="switchMockRole(this.value)">
            <option value="public" <%= activeRole.equals("public") ? "selected" : "" %>>Public / Visitor</option>
            <option value="registrant" <%= activeRole.equals("registrant") ? "selected" : "" %>>Registrant (Applicant)</option>
            <option value="staff" <%= activeRole.equals("staff") ? "selected" : "" %>>System Staff</option>
            <option value="examiner" <%= activeRole.equals("examiner") ? "selected" : "" %>>Examiner</option>
            <option value="admin" <%= activeRole.equals("admin") ? "selected" : "" %>>Administrator</option>
            <option value="candidate" <%= activeRole.equals("candidate") ? "selected" : "" %>>Candidate (Exam Mode)</option>
        </select>
    </div>

    <!-- Menu Section: Public / Visitor -->
    <ul class="sidebar-menu role-menu" id="menu-public" style="display: none;">
        <li class="menu-label">Visitor Portal</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/home.jsp" class="sidebar-link <%= currentPath.contains("home.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            Home
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/about.jsp" class="sidebar-link <%= currentPath.contains("about.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            About Info
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/licenseTypes.jsp" class="sidebar-link <%= currentPath.contains("licenseTypes.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="4"/><line x1="8" y1="2" x2="8" y2="4"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            License Guides
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/process.jsp" class="sidebar-link <%= currentPath.contains("process.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            Workflow Process
        </a></li>
        <li class="menu-label">Authentication</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/login.jsp" class="sidebar-link <%= currentPath.contains("login.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
            Login / Sign In
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/public/register.jsp" class="sidebar-link <%= currentPath.contains("register.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
            Register / Sign Up
        </a></li>
    </ul>

    <!-- Menu Section: Registrant (Candidate Portal) -->
    <ul class="sidebar-menu role-menu" id="menu-registrant" style="display: none;">
        <li class="menu-label">Registrant Dashboard</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/dashboard.jsp" class="sidebar-link <%= currentPath.contains("dashboard.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
            Overview
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/profile.jsp" class="sidebar-link <%= currentPath.contains("profile.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            My Profile
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/uploadDocuments.jsp" class="sidebar-link <%= currentPath.contains("uploadDocuments.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            Upload Documents
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/registerExam.jsp" class="sidebar-link <%= currentPath.contains("registerExam.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            Register Exam
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/trackApplication.jsp" class="sidebar-link <%= currentPath.contains("trackApplication.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            Track Status
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/registrant/examSchedule.jsp" class="sidebar-link <%= currentPath.contains("examSchedule.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            My Exam Dates
        </a></li>
    </ul>

    <!-- Menu Section: Operational Staff -->
    <ul class="sidebar-menu role-menu" id="menu-staff" style="display: none;">
        <li class="menu-label">Staff Management</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/managing/dashboard.jsp" class="sidebar-link <%= currentPath.contains("/managing/dashboard.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
            Overview
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/managing/userList.jsp" class="sidebar-link <%= currentPath.contains("userList.jsp") || currentPath.contains("userDetail.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            Candidates List
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/managing/documentApproval.jsp" class="sidebar-link <%= currentPath.contains("documentApproval.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg>
            Approve Docs
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/managing/createUser.jsp" class="sidebar-link <%= currentPath.contains("createUser.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
            New Candidate
        </a></li>
        <li class="menu-label">Session Controls</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/importList.jsp" class="sidebar-link <%= currentPath.contains("importList.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            Import SBD List
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/allocate.jsp" class="sidebar-link <%= currentPath.contains("allocate.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            Seat Allocation
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/waitingRoom.jsp" class="sidebar-link <%= currentPath.contains("waitingRoom.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
            Waiting Room
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/profileVerification.jsp" class="sidebar-link <%= currentPath.contains("profileVerification.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            Verify Profile
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/capturePhoto.jsp" class="sidebar-link <%= currentPath.contains("capturePhoto.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
            Capture Photo
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/feeQR.jsp" class="sidebar-link <%= currentPath.contains("feeQR.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="2" y1="7" x2="22" y2="7"/><line x1="2" y1="17" x2="22" y2="17"/></svg>
            Fee Collection
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/exportReport.jsp" class="sidebar-link <%= currentPath.contains("exportReport.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            Export Reports
        </a></li>
        <li class="menu-label">Live Broadcast</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/live/scoreboard.jsp" class="sidebar-link <%= currentPath.contains("scoreboard.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
            Live Scoreboard
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/live/callDisplay.jsp" class="sidebar-link <%= currentPath.contains("callDisplay.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/><path d="M19 10v1a7 7 0 0 1-14 0v-1"/><line x1="12" y1="19" x2="12" y2="22"/></svg>
            Calling Screen
        </a></li>
        <li class="menu-label">Security</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/staff/exam/audit.jsp" class="sidebar-link <%= currentPath.contains("/exam/audit.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Audit Log
        </a></li>
    </ul>

    <!-- Menu Section: Examiner Panel -->
    <ul class="sidebar-menu role-menu" id="menu-examiner" style="display: none;">
        <li class="menu-label">Examiner Tasks</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/examiner/selectSession.jsp" class="sidebar-link <%= currentPath.contains("selectSession.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
            Select Session
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/examiner/theoryMonitor.jsp" class="sidebar-link <%= currentPath.contains("theoryMonitor.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            Theory Monitor
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/examiner/practicalControl.jsp" class="sidebar-link <%= currentPath.contains("practicalControl.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="M12 6v6l4 2"/></svg>
            Practical Exam
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/examiner/roadControl.jsp" class="sidebar-link <%= currentPath.contains("roadControl.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
            Road Control
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/examiner/audit.jsp" class="sidebar-link <%= currentPath.contains("/examiner/audit.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Audit Trails
        </a></li>
    </ul>

    <!-- Menu Section: Administrator -->
    <ul class="sidebar-menu role-menu" id="menu-admin" style="display: none;">
        <li class="menu-label">System Control</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/dashboard.jsp" class="sidebar-link <%= currentPath.contains("/admin/dashboard.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
            Master Dashboard
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/areas.jsp" class="sidebar-link <%= currentPath.contains("areas.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            Areas/Branches
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/rooms.jsp" class="sidebar-link <%= currentPath.contains("rooms.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="3" x2="9" y2="21"/></svg>
            Exam Rooms
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/computers.jsp" class="sidebar-link <%= currentPath.contains("computers.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            Computers Status
        </a></li>
        <li class="menu-label">Settings</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/licenseTypes.jsp" class="sidebar-link <%= currentPath.contains("/admin/licenseTypes.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="4"/><line x1="8" y1="2" x2="8" y2="4"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            License Options
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/fees.jsp" class="sidebar-link <%= currentPath.contains("fees.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            Fees & Billing
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/users.jsp" class="sidebar-link <%= currentPath.contains("/admin/users.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            Staff Accounts
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/admin/audit.jsp" class="sidebar-link <%= currentPath.contains("/admin/audit.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Global System Log
        </a></li>
    </ul>

    <!-- Menu Section: Candidate (Active Exam Mode) -->
    <ul class="sidebar-menu role-menu" id="menu-candidate" style="display: none;">
        <li class="menu-label">Active Examination</li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/candidate/enterSBD.jsp" class="sidebar-link <%= currentPath.contains("enterSBD.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
            Enter ID (SBD)
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/candidate/confirmInfo.jsp" class="sidebar-link <%= currentPath.contains("confirmInfo.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            Confirm Info
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/candidate/captureFaceID.jsp" class="sidebar-link <%= currentPath.contains("captureFaceID.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
            Face ID Capture
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/candidate/theoryTest.jsp" class="sidebar-link <%= currentPath.contains("theoryTest.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Active Test
        </a></li>
        <li><a href="<%= request.getContextPath() %>/WEB-INF/views/candidate/result.jsp" class="sidebar-link <%= currentPath.contains("result.jsp") ? "active" : "" %>">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            Immediate Result
        </a></li>
    </ul>

    <!-- Quick Logout Option -->
    <div style="margin-top: auto; padding-top: 1rem; border-top: 1px solid var(--border-color)">
        <a href="<%= request.getContextPath() %>/WEB-INF/views/public/login.jsp" class="sidebar-link" style="color: var(--danger)">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Sign Out
        </a>
    </div>
</aside>

<script>
    // Initialize mock role UI on load
    function initMockRole() {
        const storedRole = localStorage.getItem('mockRole') || '<%= activeRole %>';
        const selectEl = document.getElementById('roleMockSelector');
        if (selectEl) {
            selectEl.value = storedRole;
        }
        
        // Hide all lists and show selected
        document.querySelectorAll('.role-menu').forEach(el => el.style.display = 'none');
        const activeMenuEl = document.getElementById('menu-' + storedRole);
        if (activeMenuEl) {
            activeMenuEl.style.display = 'flex';
        }
    }

    function switchMockRole(role) {
        localStorage.setItem('mockRole', role);
        // Find a suitable landing page for this role
        let targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/public/home.jsp';
        if (role === 'registrant') targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/registrant/dashboard.jsp';
        else if (role === 'staff') targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/staff/managing/dashboard.jsp';
        else if (role === 'examiner') targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/examiner/selectSession.jsp';
        else if (role === 'admin') targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/admin/dashboard.jsp';
        else if (role === 'candidate') targetUrl = '<%= request.getContextPath() %>/WEB-INF/views/candidate/enterSBD.jsp';
        
        window.location.href = targetUrl;
    }

    document.addEventListener('DOMContentLoaded', initMockRole);
</script>
