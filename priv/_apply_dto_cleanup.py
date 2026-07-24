from pathlib import Path
root = Path(r"c:\Users\admin\Documents\University\Term5\SWP391\Project\Driving-Licence-Examination-Management-Monolith")

jsp_replacements = [
    ("${c.sbd}", "${c.candidateNumber}"),
    ("${candidate.sbd}", "${candidate.candidateNumber}"),
    ("requestScope.candidate.sbd", "requestScope.candidate.candidateNumber"),
    ("candidate.sbd", "candidate.candidateNumber"),
]
jsp_files = [
    "web/views/examiner/components/candidate-list.jsp",
    "web/views/examiner/candidate-details.jsp",
    "web/views/examiner/components/toolbar.jsp",
    "web/views/examiner/score-entry.jsp",
    "web/views/examiner/result-details-edit.jsp",
    "web/views/examiner/components/faults.jsp",
    "web/views/examiner/candidate-paper.jsp",
]
for rel in jsp_files:
    p = root / rel
    text = p.read_text(encoding="utf-8")
    orig = text
    for old, new in jsp_replacements:
        text = text.replace(old, new)
    if text != orig:
        p.write_text(text, encoding="utf-8", newline="\n")
        print("updated", rel)

p = root / "src/java/examiner/service/impl/ActionServiceImpl.java"
text = p.read_text(encoding="utf-8")
repls = [
    ("enrollment.getId()", "enrollment.getCandidateId()"),
    ("enrollment.getCandidateNo()", "enrollment.getCandidateNumber()"),
    ("CandidateStatus.COMPLETED.getValue().equals(enrollment.getSectionStatus())", "enrollment.getSectionStatus() == CandidateStatus.COMPLETED"),
    ("CandidateStatus.AWAITING_SIGNATURE.getValue().equals(enrollment.getSectionStatus())", "enrollment.getSectionStatus() == CandidateStatus.AWAITING_SIGNATURE"),
    ("!CandidateStatus.AWAITING_SIGNATURE.getValue().equals(enrollment.getSectionStatus())", "enrollment.getSectionStatus() != CandidateStatus.AWAITING_SIGNATURE"),
    ("CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.NOT_STARTED", "(enrollment.getSectionStatus() == null || enrollment.getSectionStatus() == CandidateStatus.NOT_STARTED)"),
    ("CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.IN_PROGRESS", "enrollment.getSectionStatus() == CandidateStatus.IN_PROGRESS"),
    ("CandidateStatus current = CandidateStatus.fromValue(enrollment.getSectionStatus());", "CandidateStatus current = enrollment.getSectionStatus() != null ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED;"),
    ("int enrollmentId = enrollment.getEnrollment() != null ? enrollment.getEnrollment().getExamEnrollmentId() : 0;", "int enrollmentId = enrollment.getExamEnrollmentId();"),
]
for a,b in repls:
    text = text.replace(a,b)
p.write_text(text, encoding="utf-8", newline="\n")
print("updated ActionServiceImpl")

for rel in ["src/java/examiner/controller/PrintServlet.java", "src/java/examiner/controller/ExportServlet.java"]:
    p = root / rel
    text = p.read_text(encoding="utf-8").replace("enrollment.getId()", "enrollment.getCandidateId()")
    p.write_text(text, encoding="utf-8", newline="\n")
    print("updated", rel)

p = root / "src/java/examiner/service/impl/DispatchServiceImpl.java"
text = p.read_text(encoding="utf-8")
text = text.replace("if (enrollment != null && enrollment.getEnrollment() != null) {", "if (enrollment != null && enrollment.getExamEnrollmentId() > 0) {")
text = text.replace("enrollment.getEnrollment().getExamEnrollmentId()", "enrollment.getExamEnrollmentId()")
text = text.replace("setPromotedSbd(", "setPromotedCandidateNumber(")
p.write_text(text, encoding="utf-8", newline="\n")
print("updated DispatchServiceImpl")

section_expr = "ctx.section() != null ? ctx.section().getValue() : SectionType.LAYOUT.getValue()"
for rel in ["src/java/examiner/service/impl/ExcelServiceImpl.java", "src/java/examiner/service/impl/DocxServiceImpl.java"]:
    p = root / rel
    text = p.read_text(encoding="utf-8")
    text = text.replace("ctx.sectionTypeValue()", section_expr)
    text = text.replace("enrollment.getId()", "enrollment.getCandidateId()")
    p.write_text(text, encoding="utf-8", newline="\n")
    print("updated sectionType", rel)

p = root / "src/java/examiner/service/impl/ExcelServiceImpl.java"
text = p.read_text(encoding="utf-8")
text = text.replace("payload.primaryHeaders(), payload.primaryRows(), out);",
                    "payload.tables().get(0).headers(), payload.tables().get(0).rows(), out);")
text = text.replace("exportToExcel(payload.excelSheetName(), payload.primaryHeaders(),\n                    payload.primaryRows(), out);",
                    "exportToExcel(payload.excelSheetName(), payload.tables().get(0).headers(),\n                    payload.tables().get(0).rows(), out);")
text = text.replace("payload.toXmlDocument()", "new XmlExportDocument(payload.xmlRootElement(), payload.metadata(), payload.tables())")
p.write_text(text, encoding="utf-8", newline="\n")
print("updated ExcelServiceImpl export")

p = root / "src/java/examiner/service/impl/DocxServiceImpl.java"
text = p.read_text(encoding="utf-8")
if "payload.toXmlDocument()" in text:
    text = text.replace("payload.toXmlDocument()", "new XmlExportDocument(payload.xmlRootElement(), payload.metadata(), payload.tables())")
    if "import examiner.dto.XmlExportDocument;" not in text:
        text = text.replace("import examiner.dto.ExportPayloadDTO;", "import examiner.dto.ExportPayloadDTO;\nimport examiner.dto.XmlExportDocument;")
    p.write_text(text, encoding="utf-8", newline="\n")
    print("updated DocxServiceImpl xml")
else:
    print("Docx toXmlDocument not found", "toXmlDocument" in text)
