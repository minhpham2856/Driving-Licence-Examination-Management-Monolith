# Database Entity Specification

Database: **DLEM_DB_2** - Driving Licence Examination Management System  
Source schema: [web/WEB-INF/others/sql/DDL_DLEM_DB.sql](../web/WEB-INF/others/sql/DDL_DLEM_DB.sql)

| # | Entity | Description |
| --- | --- | --- |
| 1 | Role | Table storing user role types for system access control. Defines what permissions and access levels different system users have (e.g. Admin, Examiner, Exam Staff, Managing Staff, Registrant). |
| 2 | User | Table storing system user accounts with login credentials. Note that exam-day candidates do not have accounts and are identified by candidate number (SBD) instead of login credentials. |
| 3 | Profile | Table storing personal information for registered system users (registrants and staff). Linked one-to-one with User; represents identity data excluding exam-day candidate records. |
| 4 | DocumentType | Lookup table defining categories of documents that registrants may upload (e.g. ID card, medical certificate, application form). |
| 5 | Document | Table storing uploaded documents associated with user profiles. Each record references a document type, file URL, optional notes, and the owning Profile. |
| 6 | Licence | Table storing driving licence categories (e.g. A1, B1). Defines class name, description, minimum age, validity period, and optional upgrade path from another licence. |
| 7 | ExamRegistration | Table storing online exam registration requests submitted by registrants through the centre portal. Tracks registration status, notes, and the target licence class before a formal exam session assignment. |
| 8 | Exam | Table storing scheduled exam sessions. Each row represents one exam event with code, date, start/end time, status, centre name, and licence type. EndTime remains NULL until exam staff officially ends the session. |
| 9 | ExamSection | Table defining exam sections belonging to an exam session (e.g. theory, practical layout). Specifies section type, duration, and licence context per Exam. |
| 10 | ExamZone | Table storing exam campus or zone definitions (khuôn viên thi). Groups physical locations under a named zone with address and active flag. |
| 11 | ExamArea | Table storing specific exam locations (rooms, yards, stations) within an ExamZone. Defines area name, type, capacity, and physical location. |
| 12 | Exam_ExamArea | Junction table linking an Exam session to the ExamArea locations used for that session. Enables many-to-many assignment of areas to a single exam. |
| 13 | ExaminerSchedule | Table assigning examiners (Users) to an exam session. Records which examiner supervises which section and area, who assigned the schedule, and when. |
| 14 | ExamDevice | Table storing exam equipment at a specific ExamArea (computers for theory, motorcycles for practical). Tracks device name, type, and active status. |
| 15 | Candidate | Table storing exam-day candidate records imported or created for a session. Separate from User/Profile; identified by candidate number (SBD), with personal data, photo, take flags (theory/layout), attempt number, and absence/suspension status. |
| 16 | ExamEnrollment | Table linking a Candidate to an Exam session. Represents a candidate's participation in a specific exam, with optional area and device allocation at enrollment level. |
| 17 | ExamEnrollmentSection | Table tracking a candidate's progress per exam section within an enrollment. Stores section status (e.g. Pending, In Progress, Completed), allocation (area, device), timestamps, and allocating staff. |
| 18 | Fee | Lookup table defining fee items (name, type) that may be charged during exam registration or payment processing. |
| 19 | Payment | Table storing payment transactions for an ExamEnrollment. Records status, method, transaction reference, total amount, and payment timestamp. |
| 20 | Payment_Fee | Junction table linking a Payment to one or more Fee line items, breaking down what charges were included in a transaction. |
| 21 | Licence_Fee | Table mapping fee amounts to licence categories. Defines how much each Fee costs for a given Licence (or globally when LicenceId is NULL). |
| 22 | QuestionCategory | Lookup table grouping theory exam questions by subject or topic area. |
| 23 | Question | Table storing theory exam questions. Includes question number, optional image, correct answer, critical-flag, and category reference. |
| 24 | Licence_Question | Junction table associating questions with licence categories. Defines which questions are eligible for theory papers of each licence class. |
| 25 | TheoryPaper | Table representing an individual candidate's generated theory exam paper for one ExamEnrollmentSection. Tracks when the paper was started and submitted. |
| 26 | CandidateAnswer | Table storing a candidate's selected answer for each question on a TheoryPaper. One row per question per paper. |
| 27 | ExamResult | Table storing the overall pass/fail outcome for an ExamEnrollment. One final result record per candidate per exam session. |
| 28 | ExamScore | Table storing section-level scores within an ExamResult (theory score, practical score). One score per exam section per result. |
| 29 | ScoreDeduction | Master table of practical exam violation rules per licence and section. Defines deduction reason, points, critical flag, and applicable ExamSection. |
| 30 | DeductionRecord | Table recording actual violation deductions applied to a candidate's ExamScore. Links a score to a ScoreDeduction rule with occurrence count and timestamp. |
| 31 | Audit | Table storing system audit log entries. Records user actions, entity changes (old/new values), reasons, and timestamps for security and compliance tracking. |
