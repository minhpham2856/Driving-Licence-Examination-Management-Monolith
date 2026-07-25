| No | Entity | Description |
| --- | --- | --- |
| 1 | Registrant | Person who registers for an exam; owns a personal profile and submits exam applications. |
| 2 | Profile | Identity profile of a Registrant (full name, government ID, contact details). |
| 3 | Document | Supporting file attached to a Profile (ID card, health certificate, portrait photo, etc.). |
| 4 | ExamRegistration | Exam application for a licence class; tracks dossier processing status. |
| 5 | Licence | Driving-licence class (A1, A, B1, …) used as the basis for registration and exams. |
| 6 | ExamDates | Tentative exam date opened by ManagingStaff; has open/locked/cancelled status and police submission status. |
| 7 | RegistrationDates | Link of a Registrant choosing a tentative exam date; PoliceStaff approves or rejects each dossier. |
| 8 | ManagingStaff | Managing officer who opens/cancels tentative exam dates and creates official exam sessions. |
| 9 | PoliceStaff | Traffic-police officer who reviews RegistrationDates and finalizes the official candidate roster. |
| 10 | ExamStaff | Exam-day staff who manage candidates, exam areas, and on-site coordination. |
| 11 | Examiner | Examiner assigned to an exam/section; grades scores, applies deductions, and records violations. |
| 12 | Exam | Official exam session created from ExamDates; has exam code, date/time, and status. |
| 13 | Candidate | Exam-day sitter (candidate number, name, absence, suspension); separate from Registrant/Profile. |
| 14 | ExamSection | Section within an Exam (Theory, Layout practical, etc.). |
| 15 | ExamZone | Campus / large exam zone that contains ExamAreas. |
| 16 | ExamArea | Specific room or yard under an ExamZone; assigned to an Exam and ExamSection. |
| 17 | ExamResult | Overall result of a Candidate in an Exam (pass/fail). |
| 18 | ExamScore | Score of one ExamSection under an ExamResult. |
| 19 | Deduction | Score deduction (reason, points) recorded when an Examiner grades a section. |
| 20 | Violation | Serious candidate violation during a section (reason, evidence image). |
