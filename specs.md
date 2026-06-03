# Use Case Specifications — Public Authentication (DLEM)

---

## UC-01 Register Account

| Field | Details |
| --- | --- |
| **ID and Name** | UC-01 Register Account |
| **Created By** | DLEM Team |
| **Primary Actor** | Guest / Prospective Registrant |
| **Description** | A guest creates a new system account by providing personal information (CCCD, full name, phone, date of birth, address, email). The system creates a `Person` record and a linked `User` account with auto-generated username and password, then emails the credentials to the registrant. |
| **Trigger** | Guest clicks **"Đăng ký"** on the public header or submits the registration form at `/register`. |
| **Preconditions** | **PRE-1:** Guest is not required to be logged in.<br>**PRE-2:** Registration page is accessible at `/register`.<br>**PRE-3:** Database connection to `DLEM_DB` is available.<br>**PRE-4:** Role **Registrant** exists in the `Role` table.<br>**PRE-5:** SMTP configuration in `email.properties` is valid. |
| **Postconditions** | **POST-1:** A new `Person` record is created with `approvalStatus = Pending`.<br>**POST-2:** A new active `User` record is linked to the `Person`, assigned **Registrant** role.<br>**POST-3:** Username is auto-generated (e.g. `Nguyễn Văn Bình` → `binhnv738274`).<br>**POST-4:** Random password is generated and emailed to the registrant.<br>**POST-5:** Guest is redirected to the login page with a success flash message. |
| **Normal Flow** | **1.0 Register new account**<br>1. Guest opens the registration page (`GET /register`).<br>2. System displays the registration form (CCCD, full name, phone, date of birth, address, email, gender, terms checkbox).<br>3. Guest fills in all fields, accepts terms, and clicks **"Đăng ký ngay"**.<br>4. System validates required fields and terms acceptance.<br>5. System checks CCCD, email, and phone are not already in use.<br>6. System inserts a new `Person` record.<br>7. System generates a unique username from full name and a random password.<br>8. System creates a new `User` linked to the `Person` with role **Registrant** and `isActive = true`.<br>9. System sends credentials to the registrant's email.<br>10. System stores a success message in session and redirects to `/login`. |
| **Alternative Flows** | **A1 Duplicate CCCD**<br>A1.1. At step 5, CCCD already exists.<br>A1.2. System displays: *"Số CCCD đã được sử dụng."*<br>A1.3. Guest remains on registration page and may correct input.<br><br>**A2 Duplicate email**<br>A2.1. At step 5, email already exists on a `User` or `Person` record.<br>A2.2. System displays: *"Email đã được sử dụng."*<br>A2.3. Guest remains on registration page and may correct input.<br><br>**A3 Duplicate phone**<br>A3.1. At step 5, phone number already exists.<br>A3.2. System displays: *"Số điện thoại đã được sử dụng."*<br>A3.3. Guest remains on registration page and may correct input. |
| **Exceptions** | **E1 Missing or empty required fields**<br>E1.1. At step 4, one or more required fields are empty.<br>E1.2. System displays: *"Vui lòng nhập đầy đủ thông tin."*<br>E1.3. Use case resumes at step 2.<br><br>**E2 Terms not accepted**<br>E2.1. At step 4, terms checkbox is not checked.<br>E2.2. System displays: *"Bạn phải đồng ý với Điều khoản và Chính sách bảo mật."*<br>E2.3. Use case resumes at step 2.<br><br>**E3 Database insert failure**<br>E3.1. At step 6 or 8, record cannot be saved.<br>E3.2. System displays an appropriate error message.<br>E3.3. Use case resumes at step 2.<br><br>**E4 Email delivery failure**<br>E4.1. At step 9, SMTP send fails.<br>E4.2. System displays: *"Tài khoản đã được tạo nhưng không thể gửi email. Vui lòng liên hệ hỗ trợ."*<br>E4.3. Use case ends. |
| **Priority** | High |
| **Frequency of Use** | Frequently — whenever a new candidate or visitor creates an account. |
| **Business Rules** | **BR-AUTH-01:** Username is auto-generated: given name (lowercase, no accents) + initials of other name parts + 6 random digits (e.g. `binhnv738274`).<br>**BR-AUTH-02:** CCCD, email, and phone must be unique.<br>**BR-AUTH-03:** New self-registered accounts are assigned the **Registrant** role by default.<br>**BR-AUTH-04:** Registration creates both `Person` and linked `User` records.<br>**BR-AUTH-05:** Credentials are sent to the registrant's email; guest does not choose username/password. |
| **Other Information** | Endpoint: `POST /register`. Username generator: `Utils.UsernameGenerator`. Email subject: *"[Lái Vui] Thông tin tài khoản đăng ký"*. |
| **Assumptions** | Guest provides a valid, accessible email address. Guest receives username and password via email and logs in separately. |

