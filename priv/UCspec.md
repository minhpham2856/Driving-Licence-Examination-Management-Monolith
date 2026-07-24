# example

| ID and Name       | UC-01 Register Account                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Created By        | Pham Nhat Minh                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Primary Actor     | Guest                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Secondary Actors] | None                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Description       | As a guest i want to create a new account by providing a username, email, and password so I cana access the system's authenticated features and access my personalised account with the role of registrant.                                                                                                                                                                                                                                                                                                                                                                                                       |
| Trigger           | Guest submits the registration form at /register.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Preconditions     | PRE-1: Guest is not required to be logged in.<br>PRE-2: Registration page is accessible at /register.<br>PRE-3: Database connection to DLEM_DB is available.<br>PRE-4: Role Registrant exists in the Role table.                                                                                                                                                                                                                                                                                                                                                                                                  |
| Postconditions    | POST-1: A new active User record is created with personId = NULL, assigned Registrant role.<br>POST-2: Username and email are unique in the system.<br>POST-3: Guest is redirected to the login page with a success flash message.                                                                                                                                                                                                                                                                                                                                                                                |
| Normal Flow       | 1.0 Register new account<br>1. Guest opens the registration page<br>2. System displays the registration form (username, email, password, confirm password, terms checkbox).<br>3. Guest fills in all fields, accepts terms, and clicks "Đăng ký ngay".<br>4. System validates required fields, terms acceptance, and password confirmation match.<br>5. System checks username and email are not already in use (User or Person).<br>6. System creates a new User with username, email, password, role Registrant, and isActive = true.<br>7. System stores a success message in session and redirects to /login. |
| Alternative Flows | A1 Duplicate username<br>A1.1. At step 5, username already exists.<br>A1.2. System displays: "Tên đăng nhập đã tồn tại."<br>A1.3. Guest remains on registration page and may correct input.<br><br>A2 Duplicate email<br>A2.1. At step 5, email already exists on a User or Person record.<br>A2.2. System displays: "Email đã được sử dụng."<br>A2.3. Guest remains on registration page and may correct input.                                                                                                                                                                                                  |
| Exceptions        | E1 Missing or empty required fields<br>E1.1. At step 4, one or more required fields are empty.<br>E1.2. System displays: "Vui lòng điền vào ô trống."<br>E1.3. Use case resumes at step 2.<br><br>E3 Password mismatch<br>E3.1. At step 4, password and confirm password do not match.<br>E3.2. System displays: "Mật khẩu nhập lại không khớp."<br>E3.3. Use case resumes at step 2.<br><br>E4 Database insert failure<br>E4.1. At step 6, account cannot be saved.<br>E4.2. System displays: "Không thể đăng ký tài khoản. Vui lòng thử lại."<br>E4.3. Use case resumes at step 2.                              |

# real

## 2. Use Case Specifications

### 2.1 Information

#### 2.1.1 View Home Page

| ID and Name | UC-01 View Home Page |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Guest |
| Secondary Actors | None |
| Description | Displays the system's main landing page with an overview. |
| Trigger | Guest navigates to /home. |
| Preconditions | PRE-1: Guest is not required to be logged in.<br>PRE-2: Home page is accessible at /home.<br>PRE-3: Database connection to DLEM_DB is available. |
| Postconditions | POST-1: System displays the landing page overview.<br>POST-2: Navigation links to public information pages are available. |
| Normal Flow | 1.0 View home page<br>1. Guest opens /home (HomeServlet).<br>2. System loads public content via general.controller.HomeServlet.<br>3. System forwards to web/views/general/home.jsp.<br>4. Guest views system overview, licence highlights, and navigation links. |
| Alternative Flows | A1 Guest selects another public page<br>A1.1. At step 5, guest clicks a navigation link (e.g. /license-categories, /process).<br>A1.2. System navigates to the selected public page. |
| Exceptions | E1 Database unavailable<br>E1.1. At step 2, database connection fails.<br>E1.2. System displays: "Không thể tải trang. Vui lòng thử lại."<br>E1.3. Use case ends. |

#### 2.1.2 View Licence Details

| ID and Name | UC-02 View Licence Details |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Guest |
| Secondary Actors | None |
| Description | Shows detailed information about different driving license types. |
| Trigger | Guest selects a licence type on /license-categories. |
| Preconditions | PRE-1: Guest is not required to be logged in.<br>PRE-2: Licence categories page is accessible at /license-categories.<br>PRE-3: Licence records exist in the Licence table. |
| Postconditions | POST-1: Detailed licence information is displayed for the selected category.<br>POST-2: Guest remains on the public information area. |
| Normal Flow | 1.0 View licence details<br>1. Guest opens /license-categories (LicenceCategoriesServlet).<br>2. System loads licence categories from LicenceService.<br>3. Guest selects a licence type to view details.<br>4. System displays fees, requirements, and description for the selected licence. |
| Alternative Flows | A1 Guest returns to home<br>A1.1. At step 5, guest clicks Home.<br>A1.2. System navigates to /home. |
| Exceptions | E1 Licence not found<br>E1.1. At step 4, selected licence id is invalid.<br>E1.2. System displays: "Không tìm thấy hạng giấy phép."<br>E1.3. Use case resumes at step 2. |

#### 2.1.3 View Licence Categories

| ID and Name | UC-03 View Licence Categories |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Guest |
| Secondary Actors | None |
| Description | Lists all available driving license categories (e.g., A, B, C). |
| Trigger | Guest navigates to /license-categories. |
| Preconditions | PRE-1: Guest is not required to be logged in.<br>PRE-2: Licence categories page is accessible at /license-categories. |
| Postconditions | POST-1: All active licence categories are listed.<br>POST-2: Guest can navigate to licence details. |
| Normal Flow | 1.0 View licence categories<br>1. Guest opens /license-categories (LicenceCategoriesServlet).<br>2. System queries LicenceDAO for all licence categories.<br>3. System forwards to web/views/general/license-categories.jsp with the category list.<br>4. Guest views available categories (A, B, C, etc.). |
| Alternative Flows | None |
| Exceptions | E1 Empty catalogue<br>E1.1. At step 3, no licence categories are returned.<br>E1.2. System displays: "Chưa có hạng giấy phép nào."<br>E1.3. Use case ends. |

#### 2.1.4 View Process Page

| ID and Name | UC-04 View Process Page |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Guest |
| Secondary Actors | None |
| Description | Explains the step-by-step process for obtaining a driving license. |
| Trigger | Guest navigates to /process. |
| Preconditions | PRE-1: Guest is not required to be logged in.<br>PRE-2: Process page is accessible at /process. |
| Postconditions | POST-1: Step-by-step licence acquisition process is displayed. |
| Normal Flow | 1.0 View process page<br>1. Guest opens /process (ProcessServlet).<br>2. System forwards to web/views/general/process.jsp.<br>3. Guest reads the step-by-step process for obtaining a driving licence. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.1.5 View Dashboard

