```mermaid
erDiagram
    ManagingStaff {
    }

    ExamStaff {
    }

    Examiner {
    }

    PoliceStaff {
    }

    Registrant {
    }

    Profile {
        int ProfileId PK
        nvarchar(255) FullName
        nvarchar(100) GovernmentIdNumber
    }

    Document {
        int DocumentId PK
        nvarchar(100) DocumentType
        nvarchar(500) DocumentUrl
    }

    Licence {
        int LicenceId PK
        nvarchar(50) LicenceClass
    }

    ExamRegistration {
        int ExamRegistrationId PK
        nvarchar(50) RegistrationStatus
    }

    ExamDates {
        int ExamDateId PK
        date ExamDate
        nvarchar(20) Status
        nvarchar(20) PoliceStatus
    }

    RegistrationDates {
        int RegistrationDateId PK
        bit IsActive
        nvarchar(20) PoliceStatus
        nvarchar(50) OfficialCandidateNumber
    }

    Exam {
        int ExamId PK
        nvarchar(50) ExamCode
        datetime ExamDate
        nvarchar(50) Status
    }

    Candidate {
        int CandidateId PK
        nvarchar(50) CandidateNumber
        nvarchar(255) FullName
        bit IsAbsent
        bit IsSuspended
    }

    ExamSection {
        int ExamSectionId PK
        nvarchar(100) SectionType
    }

    ExamZone {
        int ExamZoneId PK
        nvarchar(100) ZoneName
    }

    ExamArea {
        int ExamAreaId PK
        nvarchar(100) AreaName
        nvarchar(50) AreaType
    }

    ExamResult {
        int ExamResultId PK
        bit IsPassed
    }

    ExamScore {
        int ExamScoreId PK
        decimal(5,2) Score
    }

    Deduction {
        int DeductionId PK
        nvarchar(500) Reason
        decimal(5,2) Points
    }

    Violation {
        int ViolationId PK
        nvarchar(100) Reason
        nvarchar(500) EvidenceUrl
    }

    Registrant ||--|| Profile : "has"
    Profile ||--o{ Document : "submits"
    Profile ||--o{ ExamRegistration : "applies"
    Licence ||--o{ ExamRegistration : "for"
    Licence ||--o{ ExamDates : "for"
    Licence ||--o{ Exam : "for"
    Licence ||--o{ Deduction : "defines"

    ManagingStaff ||--o{ ExamDates : "opens"
    ManagingStaff ||--o{ Exam : "creates"
    PoliceStaff ||--o{ RegistrationDates : "reviews"
    PoliceStaff ||--o{ ExamDates : "completes"
    ExamStaff ||--o{ ExamArea : "prepares"
    ExamStaff ||--o{ Candidate : "manages"
    Examiner ||--o{ Exam : "assigned to"
    Examiner ||--o{ ExamSection : "covers"
    Examiner ||--o{ ExamScore : "grades"
    Examiner ||--o{ Deduction : "applies"
    Examiner ||--o{ Violation : "records"

    ExamRegistration ||--o{ RegistrationDates : "chooses"
    ExamDates ||--o{ RegistrationDates : "receives"
    ExamDates ||--o| Exam : "becomes"
    RegistrationDates ||--o{ Candidate : "becomes"
    ExamRegistration ||--o{ Candidate : "may map to"

    Exam ||--|{ ExamSection : "has"
    ExamZone ||--o{ ExamArea : "contains"
    Exam ||--o{ ExamArea : "uses"
    ExamArea ||--o{ ExamSection : "hosts"

    Candidate ||--o{ Exam : "sits"
    Candidate ||--o{ ExamSection : "attempts"
    Candidate ||--o| ExamResult : "receives"
    Exam ||--o{ ExamResult : "produces"
    ExamResult ||--o{ ExamScore : "has"
    ExamSection ||--o{ ExamScore : "for"
    ExamScore ||--o{ Deduction : "includes"
    Candidate ||--o{ Violation : "may have"
    ExamSection ||--o{ Violation : "during"
```
