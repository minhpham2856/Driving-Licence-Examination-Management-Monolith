# Project Tracking (Minh's Scope)

Below is the software screen/function evaluation table for the components under Minh's responsibility (`/auth`, `/general`, `/examiner`, including both frontend and backend).

Complexity level and Converted LOC are calculated based on the following evaluation guide:
- **1 Transaction = 2 Fields**
- **Fields grouping rules:**
  - A form input group (like username + password) counts inputs individually, but a collection of navigation links (like sidebar, header navbar, or footer) is grouped as **1 field**.
  - A table sorting header row is grouped as **1 field**.
  - A table pagination controller is grouped as **1 field**.
  - A set of similar action buttons (like Gọi/Hoàn tác/Đình chỉ per row) is grouped as **1 field**.
- **Transactions grouping rules:**
  - Loading a table or list page from database count as **1 transaction** (regardless of how many internal tables are joined).
  - Loading a details view profile or a single exam paper counts as **1 transaction**.
  - Pages that only forward/redirect without querying database (like login load, static landing pages) count as **0 transactions**.

- Level 1: 3-5 fields or 2 transactions $\rightarrow$ 60 LOC
- Level 2: 6-7 fields or 3 transactions $\rightarrow$ 90 LOC
- Level 3: 8-9 fields or 4 transactions $\rightarrow$ 120 LOC
- Level 4: 10-11 fields or 5 transactions $\rightarrow$ 150 LOC
- Level 5: 12-13 fields or 6 transactions $\rightarrow$ 180 LOC
- Level 6: 14-15 fields or 7 transactions $\rightarrow$ 210 LOC
- Level 7: >15 fields or >7 transactions $\rightarrow$ 240 LOC

| Iteration | Screen/Function | Actor | Description | SRS | SDS | In Charge | Status | Fields | Transactions | LOC | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Iter1 | Login | Registrant | Authenticate by username/email/phone + password | | | Minh | Done | 5 | 0 | 60 | 2 inputs, 1 submit button, 1 navigation header, 1 action link. 0 DB transactions when loading (GET). Complexity Level 1 based on Fields = 5. |
| Iter1 | Register Account | Registrant | Create account; auto username/password via email | | | Minh | Done | 10 | 0 | 150 | 6 inputs, 1 sex select, 1 checkbox terms, 1 submit button, 1 back link. 0 DB transactions when loading (GET). Complexity Level 4 based on Fields = 10. |
| Iter1 | Forgot Password | Registrant | Reset password via email (6-digit temp password) | | | Minh | Done | 3 | 0 | 60 | 1 input, 1 submit button, 1 back link. 0 DB transactions when loading (GET). Complexity Level 1 based on Fields = 3. |
| Iter1 | Home | Guest | Landing page with CTA and info sections | | | Minh | Done | 3 | 0 | 60 | 1 header navbar, 1 footer navbar, 1 main CTA buttons group. 0 DB transactions. Complexity Level 1 based on Fields = 3. |
| Iter1 | License Categories | Guest | Display GPLX categories and requirements | | | Minh | Done | 6 | 1 | 90 | 1 search input, 2 filter checkbox groups, 1 sort dropdown, 1 submit button, 1 nav bar. 1 DB transaction (load list). Complexity Level 2 based on Fields = 6. |
| Iter1 | Exam Process | Guest | Explain registration/exam process steps | | | Minh | Done | 3 | 0 | 60 | 1 header navbar, 1 footer navbar, 1 content link. 0 DB transactions. Complexity Level 1 based on Fields = 3. |
| Iter2 | Examiner Login (shared) | Examiner | Uses staff authentication portal; role redirect to examiner dashboard | | | Minh | Doing | 4 | 0 | 60 | 2 inputs, 1 submit button, 1 home link. 0 DB transactions when loading (GET). Complexity Level 1 based on Fields = 4. |
| Iter2 | View Dashboard | Examiner | Dashboard with search, summary, candidate table | | | Minh | Done | 7 | 2 | 90 | 1 sidebar, 1 search input, 1 search button, 1 sorting headers row, 1 pagination row, 1 stats card, 1 row action. 2 DB transactions. Complexity Level 2 based on Fields = 7. |
| Iter2 | Call Candidate | Examiner | Queue list + call/print actions | | | Minh | Done | 7 | 1 | 90 | 1 sidebar, 1 search input, 1 search button, 1 section filter, 1 sorting headers row, 1 row actions group, 1 pagination. 1 DB transaction. Complexity Level 2 based on Fields = 7. |
| Iter2 | Candidate List (Edit Info) | Examiner | Search + list candidates for info correction | | | Minh | Done | 6 | 1 | 90 | 1 sidebar, 1 search input, 1 search button, 1 sorting headers row, 1 row actions group, 1 pagination. 1 DB transaction. Complexity Level 2 based on Fields = 6. |
| Iter2 | Candidate Detail View | Examiner | Read-only candidate profile + exam summary | | | Minh | Done | 3 | 1 | 60 | 1 sidebar, 1 details grid, 1 back button. 1 DB transaction. Complexity Level 1 based on Fields = 3. |
| Iter2 | Exam Paper View | Examiner | 35-question paper with image content + filters | | | Minh | Done | 4 | 1 | 60 | 1 sidebar, 1 questions grid, 1 filters group, 1 back button. 1 DB transaction. Complexity Level 1 based on Fields = 4. |
| Iter2 | Results List | Examiner | Search + list candidate results | | | Minh | Done | 5 | 1 | 60 | 1 sidebar, 1 search/filter inputs, 1 sorting row, 1 row actions group, 1 pagination. 1 DB transaction. Complexity Level 1 based on Fields = 5. |
| Iter2 | Edit / Change Score | Examiner | Adjust score with reason, password, captcha | | | Minh | Done | 6 | 2 | 90 | 1 sidebar, 1 fault list scoring, 1 reason inputs, 1 password input, 1 actions group, 1 SBD selector. 2 DB transactions. Complexity Level 2 based on Fields = 6. |
| Iter2 | View Audit Log | Examiner | Audit table with search, filter, pagination | | | Minh | Done | 5 | 1 | 60 | 1 sidebar, 1 search/filters group, 1 sorting row, 1 pagination, 1 submit button. 1 DB transaction. Complexity Level 1 based on Fields = 5. |
| Iter2 | Export Reports | Examiner | Export candidates/results/logs to file formats | | | Minh | Done | 4 | 1 | 60 | 1 sidebar, 1 select report, 1 format buttons, 1 submit button. 1 DB transaction. Complexity Level 1 based on Fields = 4. |
| Iter2 | Sidebar Navigation | Examiner | Shared examiner navigation layout | | | Minh | Done | 1 | 0 | 60 | 1 sidebar navigation panel. 0 DB transactions. Complexity Level 1 based on Fields = 1. |
