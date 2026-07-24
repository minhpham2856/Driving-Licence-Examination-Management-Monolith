from pathlib import Path
root = Path(r"c:\Users\admin\Documents\University\Term5\SWP391\Project\Driving-Licence-Examination-Management-Monolith")

# Fix ActionServiceImpl broken negations
p = root / "src/java/examiner/service/impl/ActionServiceImpl.java"
text = p.read_text(encoding="utf-8")
text = text.replace("!enrollment.getSectionStatus() == CandidateStatus.AWAITING_SIGNATURE",
                    "enrollment.getSectionStatus() != CandidateStatus.AWAITING_SIGNATURE")
p.write_text(text, encoding="utf-8", newline="\n")
print("fixed ActionServiceImpl negations")

# Add SectionType import to Excel and Docx if missing
for rel in ["src/java/examiner/service/impl/ExcelServiceImpl.java", "src/java/examiner/service/impl/DocxServiceImpl.java"]:
    p = root / rel
    text = p.read_text(encoding="utf-8")
    if "import shared.enums.SectionType;" not in text:
        text = text.replace("import shared.enums.FileType;", "import shared.enums.FileType;\nimport shared.enums.SectionType;")
        p.write_text(text, encoding="utf-8", newline="\n")
        print("added SectionType import", rel)

# ExamViewServiceImpl bulk replacements
p = root / "src/java/examiner/service/impl/ExamViewServiceImpl.java"
text = p.read_text(encoding="utf-8")
repls = [
    ("if (enrollment == null || enrollment.getEnrollment() == null) {", "if (enrollment == null || enrollment.getExamEnrollmentId() <= 0) {"),
    ("int enrollmentId = enrollment.getEnrollment().getExamEnrollmentId();", "int enrollmentId = enrollment.getExamEnrollmentId();"),
    ("activeReg.getId()", "activeReg.getCandidateId()"),
    ("if (activeReg != null && activeReg.getEnrollment() != null) {", "if (activeReg != null && activeReg.getExamEnrollmentId() > 0) {"),
    ("activeReg.getEnrollment().getExamDeviceId()", "activeReg.getExamDeviceId()"),
    ("enrollment.getId()", "enrollment.getCandidateId()"),
    ("int enrollmentId = enrollment.getEnrollment() != null ? enrollment.getEnrollment().getExamEnrollmentId() : 0;", "int enrollmentId = enrollment.getExamEnrollmentId();"),
    ("row.setGovernmentId(enrollment.getGovIdNo());", "row.setGovernmentId(enrollment.getGovernmentIdNumber());"),
    ("row.setPhoneNo(enrollment.getPhoneNo());", "row.setPhoneNo(enrollment.getPhoneNumber());"),
    ("row.setDob(formatDate(enrollment.getDob()));", "row.setDob(formatDate(enrollment.getDateOfBirth()));"),
    ("row.setDobRaw(formatDateRaw(enrollment.getDob()));", "row.setDobRaw(formatDateRaw(enrollment.getDateOfBirth()));"),
    ("        if (enrollment.getCandidate() != null) {\n            row.setPhotoImageUrl(enrollment.getCandidate().getPhotoImageUrl());\n        }", "        row.setPhotoImageUrl(enrollment.getPhotoImageUrl());"),
    ("        if (practicalSection && enrollmentId > 0 && enrollment.getCandidate() != null) {\n            practicalEntryAllowed = sectionProgressService.isPracticalEntryAllowed(\n                    enrollmentId,\n                    enrollment.getCandidate().getTakeTheory(),\n                    enrollment.getCandidate().getTakeLayout());", "        if (practicalSection && enrollmentId > 0 && enrollment.getCandidateId() > 0) {\n            practicalEntryAllowed = sectionProgressService.isPracticalEntryAllowed(\n                    enrollmentId,\n                    enrollment.isTakeTheory(),\n                    enrollment.isTakeLayout());"),
    ("Integer deviceId = enrollment.getEnrollment() != null ? enrollment.getEnrollment().getExamDeviceId() : null;", "Integer deviceId = enrollment.getExamDeviceId();"),
    ("return !CandidateStatus.COMPLETED.getValue().equals(enrollment.getSectionStatus());", "return enrollment.getSectionStatus() != CandidateStatus.COMPLETED;"),
]
for a,b in repls:
    text = text.replace(a,b)

# isScoreQueueEligible
old = """        String status = enrollment.getSectionStatus();
        // Must not be COMPLETED or AWAITING_SIGNATURE
        return !CandidateStatus.COMPLETED.getValue().equals(status)
                && !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(status);"""
new = """        CandidateStatus status = enrollment.getSectionStatus();
        // Must not be COMPLETED or AWAITING_SIGNATURE
        return status != CandidateStatus.COMPLETED
                && status != CandidateStatus.AWAITING_SIGNATURE;"""
text = text.replace(old, new)