| ID and Name | UC-09 View Dashboard |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Displays a summary of the registrant's status, upcoming exams, and notifications. |
| Trigger | Registrant navigates to /views/registrant/dashboard.jsp. |
| Preconditions | PRE-1: Registrant is logged in. |
| Postconditions | POST-1: Dashboard summary of registrations, exams, and notifications is displayed. |
| Normal Flow | 1.0 View registrant dashboard<br>1. Registrant opens registrant dashboard after login.<br>2. System loads profile summary, upcoming exams, and registration status.<br>3. Registrant reviews personal status and quick links. |
| Alternative Flows | None |
| Exceptions | E1 Session expired<br>E1.1. At step 1, session is missing or expired.<br>E1.2. System redirects to /login.<br>E1.3. System displays: "Phiên đăng nhập đã hết hạn." |

#### 2.1.6 View Exam Scores

| ID and Name | UC-16 View Exam Scores |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Allows a registrant to see their scores from previous exams. |
| Trigger | Registrant views scores on /views/registrant/my-exams.jsp. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Completed exam results exist. |
| Postconditions | POST-1: Theory and practical scores are displayed. |
| Normal Flow | 1.0 View exam scores<br>1. Registrant opens my-exams or score detail view.<br>2. System loads ExamResult and ExamScore records.<br>3. Registrant reviews past exam scores and pass/fail outcome. |
| Alternative Flows | None |
| Exceptions | E1 No results yet<br>E1.1. At step 2, no finalized results exist.<br>E1.2. System displays: "Chưa có kết quả thi."<br>E1.3. Use case ends. |

#### 2.1.7 View Exam Details

| ID and Name | UC-17 View Exam Details |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Provides detailed information about a specific exam session. |
| Trigger | Registrant selects a specific exam from /views/registrant/my-exams.jsp. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Selected enrollment exists. |
| Postconditions | POST-1: Detailed exam session information is displayed. |
| Normal Flow | 1.0 View exam details<br>1. Registrant selects an exam from the registration list.<br>2. System loads exam session, licence category, area, and status.<br>3. Registrant reviews detailed exam information. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.1.8 View Dashboard

| ID and Name | UC-25 View Dashboard |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | Exam Staff, Examiner, Admin |
| Description | Shows a overview of data and information based on role. |
| Trigger | Authenticated user opens role-specific dashboard. |
| Preconditions | PRE-1: User is logged in with a valid role. |
| Postconditions | POST-1: Role-appropriate summary dashboard is displayed. |
| Normal Flow | 1.0 View role dashboard<br>1. User logs in via /login or /staff/login.<br>2. System routes to dashboard by role:<br>3. Registrant -> /views/registrant/dashboard.jsp;<br>4. Exam Staff -> /views/staff/examstaff/dashboard (DashboardServlet);<br>5. Examiner -> /views/examiner/dashboard (ExaminerDashboardServlet);<br>6. Managing Staff -> /views/staff/managing/dashboard.jsp;<br>7. Admin -> /admin/dashboard.<br>8. User reviews summary metrics and navigation. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.1.9 View Audit Log

| ID and Name | UC-26 View Audit Log |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows staff to view a log of system actions for tracking and security. |
| Trigger | Managing Staff opens /views/staff/examstaff/audit. |
| Preconditions | PRE-1: Managing Staff is logged in via /staff/login.<br>PRE-2: Audit records exist in Audit table. |
| Postconditions | POST-1: Filtered audit log entries are displayed. |
| Normal Flow | 1.0 View audit log (Managing Staff)<br>1. Managing Staff navigates to /views/staff/examstaff/audit (AuditServlet).<br>2. System loads audit entries via AuditService.<br>3. Managing Staff filters/searches actions by user, date, or action type.<br>4. System displays audit log table. |
| Alternative Flows | A1 Export audit log<br>A1.1. At step 4, staff clicks export.<br>A1.2. System generates file via /views/staff/examstaff/audit-export. |
| Exceptions | None |

#### 2.1.10 View Audit Log

| ID and Name | UC-56 View Audit Log |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Provides a complete overview of all system activities for security auditing. |
| Trigger | Admin opens /views/examiner/audit. |
| Preconditions | PRE-1: Admin is logged in via /staff/login.<br>PRE-2: Audit records exist. |
| Postconditions | POST-1: Complete system audit log overview is displayed. |
| Normal Flow | 1.0 View audit log (Admin)<br>1. Admin navigates to /views/examiner/audit (ExaminerMiscServlet).<br>2. System loads full audit history via AuditService.<br>3. Admin filters by user, action, and date range.<br>4. System displays security audit overview. |
| Alternative Flows | A1 Export audit<br>A1.1. Admin exports audit data.<br>A1.2. System generates export via /examiner/export/audit. |
| Exceptions | None |

### 2.2 Authentication

#### 2.2.1 Register Registrant Account

| ID and Name | UC-05 Register Registrant Account |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Guest |
| Secondary Actors | None |
| Description | Allows a guest to create a new user account. |
| Trigger | Guest submits the registration form at /register. |
| Preconditions | PRE-1: Guest is not required to be logged in.<br>PRE-2: Registration page is accessible at /register.<br>PRE-3: Database connection to DLEM_DB is available.<br>PRE-4: Role Registrant exists in the Role table. |
| Postconditions | POST-1: A new active User record is created with Registrant role.<br>POST-2: Username and email are unique in the system.<br>POST-3: Guest is redirected to /login with a success flash message. |
| Normal Flow | 1.0 Register registrant account<br>1. Guest opens /register (RegisterServlet).<br>2. System displays the registration form.<br>3. Guest submits username, email, password, confirm password, and accepts terms.<br>4. System validates required fields and password confirmation.<br>5. System checks username and email uniqueness via AuthService.<br>6. System creates User with Registrant role and isActive = true.<br>7. System stores success message in session and redirects to /login. |
| Alternative Flows | A1 Duplicate username<br>A1.1. At step 6, username already exists.<br>A1.2. System displays: "Tên đăng nhập đã tồn tại."<br>A1.3. Guest remains on /register.<br><br>A2 Duplicate email<br>A2.1. At step 6, email already exists.<br>A2.2. System displays: "Email đã được sử dụng."<br>A2.3. Guest remains on /register. |
| Exceptions | E1 Missing required fields<br>E1.1. At step 5, required fields are empty.<br>E1.2. System displays: "Vui lòng điền vào ô trống."<br>E1.3. Use case resumes at step 2.<br><br>E2 Password mismatch<br>E2.1. At step 5, password and confirm password do not match.<br>E2.2. System displays: "Mật khẩu nhập lại không khớp."<br>E2.3. Use case resumes at step 2. |

#### 2.2.2 Login