---

## UC-02 Login

| Field | Details |
| --- | --- |
| **ID and Name** | UC-02 Login |
| **Created By** | DLEM Team |
| **Primary Actor** | Registered User (any role) |
| **Description** | A registered user authenticates with an identifier (username, email, or phone number) and password. On success, the system creates a session and redirects the user to a role-specific dashboard or home page. |
| **Trigger** | User clicks **"Đăng nhập"** on the public header or submits the login form at `/login`. |
| **Preconditions** | **PRE-1:** User account exists in the `User` table.<br>**PRE-2:** User account has `isActive = true`.<br>**PRE-3:** Login page is accessible at `/login`.<br>**PRE-4:** Database connection to `DLEM_DB` is available. |
| **Postconditions** | **POST-1:** Valid user session is created with `user` object stored in `HttpSession`.<br>**POST-2:** User is redirected based on role (ManagingStaff, ExamStaff, Examiner, Admin, or Registrant/default).<br>**POST-3:** Invalid attempts do not create an authenticated session. |
| **Normal Flow** | **1.0 Authenticate user**<br>1. User opens the login page (`GET /login`).<br>2. System displays the login form (identifier, password). Any flash success/error messages from a prior action are shown once.<br>3. User enters identifier and password and clicks **"Đăng nhập"**.<br>4. System validates that both fields are provided.<br>5. System looks up the user by username, email (`User.email` or `Person.email`), or phone number (`Person.phoneNo`).<br>6. System verifies the account is active and the password matches.<br>7. System stores the authenticated `User` (with role) in session.<br>8. System redirects by role:<br>&nbsp;&nbsp;&nbsp;&nbsp;• **ManagingStaff** → managing staff dashboard<br>&nbsp;&nbsp;&nbsp;&nbsp;• **ExamStaff** → exam staff dashboard<br>&nbsp;&nbsp;&nbsp;&nbsp;• **Examiner** → examiner dashboard<br>&nbsp;&nbsp;&nbsp;&nbsp;• **Admin** → admin dashboard<br>&nbsp;&nbsp;&nbsp;&nbsp;• **Registrant / other** → registrant dashboard |
| **Alternative Flows** | **A1 Login after successful registration**<br>A1.1. User arrives at login from registration redirect with session success message.<br>A1.2. System displays: *"Đăng ký thành công! Tên đăng nhập và mật khẩu đã được gửi tới email của bạn."*<br>A1.3. User continues from step 3 of the normal flow.<br><br>**A2 Login redirected from protected area**<br>A2.1. User attempted to access a staff-only page without a session.<br>A2.2. System stores an error message and redirects to `/login`.<br>A2.3. User continues from step 2 of the normal flow. |
| **Exceptions** | **E1 Missing credentials**<br>E1.1. At step 4, identifier or password is empty.<br>E1.2. System displays: *"Vui lòng nhập tên đăng nhập/email/SĐT và mật khẩu."*<br>E1.3. Use case resumes at step 2.<br><br>**E2 Invalid credentials or inactive account**<br>E2.1. At step 6, user is not found, password is wrong, or account is inactive.<br>E2.2. System displays: *"Tên đăng nhập/Email/SĐT hoặc mật khẩu không chính xác."*<br>E2.3. Use case resumes at step 2; no session is created. |
| **Priority** | High |
| **Frequency of Use** | Very high — every time a user accesses the system. |
| **Business Rules** | **BR-AUTH-05:** Only active accounts (`isActive = true`) may log in.<br>**BR-AUTH-06:** Login identifier may be username, registered email, or linked phone number.<br>**BR-AUTH-07:** Post-login navigation is determined by the user's assigned role.<br>**BR-AUTH-08:** Staff-only areas require an authenticated session (enforced by `AuthFilter` on `/views/staff/*`). |
| **Other Information** | Endpoint: `POST /login`. Logout is handled separately at `/logout`. Users without a linked `Person` record can still log in with username if the account exists. |
| **Assumptions** | User remembers their credentials. Role and dashboard URLs are correctly configured for each account type. |