# sectionStatusOf
old_section = """    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {
        String normalized = SectionStatusUtil.normalize(enrollment.getSectionStatus());
        CandidateStatus status = CandidateStatus.fromValue(normalized);
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }"""
new_section = """    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {
        CandidateStatus status = enrollment.getSectionStatus();
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }"""
text = text.replace(old_section, new_section)

# buildCandidateRow sex + status fields
old_sex = "        row.setSex(enrollment.isSex() ? shared.enums.Sex.FEMALE : shared.enums.Sex.MALE);"
new_sex = """        Sex sex = enrollment.isSex() ? Sex.FEMALE : Sex.MALE;
        row.setSex(sex);"""
text = text.replace(old_sex, new_sex)

old_after_suspend = """        row.setSectionStatus(sectionStatus);
        row.setSuspended(enrollment.isSuspended());
        row.setAbsent(enrollment.isAbsent());"""
new_after_suspend = """        row.setSectionStatus(sectionStatus);
        row.setSuspended(enrollment.isSuspended());
        row.setStatus(resolveStatusKey(sectionStatus, enrollment.isSuspended()));
        row.setStatusLabel(resolveStatusLabel(sectionStatus, enrollment.isSuspended()));
        row.setSexValue(sex == Sex.FEMALE ? \"1\" : \"0\");
        row.setSexLabel(resolveSexLabel(sex));
        row.setAwaitingSignature(sectionStatus == CandidateStatus.AWAITING_SIGNATURE);
        row.setAbsent(enrollment.isAbsent());"""
text = text.replace(old_after_suspend, new_after_suspend)

# imports
if "import shared.enums.Sex;" not in text:
    text = text.replace("import shared.enums.SectionType;", "import shared.enums.SectionType;\nimport shared.enums.Sex;")
if "import java.sql.Timestamp;" not in text:
    text = text.replace("import java.sql.Date;", "import java.sql.Date;\nimport java.sql.Timestamp;")

# Remove SectionStatusUtil if unused
if "SectionStatusUtil" not in text.replace("import shared.util.SectionStatusUtil;\n", ""):
    text = text.replace("import shared.util.SectionStatusUtil;\n", "")
elif "SectionStatusUtil." not in text:
    text = text.replace("import shared.util.SectionStatusUtil;\n", "")

# Add Timestamp overloads before formatDate(Date)
if "private String formatDate(Timestamp timestamp)" not in text:
    insert = """
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return \"-\";
        }
        return formatDate(new Date(timestamp.getTime()));
    }

    private String formatDateRaw(Timestamp timestamp) {
        if (timestamp == null) {
            return \"\";
        }
        return formatDateRaw(new Date(timestamp.getTime()));
    }

"""
    marker = "    // Formats a Date to dd/MM/yyyy\n    private String formatDate(Date date) {"
    text = text.replace(marker, insert + marker)

# Add resolve helpers before sectionStatusOf
if "resolveStatusKey" not in text:
    helpers = """
    private static String resolveStatusKey(CandidateStatus sectionStatus, boolean suspended) {
        if (suspended) {
            return \"suspended\";
        }
        if (sectionStatus == null) {
            return \"pending\";
        }
        if (sectionStatus == CandidateStatus.COMPLETED) {
            return \"done\";
        }
        if (sectionStatus == CandidateStatus.AWAITING_SIGNATURE) {
            return \"awaiting\";
        }
        if (sectionStatus == CandidateStatus.IN_PROGRESS) {
            return \"testing\";
        }
        return \"pending\";
    }

    private static String resolveStatusLabel(CandidateStatus sectionStatus, boolean suspended) {
        if (suspended) {
            return \"\u0110\u00ecnh ch\u1ec9\";
        }
        if (sectionStatus == null) {
            return CandidateStatus.NOT_STARTED.getValue();
        }
        return sectionStatus.getValue();
    }

    private static String resolveSexLabel(Sex sex) {
        if (sex == null) {
            return \"-\";
        }
        return sex.getValue();
    }

"""
    text = text.replace("    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {", helpers + "    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {")

p.write_text(text, encoding="utf-8", newline="\n")
print("updated ExamViewServiceImpl")

# Check remaining old patterns in target files
import subprocess
files = [
    "src/java/examiner/service/impl/ExamViewServiceImpl.java",
    "src/java/examiner/service/impl/ActionServiceImpl.java",
    "src/java/examiner/service/impl/DispatchServiceImpl.java",
    "src/java/examiner/service/impl/DocxServiceImpl.java",
    "src/java/examiner/service/impl/ExcelServiceImpl.java",
    "src/java/examiner/controller/PrintServlet.java",
    "src/java/examiner/controller/ExportServlet.java",
]
for rel in files:
    t = (root / rel).read_text(encoding="utf-8")
    for pat in ["getEnrollment()", "getCandidate()", "getId()", "getGovIdNo", "getDob()", "getPhoneNo()", "sectionTypeValue()", "primaryHeaders", "toXmlDocument", "setPromotedSbd"]:
        if pat in t:
            print("REMAIN", rel, pat)