| ID and Name | UC-06 Login |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Authenticates a registrant to access their personalized area. |
| Trigger | Registrant submits credentials at /login. |
| Preconditions | PRE-1: Registrant account exists and is active.<br>PRE-2: Login page is accessible at /login. |
| Postconditions | POST-1: Registrant session is created with user profile.<br>POST-2: Registrant is redirected to /views/registrant/dashboard.jsp. |
| Normal Flow | 1.0 Login as registrant<br>1. Registrant opens /login (auth.controller.general.LoginServlet).<br>2. Registrant enters username and password and submits.<br>3. System validates credentials via AuthService.<br>4. System stores user in session and redirects to registrant dashboard. |
| Alternative Flows | A1 Invalid credentials<br>A1.1. At step 4, username or password is incorrect.<br>A1.2. System displays: "Tên đăng nhập hoặc mật khẩu không đúng."<br>A1.3. Registrant remains on /login. |
| Exceptions | E1 Account inactive<br>E1.1. At step 4, account is locked or inactive.<br>E1.2. System displays: "Tài khoản đã bị khóa."<br>E1.3. Use case ends. |

#### 2.2.3 Logout

| ID and Name | UC-07 Logout |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Securely ends a registrant's active session. |
| Trigger | Registrant selects logout at /logout. |
| Preconditions | PRE-1: Registrant has an active session. |
| Postconditions | POST-1: Session is invalidated.<br>POST-2: Registrant is redirected to /home or /login. |
| Normal Flow | 1.0 Logout registrant<br>1. Registrant clicks logout.<br>2. System invokes /logout (LogoutServlet).<br>3. System invalidates the HTTP session.<br>4. System redirects to public landing page. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.2.4 Change Password

| ID and Name | UC-08 Change Password |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Enables a registrant to update their own login password. |
| Trigger | Registrant submits the change-password form at /change-password. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Change password page is accessible at /change-password. |
| Postconditions | POST-1: Password hash is updated in the User record.<br>POST-2: Success message is shown to the registrant. |
| Normal Flow | 1.0 Change password<br>1. Registrant opens /change-password (ChangePasswordServlet).<br>2. Registrant enters current password, new password, and confirmation.<br>3. System validates current password and new password rules.<br>4. System updates password via AuthService.<br>5. System displays success and keeps registrant logged in. |
| Alternative Flows | A1 Wrong current password<br>A1.1. At step 4, current password is incorrect.<br>A1.2. System displays: "Mật khẩu hiện tại không đúng."<br>A1.3. Use case resumes at step 2. |
| Exceptions | E1 Password mismatch<br>E1.1. At step 4, new password and confirmation differ.<br>E1.2. System displays: "Mật khẩu nhập lại không khớp."<br>E1.3. Use case resumes at step 2. |

#### 2.2.5 Login / Logout / Change Password

| ID and Name | UC-23 Login / Logout / Change Password |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | Exam Staff, Examiner, Admin |
| Description | Standard authentication actions for staff/ internal members. |
| Trigger | Staff member uses /staff/login, /staff/logout, or /change-password. |
| Preconditions | PRE-1: Staff account exists with appropriate role.<br>PRE-2: Staff login page is accessible at /staff/login. |
| Postconditions | POST-1: Staff session is created, ended, or password updated.<br>POST-2: Staff is redirected to role dashboard (examstaff/examiner/admin). |
| Normal Flow | 1.0 Staff authentication actions<br>1. Staff opens /staff/login (auth.controller.internal.LoginServlet).<br>2. Staff submits credentials; system validates role (Managing Staff, Exam Staff, Examiner, Admin).<br>3. System creates session and redirects to role dashboard.<br>4. For logout, staff invokes /staff/logout.<br>5. For password change, staff uses /change-password while logged in. |
| Alternative Flows | A1 Logout<br>A1.1. Staff selects logout.<br>A1.2. System invalidates session via /staff/logout.<br>A1.3. Staff is redirected to /staff/login. |
| Exceptions | E1 Unauthorized role<br>E1.1. At step 3, account lacks staff role.<br>E1.2. System displays: "Bạn không có quyền truy cập."<br>E1.3. Use case ends. |

#### 2.2.6 Recover Password

| ID and Name | UC-24 Recover Password |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows a staff member to reset their password if forgotten. |
| Trigger | Staff submits forgot-password form at /forgot-password. |
| Preconditions | PRE-1: Staff account email exists in the system.<br>PRE-2: Mail SMTP configuration is available. |
| Postconditions | POST-1: Password reset token or temporary password is sent by email.<br>POST-2: Staff can log in with new credentials at /staff/login. |
| Normal Flow | 1.0 Recover password<br>1. Staff opens /forgot-password (ForgotPasswordServlet).<br>2. Staff enters registered email and submits.<br>3. System validates email and generates reset credentials.<br>4. System sends recovery email via EmailService.<br>5. Staff receives email and resets password. |
| Alternative Flows | None |
| Exceptions | E1 Email not found<br>E1.1. At step 3, email is not registered.<br>E1.2. System displays: "Email không tồn tại trong hệ thống."<br>E1.3. Use case resumes at step 2.<br><br>E2 Email send failure<br>E2.1. At step 4, SMTP send fails.<br>E2.2. System displays: "Không thể gửi email. Vui lòng thử lại."<br>E2.3. Use case ends. |

### 2.3 Profile Management

#### 2.3.1 Update Personal Information

| ID and Name | UC-10 Update Personal Information |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Allows a registrant to edit their own contact and personal details. |
| Trigger | Registrant submits personal information at /views/registrant/profile.jsp. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Profile page is accessible. |
| Postconditions | POST-1: Person/Profile records are updated.<br>POST-2: Updated information is shown on the profile page. |
| Normal Flow | 1.0 Update personal information<br>1. Registrant opens /views/registrant/profile.jsp.<br>2. System displays current personal and contact details.<br>3. Registrant edits fields and submits the form.<br>4. System validates input and persists changes via ProfileService.<br>5. System confirms update to the registrant. |
| Alternative Flows | None |
| Exceptions | E1 Validation failure<br>E1.1. At step 4, required fields are invalid.<br>E1.2. System displays: "Thông tin cá nhân không hợp lệ."<br>E1.3. Use case resumes at step 2. |

#### 2.3.2 Manage Documents

| ID and Name | UC-11 Manage Documents |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Enables upload, viewing, and updating of required documents (e.g., ID, medical certificate). |
| Trigger | Registrant uploads or updates documents at /views/registrant/upload-documents.jsp. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Required document types are configured. |
| Postconditions | POST-1: Document metadata and files are stored.<br>POST-2: Document list reflects the latest uploads. |
| Normal Flow | 1.0 Manage documents<br>1. Registrant opens /views/registrant/upload-documents.jsp.<br>2. System lists required documents and current upload status.<br>3. Registrant selects file(s) and submits upload.<br>4. System validates file type/size and stores document records.<br>5. System refreshes document status on the page. |
| Alternative Flows | A1 Replace existing document<br>A1.1. At step 3, registrant chooses to replace an uploaded document.<br>A1.2. System overwrites prior file reference after validation.<br>A1.3. Use case continues at step 5. |
| Exceptions | E1 Invalid file<br>E1.1. At step 4, file type or size is not allowed.<br>E1.2. System displays: "Tệp tải lên không hợp lệ."<br>E1.3. Use case resumes at step 2. |

#### 2.3.3 Update Documents

