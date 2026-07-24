from pathlib import Path
root = Path(r"c:\Users\admin\Documents\University\Term5\SWP391\Project\Driving-Licence-Examination-Management-Monolith")
p = root / "src/java/examiner/service/impl/ExamViewServiceImpl.java"
text = p.read_text(encoding="utf-8")
if "private static String resolveStatusKey" in text:
    print("method exists")
else:
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
    marker = "    // Converts section status string to CandidateStatus enum, defaulting to\n    // NOT_STARTED.\n    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {"
    text = text.replace(marker, helpers + marker)
    p.write_text(text, encoding="utf-8", newline="\n")
    print("inserted")