---

## UC-03 Forgot Password

| Field | Details |
| --- | --- |
| **ID and Name** | UC-03 Forgot Password |
| **Created By** | DLEM Team |
| **Primary Actor** | Registered User |
| **Description** | A registered user who forgot their password submits their email address. The system generates a temporary 6-digit password, updates the account, and sends the temporary password to the user's email via SMTP. |
| **Trigger** | User clicks **"Quên mật khẩu?"** on the login page or submits the recovery form at `/forgot-password`. |
| **Preconditions** | **PRE-1:** User account exists and is associated with the submitted email (`User.email` or linked `Person.email`).<br>**PRE-2:** SMTP configuration in `email.properties` is valid and the mail server is reachable.<br>**PRE-3:** Forgot-password page is accessible at `/forgot-password`.<br>**PRE-4:** Database connection to `DLEM_DB` is available. |
| **Postconditions** | **POST-1:** User's password in the database is replaced with a new temporary password.<br>**POST-2:** An email containing the temporary password is sent to the submitted address.<br>**POST-3:** User sees a success confirmation on the forgot-password page.<br>**POST-4:** User must log in with the temporary password and change it in account settings (future UC). |
| **Normal Flow** | **1.0 Reset password via email**<br>1. User opens the forgot-password page (`GET /forgot-password`).<br>2. System displays the email recovery form.<br>3. User enters their registered email and clicks submit.<br>4. System validates the email field is not empty.<br>5. System finds the account by `User.email` or by identifier lookup (email / linked `Person`).<br>6. System generates a random 6-digit temporary password.<br>7. System updates the user's password in the database.<br>8. System sends a plain-text email with subject *"[Lái Vui] Khôi phục mật khẩu tài khoản"* containing the temporary password.<br>9. System displays: *"Mật khẩu tạm thời đã được gửi tới email của bạn."* |
| **Alternative Flows** | **A1 Account found via Person email only**<br>A1.1. At step 5, email is not on `User.email` but matches a linked `Person.email` through identifier lookup.<br>A1.2. System continues from step 6 of the normal flow. |
| **Exceptions** | **E1 Empty email**<br>E1.1. At step 4, email is missing or blank.<br>E1.2. System displays: *"Vui lòng nhập địa chỉ email."*<br>E1.3. Use case resumes at step 2.<br><br>**E2 Account not found**<br>E2.1. At step 5, no user is associated with the email.<br>E2.2. System displays: *"Không tìm thấy tài khoản"*<br>E2.3. Use case resumes at step 2.<br><br>**E3 Database update failure**<br>E3.1. At step 7, password cannot be saved.<br>E3.2. System displays: *"Không thể cập nhật mật khẩu khôi phục. Vui lòng thử lại."*<br>E3.3. Use case resumes at step 2.<br><br>**E4 Email delivery failure**<br>E4.1. At step 8, SMTP send fails (invalid credentials, network error, etc.).<br>E4.2. System displays: *"Không thể gửi email khôi phục mật khẩu. Vui lòng thử lại."*<br>E4.3. Note: password may already have been updated in step 7; user should retry or contact support.<br>E4.4. Use case resumes at step 2. |
| **Priority** | High |
| **Frequency of Use** | Occasional — when users forget their password. |
| **Business Rules** | **BR-AUTH-09:** Password recovery is initiated only by a registered email linked to an account.<br>**BR-AUTH-10:** Temporary passwords are 6-digit numeric codes.<br>**BR-AUTH-11:** Recovery notification must be sent via configured SMTP (Gmail App Password in current deployment).<br>**BR-AUTH-12:** User is advised to change the temporary password after logging in. |
| **Other Information** | Endpoint: `POST /forgot-password`. Email service uses Jakarta Mail with `email.properties` (SMTP host, port, sender credentials). Required runtime libraries: `jakarta.mail`, `jakarta.activation-api`, `angus-activation`. |
| **Assumptions** | User has access to the email inbox submitted. SMTP sender account is properly configured with a valid App Password. Email delivery is not blocked by spam filters. |