| ID and Name | UC-30 Update Documents |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows staff to update or correct a registrant's uploaded documents. |
| Trigger | Managing Staff updates registrant documents from staff document review. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Registrant profile and documents exist. |
| Postconditions | POST-1: Document records are corrected or replaced.<br>POST-2: Registrant document status is updated. |
| Normal Flow | 1.0 Update documents (staff)<br>1. Managing Staff opens registrant document review.<br>2. Staff uploads corrected document or updates metadata.<br>3. System validates and persists document changes.<br>4. System records audit entry and updates registrant status. |
| Alternative Flows | None |
| Exceptions | E1 Invalid document<br>E1.1. At step 3, file fails validation.<br>E1.2. System displays: "Tệp tài liệu không hợp lệ."<br>E1.3. Use case resumes at step 2. |

### 2.4 Exam Registration

#### 2.4.1 Register for Exam

| ID and Name | UC-12 Register for Exam |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Enables a registrant to sign up for a specific exam date and category. |
| Trigger | Registrant submits exam registration at /views/registrant/register-exam.jsp. |
| Preconditions | PRE-1: Registrant is logged in with complete profile.<br>PRE-2: An open exam session exists for the selected licence category. |
| Postconditions | POST-1: ExamEnrollment record is created with pending status.<br>POST-2: Registrant sees confirmation of registration request. |
| Normal Flow | 1.0 Register for exam<br>1. Registrant opens /views/registrant/register-exam.jsp.<br>2. System lists available exam sessions and licence categories.<br>3. Registrant selects exam date, category, and submits registration.<br>4. System validates eligibility and capacity via RegistrationService.<br>5. System creates ExamEnrollment with pending status.<br>6. System shows confirmation and redirects to registration list. |
| Alternative Flows | A1 Exam full<br>A1.1. At step 5, selected exam has no remaining capacity.<br>A1.2. System displays: "Kỳ thi đã đủ số lượng đăng ký."<br>A1.3. Registrant selects another exam. |
| Exceptions | E1 Incomplete profile<br>E1.1. At step 5, required profile/documents are missing.<br>E1.2. System displays: "Vui lòng hoàn thiện hồ sơ trước khi đăng ký."<br>E1.3. Use case ends. |

#### 2.4.2 View Exam Registrations

| ID and Name | UC-13 View Exam Registrations |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Lists all exams the registrant has signed up for. |
| Trigger | Registrant opens /views/registrant/my-exams.jsp. |
| Preconditions | PRE-1: Registrant is logged in. |
| Postconditions | POST-1: List of registrant exam enrollments is displayed. |
| Normal Flow | 1.0 View exam registrations<br>1. Registrant navigates to /views/registrant/my-exams.jsp.<br>2. System loads all ExamEnrollment records for the registrant.<br>3. Registrant reviews registered exams, dates, and statuses. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.4.3 View Exam Schedule

| ID and Name | UC-14 View Exam Schedule |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Shows the date, time, and location of upcoming exams. |
| Trigger | Registrant views schedule on /views/registrant/my-exams.jsp or dashboard. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Registrant has at least one approved enrollment. |
| Postconditions | POST-1: Exam date, time, and location are displayed. |
| Normal Flow | 1.0 View exam schedule<br>1. Registrant opens exam registration or dashboard page.<br>2. System loads linked Exam and ExamArea details.<br>3. Registrant views scheduled date, time, and location. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.4.4 Track Registration Status

| ID and Name | UC-15 Track Registration Status |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Displays the current processing status of the registrant's application. |
| Trigger | Registrant opens /views/registrant/track-profile.jsp. |
| Preconditions | PRE-1: Registrant is logged in. |
| Postconditions | POST-1: Current registration processing status is displayed. |
| Normal Flow | 1.0 Track registration status<br>1. Registrant opens /views/registrant/track-profile.jsp.<br>2. System loads enrollment and review status history.<br>3. Registrant views pending, approved, rejected, or on-hold status. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.4.5 Request Exam Cancellation

| ID and Name | UC-18 Request Exam Cancellation |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Registrant |
| Secondary Actors | None |
| Description | Allows a registrant to submit a request to cancel their exam registration. |
| Trigger | Registrant submits cancellation request from exam detail or my-exams page. |
| Preconditions | PRE-1: Registrant is logged in.<br>PRE-2: Enrollment is in a cancellable state. |
| Postconditions | POST-1: Cancellation request is recorded.<br>POST-2: Enrollment status reflects cancellation pending or cancelled. |
| Normal Flow | 1.0 Request exam cancellation<br>1. Registrant opens exam detail from /views/registrant/my-exams.jsp.<br>2. Registrant submits cancellation request with reason.<br>3. System validates cancellation window and updates enrollment status.<br>4. System notifies registrant of submitted cancellation request. |
| Alternative Flows | None |
| Exceptions | E1 Cancellation not allowed<br>E1.1. At step 3, exam date is too close or exam already started.<br>E1.2. System displays: "Không thể hủy đăng ký trong giai đoạn này."<br>E1.3. Use case ends. |

#### 2.4.6 Update Registration Status

| ID and Name | UC-28 Update Registration Status |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows a staff member to approve, reject, or hold a registration, with a provided reason. |
| Trigger | Managing Staff updates enrollment status from registration review screen. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Pending ExamEnrollment exists. |
| Postconditions | POST-1: Enrollment status is updated (approved, rejected, on hold).<br>POST-2: Registrant can track new status. |
| Normal Flow | 1.0 Update registration status<br>1. Managing Staff opens pending registration review.<br>2. Staff selects approve, reject, or hold and enters reason.<br>3. System validates status transition via RegistrationService.<br>4. System updates ExamEnrollment and writes audit log.<br>5. System notifies registrant if configured. |
| Alternative Flows | None |
| Exceptions | E1 Missing reason<br>E1.1. At step 3, reject/hold requires reason but none provided.<br>E1.2. System displays: "Vui lòng nhập lý do."<br>E1.3. Use case resumes at step 2. |

### 2.5 Examination

#### 2.5.1 Authenticate Identity

| ID and Name | UC-19 Authenticate Identity |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Candidate |
| Secondary Actors | None |
| Description | Verifies the candidate's identity using FaceID or another method before starting. |
| Trigger | Exam Staff captures candidate photo at /views/staff/examstaff/candidate-photo. |
| Preconditions | PRE-1: Exam Staff is logged in via /staff/login.<br>PRE-2: Active exam session is selected in examstaff context.<br>PRE-3: Candidate is registered for the session. |
| Postconditions | POST-1: Candidate identity photo is stored.<br>POST-2: Candidate is marked ready for check-in/procedure. |
| Normal Flow | 1.0 Authenticate candidate identity<br>1. Exam Staff opens /views/staff/examstaff/candidate-photo (CandidatePhotoServlet).<br>2. Exam Staff selects candidate and captures/uploads identity photo.<br>3. System validates candidate enrollment for active exam.<br>4. System stores photo and updates candidate verification status. |
| Alternative Flows | None |
| Exceptions | E1 Candidate not found<br>E1.1. At step 3, candidate id is invalid for active exam.<br>E1.2. System displays: "Không tìm thấy thí sinh."<br>E1.3. Use case resumes at step 2. |

