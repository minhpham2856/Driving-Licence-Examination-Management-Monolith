| Iteration | Screen/Function | Actor | Description | SRS | SDS | In Charge | Status | Fields | Trans | Level | Conv. LOC | Q | Eval LOC | Raw LOC | Notes |
| --------- | --------------- | ----- | ----------- | --- | --- | --------- | ------ | ------ | ----- | ----- | --------- | - | -------- | ------- | ----- |
| Iter1 | Home | Guest | Landing page with CTA and info sections | Y | Y | Minh | Done | 5 | 0 | L1 | 60 | 0.5 | 30 | 805 | `home.jsp`, `HomeServlet`, `landing.css` |
| Iter1 | Login | All roles | Authenticate by username/email/phone + password | Y | Y | Minh | Done | 2 | 2 | L2 | 90 | 0.75 | 68 | 590 | `login.jsp`, `LoginServlet`, unhappy paths in servlet |
| Iter1 | Register Account | Registrant | Create account; auto username/password via email | Y | Y | Minh | Done | 8 | 3 | L4 | 150 | 0.75 | 112 | 643 | `register.jsp`, `RegisterServlet`, duplicate checks |
| Iter1 | Forgot Password | Registrant | Reset password via email (6-digit temp password) | Y | Y | Minh | Done | 1 | 3 | L3 | 120 | 0.75 | 90 | 396 | `forgot-password.jsp`, `ForgotPasswordServlet` |
| Iter1 | License Categories | Guest | Display GPLX categories and requirements | Y | Y | Minh | Done | 6 | 0 | L2 | 90 | 0.5 | 45 | 612 | `license-categories.jsp`, FE-only display |
| Iter1 | Exam Process | Guest | Explain registration/exam process steps | Y | Y | Minh | Done | 4 | 0 | L1 | 60 | 0.5 | 30 | 489 | `process.jsp`, informational |
| Iter2 | Examiner Login (shared) | Examiner | Uses public login; role redirect to examiner dashboard | Y | Y | Minh | Done | 2 | 2 | L2 | 90 | 0.75 | 68 | — | Shared with `LoginServlet` |
| Iter2 | View Dashboard | Examiner | Dashboard with search, summary, candidate table | Y | Y | Minh | Done | 12 | 2 | L5 | 180 | 0.5 | 90 | 286 | `dashboard.jsp`; FE mock, no live DB |
| Iter2 | Call Candidate | Examiner | Queue list + call/print actions | Y | Y | Minh | Done | 10 | 1 | L4 | 150 | 0.5 | 75 | 163 | `candidate-call.jsp`; FE mock |
| Iter2 | Candidate List (Edit Info) | Examiner | Search + list candidates for info correction | Y | Y | Minh | Done | 10 | 1 | L4 | 150 | 0.5 | 75 | 115 | `candidate-details.jsp` |
| Iter2 | Candidate Detail View | Examiner | Read-only candidate profile + exam summary | Y | Y | Minh | Done | 14 | 0 | L5 | 180 | 0.5 | 90 | 124 | `candidate-details-edit.jsp` |
| Iter2 | Exam Paper View | Examiner | 35-question paper with image content + filters | Y | Y | Minh | Done | 18 | 0 | L7 | 240 | 0.5 | 120 | 284 | `candidate-paper.jsp`; static SQL image URLs |
| Iter2 | Results List | Examiner | Search + list candidate results | Y | Y | Minh | Done | 10 | 1 | L4 | 150 | 0.5 | 75 | 125 | `result-details.jsp` |
| Iter2 | Edit / Change Score | Examiner | Adjust score with reason, password, captcha | Y | Y | Minh | Doing | 12 | 2 | L6 | 210 | 0.5 | 105 | 159 | `result-details-edit.jsp`; form only, no BE submit |
| Iter2 | View Audit Log | Examiner | Audit table with search, filter, pagination | Y | Y | Minh | Done | 10 | 2 | L5 | 180 | 0.5 | 90 | 151 | `audit.jsp`; FE mock data |
| Iter2 | Export Reports | Examiner | Export candidates/results/logs to file formats | Y | Y | Minh | Doing | 8 | 1 | L4 | 150 | 0.75 | 112 | 287 | `export.jsp`; Excel candidates wired via `ExportCandidatesExcelServlet` |
| Iter2 | Sidebar Navigation | Examiner | Shared examiner navigation layout | Y | Y | Minh | Done | 6 | 0 | L2 | 90 | 0.75 | 68 | 87 | `sidebar-examiner.jsp`, `header-examiner.jsp` |

**Totals (16 screens/functions)**

| Metric | Value |
| ------ | ----- |
| Converted LOC (complexity sum) | 2,250 |
| Evaluated LOC (after quality Q) | 1,275 |
| Raw LOC (JSP + page CSS + direct Java sampled) | 5,316 |
| Shared auth/DAO stack (additional, not double-counted above) | ~987 |

**Status key:** Done = UI complete (and BE where noted). Doing = partial BE or missing unhappy-path/optimization.