#### 2.5.2 Take Exam

| ID and Name | UC-21 Take Exam |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Candidate |
| Secondary Actors | None |
| Description | The main process for a candidate to complete their driving exam. |
| Trigger | Candidate begins exam after call from /views/staff/examstaff/candidatecall. |
| Preconditions | PRE-1: Candidate is checked in and payment confirmed.<br>PRE-2: Exam session status is In Progress. |
| Postconditions | POST-1: Candidate completes theory and/or practical sections.<br>POST-2: ExamResult and scores are stored. |
| Normal Flow | 1.0 Take exam<br>1. Exam Staff calls candidate via /views/staff/examstaff/candidatecall.<br>2. Candidate proceeds to theory station or practical exam area.<br>3. Examiner manages scoring at /views/examiner/score-entry and /views/examiner/exam.<br>4. System records answers, deductions, and final result. |
| Alternative Flows | A1 Theory only session<br>A1.1. At step 3, exam section is Theory.<br>A1.2. System generates paper via /views/examiner/exam.<br>A1.3. Candidate completes theory and result is finalized. |
| Exceptions | E1 Candidate not called<br>E1.1. At step 2, candidate is not in active queue.<br>E1.2. System displays: "Thí sinh chưa được gọi thi."<br>E1.3. Use case ends. |

#### 2.5.3 Save Answers During Exam

| ID and Name | UC-22 Save Answers During Exam |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Candidate |
| Secondary Actors | None |
| Description | Allows a candidate to save their progress and answers during the exam. |
| Trigger | Candidate submits or auto-saves answers during active exam session. |
| Preconditions | PRE-1: Candidate exam session is in progress.<br>PRE-2: Theory paper or practical score entry is active. |
| Postconditions | POST-1: Answers or interim scores are persisted.<br>POST-2: Candidate can resume from saved state. |
| Normal Flow | 1.0 Save answers during exam<br>1. Candidate answers question or examiner records interim score.<br>2. System saves CandidateAnswer or ExamScore via examiner/examstaff services.<br>3. System confirms save without ending the session. |
| Alternative Flows | None |
| Exceptions | E1 Save failure<br>E1.1. At step 2, database write fails.<br>E1.2. System displays: "Không thể lưu câu trả lời. Vui lòng thử lại."<br>E1.3. Use case resumes at step 1. |

#### 2.5.4 Import Candidate List

| ID and Name | UC-31 Import Candidate List |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Enables bulk import of candidate information from an external file. |
| Trigger | Managing Staff uploads candidate file at /staff/examstaff/upload. |
| Preconditions | PRE-1: Managing Staff or Exam Staff is logged in.<br>PRE-2: Target exam session is selected.<br>PRE-3: Upload template format is valid. |
| Postconditions | POST-1: Candidate rows are imported into the exam roster.<br>POST-2: Import summary with success/error counts is displayed. |
| Normal Flow | 1.0 Import candidate list<br>1. Staff opens /staff/examstaff/upload (ExamStaffUploadServlet).<br>2. Staff selects exam and uploads Excel file.<br>3. System parses rows and validates required fields.<br>4. System bulk-inserts Candidate and ExamEnrollment records.<br>5. System displays import result summary. |
| Alternative Flows | A1 Partial import<br>A1.1. At step 4, some rows fail validation.<br>A1.2. System imports valid rows and lists errors per row.<br>A1.3. Staff corrects file and re-uploads. |
| Exceptions | E1 Invalid file format<br>E1.1. At step 3, file is not a supported Excel template.<br>E1.2. System displays: "Định dạng tệp không hợp lệ."<br>E1.3. Use case resumes at step 2. |

#### 2.5.5 Check In Candidates

| ID and Name | UC-36 Check In Candidates |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Confirms a candidate's physical presence on the exam day. |
| Trigger | Exam Staff checks in candidate at /views/staff/examstaff/candidatecall. |
| Preconditions | PRE-1: Exam Staff is logged in.<br>PRE-2: Active exam session is in progress or ready.<br>PRE-3: Candidate is on the exam roster. |
| Postconditions | POST-1: Candidate check-in status is updated.<br>POST-2: Candidate appears in call queue. |
| Normal Flow | 1.0 Check in candidates<br>1. Exam Staff opens /views/staff/examstaff/candidatecall (CandidateCallServlet).<br>2. Staff confirms candidate presence and identity.<br>3. System updates candidate status to checked-in.<br>4. System adds candidate to active call queue. |
| Alternative Flows | None |
| Exceptions | E1 Candidate absent<br>E1.1. At step 3, candidate did not arrive.<br>E1.2. System marks absent via /views/staff/examstaff/procedure.<br>E1.3. Use case branches to UC-40. |

#### 2.5.6 Process Candidate Profile

| ID and Name | UC-37 Process Candidate Profile |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Reviews and prepares a candidate's file/profle before they begin the exam. |
| Trigger | Exam Staff opens /views/staff/examstaff/candidate-dossier. |
| Preconditions | PRE-1: Exam Staff is logged in.<br>PRE-2: Candidate is registered for active exam. |
| Postconditions | POST-1: Candidate dossier is reviewed and preparation status updated. |
| Normal Flow | 1.0 Process candidate profile<br>1. Exam Staff opens /views/staff/examstaff/candidate-dossier (CandidateDossierServlet).<br>2. System displays candidate profile, documents, and enrollment data.<br>3. Exam Staff verifies completeness and confirms dossier ready.<br>4. System updates candidate preparation status. |
| Alternative Flows | None |
| Exceptions | E1 Incomplete dossier<br>E1.1. At step 3, required documents are missing.<br>E1.2. System displays: "Hồ sơ thí sinh chưa đầy đủ."<br>E1.3. Use case ends until documents are provided. |

#### 2.5.7 Manage Exam Status

| ID and Name | UC-38 Manage Exam Status |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Allows staff to start, pause, or stop an exam session. |
| Trigger | Exam Staff changes exam status at /views/staff/examstaff/exam-control. |
| Preconditions | PRE-1: Exam Staff is logged in.<br>PRE-2: Exam session exists with valid status transition. |
| Postconditions | POST-1: Exam status is updated (start, pause, stop).<br>POST-2: Examiner and call-board views reflect new status. |
| Normal Flow | 1.0 Manage exam status<br>1. Exam Staff opens /views/staff/examstaff/exam-control (ExamControlServlet).<br>2. Staff selects start, pause, or stop action.<br>3. System validates transition via ExamService.<br>4. System updates Exam status and notifies dependent views. |
| Alternative Flows | None |
| Exceptions | E1 Invalid transition<br>E1.1. At step 3, status change is not allowed.<br>E1.2. System displays: "Không thể thay đổi trạng thái kỳ thi."<br>E1.3. Use case resumes at step 2. |

#### 2.5.8 End Exam

| ID and Name | UC-39 End Exam |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Officially concludes an exam session and finalizes all records. |
| Trigger | Exam Staff ends exam at /views/staff/examstaff/exam-control. |
| Preconditions | PRE-1: Exam session is in progress.<br>PRE-2: All active candidates are finalized or marked absent. |
| Postconditions | POST-1: Exam status is set to Completed.<br>POST-2: Results are locked for export. |
| Normal Flow | 1.0 End exam<br>1. Exam Staff opens /views/staff/examstaff/exam-control.<br>2. Staff confirms end-of-exam action.<br>3. System finalizes open records and sets Exam status to Completed.<br>4. System redirects staff to report/export options. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.5.9 Handle Absent Candidate

| ID and Name | UC-40 Handle Absent Candidate |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Manages the process for candidates who do not show up for the exam. |
| Trigger | Exam Staff marks absent candidate at /views/staff/examstaff/procedure. |
| Preconditions | PRE-1: Exam Staff is logged in.<br>PRE-2: Candidate did not check in before cutoff. |
| Postconditions | POST-1: Candidate is marked absent.<br>POST-2: Exam result reflects absence. |
| Normal Flow | 1.0 Handle absent candidate<br>1. Exam Staff opens /views/staff/examstaff/procedure (ProcedureServlet).<br>2. Staff selects candidate and marks as absent.<br>3. System updates candidate and result status to Absent.<br>4. System removes candidate from active call queue. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.5.10 Allocate Candidates

| ID and Name | UC-42 Allocate Candidates |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Assigns candidates to specific exam stations, rooms, or queues. |
| Trigger | Examiner allocates candidates at /views/examiner/candidate-call. |
| Preconditions | PRE-1: Examiner is logged in via /staff/login.<br>PRE-2: Active exam schedule is selected in examiner session. |
| Postconditions | POST-1: Candidates are assigned to stations/queues.<br>POST-2: Call board reflects allocation order. |
| Normal Flow | 1.0 Allocate candidates<br>1. Examiner opens /views/examiner/candidate-call (ExaminerCandidateCallServlet).<br>2. Examiner selects candidate and target station/section.<br>3. System updates queue order via CallService.<br>4. Public call board at /views/public/public-call reflects allocation. |
| Alternative Flows | None |
| Exceptions | E1 No active exam<br>E1.1. At step 1, examiner session has no selected exam.<br>E1.2. System redirects to /views/examiner/exam-select.<br>E1.3. System displays: "Vui lòng chọn kỳ thi." |

#### 2.5.11 Manage Exam Results

| ID and Name | UC-43 Manage Exam Results |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Allows the examiner to input, update, and finalize candidate scores. |
| Trigger | Examiner enters scores at /views/examiner/score-entry. |
| Preconditions | PRE-1: Examiner is logged in.<br>PRE-2: Candidate is called and exam section is active. |
| Postconditions | POST-1: Scores and deductions are saved.<br>POST-2: Result detail view shows updated outcome. |
| Normal Flow | 1.0 Manage exam results<br>1. Examiner opens /views/examiner/score-entry (ExaminerScoreEntryServlet).<br>2. Examiner records theory answers or practical deductions.<br>3. System persists ExamScore and DeductionRecord via ExamScoreService.<br>4. Examiner reviews result at /views/examiner/result-details.<br>5. System calculates pass/fail and stores ExamResult. |
| Alternative Flows | A1 Edit result<br>A1.1. Examiner opens /views/examiner/result-details-edit.<br>A1.2. System allows authorized score correction before finalize. |
| Exceptions | E1 Save failure<br>E1.1. At step 3, score save fails.<br>E1.2. System displays: "Không thể lưu điểm. Vui lòng thử lại."<br>E1.3. Use case resumes at step 2. |

#### 2.5.12 Cancel Candidate Result

| ID and Name | UC-44 Cancel Candidate Result |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Enables the examiner to void or cancel a candidate's result if necessary. |
| Trigger | Examiner cancels result at /views/examiner/result-details-edit. |
| Preconditions | PRE-1: Examiner is logged in.<br>PRE-2: Candidate result exists and is cancellable. |
| Postconditions | POST-1: Result is voided or marked cancelled.<br>POST-2: Audit log records cancellation. |
| Normal Flow | 1.0 Cancel candidate result<br>1. Examiner opens /views/examiner/result-details-edit.<br>2. Examiner selects cancel/void result and confirms reason.<br>3. System voids ExamResult via ExamScoreService.<br>4. System writes audit entry. |
| Alternative Flows | None |
| Exceptions | E1 Result finalized<br>E1.1. At step 3, result is locked after exam closure.<br>E1.2. System displays: "Không thể hủy kết quả đã chốt."<br>E1.3. Use case ends. |

#### 2.5.13 Create Violation

| ID and Name | UC-45 Create Violation |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Records any rule violations committed by a candidate during the exam. |
| Trigger | Examiner records violation at /views/examiner/violations. |
| Preconditions | PRE-1: Examiner is logged in.<br>PRE-2: Candidate is in active practical exam. |
| Postconditions | POST-1: DeductionRecord/violation is stored.<br>POST-2: Candidate score reflects violation penalty. |
| Normal Flow | 1.0 Create violation<br>1. Examiner opens /views/examiner/violations (ExaminerViolationsServlet).<br>2. Examiner selects violation type and candidate.<br>3. System records deduction via ScoreDeductionDAO.<br>4. Examiner confirms at /views/examiner/violation-confirm.<br>5. System updates running score total. |
| Alternative Flows | A1 Undo violation<br>A1.1. Examiner opens violation undo flow.<br>A1.2. System reverses deduction if allowed. |
| Exceptions | None |

#### 2.5.14 Generate Theory Paper

| ID and Name | UC-46 Generate Theory Paper |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Creates a randomized theory exam paper for the session. |
| Trigger | Examiner generates theory paper at /views/examiner/exam. |
| Preconditions | PRE-1: Examiner is logged in.<br>PRE-2: Theory section is active for candidate.<br>PRE-3: Question bank contains sufficient questions. |
| Postconditions | POST-1: Randomized theory paper is generated.<br>POST-2: Candidate paper view is available. |
| Normal Flow | 1.0 Generate theory paper<br>1. Examiner opens /views/examiner/exam (ExaminerExamServlet).<br>2. Examiner selects candidate for theory section.<br>3. System randomizes questions via TheoryPaperDAO/QuestionDAO.<br>4. System displays paper at /views/examiner/candidate-paper. |
| Alternative Flows | None |
| Exceptions | E1 Insufficient questions<br>E1.1. At step 3, question pool is too small.<br>E1.2. System displays: "Không đủ câu hỏi để tạo đề thi."<br>E1.3. Use case ends. |

### 2.6 Payment Processing

#### 2.6.1 Pay Exam Fee

| ID and Name | UC-20 Pay Exam Fee |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Candidate |
| Secondary Actors | None |
| Description | Enables payment of the exam fee using various methods (QR code, cash). |
| Trigger | Candidate initiates payment from exam registration or check-in flow. |
| Preconditions | PRE-1: Candidate registration is approved.<br>PRE-2: Exam fee amount is configured for licence category. |
| Postconditions | POST-1: Payment record is created with paid or pending status.<br>POST-2: Candidate may proceed to examination when payment confirmed. |
| Normal Flow | 1.0 Pay exam fee<br>1. Candidate selects payment method (QR code or cash).<br>2. System displays payable amount from Licence fee configuration.<br>3. Candidate completes payment or staff confirms cash receipt.<br>4. System records Payment with status Paid.<br>5. System updates enrollment payment status. |
| Alternative Flows | A1 QR payment pending confirmation<br>A1.1. At step 4, online payment awaits gateway confirmation.<br>A1.2. System stores Payment as Pending.<br>A1.3. Candidate is notified when payment is confirmed. |
| Exceptions | E1 Payment failed<br>E1.1. At step 4, payment cannot be processed.<br>E1.2. System displays: "Thanh toán thất bại. Vui lòng thử lại."<br>E1.3. Use case resumes at step 2. |

### 2.7 User Management

#### 2.7.1 Manage Registrants

| ID and Name | UC-27 Manage Registrants |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Enables viewing, searching, and sorting the full list of registered users. |
| Trigger | Managing Staff opens registrant management at /views/staff/managing/registrants.jsp. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Registrant records exist. |
| Postconditions | POST-1: Searchable registrant list is displayed. |
| Normal Flow | 1.0 Manage registrants<br>1. Managing Staff opens registrant list page.<br>2. System loads registrants with pagination, search, and sort.<br>3. Managing Staff searches or sorts the list.<br>4. System refreshes results according to criteria. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.7.2 Register New User

| ID and Name | UC-29 Register New User |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Enables a staff member to manually create a user account on behalf of someone. |
| Trigger | Managing Staff submits new user form from staff user management. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Target role exists in Role table. |
| Postconditions | POST-1: New User and Profile records are created.<br>POST-2: Credentials are communicated to the new user. |
| Normal Flow | 1.0 Register new user (staff)<br>1. Managing Staff opens user creation form.<br>2. Staff enters personal details, role, and contact information.<br>3. System validates uniqueness and required fields.<br>4. System creates account via AuthService/RegistrationService.<br>5. System displays confirmation with generated credentials. |
| Alternative Flows | None |
| Exceptions | E1 Duplicate username<br>E1.1. At step 4, username exists.<br>E1.2. System displays: "Tên đăng nhập đã tồn tại."<br>E1.3. Use case resumes at step 2. |

### 2.8 Exam Management

#### 2.8.1 Create New Exam

| ID and Name | UC-32 Create New Exam |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows a staff member to schedule a new exam session with date, time, and capacity. |
| Trigger | Managing Staff submits create-exam form from exam management. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Licence category and exam area exist. |
| Postconditions | POST-1: New Exam record is created with schedule and capacity.<br>POST-2: Exam appears in management and registration lists. |
| Normal Flow | 1.0 Create new exam<br>1. Managing Staff opens exam creation form.<br>2. Staff enters date, time, licence category, area, and capacity.<br>3. System validates schedule conflicts and capacity.<br>4. System creates Exam via ExamService.<br>5. System confirms creation and shows new exam in list. |
| Alternative Flows | None |
| Exceptions | E1 Schedule conflict<br>E1.1. At step 4, exam area/time conflicts with existing exam.<br>E1.2. System displays: "Lịch thi bị trùng. Vui lòng chọn thời gian khác."<br>E1.3. Use case resumes at step 2. |

#### 2.8.2 Manage Exams

| ID and Name | UC-33 Manage Exams |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Enables viewing, editing, or canceling existing exam sessions. |
| Trigger | Managing Staff opens exam management list. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Exam records exist. |
| Postconditions | POST-1: Exam list reflects view, edit, or cancel actions. |
| Normal Flow | 1.0 Manage exams<br>1. Managing Staff opens exam management page.<br>2. System lists exams with status, date, and capacity.<br>3. Staff views, edits, or cancels an exam session.<br>4. System persists changes via ExamService and audit log. |
| Alternative Flows | A1 Cancel exam<br>A1.1. At step 4, staff cancels exam.<br>A1.2. System sets Exam status to Cancelled.<br>A1.3. Enrolled registrants are notified. |
| Exceptions | None |

#### 2.8.3 Manage Exam Roster

| ID and Name | UC-35 Manage Exam Roster |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Allows staff to view and manage the list of candidates assigned to a specific exam. |
| Trigger | Exam Staff opens /views/staff/examstaff/allocation. |
| Preconditions | PRE-1: Exam Staff is logged in via /staff/login.<br>PRE-2: Active exam is selected at /views/staff/examstaff/select-exam. |
| Postconditions | POST-1: Exam roster with candidate statuses is displayed. |
| Normal Flow | 1.0 Manage exam roster<br>1. Exam Staff selects exam at /views/staff/examstaff/select-exam.<br>2. Exam Staff opens /views/staff/examstaff/allocation (AllocationServlet).<br>3. System loads candidate queue and stage views.<br>4. Exam Staff reviews, filters, and manages roster assignments. |
| Alternative Flows | A1 View stage-specific roster<br>A1.1. Staff navigates to allocation-theory, allocation-practical, or allocation-waiting.<br>A1.2. System filters roster by section stage. |
| Exceptions | None |

### 2.9 Communication

#### 2.9.1 Notify Registrants

| ID and Name | UC-34 Notify Registrants |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Managing Staff |
| Secondary Actors | None |
| Description | Allows staff to send email/SMS notifications about upcoming exams. |
| Trigger | Managing Staff sends notification from communication module. |
| Preconditions | PRE-1: Managing Staff is logged in.<br>PRE-2: Mail SMTP configuration is available.<br>PRE-3: Target registrants are selected. |
| Postconditions | POST-1: Notification emails are queued or sent.<br>POST-2: Send status is recorded. |
| Normal Flow | 1.0 Notify registrants<br>1. Managing Staff composes notification about upcoming exam.<br>2. Staff selects recipient registrants or exam roster.<br>3. System sends email via EmailService.<br>4. System records notification outcome. |
| Alternative Flows | None |
| Exceptions | E1 Send failure<br>E1.1. At step 3, email delivery fails.<br>E1.2. System displays: "Không thể gửi thông báo. Vui lòng thử lại."<br>E1.3. Use case ends. |

### 2.10 File Processing

#### 2.10.1 Export Files

| ID and Name | UC-41 Export Files |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Exam Staff |
| Secondary Actors | None |
| Description | Generates and exports exam-related reports in formats like Excel or Docx. |
| Trigger | Exam Staff requests export at /views/staff/examstaff/report. |
| Preconditions | PRE-1: Exam Staff is logged in.<br>PRE-2: Exam session has reportable data. |
| Postconditions | POST-1: Excel or printable report file is generated.<br>POST-2: File is downloaded or opened for printing. |
| Normal Flow | 1.0 Export files (Exam Staff)<br>1. Exam Staff opens /views/staff/examstaff/report (ReportServlet).<br>2. Staff selects report type and export format.<br>3. System generates Excel/report via ExcelService.<br>4. System returns file download or print view. |
| Alternative Flows | A1 Print view<br>A1.1. At step 3, staff chooses print.<br>A1.2. System opens report-print view. |
| Exceptions | E1 Export failure<br>E1.1. At step 3, report generation fails.<br>E1.2. System displays: "Không thể xuất báo cáo. Vui lòng thử lại."<br>E1.3. Use case ends. |

#### 2.10.2 Print / Export Results Files

| ID and Name | UC-47 Print / Export Results Files |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Examiner |
| Secondary Actors | None |
| Description | Prints or exports final results files for official records. |
| Trigger | Examiner exports or prints results via /examiner/export/result or /examiner/print. |
| Preconditions | PRE-1: Examiner is logged in.<br>PRE-2: Exam session has finalized or draft results. |
| Postconditions | POST-1: Results file is exported or sent to printer. |
| Normal Flow | 1.0 Print/export results files<br>1. Examiner opens /views/examiner/export (ExaminerMiscServlet) or export toolbar.<br>2. Examiner selects export type (Excel/DOCX) or print.<br>3. System generates file via /examiner/export/result (ExportServlet) or /examiner/print (PrintServlet).<br>4. Examiner downloads or prints official results. |
| Alternative Flows | A1 DOCX export<br>A1.1. Examiner selects DOCX template export.<br>A1.2. System generates document via /examiner/export/docx. |
| Exceptions | E1 Export failure<br>E1.1. At step 3, export service fails.<br>E1.2. System displays: "Không thể xuất kết quả. Vui lòng thử lại."<br>E1.3. Use case ends. |

### 2.11 Administration

#### 2.11.1 Manage Accounts

| ID and Name | UC-48 Manage Accounts |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Creates, locks/unlocks, and resets passwords for all user and staff accounts. |
| Trigger | Admin opens account management at /admin/accounts. |
| Preconditions | PRE-1: Admin is logged in via /staff/login.<br>PRE-2: User records exist. |
| Postconditions | POST-1: Account create, lock/unlock, or reset action is applied. |
| Normal Flow | 1.0 Manage accounts<br>1. Admin navigates to /admin/accounts.<br>2. Admin searches user and selects lock, unlock, or reset password.<br>3. System validates admin authorization.<br>4. System updates User record and writes audit log. |
| Alternative Flows | None |
| Exceptions | E1 Unauthorized<br>E1.1. At step 3, actor is not Admin.<br>E1.2. System displays: "Bạn không có quyền quản trị."<br>E1.3. Use case ends. |

#### 2.11.2 Create Staff Accounts

| ID and Name | UC-49 Create Staff Accounts |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Specifically creates accounts for new staff members. |
| Trigger | Admin submits create-staff form at /admin/staff/create. |
| Preconditions | PRE-1: Admin is logged in.<br>PRE-2: Staff roles exist. |
| Postconditions | POST-1: Staff User account is created.<br>POST-2: Credentials are issued. |
| Normal Flow | 1.0 Create staff accounts<br>1. Admin opens staff account creation page.<br>2. Admin enters staff details and assigns role (Exam Staff, Examiner, Managing Staff).<br>3. System validates and creates account.<br>4. System displays generated credentials. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.11.3 Manage Driving Licenses

| ID and Name | UC-50 Manage Driving Licenses |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Adds, modifies, or removes driving license categories and their fees. |
| Trigger | Admin opens licence management at /admin/licences. |
| Preconditions | PRE-1: Admin is logged in. |
| Postconditions | POST-1: Licence catalogue reflects add/modify/remove actions. |
| Normal Flow | 1.0 Manage driving licenses<br>1. Admin opens licence management page.<br>2. Admin adds, edits, or deactivates licence categories.<br>3. System persists Licence records. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.11.4 Modify License Categories

| ID and Name | UC-51 Modify License Categories |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Edits the attributes and requirements of license categories. |
| Trigger | Admin edits licence category attributes at /admin/licences/edit. |
| Preconditions | PRE-1: Admin is logged in.<br>PRE-2: Licence category exists. |
| Postconditions | POST-1: Licence category attributes and requirements are updated. |
| Normal Flow | 1.0 Modify license categories<br>1. Admin selects licence category to edit.<br>2. Admin updates name, requirements, and metadata.<br>3. System validates and saves Licence record. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.11.5 Update License Fees

| ID and Name | UC-52 Update License Fees |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Updates the exam fees associated with each license category. |
| Trigger | Admin updates fees at /admin/licences/fees. |
| Preconditions | PRE-1: Admin is logged in.<br>PRE-2: Licence fee records exist. |
| Postconditions | POST-1: Exam fees are updated for selected categories. |
| Normal Flow | 1.0 Update license fees<br>1. Admin opens fee management for licence categories.<br>2. Admin enters new exam fee amounts.<br>3. System validates numeric input and saves fees. |
| Alternative Flows | None |
| Exceptions | E1 Invalid fee<br>E1.1. At step 3, fee is negative or non-numeric.<br>E1.2. System displays: "Mức phí không hợp lệ."<br>E1.3. Use case resumes at step 2. |

#### 2.11.6 Manage Exam Areas

| ID and Name | UC-53 Manage Exam Areas |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Defines and manages different exam locations and their details. |
| Trigger | Admin manages exam areas at /admin/exam-areas. |
| Preconditions | PRE-1: Admin is logged in. |
| Postconditions | POST-1: ExamArea records are created, updated, or removed. |
| Normal Flow | 1.0 Manage exam areas<br>1. Admin opens exam area management.<br>2. Admin defines location name, address, and capacity details.<br>3. System persists ExamArea via admin service. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.11.7 Manage Exam Devices

| ID and Name | UC-54 Manage Exam Devices |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Adds, updates status, or removes devices used for exams. |
| Trigger | Admin manages devices at /admin/devices. |
| Preconditions | PRE-1: Admin is logged in. |
| Postconditions | POST-1: ExamDevice records reflect add/update/remove actions. |
| Normal Flow | 1.0 Manage exam devices<br>1. Admin opens device management page.<br>2. Admin registers new device or updates device metadata.<br>3. System persists ExamDevice records. |
| Alternative Flows | None |
| Exceptions | None |

#### 2.11.8 Update Device Status

| ID and Name | UC-55 Update Device Status |
| ----------- | -------------------- |
| Created By | TBD |
| Primary Actor | Admin |
| Secondary Actors | None |
| Description | Changes the operational status of exam devices. |
| Trigger | Admin or Examiner updates device status at /views/examiner/devices. |
| Preconditions | PRE-1: Actor is Admin or Examiner with device permissions.<br>PRE-2: ExamDevice record exists. |
| Postconditions | POST-1: Device operational status is updated. |
| Normal Flow | 1.0 Update device status<br>1. Actor opens /views/examiner/devices (ExaminerDevicesServlet) or /admin/devices.<br>2. Actor selects device and new status (available, in use, maintenance).<br>3. System updates ExamDevice status.<br>4. Device grid reflects new status. |
| Alternative Flows | None |
| Exceptions | E1 Device in use<br>E1.1. At step 3, device cannot change status while assigned.<br>E1.2. System displays: "Thiết bị đang được sử dụng."<br>E1.3. Use case resumes at step 2. |
